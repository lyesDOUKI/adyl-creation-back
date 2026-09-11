package ld.application.infra.db.jpa.adapter;

import ld.application.infra.db.entity.AppointmentEntity;
import ld.application.infra.db.jpa.AppointmentJpaRepository;
import ld.application.infra.db.jpa.mapper.AppointmentMapper;
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
