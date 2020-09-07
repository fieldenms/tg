package ua.com.fielden.platform.cypher;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 *
 * A generator of cryptographically random series identifiers for user session, and 2-factor authentication pin codes.
 * <p>
 * It also provides functions for generating a secrete key to produce HMAC-SHA1 hash codes.
 * This is should be unique for each specific application, and should be changed from time to time, which would lead to strogner security, but also the need to
 * user to re-authenticate their sessions by logging in explicitly.
 *
 * @author TG Team
 *
 */
public final class SessionIdentifierGenerator {
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final Logger LOGGER = Logger.getLogger(SessionIdentifierGenerator.class);
    private SecureRandom random = new SecureRandom();

    /**
     * Generates cryptographically random series identifier.
     *
     * @return
     */
    public String nextSessionId() {
        return new BigInteger(128, random).toString(32);
    }

    /**
     * Generates cryptographically random pin codes.
     *
     * @return
     */
    public String nextPin() {
        return new BigInteger(32, random).toString(32);
    }
    
    /**
     * Generates cryptographically random salt that can be used with various hashing algorithms to strengthen the hashing result.
     *
     * @return
     */
    public String genSalt() {
        return new BigInteger(128, random).toString(32);
    }

    /**
     * Generates a 4096 bit key using the HMAC-SHA256 algorithm.
     *
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String genHmacSha256Key() throws NoSuchAlgorithmException {
        // Generate a key for the HMAC-SHA1 keyed-hashing algorithm
        final KeyGenerator keyGen = KeyGenerator.getInstance(HMAC_SHA256_ALGORITHM);
        keyGen.init(4096);
        final SecretKey key = keyGen.generateKey();
        return HexString.bufferToHex(key.getEncoded());
    }

    /**
     * Calculates a hash code for a given data using algorithm HMAC-SHA256 with the provided key.
     *
     * @param data
     * @param key
     * @return
     * @throws SignatureException
     */
    public String calculateRFC2104HMAC(final String data, final String key) throws SignatureException {
        final String result;
        try {

            // get an hmac_sha1 key from the raw key bytes
            final SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA256_ALGORITHM);

            // get an hmac_sha1 Mac instance and initialize with the signing key
            final Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
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

    /**
     * Calculates a hash code for a given data using algorithm HMAC-SHA1 with the provided key.
     * Good <a href="http://security.stackexchange.com/questions/41617/do-salts-have-to-be-random-or-just-unique-and-unknown">read</a> about salt.
     * Hashing algorithm used is <a href="https://en.wikipedia.org/wiki/PBKDF2">PBKDF2</a>.
     * <a href="https://adambard.com/blog/3-wrong-ways-to-store-a-password/">This</a> article was used to implement this method.
     * Also, refer <a href="https://crackstation.net/hashing-security.htm">this</a>.
     * 
     * @param data
     * @param salt
     * @return
     * @throws SignatureException
     */
    public String calculatePBKDF2WithHmacSHA1(final String data, final String salt) throws SignatureException {
        final int ITERATIONS = 100000;
        final int KEY_LENGTH = 192; // bits

        final String result;
        try {
            final char[] passwordChars = data.toCharArray();
            final PBEKeySpec spec = new PBEKeySpec(
                    passwordChars,
                    salt.getBytes(),
                    ITERATIONS,
                    KEY_LENGTH
                );
            
            final SecretKeyFactory signingKey = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            final byte[] hashedPassword = signingKey.generateSecret(spec).getEncoded();
            
            // base64-encode the hmac
            result = HexString.bufferToHex(hashedPassword);
        } catch (final Exception ex) {
            final String msg = "Failed to generate HMAC.";
            LOGGER.error(msg, ex);
            throw new SignatureException(msg, ex);
        }
        return result;
    }

    public static void main(final String[] args) throws Exception {
        final SessionIdentifierGenerator gen = new SessionIdentifierGenerator();

        final DateTime start = new DateTime();
        int count = 0;

        final Set<String> duplicates = new HashSet<>(100);
        //System.out.println(String.format("HMAC-SHA1 key: %s", getnHmacSha1Key()));
        final String key = "354B925D34587CF6729F1D3E20B9FA9087885C1E1E8553EA703AA9C70774658FB59B3A1B523FA77688884CEB6DBAFF52E5D7F54B58EEAF485AFFD82260B5694B21F9D6462A6359FB7C242ABD360935B5BD8565C3AB698E0AD4A86AFB91D4EF16F6FC9CBFE1473EDCE3BE7AE8D3C09C86B0BEA3AA2BB03C3FE7A33B2EAFD2E3593DE6776CA6DB48E4E0B12A487AFCE12495FA0721C5DF0B0CF200778A7EA2F848EE67BB88F18882C78989F8DEBBEDDAC08F9613F9E583231E17E39247E574326A905D3C45AD1580B90226E365ED6558615850A2CCE0FE9C0FDA60039D9765E68347EDCC4DA6A79D3DE6E3C845EF9704B1B5D33607A9CED96127F6E12AC4119960";

        for (int index = 0; index < 10000; index++) {

            final Set<String> all = new HashSet<>(10000);
            for (int times = 0; times < 10000; times++) {

                //final String next = calculateRFC2104HMAC(gen.nextSessionId(), key);
                final String next = gen.nextSessionId();
                //System.out.println(next);
                if (all.contains(next)) {
                    duplicates.add(next);
                } else {
                    all.add(next);
                }
            }

            count = count + (1000 - all.size());

        }

        System.out.println("duplicates: " + duplicates.size() / 10000);
        System.out.println("total duplicates: " + duplicates.size());
        System.out.println(Arrays.toString(duplicates.toArray()));

        final DateTime end = new DateTime();
        final Period period = new Period(start, end);

        final PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendMillis().appendSuffix(" millis ago\n")
                .appendSeconds().appendSuffix(" seconds ago\n")
                .appendMinutes().appendSuffix(" minutes ago\n")
                .appendHours().appendSuffix(" hours ago\n")
                .appendDays().appendSuffix(" days ago\n")
                .appendWeeks().appendSuffix(" weeks ago\n")
                .appendMonths().appendSuffix(" months ago\n")
                .appendYears().appendSuffix(" years ago\n")
                .printZeroNever()
                .toFormatter();

        final String elapsed = formatter.print(period);
        System.out.println(elapsed);
    }
}