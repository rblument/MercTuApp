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

One command, run as root from the repository root:

```bash
mysql -u root -p < setupDB.sql
```

It creates the `MercTuTs` database user, rebuilds `MercTuDB`, and seeds the
"See One" unit — the Zero Function overview problem with its Turing machine,
lambda expressions, mu-recursive function, hints, and knowledge component.

> **This destroys any existing `MercTuDB`.** The script begins with
> `DROP DATABASE IF EXISTS`, so every account, tutoring session, and student
> model in it is lost. There is no migration path between schema versions —
> recreating is the supported upgrade. Re-running the script is otherwise
> safe, and is how you pick up a schema change from `development`.

Run it from the repository root specifically: `setupDB.sql` pulls in the files
under `sql/` with `SOURCE`, whose paths resolve against your current directory
rather than the script's own location.

| File | Contents |
| --- | --- |
| `setupDB.sql` | Entry point. Drops and recreates the database, then sources the three below in order. |
| `sql/01_user.sql` | Creates `MercTuTs` and grants it privileges on `MercTuDB`. Idempotent. |
| `sql/02_schema.sql` | Every table definition — the single source of truth for the schema. |
| `sql/03_seed.sql` | The "See One" seed data. |
| `deleteTestUser.sql` | Removes the `test@regis.edu` account between manual test runs. |

Add new tables to `sql/02_schema.sql` and new seed rows to `sql/03_seed.sql`.
Do not copy table definitions into other scripts — three partial copies of the
schema is what this layout replaced, and they had silently drifted apart.

To confirm the setup worked, connect as the application's own user:

```bash
mysql -u MercTuTs -p MercTuDB -e \
  "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='MercTuDB';"
```

Expect **39** tables. If you get 0, your `mysql` client did not process the
`SOURCE` lines; run `sql/01_user.sql`, `sql/02_schema.sql`, and
`sql/03_seed.sql` by hand instead.

If the application later fails to authenticate, a `MercTuTs` user from an
earlier setup may have a different password — `CREATE USER IF NOT EXISTS`
leaves existing users untouched. Reset it to match `Merc.properties`:

```sql
ALTER USER 'MercTuTs'@'localhost' IDENTIFIED BY 'MercTu2025';
```

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
mvn test                                      # everything
mvn -Dtest=TuringMachineTest test             # one class
mvn -Dtest='TuringMachineTest#testStep' test  # one method (quote it in bash)
```

Fifteen JUnit 5 tests across four classes under `src/test`, all of which run in
CI on pull requests to `main` and `development`:

| Class | Covers |
| --- | --- |
| `model.TuringMachineTest` | Accept and reject runs of a Turing machine over `0`/`1` input. |
| `model.MuFunctionTest` | Mu-recursive function formatting for each operator. |
| `MuTest` | Zero, successor, and nested mu-recursive functions. |
| `test.TestLCParser` | The lambda calculus parser. |

None of them need a database. Failure detail lands in
`target/surefire-reports/`.

One driver still runs as a `main` method rather than through Maven:

- `edu.regis.merc.MuDAOTest` — inserts and reads back a mu function. It
  requires a live database.

## Authors

Framework and the majority of the codebase by Richard Blumenthal (`rickb`), with
capstone contributions from Regis Students.

## License

© Richard Blumenthal, all rights reserved. Unauthorized use, duplication, or
distribution without the authors' permission is prohibited. The software is
distributed on an "AS IS" basis without warranties or conditions of any kind,
either expressed or implied.
