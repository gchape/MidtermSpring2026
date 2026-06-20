package tech.provokedynamic.uno.db;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Builds and holds the single SqlSessionFactory for the application.
 * Call {@link #init(Properties)} once at startup before any repository use.
 *
 * <p>Credentials are resolved from environment variables
 * ({@code DB_USERNAME}, {@code DB_PASSWORD}) with sensible defaults for
 * the embedded H2 database used in development and testing. Do not put
 * real credentials in source code.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Database {

    private static SqlSessionFactory factory;

    public static void init(Properties props) {
        log.info("Initialising database: url={}", props.getProperty("db.url"));
        try (InputStream is = Resources.getResourceAsStream("mybatis-config.xml")) {
            factory = new SqlSessionFactoryBuilder().build(is, props);
            log.debug("SqlSessionFactory created successfully");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load mybatis-config.xml", e);
        }
    }

    public static SqlSessionFactory factory() {
        if (factory == null) {
            throw new IllegalStateException("Database.init() has not been called");
        }
        return factory;
    }

    /**
     * Convenience: build properties for an H2 file database at the given path.
     *
     * <p>Username/password are read from the {@code DB_USERNAME} / {@code DB_PASSWORD}
     * environment variables. If unset, the H2 embedded defaults ("sa" / "") are used,
     * which is safe because the embedded H2 instance is not network-accessible.
     */
    public static Properties h2FileProps(String filePath) {
        log.debug("Building H2 file props: path={}", filePath);
        Properties p = new Properties();
        p.setProperty("db.driver", "org.h2.Driver");
        p.setProperty("db.url", "jdbc:h2:file:" + filePath + ";AUTO_SERVER=TRUE");
        p.setProperty("db.username", envOrDefault("DB_USERNAME", "sa"));
        p.setProperty("db.password", envOrDefault("DB_PASSWORD", ""));
        return p;
    }

    /**
     * Convenience: build properties for an in-memory H2 database (tests).
     */
    public static Properties h2MemProps(String name) {
        log.debug("Building H2 in-memory props: name={}", name);
        Properties p = new Properties();
        p.setProperty("db.driver", "org.h2.Driver");
        p.setProperty("db.url", "jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1");
        p.setProperty("db.username", envOrDefault("DB_USERNAME", "sa"));
        p.setProperty("db.password", envOrDefault("DB_PASSWORD", ""));
        return p;
    }

    private static String envOrDefault(String envVar, String defaultValue) {
        String value = System.getenv(envVar);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }
}
