package ua.com.fielden.platform.cypher;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.utils.Pair;

/**
 * A utility class to calculate a checksum for an input stream and potentially other types of data.
 * 
 * @author TG Team
 * 
 */
public class Checksum {

    private Checksum() {
    }

    /**
     * Calculates a SHA1 code and the size (number of bytes) for a content of the input stream.
     * 
     * @param is
     * @return
     * @throws Exception
     */
    public static Pair<String, Long> sha1(final InputStream is) throws Exception {
	final MessageDigest md = MessageDigest.getInstance("SHA1");
	final byte[] dataBytes = new byte[1024];
	int nread = 0;
	long size = 0;
	while ((nread = is.read(dataBytes)) != -1) {
	    md.update(dataBytes, 0, nread);
	    size += nread;
	}

	final byte[] mdbytes = md.digest();

	return new Pair<String, Long>(HexString.bufferToHex(mdbytes, 0, mdbytes.length), size);
    }

    /**
     * Calculates a SHA1 code for the passed in byte array.
     * 
     * @param dataBytes
     * @return
     * @throws Exception
     */
    public static String sha1(final byte[] dataBytes) throws Exception {
	final MessageDigest md = MessageDigest.getInstance("SHA1");
	md.update(dataBytes);
	final byte[] mdbytes = md.digest();
	return HexString.bufferToHex(mdbytes, 0, mdbytes.length);
    }

    /**
     * Takes an existing file or a directory and calculates checksum for that file or all files in a directory (excluding sub-directories) returning a map of file
     * name/checksum/file size entries, where file name is a key and checksum/file size is represented by a {@link Pair} class.
     * 
     * @param fileOrDirectory
     * @return
     * @throws Exception
     */
    public static Map<String, Pair<String, Long>> sha1(final File fileOrDirectory) throws Exception {
	if (!fileOrDirectory.exists()) {
	    throw new IllegalArgumentException("File or directory should exist.");
	}

	final Map<String, Pair<String, Long>> result = new HashMap<String, Pair<String, Long>>();
	if (fileOrDirectory.isDirectory()) {
	    for (final File file : fileOrDirectory.listFiles()) {
		if (file.isFile()) {
		    result.put(file.getName(), Checksum.sha1(new FileInputStream(file)));
		}
	    }
	} else {
	    result.put(fileOrDirectory.getName(), Checksum.sha1(new FileInputStream(fileOrDirectory)));
	}

	return result;
    }

    /**
     * Calculates a SHA1 code for string value.
     * 
     * @param value
     * @return
     * @throws Exception
     */
    public static String sha1(final String value) throws Exception {
	final MessageDigest md = MessageDigest.getInstance("SHA1");
	final byte[] data = value.getBytes();
	md.update(data, 0, data.length);

	final byte[] mdbytes = md.digest();

	return HexString.bufferToHex(mdbytes, 0, mdbytes.length);
    }
}
