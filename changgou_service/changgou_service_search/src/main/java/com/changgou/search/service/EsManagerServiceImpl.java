package com.changgou.search.service;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.pojo.SkuInfo;
import com.changgou.search.dao.ESDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
        esTemplate.createIndex(SkuInfo.class);
        //2. 创建mapping也就是索引库内部的结构, 都有哪些属性, 都是哪些类型等
        esTemplate.putMapping(SkuInfo.class);
    }

    /**
     * 根据spuid导入数据到ES索引库
     * @param spuId 商品id
     */
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
            //将规格json格式字符串转换成Map，因为json字符串不好做查询，所以这里将它转成map
            Map specMap = JSON.parseObject(skuInfo.getSpec(), Map.class);
            skuInfo.setSpecMap(specMap);
        }
        //导入数据到索引库
        esDao.saveAll(skuInfoList);
    }

    /**
     * 导入全部数据到ES索引库
     */
    @Override
    public void importAllToES() {
        Map paramMap = new HashMap();
        paramMap.put("status", "1");
        Result result = skuFeign.findAllList(paramMap);
        String s = JSON.toJSONString(result.getData());
        List<SkuInfo> skuInfos = JSON.parseArray(s, SkuInfo.class);
        for (SkuInfo skuInfo : skuInfos) {
            skuInfo.setPrice(skuInfo.getPrice());
            skuInfo.setSpecMap(JSON.parseObject(skuInfo.getSpec(), Map.class));
        }
        esDao.saveAll(skuInfos);
    }
}
