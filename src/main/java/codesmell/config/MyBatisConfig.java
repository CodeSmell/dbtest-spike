package codesmell.config;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

/** 
 * This class configures MyBatis
 *
 * When using the MyBatis Spring Boot Starter, Mappers do NOT need to be added as Beans
 * The starter will find Mappers automatically if they are annotated with @Mapper
 */
@Configuration
@MapperScan(value = {"codesmell.dao.mybatis"}, 
    annotationClass = Mapper.class, 
    sqlSessionFactoryRef = "sqlSessionFactory")
public class MyBatisConfig {
    
    @Bean
    SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setConfigLocation(new ClassPathResource("mybatis-config.xml"));
        factoryBean.setTypeAliasesPackage("codesmell.dao.mybatis");
        return factoryBean.getObject();
    }
    
}
