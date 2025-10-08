package cn.civer.blog.Service.Impl;

import cn.civer.blog.Exception.BizException;
import cn.civer.blog.Mapper.LabelMapper;
import cn.civer.blog.Model.Entity.Label;
import cn.civer.blog.Model.Entity.MessageConstants;
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

    /**
     * 返回用户ID
     * @return 用户Id
     */
    private BigInteger getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return new BigInteger(authentication.getName());
    }

    /**
     * 获取标签
     * @param title    标题
     * @param authorId 作者Id
     * @return 标签
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Label findOrCreate(String title, BigInteger authorId) {

        // 查询标签是否存在
        Label label = labelMapper.selectByTitle(title);
        if (label != null) return label;
        // 创建新标签
        Label newLabel = new Label();
        newLabel.setTitle(title);
        newLabel.setAuthorId(authorId);
        labelMapper.insert(newLabel);

        log.info(MessageConstants.LABEL_INSERT_SUCCESS + ": {}", title);
        return newLabel;
    }

    /**
     * 插入标签
     * @param title 标签标题
     * @return 插入成功结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean labelInsert(String title) {
        // 获取用户Id
        BigInteger userId = getCurrentUserId();
        // 查询标签是否存在
        Label label = labelMapper.selectByTitle(title);
        if (label != null)
            throw new BizException(MessageConstants.LABEL_EXIST);

        // 创建新标签
        Label newlabel = new Label();
        newlabel.setAuthorId(userId); // 创作者ID
        newlabel.setTitle(title); // 标签标题
        labelMapper.insert(newlabel);

        log.info(MessageConstants.LABEL_INSERT_SUCCESS + ": {}", title);
        return Boolean.TRUE;
    }

    /**
     * 根据标签Id删除标签
     *
     * @param labelId 标签Id
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean labelDelete(BigInteger labelId) {
        if (labelMapper.selectById(labelId) == null) {
            throw new BizException(MessageConstants.LABEL_NOT_EXIST+ ": " + labelId);
        }
        // 删除标签
        labelMapper.deleteById(labelId);
        log.info(MessageConstants.LABEL_DELETE_SUCCESS + ": {}", labelId);
        return Boolean.TRUE;
    }

    /**
     * 更新标签
     *
     * @param labelId 标签Id
     * @param title   标签名
     * @param status  标签状态
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean labelUpdate(BigInteger labelId, String title, Integer status) {
        // 查询标签
        Label label = labelMapper.selectById(labelId);
        if (label == null) {
            throw new BizException(MessageConstants.LABEL_NOT_EXIST + ": " + title);
        }
        if (StringUtils.hasText(title)) {
            label.setTitle(title);
        }
        if (status != null) {
            label.setStatus(status);
        }
        labelMapper.update(label);
        log.info(MessageConstants.LABEL_UPDATE_SUCCESS + ": {}", title);
        return Boolean.TRUE;
    }

    /**
     * 根据Id查询标签
     *
     * @param labelId 标签Id
     * @return 查询结果Label
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Label labelSelectById(BigInteger labelId) {
        Label label = labelMapper.selectById(labelId);
        log.info(MessageConstants.LABEL_SELECT_SUCCESS+": {}",label.getTitle());
        return label;
    }

    /**
     * 根据标签名查询标签
     *
     * @param title 标签名
     * @return 标签
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Label labelSelectByTitle(String title) {
        Label label = labelMapper.selectByTitle(title);
        log.info(MessageConstants.LABEL_SELECT_SUCCESS+": {}",title);
        return label;
    }

    /**
     * 查询所有标签
     *
     * @return 所有标签
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<Label> labelSelectByAll() {
        List<Label> labels = labelMapper.selectAll();
        log.info(MessageConstants.LABEL_SELECT_SUCCESS);
        return labels;
    }
}
