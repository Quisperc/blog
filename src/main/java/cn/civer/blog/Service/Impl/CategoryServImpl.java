package cn.civer.blog.Service.Impl;

import cn.civer.blog.Mapper.CategoryMapper;
import cn.civer.blog.Model.Entity.Category;
import cn.civer.blog.Model.Entity.MessageConstants;
import cn.civer.blog.Model.Entity.Result;
import cn.civer.blog.Service.CategoryServ;
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
public class CategoryServImpl implements CategoryServ {
    @Autowired
    private CategoryMapper categoryMapper;

    private BigInteger getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return new BigInteger(authentication.getName());
    }

    /**
     * 获取分类
     * @param title    标题
     * @param summary  介绍
     * @param authorId 作者Id
     * @return 分类
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Category findOrCreate(String title, String summary, BigInteger authorId) {
        Category category = categoryMapper.selectByTitle(title);
        if (category != null) return category;

        Category newCategory = new Category();
        newCategory.setTitle(title);
        newCategory.setSummary(summary);
        newCategory.setAuthorId(authorId);
        categoryMapper.insert(newCategory);
        log.info(MessageConstants.CATEGORY_INSERT_SUCCESS + ": {}", title);
        return newCategory;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result categoryInsert(String title, String summary) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BigInteger userId = new BigInteger(auth.getName());
        Category category = categoryMapper.selectByTitle(title);
        if (category != null)
            return Result.error(MessageConstants.CATEGORY_EXIST);

        Category newcategory = new Category();
        newcategory.setAuthorId(userId); // 创作者ID
        newcategory.setTitle(title); // 分类标题
        newcategory.setSummary(summary); // 分类介绍
        categoryMapper.insert(newcategory);

        return Result.success(MessageConstants.CATEGORY_INSERT_SUCCESS + ": {}", title);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result categoryDelete(BigInteger categoryId) {
        if (categoryMapper.selectById(categoryId) == null) {
            return Result.error(MessageConstants.CATEGORY_NOT_EXIST);
        }
        // Id不为空
        categoryMapper.deleteById(categoryId);
        return Result.success(MessageConstants.CATEGORY_DELETE_SUCCESS);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result categoryUpdate(BigInteger categoryId, String title, String summary, Integer status) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            return Result.error(MessageConstants.CATEGORY_NOT_EXIST + ": {}", title);
        }
        if (StringUtils.hasText(title)) {
            category.setTitle(title);
        }
        if (StringUtils.hasText(summary)) {
            category.setSummary(summary);
        }
        if (status != null) {
            category.setStatus(status);
        }
        categoryMapper.update(category);
        return Result.success(MessageConstants.CATEGORY_UPDATE_SUCCESS);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result categorySelectById(BigInteger categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        log.info(MessageConstants.CATEGORY_SELECT_SUCCESS);
        return Result.success(category);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result categorySelectByTitle(String title) {
        Category category = categoryMapper.selectByTitle(title);
        log.info(MessageConstants.CATEGORY_SELECT_SUCCESS);
        return Result.success(category);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result categorySelectByAll() {
        List<Category> categorys = categoryMapper.selectAll();
        return Result.success(categorys);
    }
}
