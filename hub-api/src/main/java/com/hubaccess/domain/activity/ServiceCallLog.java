package com.hubaccess.domain.activity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_call_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "case_id")
    private UUID caseId;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "method_name", nullable = false, length = 100)
    private String methodName;

    @Column(name = "is_stub", nullable = false)
    private Boolean isStub;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_payload", columnDefinition = "JSONB")
    private String requestPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_payload", columnDefinition = "JSONB")
    private String responsePayload;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(nullable = false)
    private Boolean success;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "demo_scenario", length = 100)
    private String demoScenario;

    @Column(name = "called_at", nullable = false)
    private OffsetDateTime calledAt;

    @Column(name = "called_by_user")
    private UUID calledByUser;

    @PrePersist
    protected void onCreate() {
        if (calledAt == null) calledAt = OffsetDateTime.now();
    }
}
