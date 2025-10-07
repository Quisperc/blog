package cn.civer.blog.Service.Impl;

import cn.civer.blog.Exception.BizException;
import cn.civer.blog.Mapper.MyFileMapper;
import cn.civer.blog.Model.Entity.MessageConstants;
import cn.civer.blog.Model.Entity.MyFile;
import cn.civer.blog.Service.FileServ;
import cn.civer.blog.Utils.ObsUtils;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObsObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class FileServImpl implements FileServ {
    @Autowired
    private ObsUtils obsUtils;
    @Autowired
    private MyFileMapper myFileMapper;

    /**
     * 根据前端所传文件进行上传
     * @param file 待上传的文件
     * @return 上传后的url
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadFile(MultipartFile file) {
        // 1. 校验
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(MessageConstants.FILE_NOT_EXIST);
        }

        // 2. 检查是否已存在同名文件
        String objectKey = obsUtils.generateObjectKey(file.getOriginalFilename());
        if(myFileMapper.selectByObjectKey(objectKey) != null){
            log.warn(MessageConstants.FILE_EXIST +": {}", objectKey);
            // 可以选择覆盖、重命名、或返回已存在文件URL
            return obsUtils.generateFileUrl(objectKey);
        }else if (obsUtils.doesFileExist(objectKey)) {
            log.warn(MessageConstants.FILE_EXIST +": {}", objectKey);
            // 可以选择覆盖、重命名、或返回已存在文件URL
            return obsUtils.generateFileUrl(objectKey);
        }

        // 3. 上传
        String url = obsUtils.uploadStream(file, objectKey);

        // 4. TODO 可在数据库中记录文件信息（如文件名、URL、上传者）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyFile myFile = new MyFile();
        myFile.setOriginName(file.getOriginalFilename());
        myFile.setObjectKey(objectKey);
        myFile.setAuthorId(new BigInteger(authentication.getName()));
        myFileMapper.insert(myFile);
        // fileRepository.save(...)

        // 5. 返回结果
        return url;
    }

    /**
     * 删除文件的业务逻辑封装
     * @param objectKey OBS路径
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean deleteFile(String objectKey) {
        // 数据库中不存在则直接返回
        if(myFileMapper.selectByObjectKey(objectKey) == null){
            throw new BizException("数据库："+MessageConstants.FILE_NOT_EXIST);
        }else if (!obsUtils.doesFileExist(objectKey)) {
            throw new BizException("OBS桶："+MessageConstants.FILE_NOT_EXIST);
        }
        myFileMapper.delete(objectKey);
        return obsUtils.deleteFile(objectKey);
    }

    /**
     * 列举所有对象
     * @return 列举结果
     */
    @Override
    public ObjectListing listFile(){
        ObjectListing result = obsUtils.listFile();
        for(ObsObject obsObject : result.getObjects()){
            obsObject.getObjectKey();
            obsObject.getObjectContent();
            obsObject.getOwner();
        }
        return result;
    }

    /**
     * 流式下载文件
     * @param objectKey 文件所在路径
     * @return StreamingResponseBody异步下载流
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResponseEntity<StreamingResponseBody> downloadFile(String objectKey) {
        // 先判断文件是否存在
        if (!obsUtils.doesFileExist(objectKey)) {
            throw new BizException(MessageConstants.FILE_NOT_EXIST);
        }

        // 获取ObsObject
        ObsObject obsObject = obsUtils.downloadFile(objectKey);
        // 获取文件输入流
        InputStream inputStream = obsObject.getObjectContent();
        // 文件名
        String fileName = obsObject.getObjectKey();
        // 使用UTF8编码，解决中文乱码问题
        String encodeFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        // 设置文件类型
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        // 设置文件访问类型，inline为预览模式，attachment为直接下载
        headers.setContentDispositionFormData("inline", encodeFileName);

        // 设置异步下载流并关闭
        StreamingResponseBody stream = outputStream -> {
            inputStream.transferTo(outputStream);
            inputStream.close();
        };
        return new ResponseEntity<>(stream, headers, HttpStatus.OK);
    }

    @Override
    public String downloadFileUrl(String objectKey) {
        // 先判断文件是否存在
        if(myFileMapper.selectByObjectKey(objectKey) == null){
            throw new BizException(MessageConstants.FILE_NOT_EXIST);
        }else if (!obsUtils.doesFileExist(objectKey)) {
            throw new BizException(MessageConstants.FILE_NOT_EXIST);
        }
        String result = obsUtils.generateFileUrl(objectKey);
        return result;
    }
}
