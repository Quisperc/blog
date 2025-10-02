package cn.civer.blog.Service;

import cn.civer.blog.Model.Entity.Result;

import java.math.BigInteger;

public interface CategoryServ {
    Result categoryInsert(String title,String summary);
    Result categoryDelete(BigInteger categoryId);
    Result categoryUpdate(BigInteger categoryId,String title,String summary,Integer status);
    Result categorySelectById(BigInteger categoryId);
    Result categorySelectByTitle(String title);
}
