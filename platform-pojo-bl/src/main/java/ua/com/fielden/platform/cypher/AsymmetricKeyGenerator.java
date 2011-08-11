package ua.com.fielden.platform.cypher;

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

    public static void main(final String[] args) throws Exception {
	final AsymmetricKeyGenerator gen = new AsymmetricKeyGenerator(512);
	System.out.println(gen);

	final PrivateKey privateKey = AsymmetricKeyGenerator.restorePrivateKey(gen.getStrPrivateKey());
	final PublicKey publicKey = AsymmetricKeyGenerator.restorePublicKey(gen.getStrPublicKey());
	// the original and restored keys should be the same
	System.out.println("\nIs private key restored correctly: " + HexString.bufferToHex(privateKey.getEncoded()).equals(gen.getStrPrivateKey()));
	System.out.println("Is public key restored correctly: " + HexString.bufferToHex(publicKey.getEncoded()).equals(gen.getStrPublicKey()));

	final String encryptedValue = new Cypher().encrypt("http://www.restlet.org", gen.getStrPrivateKey());
	System.out.println("http://www.restlet.org: " + encryptedValue);
    }
}
