package ua.com.fielden.platform.cypher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import org.junit.Test;

/**
 * Asymmetric key generator test case.
 * 
 * @author TG Team
 * 
 */
public class AsymmetricKeyGeneratorTest {

    /**
     * The original and restored keys should be the same.
     */
    @Test
    public void test_that_public_and_private_key_restorations_work_correctly() {
	AsymmetricKeyGenerator gen = null;
	try {
	    gen = new AsymmetricKeyGenerator(1024);
	} catch (final NoSuchAlgorithmException e) {
	    fail("AsymmetricKeyGenerator failed to be created");
	}

	try {
	    final PrivateKey privateKey = AsymmetricKeyGenerator.restorePrivateKey(gen.getStrPrivateKey());
	    assertEquals("Private key was not restored correctly.", gen.getStrPrivateKey(), HexString.bufferToHex(privateKey.getEncoded()));
	} catch (final NoSuchAlgorithmException e) {
	    fail("AsymmetricKeyGenerator.restorePrivateKey did not recognise the algorithm.");
	} catch (final InvalidKeySpecException e) {
	    fail("AsymmetricKeyGenerator.restorePrivateKey did not recognise the key spec.");
	}

	try {
	    final PublicKey publicKey = AsymmetricKeyGenerator.restorePublicKey(gen.getStrPublicKey());
	    assertEquals("Public key was not restored correctly.", gen.getStrPublicKey(), HexString.bufferToHex(publicKey.getEncoded()));
	} catch (final NoSuchAlgorithmException e) {
	    fail("AsymmetricKeyGenerator.restorePublicKey did not recognise the algorithm.");
	} catch (final InvalidKeySpecException e) {
	    fail("AsymmetricKeyGenerator.restorePublicKey did not recognise the key spec.");
	}
    }
}
