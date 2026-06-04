package net.alek.spawnlistgenerator.core;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";

    private static final Object lock = new Object();

    public static String getCallerInfo() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        StackTraceElement element = stackTrace[3];
        String className = element.getClassName();
        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        int lineNumber = element.getLineNumber();
        return simpleClassName + ":" + lineNumber;
    }

    private static void logWriter(String toWrite, String type) {
        synchronized (lock) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String caller = getCallerInfo();
            String logMessage = timestamp + " " + type + "  " + caller + " - " + toWrite;

            String coloredMessage;
            if (type.equals("ERROR")) {
                coloredMessage = RED + logMessage + RESET;
            } else if(type.equals("WARNING")) {
                coloredMessage = YELLOW + logMessage + RESET;
            } else {
                coloredMessage = logMessage;
            }

            System.out.println(coloredMessage);
            System.out.flush();
        }
    }

    public static class Log {
        public static void info(String message) {
            logWriter(message, "INFO");
        }

        public static void warn(String message) {
            logWriter(message, "WARNING");
        }

        public static void error(String message) {
            logWriter(message, "ERROR");
        }
    }
}