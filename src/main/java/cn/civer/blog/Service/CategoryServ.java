package cn.civer.blog.Service;

import cn.civer.blog.Model.Entity.Category;
import cn.civer.blog.Model.Entity.Result;

import java.math.BigInteger;

public interface CategoryServ {
    Result categoryInsert(Category category);
    Result categoryDelete(Category category);
    Result categorySelectById(BigInteger categoryId);
    Result categorySelectByTitle(String title);
    Result categoryUpdate(Category category);
}
