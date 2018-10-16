package de.adorsys.springoauth2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.adorsys.lockpersistence.client.LockClient;
import de.adorsys.lockpersistence.client.NoopLockClient;
import de.adorsys.sts.keymanagement.persistence.InMemoryKeyStoreRepository;
import de.adorsys.sts.keymanagement.persistence.KeyStoreRepository;
import de.adorsys.sts.token.authentication.EnableTokenAuthentication;

@Configuration
@EnableTokenAuthentication
public class STSConfiguration {

    @Bean
    KeyStoreRepository keyStoreRepository() {
        return new InMemoryKeyStoreRepository();
    }

    @Bean
    public LockClient getLockClient() {
        return new NoopLockClient();
    }
}
