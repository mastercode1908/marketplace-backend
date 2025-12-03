package com.group7.marketplacesystem.common.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(basePackages = {
        "com.group7.marketplacesystem.catalog.repository",
        "com.group7.marketplacesystem.identity.repository",
        "com.group7.marketplacesystem.commerce.payment.repository",
        "com.group7.marketplacesystem.commerce.cart.repository",
        "com.group7.marketplacesystem.commerce.order.repository",
        "com.group7.marketplacesystem.commerce.shipping.repository",
        "com.group7.marketplacesystem.communication.repository",
        "com.group7.marketplacesystem.promotion.repository",
        "com.group7.marketplacesystem.infrastructure.repository"
}, entityManagerFactoryRef = "mysqlEntityManagerFactory", transactionManagerRef = "mysqlTransactionManager")
public class MySQLConfig {

    @Value("${spring.datasource.url}")
    private String mysqlUrl;

    @Value("${spring.datasource.username}")
    private String mysqlUsername;

    @Value("${spring.datasource.password}")
    private String mysqlPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String mysqlDriverClassName;

    @Primary
    @Bean
    public DataSource mysqlDataSource() {
        return DataSourceBuilder.create()
                .url(mysqlUrl)
                .username(mysqlUsername)
                .password(mysqlPassword)
                .driverClassName(mysqlDriverClassName)
                .build();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.show_sql", true);
        properties.put("hibernate.format_sql", true);

        return builder
                .dataSource(mysqlDataSource())
                .packages(
                        "com.group7.marketplacesystem.catalog.entity",
                        "com.group7.marketplacesystem.identity.entity",
                        "com.group7.marketplacesystem.commerce.payment.entity",
                        "com.group7.marketplacesystem.commerce.cart.entity",
                        "com.group7.marketplacesystem.commerce.order.entity",
                        "com.group7.marketplacesystem.commerce.shipping.entity",
                        "com.group7.marketplacesystem.communication.entity",
                        "com.group7.marketplacesystem.promotion.entity",
                        "com.group7.marketplacesystem.infrastructure.entity")
                .persistenceUnit("mysql")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager mysqlTransactionManager(
            @Qualifier("mysqlEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
