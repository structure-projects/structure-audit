package cn.structured.audit.api.constant;

public class AuditRabbitConstants {

    private AuditRabbitConstants() {
    }

    public static final String AUDIT_EXCHANGE = "exchange.audit";

    public static final String AUDIT_REQUEST_QUEUE = "queue.audit.request";

    public static final String AUDIT_RESPONSE_QUEUE = "queue.audit.response";

    public static final String AUDIT_REQUEST_ROUTING_KEY = "audit.request";

    public static final String AUDIT_RESPONSE_ROUTING_KEY = "audit.response";
}
