package cn.civer.blog.Service.Impl;

import cn.civer.blog.Mapper.LabelMapper;
import cn.civer.blog.Model.Entity.Label;
import cn.civer.blog.Model.Entity.Result;
import cn.civer.blog.Service.LabelServ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Slf4j
@Service
public class LabelServImpl implements LabelServ {
    @Autowired
    private LabelMapper labelMapper;
    @Override
    public Result labelInsert(String title,String summary) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BigInteger userId = new BigInteger(auth.getName());
        try {
            Label label = labelMapper.selectByTitle(title);
            if(label != null)
                return Result.error("标签已存在");
            
            Label newlabel = new Label();
            newlabel.setAuthorId(userId); // 创作者ID
            newlabel.setTitle(title); // 标签标题
            newlabel.setSummary(summary); // 标签介绍
            labelMapper.insert(label);
            
            return Result.success("标签插入成功");
        } catch (Exception e) {
            return Result.error("标签插入失败"+e);
        }
    }

    @Override
    public Result labelDelete(BigInteger labelId) {
        try {
            if(labelMapper.selectById(labelId) == null){
                return Result.error("删除失败，标签不存在");
            } 
            // Id不为空
            labelMapper.deleteById(labelId);
            return Result.success("标签删除成功");
        } catch (Exception e) {
            return Result.error("标签删除失败："+e);
        }
    }

    @Override
    public Result labelUpdate(BigInteger labelId, String title,String summary,Integer status) {
        try {
            Label label = labelMapper.selectById(labelId);
            if(label == null){
                return Result.error(title+"标签不存在");
            }
            if(!"".equals(title)){
                label.setTitle(title);
            }
            if(!"".equals(summary)){
                label.setSummary(summary);
            }
            if(status != null){
                label.setStatus(status);
            }
            labelMapper.update(label);
            return Result.success(title+"标签更新成功");
        } catch (Exception e) {
            return Result.error("标签更新失败："+e);
        }
    }
    @Override
    public Result labelSelectById(BigInteger labelId) {
        try {
            Label label =  labelMapper.selectById(labelId);
            log.info("标签查询成功");
            return Result.success(label);
        } catch (Exception e) {
            log.error("标签查询失败"+e);
            return Result.error("标签查询失败");
        }
    }

    @Override
    public Result labelSelectByTitle(String title) {
        try {
            Label label =  labelMapper.selectByTitle(title);
            log.info("标签查询成功");
            return Result.success(label);
        } catch (Exception e) {
            log.error("标签查询失败"+e);
            return Result.error("标签查询失败");
        }
    }
}
