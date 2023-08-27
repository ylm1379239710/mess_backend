package com.greate.community.config;

import com.greate.community.controller.interceptor.DataInterceptor;
import com.greate.community.controller.interceptor.LoginTicketInterceptor;
import com.greate.community.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Autowired
    private DataInterceptor dataInterceptor;

    // 对除静态资源外所有路径进行拦截
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/img/**","/error","/login","/register","/banner/findAllBanner","/home","/kaptcha","/checkKaptchaCode","/activation/**","/discussPost/findDiscussPostByCondition","/blackList/findBlackListByCondition");

        registry.addInterceptor(messageInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/img/**","/error");

        registry.addInterceptor(dataInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/img/**","/error");
    }

//    // 配置虚拟路径映射访问
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry){
//        // System.getProperty("user.dir") 获取程序的当前路径
//        String path = System.getProperty("user.dir")+"\\src\\main\\resources\\static\\editor-md-upload\\";
//        registry.addResourceHandler("/editor-md-upload/**").addResourceLocations("file:" + path);
//    }
    // 解决跨域
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")//设置允许跨域的路径
                //如果有多个路径需要跨域，只需要将跨域路径放入数组中
                //String []  allowDomain={"http://**","http://*","http://*"};
                //.allowedOrigins(allowDomain)//多url跨域
                .allowedOrigins("*")//设置允许跨域请求的域名
                .allowedHeaders("*")
                .allowCredentials(true)//是否允许证书 不写默认开启
                .allowedMethods("GET","POST","PUT","OPTIONS","DELETE","PATCH") //设置允许的方法
                .maxAge(3600);//跨域允许时间
    }
}
