package cn.civer.blog.Service;

import cn.civer.blog.Model.DTO.PostDTO;
import cn.civer.blog.Model.Entity.Category;
import cn.civer.blog.Model.Entity.Label;
import cn.civer.blog.Model.Entity.Post;
import cn.civer.blog.Model.Entity.Result;

import java.math.BigInteger;

public interface PostServ {
    // 新增文章
    Result postAdd(PostDTO post);
    // 删除文章
    Result postDeleteById(BigInteger id);
//    Result postDeleteByTitle(String title);
    Result postDeleteByCategory(BigInteger categoryId);
    Result postDeleteByLabel(BigInteger labelId);
    Result postDeleteByUserId(BigInteger userId);
    // 修改文章
    Result postUpdate(BigInteger postId,PostDTO postDTO);
    // 查询文章
    Result postSelectById(BigInteger id);
    Result postSelectByTitle(String title);
    Result postSelectByCategory(BigInteger categoryId);
    Result postSelectByLabel(BigInteger labelId);
    Result postSelectAll();
}
