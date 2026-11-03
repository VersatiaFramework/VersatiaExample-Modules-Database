CREATE TABLE {prefix}payments (
    id      {identity},
    payer   {uuid} NOT NULL,
    payee   {uuid} NOT NULL,
    coins   BIGINT NOT NULL,
    note    {text} NOT NULL,
    paid_at {timestamp} NOT NULL DEFAULT {now}
) {table_options};
