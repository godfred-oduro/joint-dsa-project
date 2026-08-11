-- =====================================================================
--  University of Ghana Hospital, Legon - Operations Model
--  Owner: 22015166
--
--  Creates the six tables the project needs.
--  Safe to run more than once: nothing is dropped and nothing is duplicated.
--
--  Run it with:  mysql -u root -p < sql/schema.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS ugh_legon
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ugh_legon;

-- ---------------------------------------------------------------------
-- 1. locations
--
--    Note that x_coord holds the latitude and y_coord the longitude.
--    That is the reverse of the usual convention, but it is what the
--    project template shows, so we follow it.
--
--    source and confidence say where each row came from:
--      A = measured on the ground or from imagery
--      C = position estimated between measured points
--      D = figure worked out rather than observed
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS locations (
    location_id    VARCHAR(8)    NOT NULL,
    name           VARCHAR(120)  NOT NULL,
    area           VARCHAR(32)   NOT NULL,
    location_type  VARCHAR(32)   NOT NULL,
    x_coord        DECIMAL(10,7) NOT NULL,
    y_coord        DECIMAL(10,7) NOT NULL,
    internal_code  VARCHAR(16)   NOT NULL,
    zone           VARCHAR(32),
    source         VARCHAR(32)   NOT NULL DEFAULT 'MODELLED',
    confidence     CHAR(1)       NOT NULL DEFAULT 'C',
    notes          TEXT,
    PRIMARY KEY (location_id),
    UNIQUE KEY uq_locations_internal_code (internal_code),
    KEY ix_locations_zone (zone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 2. roads
--
--    weighted_cost_min is the number the routing algorithms use. It is
--    the travel time multiplied by the condition weight, so an unpaved
--    path costs more than a paved one of the same length.
--
--    The two obs_time columns stay empty until someone walks the paths
--    with a stopwatch.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roads (
    road_id            VARCHAR(8)    NOT NULL,
    from_location_id   VARCHAR(8)    NOT NULL,
    to_location_id     VARCHAR(8)    NOT NULL,
    distance_km        DECIMAL(10,4) NOT NULL,
    travel_time_min    DECIMAL(8,2)  NOT NULL,
    condition_weight   DECIMAL(6,3)  NOT NULL DEFAULT 1.000,
    road_type          VARCHAR(24)   NOT NULL,
    surface            VARCHAR(16),
    covered            CHAR(1),
    distance_m         DECIMAL(10,2) NOT NULL,
    walk_time_min      DECIMAL(8,2),
    weighted_cost_min  DECIMAL(10,3) NOT NULL,
    is_accessible      CHAR(1)       NOT NULL DEFAULT 'Y',
    congestion_zone    VARCHAR(24)   NOT NULL DEFAULT 'NORMAL',
    obs_time_0900_min  DECIMAL(8,2)  DEFAULT NULL,
    obs_time_1500_min  DECIMAL(8,2)  DEFAULT NULL,
    source             VARCHAR(32)   NOT NULL DEFAULT 'MODELLED',
    confidence         CHAR(1)       NOT NULL DEFAULT 'C',
    notes              TEXT,
    PRIMARY KEY (road_id),
    UNIQUE KEY uq_roads_pair (from_location_id, to_location_id),
    KEY ix_roads_from (from_location_id),
    KEY ix_roads_to (to_location_id),
    CONSTRAINT fk_roads_from FOREIGN KEY (from_location_id)
        REFERENCES locations (location_id) ON DELETE CASCADE,
    CONSTRAINT fk_roads_to FOREIGN KEY (to_location_id)
        REFERENCES locations (location_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 3. resources
--
--    Porters, nurses, wheelchairs, trolleys, ambulances.
--    Every row carries confidence D, because nobody has counted them yet.
--    The numbers were worked out from the hospital's published staff
--    total of 302, of whom 232 deal with patients directly.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS resources (
    resource_id          VARCHAR(8)  NOT NULL,
    resource_type        VARCHAR(32) NOT NULL,
    home_location_id     VARCHAR(8)  NOT NULL,
    capacity             INT         NOT NULL DEFAULT 1,
    availability_status  VARCHAR(16) NOT NULL DEFAULT 'AVAILABLE',
    shift                VARCHAR(16) NOT NULL DEFAULT 'ALL',
    available_from       TIME,
    available_to         TIME,
    source               VARCHAR(32) NOT NULL DEFAULT 'DERIVED',
    confidence           CHAR(1)     NOT NULL DEFAULT 'D',
    PRIMARY KEY (resource_id),
    KEY ix_resources_type (resource_type),
    CONSTRAINT fk_resources_home FOREIGN KEY (home_location_id)
        REFERENCES locations (location_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 4. service_requests
--
--    All 300 rows are made up. They were generated from the hospital's
--    published clinic timetable, the published list of services, the real
--    triage scale and our surveyed location codes. No patient information
--    was used at any point, and the source column says SYNTHETIC so that
--    nobody can mistake them for real records.
--
--    urgency is a number because the project template asks for one:
--    5 is red, 4 orange, 3 yellow, 2 green, 1 blue.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS service_requests (
    request_id               VARCHAR(8)  NOT NULL,
    source_location_id       VARCHAR(8)  NOT NULL,
    destination_location_id  VARCHAR(8),
    category                 VARCHAR(48) NOT NULL,
    urgency                  INT         NOT NULL,
    time_submitted           DATETIME    NOT NULL,
    deadline                 DATETIME    NOT NULL,
    status                   VARCHAR(16) NOT NULL DEFAULT 'NEW',
    sats_colour              VARCHAR(8)  NOT NULL,
    tews_score               TINYINT,
    patient_class            VARCHAR(16) NOT NULL,
    requires_gp_referral     TINYINT(1)  NOT NULL DEFAULT 0,
    eligible_at_clinic       TINYINT(1)  NOT NULL DEFAULT 0,
    assigned_resource_id     VARCHAR(8),
    target_clinic            VARCHAR(32),
    porter_minutes           INT         NOT NULL DEFAULT 0,
    source                   VARCHAR(32) NOT NULL DEFAULT 'SYNTHETIC',
    PRIMARY KEY (request_id),
    KEY ix_requests_status (status),
    KEY ix_requests_colour (sats_colour),
    KEY ix_requests_submitted (time_submitted),
    CONSTRAINT fk_requests_source FOREIGN KEY (source_location_id)
        REFERENCES locations (location_id),
    CONSTRAINT fk_requests_destination FOREIGN KEY (destination_location_id)
        REFERENCES locations (location_id),
    CONSTRAINT fk_requests_resource FOREIGN KEY (assigned_resource_id)
        REFERENCES resources (resource_id) ON DELETE SET NULL,
    CONSTRAINT ck_requests_urgency CHECK (urgency BETWEEN 1 AND 5),
    CONSTRAINT ck_requests_tews CHECK (tews_score IS NULL OR tews_score BETWEEN 0 AND 17)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 5. algorithm_runs
--
--    One row per timing measurement. The graphs in report section 10 are
--    drawn from this table.
--
--    repetition 1 is always the warm-up pass and is left out of every
--    average, because Java speeds up as it runs.
--
--    machine_spec must be the same on every row. Mixing computers would
--    make the numbers meaningless.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS algorithm_runs (
    run_id          INT AUTO_INCREMENT NOT NULL,
    experiment_id   VARCHAR(4)   NOT NULL,
    algorithm_name  VARCHAR(64)  NOT NULL,
    structure_name  VARCHAR(64),
    input_size      INT          NOT NULL,
    repetition      TINYINT      NOT NULL DEFAULT 1,
    elapsed_ns      BIGINT       NOT NULL,
    primitive_ops   BIGINT,
    machine_spec    VARCHAR(200) NOT NULL,
    run_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (run_id),
    KEY ix_runs_experiment (experiment_id, algorithm_name, input_size)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- 6. audit_events
--
--    Every decision the program makes, with what the data looked like
--    before. undo_payload is what makes the undo feature possible.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_events (
    event_id      INT AUTO_INCREMENT NOT NULL,
    event_type    VARCHAR(48) NOT NULL,
    entity_table  VARCHAR(32),
    entity_id     VARCHAR(16),
    actor         VARCHAR(64),
    payload       TEXT,
    undo_payload  TEXT,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (event_id),
    KEY ix_audit_type (event_type),
    KEY ix_audit_entity (entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Checks to run after loading the seed data
-- ---------------------------------------------------------------------
-- SELECT COUNT(*) AS locations FROM locations;              -- expect 52
-- SELECT COUNT(*) AS roads FROM roads;                      -- expect 116
-- SELECT COUNT(*) AS resources FROM resources;              -- expect 34
-- SELECT COUNT(*) AS requests FROM service_requests;        -- expect 300
--
-- How much of the data is still estimated rather than measured:
-- SELECT source, confidence, COUNT(*) FROM locations GROUP BY source, confidence;
--
-- Does the generated urgency mix match the guide?
-- Expect RED 12, ORANGE 33, YELLOW 120, GREEN 132, BLUE 3.
-- SELECT sats_colour, COUNT(*) FROM service_requests GROUP BY sats_colour;
--
-- Any place with no path touching it? This should return no rows.
-- SELECT l.location_id, l.name FROM locations l
--   LEFT JOIN roads r ON r.from_location_id = l.location_id
--                     OR r.to_location_id = l.location_id
--   WHERE r.road_id IS NULL;
