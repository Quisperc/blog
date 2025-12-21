package cn.civer.blog.Mapper;

import cn.civer.blog.Model.Entity.Post;
import org.apache.ibatis.annotations.*;

import java.math.BigInteger;
import java.util.List;

@Mapper
public interface PostMapper {
    /**
     * 查询所有文章
     * @return 文章
     */
    @Select("select * from t_post")
    List<Post> selectAll();
    /**
     * 根据ID查询文章
     * @param id 文章ID
     * @return 查询到的文章
     */
    @Select("select * from t_post where id = #{id}")
    Post selectById(BigInteger id);

    /**
     * 根据文章名查询文章
     * @param title 文章名
     * @return 文章集合
     */
    @Select("select id from t_post where title like CONCAT('%', #{title}, '%')")
    List<BigInteger> selectByTitle(String title);

    /**
     * 根据作者Id查询文章
     * @param authorId 作者Id
     * @return 文章结果
     */
    @Select("select * from t_post where author_id = #{authorId}")
    List<BigInteger> selectByAuthorId(BigInteger authorId);

    /**
     * 根据作者名查询文章
     * @param authorName 作者名
     * @return 文章结果
     */
    List<Post> selectByAuthorname(String authorName);

    /**
     * 根据分类查询文章
     * @param category 分类名
     * @return 查询到的文章
     */
    List<Post> selectByCategory(String category);

    /**
     * 根据标签查询文章
     * @param label 标签名
     * @return 查询到的文章
     */
    List<Post> selectByLabel(String label);

    /**
     * 查询指定页的文章
     * @param offset 起止点
     * @param limit 查询几个
     * @return 查询结果
     */
    List<Post> selectPage(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 新增文章
     * @param post 文章参数
     * @return 修改行数
     */
    int insert(Post post);

    /**
     * 更新文章
     * @param post 待更新的地方
     * @return 修改行数
     */
    int update(Post post);

    /**
     * 查阅+1
     * @param id 查阅的文章
     * @return 修改行数
     */
    @Update("UPDATE t_post SET views = IFNULL(views,0) + 1 WHERE id = #{id}")
    int incrementViews(@Param("id") BigInteger id);

    /**
     * 点赞+1
     * @param id 点赞的文章Id
     * @return 修改行数
     */
    @Update("UPDATE t_post SET likes = IFNULL(likes,0) + 1 WHERE id = #{id}")
    int incrementLikes(@Param("id") BigInteger id);

    /**
     * 根据ID删除文章
     * @param id 文章ID
     * @return 删除行数
     */
    @Delete("delete from t_post where id = #{id}")
    int deleteById(BigInteger id);
    /**
     * 批量删除文章
     * @param ids 文章ID集合
     * @return 删除行数
     */
    int deleteByIds(@Param("ids") List<BigInteger> ids);

    /**
     * 批量按 delta 增加 views（用于异步从 Redis 刷盘）
     * @param id 文章ID
     * @param delta 增量
     * @return 修改行数
     */
    @Update("UPDATE t_post SET views = IFNULL(views,0) + #{delta} WHERE id = #{id}")
    int incrementViewsBy(@Param("id") BigInteger id, @Param("delta") long delta);

    /**
     * 批量按 delta 增加 likes（用于异步从 Redis 刷盘）
     * @param id 文章ID
     * @param delta 增量
     * @return 修改行数
     */
    @Update("UPDATE t_post SET likes = IFNULL(likes,0) + #{delta} WHERE id = #{id}")
    int incrementLikesBy(@Param("id") BigInteger id, @Param("delta") long delta);

    /**
     * 批量按 delta 更新 views（用于异步从 Redis 刷盘）
     * @param id 文章ID
     * @param delta 增量
     * @return 修改行数
     */
    @Update("UPDATE t_post SET views = #{delta} WHERE id = #{id}")
    int updateViewsBy(@Param("id") BigInteger id, @Param("delta") long delta);

    /**
     * 批量按 delta 更新 likes（用于异步从 Redis 刷盘）
     * @param id 文章ID
     * @param delta 增量
     * @return 修改行数
     */
    @Update("UPDATE t_post SET likes = #{delta} WHERE id = #{id}")
    int updateLikesBy(@Param("id") BigInteger id, @Param("delta") long delta);
}
