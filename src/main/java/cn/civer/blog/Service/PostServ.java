package cn.civer.blog.Service;

import cn.civer.blog.Model.Entity.Category;
import cn.civer.blog.Model.Entity.Label;
import cn.civer.blog.Model.Entity.Post;
import cn.civer.blog.Model.Entity.Result;

import java.math.BigInteger;

public interface PostServ {
    // 新增文章
    Result postAdd(Post post);
    // 删除文章
    Result postDeleteById(BigInteger id);
    Result postDeleteByTitle(String title);
    Result postDeleteByCategory(Category category);
    Result postDeleteByLabel(Label label);
    // 修改文章
    Result postUpdate(Post post);
    // 查询文章
    Result postSelectById(BigInteger id);
    Result postSelectByTitle(String title);
    Result postSelectByCategory(Category category);
    Result postSelectByLabel(Label label);
}
