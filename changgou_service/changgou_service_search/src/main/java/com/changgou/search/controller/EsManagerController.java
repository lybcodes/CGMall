package com.changgou.search.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.search.service.EsManagerService;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ZJ
 */
@RestController
@RequestMapping("/manager")
public class EsManagerController {

    @Autowired
    private EsManagerService esManagerService;

    @GetMapping("/create")
    public Result createIndexAndMapping() {
        esManagerService.createIndexAndMapping();
        return new Result(true, StatusCode.OK, "创建索引库结构成功");
    }

    /**
     * 导入所有数据到es索引库
     * @return
     */
    @GetMapping("/importAll")
    public Result importAllDataToES() {
        esManagerService.importAllToES();
        return new Result(true, StatusCode.OK, "导入数据成功");
    }
}
