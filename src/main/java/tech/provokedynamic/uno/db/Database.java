package tech.provokedynamic.uno.db;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Builds and holds the single SqlSessionFactory for the application.
 * Call {@link #init(Properties)} once at startup before any repository use.
 */
public class Database {

    private static SqlSessionFactory factory;

    private Database() {
    }

    public static void init(Properties props) {
        try (InputStream is = Resources.getResourceAsStream("mybatis-config.xml")) {
            factory = new SqlSessionFactoryBuilder().build(is, props);
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
     */
    public static Properties h2FileProps(String filePath) {
        Properties p = new Properties();
        p.setProperty("db.driver", "org.h2.Driver");
        p.setProperty("db.url", "jdbc:h2:file:" + filePath + ";AUTO_SERVER=TRUE");
        p.setProperty("db.username", "sa");
        p.setProperty("db.password", "");
        return p;
    }

    /**
     * Convenience: build properties for an in-memory H2 database (tests).
     */
    public static Properties h2MemProps(String name) {
        Properties p = new Properties();
        p.setProperty("db.driver", "org.h2.Driver");
        p.setProperty("db.url", "jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1");
        p.setProperty("db.username", "sa");
        p.setProperty("db.password", "");
        return p;
    }
}
