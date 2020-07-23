package com.changgou.goods.service.impl;

import com.changgou.goods.dao.SkuMapper;
import com.changgou.goods.dao.SpuMapper;
import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.goods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Override
    public List<Goods> findAll() {
        List<Goods> goodsList = new ArrayList<>();
        //1、查询所有商品
        List<Spu> spuList = spuMapper.selectByExample(null);
        //2、根据商品id查询对应的库存集合
        if(spuList != null){
            for (Spu spu : spuList) {
                Goods goods = new Goods();
                Example example = new Example(Sku.class);
                Example.Criteria criteria = example.createCriteria();
                criteria.andEqualTo("spuId", spu.getId());
                List<Sku> skuList = skuMapper.selectByExample(example);
                //3、将数据封装后返回
                goods.setSpu(spu);
                goods.setSkuList(skuList);
                goodsList.add(goods);
            }
        }
        return goodsList;
    }
}
