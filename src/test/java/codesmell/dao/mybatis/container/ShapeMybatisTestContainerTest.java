package codesmell.dao.mybatis.container;

import codesmell.config.MyBatisConfig;
import codesmell.dao.mybatis.ShapeDao;
import codesmell.dao.mybatis.ShapeDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.TestcontainersConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Note: should not use @TestContainers when reusing a container
 * Note: separated embedded and testcontainer tests by package 
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test-pg.properties")
//tell Spring Boot not to use an Embedded DB
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@ContextConfiguration(classes = {
    MyBatisConfig.class,
    ShapeMybatisTestContainerTest.TestConfig.class
})
class ShapeMybatisTestContainerTest {

    @Autowired
    ShapeDao dao;
    
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("test")
        .withUsername("test")
        .withPassword("spike")
        .withReuse(true)
        .withInitScript("shape.ddl");

    @BeforeAll
    static void setup() {
        // not sure why reuse error showed up in mybastis 
        // Error was
        // To enable reuse of containers, you must set 'testcontainers.reuse.enable=true' in a file located at ...
        TestcontainersConfiguration.getInstance().updateUserConfig("testcontainers.reuse.enable", "true");
        postgres.start();
    }
    
    @AfterAll
    static void cleanup() {
        postgres.stop();
    }

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
     * unlike H2 we can run this now
     */
    @Test
    void test_collision_on_upsert() {
        ShapeDto fetchShape = dao.findByShapeColor("Square", "Blue");
        assertNotNull(fetchShape);
        assertNotNull(fetchShape.getId());
        assertEquals("Square", fetchShape.getShape());
        assertEquals("Blue", fetchShape.getColor());
        assertEquals(null, fetchShape.getDescription());
        
        ShapeDto newShape = new ShapeDto();
        newShape.setShape("Square");
        newShape.setColor("Blue");
        newShape.setDescription("UPSERT");

        assertNull(newShape.getId());

        dao.upsertShape(newShape);
        assertNotNull(newShape.getId());
        assertEquals(fetchShape.getId(), newShape.getId());
        
        ShapeDto fetchShapeAgain = dao.findByShapeColor("Square", "Blue");
        assertNotNull(fetchShapeAgain);
        assertNotNull(fetchShapeAgain.getId());
        assertEquals(fetchShape.getId(), fetchShapeAgain.getId());
        assertEquals("Square", fetchShapeAgain.getShape());
        assertEquals("Blue", fetchShapeAgain.getColor());
        assertEquals("UPSERT", fetchShapeAgain.getDescription());
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
        assertEquals("Sphere", newShape.getShape());
        assertEquals("Gray", newShape.getColor());
    }

    
    @Configuration
    public static class TestConfig {
    }
}
