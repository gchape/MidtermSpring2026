package tech.provokedynamic.uno;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CliArgs#parse(String[])}.
 * <p>
 * Pure logic — no I/O, no game state. Verifies defaults, each individual
 * flag, combined flags, and the --help short-circuit.
 */
class CliArgsTest {

    @Test
    void defaultsWithNoArgs() {
        CliArgs args = CliArgs.parse(new String[]{});

        assertNotNull(args);
        assertEquals(3, args.bots());
        assertEquals(500, args.target());
        assertFalse(args.human());
        assertFalse(args.quiet());
        assertFalse(args.noDb());
        assertFalse(args.report());
        assertEquals("./data/uno", args.dbPath());
    }

    @Test
    void botsFlag() {
        CliArgs args = CliArgs.parse(new String[]{"--bots", "2"});
        assertNotNull(args);
        assertEquals(2, args.bots());
    }

    @Test
    void targetFlag() {
        CliArgs args = CliArgs.parse(new String[]{"--target", "200"});
        assertNotNull(args);
        assertEquals(200, args.target());
    }

    @Test
    void humanFlag() {
        CliArgs args = CliArgs.parse(new String[]{"--human"});
        assertNotNull(args);
        assertTrue(args.human());
    }

    @Test
    void quietFlag() {
        CliArgs args = CliArgs.parse(new String[]{"--quiet"});
        assertNotNull(args);
        assertTrue(args.quiet());
    }

    @Test
    void noDbFlag() {
        CliArgs args = CliArgs.parse(new String[]{"--no-db"});
        assertNotNull(args);
        assertTrue(args.noDb());
    }

    @Test
    void reportFlag() {
        CliArgs args = CliArgs.parse(new String[]{"--report"});
        assertNotNull(args);
        assertTrue(args.report());
    }

    @Test
    void dbPathFlag() {
        CliArgs args = CliArgs.parse(new String[]{"--db-path", "/tmp/mydb"});
        assertNotNull(args);
        assertEquals("/tmp/mydb", args.dbPath());
    }

    @Test
    void seedFlag() {
        CliArgs args = CliArgs.parse(new String[]{"--seed", "42"});
        assertNotNull(args);
        assertEquals(42L, args.seed());
    }

    @Test
    void helpReturnsNull() {
        CliArgs args = CliArgs.parse(new String[]{"--help"});
        assertNull(args, "--help should return null to signal early exit");
    }

    @Test
    void multipleFlagsCombined() {
        CliArgs args = CliArgs.parse(new String[]{
                "--bots", "2", "--target", "300", "--quiet", "--no-db", "--seed", "99"
        });

        assertNotNull(args);
        assertEquals(2, args.bots());
        assertEquals(300, args.target());
        assertTrue(args.quiet());
        assertTrue(args.noDb());
        assertEquals(99L, args.seed());
        assertFalse(args.human());
    }

    @Test
    void seedIsRandomByDefaultNotZero() {
        // Two parses without --seed should produce different seeds (with overwhelming probability)
        CliArgs a = CliArgs.parse(new String[]{});
        CliArgs b = CliArgs.parse(new String[]{});
        assertNotNull(a);
        assertNotNull(b);
        // Not asserting inequality — System.currentTimeMillis could collide in theory —
        // but we do assert the seed is not the suspicious default of 0
        assertNotEquals(0L, a.seed(), "Default seed should be time-based, not zero");
    }
}
