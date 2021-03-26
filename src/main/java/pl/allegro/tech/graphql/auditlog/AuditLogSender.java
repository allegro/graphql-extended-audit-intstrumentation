package pl.allegro.tech.graphql.auditlog;

public interface AuditLogSender {
  default void send(AuditLogItem auditLogItem){}

  default void sendAnonymized(AuditLogItem auditLogItem){}
}
