package pl.allegro.tech.graphql.auditlog

class InMemoryAuditLogSender implements AuditLogSender {

    private List<AuditLogItem> sendItems = []
    private List<AuditLogItem> sendAnonymizedItems = []

    @Override
    void send(AuditLogItem auditLogItem) {
        sendItems.add(auditLogItem)
    }

    @Override
    void sendAnonymized(AuditLogItem auditLogItem) {
        sendAnonymizedItems.add(auditLogItem)
    }

    List<AuditLogItem> sendAuditLogItems() {
        return new ArrayList<>(sendItems)
    }

    List<AuditLogItem> sendAnonymizedAuditLogItems() {
        return new ArrayList<>(sendAnonymizedItems)
    }

    void reset() {
        sendItems = []
        sendAnonymizedItems = []
    }
}
