package pl.allegro.tech.graphql.auditlog.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.allegro.tech.graphql.auditlog.AuditLogInstrumentation
import pl.allegro.tech.graphql.auditlog.AuditLogInstrumentationBuilder
import pl.allegro.tech.graphql.auditlog.root.InMemoryAuditLogSender

@Configuration
class AuditLogInstrumentationConfig {

    @Bean
    AuditLogInstrumentation auditLogInstrumentation(InMemoryAuditLogSender inMemoryAuditLogSender){
        return new AuditLogInstrumentationBuilder()
                .withActionLogSender(inMemoryAuditLogSender)
                .build()
    }
}
