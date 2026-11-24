CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE ip_hits (
    time TIMESTAMPTZ NOT NULL,
    ip INET
);

SELECT create_hypertable('ip_hits', 'time', chunk_time_interval => INTERVAL '1 day');

CREATE INDEX idx_ip_hits_ip_time ON ip_hits (ip, time DESC);

ALTER TABLE ip_hits SET (timescaledb.compress, timescaledb.compress_segmentby = 'ip', timescaledb.compress_orderby = 'time DESC');

SELECT add_compression_policy('ip_hits', INTERVAL '7 days');
SELECT add_retention_policy('ip_hits', INTERVAL '90 days');