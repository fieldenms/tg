package ua.com.fielden.platform.cypher;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 *
 * A generator of cryptographically random series identifiers for user session, and 2-factor authentication pin codes.
 *
 * @author TG Team
 *
 */
public final class SessionIdentifierGenerator {
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
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
     * <p>
     * For 1000 numbers produces on average 10 duplicates, with close to 0 average duplicates for 100 numbers, which is fully acceptable.
     *
     * @return
     */
    public String nextPin() {
        return new BigInteger(32, random).toString(32);
    }

    /**
     * Generates a 2048 bit key using the HMAC-SHA1 algorithm.
     *
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String genHmacSha1Key() throws NoSuchAlgorithmException {
        // Generate a key for the HMAC-SHA1 keyed-hashing algorithm
        final KeyGenerator keyGen = KeyGenerator.getInstance(HMAC_SHA1_ALGORITHM);
        keyGen.init(2048);
        final SecretKey key = keyGen.generateKey();
        return HexString.bufferToHex(key.getEncoded());
    }

    /**
     * Calculates a hash code for a given data using algorithm HMAC-SHA1 with the provided key.
     *
     * @param data
     * @param key
     * @return
     * @throws SignatureException
     */
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