package pl.allegro.tech.graphqlaudit.auditlog

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.allegro.tech.graphqlaudit.auditlog.InMemoryAuditLogSender

@Configuration
class InMemoryAuditLogSenderConfig {

    @Bean
    InMemoryAuditLogSender inMemoryAuditLogSender() {
        return new InMemoryAuditLogSender()
    }
}
