package com.changgou.goods.controller;
import com.changgou.entity.PageResult;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.goods.pojo.Goods;
import com.changgou.goods.service.SpuService;
import com.changgou.goods.pojo.Spu;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@CrossOrigin
@RequestMapping("/spu")
public class SpuController {


    @Autowired
    private SpuService spuService;

    /**
     * 查询全部数据
     * @return
     */
    @GetMapping
    public Result findAll(){
        List<Spu> spuList = spuService.findAll();
        return new Result(true, StatusCode.OK,"查询成功",spuList) ;
    }

    /***
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable String id){
        Spu spu = spuService.findById(id);
        return new Result(true,StatusCode.OK,"查询成功",spu);
    }

    @GetMapping("/goods/{id}")
    public Goods findGoodsById(@PathVariable String id){
        Goods goods = spuService.findGoodsById(id);
        return goods;
    }


    /***
     * 新增数据
     * @param spu
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Spu spu){
        spuService.add(spu);
        return new Result(true,StatusCode.OK,"添加成功");
    }

    @PostMapping("/goods")
    public Result addGoods(@RequestBody Goods goods){
        spuService.addGoods(goods);
        return new Result(true,StatusCode.OK,"添加成功");
    }


    /***
     * 修改数据
     * @param spu
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    public Result update(@RequestBody Spu spu,@PathVariable String id){
        spu.setId(id);
        spuService.update(spu);
        return new Result(true,StatusCode.OK,"修改成功");
    }

    @PutMapping(value="/goods/{id}")
    public Result updateGoods(@RequestBody Goods goods,@PathVariable String id){
        goods.getSpu().setId(id);
        spuService.updateGoods(goods);
        return new Result(true,StatusCode.OK,"修改成功");
    }



    /***
     * 多条件搜索品牌数据
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search" )
    public Result findList(@RequestParam Map searchMap){
        List<Spu> list = spuService.findList(searchMap);
        return new Result(true,StatusCode.OK,"查询成功",list);
    }


    /***
     * 分页搜索实现
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    public Result findPage(@RequestParam Map searchMap, @PathVariable  int page, @PathVariable  int size){
        Page<Spu> pageList = spuService.findPage(searchMap, page, size);
        PageResult pageResult=new PageResult(pageList.getTotal(),pageList.getResult());
        return new Result(true,StatusCode.OK,"查询成功",pageResult);
    }

    /**
     * 商品审核
     * @param spuId
     * @return
     */
    @GetMapping("/audit/{spuId}")
    public Result audit(@PathVariable("spuId") String spuId) {
        spuService.audit(spuId);
        return new Result(true, StatusCode.OK, "审核成功");
    }

    /**
     * 商品上架
     * @param spuId
     * @return
     */
    @GetMapping("/put/{spuId}")
    public Result put(@PathVariable("spuId") String spuId) {
        spuService.put(spuId);
        return new Result(true, StatusCode.OK, "上架成功");
    }

    /**
     * 商品下架
     * @param spuId
     * @return
     */
    @GetMapping("/pull/{spuId}")
    public Result pull(@PathVariable("spuId") String spuId) {
        spuService.pull(spuId);
        return new Result(true, StatusCode.OK, "下架成功");
    }

    /***
     * 根据ID删除品牌数据(真实)
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    public Result realdelete(@PathVariable String id){
        spuService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /**
     * 逻辑删除
     * @param spuId
     * @return
     */
    @GetMapping(value = "/delete/{spuId}" )
    public Result delete(@PathVariable String spuId){
        spuService.del(spuId);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /**
     * 将逻辑删除的数据找回
     * @param spuId
     * @return
     */
    @GetMapping(value = "/restore/{spuId}" )
    public Result restore(@PathVariable String spuId){
        spuService.restore(spuId);
        return new Result(true,StatusCode.OK,"数据找回成功");
    }

}
