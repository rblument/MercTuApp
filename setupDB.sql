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

-- Creates MercTuDB from scratch and loads the "See One" seed data.
--
--   mysql -u root -p < setupDB.sql
--
-- Run it from the repository root: the SOURCE paths below are relative to
-- the directory mysql was started in, not to this file.
--
-- Create the tutor's database user first, once per MySQL server:
--
--   mysql -u root -p < sql/01_user.sql
--
-- WARNING: this DROPS MercTuDB. Every account, tutoring session and student
-- model in it is destroyed. There is no migration path from an older schema
-- -- recreating is the supported upgrade.

DROP DATABASE IF EXISTS MercTuDB;

CREATE DATABASE MercTuDB;

USE MercTuDB;

SOURCE sql/02_schema.sql;

SOURCE sql/03_seed.sql;
