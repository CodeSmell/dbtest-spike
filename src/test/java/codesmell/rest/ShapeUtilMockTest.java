package codesmell.rest;

import codesmell.config.BeanConfig;
import codesmell.dao.mybatis.ShapeDao;
import codesmell.dao.mybatis.ShapeDto;
import org.apache.ibatis.exceptions.PersistenceException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.*;

/**
 * testing the Shape object and the underlying DB interactions
 * with Mocks
 */
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {
    BeanConfig.class,
    ShapeUtilMockTest.TestConfig.class
})
class ShapeUtilMockTest {
    
    @Autowired
    private ShapeUtil shapeUtil;
    
    @Autowired
    private ShapeDao mockShapeDao;
    
    @Test
    void test_find_shape_not_found() {
        Mockito.doReturn(null).when(mockShapeDao).findShapeById(Mockito.anyLong());
        
        Shape myShape = shapeUtil.fetchShape(999L);
        assertNull(myShape);
    }
    
    @Test
    void test_find_shape_id() {
        Mockito.doAnswer(invocation -> {
            Long theId = invocation.getArgument(0);
            return ShapeDto.builder()
                .id(theId)
                .shape("Octagon")
                .color("Red")
                .build();
        }).when(mockShapeDao).findShapeById(Mockito.anyLong());
        
        Shape myShape = shapeUtil.fetchShape(100L);
        assertNotNull(myShape);
        assertEquals(100L, myShape.getId());
        assertEquals("Octagon", myShape.getShape());
        assertEquals("Red", myShape.getColor());
    }
    
    @Test
    void test_find_shape_data() {
        ShapeDto dto = ShapeDto.builder()
                .id(1L)
                .shape("Square")
                .color("Blue")
                .build();

        Mockito.doReturn(dto)
            .when(mockShapeDao).findByShapeColor("Square","Blue");
        
        Shape myShape = shapeUtil.fetchShape("Square","Blue");
        assertNotNull(myShape);
        assertEquals(1L, myShape.getId());
        assertEquals("Square", myShape.getShape());
        assertEquals("Blue", myShape.getColor());
    }
    
    
    @Test
    void test_persist() {
        Mockito.doNothing()
            .when(mockShapeDao).upsertShape(Mockito.any(ShapeDto.class));
        
        Shape myShape = Shape.builder()
            .shape("Octagon")
            .color("Red")
            .build();
        
        shapeUtil.persist(myShape);
    }
    
    @Test
    void test_persist_exception() {
        Mockito.doThrow(PersistenceException.class)
            .when(mockShapeDao).upsertShape(Mockito.any(ShapeDto.class));
        
        Shape myShape = Shape.builder()
            .shape("Octagon")
            .color("Red")
            .build();
    
        assertThrows(PersistenceException.class, () -> shapeUtil.persist(myShape));
    }
    
    @TestConfiguration
    public static class TestConfig {
        
        @Bean
        ShapeDao buildMockDao() {
            return Mockito.mock(ShapeDao.class);
        }
    }

}
