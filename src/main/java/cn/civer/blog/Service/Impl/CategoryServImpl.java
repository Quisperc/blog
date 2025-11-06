package cn.civer.blog.Service.Impl;

import cn.civer.blog.Exception.BizException;
import cn.civer.blog.Mapper.CategoryMapper;
import cn.civer.blog.Model.Entity.Category;
import cn.civer.blog.Model.Entity.MessageConstants;
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

    /**
     * 返回用户ID
     * @return 用户Id
     */
    private BigInteger getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return new BigInteger(authentication.getName());
    }

    /**
     * 获取分类
     * @param title    标题
     * @param authorId 作者Id
     * @return 分类
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Category findOrCreate(String title, BigInteger authorId) {
        
        // 查询分类是否存在
        Category category = categoryMapper.selectByTitle(title);
        if (category != null) return category;
        // 创建新分类
        Category newCategory = new Category();
        newCategory.setTitle(title);
        newCategory.setAuthorId(authorId);
        categoryMapper.insert(newCategory);
        
        log.info(MessageConstants.CATEGORY_INSERT_SUCCESS + ": {}", title);
        return newCategory;
    }

    /**
     * 插入分类
     * @param title 分类标题
     * @return 插入成功结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean categoryInsert(String title) {
        // 获取用户Id
        BigInteger userId = getCurrentUserId();
        // 查询分类是否存在
        Category category = categoryMapper.selectByTitle(title);
        if (category != null)
            throw new BizException(MessageConstants.CATEGORY_EXIST);

        // 创建新分类
        Category newcategory = new Category();
        newcategory.setAuthorId(userId); // 创作者ID
        newcategory.setTitle(title); // 分类标题
        categoryMapper.insert(newcategory);
        
        log.info(MessageConstants.CATEGORY_INSERT_SUCCESS + ": {}", title);
        return Boolean.TRUE;
    }

    /**
     * 根据分类Id删除分类
     *
     * @param categoryId 分类Id
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean categoryDelete(BigInteger categoryId) {
        if (categoryMapper.selectById(categoryId) == null) {
            throw new BizException(MessageConstants.CATEGORY_NOT_EXIST+ ": " + categoryId);
        }
        // 删除分类
        categoryMapper.deleteById(categoryId);
        log.info(MessageConstants.CATEGORY_DELETE_SUCCESS + ": {}", categoryId);
        return Boolean.TRUE;
    }

    /**
     * 更新分类
     *
     * @param categoryId 分类Id
     * @param title   分类名
     * @param status  分类状态
     * @return 更新结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean categoryUpdate(BigInteger categoryId, String title, Integer status) {
        // 查询分类
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BizException(MessageConstants.CATEGORY_NOT_EXIST + ": " + title);
        }
        if (StringUtils.hasText(title)) {
            category.setTitle(title);
        }
        if (status != null) {
            category.setStatus(status);
        }
        categoryMapper.update(category);
        log.info(MessageConstants.CATEGORY_UPDATE_SUCCESS + ": {}", title);
        return Boolean.TRUE;
    }

    /**
     * 根据Id查询分类
     *
     * @param categoryId 分类Id
     * @return 查询结果Category
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Category categorySelectById(BigInteger categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        log.info(MessageConstants.CATEGORY_SELECT_SUCCESS+": {}",category.getTitle());
        return category;
    }

    /**
     * 根据分类名查询分类
     *
     * @param title 分类名
     * @return 分类
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Category categorySelectByTitle(String title) {
        Category category = categoryMapper.selectByTitle(title);
        log.info(MessageConstants.CATEGORY_SELECT_SUCCESS+": {}",title);
        return category;
    }

    /**
     * 查询所有分类
     *
     * @return 所有分类
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<Category> categorySelectByAll() {
        List<Category> categorys = categoryMapper.selectAll();
        log.info(MessageConstants.CATEGORY_SELECT_SUCCESS);
        return categorys;
    }
}
