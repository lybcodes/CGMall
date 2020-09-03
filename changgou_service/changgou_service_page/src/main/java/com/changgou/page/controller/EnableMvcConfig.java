package com.changgou.page.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * resource目录下只有static下的静态资源可以通过浏览器直接访问，
 * template下的页面不能通过浏览器直接访问，这个类就是用来放行的
 */
@ControllerAdvice
@Configuration
public class EnableMvcConfig implements WebMvcConfigurer {

    /***
     * 静态资源放行
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //如果浏览器中访问的uri地址中有/items/**则放行，放行的文件在/template/items/文件夹下
        registry.addResourceHandler("/items/**").addResourceLocations("classpath:/templates/items/");
    }
}