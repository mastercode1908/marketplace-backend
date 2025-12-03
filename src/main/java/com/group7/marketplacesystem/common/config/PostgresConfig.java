package com.group7.marketplacesystem.common.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(basePackages = "com.group7.marketplacesystem.chatbotRAG.repository", entityManagerFactoryRef = "postgresEntityManagerFactory", transactionManagerRef = "postgresTransactionManager")
public class PostgresConfig {

    @Value("${postgres.datasource.url}")
    private String postgresUrl;

    @Value("${postgres.datasource.username}")
    private String postgresUsername;

    @Value("${postgres.datasource.password}")
    private String postgresPassword;

    @Value("${postgres.datasource.driver-class-name}")
    private String postgresDriverClassName;

    @Bean
    public DataSource postgresDataSource() {
        return DataSourceBuilder.create()
                .url(postgresUrl)
                .username(postgresUsername)
                .password(postgresPassword)
                .driverClassName(postgresDriverClassName)
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean postgresEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "none"); // hoặc 'update' nếu muốn auto tạo bảng
        properties.put("hibernate.show_sql", true);
        properties.put("hibernate.format_sql", true);

        return builder
                .dataSource(postgresDataSource())
                .packages("com.group7.marketplacesystem.chatbotRAG.entity")
                .persistenceUnit("postgres")
                .properties(properties)
                .build();
    }

    @Bean
    public PlatformTransactionManager postgresTransactionManager(
            @Qualifier("postgresEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
