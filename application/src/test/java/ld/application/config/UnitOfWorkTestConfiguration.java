package ld.application.config;

import ld.application.infra.TransactionalSpringUnitOfWork;
import ld.standard.lib.UnitOfWork;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

@TestConfiguration
@EnableTransactionManagement
public class UnitOfWorkTestConfiguration {

    @Bean
    UnitOfWork unitOfWork(TransactionTemplate transactionTemplate) {
        return new TransactionalSpringUnitOfWork(transactionTemplate);
    }

    @Bean
    TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
