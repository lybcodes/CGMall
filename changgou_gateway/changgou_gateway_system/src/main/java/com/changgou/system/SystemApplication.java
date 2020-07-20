package com.changgou.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableEurekaClient
public class SystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
    }


    /**
     * 初始化ipkeyResolver,使用redis的令牌桶算法限流
     */
    @Bean
    public KeyResolver ipKeyResolver(){

        return new KeyResolver() {
            @Override
            public Mono<String> resolve(ServerWebExchange exchange) {
                //获取访问者的ip地址返回，按照Ip地址限流
                return Mono.just(exchange.getRequest().getRemoteAddress().getHostString());
            }
        };
    }
}
