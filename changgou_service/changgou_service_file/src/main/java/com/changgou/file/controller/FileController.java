package com.changgou.file.controller;


import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.file.util.FastDFSClient;
import com.changgou.file.util.FastDFSFile;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


/**
 * 文件操作controller
 */
@RestController
@RequestMapping("/file")
public class FileController {
    /**
     * <input type="file" name="file"/>
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Result fileUpload(@RequestParam("file") MultipartFile file){
        try {
            //1、获取完整的文件名
            String fileName = file.getOriginalFilename();
            //2、获取文件拓展名
            if(StringUtils.isEmpty(fileName)){
                throw new RuntimeException("文件不存在");
            }
            String ext = fileName.substring(fileName.lastIndexOf("."));
            //3、获取文件内容
            byte[] contents = file.getBytes();
            //4、创建上传文件实体类
            FastDFSFile fastDFSFile = new FastDFSFile(fileName, contents, ext);
            //5、调用上传文件工具方法，上传，并返回存储后的文件路径和文件名
            String[] path = FastDFSClient.upload(fastDFSFile);
            //6、返回上传后的路径和文件名
            String url = FastDFSClient.getTrackerUrl() + path[0] + "/" + path[1];
            return new Result(true, StatusCode.OK, "上传成功", path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Result(false, StatusCode.ERROR, "上传失败");
    }
}
