package cn.civer.blog.Mapper;

import cn.civer.blog.Model.DTO.PostCategoryDTO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigInteger;
import java.util.List;

@Mapper
public interface PostCategoryMapper {
    /**
     * 插入新的文章对应分类
     * @param postId 文章ID
     * @param categoryId 分类ID
     * @return 记录Id
     */
//    int insert(@Param("postId") BigInteger postId, @Param("categoryId")BigInteger categoryId);

    /**
     * 插入新的文章对应分类
     * @param postCategoryDTO 文章分类DTO
     * @return 记录ID
     */
    int insert(PostCategoryDTO postCategoryDTO);

    /**
     * 根据文章ID删除文章
     * @param postId 文章ID
     * @return 修改行数
     */
    @Delete("delete from t_post_category where post_id = #{postId}")
    int deleteByPostId(BigInteger postId);

    /**
     * 根据分类ID删除文章
     * @param categoryId 分类ID
     * @return 修改行数
     */
    @Delete("delete from t_post_category where category_id = #{categoryId}")
    int deleteByCategoryId(BigInteger categoryId);

    /**
     * 根据文章ID查询分类
     * @param postId 文章ID
     * @return 分类ID集合
     */
    @Select("select category_id from t_post_category where post_id = #{postId}")
    List<BigInteger> selectByPostId(BigInteger postId);

    /**
     * 根据分类ID查询文章
     * @param categoryId 分类ID
     * @return 文章ID集合
     */
    @Select("select post_id from t_post_category where category_id = #{categoryId}")
    List<BigInteger> selectByCategoryId(BigInteger categoryId);

    /**
     * 根据多个文章ID批量查询记录（用于批量组装）
     * @param postIds 文章ID集合
     * @return 包含 post_id 和 category_id 的 DTO 列表
     */
    List<PostCategoryDTO> selectByPostIds(@Param("postIds") List<BigInteger> postIds);

    /**
     * 根据文章ID和分类ID获取记录
     * @param postCategoryDTO 文章-分类DTO
     * @return 记录ID
     */
    @Select("select id from t_post_category where post_id = #{postId} and category_id = #{categoryId}")
    List<BigInteger> selectByPostAndCategory(PostCategoryDTO postCategoryDTO);

    /**
     * 根据记录ID修改文章对应的分类
     * @param id 记录ID
     * @param categoryId 修改后的分类ID
     * @return 修改行数
     */
    int update(BigInteger id, BigInteger categoryId);

    int insertIfNotExist(@Param("postId") BigInteger postId,@Param("categoryId") BigInteger categoryId);

    /**
     * 根据文章ID批量删除记录
     * @param postIds 文章ID集合
     * @return 修改行数
     */
    int deleteByPostIds(@Param("postIds") List<BigInteger> postIds);
}
