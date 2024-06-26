package br.com.cardoso.configuration

import jakarta.persistence.EntityManagerFactory
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.envers.repository.config.EnableEnversRepositories
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableEnversRepositories
@EnableTransactionManagement
@EnableJpaRepositories(
    repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean::class,
    basePackages = ["br.com.cardoso.repository"]
)
class EnversConfiguration {

    @Bean
    fun entityManagerFactory(dataSource: DataSource): LocalContainerEntityManagerFactoryBean {
        val vendorAdapter = HibernateJpaVendorAdapter()
        vendorAdapter.setGenerateDdl(true)

        val factory = LocalContainerEntityManagerFactoryBean()
        factory.jpaVendorAdapter = vendorAdapter
        factory.setPackagesToScan("br.com.cardoso.entity")
        factory.dataSource = dataSource
        return factory
    }

    @Bean
    fun transactionManager(entityManagerFactory: EntityManagerFactory): PlatformTransactionManager {
        val txManager = JpaTransactionManager()
        txManager.entityManagerFactory = entityManagerFactory
        return txManager
    }
}