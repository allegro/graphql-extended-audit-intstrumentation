package pl.allegro.tech.graphqlaudit.auditlog

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.allegro.tech.graphqlaudit.auditlog.fixture.UpdatableAnonymizedFieldsSetupChecker

@Configuration
class AuditLogAnonymizerConfig {

    @Bean
    AuditLogAnonymizer actionLogAnonymizer() {
        return new AuditLogAnonymizer(new UpdatableAnonymizedFieldsSetupChecker())
    }
}
