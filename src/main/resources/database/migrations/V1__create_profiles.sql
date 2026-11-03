CREATE TABLE {prefix}profiles (
    uuid      {uuid} NOT NULL PRIMARY KEY,
    name      VARCHAR(16) NOT NULL DEFAULT '',
    coins     BIGINT NOT NULL DEFAULT 0,
    last_seen {timestamp}
) {table_options};
CREATE INDEX {prefix}profiles_coins ON {prefix}profiles (coins);
