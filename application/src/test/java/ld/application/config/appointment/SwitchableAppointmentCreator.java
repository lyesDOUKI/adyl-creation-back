package ld.application.config.appointment;

import ld.domain.features.appointment.create.AppointmentCreator;
import ld.domain.features.appointment.model.AppointmentSnapshot;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class SwitchableAppointmentCreator implements AppointmentCreator {

    private final AppointmentCreator delegate;
    private final AtomicReference<Consumer<AppointmentSnapshot>> afterCreateHook =
            new AtomicReference<>();

    public SwitchableAppointmentCreator(AppointmentCreator delegate) {
        this.delegate = delegate;
    }

    public void failAfterCreateWith(RuntimeException ex) {
        afterCreateHook.set(snapshot -> { throw ex; });
    }

    public void afterCreate(Consumer<AppointmentSnapshot> hook) {
        afterCreateHook.set(hook);
    }

    public void reset() {
        afterCreateHook.set(null);
    }

    public void create(AppointmentSnapshot snapshot) {
        delegate.create(snapshot);
        var hook = afterCreateHook.getAndSet(null);
        if (hook != null) {
            hook.accept(snapshot);
        }
    }
}