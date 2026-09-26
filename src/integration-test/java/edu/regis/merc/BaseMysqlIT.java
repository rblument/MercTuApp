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
package edu.regis.merc;

import edu.regis.merc.dao.AccountDAO;
import edu.regis.merc.dao.MySqlDAO;
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
    public static void setUpClass() {
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

        MySqlDAO unused = new AccountDAO(); //have the MySqlDAO go through it's own startup and load the driver.
        MySqlDAO.URL = String.format("%s?user=%s&password=%s", mysqlContainer.getJdbcUrl(), DB_USER, DB_PASS); //update with new connection information.
    }

    @AfterAll
    public static void tearDownClass() {
        if (mysqlContainer != null) {
            mysqlContainer.stop();
        }
    }
}
