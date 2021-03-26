package pl.allegro.tech.graphql.auditlog;

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AuditLogSenderImpl implements AuditLogSender {

  private static final Logger logger =
      LoggerFactory.getLogger(AuditLogSenderImpl.class);

  @Override
  public void send(AuditLogItem auditLogItem) {
    logger.info(format("A audit log was sent - %s", auditLogItem.toString()));
  }

  @Override
  public void sendAnonymized(AuditLogItem auditLogItem) {
    logger.info(format("A anonymized audit log was sent - %s", auditLogItem.toString()));
  }
}
