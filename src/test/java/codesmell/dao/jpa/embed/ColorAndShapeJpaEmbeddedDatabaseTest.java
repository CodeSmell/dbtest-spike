package codesmell.dao.jpa.embed;

import codesmell.dao.jpa.ColorAndShapeEntity;
import codesmell.dao.jpa.ColorAndShapeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Note: separated embedded and testcontainer tests by package 
 * that required the base package in @EntityScan and @EnableJpaRepositories
 */
@ActiveProfiles("test")
@DataJpaTest(showSql = true)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Sql(scripts = "/shape.ddl", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=none"
})
//@TestPropertySource(properties = {
//    "spring.jpa.hibernate.ddl-auto=create-drop"
//})
@EnableJpaRepositories("codesmell.dao.jpa")
@EntityScan("codesmell.dao.jpa")
@ContextConfiguration(classes = {
    ColorAndShapeJpaEmbeddedDatabaseTest.TestConfig.class
})
class ColorAndShapeJpaEmbeddedDatabaseTest {
    
    @Autowired
    ColorAndShapeRepository rep;

    @Test
    @Sql(scripts = "/test-data.sql")
    void test_collision_on_insert() {
        assertEquals(1, rep.count());
        
        ColorAndShapeEntity newEntity = new ColorAndShapeEntity();
        newEntity.setShape("Square");
        newEntity.setColor("Blue");

        assertNull(newEntity.getId());
        assertEquals(1, rep.count());

        // insert attempt
        assertThrows(
            DataIntegrityViolationException.class,
            () -> rep.save(newEntity)
         );
    }
    
    @Test
    void test_managed_update() {
        ColorAndShapeEntity newEntity = new ColorAndShapeEntity();
        newEntity.setShape("Sphere");
        newEntity.setColor("Gray");

        assertNull(newEntity.getId());
        assertEquals(0, rep.count());

        // insert
        ColorAndShapeEntity savedEntity = rep.save(newEntity);
        assertNotNull(savedEntity.getId());
        assertEquals("Sphere", savedEntity.getShape());
        assertEquals("Gray", savedEntity.getColor());
        assertEquals(savedEntity.getShape(), newEntity.getShape());
        assertEquals(savedEntity.getColor(), newEntity.getColor());
        assertEquals(1, rep.count());

        // this entity is managed
        Optional<ColorAndShapeEntity> fetchedEntityOpt = rep.findById(newEntity.getId());
        ColorAndShapeEntity fetchedEntity = fetchedEntityOpt.get();
        assertNotNull(fetchedEntity);
        assertNotNull(fetchedEntity.getId());
        assertEquals(fetchedEntity.getId(), newEntity.getId());
        assertEquals(fetchedEntity.getShape(), newEntity.getShape());
        assertEquals(fetchedEntity.getColor(), newEntity.getColor());
        assertEquals(1, rep.count());

        fetchedEntity.setColor("Red");

        savedEntity = rep.save(fetchedEntity);
        assertNotNull(savedEntity.getId());
        assertEquals(savedEntity.getId(), newEntity.getId());
        assertEquals("Sphere", savedEntity.getShape());
        assertEquals("Red", savedEntity.getColor());
        assertEquals(1, rep.count());
    }
    

    @Test
    void test_detached_save() {
        Long theId = null;
        
        ColorAndShapeEntity newEntity = new ColorAndShapeEntity();
        newEntity.setShape("Triangle");
        newEntity.setColor("Green");
        newEntity.setDescription("initial");

        assertNull(newEntity.getId());
        assertEquals(0, rep.count());

        // insert
        ColorAndShapeEntity savedEntity = rep.save(newEntity);
        theId = savedEntity.getId();
        assertNotNull(theId);
        assertEquals(theId, newEntity.getId());
        assertEquals("Triangle", savedEntity.getShape());
        assertEquals("Green", savedEntity.getColor());
        assertEquals("initial", savedEntity.getDescription());
        assertEquals(1, rep.count());

        // thjs entity is managed
        savedEntity.setColor("Yellow");
        savedEntity = rep.save(savedEntity);
        assertNotNull(savedEntity.getId());
        assertEquals(theId, savedEntity.getId());
        assertTrue("Triangle".equals(savedEntity.getShape()));
        assertTrue("Yellow".equals(savedEntity.getColor()));
        assertEquals("initial", savedEntity.getDescription());        
        assertEquals(1, rep.count());

        // this entity is detached
        ColorAndShapeEntity updateEntity = new ColorAndShapeEntity();
        updateEntity.setId(theId);
        updateEntity.setShape("Circle");

        savedEntity = rep.save(updateEntity);
        assertNotNull(savedEntity.getId());
        assertEquals(theId, savedEntity.getId());
        // this is updated
        assertEquals("Circle", savedEntity.getShape());
        // this was overwritten
        assertEquals(null, savedEntity.getColor());
        // this was overwritten
        assertEquals(null, savedEntity.getDescription());
        assertEquals(1, rep.count());

        // this entity is managed
        Optional<ColorAndShapeEntity> fetchedEntityOpt = rep.findById(theId);
        ColorAndShapeEntity fetchedEntity = fetchedEntityOpt.get();
        assertNotNull(fetchedEntity);
        assertNotNull(fetchedEntity.getId());
        assertEquals(theId, fetchedEntity.getId());
        assertEquals("Circle", fetchedEntity.getShape());
        assertEquals(null, fetchedEntity.getColor());
        assertEquals(null, fetchedEntity.getDescription());
        assertEquals(1, rep.count());
    }

    
    @Configuration
    public static class TestConfig {
    }

}
