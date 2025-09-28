package cn.civer.blog.Service.Impl;

import cn.civer.blog.Mapper.CategoryMapper;
import cn.civer.blog.Model.Entity.Category;
import cn.civer.blog.Model.Entity.Result;
import cn.civer.blog.Service.CategoryServ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Slf4j
@Service
public class CategoryServImpl implements CategoryServ {
    @Autowired
    private CategoryMapper categoryMapper;
    @Override
    public Result categoryInsert(Category category) {
        try {
            categoryMapper.insert(category);
            log.info("分类("+category.getTitle()+")插入成功");
            return Result.success("分类插入成功");
        } catch (Exception e) {
            log.error("分类插入失败");
            return Result.error("分类插入失败");
        }
    }

    @Override
    public Result categoryDelete(Category category) {
        try {
            if(category.getId() != null){
                // Id不为空
                categoryMapper.deleteById(category.getId());
                log.info("分类("+category.getTitle()+")删除成功");
                return Result.success("分类删除成功");
            } else if (category.getTitle() != null) {
                // Ttitle 不为空
                categoryMapper.deleteByTitle(category.getTitle());
                log.info("分类("+category.getTitle()+")删除成功");
                return Result.success("分类删除成功");
            }
            log.warn("分类删除失败");
            return Result.error("分类删除失败");
        } catch (Exception e) {
            log.error("分类删除失败");
            return Result.error("分类删除失败");
        }
    }

    @Override
    public Result categorySelectById(BigInteger categoryId) {
        try {
            categoryMapper.selectById(categoryId);
            log.info("分类查询成功");
            return Result.success("分类查询成功");
        } catch (Exception e) {
            log.error("分类查询失败"+e);
            return Result.error("分类查询失败");
        }
    }

    @Override
    public Result categorySelectByTitle(String title) {
        return null;
    }

    @Override
    public Result categoryUpdate(Category category) {
        return null;
    }
}
