package ua.com.fielden.platform.cypher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Cypher test case.
 * 
 * @author TG Team
 * 
 */
public class CypherTest {
    private static final String privateKey = "30820154020100300D06092A864886F70D01010105000482013E3082013A020100024100A6011E18C0C863215BB57F18EFA4B6FFA5B1BA2E3711BC682C7645972460D71F85504D50DE43A36601903CE5400AB0243C39D68434938F16AC5E1C58DD08B6670203010001024036EC39BBF0D49BCFA69B07010610962740F7EB646CFDE63B0532E0556048D6036401AC48175BACDC32D5639915C6F41640FB21EB1D005D09C4694FA99EDC0521022100DD8421FE3095C62B4F38EE7B3845FED13B6124C6BF65965A389412572629F691022100BFD8BA5978AEBF410D59AC9FD3FC111286FCB791CF6325C6BE04020FCE1C0977022100CA629FC022D9A47E0B5AA3A0D6D034B92B7C5CE26D1A3E9D5D96038FB6219ED102200D9A6984449967784E61086B80D583C4638BF1DC45EF4AD36FCFCAF1A1F4BCFB022065E1253643375F7704A405FA2C8FEF4E5C43D9CA67D635FC02DB191ADF291FF1";
    private static final String publicKey = "305C300D06092A864886F70D0101010500034B003048024100A6011E18C0C863215BB57F18EFA4B6FFA5B1BA2E3711BC682C7645972460D71F85504D50DE43A36601903CE5400AB0243C39D68434938F16AC5E1C58DD08B6670203010001";
    private final Cypher cypher;

    public CypherTest() throws Exception {
	cypher = new Cypher();
    }

    @Test
    public void test_that_short_text_is_encoded_and_decoded_correctely() throws Exception {
	final String textToEncode = "short text"; // under 53 bytes
	assertTrue("Text to encode is too long for this test.", textToEncode.getBytes().length <= 53);
	final String encryptedText = cypher.encrypt(textToEncode, privateKey);
	assertEquals("Incorrectly decrypted short text.", textToEncode, cypher.decrypt(encryptedText, publicKey));
    }

    @Test
    public void test_that_long_text_is_encoded_and_decoded_correctely_using_default_separator() throws Exception {
	final String textToEncode = "long text long text long text long text long text long text long text long text long text"; // over 53 bytes
	assertTrue("Text to encode is too short for this test.", textToEncode.getBytes().length > 53);
	final String encryptedText = cypher.encrypt(textToEncode, privateKey);
	assertEquals("Incorrectly decrypted short text.", textToEncode, cypher.decrypt(encryptedText, publicKey));
    }

    @Test
    public void test_that_long_text_is_encoded_and_decoded_correctely_using_specified_separator() throws Exception {
	final String textToEncode = "long text long text long text long text long text long text long text long text long text"; // over 53 bytes
	assertTrue("Text to encode is too short for this test.", textToEncode.getBytes().length > 53);
	final String separator = "=";
	final String encryptedText = cypher.encrypt(textToEncode, privateKey, separator);
	assertEquals("Incorrectly decrypted short text.", textToEncode, cypher.decrypt(encryptedText, publicKey, separator));
    }

}
