package com.changgou.system.filter;


import com.changgou.system.filter.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


/**
 * 自定义网关过滤器 鉴权
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered
{
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1、获取请求
        ServerHttpRequest request = exchange.getRequest();
        //2、获取相应
        ServerHttpResponse response = exchange.getResponse();
        //3、从请求中获取访问者的uri地址
        String uri = request.getURI().getPath();
        //4、判断uri地址 如果是登录或注册 放行
        if(uri.contains("admin/login")){
            return chain.filter(exchange);
        }
        //5、从请求中获取jwt令牌
        String token = request.getHeaders().getFirst("token");
        //6、如果jwt令牌获取不到 拦截
        if(StringUtils.isEmpty(token)){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //返回响应
            return response.setComplete();
        }
        try {
            //7、解析令牌
            JwtUtil.parseJWT(token);
            //8、解析正确 放行
           return chain.filter(exchange);
        } catch (Exception e) {
            e.printStackTrace();
            //9、解析出错 拦截
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //返回响应
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
