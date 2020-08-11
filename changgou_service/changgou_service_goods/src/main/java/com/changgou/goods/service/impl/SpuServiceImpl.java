package com.changgou.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.dao.*;
import com.changgou.goods.pojo.*;
import com.changgou.goods.service.SpuService;
import com.changgou.util.IdWorker;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RabbitMessagingTemplate rabbitMessagingTemplate;

    /**
     * 查询全部列表
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    @Override
    public Spu findById(String id){
        return  spuMapper.selectByPrimaryKey(id);
    }

    @Override
    public Goods findGoodsById(String id) {
        //1. 根据商品查询商品对象
        Spu spu = spuMapper.selectByPrimaryKey(id);

        //2. 根据商品id查询库存集合对象
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId", id);
        List<Sku> skuList = skuMapper.selectByExample(example);

        //3. 将查询到的数据封装后返回
        Goods goods = new Goods();
        goods.setSpu(spu);
        goods.setSkuList(skuList);
        return goods;
    }

    /**
     * 增加
     * @param spu
     */
    @Override
    public void add(Spu spu){
        spuMapper.insert(spu);
    }

    @Override
    public void addGoods(Goods goods) {
        /**
         * 1、添加商品对象
         */
        Spu spu = goods.getSpu();
        spu.setId(String.valueOf(idWorker.nextId()));
        spu.setIsDelete("0");
        spu.setIsMarketable("0");
        spu.setStatus("0");
        spuMapper.insertSelective(spu);

        /**
         * 2. 维护品牌和分类关联关系
         * 在添加商品后, 拿着商品中的品牌id和分类id到品牌和分类关联表中查询
         * 如果查询到数据, 不进行任何操作, 如果查询不到, 添加关联关系数据
         */
        //使用这个对象作为查询条件
        CategoryBrand categoryBrand  = new CategoryBrand();
        //设置品牌id
        categoryBrand.setBrandId(spu.getBrandId());
        //设置分类id
        categoryBrand.setCategoryId(spu.getCategory3Id());
        //查询到的符合条件的总数
        int count = categoryBrandMapper.selectCount(categoryBrand);
        //判断是否能查询到品牌和规格关联关系数据
        if (count == 0) {
            //添加分类和品牌关联关系数据
            categoryBrandMapper.insertSelective(categoryBrand);
        }

        /**
         * 3、添加库存对象
         */
        insertSkuList(goods);
        }

    /**
     * 添加库存集合数据
     * @param goods
     */
    private void insertSkuList(Goods goods) {
        Spu spu = goods.getSpu();
        List<Sku> skuList = goods.getSkuList();
        if (skuList != null) {
            for (Sku sku : skuList) {
                //库存id
                sku.setId(String.valueOf(idWorker.nextId()));
                //分类id
                sku.setCategoryId(spu.getCategory3Id());
                //使用分类id,查询分类对象
                Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());
                //分类名称
                sku.setCategoryName(category.getName());
                //规格(页面填写传过来的)
                if (StringUtils.isEmpty(sku.getSpec())) {
                    sku.setSpec("{}");
                }
                //商品id
                sku.setSpuId(spu.getId());
                //审核状态, 默认为未审核
                sku.setStatus("0");
                //创建时间
                sku.setCreateTime(new Date());
                //修改时间
                sku.setUpdateTime(new Date());
                //示例图片
                String images = sku.getImages();
                if (!StringUtils.isEmpty(images)) {
                    String[] imageArray = images.split(",");
                    if (imageArray.length > 0) {
                        sku.setImage(imageArray[0]);

                    }
                }

                //品牌名称
                //根据品牌id, 查询品牌对象
                Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
                sku.setBrandName(brand.getName());

                //库存名称 = 商品名称 + 规格
                String title = spu.getName();
                //获取库存规格, 例如: {'颜色': '紫色'}
                String specJsonStr = sku.getSpec();
                //将json转换成单个java对象   , 例如 {'颜色': '紫色'}    使用parseObject方法转换
                Map<String, String> specMap = JSON.parseObject(specJsonStr, Map.class);
                for (String specValue : specMap.values()) {
                    title += " " + specValue;
                }
                sku.setName(title);
                //将json转换成集合对象  , 例如: [{},{},{}]使用parseArray方法
                //JSON.parseArray();

                skuMapper.insertSelective(sku);
            }
        }
    }



    /**
     * 修改
     * @param spu
     */
    @Override
    public void update(Spu spu){
        spuMapper.updateByPrimaryKey(spu);
    }

    @Override
    public void updateGoods(Goods goods) {
        //1. 修改商品对象
        spuMapper.updateByPrimaryKeySelective(goods.getSpu());

        //2. 根据商品id删除对应的库存集合数据
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId", goods.getSpu().getId());
        skuMapper.deleteByExample(example);

        //3. 重新添加库存集合数据
        insertSkuList(goods);
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(String id){
        //1. 根据商品id, 真实删除商品数据
        spuMapper.deleteByPrimaryKey(id);
        //2. 根据商品id, 真实删除库存集合数据
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId", id);
        skuMapper.deleteByExample(example);
    }


    /**
     * 条件查询
     * @param searchMap
     * @return
     */
    @Override
    public List<Spu> findList(Map<String, Object> searchMap){
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Spu> findPage(int page, int size){
        PageHelper.startPage(page,size);
        return (Page<Spu>)spuMapper.selectAll();
    }

    /**
     * 条件+分页查询
     * @param searchMap 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public Page<Spu> findPage(Map<String,Object> searchMap, int page, int size){
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        return (Page<Spu>)spuMapper.selectByExample(example);
    }

    @Override
    public void audit(String spuId) {
        //1. 根据商品id查询到spu对象
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //2. 判断上架状态为0, 代表未上架的商品才可以进行审核
        if (!"0".equals(spu.getIsMarketable())) {
            throw new RuntimeException("这个商品已上架, 不能进行审核");
        }
        //3. 判断删除状态, 删除状态为0, 未删除的商品才可以审核
        if (!"0".equals(spu.getIsDelete())) {
            throw new RuntimeException("这个商品已删除, 不能进行审核");
        }
        //4. 修改商品的审核状态
        spu.setStatus("1");
        spuMapper.updateByPrimaryKeySelective(spu);
        //5. 修改库存数据的审核状态
        //创建修改的对象
        Sku sku = new Sku();
        //设置审核状态为1, 已审核
        sku.setStatus("1");
        //设置修改条件
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId", spuId);
        //根据非主键作为修改条件, 第一个参数为修改的对象内容, 第二个参数为修改条件
        skuMapper.updateByExampleSelective(sku, example);
    }

    @Override
    public void put(String id) {
        //1. 根据商品id获取商品对象
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //2. 判断删除状态必须为未删除的
        if (!"0".equals(spu.getIsDelete())) {
            throw new RuntimeException("您的商品已删除, 不可以上架");
        }
        //3. 判断审核状态必须为已审核的
        if (!"1".equals(spu.getStatus())) {
            throw new RuntimeException("您的商品未审核通过, 不可以上架");
        }
        //4. 将数据库中商品的上架状态改为已上架
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);
        //将数据发送到rabbitMQ中
        rabbitMessagingTemplate.convertAndSend("goods_up_exchange", "", id);
    }

    @Override
    public void pull(String spuId) {
        Spu spu = new Spu();
        //设置修改的商品主键id
        spu.setId(spuId);
        //设置状态为下架
        spu.setIsMarketable("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }


    @Override
    public void del(String spuId) {
        //1. 根据商品id, 获取商品对象
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //2. 判断, 如果是已上架的商品不能够删除
        if ("1".equals(spu.getIsMarketable())) {
            throw new RuntimeException("该商品已上架, 不能够删除");
        }
        //4. 逻辑删除
        spu.setIsDelete("1");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    @Override
    public void restore(String spuId) {
        Spu spu = new Spu();
        //设置商品id, 作为修改条件
        spu.setId(spuId);
        //删除状态设置为0, 未删除
        spu.setIsDelete("0");
        //审核状态设置为0, 未审核
        spu.setStatus("0");
        //上架状态设置为0, 未上架
        spu.setIsMarketable("0");

        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 构建查询对象
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 主键
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andEqualTo("id",searchMap.get("id"));
           	}
            // 货号
            if(searchMap.get("sn")!=null && !"".equals(searchMap.get("sn"))){
                criteria.andEqualTo("sn",searchMap.get("sn"));
           	}
            // SPU名
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
           	}
            // 副标题
            if(searchMap.get("caption")!=null && !"".equals(searchMap.get("caption"))){
                criteria.andLike("caption","%"+searchMap.get("caption")+"%");
           	}
            // 图片
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
           	}
            // 图片列表
            if(searchMap.get("images")!=null && !"".equals(searchMap.get("images"))){
                criteria.andLike("images","%"+searchMap.get("images")+"%");
           	}
            // 售后服务
            if(searchMap.get("saleService")!=null && !"".equals(searchMap.get("saleService"))){
                criteria.andLike("saleService","%"+searchMap.get("saleService")+"%");
           	}
            // 介绍
            if(searchMap.get("introduction")!=null && !"".equals(searchMap.get("introduction"))){
                criteria.andLike("introduction","%"+searchMap.get("introduction")+"%");
           	}
            // 规格列表
            if(searchMap.get("specItems")!=null && !"".equals(searchMap.get("specItems"))){
                criteria.andLike("specItems","%"+searchMap.get("specItems")+"%");
           	}
            // 参数列表
            if(searchMap.get("paraItems")!=null && !"".equals(searchMap.get("paraItems"))){
                criteria.andLike("paraItems","%"+searchMap.get("paraItems")+"%");
           	}
            // 是否上架
            if(searchMap.get("isMarketable")!=null && !"".equals(searchMap.get("isMarketable"))){
                criteria.andEqualTo("isMarketable",searchMap.get("isMarketable"));
           	}
            // 是否启用规格
            if(searchMap.get("isEnableSpec")!=null && !"".equals(searchMap.get("isEnableSpec"))){
                criteria.andEqualTo("isEnableSpec", searchMap.get("isEnableSpec"));
           	}
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andEqualTo("isDelete",searchMap.get("isDelete"));
           	}
            // 审核状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andEqualTo("status",searchMap.get("status"));
           	}

            // 品牌ID
            if(searchMap.get("brandId")!=null ){
                criteria.andEqualTo("brandId",searchMap.get("brandId"));
            }
            // 一级分类
            if(searchMap.get("category1Id")!=null ){
                criteria.andEqualTo("category1Id",searchMap.get("category1Id"));
            }
            // 二级分类
            if(searchMap.get("category2Id")!=null ){
                criteria.andEqualTo("category2Id",searchMap.get("category2Id"));
            }
            // 三级分类
            if(searchMap.get("category3Id")!=null ){
                criteria.andEqualTo("category3Id",searchMap.get("category3Id"));
            }
            // 模板ID
            if(searchMap.get("templateId")!=null ){
                criteria.andEqualTo("templateId",searchMap.get("templateId"));
            }
            // 运费模板id
            if(searchMap.get("freightId")!=null ){
                criteria.andEqualTo("freightId",searchMap.get("freightId"));
            }
            // 销量
            if(searchMap.get("saleNum")!=null ){
                criteria.andEqualTo("saleNum",searchMap.get("saleNum"));
            }
            // 评论数
            if(searchMap.get("commentNum")!=null ){
                criteria.andEqualTo("commentNum",searchMap.get("commentNum"));
            }

        }
        return example;
    }

}
