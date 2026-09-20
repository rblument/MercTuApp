/*
 * MERC^T: Multiple External Representations of Computation Tutor
 *
 *  (C) Richard Blumenthal, All rights reserved
 *
 *  Unauthorized use, duplication or distribution without the authors'
 *  permission is strictly prohibited.
 *
 *  Unless required by applicable law or agreed to in writing, this
 *  software is distributed on an "AS IS" basis without warranties
 *  or conditions of any kind, either expressed or implied.
 */

-- Creates the database user the tutor authenticates as.
--
-- Sourced by setupDB.sql; run it directly only to repair the user without
-- rebuilding the database. Requires root either way.
--
-- Idempotent: re-running neither fails nor resets an existing user's
-- password. The GRANT is issued before MercTuDB exists, which MySQL permits
-- -- database-level privileges do not require the schema to be present, and
-- they survive the DROP DATABASE that follows.
--
-- The credentials must match DB_USER / DB_PASS in
-- src/main/java/resources/Merc.properties.

CREATE USER IF NOT EXISTS 'MercTuTs'@'localhost' IDENTIFIED BY 'MercTu2025';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP
   ON MercTuDB.*
   TO 'MercTuTs'@'localhost';

FLUSH PRIVILEGES;
