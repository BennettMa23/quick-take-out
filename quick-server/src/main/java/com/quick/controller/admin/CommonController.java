package com.quick.controller.admin;

import com.alibaba.fastjson.JSON;
import com.quick.constant.MessageConstant;
import com.quick.result.Result;
import io.minio.*;
import io.minio.messages.Item;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 通用接口
 * 主要使用MinIO对象存储 进行文件处理
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {
    @Autowired
    private MinioClient minioClient;
//    @Autowired
//    private MinioProperties minioProperties;

    @Value("${quick.minio.bucketName}")
    private String bucketName;
    @Value("${quick.minio.endpoint}")
    private String endpoint;

    /**
     * 查询
     * @return
     * @throws Exception
     */
    @GetMapping("/list")
    public List<Object> list() throws Exception {
//        获得bucket列表
        Iterable<io.minio.Result<Item>> myObjects = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).build());
        Iterator<io.minio.Result<Item>> iterator = myObjects.iterator();
        List<Object> items = new ArrayList<>();
        String format = "{'fileName':'%s','fileSize':'%s'}";
        while (iterator.hasNext()){
            Item item = iterator.next().get();
            items.add(JSON.parse(String.format(format, item.objectName(), formatFileSize(item.size()))));
        }
        return items;
    }

    /**
     * 下载
     * @param response
     * @param fileName
     */
    @RequestMapping("/download/{fileName}")
    public void download(HttpServletResponse response, @PathVariable("fileName") String fileName){
        InputStream in = null;
        try{
            // 获取对象信息
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucketName).object(fileName).build());
            response.setContentType(stat.contentType());
            response.setHeader( "Content-Disposition","attachment;filename=" +
                    URLEncoder.encode(fileName, "UTF-8"));
            //文件下载
            in = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
            IOUtils.copy(in, response.getOutputStream());
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    /**
     * 上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传：{}",file);

        try {
            String orgfileName = file.getOriginalFilename();
            //文件上传
            InputStream in = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(orgfileName).stream(
                                    in, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            in.close();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder
                    .append(endpoint)
                    .append("/")
                    .append(bucketName)
                    .append("/")
                    .append(orgfileName);
//            return Result.success(MessageConstant.);
            return Result.success(stringBuilder.toString());
//            //原始文件名
//            String originalFilename = file.getOriginalFilename();
//            //截取原始文件名的后缀   dfdfdf.png
//            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
//            //构造新文件名称
//            String objectName = UUID.randomUUID().toString() + extension;
//
//            //文件的请求路径
//            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
//            return Result.success(filePath);
        } catch (Exception e) {
            log.error("文件上传失败：{}", e);
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
    /**
     * 删除
     * @param fileName
     * @return
     */
    @DeleteMapping("/delete/{fileName}")
    public Result delete(@PathVariable("fileName") String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.error("删除失败");
        }
        return Result.success("删除成功");
    }


//    文件大小转换
    private static String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + " B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + " KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + " MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + " GB";
        }
        return fileSizeString;
    }

}
