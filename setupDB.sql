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

-- Creates the tutor's database user, builds MercTuDB from scratch, and loads
-- the "See One" seed data. This is the only command needed to set up a
-- development machine:
--
--   mysql -u root -p < setupDB.sql
--
-- Run it from the repository root. The SOURCE paths below resolve against the
-- directory mysql was started in, not against this file's location.
--
-- Everything here is safe to re-run. The user is created only if absent, and
-- the database is rebuilt from nothing each time.
--
-- WARNING: this DROPS MercTuDB. Every account, tutoring session and student
-- model in it is destroyed. There is no migration path from an older schema
-- -- recreating is the supported upgrade.

DROP DATABASE IF EXISTS MercTuDB;

CREATE DATABASE MercTuDB;

USE MercTuDB;

SOURCE sql/01_user.sql;

SOURCE sql/02_schema.sql;

SOURCE sql/03_seed.sql;
