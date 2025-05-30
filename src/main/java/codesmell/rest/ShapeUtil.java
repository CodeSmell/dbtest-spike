package codesmell.rest;

import codesmell.dao.mybatis.ShapeDao;
import codesmell.dao.mybatis.ShapeDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

public class ShapeUtil {

    @Autowired
    private ShapeDao dao;

    public Long persist(Shape shape) {
        Long theId = null;

        if (Objects.nonNull(shape)
            && Objects.nonNull(shape.getShape())
            && Objects.nonNull(shape.getColor())) {

            ShapeDto dto = new ShapeDto();
            dto.setId(shape.getId());
            dto.setShape(shape.getShape());
            dto.setColor(shape.getColor());
            dto.setDescription(shape.getDescription());

            dao.upsertShape(dto);

            theId = dto.getId();
            shape.setId(theId);
        }

        return theId;
    }

    public Shape fetchShape(Long id) {
        ShapeDto dto = dao.findShapeById(id);
        
        if (Objects.nonNull(dto)) {
           return Shape.builder()
                .id(dto.getId())
                .shape(dto.getShape())
                .color(dto.getColor())
                .description(dto.getDescription())
                .build();
        } else {
            return null;
        }
    }
    
    public Shape fetchShape(String shape, String color) {
        ShapeDto dto = dao.findByShapeColor(shape, color);
        
        if (Objects.nonNull(dto)) {
           return Shape.builder()
                .id(dto.getId())
                .shape(dto.getShape())
                .color(dto.getColor())
                .description(dto.getDescription())
                .build();
        } else {
            return null;
        }
    }
}
