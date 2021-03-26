package pl.allegro.tech.graphql.auditlog

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.allegro.tech.graphql.auditlog.fixture.UpdatableAnonymizedFieldsSetupChecker

@Configuration
class AuditLogAnonymizerConfig {

    @Bean
    AuditLogAnonymizer actionLogAnonymizer() {
        return new AuditLogAnonymizer(new UpdatableAnonymizedFieldsSetupChecker())
    }
}
