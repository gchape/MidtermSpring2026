package tech.provokedynamic.uno;

/**
 * Immutable value object holding all parsed CLI arguments.
 * <p>
 * {@link #parse(String[])} returns {@code null} when {@code --help} is
 * requested so that {@code Main} can return immediately without playing.
 */
record CliArgs(
        int bots,
        int target,
        boolean human,
        boolean quiet,
        boolean noDb,
        boolean report,
        long seed,
        String dbPath
) {
    private static final int DEFAULT_BOTS = 3;
    private static final int DEFAULT_TARGET = 500;
    private static final String DEFAULT_DB_PATH = "./data/uno";

    /**
     * Parses {@code args} into a {@code CliArgs} instance.
     *
     * @return parsed args, or {@code null} if {@code --help} was requested
     */
    static CliArgs parse(String[] args) {
        int bots = DEFAULT_BOTS;
        int target = DEFAULT_TARGET;
        boolean human = false;
        boolean quiet = false;
        boolean noDb = false;
        boolean report = false;
        long seed = System.currentTimeMillis();
        String dbPath = DEFAULT_DB_PATH;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--bots" -> bots = Integer.parseInt(args[++i]);
                case "--target" -> target = Integer.parseInt(args[++i]);
                case "--human" -> human = true;
                case "--quiet" -> quiet = true;
                case "--seed" -> seed = Long.parseLong(args[++i]);
                case "--db-path" -> dbPath = args[++i];
                case "--no-db" -> noDb = true;
                case "--report" -> report = true;
                case "--help" -> {
                    IO.println("Usage: scripts/run.sh [--bots N] [--target N] [--human] [--quiet] " +
                            "[--seed N] [--db-path PATH] [--no-db] [--report]");
                    return null;
                }
            }
        }

        return new CliArgs(bots, target, human, quiet, noDb, report, seed, dbPath);
    }
}
