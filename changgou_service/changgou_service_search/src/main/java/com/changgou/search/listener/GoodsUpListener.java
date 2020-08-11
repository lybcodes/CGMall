package com.changgou.search.listener;

import com.changgou.search.service.EsManagerService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 商品上架监听器
 * 监听来自于rabbitmq中的商品id, 根据商品id, 查询商品所有库存数据
 * 将库存数据放入ES索引库中供消费者搜索使用
 * @author ZJ
 */
@Component
@RabbitListener(queues = "search_add_queue")
public class GoodsUpListener {

    @Autowired
    private EsManagerService esManagerService;

    @RabbitHandler
    public void messageHandler(String spuId) {
        System.out.println("======需要上架, 导入数据到索引库的商品id为:=====" + spuId);
        esManagerService.importDataToEs(spuId);
    }
}
