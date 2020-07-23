package com.changgou.goods.pojo;


import java.io.Serializable;
import java.util.List;

/**
 * 自定义商品实体类
 * 包含商品还有对应的库存集合数据
 */
public class Goods implements Serializable {
    //商品对象
    private Spu spu;
    //库存集合对象
    private List<Sku> skuList;

    public Spu getSpu() {
        return spu;
    }

    public void setSpu(Spu spu) {
        this.spu = spu;
    }

    public List<Sku> getSkuList() {
        return skuList;
    }

    public void setSkuList(List<Sku> skuList) {
        this.skuList = skuList;
    }
}
