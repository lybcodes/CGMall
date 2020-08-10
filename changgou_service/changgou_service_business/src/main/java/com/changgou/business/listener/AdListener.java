package com.changgou.business.listener;


import okhttp3.*;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 监听mq发送来的广告数据
 */
@Component
@RabbitListener(queues = "ad_update_queue")
public class AdListener {

    /**
     * 接收广告队列中的数据进行处理
     * 业务：模拟http请求到nginx，让nginx调用lua脚本进行大广告redis中的数据更新
     * @param message
     */
    @RabbitHandler
    public void updateAd(String message){
        System.out.println("接收到消息："+message);
        String url = "http://192.168.200.128/ad_update?position=" + message;
        //创建OkHttpClient对象，这个是目前最快的发送请求的中间件
        OkHttpClient okHttpClient = new OkHttpClient();
        //创建请求对象
        Request request = new Request.Builder().url(url).build();
        //发送请求
        Call call = okHttpClient.newCall(request);
        //接收Call的回调（也就是发送的成功或失败）
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();//打印错误信息
                System.out.println("=====发送失败=====");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("======发送成功======" + response.message());
            }
        });
    }
}
