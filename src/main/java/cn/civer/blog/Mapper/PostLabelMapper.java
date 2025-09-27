package cn.civer.blog.Mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigInteger;
import java.util.List;

@Mapper
public interface PostLabelMapper {
    /**
     * 插入新的文章对应标签
     * @param postId 文章ID
     * @param labelId 标签ID
     * @return 修改行数
     */
    int insert(BigInteger postId,BigInteger labelId);

    /**
     * 根据文章ID删除记录
     * @param postId 文章ID
     * @return 修改行数
     */
    @Delete("delete from t_post_label where post_id = #{postId}")
    int deleteByPostId(BigInteger postId);

    /**
     * 根据标签ID删除记录
     * @param labelId 标签ID
     * @return 修改行数
     */
    @Delete("delete from t_post_label where label_id = #{labelId}")
    int deleteBylabelId(BigInteger labelId);

    /**
     * 根据文章ID查询标签
     * @param postId 文章ID
     * @return 标签ID集合
     */
    @Select("select label_id from t_post_label where post_id = #{postId}")
    List<BigInteger> selectByPostId(BigInteger postId);

    /**
     * 根据标签ID查询文章
     * @param labelId 标签ID
     * @return 文章ID集合
     */
    @Select("select post_id from t_post_label where label_id = #{labelId}")
    List<BigInteger> selectBylabelId(BigInteger labelId);

    /**
     * 根据文章ID和标签ID获取记录ID
     * @param postId 文章ID
     * @param labelId 记录ID
     * @return 记录ID
     */
    @Select("select id from t_post_label where post_id = #{postId} and label_id = #{labelId}")
    List<BigInteger> selectByPostAndlabel(BigInteger postId,BigInteger labelId);

    /**
     * 更新记录（更新文章标签
     * @param id 记录ID
     * @param labelId 修改后的标签ID
     * @return 修改行数
     */
    int update(BigInteger id, BigInteger labelId);
}
