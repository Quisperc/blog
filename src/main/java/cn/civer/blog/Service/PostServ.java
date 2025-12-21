package cn.civer.blog.Service;

import cn.civer.blog.Model.DTO.PostDTO;
import cn.civer.blog.Model.Entity.Post;

import java.math.BigInteger;
import java.util.List;

public interface PostServ {
    // 新增文章
    Boolean postAdd(PostDTO post);
    // 删除文章
    Boolean postDeleteById(BigInteger id);
//    Result postDeleteByTitle(String title);
    Boolean postDeleteByCategory(BigInteger categoryId);
    Boolean postDeleteByLabel(BigInteger labelId);
    Boolean postDeleteByUserId(BigInteger userId);
    // 修改文章
    Boolean postUpdate(BigInteger postId, PostDTO postDTO);
    // 查询文章
    Post postSelectById(BigInteger id);
    List<Post> postSelectByTitle(String title);
    List<Post> postSelectByAuthor(BigInteger authorId);
    List<Post> postSelectByCategory(BigInteger categoryId);
    List<Post> postSelectByLabel(BigInteger labelId);
    List<Post> postSelectAll();
    Boolean postIncreLikes(BigInteger postId);
    Boolean postIncreViews(BigInteger postId);

    // 轻量缓存：按分类/标签返回文章 ID 列表，供复用 postsById 缓存加载
    List<BigInteger> postIdsByCategory(BigInteger categoryId);
    List<BigInteger> postIdsByLabel(BigInteger labelId);
    List<BigInteger> postIdsByTitle(String title);
    List<BigInteger> postIdsByAuthor(BigInteger authorId);
}
