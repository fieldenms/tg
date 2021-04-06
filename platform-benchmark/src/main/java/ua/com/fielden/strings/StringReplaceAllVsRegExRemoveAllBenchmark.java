package ua.com.fielden.strings;

import static org.openjdk.jmh.annotations.Threads.MAX;

import java.util.regex.Pattern;

import org.apache.commons.lang3.RegExUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Fork(value = 1, jvmArgsAppend = "-Djmh.stack.lines=3")
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Threads(MAX)
public class StringReplaceAllVsRegExRemoveAllBenchmark {

    private static Pattern WILD_CHAR_AT_THE_END = Pattern.compile("%$");

    private static final String SHORT_STRING_NO_MATCH = "abc";
    private static final String SHORT_STRING_ONE_MATCH = "abc%";
    private static final String SHORT_STRING_SEVERAL_MATCHES = " abc%%";
    private static final String LONG_STRING_NO_MATCH = "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc";
    private static final String LONG_STRING_ONE_MATCH = "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc%";
    private static final String LONG_STRING_SEVERAL_MATCHES = "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc%%";

    ///////////////////////////////////////////
    ///////////// String.replaceAll ///////////
    ///////////////////////////////////////////

    @Benchmark
    public void stringReplaceAllShortStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_NO_MATCH.replaceAll("%$", ""));
    }

    @Benchmark
    public void stringReplaceAllShortStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_ONE_MATCH.replaceAll("%$", ""));
    }

    @Benchmark
    public void stringReplaceAllShortStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_SEVERAL_MATCHES.replaceAll("%$", ""));
    }

    @Benchmark
    public void stringReplaceAllLongStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(LONG_STRING_NO_MATCH.replaceAll("%$", ""));
    }

    @Benchmark
    public void stringReplaceAllLongStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(LONG_STRING_ONE_MATCH.replaceAll("%$", ""));
    }

    @Benchmark
    public void stringReplaceAllLongStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(LONG_STRING_SEVERAL_MATCHES.replaceAll("%$", ""));
    }

    ///////////////////////////////////////////
    /////////// RegExUtils.removeAll /////////
    ///////////////////////////////////////////

    @Benchmark
    public void regExUtilRemoveAllShortStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(RegExUtils.removeAll(SHORT_STRING_NO_MATCH, WILD_CHAR_AT_THE_END));
    }

    @Benchmark
    public void regExUtilRemoveAllShortStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(RegExUtils.removeAll(SHORT_STRING_ONE_MATCH, WILD_CHAR_AT_THE_END));
    }

    @Benchmark
    public void regExUtilRemoveAllShortStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(RegExUtils.removeAll(SHORT_STRING_SEVERAL_MATCHES, WILD_CHAR_AT_THE_END));
    }

    @Benchmark
    public void regExUtilRemoveAllLongStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(RegExUtils.removeAll(LONG_STRING_NO_MATCH, WILD_CHAR_AT_THE_END));
    }

    @Benchmark
    public void regExUtilRemoveAllLongStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(RegExUtils.removeAll(LONG_STRING_ONE_MATCH, WILD_CHAR_AT_THE_END));
    }

    @Benchmark
    public void regExUtilRemoveAllLongStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(RegExUtils.removeAll(LONG_STRING_SEVERAL_MATCHES, WILD_CHAR_AT_THE_END));
    }

}