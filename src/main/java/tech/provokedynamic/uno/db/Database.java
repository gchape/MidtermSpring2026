package tech.provokedynamic.uno.db;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.util.Properties;

/**
 * Bootstraps MyBatis from mybatis-config.xml.
 * Call {@link #init(Properties)} once at startup before any repository use.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Database {

    private static SqlSessionFactory FACTORY;

    public static void init(Properties props) {
        try {
            var config = Resources.getResourceAsStream("mybatis-config.xml");
            FACTORY = new SqlSessionFactoryBuilder().build(config, props);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialise MyBatis", e);
        }
    }

    public static SqlSessionFactory factory() {
        if (FACTORY == null) throw new IllegalStateException("Database.init() was not called");
        return FACTORY;
    }

    /**
     * H2 file-mode properties. Path should be without extension (H2 appends .mv.db).
     */
    public static Properties h2FileProps(String path) {
        Properties p = new Properties();
        p.setProperty("db.driver", "org.h2.Driver");
        p.setProperty("db.url", "jdbc:h2:file:" + path + ";AUTO_SERVER=TRUE");
        p.setProperty("db.username", "sa");
        p.setProperty("db.password", "");
        return p;
    }

    /**
     * H2 in-memory properties. Each unique name is a separate isolated database.
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
