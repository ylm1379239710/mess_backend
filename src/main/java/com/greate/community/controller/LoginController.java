package com.greate.community.controller;

import com.google.code.kaptcha.Producer;
import com.greate.community.commons.result.ActionResult;
import com.greate.community.commons.result.ResultBuilder;
import com.greate.community.entity.User;
import com.greate.community.service.UserService;
import com.greate.community.util.CommunityConstant;
import com.greate.community.util.CommunityUtil;
import com.greate.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 登录、登出、注册
 */
@RestController
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * 注册用户
     * @param user
     * @return
     */
    @PostMapping("/register")
    public ActionResult register(@RequestBody User user) {
        System.out.println(user);
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            return ResultBuilder.buildActionSuccess();
        } else {
            return ResultBuilder.buildActionFail(map, HttpStatus.ACCEPTED.value());
        }
    }

    /**
     * 激活用户
     * @param model
     * @param userId
     * @param code 激活码
     * @return
     * http://localhost:8080/echo/activation/用户id/激活码
     */
    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") int userId,
                             @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            return "激活成功, 您的账号已经可以正常使用!";
        }
        else if (result == ACTIVATION_REPEAT) {
            return "无效的操作, 您的账号已被激活过!";
        }
        return "未知错误";
    }


    /**
     * 生成验证码, 并存入 Redis
     * @param response
     */
    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response) {
        // 生成验证码
        String text = kaptchaProducer.createText(); // 生成随机字符
        System.out.println("验证码：" + text);
        BufferedImage image = kaptchaProducer.createImage(text); // 生成图片
        
        // 验证码的归属者
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // 将验证码存入 redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败", e.getMessage());
        }
    }

    /**
     * 验证用户输入的图片验证码是否和redis中存入的是否相等
     *
     * @param kaptchaOwner 从 cookie 中取出的 kaptchaOwner
     * @param checkCode 用户输入的图片验证码
     * @return 失败则返回原因, 验证成功返回 "",
     */
    @GetMapping("/checkKaptchaCode")
    private ActionResult checkKaptchaCode(@CookieValue("kaptchaOwner") String kaptchaOwner, @RequestParam("checkCode") String checkCode) {
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        String kaptchaValue = (String) redisTemplate.opsForValue().get(redisKey);
        if (StringUtils.isBlank(kaptchaValue)) {
            return ResultBuilder.buildActionFail(null,"图形验证码过期");
        } else if (!kaptchaValue.equalsIgnoreCase(checkCode)) {
            return ResultBuilder.buildActionFail(null,"图形验证码错误");
        }
        return ResultBuilder.buildActionSuccess();
    }

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @param rememberMe 是否记住我（点击记住我后，凭证的有效期延长）
     * @param response
     * @return
     */
    @PostMapping("/login")
    public ActionResult login(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               @RequestParam(value = "rememberMe", required = false) boolean rememberMe,
                               HttpServletResponse response) {
        // 凭证过期时间（是否记住我）
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        // 验证用户名和密码
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            // 账号和密码均正确，则服务端会生成 ticket，浏览器通过 cookie 存储 ticket
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath); // cookie 有效范围
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return ResultBuilder.buildActionSuccess(null,"登陆成功");
        }
        else {
            if(map.containsKey("usernameMsg")){
                return ResultBuilder.buildActionFail(map,map.get("usernameMsg").toString());
            }else if(map.containsKey("passwordMsg")){
                return ResultBuilder.buildActionFail(map,map.get("passwordMsg").toString());
            }else {
                return ResultBuilder.buildActionFail();
            }

        }

    }

    /**
     * 用户登出
     * @param ticket 设置凭证状态为无效
     * @return
     */
    @GetMapping("/logout")
    public ActionResult logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return ResultBuilder.buildActionSuccess();
    }

    /**
     * 重置密码
     */
    @PostMapping("/resetPwd")
    public ActionResult resetPwd(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @RequestParam("emailVerifyCode") String emailVerifyCode) {
        Map<String, Object> map = new HashMap<>(4);
        // 检查邮件验证码
        String emailVerifyCodeCheckRst = checkRedisResetPwdEmailCode(username, emailVerifyCode);
        if (StringUtils.isNotBlank(emailVerifyCodeCheckRst)) {
            return ResultBuilder.buildActionFail(null,emailVerifyCodeCheckRst);
        }
        // 执行重置密码操作
        Map<String, Object> stringObjectMap = userService.doResetPwd(username, password);
        String usernameMsg = (String) stringObjectMap.get("errMsg");
        if (StringUtils.isBlank(usernameMsg)) {
            return ResultBuilder.buildActionSuccess(null,"重置密码成功!");
        }else {
            return ResultBuilder.buildActionFail(null,usernameMsg);
        }
    }

    /**
     * 发送邮件验证码(用于重置密码)
     * @param username 用户输入的需要找回的账号
     */
    @PostMapping("/sendEmailCodeForResetPwd")
    public ActionResult sendEmailCodeForResetPwd(@RequestParam("username") String username) {
        Map<String, Object> map = new HashMap<>(3);
        Map<String, Object> stringObjectMap = userService.doSendEmailCode4ResetPwd(username);
        String usernameMsg = (String) stringObjectMap.get("errMsg");
        if (StringUtils.isBlank(usernameMsg)) {
            return ResultBuilder.buildActionSuccess(null,"已经往您的邮箱发送了一封验证码邮件, 请查收!");
        }
        return ResultBuilder.buildActionFail(null,usernameMsg);
    }

    /**
     * 检查 邮件 验证码
     *
     * @param username 用户名
     * @param checkCode 用户输入的图片验证码
     * @return 验证成功 返回"", 失败则返回原因
     */
    private String checkRedisResetPwdEmailCode(String username, String checkCode) {
        if (StringUtils.isBlank(checkCode)) {
            return "未发现输入的邮件验证码";
        }
        final String redisKey = "EmailCode4ResetPwd:" + username;
        String emailVerifyCodeInRedis = (String) redisTemplate.opsForValue().get(redisKey);
        if (StringUtils.isBlank(emailVerifyCodeInRedis)) {
            return "邮件验证码已过期";
        } else if (!emailVerifyCodeInRedis.equalsIgnoreCase(checkCode)) {
            return "邮件验证码错误";
        }
        return "";
    }


}
