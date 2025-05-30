package codesmell.rest;

import codesmell.config.BeanConfig;
import codesmell.config.MyBatisConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * testing the Shape object and the underlying DB interactions
 * with Embedded H2
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Sql(scripts = "/shape.ddl", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=none"
})
@ContextConfiguration(classes = {
    BeanConfig.class,
    MyBatisConfig.class
})
class ShapeUtilIntEmbeddedTest {
    
    @Autowired
    private ShapeUtil shapeUtil;
    
    @Test
    void test_find_shape() {
        Shape myShape = shapeUtil.fetchShape("Square", "Blue");
        assertNotNull(myShape);
        assertEquals("Square", myShape.getShape());
        assertEquals("Blue", myShape.getColor());
        
        Long theId = myShape.getId();
        Shape myShapeById = shapeUtil.fetchShape(theId);
        assertNotNull(myShapeById);
        assertEquals("Square", myShapeById.getShape());
        assertEquals("Blue", myShapeById.getColor());
    }
    
    @Test
    void test_find_shape_not_found() {
        Shape myShape = shapeUtil.fetchShape(Long.MAX_VALUE);
        assertNull(myShape);
        
        Shape myShapeById = shapeUtil.fetchShape("Square", "Red");
        assertNull(myShapeById);
    }
    
    @Test
    void test_collision_on_upsert() {
        Shape myShape = Shape.builder()
            .shape("Square")
            .color("Blue")
            .description("UPSERT")
            .build();

        assertThrows(
            BadSqlGrammarException.class,
            () -> shapeUtil.persist(myShape)
        );
    }
    
    @Test
    void test_upsert_and_retrieve() {
        Shape myShape = Shape.builder()
            .shape("Octagon")
            .color("RED")
            .description("NEW")
            .build();

        assertThrows(
            BadSqlGrammarException.class,
            () -> shapeUtil.persist(myShape)
        );
    }
    
}
