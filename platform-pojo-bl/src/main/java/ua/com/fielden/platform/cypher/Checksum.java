package ua.com.fielden.platform.cypher;

import static ua.com.fielden.platform.utils.Pair.pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.cypher.exceptions.ChecksumException;
import ua.com.fielden.platform.utils.Pair;

/**
 * A utility class to calculate a checksum for an input stream and potentially other types of data.
 * 
 * @author TG Team
 * 
 */
public class Checksum {

    private Checksum() {}

    /**
     * Calculates a SHA1 code and the size (number of bytes) for a content of the input stream.
     * 
     * @param is
     * @return
     * @throws Exception
     */
    public static Pair<String, Long> sha1(final InputStream is) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA1");
            final byte[] dataBytes = new byte[1024];
            int nread = 0;
            long size = 0;
            while ((nread = is.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
                size += nread;
            }

            final byte[] mdbytes = md.digest();
            return pair(HexString.bufferToHex(mdbytes, 0, mdbytes.length), size);
        } catch (final Exception ex) {
            throw new ChecksumException("Exception occurred while calculating SHA1 for an input stream.", ex);
        }
    }

    /**
     * Calculates a SHA1 code for the passed in byte array.
     * 
     * @param dataBytes
     * @return
     */
    public static String sha1(final byte[] dataBytes) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(dataBytes);
            final byte[] mdbytes = md.digest();
            return HexString.bufferToHex(mdbytes, 0, mdbytes.length);
        } catch (final Exception ex) {
            throw new ChecksumException("Exception occurred while calculating SHA1 for a byte array.", ex);
        }
    }

    /**
     * Calculates a SHA1 code for the passed in byte array.
     * 
     * @param dataBytes
     * @return
     */
    public static String sha256(final byte[] dataBytes) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA256");
            md.update(dataBytes);
            final byte[] mdbytes = md.digest();
            return HexString.bufferToHex(mdbytes, 0, mdbytes.length);
        } catch (final Exception ex) {
            throw new ChecksumException("Exception occurred while calculating SHA256 for a byte array.", ex);
        }
    }

    /**
     * Takes an existing file or a directory and calculates checksum for that file or all files in a directory (excluding sub-directories) returning a map of file
     * name/checksum/file size entries, where file name is a key and checksum/file size is represented by a {@link Pair} class.
     * 
     * @param fileOrDirectory
     * @return
     */
    public static Map<String, Pair<String, Long>> sha1(final File fileOrDirectory) {
        if (!fileOrDirectory.exists()) {
            throw new IllegalArgumentException("File or directory should exist.");
        }

        try {
            final Map<String, Pair<String, Long>> result = new HashMap<>();
            if (fileOrDirectory.isDirectory()) {
                for (final File file : fileOrDirectory.listFiles()) {
                    if (file.isFile()) {
                        result.put(file.getName(), sha1(new FileInputStream(file)));
                    }
                }
            } else {
                result.put(fileOrDirectory.getName(), sha1(new FileInputStream(fileOrDirectory)));
            }

            return result;
        } catch (final Exception ex) {
            throw new ChecksumException("Exception occurred while calculating SHA1 for a file or directory.", ex);
        }
    }

    /**
     * Calculates a SHA1 code for string value.
     * 
     * @param value
     * @return
     */
    public static String sha1(final String value) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA1");
            final byte[] data = value.getBytes();
            md.update(data, 0, data.length);

            final byte[] mdbytes = md.digest();

            return HexString.bufferToHex(mdbytes, 0, mdbytes.length);
        } catch (final Exception ex) {
            throw new ChecksumException("Exception occurred while calculating SHA1 for a string.", ex);
        }
    }
}
