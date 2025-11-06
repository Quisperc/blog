package cn.civer.blog.Mapper;

import cn.civer.blog.Model.Entity.Label;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigInteger;
import java.util.List;

@Mapper
public interface LabelMapper {
    /**
     * 插入分类
     * @param label 分类
     * @return 修改行数
     */
    int insert(Label label);

    /**
     * 更新分类
     * @param label 分类信息
     * @return 修改行数
     */
    int update(Label label);

    /**
     * 根据分类Id查询分类
     * @param id 分类ID
     * @return 分类
     */
    @Select("select *from t_label where id = #{id}")
    Label selectById(BigInteger id);

    /**
     * 根据分类名查询分类
     * @param title 分类
     * @return 分类
     */
    @Select("select *from t_label where title = #{title}")
    Label selectByTitle(String title);

    /**
     * 查询所有分类
     * @return 所有分类
     */
    @Select("select *from t_label")
    List<Label> selectAll();

    /**
     * 根据多个标签ID批量查询标签（用于批量组装）
     * @param ids 标签ID列表
     * @return 标签列表
     */
    List<Label> selectByIds(@Param("ids") List<BigInteger> ids);

    /**
     * 根据分类Id删除分类
     * @param id 分类Id
     * @return 修改行数
     */
    @Delete("delete from t_label where id = #{id}")
    int deleteById(BigInteger id);

    /**
     * 根据分类名删除分类
     * @param title 分类名
     * @return 修改行数
     */
    @Delete("delete from t_label where title = #{title}")
    int deleteByTitle(String title);
}
