package com.changgou.goods.service;

import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.github.pagehelper.Page;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

public interface SpuService {

    /***
     * 查询所有
     * @return
     */
    List<Spu> findAll();

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    Spu findById(String id);

    Goods findGoodsById(String id);
    /***
     * 新增
     * @param spu
     */
    void add(Spu spu);

    void addGoods(Goods goods);

    /***
     * 修改
     * @param spu
     */
    void update(Spu spu);

    void updateGoods(Goods goods);

    /***
     * 删除
     * @param id
     */
    void delete(String id);

    /***
     * 多条件搜索
     * @param searchMap
     * @return
     */
    List<Spu> findList(Map<String, Object> searchMap);

    /***
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    Page<Spu> findPage(int page, int size);

    /***
     * 多条件分页查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    Page<Spu> findPage(Map<String, Object> searchMap, int page, int size);


    /**
     * 商品审核
     * @param spuId
     */
    public void audit(String spuId);

    /**
     * 商品上架
     * @param id
     */
    public void put(String id);

    /**
     * 商品下架
     * @param spuId
     */
    public void pull(String spuId);

    /**
     * 逻辑删除
     * @param spuId
     */
    public void del(String spuId);

    /**
     * 逻辑恢复
     * @param spuId
     */
    public void restore(String spuId);


}
