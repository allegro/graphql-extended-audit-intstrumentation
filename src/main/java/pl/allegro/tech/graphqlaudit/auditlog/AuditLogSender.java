package pl.allegro.tech.graphqlaudit.auditlog;

public interface AuditLogSender {
  default void send(AuditLogItem auditLogItem){}

  default void sendAnonymized(AuditLogItem auditLogItem){}
}
