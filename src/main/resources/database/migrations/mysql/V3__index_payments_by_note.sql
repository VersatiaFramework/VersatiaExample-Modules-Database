-- MySQL and MariaDB can index a LONGTEXT column only by a prefix
CREATE INDEX {prefix}payments_note ON {prefix}payments (note(64));
