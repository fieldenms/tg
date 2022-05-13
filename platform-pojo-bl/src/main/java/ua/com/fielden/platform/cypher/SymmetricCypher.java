package ua.com.fielden.platform.cypher;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Implements symmetric encryption for password-based encryption of string values, using the {@code AES/GCM/NoPadding} algorithm.
 * The main advantage of symmetric over asymmetric encryption, is the speed of encryption, which could be decisive factor.
 * <p>
 * Never use symmetric encryption for encrypting user passwords or similar security strings. Carefully consider if the encrypted values need to be ever decrypted.
 * Always use one way hashing instead of symmetric encryption in such cases. For example, user passwords do not need to be decrypted – it is sufficient to have a hashed value in order to match it to a password that user presents upon login (naturally, such password needs to be hashed before comparison).
 * <p>
 * There are 3 important peaces that need to be provided for encoding and decoding:
 * <ul>
 * <li>{@code passwd} – a constructor argument, which represents a password/passphrase, which is used to generate a key that is then used for encryption/decryption.
 * <li>{@code salt} – a constructor argument, used to strengthen a password-based encryption (salted passwords).
 * <li>{@code nonce} – the second argument, passed to methods {@link #encrypt(String, byte[])} and {@link #decrypt(String, byte[])}, which is used for making {@link GCMParameterSpec} (more specifically, for Initialization Vector).
 * </ul>
 * <p>
 * The main reason for using {@code salt} is to prevent a rainbow table attack. For example, this would be critical is the same string value, which needs to be encoded, is provided by different users.
 * Without the use of different salt values, these values would be encoded to the same value. And if an adversary guesses one value, they would immediately have another value.
 * This is avoided when using different and random {@code salt} values when encoding those two identical strings, because the encoded results would be different.
 * <p>
 * Both {@code sault} and {@code nonce} values can be stored together with the encrypted value. This is necessary in order to decrypt the original value.
 *
 * @author TG Team
 *
 */
public class SymmetricCypher {
    private static final int GCM_TAG_LENGTH = 16;

    /** Initialisation for {@link SecureRandom()} can be slow. This is why it is best to keep an instance per thread. */
    private final ThreadLocal<SecureRandom> random = ThreadLocal.withInitial(() -> new SecureRandom());

    private final SecretKey aesKey;

    public SymmetricCypher(final String passwd, final String salt) throws Exception {
        this.aesKey = prepKeyFromPassword(passwd, salt);
    }

    /**
     * Generates a cryptographically strong nonce for AES/GCM, which is used of the Initialization Vector.
     * This method should be used to generate a nonce value for {@link #encrypt(String, byte[])} and {@link #decrypt(String, byte[])}.
     *
     * @return
     */
    public byte[] genNonceForGcm() {
        final byte[] nonce = new byte[12];
        random.get().nextBytes(nonce);
        return nonce;
    }

    /**
     * Encrypts {@code text}. Use method {@link #genNonceForGcm()} to generate a random {@code nonce}.
     * The generated values can be stored together with the encrypted text in order to decrypt it at later stage.
     *
     * @param text
     * @param nonce
     * @return
     * @throws Exception
     */
    public String encrypt(final String text, final byte[] nonce) throws Exception {
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce));
        final byte[] cipherText = cipher.doFinal(text.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(cipherText);
    }

    /**
     * Decrypts {@code encryptedText}. The same {@code nonce} as was used for encryption should be passed for decryption.
     *
     * @param encryptedText
     * @param nonce
     * @return
     * @throws Exception
     */
    public String decrypt(final String encryptedText, final byte[] nonce) throws Exception {
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce));
        final byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(plainText);
    }

    private SecretKey prepKeyFromPassword(final String key, final String salt) throws Exception {
        final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        final KeySpec spec = new PBEKeySpec(key.toCharArray(), salt.getBytes("UTF-8"), 65536, 256);
        final SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        return secret;
    }

    public static void main(String[] args) throws Exception {
        final String text = "some text that needs to be encrypted and later decrypted";
        final SymmetricCypher cypher = new SymmetricCypher("some strong password", "some salt");
        
        byte[] nonce = cypher.genNonceForGcm();
        final String encryptedText = cypher.encrypt(text, nonce);
        System.out.println(encryptedText);
        final String decryptedText = cypher.decrypt(encryptedText, nonce);
        System.out.println(decryptedText);
    }

}