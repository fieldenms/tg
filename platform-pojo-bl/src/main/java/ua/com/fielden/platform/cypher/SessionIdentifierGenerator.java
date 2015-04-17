package ua.com.fielden.platform.cypher;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public final class SessionIdentifierGenerator {
    private SecureRandom random = new SecureRandom();

    public String nextSessionId() {
        return new BigInteger(16, random).toString(32);
    }

    /**
     * For 1000 numbers produces on average 10 duplicates, with close to 0 average duplicates for 100 numbers, which is fully acceptable.
     *
     * @return
     */
    public BigInteger nextPin() {
        return new BigInteger(18, random);
    }

    public static void main(final String[] args) {
        final SessionIdentifierGenerator gen = new SessionIdentifierGenerator();
        System.out.println(gen.nextPin());

        final DateTime start = new DateTime();
        int count = 0;
        BigInteger min = new BigInteger("10000000000");
        BigInteger max = new BigInteger("0");

        final Set<BigInteger> duplicates = new HashSet<>(100);

        for (int index = 0; index < 100; index++) {

            final Set<BigInteger> all = new HashSet<>(10000);
            for (int times = 0; times < 1000; times++) {
                //final String value = gen.nextSessionId();
                BigInteger next = gen.nextPin();
                if (next.compareTo(new BigInteger("10000")) <= 0) {
                    next = next.add(new BigInteger("10000"));
                }
                if (all.contains(next)) {
                    duplicates.add(next);
                } else {
                    all.add(next);
                }
            }

            count = count + (1000 - all.size());
            final BigInteger lMin = all.stream().min(Comparator.naturalOrder()).get();
            if (min.compareTo(lMin) > 0) {
                min = lMin;
            }
            final BigInteger lMax = all.stream().max(Comparator.naturalOrder()).get();
            if (max.compareTo(lMax) < 0) {
                max = lMax;
            }

        }

        System.out.println("duplicates: " + duplicates.size() / 100);
        System.out.println("min " + min);
        System.out.println("max " + max);
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