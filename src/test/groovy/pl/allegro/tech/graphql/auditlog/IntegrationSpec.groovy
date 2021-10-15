package pl.allegro.tech.graphql.auditlog

import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration
import groovy.transform.TypeChecked
import org.springframework.boot.test.context.SpringBootTest
import pl.allegro.tech.graphql.auditlog.config.AuditLogAdditionalFieldsConfig
import pl.allegro.tech.graphql.auditlog.config.AuditLogAnonymizerConfig
import pl.allegro.tech.graphql.auditlog.config.AuditLogInstrumentationConfig
import pl.allegro.tech.graphql.auditlog.config.InMemoryAuditLogSenderConfig
import spock.lang.Specification

import javax.inject.Inject

@TypeChecked
@SpringBootTest(classes = [DgsAutoConfiguration.class, InMemoryAuditLogSenderConfig.class,
    AuditLogAnonymizerConfig.class, AuditLogInstrumentationConfig.class, UserIdScalar.class, AuditLogAdditionalFieldsConfig.class])
abstract class IntegrationSpec extends Specification {

    @Inject
    InMemoryAuditLogSender actionLog

    def cleanup() {
        actionLog.reset()
    }

    String existingUserId = "12345"
    String noExistingUserId = "54321"
}
