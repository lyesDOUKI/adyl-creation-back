package ld.bootstrap.infra.db.jpa.adapter;

import ld.bootstrap.infra.db.entity.AppointmentEntity;
import ld.bootstrap.infra.db.jpa.AppointmentJpaRepository;
import ld.bootstrap.infra.db.jpa.mapper.AppointmentMapper;
import ld.domain.features.appointment.create.AppointmentCreator;
import ld.domain.features.appointment.model.AppointmentSnapshot;
import org.springframework.stereotype.Repository;

@Repository
public class AppointmentCreatorJpaAdapter implements AppointmentCreator {

    private final AppointmentJpaRepository appointmentJpaRepository;

    public AppointmentCreatorJpaAdapter(AppointmentJpaRepository appointmentJpaRepository) {
        this.appointmentJpaRepository = appointmentJpaRepository;
    }

    @Override
    public void create(AppointmentSnapshot appointmentSnapshot) {
        AppointmentEntity entity = AppointmentMapper.toEntity(appointmentSnapshot);
        appointmentJpaRepository.save(entity);
    }
}
