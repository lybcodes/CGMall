package com.changgou.goods.controller;

import com.changgou.goods.pojo.Goods;
import com.changgou.goods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    /**
     * 查询所有数据 这个在大型项目中禁用，数据量太大会导致数据库崩溃
     * @return
     */
    @GetMapping
    public List<Goods> findAll(){
        List<Goods> list = goodsService.findAll();
        return list;
    }
}
