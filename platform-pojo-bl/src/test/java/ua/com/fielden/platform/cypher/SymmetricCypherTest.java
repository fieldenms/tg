package ua.com.fielden.platform.cypher;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 * A test case for {@link SymmetricCypher}.
 * 
 * @author TG Team
 * 
 */
public class SymmetricCypherTest {
    private static final String password = "ljxGGmmiwD5KZSbMYOfSRaNmyTDfhL1cThbd8RUGlA";
    private static final String salt = "QnNCUWu19";
    private final byte[] nonce;
    
    private final SymmetricCypher cypher;

    public SymmetricCypherTest() throws Exception {
        cypher = new SymmetricCypher(password, salt);
        nonce = cypher.genNonceForGcm();
    }

    @Test
    public void short_text_is_encoded_and_decoded_correctely() throws Exception {
        final String textToEncode = "short text";
        final String encryptedText = cypher.encrypt(textToEncode, nonce);
        assertEquals(textToEncode, cypher.decrypt(encryptedText, nonce));
    }

    @Test
    public void long_text_is_encoded_and_decoded_correctely() throws Exception {
        final String textToEncode = StringUtils.repeat("very long text ", 500);
        final String encryptedText = cypher.encrypt(textToEncode, nonce);
        assertEquals(textToEncode, cypher.decrypt(encryptedText, nonce));
    }

}