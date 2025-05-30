package codesmell.config;

import codesmell.rest.ShapeUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {
    
    @Bean
    ShapeUtil getShapeUtil() {
        return new ShapeUtil();
    }
}
