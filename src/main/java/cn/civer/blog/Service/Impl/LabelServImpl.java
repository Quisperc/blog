package cn.civer.blog.Service.Impl;

import cn.civer.blog.Mapper.LabelMapper;
import cn.civer.blog.Model.Entity.Label;
import cn.civer.blog.Model.Entity.MessageConstants;
import cn.civer.blog.Model.Entity.Result;
import cn.civer.blog.Service.LabelServ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.util.List;

@Slf4j
@Service
public class LabelServImpl implements LabelServ {
    @Autowired
    private LabelMapper labelMapper;

    private BigInteger getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return new BigInteger(authentication.getName());
    }

    /**
     * 获取标签
     * @param title    标题
     * @param summary  介绍
     * @param authorId 作者Id
     * @return 标签
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Label findOrCreate(String title, String summary, BigInteger authorId) {
        Label label = labelMapper.selectByTitle(title);
        if (label != null) return label;

        Label newLabel = new Label();
        newLabel.setTitle(title);
        newLabel.setSummary(summary);
        newLabel.setAuthorId(authorId);
        labelMapper.insert(newLabel);
        log.info(MessageConstants.LABEL_INSERT_SUCCESS + ": {}", title);
        return newLabel;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result labelInsert(String title, String summary) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BigInteger userId = new BigInteger(auth.getName());
        Label label = labelMapper.selectByTitle(title);
        if (label != null)
            return Result.error(MessageConstants.LABEL_EXIST);

        Label newlabel = new Label();
        newlabel.setAuthorId(userId); // 创作者ID
        newlabel.setTitle(title); // 标签标题
        newlabel.setSummary(summary); // 标签介绍
        labelMapper.insert(newlabel);

        return Result.success(MessageConstants.LABEL_INSERT_SUCCESS + ": {}", title);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result labelDelete(BigInteger labelId) {
        if (labelMapper.selectById(labelId) == null) {
            return Result.error(MessageConstants.LABEL_NOT_EXIST);
        }
        // Id不为空
        labelMapper.deleteById(labelId);
        return Result.success(MessageConstants.LABEL_DELETE_SUCCESS);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result labelUpdate(BigInteger labelId, String title, String summary, Integer status) {
        Label label = labelMapper.selectById(labelId);
        if (label == null) {
            return Result.error(MessageConstants.LABEL_NOT_EXIST + ": {}", title);
        }
        if (StringUtils.hasText(title)) {
            label.setTitle(title);
        }
        if (StringUtils.hasText(summary)) {
            label.setSummary(summary);
        }
        if (status != null) {
            label.setStatus(status);
        }
        labelMapper.update(label);
        return Result.success(MessageConstants.LABEL_UPDATE_SUCCESS);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result labelSelectById(BigInteger labelId) {
        Label label = labelMapper.selectById(labelId);
        log.info(MessageConstants.LABEL_SELECT_SUCCESS);
        return Result.success(label);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result labelSelectByTitle(String title) {
        Label label = labelMapper.selectByTitle(title);
        log.info(MessageConstants.LABEL_SELECT_SUCCESS);
        return Result.success(label);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result labelSelectByAll() {
        List<Label> labels = labelMapper.selectAll();
        return Result.success(labels);
    }
}
