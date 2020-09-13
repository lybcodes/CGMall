package com.changgou.web.order.controller;

import com.changgou.entity.Result;
import com.changgou.order.feign.CartFeign;
import com.changgou.order.feign.OrderFeign;
import com.changgou.order.pojo.Order;
import com.changgou.order.pojo.OrderItem;
import com.changgou.user.feign.AddressFeign;
import com.changgou.user.pojo.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.swing.*;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/worder")
public class OrderController {

    @Autowired
    private AddressFeign addressFeign;

    @Autowired
    private CartFeign cartFeign;

    @Autowired
    private OrderFeign orderFeign;

    @GetMapping("/ready/order")
    public String toOrder(Model model){
        //地址
        List<Address> addressList = addressFeign.list().getData();
        model.addAttribute("address", addressList);

        //购物车相关数据
        Map map = cartFeign.list();
        List<OrderItem> orderItemList = (List<OrderItem>)map.get("orderItemList");
        Integer totalNum = (Integer) map.get("totalNum");
        Integer totalMoney = (Integer) map.get("totalMoney");

        model.addAttribute("carts", orderItemList);
        model.addAttribute("totalNum", totalNum);
        model.addAttribute("totalMoney", totalMoney);

        //加载默认收件人信息
        for (Address address : addressList) {
            if("1".equals(address.getIsDefault())){
                model.addAttribute("defAddr", address);
            }
        }
        return "order";
    }



    /***
     * 生成订单
     * @param order
     * @return
     */
    @PostMapping(value = "/add")
    @ResponseBody
    public Result add(@RequestBody Order order){
        Result result = orderFeign.add(order);
        return result;
    }
}
