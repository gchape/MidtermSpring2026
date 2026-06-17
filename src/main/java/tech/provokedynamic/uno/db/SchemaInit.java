package tech.provokedynamic.uno.db;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * Runs db/schema.sql against the configured database.
 * Safe to call on every startup — all statements use CREATE IF NOT EXISTS.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SchemaInit {

    public static void run() {
        try (var sqlSession = Database.factory().openSession();
             var conn = sqlSession.getConnection()) {
            ScriptRunner runner = new ScriptRunner(conn);
            runner.setLogWriter(null);           // suppress ScriptRunner output
            runner.setErrorLogWriter(new PrintWriter(System.err));
            runner.runScript(new InputStreamReader(
                    Resources.getResourceAsStream("db/schema.sql")));
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Schema init failed", e);
        }
    }
}
