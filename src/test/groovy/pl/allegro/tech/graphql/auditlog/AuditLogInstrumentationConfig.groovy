package pl.allegro.tech.graphql.auditlog

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AuditLogInstrumentationConfig {

    @Bean
    AuditLogInstrumentation auditLogInstrumentation(InMemoryAuditLogSender inMemoryAuditLogSender){
        return new AuditLogInstrumentationBuilder()
                .withActionLogSender(inMemoryAuditLogSender)
                .build()
    }
}
