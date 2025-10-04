package cn.civer.blog.Service;

import cn.civer.blog.Model.Entity.Label;
import cn.civer.blog.Model.Entity.Result;

import java.math.BigInteger;

public interface LabelServ {
    public Label findOrCreate(String title, String summary, BigInteger authorId);
    Result labelInsert(String title,String summary);
    Result labelDelete(BigInteger labelId);
    Result labelUpdate(BigInteger labelId,String title,String summary,Integer status);
    Result labelSelectById(BigInteger labelId);
    Result labelSelectByTitle(String title);
    Result labelSelectByAll();
}
