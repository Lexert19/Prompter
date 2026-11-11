CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE metric_point (
    id    BIGSERIAL,
    time  TIMESTAMPTZ NOT NULL,
    name  VARCHAR(128) NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (id, time)
);

SELECT create_hypertable('metric_point', 'time', if_not_exists => TRUE);
CREATE INDEX idx_mp_name_time ON metric_point(name, time DESC);

ALTER TABLE metric_point SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'name',
    timescaledb.compress_orderby   = 'time DESC'
);
SELECT add_compression_policy('metric_point', INTERVAL '7 days', if_not_exists => TRUE);
SELECT add_retention_policy('metric_point', INTERVAL '30 days', if_not_exists => TRUE);