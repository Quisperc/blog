package cn.civer.blog.Mapper;

import cn.civer.blog.Model.Entity.MyFile;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigInteger;
import java.util.List;

@Mapper
public interface MyFileMapper {
    // TODO 修改文件名以及路径

    @Insert("INSERT INTO t_file(origin_name, author_id, object_key, upload_time)" +
            " VALUES (#{originName},#{authorId},#{objectKey},NOW())")
    int insert(MyFile myFile);

    @Delete("DELETE from t_file where object_key = #{objectKey}")
    int delete(String objectKey);

    @Select("SELECT * FROM t_file where object_key = #{objectKey}")
    MyFile selectByObjectKey(String myFile);

    @Select("SELECT * FROM t_file WHERE origin_name = #{originName}")
    List<MyFile> selectByOriginName(String originName);

    @Select("SELECT * FROM t_file WHERE author_id = #{authorId}")
    List<MyFile> selectByAuthorId(BigInteger authorId);

    @Select("SELECT * FROM t_file WHERE id = #{id}")
    List<MyFile> selectById(BigInteger id);

    @Select("SELECT * FROM t_file")
    List<MyFile> select();

}
