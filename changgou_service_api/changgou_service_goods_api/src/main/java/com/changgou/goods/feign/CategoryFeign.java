package com.changgou.goods.feign;

import com.changgou.entity.Result;
import com.changgou.goods.pojo.Category;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "goods")
@RequestMapping("/category")
public interface CategoryFeign {

    @GetMapping("/{id}")
    public Result<Category> findById(@PathVariable("id") Integer id);
}
