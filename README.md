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

### Execution modes

The application supports three modes: **both** (default), **server**, and **client**.

| Mode | Command | Description |
| --- | --- | --- |
| Both (default) | `mvn exec:java` | Starts server in background, then client GUI |
| Server only | `mvn -Pserver exec:java` | Runs server in foreground (no GUI) |
| Client only | `mvn -Pclient exec:java` | Connects to existing server (no server startup) |

#### Server mode

Server-only mode runs in the foreground and does not start a GUI:

```bash
mvn -Pserver exec:java
```

The server reads port configuration from `server.properties` (default: 53640).
Shutdown with `Ctrl+C` or send SIGTERM.

#### Client mode

Client-only mode connects to an existing server and requires the server to be
running first:

```bash
mvn -Pclient exec:java
```

Client configuration is read from `client.properties`:
- `server.host` (default: `localhost`)
- `server.port` (default: `53640`)

### Configuration

Server and client use separate configuration files in
`src/main/java/resources/`:

| File | Properties |
| --- | --- |
| `server.properties` | `server.port` |
| `client.properties` | `server.host`, `server.port` |

Override defaults by passing an external properties file via `--config=<path>`.
External file overrides classpath defaults.

**Example:**

```bash
# Create custom config file
echo "server.port=9999" > custom-server.properties

# Run server with custom config
mvn -Pserver exec:java -Dexec.args="--server --config=custom-server.properties"
```

### TLS transport

PR #41's `--server`, `--client`, Maven profiles, and `--config` remain the startup
interface. Enable TLS in the external properties file (`tls.properties`):

```properties
server.tls=true
server.host=localhost
# Optional: server.port=53641
```

Both client and server default to port 53641 with TLS, or 53640 with plaintext.
An explicit `server.port` always wins. `server.host` is the client destination.
No TLS switch or separate transport startup parser is required. Existing plaintext
configurations continue to work. Invalid configuration or TLS stores fail startup
instead of falling back to plaintext.

The server uses standard JVM `javax.net.ssl.keyStore`, `keyStoreType`, and
`keyStorePassword` properties; the client uses `javax.net.ssl.trustStore`,
`trustStoreType`, and `trustStorePassword`. Prefix all property names with
`javax.net.ssl.`. An omitted client truststore uses JSSE's platform trust roots.
TLS clients validate both the certificate chain and the destination hostname.

### Local development certificate

Use the JDK's `keytool` (in `JAVA_HOME/bin` if it is not on PATH). From the
project directory, create the ignored `.dev-tls` directory with `mkdir .dev-tls`.
Run each command below as one line; keytool prompts for store passwords. Use the
same password for the server private key and its keystore. Choose your own passwords;
no password file or generated store belongs in Git.

```text
keytool -genkeypair -alias merc-server -keyalg RSA -keysize 2048 -validity 365 -dname "CN=localhost" -ext "SAN=dns:localhost,ip:127.0.0.1" -storetype PKCS12 -keystore .dev-tls/server.p12
keytool -exportcert -alias merc-server -keystore .dev-tls/server.p12 -file .dev-tls/server.cer
keytool -importcert -alias merc-server -file .dev-tls/server.cer -storetype PKCS12 -keystore .dev-tls/client-trust.p12
```

Confirm the certificate fingerprint during import. For another host, replace/add
its actual DNS name or IP in the SAN list before generating the certificate.
Copy only the exported public certificate to the client and import it into the
client's own truststore; the server's private-key store stays on the server.

Launch the server with `--server --config=tls.properties` and these JVM properties:

```text
-Djavax.net.ssl.keyStore=.dev-tls/server.p12
-Djavax.net.ssl.keyStoreType=PKCS12
-Djavax.net.ssl.keyStorePassword=<your-server-store-password>
```

Launch the client with `--client --config=tls.properties` and:

```text
-Djavax.net.ssl.trustStore=.dev-tls/client-trust.p12
-Djavax.net.ssl.trustStoreType=PKCS12
-Djavax.net.ssl.trustStorePassword=<your-client-store-password>
```

Replace placeholders locally; do not save actual passwords in tracked launch
configurations. With `mvn exec:java`, pass the same JVM properties to Maven and
use `-Dexec.args="--server --config=tls.properties"` or the corresponding client arguments.
For `--config=tls.properties`, supply both sets of store properties to the same process.

Connection and TLS handshake read timeouts are 10 seconds; request reads time out
after 15 seconds. The server binds before reporting startup success and continues
accepting connections after a client handshake or request failure.

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

Local TLS regression tests run with `mvn clean test`, using the running JDK's
keytool, temporary stores/random passwords, and ephemeral ports. They verify
trusted TLS and plaintext JSON exchanges, untrusted/hostname-mismatched certificate
rejection, wrong passwords, and socket shutdown without Docker or database access.
Docker integration tests from PR #41 are unchanged and run separately with `mvn verify`.

## Authors

Framework and the majority of the codebase by Richard Blumenthal (`rickb`), with
capstone contributions from Regis Students.

## License

© Richard Blumenthal, all rights reserved. Unauthorized use, duplication, or
distribution without the authors' permission is prohibited. The software is
distributed on an "AS IS" basis without warranties or conditions of any kind,
either expressed or implied.
