package com.changgou.goods.feign;

import com.changgou.entity.Result;
import com.changgou.goods.dao.SkuMapper;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.service.SkuService;
import com.changgou.goods.service.impl.SkuServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

@RestController
public class SkuFeignImpl implements SkuFeign{

    @Autowired
    private SkuService skuService;

    @Autowired
    private SkuMapper skuMapper;

    @Override
    public List<Sku> findListBySpuId(String spuId) {
        return skuService.findListBySpuId(spuId);
    }

    @Override
    public Result findAllList(Map searchMap) {
        SkuServiceImpl skuServiceImpl = new SkuServiceImpl();
        Example example = skuServiceImpl.createExample(searchMap);
        List<Sku> skus = skuMapper.selectByExample(example);
        Result<List<Sku>> skuResult = new Result<>();
        skuResult.setData(skus);
        return skuResult;
    }
}
