package ua.com.fielden.platform.cypher;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;

/**
 * Provides convenient methods for encryption and decryption using RSA algorithm
 * 
 * @author TG Team
 * 
 */
public class Cypher {
    private final String algorithm = AsymmetricKeyGenerator.ALGORITHM;
    private final Cipher cipher;
    // the minimum key size for RSA is 512 bits, which can encode maximum of 53 bites of information
    private final int BLOCK_SIZE = 53;
    private final String DEFAULT_SEPARATOR = "/";

    public Cypher() throws Exception {
        cipher = Cipher.getInstance(algorithm);
    }

    /**
     * Encrypts any text value with the provided private key.
     * <p>
     * If the text is longer than can be encrypted then it is split in encryptable chunks, which are handled separately. The returned string combines these encrypted chunks using
     * symbol "|".
     * 
     * @param value
     * @param privateKey
     * @return
     * @throws Exception
     */
    public String encrypt(final String value, final String privateKey) throws Exception {
        final byte[] inputBytes = value.getBytes();
        if (inputBytes.length <= BLOCK_SIZE) {
            final PrivateKey key = AsymmetricKeyGenerator.restorePrivateKey(privateKey);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return HexString.bufferToHex(cipher.doFinal(inputBytes));
        } else {
            return encrypt(value, privateKey, DEFAULT_SEPARATOR);
        }

    }

    /**
     * Encrypts a long text with provided private key using passed separator to join encrypted parts.
     * 
     * @param value
     * @param privateKey
     * @return
     * @throws Exception
     */
    public String encrypt(final String text, final String privateKey, final String separator) throws Exception {
        final PrivateKey key = AsymmetricKeyGenerator.restorePrivateKey(privateKey);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        // split text into chunks of BLOCK_SIZE and encode them separately
        final List<String> encodedParts = new ArrayList<String>();
        final byte[] textAsBytes = text.getBytes();
        int fromIndex = 0;
        while (fromIndex < textAsBytes.length) {
            final int toIndex = fromIndex + ((fromIndex + BLOCK_SIZE < textAsBytes.length) ? BLOCK_SIZE : textAsBytes.length - fromIndex);
            final byte[] portion = Arrays.copyOfRange(textAsBytes, fromIndex, toIndex);
            encodedParts.add(HexString.bufferToHex(cipher.doFinal(portion)));
            fromIndex = toIndex;
        }

        // concatenate encoded parts into one string using provided separator
        final StringBuffer combined = new StringBuffer();
        for (int index = 0; index < encodedParts.size(); index++) {
            combined.append(encodedParts.get(index) + (index + 1 < encodedParts.size() ? DEFAULT_SEPARATOR : ""));
        }
        return combined.toString();
    }

    /**
     * Encrypts any integer value wit the provided public key.
     * 
     * @param numberOfLicences
     * @param privateKey
     * @return
     * @throws Exception
     */
    public String encrypt(final int numberOfLicences, final String privateKey) throws Exception {
        return encrypt(Integer.toString(numberOfLicences), privateKey);
    }

    /**
     * Decodes value using the provided private key.
     * 
     * @param encryptedValue
     * @param publicKey
     * @return
     * @throws Exception
     */
    public String decrypt(final String encryptedValue, final String publicKey) throws Exception {
        final PublicKey key = AsymmetricKeyGenerator.restorePublicKey(publicKey);
        cipher.init(Cipher.DECRYPT_MODE, key);

        if (encryptedValue.contains(DEFAULT_SEPARATOR)) {
            return decrypt(encryptedValue, publicKey, DEFAULT_SEPARATOR);
        } else {
            final byte[] recoveredBytes = cipher.doFinal(HexString.hexToBuffer(encryptedValue));
            final String value = new String(recoveredBytes);
            return value;
        }
    }

    /**
     * Decodes text, consisting of several parts concatenated by separator, using provided public key.
     * 
     * @param encryptedText
     * @param publicKey
     * @param separator
     * @return
     * @throws Exception
     */
    public String decrypt(final String encryptedText, final String publicKey, final String separator) throws Exception {
        final PublicKey key = AsymmetricKeyGenerator.restorePublicKey(publicKey);
        cipher.init(Cipher.DECRYPT_MODE, key);

        final String[] encodedTokenParts = encryptedText.split(separator);
        final StringBuffer decodedToken = new StringBuffer();
        for (final String encodedToken : encodedTokenParts) {
            decodedToken.append(decrypt(encodedToken, publicKey));
        }
        return decodedToken.toString();
    }

    /**
     * Decrypts value and converts it to integer using the provided private key
     * 
     * @param encryptedValue
     * @param publicKey
     * @return
     * @throws Exception
     */
    public int decryptInt(final String encryptedValue, final String publicKey) throws Exception {
        return Integer.valueOf(decrypt(encryptedValue, publicKey));
    }
}
