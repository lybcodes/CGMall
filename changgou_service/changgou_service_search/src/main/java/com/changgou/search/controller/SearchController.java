package com.changgou.search.controller;

import com.changgou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author ZJ
 */
@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 处理接收的参数中的特殊符号
     * %2B代表加号, 这里在接受参数的时候, 对于加号有可能会自动转换成空格, 造成查询不到规格对应的数据
     * @param searchMap
     * @return
     */
    public Map<String, String> paramHandler(Map<String, String> searchMap) {
        if (searchMap != null && searchMap.size() > 0) {
            for (String key : searchMap.keySet()) {
                if (key.startsWith("spec_")) {
                    searchMap.put(key, searchMap.get(key).replace("+" , "%2B"));
                    searchMap.put(key, searchMap.get(key).replace(" " , "%2B"));
                }
            }
        }
        return searchMap;
    }

    /**
     * 高级搜索
     * @param searchMap
     * @return
     */
    @GetMapping
    public Map search(@RequestParam Map<String, String> searchMap) {
        //处理特殊字符方法
        paramHandler(searchMap);
        Map resultMap = searchService.search(searchMap);
        return resultMap;
    }
}
