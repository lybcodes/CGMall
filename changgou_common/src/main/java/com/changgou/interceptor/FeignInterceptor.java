package com.changgou.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * 自定义拦截器, 拦截所有请求
 * 每次微服务调用之前都先检查下头文件，将请求的头文件中的令牌数据再放入到header中
 */
@Component
public class FeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        //传递当前的令牌
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if(requestAttributes != null){
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            if(request != null){
                //拿到当前请求所有的头信息
                Enumeration<String> headerNames = request.getHeaderNames();
                if(headerNames != null){
                    while(headerNames.hasMoreElements()){
                        String headerName = headerNames.nextElement();
                        if("authorization".equals(headerName)){
                            //将对应的Jwt令牌取出来
                            String headerValue = request.getHeader(headerName);//Bearer jwt

                            //向下传递令牌(最关键的代码)
                            requestTemplate.header(headerName, headerValue);
                        }
                    }
                }
            }
        }
    }
}
