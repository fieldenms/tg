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
public class StringReplaceVsRegExReplaceAllBenchmark {

    private static final Pattern SPACE_OR_SLASH = Pattern.compile("\\s|/");

    private static final String SHORT_STRING_NO_MATCH = "abc";
    private static final String SHORT_STRING_ONE_MATCH = "a bc";
    private static final String SHORT_STRING_SEVERAL_MATCHES = " a/b c/";
    private static final String LONG_STRING_NO_MATCH = "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc";
    private static final String LONG_STRING_ONE_MATCH = "abcabcabcabcabcabcabcabcabcabcabca bcabcabcabcabcabcabcabcabcabcabcabcabc";
    private static final String LONG_STRING_SEVERAL_MATCHES = "abcabca bcabcabcabcabcabc/abcabcabca/bcabcabcabcabcabca bcabcabcabcabcabcabc";

    ///////////////////////////////////////////
    ////////// String.replace.replace /////////
    ///////////////////////////////////////////

    @Benchmark
    public void stringReplaceTwiceShortStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_NO_MATCH.replace(" ", "_").replace("/", "_"));
    }

    @Benchmark
    public void stringReplaceTwiceShortStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_ONE_MATCH.replace(" ", "_").replace("/", "_"));
    }

    @Benchmark
    public void stringReplaceTwiceShortStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_SEVERAL_MATCHES.replace(" ", "_").replace("/", "_"));
    }

    @Benchmark
    public void stringReplaceTwiceLongStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(LONG_STRING_NO_MATCH.replace(" ", "_").replace("/", "_"));
    }

    @Benchmark
    public void stringReplaceTwiceLongStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(LONG_STRING_ONE_MATCH.replace(" ", "_").replace("/", "_"));
    }

    @Benchmark
    public void stringReplaceTwiceLongStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(LONG_STRING_SEVERAL_MATCHES.replace(" ", "_").replace("/", "_"));
    }

    ///////////////////////////////////////////
    /////////// RegExUtils.replaceAll /////////
    ///////////////////////////////////////////

    @Benchmark
    public void stringReplaceAllWithRegExShortStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(RegExUtils.replaceAll(SHORT_STRING_NO_MATCH, SPACE_OR_SLASH, "_"));
    }

    @Benchmark
    public void stringReplaceAllWithRegExShortStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(RegExUtils.replaceAll(SHORT_STRING_ONE_MATCH, SPACE_OR_SLASH, "_"));
    }

    @Benchmark
    public void stringReplaceAllWithRegExShortStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(RegExUtils.replaceAll(SHORT_STRING_SEVERAL_MATCHES, SPACE_OR_SLASH, "_"));
    }

    @Benchmark
    public void stringReplaceAllWithRegExLongStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(RegExUtils.replaceAll(LONG_STRING_NO_MATCH, SPACE_OR_SLASH, "_"));
    }

    @Benchmark
    public void stringReplaceAllWithRegExLongStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(RegExUtils.replaceAll(LONG_STRING_ONE_MATCH, SPACE_OR_SLASH, "_"));
    }

    @Benchmark
    public void stringReplaceAllWithRegExLongStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(RegExUtils.replaceAll(LONG_STRING_SEVERAL_MATCHES, SPACE_OR_SLASH, "_"));
    }

}