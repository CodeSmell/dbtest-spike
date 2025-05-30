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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Note: should not use @TestContainers when reusing a container
 * Note: separated embedded and testcontainer tests by package 
 * that required the base package in @EntityScan and @EnableJpaRepositories
 * 
 */
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test-pg.properties")
@DataJpaTest
//tell Spring Boot not to use an Embedded DB
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableJpaRepositories("codesmell.dao.jpa")
@EntityScan("codesmell.dao.jpa")
//@Testcontainers
class ColorAndShapeJpaTestContainerTest {

    @Autowired
    ColorAndShapeRepository rep;

    //@Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("test")
        .withUsername("test")
        .withPassword("spike")
        .withReuse(true)
        .withInitScript("shape.ddl");

// This can be removed when using @ServiceConnection
//    @DynamicPropertySource
//    static void datasourceProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgres::getJdbcUrl);
//        registry.add("spring.datasource.username", postgres::getUsername);
//        registry.add("spring.datasource.password", postgres::getPassword);
//    }

    @BeforeAll
    static void setup() {
        postgres.start();
    }
    
    @AfterAll
    static void cleanup() {
        postgres.stop();
    }

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
                () -> rep.save(newEntity));
    }
    
    @Test
    void test_not_found_by_id() {
        Optional<ColorAndShapeEntity> fetchedEntityOpt = rep.findById(Long.MAX_VALUE);
        assertTrue(fetchedEntityOpt.isEmpty());
    }

}