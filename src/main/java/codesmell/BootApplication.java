package codesmell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.Iterator;

@SpringBootApplication
public class BootApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(BootApplication.class);
    
    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(BootApplication.class, args);
        BootApplication.logPropertyFiles(ctx);
    }

    private static void logPropertyFiles(ApplicationContext ctx) {
        final Environment env = ctx.getEnvironment();
        final MutablePropertySources sources = ((AbstractEnvironment) env).getPropertySources();

        LOGGER.info("====== the following properties files were loaded ======");
        Iterator<PropertySource<?>> itr = sources.iterator();

        while (itr.hasNext()) {
            PropertySource<?> propSource = itr.next();
            LOGGER.info(propSource.getName());
        }
    }
}