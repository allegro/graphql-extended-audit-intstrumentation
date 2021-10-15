package pl.allegro.tech.graphql.auditlog.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.allegro.tech.graphql.auditlog.InMemoryAuditLogSender

@Configuration
class InMemoryAuditLogSenderConfig {

    @Bean
    InMemoryAuditLogSender inMemoryAuditLogSender() {
        return new InMemoryAuditLogSender()
    }
}
