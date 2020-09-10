package com.changgou.web.gateway.filter;


import com.changgou.web.gateway.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    /**
     * 业务逻辑：
     *
     * 1）判断当前请求是否为登录请求，是的话，则放行
     *
     * 2) 判断cookie中是否存在信息, 没有的话，拒绝访问
     *
     * 3）判断redis中令牌是否过期，过期的话，拒绝访问
     *
     * 4）判断请求头上是否存在令牌，不存在，拒绝访问
     */

    public static final String Authorization = "Authorization";

    @Autowired
    private AuthService authService;

    @Override
    //业务逻辑
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //获取当前请求路径
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String path = request.getURI().getPath();
        if( "/api/oauth/login".equals(path) || !URLFilter.hasAuthorize(path)){
            //放行
            return chain.filter(exchange);
        }

        //判断cookie中是否存在jti
        String jti = authService.getJtiFromCookie(request);
        if (StringUtils.isEmpty(jti)){
            //拒绝访问,请求跳转
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //判断redis中是否存在jwt
        String jwt = authService.getJwtFromRedis(jti);
        if (StringUtils.isEmpty(jwt)){
            //拒绝访问，请求跳转
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //校验通过 , 请求头增强，放行(将长令牌放入请求头)
        request.mutate().header(Authorization,"Bearer "+jwt);
        return chain.filter(exchange);
    }

    @Override
    //设置过滤器执行顺序
    public int getOrder() {
        return 0;
    }
}
