ALTER TABLE PUBLIC.POSTS ADD COLUMN PREVIEW TEXT;

ALTER TABLE PUBLIC.USERS ADD COLUMN PROMOTION_SCALE REAL DEFAULT (1.0);

ALTER TABLE PUBLIC.USERS ADD COLUMN PROMOTION_LEVEL VARCHAR DEFAULT ('BASIC');

ALTER TABLE PUBLIC.USERS DROP COLUMN DIRECTION_ID;