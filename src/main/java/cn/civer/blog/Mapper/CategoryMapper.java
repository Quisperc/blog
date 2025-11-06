package cn.civer.blog.Mapper;

import cn.civer.blog.Model.Entity.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigInteger;
import java.util.List;

@Mapper
public interface CategoryMapper {
    /**
     * 插入分类
     * @param category 分类
     * @return 修改行数
     */
    int insert(Category category);

    /**
     * 更新分类
     * @param category 分类信息
     * @return 修改行数
     */
    int update(Category category);

    /**
     * 根据分类Id查询分类
     * @param id 分类ID
     * @return 分类
     */
    @Select("select *from t_category where id = #{id}")
    Category selectById(BigInteger id);

    /**
     * 根据分类名查询分类
     * @param title 分类
     * @return 分类
     */
    @Select("select *from t_category where title = #{title}")
    Category selectByTitle(String title);

    /**
     * 查询所有分类
     * @return 所有分类
     */
    @Select("select *from t_category")
    List<Category> selectAll();

    /**
     * 根据多个分类ID批量查询分类（用于批量组装）
     * @param ids 分类ID列表
     * @return 分类列表
     */
    List<Category> selectByIds(@Param("ids") List<BigInteger> ids);

    /**
     * 根据分类Id删除分类
     * @param id 分类Id
     * @return 修改行数
     */
    @Delete("delete from t_category where id = #{id}")
    int deleteById(BigInteger id);

    /**
     * 根据分类名删除分类
     * @param title 分类名
     * @return 修改行数
     */
    @Delete("delete from t_category where title = #{title}")
    int deleteByTitle(String title);
}
