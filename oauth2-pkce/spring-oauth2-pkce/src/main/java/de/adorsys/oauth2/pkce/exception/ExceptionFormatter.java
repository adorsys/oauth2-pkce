package de.adorsys.oauth2.pkce.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;

public class ExceptionFormatter {

    public static String format(String uuid, Throwable e) {
        String message = MessageFormat.format(
                "uuid: [{0}], exception: [{1}]",
                uuid,
                formatException(e)
        );

        message = message.replace("\n", "\\n");

        return message;
    }

    private static String formatException(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        e.printStackTrace(pw);

        return sw.toString();
    }

    private ExceptionFormatter() {
        throw new UnsupportedOperationException();
    }
}
