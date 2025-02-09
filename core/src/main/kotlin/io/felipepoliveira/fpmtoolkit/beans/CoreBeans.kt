package io.felipepoliveira.fpmtoolkit.beans

import io.felipepoliveira.fpmtoolkit.beans.context.ContextualBeans
import io.felipepoliveira.fpmtoolkit.beans.context.DevelopmentBeans
import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ComponentScans
import org.springframework.context.annotation.Configuration
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

/**
 * Inject the main beans of the application. For this class to be injected successfully the AppContextProvider
 * should be declared in the beans context as it is needed to declare the ContextualBeans bean
 * @see AppContextProvider
 * @see ContextualBeans
 */
@ComponentScans(value = [
    ComponentScan("io.felipepoliveira.fpmtoolkit.dao"),
    ComponentScan("io.felipepoliveira.fpmtoolkit.features"),
    ComponentScan("io.felipepoliveira.fpmtoolkit.security"),
])
@Configuration
class CoreBeans @Autowired constructor(
    private val appContextProvider: AppContextProvider
) {

    /**
     * Return the bean that provide the contextuals beans of the application
     * @see ContextualBeans
     */
    @Bean
    fun contextualBeans(): ContextualBeans {
        return when (appContextProvider.getAppContext()) {
            AppContext.DEVELOPMENT -> DevelopmentBeans()
        }
    }

    /**
     * Return the entity manager factory instance for the JPA implementation of the DAO interface
     */
    @Bean
    fun jpaEntityManager(contextualBeans: ContextualBeans): LocalContainerEntityManagerFactoryBean {
        val entityManagerFactory = LocalContainerEntityManagerFactoryBean()
        entityManagerFactory.dataSource = contextualBeans.jpaDataSource()
        entityManagerFactory.setJpaProperties(contextualBeans.jpaProperties())
        entityManagerFactory.setPackagesToScan("io.felipepoliveira.fpmtoolkit.features")

        val vendorAdapter = HibernateJpaVendorAdapter()
        entityManagerFactory.jpaVendorAdapter = vendorAdapter

        return entityManagerFactory
    }

    /**
     * Default instance jakarta validation
     */
    @Bean
    fun localValidatorFactoryBean() = LocalValidatorFactoryBean()

    /**
     * Default instance for transaction management for Spring TX framework
     */
    @Bean
    fun transactionManager(entityManagerFactory: EntityManagerFactory): PlatformTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = entityManagerFactory
        return transactionManager
    }
}

