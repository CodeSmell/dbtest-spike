package codesmell.dao.mybatis;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ShapeDao {
    
    void insertShape(@Param("shape") ShapeDto shape);
    
    void upsertShape(@Param("shape") ShapeDto shape);
    
    ShapeDto findShapeById(@Param("id") Long id);

}
