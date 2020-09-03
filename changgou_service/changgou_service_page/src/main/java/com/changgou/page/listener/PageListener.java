package com.changgou.page.listener;

import com.changgou.page.service.PageService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 监听静态生成页队列，接受到消息（商品id）
 * 根据商品id ，查询商品的详细数据，然后根据数据加模板生成静态化页面
 * 通过IO流写入到这个服务器的硬盘上保存（上线阶段将生成好的页面通过IO流写入到nginx负载均衡器中，
 * 因为nginx性能比tomcat高，而且nginx可以作为http服务器运行静态页面）
 */
@Component
@RabbitListener(queues = "page_create_queue")
public class PageListener {

    @Autowired
    private PageService pageService;

    /**
     * 接收队列中的商品id，进行生成静态化商品详情页面
     * @param spuId
     */
    @RabbitHandler
    public void messageHandler(String spuId){
        //1、获取商品详情页面所需要的所有数据
        Map<String, Object> dataMap = pageService.findItemData(spuId);
        //2、根据数据生成静态化页面
        pageService.createItemPage(dataMap, spuId);
    }
}
