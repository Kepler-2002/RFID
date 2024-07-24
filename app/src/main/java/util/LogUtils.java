package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;


public class LogUtils {
    private static BufferedWriter writer;

    private static final String LOG_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "RFIDReaderAppLogs";


    public static void saveLog(String log) {
        String fileName = getLogFileName();
        String logFilePath = LOG_DIR + File.separator + fileName;

        try {
            File file = new File(logFilePath);
            file.getParentFile().mkdirs();

            if (writer == null) {
                writer = new BufferedWriter(new FileWriter(file, true));
            }

            writer.write(log);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getLogFileName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate = new Date();
        return dateFormat.format(currentDate) + ".txt";
    }

    public static void closeWriter() {
        try {
            if (writer != null) {
                writer.close();
                writer = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}