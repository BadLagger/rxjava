package logger;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class OneLineFormatter extends Formatter {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Override
    public String format(LogRecord record) {
        Date date = new Date(record.getMillis());
        String message = replaceParameters(record.getMessage(), record.getParameters());
        return sdf.format(date) + ": [" + record.getSourceClassName() + "." + record.getSourceMethodName() +
                "] [" + record.getLevel().toString() + "] " + message + "\n";
    }

    private String replaceParameters(String message, Object[] parameters) {
        if (parameters != null && parameters.length > 0) {
            return MessageFormat.format(message, parameters);
        }
        return message;
    }
}
