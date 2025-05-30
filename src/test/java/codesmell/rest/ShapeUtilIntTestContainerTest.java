package codesmell.rest;

import codesmell.config.BeanConfig;
import codesmell.config.MyBatisConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.TestcontainersConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * testing the Shape object and the underlying DB interactions
 * with TestContainers
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test-pg.properties")
//tell Spring Boot not to use an Embedded DB
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@ContextConfiguration(classes = {
    BeanConfig.class,
    MyBatisConfig.class
})
class ShapeUtilIntTestContainerTest {
    
    @Autowired
    private ShapeUtil shapeUtil;
    
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
        // To enable reuse of containers, you must set
        // 'testcontainers.reuse.enable=true' in a file located at ...
        TestcontainersConfiguration.getInstance().updateUserConfig("testcontainers.reuse.enable", "true");
        postgres.start();
    }

    @AfterAll
    static void cleanup() {
        postgres.stop();
    }
    
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
        // existing row
        Shape shapeExists = shapeUtil.fetchShape("Square", "Blue");
        assertNotNull(shapeExists);
        assertEquals("Square", shapeExists.getShape());
        assertEquals("Blue", shapeExists.getColor());
        assertEquals(null, shapeExists.getDescription());
        
        // add the row (same shape/color)
        Long existId = shapeExists.getId();
        
        Shape myShape = Shape.builder()
            .shape("Square")
            .color("Blue")
            .description("UPSERT")
            .build();

        Long theId = shapeUtil.persist(myShape);
        assertEquals(existId, theId);
        assertEquals(existId, myShape.getId());
        
        // existing row was udpated
        Shape fetchShape = shapeUtil.fetchShape(existId);
        assertNotNull(fetchShape);
        assertEquals("Square", fetchShape.getShape());
        assertEquals("Blue", fetchShape.getColor());
        assertEquals("UPSERT", fetchShape.getDescription());
    }
    
    @Test
    void test_upsert_and_retrieve() {
        Shape myShape = Shape.builder()
            .shape("Octagon")
            .color("Red")
            .description("NEW")
            .build();

        Long theId = shapeUtil.persist(myShape);
        assertNotNull(theId);
        assertNotNull(myShape.getId());
        assertEquals(theId, myShape.getId());
        
        Shape fetchShape = shapeUtil.fetchShape(myShape.getId());
        assertNotNull(fetchShape);
        assertEquals(theId, fetchShape.getId());
        assertEquals("Octagon", fetchShape.getShape());
        assertEquals("Red", fetchShape.getColor());
        assertEquals("NEW", fetchShape.getDescription());
    }
}
