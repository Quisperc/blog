package cn.civer.blog.Service;

import cn.civer.blog.Model.Entity.MyFile;
import com.obs.services.model.ObjectListing;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

public interface FileServ {
    String uploadFile(MultipartFile file);
    Boolean deleteFile(String objectKey);
    ObjectListing listFile();
    ResponseEntity<StreamingResponseBody> downloadFile(String objectKey);
    String downloadFileUrl(String objectKey);
    List<MyFile> getAllFileRecords();
    List<MyFile> getCurrentUserFileRecords();
}
