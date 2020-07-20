package com.changgou.system.filter;


import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关ip过滤器
 * 记录访问者的ip地址，供大数据分析使用
 */
@Component
public class IpFilter implements GlobalFilter, Ordered {
    /**
     * 记录访问这的ip地址
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求
        ServerHttpRequest request = exchange.getRequest();
        String hostAddress = request.getRemoteAddress().getHostString();
        return chain.filter(exchange);//必须把请求原样放回去让它继续执行
    }

    /**
     * 多个过滤器的先后执行顺序，按数字先后执行
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
