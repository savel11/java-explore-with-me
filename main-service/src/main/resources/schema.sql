CREATE TABLE IF NOT EXISTS users (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 name VARCHAR(300) NOT NULL,
 email VARCHAR(300) NOT NULL,
 CONSTRAINT uq_user_email UNIQUE (email),
 CONSTRAINT length_users_name CHECK (length(name) BETWEEN 2 AND 250),
 CONSTRAINT length_users_email CHECK (length(email) BETWEEN 6 AND 254)
);
CREATE TABLE IF NOT EXISTS categories (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 name VARCHAR(100) NOT NULL,
 CONSTRAINT length_category_name CHECK (length(name) BETWEEN 1 AND 50),
 CONSTRAINT uq_category_name UNIQUE (name)
 );
CREATE TABLE IF NOT EXISTS locations (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 lat FLOAT NOT NULL,
 lon FLOAT NOT NULL
);
CREATE TABLE IF NOT EXISTS events (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 annotation VARCHAR(2000) NOT NULL,
 category_id BIGINT NOT NULL,
 confirmed_requests BIGINT,
 created_on TIMESTAMP WITHOUT TIME ZONE NOT NULL,
 description VARCHAR(7000) NOT NULL,
 event_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
 initiator_id BIGINT NOT NULL,
 location_id BIGINT NOT NULL,
 paid BOOL DEFAULT FALSE,
 participant_limit INTEGER DEFAULT 0,
 published_on TIMESTAMP WITHOUT TIME ZONE NOT NULL,
 request_moderation BOOL DEFAULT TRUE,
 state VARCHAR(30),
 title VARCHAR(200),
 CONSTRAINT length_event_annotation CHECK (length(annotation) BETWEEN 20 AND 2000),
 CONSTRAINT length_event_description CHECK (length(description) BETWEEN 20 AND 7000),
 CONSTRAINT state_enum CHECK (state in ('PENDING', 'PUBLISHED', 'CANCELED')),
 CONSTRAINT length_event_title CHECK (length(title) BETWEEN 3 AND 120),
 CONSTRAINT fk_events_to_category FOREIGN KEY (category_id) REFERENCES categories (id),
 CONSTRAINT fk_events_to_users FOREIGN KEY (initiator_id) REFERENCES users (id),
 CONSTRAINT fk_events_to_locations FOREIGN KEY (location_id) REFERENCES locations (id)
 );
CREATE TABLE IF NOT EXISTS requests (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
 event_id BIGINT NOT NULL,
 requester_id BIGINT NOT NULL,
 request_status VARCHAR(50) NOT NULL,
 CONSTRAINT fk_requests_to_users FOREIGN KEY (requester_id) REFERENCES users (id),
 CONSTRAINT fk_requests_to_events FOREIGN KEY (event_id) REFERENCES events (id)
 );
CREATE TABLE IF NOT EXISTS compilations (
 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 pinned BOOL NOT NULL DEFAULT FALSE,
 title VARCHAR(100) NOT NULL,
 CONSTRAINT length_compilations_title CHECK (length(title) BETWEEN 1 AND 50)
);
CREATE TABLE IF NOT EXISTS compilation_events (
 compilation_id BIGINT,
 event_id BIGINT,
 CONSTRAINT fk_compilation_events_to_compilations FOREIGN KEY (compilation_id) REFERENCES compilations (id),
 CONSTRAINT fk_compilation_events_to_events FOREIGN KEY (event_id) REFERENCES events (id)
);

