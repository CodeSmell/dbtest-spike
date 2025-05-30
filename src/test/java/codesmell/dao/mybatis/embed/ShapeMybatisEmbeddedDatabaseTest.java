package codesmell.dao.mybatis.embed;

import codesmell.config.MyBatisConfig;
import codesmell.dao.mybatis.ShapeDao;
import codesmell.dao.mybatis.ShapeDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Sql(scripts = "/shape.ddl", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=none"
})
@ContextConfiguration(classes = {
    MyBatisConfig.class
})
class ShapeMybatisEmbeddedDatabaseTest {

    @Autowired
    ShapeDao dao;

    @Test
    void test_collision_on_insert() {
        ShapeDto newShape = new ShapeDto();
        newShape.setShape("Square");
        newShape.setColor("Blue");

        assertNull(newShape.getId());

        assertThrows(
            DuplicateKeyException.class,
            () -> dao.insertShape(newShape)
        );
    }
    
    /**
     * this fails b/c H2 does not support upsert
     */
    @Test
    void test_collision_on_upsert() {
        ShapeDto newShape = new ShapeDto();
        newShape.setShape("Square");
        newShape.setColor("Blue");
        newShape.setDescription("UPSERT");

        assertNull(newShape.getId());

        assertThrows(
            BadSqlGrammarException.class,
            () -> dao.upsertShape(newShape)
        );
    }
    
    @Test
    void test_insert_and_retrieve() {
        ShapeDto newShape = new ShapeDto();
        newShape.setShape("Sphere");
        newShape.setColor("Gray");

        assertNull(newShape.getId());

        // insert
        dao.insertShape(newShape);
        assertNotNull(newShape.getId());
        assertEquals("Sphere", newShape.getShape());
        assertEquals("Gray", newShape.getColor());

        ShapeDto fetchShape = dao.findShapeById(newShape.getId());
        assertEquals(fetchShape.getId(), newShape.getId());
        assertEquals("Sphere", fetchShape.getShape());
        assertEquals("Gray", fetchShape.getColor());
    }
    
    @Test
    void test_not_found_by_id() {
        ShapeDto dto = dao.findShapeById(Long.MAX_VALUE);
        assertNull(dto);
    }
    
    @Test
    void test_not_found_by_data() {
        ShapeDto dto = dao.findByShapeColor("Circle", "Green");
        assertNull(dto);
    }

}
