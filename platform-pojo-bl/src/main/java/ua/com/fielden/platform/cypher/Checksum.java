package ua.com.fielden.platform.cypher;

import ua.com.fielden.platform.cypher.exceptions.ChecksumException;
import ua.com.fielden.platform.utils.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import static ua.com.fielden.platform.utils.Pair.pair;

/**
 * A utility class to calculate a checksum for an input stream and potentially other types of data.
 * 
 * @author TG Team
 * 
 */
public class Checksum {

    private Checksum() {}

    /**
    /**
     * Calculates a SHA256 code and the size (number of bytes) for the content of the input stream {@code is}.
     *
     * @param is
     * @return
     * @throws ChecksumException
     */
    public static Pair<String, Long> sha256(final InputStream is) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA256");
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
            throw new ChecksumException("Exception occurred while calculating SHA256 for an input stream.", ex);
        }
    }



    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    /// Calculates a SHA256 code for the passed in byte array.
    ///
    public static String sha256(final byte[] dataBytes) {
        return sha256(dataBytes, EMPTY_BYTE_ARRAY);
    }

    /// Calculates a SHA256 code for the passed in byte array.
    ///
    public static String sha256(final byte[] bytes0, final byte[]... bytesRest) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA256");
            md.update(bytes0);
            for (final var bytes : bytesRest) {
                md.update(bytes);
            }
            final byte[] mdbytes = md.digest();
            return HexString.bufferToHex(mdbytes, 0, mdbytes.length);
        } catch (final Exception ex) {
            throw new ChecksumException("Exception occurred while calculating SHA256 for a byte array.", ex);
        }
    }

    /**
     * Takes an existing file or a directory and calculates checksum for that file or all files in the directory (excluding subdirectories).
     * Returns a map of filename/checksum/file size entries, where filename is a key and the checksum/file-size pair is a value represented by a {@link Pair}.
     *
     * @param fileOrDirectory
     * @return
     */
    public static Map<String, Pair<String, Long>> sha256(final File fileOrDirectory) {
        if (!fileOrDirectory.exists()) {
            throw new IllegalArgumentException("File or directory should exist.");
        }

        try {
            final Map<String, Pair<String, Long>> result = new HashMap<>();
            if (fileOrDirectory.isDirectory()) {
                for (final File file : fileOrDirectory.listFiles()) {
                    if (file.isFile()) {
                        result.put(file.getName(), sha256(new FileInputStream(file)));
                    }
                }
            } else {
                result.put(fileOrDirectory.getName(), sha256(new FileInputStream(fileOrDirectory)));
            }

            return result;
        } catch (final Exception ex) {
            throw new ChecksumException("Exception occurred while calculating SHA256 for a file or directory.", ex);
        }
    }

}
