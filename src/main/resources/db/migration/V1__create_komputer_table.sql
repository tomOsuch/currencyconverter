CREATE TABLE komputer
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nazwa            VARCHAR(255)   NOT NULL,
    data_ksiegowania DATE           NOT NULL,
    koszt_usd        NUMERIC(19, 2) NOT NULL,
    koszt_pln        NUMERIC(19, 2) NOT NULL,
    kurs             NUMERIC(10, 4) NOT NULL,
    data_kursu       DATE           NOT NULL,
    created_at       TIMESTAMPTZ    NOT NULL,
    updated_at       TIMESTAMPTZ    NOT NULL,

    CONSTRAINT uk_komputer_nazwa_data_ksiegowania UNIQUE (nazwa, data_ksiegowania),
    CONSTRAINT chk_komputer_koszt_usd_positive CHECK (koszt_usd > 0),
    CONSTRAINT chk_komputer_kurs_positive CHECK (kurs > 0),
    CONSTRAINT chk_komputer_data_kursu_before_ksiegowania CHECK (data_kursu < data_ksiegowania)
);

CREATE INDEX idx_komputer_data_ksiegowania ON komputer (data_ksiegowania);