package cn.civer.blog.Controller.Common;

import cn.civer.blog.Model.Entity.MessageConstants;
import cn.civer.blog.Model.Entity.Result;
import cn.civer.blog.Service.FileServ;
import com.obs.services.model.ObjectListing;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/file")
public class ObsFileController {
    @Autowired
    private FileServ fileServ;

    /**
     * 上传文件
     * @param file 设置文件类型为 MediaType.MULTIPART_FORM_DATA_VALUE
     * @return 上传成功后的文件访问url
     */
    @PreAuthorize("hasRole('subscriber')")
    @Operation(summary = "上传文件")
    @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result upLoad(@RequestParam("file") @RequestPart("file") MultipartFile file){
        String url = fileServ.uploadFile(file);

        return Result.success(url);
    }

    /**
     * 流式下载文件
     * @param objectKey 文件所在路径
     * @return StreamingResponseBody异步下载流
     */
    @Operation(summary = "下载文件")
    @GetMapping(value = "download/stream")
    public ResponseEntity<StreamingResponseBody> download(@RequestParam("objectKey") String objectKey)  {
        return fileServ.downloadFile(objectKey);
    }

    /**
     * 直接给出下载url
     * @param objectKey 文件所在路径
     * @return 下载url
     */
    @Operation(summary = "下载文件")
    @GetMapping(value = "download/url")
    public Result downloadUrl(@RequestParam("objectKey") String objectKey)  {
        return Result.success(fileServ.downloadFileUrl(objectKey));
    }

    /**
     * 删除文件
     * @param filePath 文件所在路径
     * @return 删除结果
     */
    @PreAuthorize("hasRole('subscriber')")
    @Operation(summary = "删除文件")
    @DeleteMapping(value = "delete")
    public Result delete(@RequestParam("filePath") String filePath){
        Boolean result = fileServ.deleteFile(filePath);
        if(!result){
            return Result.success(MessageConstants.FILE_DELETE_FAILED);
        }
        return Result.success(MessageConstants.FILE_DELETE_SUCCESS);
    }

    /**
     * 获取所有文件列表
     * @return 文件列表
     */
    @Operation(summary = "获取所有文件")
    @GetMapping(value = "select")
    public Result select(){
        ObjectListing result = fileServ.listFile();
        return Result.success(result);
    }
}
