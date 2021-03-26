package pl.allegro.tech.graphql.auditlog

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.allegro.tech.graphql.auditlog.fixture.UpdatableAdditionalFieldsSetupChecker

@Configuration
class AuditLogAdditionalFieldsConfig {

        @Bean
        AuditLogAdditionalFieldFetcher actionLogAnonymizer() {
            return new AuditLogAdditionalFieldFetcher(new UpdatableAdditionalFieldsSetupChecker())
        }
    }
