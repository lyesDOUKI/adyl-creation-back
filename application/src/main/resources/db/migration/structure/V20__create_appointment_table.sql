CREATE TABLE appointment (
                             id                UUID         NOT NULL,
                             start_at          TIMESTAMPTZ  NOT NULL,
                             end_at            TIMESTAMPTZ  NOT NULL,
                             identity_subject  UUID         NOT NULL,
                             status            VARCHAR(20)  NOT NULL,
                             submitted_at      TIMESTAMPTZ,
                             cancelled_at      TIMESTAMPTZ,
                             cancelled_reason  VARCHAR(500),
                             created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
                             updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),

                             CONSTRAINT pk_appointment PRIMARY KEY (id),
                             CONSTRAINT chk_appointment_end_after_start CHECK (end_at > start_at),
                             CONSTRAINT chk_appointment_status CHECK (status IN ('SUBMITTED', 'CANCELLED')),
                             CONSTRAINT chk_appointment_submitted_at CHECK (
                                 (status = 'SUBMITTED' AND submitted_at IS NOT NULL)
                                     OR (status = 'CANCELLED')
                                 ),
                             CONSTRAINT chk_appointment_cancelled_fields CHECK (
                                 (status = 'CANCELLED' AND cancelled_at IS NOT NULL)
                                     OR (status = 'SUBMITTED' AND cancelled_at IS NULL AND cancelled_reason IS NULL)
                                 )
);

-- Recherche de créneaux réservés sur une plage
CREATE INDEX idx_appointment_time_range ON appointment (start_at, end_at);

-- Historique / recherche des rendez-vous d'une identité
CREATE INDEX idx_appointment_identity_subject ON appointment (identity_subject);

-- Index partiel : la grande majorité des requêtes ne portent que sur les rendez-vous actifs
CREATE INDEX idx_appointment_active ON appointment (start_at, end_at) WHERE status = 'SUBMITTED';

COMMENT ON TABLE appointment IS 'Rendez-vous soumis par un sujet identité sur un créneau donné';
COMMENT ON COLUMN appointment.identity_subject IS 'UUID du sujet identité ayant soumis le rendez-vous';
COMMENT ON COLUMN appointment.status IS 'SUBMITTED | CANCELLED';