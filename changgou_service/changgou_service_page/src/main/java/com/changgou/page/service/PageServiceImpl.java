package com.changgou.page.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.changgou.entity.Result;
import com.changgou.goods.feign.CategoryFeign;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageServiceImpl implements PageService{

    @Autowired
    private SpuFeign spuFeign;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${pagepath}") //从配置文件中获取文件保存的路径
    private String path;

    @Override
    public Map<String, Object> findItemData(String spuId) {
        Map<String, Object> resultMap = new HashMap<>();
        /**
         * 1、根据商品id获取商品对象
         */
        Result<Spu> spuResult = spuFeign.findById(spuId);
        Spu spu = spuResult.getData();
        resultMap.put("spu", spu);
        /**
         * 2、获取图片集合对象
         * [
         *      {"color":"银白色","url":"//img11.360buyimg.com/n7/jfs/t30040/100/1290632710/208879/1f7e2225/5cdd0d92Nb78895a6.jpg"}
         * ]
         */
        if(spu != null){
            List<Map> imageList = JSON.parseArray(spu.getImages(), Map.class);
            List<String> imageUrlList = new ArrayList<>();
            if(imageList != null && imageList.size() > 0){
                for (Map imageMap : imageList) {
                    imageUrlList.add(String.valueOf(imageMap.get("url")));
                }
            }
            resultMap.put("imageList", imageUrlList);
        }
        /**
         * 3、根据商品对象中的分类id获取分类对象
         */
        if(spu != null){
            Category category1 = categoryFeign.findById(spu.getCategory1Id()).getData();
            resultMap.put("category1", category1);
            Category category2 = categoryFeign.findById(spu.getCategory2Id()).getData();
            resultMap.put("category2", category2);
            Category category3 = categoryFeign.findById(spu.getCategory3Id()).getData();
            resultMap.put("category3", category3);
        }

        /**
         * 4、根据商品Id获取库存集合对象
         */
        List<Sku> skuList = skuFeign.findListBySpuId(spuId);
        resultMap.put("skuList", skuList);
        return resultMap;
    }

    @Override
    public void createItemPage(Map<String, Object> dataMap, String spuId) {

        /**
         * 1、创建Context对象，这个对象中放入商品详情页面数据
         * thymrleaf作为模板引擎，里面可能放各种数据，需要将数据放到它指定的context对象中
         */
        Context context = new Context();
        //将模板中需要的数据传入给Context对象
        context.setVariables(dataMap);
        /**
         * 2、获取生成商品详情页面的位置
         */
        File dir = new File(path);
        /**
         * 3、判断商品详情页生成路径中是否有文件夹不存在
         */
        if(!dir.exists()){
            dir.mkdirs();
        }
        /**
         * 4、定义输出流，指定输出的位置以及文件名
         */
        File file = new File(dir + "/" + spuId + ".html");
        Writer out = null;
        try {
            out = new PrintWriter(file);

            /**
             * 5、生成
             * 第一个参数：模板名称
             * 第二个参数：Context对象，包含了模板中需要的所有数据
             * 第三个参数：输出流，指定页面生成的位置和名称
             */
            templateEngine.process("item", context, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            /**
             * 6、关闭流
             */
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
