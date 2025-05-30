package codesmell.dao.jpa.container;

import codesmell.dao.jpa.ColorAndShapeEntity;
import codesmell.dao.jpa.ColorAndShapeRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Playing with the reuse of test container across multiple test classes
 * Note: should not use @TestContainers when reusing a container
 * Note: separated embedded and testcontainer tests by package 
 * that required the base package in @EntityScan and @EnableJpaRepositories
 */
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test-pg.properties")
@DataJpaTest
//tell Spring Boot not to use an Embedded DB
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableJpaRepositories("codesmell.dao.jpa")
@EntityScan("codesmell.dao.jpa")
class ColorAndShapeJpaTestContainerAnotherTest {

    @Autowired
    ColorAndShapeRepository rep;

    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("test")
        .withUsername("test")
        .withPassword("spike")
        .withReuse(true)
        .withInitScript("shape.ddl");

    @BeforeAll
    static void setup() {
        postgres.start();
    }
    
    @AfterAll
    static void cleanup() {
        postgres.stop();
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

}