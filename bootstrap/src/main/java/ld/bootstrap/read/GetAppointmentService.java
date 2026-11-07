package ld.bootstrap.read;

import ld.bootstrap.infra.db.jooq.AppointmentRepository;
import ld.bootstrap.response.AppointmentsResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetAppointmentService {

    private final AppointmentRepository appointmentRepository;

    public GetAppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public List<AppointmentsResponse> getAll(UUID identitySubject) {
        return appointmentRepository.findAll(identitySubject).stream()
                .map(AppointmentsResponse::from)
                .toList();
    }
}