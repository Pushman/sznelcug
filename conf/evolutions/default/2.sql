# Users schema
 
# --- !Ups
 
ALTER TABLE User ADD COLUMN sessionKey varchar(100) NULL;
UPDATE User SET sessionKey = '';
ALTER TABLE User ALTER COLUMN sessionKey SET NOT NULL;
 
# --- !Downs
 
ALTER TABLE User DROP COLUMN sessionKey;