CREATE TABLE IF NOT EXISTS creators (
    id   VARCHAR(255) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS courses (
    id         VARCHAR(255) NOT NULL PRIMARY KEY,
    creator_id VARCHAR(255) NOT NULL,
    title      VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS sale_records (
    id         VARCHAR(255)   NOT NULL PRIMARY KEY,
    course_id  VARCHAR(255)   NOT NULL,
    student_id VARCHAR(255)   NOT NULL,
    amount     NUMERIC(12, 2) NOT NULL,
    paid_at    TIMESTAMPTZ    NOT NULL
);

CREATE TABLE IF NOT EXISTS cancellation_records (
    id             BIGSERIAL      PRIMARY KEY,
    sale_record_id VARCHAR(255)   NOT NULL,
    refund_amount  NUMERIC(12, 2) NOT NULL,
    canceled_at    TIMESTAMPTZ    NOT NULL
);

CREATE TABLE IF NOT EXISTS settlements (
    id                  BIGSERIAL      PRIMARY KEY,
    creator_id          VARCHAR(255)   NOT NULL,
    month               VARCHAR(7)     NOT NULL,
    status              VARCHAR(50)    NOT NULL,
    total_sales         NUMERIC(12, 2) NOT NULL,
    total_refunds       NUMERIC(12, 2) NOT NULL,
    net_sales           NUMERIC(12, 2) NOT NULL,
    platform_fee        NUMERIC(12, 2) NOT NULL,
    settlement_amount   NUMERIC(12, 2) NOT NULL,
    sale_count          INT            NOT NULL,
    cancellation_count  INT            NOT NULL,
    confirmed_at        TIMESTAMPTZ    NOT NULL,
    paid_at             TIMESTAMPTZ,
    UNIQUE (creator_id, month)
);
