package com.kingpixel.cobblests.utils;

import com.kingpixel.cobblests.CobbleSTS;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SalesLogger {
    private static File LOG_DIR;
    private static LocalDateTime DATE;
    private static PrintWriter WRITER;
    private static final DateTimeFormatter FILE_FORMATTER =  DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MSG_FORMATTER =  DateTimeFormatter.ofPattern("HH:mm:ss");
    public static void init(Path gameDir) {
        File logDir = gameDir.resolve(Path.of(CobbleSTS.MOD_ID)).toFile();
        if(!logDir.exists()) {
            if(logDir.mkdirs()) {
                CobbleSTS.LOGGER.info("Created log directory");
            } else {
                CobbleSTS.LOGGER.error("Failed to create log directory");
            }
        } else {
            CobbleSTS.LOGGER.info("Found log directory");
        }

        LOG_DIR = logDir;

        DATE = LocalDateTime.MIN;
        updateDate();
    }

    private static void updateDate() {
        try {
            LocalDateTime now = LocalDateTime.now();
            if (DATE.getYear() == now.getYear() && DATE.getMonth() == now.getMonth() && DATE.getDayOfMonth() == now.getDayOfMonth()) {
                return;
            }

            DATE = now;
            File currentFile = new File(LOG_DIR, DATE.format(FILE_FORMATTER) + ".txt");
            if (!currentFile.exists()) {
                if (!currentFile.createNewFile()) {
                    CobbleSTS.LOGGER.error("Failed to create the log file - it already exists (but also doesn't?)");
                }
            }

            if(WRITER != null) {
                WRITER.close();
            }

            WRITER = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(currentFile, true))), true);
            CobbleSTS.LOGGER.info("Opened new log file: " + currentFile);
        } catch (Exception e) {
            CobbleSTS.LOGGER.error("Error while updating the date", e);
        }
    }

    public static void log(String message) {
        updateDate();
        WRITER.println("[" + LocalDateTime.now().format(MSG_FORMATTER) + "] " + message);
    }
}
