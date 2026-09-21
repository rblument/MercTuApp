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
-- password.
--
-- The credentials must match DB_USER / DB_PASS in
-- src/main/java/resources/Merc.properties.

CREATE USER IF NOT EXISTS 'MercTuTs'@'localhost' IDENTIFIED BY 'MercTu2025';

-- Data privileges only. The application issues no DDL -- every SQL statement
-- in src/ is a SELECT, INSERT, UPDATE or DELETE -- so it has no need to
-- create, drop or alter tables, and should not be able to. setupDB.sql does
-- its DROP DATABASE as root, not as this user, so setup is unaffected.
--
-- A future schema-migration tool (Flyway or a startup migration runner) would
-- need CREATE and ALTER added back. Add them deliberately at that point
-- rather than granting them ahead of any need.
GRANT SELECT, INSERT, UPDATE, DELETE
   ON MercTuDB.*
   TO 'MercTuTs'@'localhost';
