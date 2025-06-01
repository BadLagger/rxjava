package logger;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerConfig {

    static {
        Logger rootLogger = Logger.getLogger("");
    }

    public static void init(final Logger logger, Level level) {
        logger.setUseParentHandlers(false);
        Handler handler = new ConsoleHandler();
        handler.setFormatter(new OneLineFormatter());
        handler.setLevel(level);
        logger.addHandler(handler);
        logger.setLevel(level);
    }
}
