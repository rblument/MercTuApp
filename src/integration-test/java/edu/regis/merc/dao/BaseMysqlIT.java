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
package edu.regis.merc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.MountableFile;

/**
 * Base class for integration tests. Starts a MySQL 8.0 container, initializes
 * it with the same scripts setupDB.sql sources (schema + seed data), and
 * points MySqlDAO at the container.
 *
 * The @Tag("integration") keeps these tests out of plain unit test runs:
 * surefire excludes the tag, failsafe includes only the tag.
 */
@Tag("integration")
public abstract class BaseMysqlIT {

    protected static final String DB_NAME = "MercTuDB";
    protected static final String DB_USER = "MercTuTs";
    protected static final String DB_PASS = "MercTu2025";

    protected static MySQLContainer<?> mysqlContainer;

    @BeforeAll
    public static void setUpClass() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if ("true".equals(System.getProperty("merctu.it.local"))) {
            configureLocalDatabase();
            return;
        }
        // setupDB.sql uses the mysql client's SOURCE command, which cannot run
        // as a container init script, so the scripts it sources are copied
        // into /docker-entrypoint-initdb.d/ instead. The official image runs
        // them via the mysql client with MercTuDB already created and
        // selected, in filename order.

        mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USER)
            .withPassword(DB_PASS)
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("01_user.sql"),
                "/docker-entrypoint-initdb.d/01_user.sql")
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("02_schema.sql"),
                "/docker-entrypoint-initdb.d/02_schema.sql")
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("03_seed.sql"),
                "/docker-entrypoint-initdb.d/03_seed.sql");

        mysqlContainer.start();
        MySqlDAO.setURL(mysqlContainer.getHost(), mysqlContainer.getDatabaseName(), DB_USER, DB_PASS, mysqlContainer.getMappedPort(mysqlContainer.MYSQL_PORT));
    }

    /** Uses an already initialized isolated database; never creates or resets it. */
    private static void configureLocalDatabase() {
        String database = System.getenv("MERCTU_IT_DATABASE");
        String user = System.getenv("MERCTU_IT_USER");
        String password = System.getenv("MERCTU_IT_PASSWORD");
        if (!"merctu_logout_test".equals(database) || !"merctu_logout_test".equals(user)
                || password == null || password.isBlank()) {
            throw new IllegalStateException("Local integration tests require the merctu_logout_test database and dedicated user, and a password.");
        }
        MySqlDAO.setURL("localhost", database, user, password, 3306);
        try {
            MySqlDAO.atomicallyLoadDriver();
            try (Connection conn = DriverManager.getConnection(MySqlDAO.getURL());
                    Statement statement = conn.createStatement();
                    ResultSet result = statement.executeQuery("SELECT DATABASE(), CURRENT_USER()")) {
                if (!result.next() || !database.equals(result.getString(1))
                        || !"merctu_logout_test@localhost".equals(result.getString(2))) {
                    throw new IllegalStateException("Local integration database identity check failed.");
                }
            }
        } catch (Exception ex) {
            // JDBC URLs contain credentials; do not expose the original exception.
            throw new IllegalStateException("Cannot connect to the isolated local integration database. Check setup and credentials.");
        }
    }

    @AfterAll
    public static void tearDownClass() {
        if (mysqlContainer != null) {
            mysqlContainer.stop();
        }
    }
}
