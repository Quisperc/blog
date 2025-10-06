package cn.civer.blog.Utils;

import cn.civer.blog.Config.Properties.ObsProperties;
import com.obs.services.ObsClient;
import com.obs.services.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnBean(ObsClient.class) // 确保ObsClient存在时才注册
@RequiredArgsConstructor
public class ObsUtils {
    private final ObsProperties obsProperties;
    private final ObsClient obsClient;

    /**
     * 上传本地文件到OBS (基于File的上传)
     * @param filePath 本地文件完整路径
     * @return 文件在OBS的访问URL
     */
    public String uploadFile(String filePath) {
        return uploadFile(filePath, null);
    }

    /**
     * 上传本地文件到OBS，并可指定目标路径
     * @param filePath 本地文件完整路径
     * @param objectKey 存储在OBS中的路径（如"images/pic.jpg"），为null则使用随机生成的文件名
     * @return 文件在OBS的访问URL
     */
    public String uploadFile(String filePath, String objectKey) {
        filePath = cleanPath(filePath);
        objectKey = cleanPath(objectKey);
        try {
            File file = new File(filePath);
            if (!StringUtils.hasText(objectKey)) {
                objectKey = generateObjectKey(file.getName());
            }
            PutObjectResult result = obsClient.putObject(obsProperties.getBucketName(), objectKey, file);
            log.info("文件上传成功，ETag: {}", result.getEtag());
            return generateFileUrl(objectKey);
        } catch (Exception e) {
            log.error("上传文件到OBS失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 流式上传 - 适用于MultipartFile（前端上传）
     * @param file 前端上传的文件对象
     * @return 文件在OBS的访问URL
     */
    public String uploadStream(MultipartFile file) {
        return uploadStream(file, null);
    }

    /**
     * 流式上传 - 适用于MultipartFile，并可指定目标路径
     * @param file 前端上传的文件对象
     * @param objectKey 存储在OBS中的路径，为null则使用原始文件名
     * @return 文件在OBS的访问URL
     */
    public String uploadStream(MultipartFile file, String objectKey) {
        objectKey = cleanPath(objectKey);
        try (InputStream inputStream = file.getInputStream()) {
            if (!StringUtils.hasText(objectKey)) {
                objectKey = generateObjectKey(file.getOriginalFilename());
            }

            // 创建上传请求并设置元数据
            PutObjectRequest request = new PutObjectRequest();
            request.setBucketName(obsProperties.getBucketName());
            request.setObjectKey(objectKey);
            request.setInput(inputStream);

            // 设置对象元数据，如Content-Type
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            request.setMetadata(metadata);

            PutObjectResult result = obsClient.putObject(request);
            log.info("流上传成功，ETag: {}", result.getEtag());
            return generateFileUrl(objectKey);
        } catch (IOException e) {
            log.error("读取文件流失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件处理失败", e);
        } catch (Exception e) {
            log.error("流式上传到OBS失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 流式上传 - 适用于通用InputStream
     * @param inputStream 输入流
     * @param objectKey 存储在OBS中的路径（必须指定）
     * @param contentType 文件MIME类型
     * @return 文件在OBS的访问URL
     */
    public String uploadStream(InputStream inputStream, String objectKey, String contentType) {
        try {
            objectKey = cleanPath(objectKey);
            PutObjectRequest request = new PutObjectRequest();
            request.setBucketName(obsProperties.getBucketName());
            request.setObjectKey(objectKey);
            request.setInput(inputStream);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            request.setMetadata(metadata);

            PutObjectResult result = obsClient.putObject(request);
            log.info("流上传成功，ETag: {}", result.getEtag());
            return generateFileUrl(objectKey);
        } catch (Exception e) {
            log.error("流式上传到OBS失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 从OBS下载文件
     * @param objectKey 文件在OBS的路径
     * @return 包含文件流和信息的ObsObject
     */
    public ObsObject downloadFile(String objectKey) {
        try {
            objectKey = cleanPath(objectKey);
            ObsObject obsObject = obsClient.getObject(obsProperties.getBucketName(), objectKey);
            log.info("文件下载成功: {}", objectKey);
            return obsObject;
        } catch (Exception e) {
            log.error("从OBS下载文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件下载失败", e);
        }
    }

    /**
     * 删除OBS上的文件
     * @param objectKey 文件在OBS的路径
     * @return 删除是否成功
     */
    public boolean deleteFile(String objectKey) {
        try {
            objectKey = cleanPath(objectKey);
            DeleteObjectResult result = obsClient.deleteObject(obsProperties.getBucketName(), objectKey);
            log.info("文件删除成功: {}", objectKey);
            return true;
        } catch (Exception e) {
            log.error("从OBS删除文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件删除失败", e);
        }
    }

    /**
     * 列举所有文件
     * @return 桶内对象列举结果
     */
    public ObjectListing listFile(){
        // 简单列举
        ObjectListing result = obsClient.listObjects(obsProperties.getBucketName());
        return result;
    }

    /**
     * 判断对象是否存在
     * @param objectKey 对象所在路径
     * @return True Or False
     */
    public Boolean doesFileExist(String objectKey){
        objectKey = cleanPath(objectKey);
        return obsClient.doesObjectExist(obsProperties.getBucketName(), objectKey);
    }
    /**
     * 生成文件的访问URL
     * @param objectKey 文件在OBS的路径
     * @return 完整的URL
     */
    public String generateFileUrl(String objectKey) {
        objectKey = cleanPath(objectKey);
        // URL格式: https://桶名.域名/对象名
        // 根据你的端点配置调整，如果端点是包含域名的完整形式
        String endpoint = obsProperties.getEndpoint().replace("https://", "");
        //c return "https://" + obsProperties.getBucketName() + "." + endpoint + "/" + objectKey;
        return "https://" + obsProperties.getAccessUrl() + "/" + objectKey;
    }
    /**
     * 生成唯一的对象键（文件路径）
     * @param originalFileName 原始文件名
     * @return 格式如 "2025/10/05/uuid.jpg"
     */
    public String generateObjectKey(String originalFileName) {
        originalFileName = cleanPath(originalFileName);
        String extension = "";
        if (StringUtils.hasText(originalFileName) && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        } else if (originalFileName == null) {
            extension = ".dat";
        }
        // 使用UUID
        String uuid = UUID.randomUUID().toString().replace("-", "");
        // 使用原始名
        // 按日期分类存储，便于管理
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String datePath = java.time.LocalDate.now().format(formatter);
        // 使用UUID
        // return datePath + "/" + uuid + extension;
        // 使用原始名
        return datePath + "/" + originalFileName;
    }

    /**
     * 清理路径前后的空格
     * @param path 路径
     * @return 清理后的路径String
     */
    public String cleanPath(String path){
        // 去除前后的空格
        path = path.trim();
        // 去除可能存在的前导斜杠
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }
}
