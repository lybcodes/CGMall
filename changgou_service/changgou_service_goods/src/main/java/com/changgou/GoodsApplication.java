package com.changgou;

import com.changgou.util.IdWorker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableEurekaClient
@MapperScan(basePackages = {"com.changgou.goods.dao"})
public class GoodsApplication {

    @Value("${workerId}")
    private long workerId;

    @Value("${datacenterId}")
    private long datacenterId;


    public static void main(String[] args) {
        SpringApplication.run( GoodsApplication.class);
    }

    /**
     * 初始化分布式ID生成器雪花算法，交给spring管理
     * @return
     */
    @Bean
    public IdWorker initIdWorker(){
        return new IdWorker(workerId, datacenterId);
    }
}
