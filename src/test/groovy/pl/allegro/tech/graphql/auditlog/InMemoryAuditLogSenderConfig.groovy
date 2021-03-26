package pl.allegro.tech.graphql.auditlog

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class InMemoryAuditLogSenderConfig {

    @Bean
    InMemoryAuditLogSender inMemoryAuditLogSender() {
        return new InMemoryAuditLogSender()
    }
}
