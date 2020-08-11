package com.changgou.search.dao;

import com.changgou.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;

/**
 * 操作ES的Dao
 * @author ZJ
 */
public interface ESDao extends ElasticsearchCrudRepository<SkuInfo, Long> {
}
