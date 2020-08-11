package com.changgou.search.service;

/**
 * @author ZJ
 */
public interface EsManagerService {

    /**
     * 创建索引库结构
     */
    public void createIndexAndMapping();

    /**
     * 根据商品id导入对应的库存数据到es索引库
     * @param spuId
     */
    public void importDataToEs(String spuId);

    /**
     * 导入所有已经审核过的数据到ES索引库(全量导入)
     */
    public void importAllToES();
}
