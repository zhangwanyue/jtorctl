package net.freehaven.tor.control.serverAndClientExample.Utils;

import com.sun.istack.internal.Nullable;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.Socket;

/**
 * Created by vera on 18-11-5.
 */
public class IoUtils {
    public static byte[] read(File f) throws IOException {
        byte[] b = new byte[(int) f.length()];
        FileInputStream in = new FileInputStream(f);
        int offset = 0;
        while (offset < b.length) {
            int read = in.read(b, offset, b.length - offset);
            if (read == -1) throw new EOFException();
            offset += read;
        }
        return b;
    }

    public static String convert(InputStream inputStream){
        String result = "";
        try {
            result = IOUtils.toString(inputStream, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void read(InputStream in, byte[] b) throws IOException {
        int offset = 0;
        while (offset < b.length) {
            int read = in.read(b, offset, b.length - offset);
            if (read == -1) throw new EOFException();
            offset += read;
        }
    }

    // Workaround for a bug in Android 7, see
    // https://android-review.googlesource.com/#/c/271775/
    public static InputStream getInputStream(Socket s) throws IOException {
        try {
            return s.getInputStream();
        } catch (NullPointerException e) {
            throw new IOException(e);
        }
    }

    // Workaround for a bug in Android 7, see
    // https://android-review.googlesource.com/#/c/271775/
    public static OutputStream getOutputStream(Socket s) throws IOException {
        try {
            return s.getOutputStream();
        } catch (NullPointerException e) {
            throw new IOException(e);
        }
    }

    public static void tryToClose(@Nullable Closeable c) {
        try {
            if (c != null) c.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
