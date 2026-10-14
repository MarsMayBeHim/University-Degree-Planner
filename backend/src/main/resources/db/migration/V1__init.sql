CREATE TABLE university (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    slug        VARCHAR(50)  NOT NULL UNIQUE
);

CREATE TABLE course (
    id             BIGSERIAL PRIMARY KEY,
    university_id  BIGINT       NOT NULL REFERENCES university(id),
    subject        VARCHAR(10)  NOT NULL,
    number         VARCHAR(10)  NOT NULL,
    title          VARCHAR(300) NOT NULL,
    credits_min    NUMERIC(3,1) NOT NULL,
    credits_max    NUMERIC(3,1) NOT NULL,
    description    TEXT,
    prereq_text    TEXT,
    coreq_text     TEXT,
    prereq_tree    JSONB,
    parse_status   VARCHAR(20) NOT NULL DEFAULT 'NONE',
    source_url     VARCHAR(500),
    scraped_at     TIMESTAMP,
    UNIQUE (university_id, subject, number)
);

CREATE INDEX idx_course_search ON course (university_id, subject, number);

CREATE TABLE course_offering (
    course_id  BIGINT      NOT NULL REFERENCES course(id) ON DELETE CASCADE,
    term_type  VARCHAR(10) NOT NULL CHECK (term_type IN ('FALL','SPRING','SUMMER')),
    PRIMARY KEY (course_id, term_type)
);

CREATE TABLE program (
    id             BIGSERIAL PRIMARY KEY,
    university_id  BIGINT       NOT NULL REFERENCES university(id),
    name           VARCHAR(200) NOT NULL,
    degree_type    VARCHAR(20)  NOT NULL,
    catalog_year   VARCHAR(9)   NOT NULL,
    total_credits  INT          NOT NULL,
    UNIQUE (university_id, name, degree_type, catalog_year)
);

CREATE TABLE requirement_group (
    id           BIGSERIAL PRIMARY KEY,
    program_id   BIGINT       NOT NULL REFERENCES program(id) ON DELETE CASCADE,
    parent_id    BIGINT       REFERENCES requirement_group(id) ON DELETE CASCADE,
    name         VARCHAR(200) NOT NULL,
    rule_type    VARCHAR(20)  NOT NULL CHECK (rule_type IN ('ALL','CHOOSE_N','MIN_CREDITS')),
    min_courses  INT,
    min_credits  NUMERIC(4,1),
    sort_order   INT NOT NULL DEFAULT 0
);

CREATE TABLE requirement_group_course (
    group_id   BIGINT NOT NULL REFERENCES requirement_group(id) ON DELETE CASCADE,
    course_id  BIGINT NOT NULL REFERENCES course(id),
    PRIMARY KEY (group_id, course_id)
);

CREATE TABLE app_user (
    id             BIGSERIAL PRIMARY KEY,
    email          VARCHAR(255) NOT NULL UNIQUE,
    password_hash  VARCHAR(100) NOT NULL,
    university_id  BIGINT REFERENCES university(id),
    program_id     BIGINT REFERENCES program(id),
    start_year     INT,
    start_term     VARCHAR(10),
    created_at     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE student_course (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT      NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    course_id      BIGINT      NOT NULL REFERENCES course(id),
    status         VARCHAR(15) NOT NULL CHECK (status IN ('COMPLETED','IN_PROGRESS','PLANNED')),
    planned_year   INT,
    planned_term   VARCHAR(10),
    UNIQUE (user_id, course_id)
);

INSERT INTO university (name, slug) VALUES ('George Mason University', 'gmu');