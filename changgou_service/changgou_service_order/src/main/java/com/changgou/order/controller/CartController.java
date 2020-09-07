package com.changgou.order.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 添加购物车
     * @param skuId 商品id
     * @param num 商品数量
     * @return
     */
    @GetMapping("/add")
    public Result add(@RequestParam("skuId") String skuId, @RequestParam("num") Integer num){
        //添加购物车（用户一定处于登录状态）
        //登录人信息，暂时写死
        String username = "itheima";
        cartService.add(username, skuId, num);
        return new Result(true, StatusCode.OK, "添加购物车成功");
    }

    /***
     * 查询用户购物车列表
     * @return
     */
    @GetMapping(value = "/list")
    public Map list(){
        //暂时静态，后续修改
        String username = "itheima";
        return cartService.list(username);
    }
}
