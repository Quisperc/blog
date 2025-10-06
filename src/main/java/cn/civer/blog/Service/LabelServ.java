package cn.civer.blog.Service;

import cn.civer.blog.Model.Entity.Label;

import java.math.BigInteger;
import java.util.List;

public interface LabelServ {
    public Label findOrCreate(String title, String summary, BigInteger authorId);
    Boolean labelInsert(String title,String summary);
    Boolean labelDelete(BigInteger labelId);
    Boolean labelUpdate(BigInteger labelId, String title, String summary, Integer status);
    Label labelSelectById(BigInteger labelId);
    Label labelSelectByTitle(String title);
    List<Label> labelSelectByAll();
}
