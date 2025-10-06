package cn.civer.blog.Service;

import cn.civer.blog.Model.Entity.Category;

import java.math.BigInteger;
import java.util.List;

public interface CategoryServ {
    public Category findOrCreate(String title, String summary, BigInteger authorId);
    Boolean categoryInsert(String title,String summary);
    Boolean categoryDelete(BigInteger categoryId);
    Boolean categoryUpdate(BigInteger categoryId, String title, String summary, Integer status);
    Category categorySelectById(BigInteger categoryId);
    Category categorySelectByTitle(String title);
    List<Category> categorySelectByAll();
}
