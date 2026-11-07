package ld.bootstrap.infra.db.jpa;

import ld.bootstrap.infra.db.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppointmentJpaRepository extends JpaRepository<AppointmentEntity, UUID> {
}
