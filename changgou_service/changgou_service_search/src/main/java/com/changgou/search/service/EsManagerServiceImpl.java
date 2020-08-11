package com.changgou.search.service;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.pojo.SkuInfo;
import com.changgou.search.dao.ESDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author ZJ
 */
@Service
public class EsManagerServiceImpl implements EsManagerService {

    @Autowired
    private ESDao esDao;

    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Autowired
    private SkuFeign skuFeign;

    @Override
    public void createIndexAndMapping() {
        //1. 创建索引库
        esTemplate.createIndex("skuinfo");
        //2. 创建mapping也就是索引库内部的结构, 都有哪些属性, 都是哪些类型等
        esTemplate.putMapping(SkuInfo.class);
    }

    @Override
    public void importDataToEs(String spuId) {
        List<Sku> skuList = skuFeign.findListBySpuId(spuId);
        if (null == skuList) {
            throw new RuntimeException("此商品对应的库存数据为空, 无数据导入索引库:" + spuId);
        }
        //将获取到的数据转换成json格式字符串
        String skuJsonStr = JSON.toJSONString(skuList);
        //将json格式数据转换成库存对应的索引库对象
        List<SkuInfo> skuInfoList = JSON.parseArray(skuJsonStr, SkuInfo.class);

        for (SkuInfo skuInfo : skuInfoList) {
            //将规格json格式字符串转换成Map
            Map specMap = JSON.parseObject(skuInfo.getSpec(), Map.class);
            skuInfo.setSpecMap(specMap);
        }
        //导入数据到索引库
        esDao.saveAll(skuInfoList);
    }

    @Override
    public void importAllToES() {
        List<Sku> skuList = skuFeign.findListBySpuId("all");
        if (null == skuList) {
            throw new RuntimeException("此商品对应的库存数据为空, 无数据导入索引库" );
        }
        //将获取到的数据转换成json格式字符串
        String skuJsonStr = JSON.toJSONString(skuList);
        //将json格式数据转换成库存对应的索引库对象
        List<SkuInfo> skuInfoList = JSON.parseArray(skuJsonStr, SkuInfo.class);

        for (SkuInfo skuInfo : skuInfoList) {
            //将规格json格式字符串转换成Map
            Map specMap = JSON.parseObject(skuInfo.getSpec(), Map.class);
            skuInfo.setSpecMap(specMap);
        }
        //导入数据到索引库
        esDao.saveAll(skuInfoList);
    }
}
