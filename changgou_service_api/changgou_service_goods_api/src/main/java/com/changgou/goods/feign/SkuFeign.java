package com.changgou.goods.feign;


import com.changgou.goods.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.changgou.entity.Result;
import java.util.List;
import java.util.Map;

@FeignClient(name = "goods")
@RequestMapping("/sku-feign")
public interface SkuFeign {
    /**
     * 根据商品id查询对应的库存集合数据
     * @param spuId
     * @return
     */
    @GetMapping("/spu/{spuId}")
    public List<Sku> findListBySpuId(@PathVariable("spuId") String spuId);


    /***
     * 多条件搜索品牌数据
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search" )
    public Result findAllList(@RequestParam Map searchMap);
}
