package pl.allegro.tech.graphql.auditlog.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.allegro.tech.graphql.auditlog.AuditLogAdditionalFieldFetcher
import pl.allegro.tech.graphql.auditlog.fixture.UpdatableAdditionalFieldsSetupPreconditions

@Configuration
class AuditLogAdditionalFieldsConfig {

    @Bean
    AuditLogAdditionalFieldFetcher actionLogAnonymizer() {
        return new AuditLogAdditionalFieldFetcher(new UpdatableAdditionalFieldsSetupPreconditions())
    }
}
