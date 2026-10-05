DROP TABLE medical_reports IF EXISTS;
DROP TABLE vet_specialties IF EXISTS;
DROP TABLE vets IF EXISTS;
DROP TABLE specialties IF EXISTS;
DROP TABLE visits IF EXISTS;
DROP TABLE pets IF EXISTS;
DROP TABLE types IF EXISTS;
DROP TABLE owners IF EXISTS;
DROP TABLE roles IF EXISTS;
DROP TABLE users IF EXISTS;


CREATE TABLE vets (
  id         INTEGER IDENTITY PRIMARY KEY,
  first_name VARCHAR(30),
  last_name  VARCHAR(30),
  email      VARCHAR(255)
);
CREATE INDEX vets_last_name ON vets (last_name);
CREATE INDEX vets_email ON vets (email);

CREATE TABLE specialties (
  id   INTEGER IDENTITY PRIMARY KEY,
  name VARCHAR(80)
);
CREATE INDEX specialties_name ON specialties (name);

CREATE TABLE vet_specialties (
  vet_id       INTEGER NOT NULL,
  specialty_id INTEGER NOT NULL
);
ALTER TABLE vet_specialties ADD CONSTRAINT fk_vet_specialties_vets FOREIGN KEY (vet_id) REFERENCES vets (id);
ALTER TABLE vet_specialties ADD CONSTRAINT fk_vet_specialties_specialties FOREIGN KEY (specialty_id) REFERENCES specialties (id);

CREATE TABLE types (
  id   INTEGER IDENTITY PRIMARY KEY,
  name VARCHAR(80)
);
CREATE INDEX types_name ON types (name);

CREATE TABLE owners (
  id         INTEGER IDENTITY PRIMARY KEY,
  first_name VARCHAR(30),
  last_name  VARCHAR_IGNORECASE(30),
  address    VARCHAR(255),
  city       VARCHAR(80),
  telephone  VARCHAR(20),
  email      VARCHAR(255)
);
CREATE INDEX owners_last_name ON owners (last_name);
CREATE INDEX owners_email ON owners (email);

CREATE TABLE pets (
  id         INTEGER IDENTITY PRIMARY KEY,
  name       VARCHAR(30),
  birth_date DATE,
  type_id    INTEGER NOT NULL,
  owner_id   INTEGER NOT NULL
);
ALTER TABLE pets ADD CONSTRAINT fk_pets_owners FOREIGN KEY (owner_id) REFERENCES owners (id);
ALTER TABLE pets ADD CONSTRAINT fk_pets_types FOREIGN KEY (type_id) REFERENCES types (id);
CREATE INDEX pets_name ON pets (name);

CREATE TABLE visits (
  id          INTEGER IDENTITY PRIMARY KEY,
  pet_id      INTEGER NOT NULL,
  visit_date  DATE,
  description VARCHAR(255)
);
ALTER TABLE visits ADD CONSTRAINT fk_visits_pets FOREIGN KEY (pet_id) REFERENCES pets (id);
CREATE INDEX visits_pet_id ON visits (pet_id);

CREATE TABLE medical_reports (
  id                BIGINT IDENTITY PRIMARY KEY,
  visit_id          INTEGER NOT NULL,
  author_vet_id     INTEGER NOT NULL,
  shared_with_vet_id INTEGER,
  diagnosis         LONGVARCHAR,
  treatment         LONGVARCHAR,
  notes             LONGVARCHAR,
  report_date       DATE NOT NULL,
  status            VARCHAR(20) NOT NULL,
  created_at        TIMESTAMP NOT NULL,
  finalized_at      TIMESTAMP,
  shared_at         TIMESTAMP
);
ALTER TABLE medical_reports ADD CONSTRAINT fk_mr_visits FOREIGN KEY (visit_id) REFERENCES visits (id);
ALTER TABLE medical_reports ADD CONSTRAINT fk_mr_author_vet FOREIGN KEY (author_vet_id) REFERENCES vets (id);
ALTER TABLE medical_reports ADD CONSTRAINT fk_mr_shared_vet FOREIGN KEY (shared_with_vet_id) REFERENCES vets (id);
CREATE INDEX mr_visit_id ON medical_reports (visit_id);
CREATE INDEX mr_author_vet_id ON medical_reports (author_vet_id);
CREATE INDEX mr_shared_vet_id ON medical_reports (shared_with_vet_id);
CREATE INDEX mr_status ON medical_reports (status);

CREATE  TABLE users (
  username    VARCHAR(20) NOT NULL ,
  password    VARCHAR(60) NOT NULL ,
  enabled     BOOLEAN DEFAULT TRUE NOT NULL ,
  PRIMARY KEY (username)
);

CREATE TABLE roles (
  id              INTEGER IDENTITY PRIMARY KEY,
  username        VARCHAR(20) NOT NULL,
  role            VARCHAR(20) NOT NULL
);
ALTER TABLE roles ADD CONSTRAINT fk_username FOREIGN KEY (username) REFERENCES users (username);
CREATE INDEX fk_username_idx ON roles (username);

