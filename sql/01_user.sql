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
-- Run this ONCE per MySQL server, as root, before 02_schema.sql. It is
-- idempotent: re-running it neither fails nor changes an existing password.
-- The credentials must match DB_USER / DB_PASS in
-- src/main/java/resources/Merc.properties.

CREATE USER IF NOT EXISTS 'MercTuTs'@'localhost' IDENTIFIED BY 'MercTu2025';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP
   ON MercTuDB.*
   TO 'MercTuTs'@'localhost';

FLUSH PRIVILEGES;
