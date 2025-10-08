package cn.civer.blog.Service;

import cn.civer.blog.Model.Entity.Category;

import java.math.BigInteger;
import java.util.List;

public interface CategoryServ {
    public Category findOrCreate(String title, BigInteger authorId);
    Boolean categoryInsert(String title);
    Boolean categoryDelete(BigInteger categoryId);
    Boolean categoryUpdate(BigInteger categoryId, String title, Integer status);
    Category categorySelectById(BigInteger categoryId);
    Category categorySelectByTitle(String title);
    List<Category> categorySelectByAll();
}
