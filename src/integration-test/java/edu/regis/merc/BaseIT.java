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

import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("Integration Test Base")
public abstract class BaseIT {

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

        pointMySqlDaoAt(mysqlContainer.getJdbcUrl() + "?user="
            + mysqlContainer.getUsername() + "&password=" + mysqlContainer.getPassword());
    }

    @AfterAll
    public static void tearDownClass() {
        if (mysqlContainer != null) {
            mysqlContainer.stop();
        }
    }

    /**
     * MySqlDAO builds its connection URL once, from Merc.properties, guarded
     * by the IS_LOADED flag. Overwrite both via reflection so every DAO in
     * this JVM connects to the container instead of a local database.
     */
    private static void pointMySqlDaoAt(String url) {
        try {
            Class<?> daoClass = Class.forName("edu.regis.merc.dao.MySqlDAO");

            Field urlField = daoClass.getDeclaredField("URL");
            urlField.setAccessible(true);
            urlField.set(null, url);

            Field loadedField = daoClass.getDeclaredField("IS_LOADED");
            loadedField.setAccessible(true);
            loadedField.set(null, true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to point MySqlDAO at the test container", e);
        }
    }
}
