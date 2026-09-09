# MercTu — Multiple External Representations of Computation Tutor

MercTu is an intelligent tutoring system (ITS) that teaches the three classical
models of computation — **Turing Machines**, **Lambda Calculus**, and
**Mu-Recursive Functions** — by showing the *same* computation in all three
representations side by side.

The core idea: a student who can recognize the zero function as a
Turing machine, as `\f. \x. x`, and as `Z(x) = 0` understands computation in a
way that a student who has only seen one notation does not. The tutor walks
students through problems, steps them through each representation, tracks what
they know, and offers hints scaled to their current level.

Built as a Regis University capstone project on top of Dr. Richard Blumenthal's
tutoring framework.

## Requirements

- **JDK 24** or later (`maven.compiler.release` is set to 24)
- **Maven 3.9+**
- **MySQL 8.x**

Dependencies, resolved by Maven: Gson 2.8.6, MySQL Connector/J 9.0.0, and
JUnit 4.12 / JUnit Jupiter 5.10.0 for tests.

## Setup

### 1. Create the database

The setup script drops and recreates the `MercTuDB` database, creates every
table, and seeds the "See One" unit — the Zero Function overview problem with
its Turing machine, hints, and knowledge component.

```bash
cd MercTuApp
mysql -u root -p < setupDB.sql
```

The script's `CREATE USER` line is commented out. Create the tutor's database
user yourself before running it, or uncomment the line:

```sql
CREATE USER 'MercTuTs'@'localhost' IDENTIFIED BY 'MercTu2025';
```

Three SQL scripts are provided:

- `setupDB.sql` — canonical schema plus seed data; includes the `GRANT`.
- `setupDB_local.sql` — same schema with the `GRANT` commented out (for running
  everything as a local root user), plus extra seed hints for the lambda
  parameter-selection steps.
- `setupDB_lambdaTables.sql` — the `LC_*` lambda expression tables only, written
  idempotently with `CREATE TABLE IF NOT EXISTS`; useful for adding them to an
  existing database.

`deleteTestUser.sql` removes the `test@regis.edu` account between manual test
runs.

### 2. Configure the connection

Database settings live in
[Merc.properties](MercTuApp/src/main/java/resources/Merc.properties):

| Property | Default |
| --- | --- |
| `edu.regis.merc.DB_HOST` | `localhost` |
| `edu.regis.merc.DB_PORT` | `3306` |
| `edu.regis.merc.DB_NAME` | `MercTuDB` |
| `edu.regis.merc.DB_USER` | `MercTuTs` |
| `edu.regis.merc.DB_PASS` | `MercTu2025` |

The same file sets the UI `language` and `country` used to pick the resource
bundle in `Msgs.properties`.

## Build and run

```bash
cd MercTuApp
mvn compile
mvn exec:java          # main class comes from the exec.mainClass property
```

Or run `edu.regis.merc.MercApp` from your IDE — NetBeans, IntelliJ, and VS Code
configurations are all checked in.

On startup the app initializes logging, loads properties, starts `MercServer` on
port **53640**, waits four seconds for it to come up, then shows the splash
screen. Sign in or create an account; a successful sign-in opens the main
tutoring window.

Note that `mvn package` produces a jar with neither a `Main-Class` manifest entry
nor bundled dependencies, so `java -jar` will not work as-is.

## Tests

```bash
cd MercTuApp
mvn test
```

`TestLCParser` (JUnit 5) is the only test under `src/test`. Two additional
drivers live in `src/main` and run as `main` methods rather than through Maven:

- `edu.regis.merc.MuTest` — prints mu-recursive function formatting checks;
  expected output is recorded in `MuTestOutput.txt`.
- `edu.regis.merc.MuDAOTest` — inserts and reads back a mu function; requires a
  live database.

## Authors

Framework and the majority of the codebase by Richard Blumenthal (`rickb`), with
capstone contributions from Regis Students.

## License

© Richard Blumenthal, all rights reserved. Unauthorized use, duplication, or
distribution without the authors' permission is prohibited. The software is
distributed on an "AS IS" basis without warranties or conditions of any kind,
either expressed or implied.
