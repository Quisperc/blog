package cn.civer.blog.Service;

import com.obs.services.model.ObjectListing;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface FileServ {
    String uploadFile(MultipartFile file);
    Boolean deleteFile(String objectKey);
    ObjectListing listFile();
    ResponseEntity<StreamingResponseBody> downloadFile(String objectKey);
    String downloadFileUrl(String objectKey);
}
