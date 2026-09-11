package ld.application.infra.db.entity;

import jakarta.persistence.*;
import ld.domain.features.appointment.model.AppointmentState;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment")
public class AppointmentEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "start_at", nullable = false)
    private ZonedDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private ZonedDateTime endAt;

    @Column(name = "identity_subject", nullable = false)
    private UUID identitySubject;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AppointmentState status;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancelled_reason", length = 500)
    private String cancelledReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AppointmentEntity() {}

    public AppointmentEntity(
            UUID id,
            ZonedDateTime startAt,
            ZonedDateTime endAt,
            UUID identitySubject,
            AppointmentState status,
            Instant submittedAt
    ) {
        this.id = id;
        this.startAt = startAt;
        this.endAt = endAt;
        this.identitySubject = identitySubject;
        this.status = status;
        this.submittedAt = submittedAt;
    }

    public AppointmentEntity(
            UUID id,
            ZonedDateTime startAt,
            ZonedDateTime endAt,
            UUID identitySubject,
            AppointmentState status,
            Instant cancelledAt,
            String cancelledReason
    ) {
        this.id = id;
        this.startAt = startAt;
        this.endAt = endAt;
        this.identitySubject = identitySubject;
        this.status = status;
        this.cancelledAt = cancelledAt;
        this.cancelledReason = cancelledReason;
    }


    public UUID getId() {
        return id;
    }

    public ZonedDateTime getStartAt() {
        return startAt;
    }

    public ZonedDateTime getEndAt() {
        return endAt;
    }

    public UUID getIdentitySubject() {
        return identitySubject;
    }

    public AppointmentState getStatus() {
        return status;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public String getCancelledReason() {
        return cancelledReason;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setStartAt(ZonedDateTime startAt) {
        this.startAt = startAt;
    }

    public void setEndAt(ZonedDateTime endAt) {
        this.endAt = endAt;
    }

    public void setIdentitySubject(UUID identitySubject) {
        this.identitySubject = identitySubject;
    }

    public void setStatus(AppointmentState status) {
        this.status = status;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public void setCancelledReason(String cancelledReason) {
        this.cancelledReason = cancelledReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
