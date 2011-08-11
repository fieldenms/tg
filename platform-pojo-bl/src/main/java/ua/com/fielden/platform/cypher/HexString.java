package ua.com.fielden.platform.cypher;

/**
 * Contains utility functions for converting between hexadecimal strings and their equivalent byte buffers or String objects.
 * 
 * @author Trygve Isaacson (the original author)
 * @author 01es
 * 
 */
public class HexString {

    /**
     * Let's hide the default constructor for this static class.
     */
    private HexString() {
    }

    /**
     * Returns a string containing the hexadecimal representation of the input string. Each byte in the input string is converted to a two-digit hexadecimal value. Thus the
     * returned string is twice the length of the input string. The output hex characters are upper case. The conversion assumes the default charset; making the charset explicit
     * could be accomplished by just adding a parameter here and passing it through to getBytes().
     * 
     * @param s
     *            a string to convert to hex
     * @return the hex string version of the input string
     */
    public static String stringToHex(final String s) {
	final byte[] stringBytes = s.getBytes();

	return HexString.bufferToHex(stringBytes);
    }

    /**
     * Returns a string containing the hexadecimal representation of the input byte array. Each byte in the input array is converted to a two-digit hexadecimal value. Thus the
     * returned string is twice the length of the input byte array. The output hex characters are upper case.
     * 
     * @param buffer
     *            a buffer to convert to hex
     * @return the hex string version of the input buffer
     */
    public static String bufferToHex(final byte buffer[]) {
	return HexString.bufferToHex(buffer, 0, buffer.length);
    }

    /**
     * Returns a string containing the hexadecimal representation of the input byte array. Each byte in the input array is converted to a two-digit hexadecimal value. Thus the
     * returned string is twice the length of the specified amount of the input byte array. The output hex characters are upper case.
     * 
     * @param buffer
     *            a buffer to convert to hex
     * @param startOffset
     *            the offset of the first byte in the buffer to process
     * @param length
     *            the number of bytes in the buffer to process
     * @return the hex string version of the input buffer
     */
    public static String bufferToHex(final byte buffer[], final int startOffset, final int length) {
	final StringBuffer hexString = new StringBuffer(2 * length);
	final int endOffset = startOffset + length;

	for (int i = startOffset; i < endOffset; i++) {
	    HexString.appendHexPair(buffer[i], hexString);
	}

	return hexString.toString();
    }

    /**
     * Returns a string built from the byte values represented by the input hexadecimal string. That is, each pair of hexadecimal characters in the input string is decoded into the
     * byte value that they represent, and that byte value is appended to a string which is ultimately returned. This function doesn't care whether the hexadecimal characters are
     * upper or lower case, and it also can handle odd-length hex strings by assuming a leading zero digit. If any character in the input string is not a valid hexadecimal digit,
     * it throws a NumberFormatException, in keeping with the behavior of Java functions like Integer.parseInt(). The conversion assumes the default charset; making the charset
     * explicit could be accomplished by just adding a parameter here and passing it through to the String constructor.
     * 
     * @param hexString
     *            a string of hexadecimal characters
     * @return a String built from the bytes indicated by the input string
     * @throws NumberFormatException
     */
    public static String hexToString(final String hexString) throws NumberFormatException {
	final byte[] bytes = HexString.hexToBuffer(hexString);

	return new String(bytes);
    }

    /**
     * Returns a byte array built from the byte values represented by the input hexadecimal string. That is, each pair of hexadecimal characters in the input string is decoded into
     * the byte value that they represent, and that byte value is appended to a byte array which is ultimately returned. This function doesn't care whether the hexadecimal
     * characters are upper or lower case, and it also can handle odd-length hex strings by assuming a leading zero digit. If any character in the input string is not a valid
     * hexadecimal digit, it throws a NumberFormatException, in keeping with the behavior of Java functions like Integer.parseInt().
     * 
     * @param hexString
     *            a string of hexadecimal characters
     * @return a byte array built from the bytes indicated by the input string
     * @throws NumberFormatException
     */
    public static byte[] hexToBuffer(final String hexString) throws NumberFormatException {
	final int length = hexString.length();
	final byte[] buffer = new byte[(length + 1) / 2];
	boolean evenByte = true;
	byte nextByte = 0;
	int bufferOffset = 0;

	// If given an odd-length input string, there is an implicit
	// leading '0' that is not being given to us in the string.
	// In that case, act as if we had processed a '0' first.
	// It's sufficient to set evenByte to false, and leave nextChar
	// as zero which is what it would be if we handled a '0'.
	if ((length % 2) == 1) {
	    evenByte = false;
	}

	for (int i = 0; i < length; i++) {
	    final char c = hexString.charAt(i);
	    // A "nibble" is 4 bits: a decimal 0..15
	    final int nibble = determineNibble(c);

	    if (evenByte) {
		nextByte = (byte) (nibble << 4);
	    } else {
		nextByte += (byte) nibble;
		buffer[bufferOffset++] = nextByte;
	    }

	    evenByte = !evenByte;
	}

	return buffer;
    }

    /**
     * Determines a nibble. A "nibble" is 4 bits: a decimal 0..15.
     * 
     * @param c
     * @return
     */
    private static int determineNibble(final char c) {
	if ((c >= '0') && (c <= '9')) {
	    return c - '0';
	} else if ((c >= 'A') && (c <= 'F')) {
	    return c - 'A' + 0x0A;
	} else if ((c >= 'a') && (c <= 'f')) {
	    return c - 'a' + 0x0A;
	} else {
	    throw new NumberFormatException("Invalid hex digit '" + c + "'.");
	}
    }

    /**
     * Appends a hexadecimal representation of a particular char value to a string buffer. That is, two hexadecimal digits are appended to the string.
     * 
     * @param b
     *            a byte whose hex representation is to be obtained
     * @param hexString
     *            the string to append the hex digits to
     */
    private static void appendHexPair(final byte b, final StringBuffer hexString) {
	final char highNibble = kHexChars[(b & 0xF0) >> 4];
	final char lowNibble = kHexChars[b & 0x0F];

	hexString.append(highNibble);
	hexString.append(lowNibble);
    }

    private static final char kHexChars[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
}
