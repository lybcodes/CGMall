package com.changgou.search.controller;

import com.changgou.entity.Page;
import com.changgou.pojo.SkuInfo;
import com.changgou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author ZJ
 */
@Controller //数据返回到页面，RestController是将数据变成json返回
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
    @ResponseBody //这个接口的数据变为json返回
    public Map search(@RequestParam Map<String, String> searchMap) {
        //处理特殊字符方法
        paramHandler(searchMap);
        Map resultMap = searchService.search(searchMap);
        return resultMap;
    }

    /**
     * 搜索并返回搜索结果列表页面
     * @param searchMap
     * @return
     */
    @GetMapping("/list")
    public String list(@RequestParam Map<String, String> searchMap, Model model){
        //处理特殊字符方法
        paramHandler(searchMap);
        //搜索并返回结果数据
        Map resultMap = searchService.search(searchMap);
        //借助MVC的model对象将数据返回页面
        model.addAttribute("result", resultMap);
        model.addAttribute("searchMap", searchMap);
        //封装分页数据返回,这里注意参数的类型强转
        Page<SkuInfo> page = new Page<SkuInfo>(Long.parseLong(String.valueOf(resultMap.get("total"))),
                                               Integer.parseInt(resultMap.get("pageNum").toString()),
                                               Page.pageSize);
        model.addAttribute("page", page);

        //url拼接：点击翻页跳转，需要Url中带着当前的查询参数
        StringBuilder url = new StringBuilder("/search/list");
        if(searchMap != null && searchMap.size() > 0){
            url.append("?");
            for (String paramKey : searchMap.keySet()) {
                if(!"sortRule".equals(paramKey) && !"sortField".equals(paramKey) && !"pageNum".equals(paramKey)) {
                    url.append(paramKey).append("=").append(searchMap.get(paramKey)).append("&");
                }
            }
            String urlStr = url.toString();
            urlStr = urlStr.substring(0, urlStr.length() - 1);//去掉url最后一个&符
            model.addAttribute("url", urlStr);
        }else{
            model.addAttribute("url", url);
        }
        return "search";
    }
}
