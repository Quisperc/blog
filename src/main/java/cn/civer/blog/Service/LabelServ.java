package cn.civer.blog.Service;

import cn.civer.blog.Model.Entity.Label;

import java.math.BigInteger;
import java.util.List;

public interface LabelServ {
    Label findOrCreate(String title, BigInteger authorId);
    Boolean labelInsert(String title);
    Boolean labelDelete(BigInteger labelId);
    Boolean labelUpdate(BigInteger labelId, String title, Integer status);
    Label labelSelectById(BigInteger labelId);
    Label labelSelectByTitle(String title);
    List<Label> labelSelectByAll();
}
