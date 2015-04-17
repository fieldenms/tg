package ua.com.fielden.platform.cypher;

import java.security.SignatureException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * A class for generation of private/public key pair for the RSA algorithm.
 * <p>
 * If the passed into the constructor key length is less than 512 (minimum allowed by RSA) then 512 is set without any user warning.
 *
 * @author TG Team
 *
 */
public class AsymmetricKeyGenerator {
    public static final String ALGORITHM = "RSA";
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private final int keyLength; // in bits
    private final String strPrivateKey;
    private final String strPublicKey;
    private final String formatPrivate;
    private final String formatPublic;
    private final Date genTime;

    public AsymmetricKeyGenerator(final int length) throws NoSuchAlgorithmException {
        keyLength = length <= 512 ? 512 : length;

        final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(getALGORITHM());

        keyGen.initialize(getKeyLength());
        final KeyPair keypair = keyGen.genKeyPair();
        final PrivateKey privateKey = keypair.getPrivate();
        final PublicKey publicKey = keypair.getPublic();
        // get the string representation of the public and private keys
        strPrivateKey = HexString.bufferToHex(privateKey.getEncoded());
        strPublicKey = HexString.bufferToHex(publicKey.getEncoded());
        // get the formats of the encoded bytes
        formatPrivate = privateKey.getFormat(); // PKCS#8
        formatPublic = publicKey.getFormat(); // X.509

        genTime = new Date();
    }

    public static PrivateKey restorePrivateKey(final String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // the bytes can be converted back to private key object
        final KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        final EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(HexString.hexToBuffer(key));
        return keyFactory.generatePrivate(privateKeySpec);
    }

    public static PublicKey restorePublicKey(final String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // the bytes can be converted back to public key object
        final KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        final EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(HexString.hexToBuffer(key));
        return keyFactory.generatePublic(publicKeySpec);
    }

    @Override
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("Algorithm: " + getALGORITHM() + " (" + getKeyLength() + " bits); Generated on " + getGenTime() + "\n");
        buffer.append("Private key information:\n");
        buffer.append("\tKey: " + getStrPrivateKey() + "\n");
        buffer.append("\tFormat: " + getFormatPrivate() + "\n");
        buffer.append("Public key information:\n");
        buffer.append("\tKey: " + getStrPublicKey() + "\n");
        buffer.append("\tFormat: " + getFormatPublic());

        return buffer.toString();
    }

    public String getALGORITHM() {
        return ALGORITHM;
    }

    public String getStrPrivateKey() {
        return strPrivateKey;
    }

    public String getStrPublicKey() {
        return strPublicKey;
    }

    public String getFormatPrivate() {
        return formatPrivate;
    }

    public String getFormatPublic() {
        return formatPublic;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public Date getGenTime() {
        return genTime;
    }

    public static String getnHmacSha1Key() throws NoSuchAlgorithmException {
        // Generate a key for the HMAC-SHA1 keyed-hashing algorithm
        final KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA1");
        keyGen.init(2048);
        final SecretKey key = keyGen.generateKey();
        return HexString.bufferToHex(key.getEncoded());
    }

    public static String calculateRFC2104HMAC(final String data, final String key) throws SignatureException {
        final String result;
        try {

            // get an hmac_sha1 key from the raw key bytes
            final SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);

            // get an hmac_sha1 Mac instance and initialize with the signing key
            final Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);

            // compute the hmac on input data bytes
            final byte[] rawHmac = mac.doFinal(data.getBytes());

            // base64-encode the hmac
            result = HexString.bufferToHex(rawHmac);

        } catch (final Exception e) {
            throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
        }
        return result;
    }

    public static void main(final String[] args) throws Exception {
        final AsymmetricKeyGenerator gen = new AsymmetricKeyGenerator(1024);
        System.out.println(gen);

        final PrivateKey privateKey = AsymmetricKeyGenerator.restorePrivateKey(gen.getStrPrivateKey());
        final PublicKey publicKey = AsymmetricKeyGenerator.restorePublicKey(gen.getStrPublicKey());
        // the original and restored keys should be the same
        System.out.println("\nIs private key restored correctly: " + HexString.bufferToHex(privateKey.getEncoded()).equals(gen.getStrPrivateKey()));
        System.out.println("Is public key restored correctly: " + HexString.bufferToHex(publicKey.getEncoded()).equals(gen.getStrPublicKey()));

        final String encryptedValue = new Cypher().encrypt("http://www.restlet.org", gen.getStrPrivateKey());
        System.out.println("http://www.restlet.org: " + encryptedValue);

        //System.out.println(String.format("HMAC-SHA1 key: %s", getnHmacSha1Key()));
        final String key = "354B925D34587CF6729F1D3E20B9FA9087885C1E1E8553EA703AA9C70774658FB59B3A1B523FA77688884CEB6DBAFF52E5D7F54B58EEAF485AFFD82260B5694B21F9D6462A6359FB7C242ABD360935B5BD8565C3AB698E0AD4A86AFB91D4EF16F6FC9CBFE1473EDCE3BE7AE8D3C09C86B0BEA3AA2BB03C3FE7A33B2EAFD2E3593DE6776CA6DB48E4E0B12A487AFCE12495FA0721C5DF0B0CF200778A7EA2F848EE67BB88F18882C78989F8DEBBEDDAC08F9613F9E583231E17E39247E574326A905D3C45AD1580B90226E365ED6558615850A2CCE0FE9C0FDA60039D9765E68347EDCC4DA6A79D3DE6E3C845EF9704B1B5D33607A9CED96127F6E12AC4119960";
        System.out.println(String.format("HMAC-SHA1 hash: %s", calculateRFC2104HMAC("my  data", key)));
        //AC205B354512667C096343FAED39252431809256
        //
    }
}
