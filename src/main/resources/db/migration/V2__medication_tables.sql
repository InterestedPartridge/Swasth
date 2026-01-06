CREATE TABLE medications (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id      BIGINT NOT NULL,
    medicine_name   VARCHAR(255) NOT NULL,
    dosage          VARCHAR(255) NOT NULL,
    timing          ENUM('PRE_BREAKFAST','POST_BREAKFAST','PRE_LUNCH','POST_LUNCH','PRE_DINNER','POST_DINNER','BEDTIME') NOT NULL,
    start_date      DATE NOT NULL,
    end_date        DATE,
    frequency       ENUM('DAILY','WEEKLY','BI_WEEKLY','MONTHLY','CUSTOM') NOT NULL,
    custom_days     VARCHAR(20),
    reminder_enabled BOOLEAN DEFAULT TRUE,
    notes           TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_med_patient
        FOREIGN KEY (patient_id) REFERENCES patients(id)
);

CREATE TABLE medication_dose_logs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    medication_id   BIGINT NOT NULL,
    taken_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes           TEXT,
    CONSTRAINT fk_dose_med
        FOREIGN KEY (medication_id) REFERENCES medications(id)
);

CREATE INDEX idx_med_patient_date ON medications(patient_id, start_date);
CREATE INDEX idx_dose_med_id ON medication_dose_logs(medication_id);