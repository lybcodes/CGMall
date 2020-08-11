package com.changgou.search.service;

import com.alibaba.fastjson.JSON;
import com.changgou.pojo.SkuInfo;
import jdk.nashorn.internal.ir.IfNode;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ZJ
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ElasticsearchTemplate esTemplate;

    //设置每页展示20条数据
    public final static Integer PAGE_SIZE = 20;

    @Override
    public Map search(Map<String, String> searchMap) {
        if (searchMap ==null || searchMap.size() == 0) {
            return null;
        }
        Map<String, Object> resultMap = new HashMap<>();
        /**
         * 获取查询条件
         */
        //获取查询的关键字
        String keywords = searchMap.get("keywords");
        //获取当前页
        String pageNum = searchMap.get("pageNum");
        if (StringUtils.isEmpty(pageNum)) {
            searchMap.put("pageNum", "1");
            pageNum = "1";
        }
        //获取排序域名
        String sortField = searchMap.get("sortField");
        //获取排序规则
        String sortRule = searchMap.get("sortRule");
        //获取品牌过滤条件
        String brand = searchMap.get("brand");
        //获取价格过滤条件
        String price = searchMap.get("price");

        /**
         * 封装查询对象
         */
        //创建顶级的查询条件对象
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //创建组合查询条件对象(这里可以放多种查询条件)
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        /**
         * 根据关键字查询
         */
        if (!StringUtils.isEmpty(keywords)) {
            //应该, 是or或者的意思
            //boolQueryBuilder.should();
            //非, not的意思
            //boolQueryBuilder.mustNot();
            //必须, 相当于and的意思
            //QueryBuilders.matchQuery是将查询的关键字根据指定的分词器切分词后, 将切分出来的词一个一个查询,
            //将查询到的结果组合到一起返回
            //operator(Operator.AND)是控制切分词后, 根据每个词查询出来结果, 这些结果组合在一块返回的关系
            boolQueryBuilder.must(QueryBuilders.matchQuery("name", keywords).operator(Operator.AND));
        }


        /**
         * 根据品牌过滤查询
         */
        if (!StringUtils.isEmpty(brand)) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("brandName", brand));
        }

        /**
         * 根据规格过滤查询
         */
        if (searchMap != null && searchMap.size() > 0) {
            //遍历所有查询参数, 获取所有查询参数的key
            for (String searchKey : searchMap.keySet()) {
                //找到所有以spec_开头的key, 这样的才是规格参数
                if (searchKey.startsWith("spec_")) {
                    //将转移符%2B转换成加号, 特殊字符处理
                    searchMap.put(searchKey, searchMap.get(searchKey).replace("%2B", "+"));
                    //注意: 因为在SkuInfo索引库中规格是text类型, 默认会切分词, 这里需要将规格当做一个整体来做过滤
                    //所以最后拼接的.keyword是强制类型转化, 将text类型转成keyword类型, 这样就不会切分词, 作为一个整体使用
                    boolQueryBuilder.filter(QueryBuilders.termQuery("specMap." + searchKey.substring(5) +".keyword", searchMap.get(searchKey)));
                }
            }
        }

        /**
         * 根据价格过滤查询
         */
        if (!StringUtils.isEmpty(price)) {
            String[] priceArray = price.split("-");
            if (priceArray.length == 2) {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(priceArray[0]).lte(priceArray[1]));
            }
        }

        /**
         * 分页查询
         * PageRequest对象, 第一参数是从第几页开始查询, 第二个参数是每页查询多少条数据
         * 注意: 第一个参数从第几页查询, 默认起始是从第0页开始查询
         */
        nativeSearchQueryBuilder.withPageable(new PageRequest(Integer.parseInt(pageNum) - 1, PAGE_SIZE));

        /**
         *
         * 高亮查询
         */
        HighlightBuilder.Field higntLightField = new HighlightBuilder.Field("name").preTags("<em style=\"color:red\">").postTags("</em>");
        nativeSearchQueryBuilder.withHighlightFields(higntLightField);

        /**
         * 排序查询
         */
        if (!StringUtils.isEmpty(sortRule) && !StringUtils.isEmpty(sortField)) {
            //升序
            if ("ASC".equals(sortRule)) {
                nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(sortField).order(SortOrder.ASC));
            }
            //降序
            if ("DESC".equals(sortRule)) {
                nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(sortField).order(SortOrder.DESC));
            }
        }

        /**
         * 根据品牌聚合查询
         */
        //给品牌聚合起个名称, 这个名称随意起名, 主要用来获取结果的时候, 通过这个名字获取结果
        String brandAgg = "brandAgg";
        TermsAggregationBuilder brandAggBuilder = AggregationBuilders.terms(brandAgg).field("brandName");
        nativeSearchQueryBuilder.addAggregation(brandAggBuilder);

        /**
         * 根据规格聚合查询
         */
        String specAgg = "specAgg";
        TermsAggregationBuilder specAggBuilder = AggregationBuilders.terms(specAgg).field("spec.keyword");
        nativeSearchQueryBuilder.addAggregation(specAggBuilder);


        /**
         * 查询并返回结果集(包含高亮)
         */
        //将组合查询对象放入顶级查询对象中
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        AggregatedPage<SkuInfo> skuInfos = esTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class, new SearchResultMapper() {

            //在这里重新组合查询结果, 将高亮名称获取到放入结果集中
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {

                List<T> resultList = new ArrayList<>();
                //从查询的响应中获取查询结果对象
                SearchHits hits = searchResponse.getHits();
                if (hits != null) {
                    //查询到的结果集
                    SearchHit[] hitArray = hits.getHits();
                    if (hitArray.length > 0) {
                        for (SearchHit searchHit : hitArray) {
                            //获取数据json格式字符串(这个结果中名字是不带高亮的)
                            String skuInfoJsonStr = searchHit.getSourceAsString();
                            //将skuInfo的json字符串转换成skuInfo对象
                            SkuInfo skuInfo = JSON.parseObject(skuInfoJsonStr, SkuInfo.class);
                            /**
                             * 获取高亮名称
                             * 高亮名称如果能够获取到, 则获取出来放入SkuInfo对象中的name属性中
                             * 如果获取不到高亮名称则使用SkuInfo对象中原有的不带高亮的名称
                             */
                            if (searchHit.getHighlightFields() != null && searchHit.getHighlightFields().size() > 0) {
                                Text[] names = searchHit.getHighlightFields().get("name").fragments();
                                skuInfo.setName(names[0].toString());
                            }
                            //将这个对象放入返回的结果集中
                            resultList.add((T) skuInfo);
                        }
                    }
                }

                return new AggregatedPageImpl<T>(resultList, pageable, hits.getTotalHits(), searchResponse.getAggregations());
            }
        });

        /**
         * 获取根据品牌聚合的结果集
         */
        StringTerms brandStrTerms = (StringTerms)skuInfos.getAggregation(brandAgg);
        //获取品牌聚合结果
        List<String> brandList = brandStrTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
        resultMap.put("brandList", brandList);

        /**
         * 获取根据规格聚合的结果集
         */
        StringTerms specStrTerms = (StringTerms)skuInfos.getAggregation(specAgg);
        List<String> specList = specStrTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
        resultMap.put("specList", specFilter(specList));

        /**
         * 封装查询结果后返回
         */
        //当前页
        resultMap.put("pageNum", pageNum);
        //查询返回的结果集
        resultMap.put("rows", skuInfos.getContent());
        //总页数
        resultMap.put("totalPages", skuInfos.getTotalPages());
        //查询到的中条数
        resultMap.put("total", skuInfos.getTotalElements());

        return resultMap;
    }

    /**
     * 处理规格聚合后的结果
     * 原始查询结果:
     *      "specList": [
     *         "{'颜色': '蓝色', '版本': '6GB+128GB'}",
     *         "{'颜色': '黑色', '版本': '6GB+128GB'}",
     *         "{'颜色': '黑色', '版本': '4GB+64GB'}",
     *         "{'颜色': '蓝色', '版本': '4GB+64GB'}",
     *         "{'颜色': '蓝色', '版本': '6GB+64GB'}",
     *         "{'颜色': '黑色', '版本': '6GB+64GB'}",
     *         "{'颜色': '黑色'}",
     *         "{'颜色': '蓝色'}",
     *         "{'颜色': '金色', '版本': '4GB+64GB'}",
     *         "{'颜色': '粉色', '版本': '6GB+128GB'}"
     *     ]
     *
     * 需要的结果:
     *    {
     *      颜色: [蓝色, 黑色, 金色, 粉色],
     *      版本: [6GB+128GB, 4GB+64GB, 6GB+64GB]
     *    }
     * @return
     */
    public Map<String, Set<String>> specFilter(List<String> specList) {
        Map<String, Set<String>> resultMap = new HashMap<>();
        if (specList != null && specList.size() > 0) {
            for (String specJsonStr : specList) {
                //将获取到的json字符串转成Map, 例如: "{'颜色': '金色', '版本': '4GB+64GB'}"
                Map<String, String> specMap = JSON.parseObject(specJsonStr, Map.class);
                for (String specKey : specMap.keySet()) {
                    //从需要返回的map中获取set集合
                    Set<String> specSet = resultMap.get(specKey);
                    if (specSet == null) {
                        specSet = new HashSet<String>();
                    }
                    //将规格的值放入Set中
                    specSet.add(specMap.get(specKey));
                    //将set放入Map中
                    resultMap.put(specKey, specSet);
                }
            }
        }
        return resultMap;
    }
}
