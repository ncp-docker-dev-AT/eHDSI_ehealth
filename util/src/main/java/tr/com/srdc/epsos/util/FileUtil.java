package tr.com.srdc.epsos.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
    private static final String LOG_FMT_MESSAGE_IOEXCEPTION = "IOException: {}";

    private FileUtil() {
    }

    /**
     * Constructs a new file with the given content and filePath. If a file with
     * the same name already exists, simply overwrites the content.
     *
     * @param filePath
     * @param content  : expressed as byte[]
     * @throws IOException : [approved by gunes] when the file cannot be
     *                     created. possible causes: 1) invalid filePath, 2) already existing file
     *                     cannot be deleted due to read-write locks
     * @author Gunes
     */
    public static void constructNewFile(String filePath, byte[] content) throws IOException {

        File file = new File(filePath);
        file.mkdirs();
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content);
        } catch (IOException e) {
            LOGGER.error(LOG_FMT_MESSAGE_IOEXCEPTION, e.getMessage(), e);
        }
    }

    private static byte[] getBytes(File file) throws IOException {

        try (InputStream is = new FileInputStream(file)) {

            // Get the size of the file
            long length = file.length();
            if (length > Integer.MAX_VALUE) {
                LOGGER.error("File is too large to process");
                return new byte[0];
            }
            // Create the byte array to hold the data
            byte[] bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead;
            while ((offset < bytes.length) && ((numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)) {

                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }
            is.close();
            return bytes;
        }
    }

    public static byte[] getBytesFromFile(String fileURI) throws IOException {
        File file = new File(fileURI);
        return getBytes(file);
    }

    public static byte[] readFromURI(URI uri) throws IOException {

        if (uri.toString().contains("http:")) {
            URL url = uri.toURL();
            URLConnection urlConnection = url.openConnection();
            int length = urlConnection.getContentLength();
            LOGGER.info("length of content in URL = '{}'", length);
            if (length > -1) {
                byte[] pureContent = new byte[length];
                DataInputStream dis = new DataInputStream(urlConnection.getInputStream());
                dis.readFully(pureContent, 0, length);
                dis.close();

                return pureContent;
            } else {
                throw new IOException("Unable to determine the content-length of the document pointed at " + url.toString());
            }
        } else {
            String file = readWholeFile(uri);
            if (file == null) {
                throw new IllegalArgumentException("Content of the file is null");
            }
            return file.getBytes(StandardCharsets.UTF_8);
        }
    }

    public static List<String> readFileLines(String filePath) throws IOException {

        try (FileInputStream fis = new FileInputStream(filePath)) {
            BufferedReader buf;
            List<String> rules = new ArrayList<>();

            InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
            buf = new BufferedReader(inputStreamReader);

            String temp;
            while ((temp = buf.readLine()) != null) {
                rules.add(temp);
            }
            buf.close();

            return rules;
        } catch (IOException e) {
            LOGGER.error(LOG_FMT_MESSAGE_IOEXCEPTION, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public static String readWholeFile(File file) throws IOException {

        return readInpustream(file, StandardCharsets.UTF_8);
    }

    public static String readWholeFile(String filePath) {

        return readInpustream(new File(filePath), StandardCharsets.UTF_8);
    }

    public static String readWholeFile(String filePath, String encoding) throws IOException {

        return readInpustream(new File(filePath), Charset.forName(encoding));
    }

    private static String readWholeFile(URI uri) {

        return readInpustream(new File(uri), StandardCharsets.UTF_8);
    }

    public static void writeToFile(File file, String content) {


        try (FileOutputStream fos = new FileOutputStream(file)) {

            OutputStreamWriter outStreamWriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            BufferedWriter bufferedWriter = new BufferedWriter(outStreamWriter);
            bufferedWriter.write(content);
            bufferedWriter.flush();
            bufferedWriter.close();

        } catch (IOException e) {
            LOGGER.error(LOG_FMT_MESSAGE_IOEXCEPTION, e.getMessage(), e);
        }
    }

    private static String readInpustream(File file, Charset charset) {

        try (FileInputStream fis = new FileInputStream(file)) {
            BufferedReader buf;
            StringBuilder rules = new StringBuilder();

            InputStreamReader inputStreamReader = new InputStreamReader(fis, charset);
            buf = new BufferedReader(inputStreamReader);
            String temp;
            while ((temp = buf.readLine()) != null) {
                rules.append(temp).append("\n");
            }
            buf.close();
            return rules.toString();
        } catch (IOException e) {
            LOGGER.error(LOG_FMT_MESSAGE_IOEXCEPTION, e.getMessage(), e);
            return null;
        }
    }
}
