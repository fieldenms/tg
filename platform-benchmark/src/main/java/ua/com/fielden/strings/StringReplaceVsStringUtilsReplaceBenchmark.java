package ua.com.fielden.strings;

import static org.openjdk.jmh.annotations.Threads.MAX;

import org.apache.commons.lang3.StringUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Fork(value = 3, jvmArgsAppend = "-Djmh.stack.lines=3")
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Threads(MAX)
public class StringReplaceVsStringUtilsReplaceBenchmark {

    private static final String SHORT_STRING_NO_MATCH = "abc";
    private static final String SHORT_STRING_ONE_MATCH = "a'bc";
    private static final String SHORT_STRING_SEVERAL_MATCHES = "'a'b'c'";
    private static final String LONG_STRING_NO_MATCH = "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc";
    private static final String LONG_STRING_ONE_MATCH = "abcabcabcabcabcabcabcabcabcabcabca'bcabcabcabcabcabcabcabcabcabcabcabcabc";
    private static final String LONG_STRING_SEVERAL_MATCHES = "abcabca'bcabcabcabcabcabc'abcabcabca'bcabcabcabcabcabca'bcabcabcabcabcabcabc";

    @Benchmark
    public void stringReplaceShortStringNoMatch(Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_NO_MATCH.replace("'", "''"));
    }

    @Benchmark
    public void stringReplaceLongStringNoMatch(Blackhole blackhole) {
        blackhole.consume(LONG_STRING_NO_MATCH.replace("'", "''"));
    }

    @Benchmark
    public void stringReplaceShortStringOneMatch(Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_ONE_MATCH.replace("'", "''"));
    }

    @Benchmark
    public void stringReplaceLongStringOneMatch(Blackhole blackhole) {
        blackhole.consume(LONG_STRING_ONE_MATCH.replace("'", "''"));
    }

    @Benchmark
    public void stringReplaceShortStringSeveralMatches(Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_SEVERAL_MATCHES.replace("'", "''"));
    }

    @Benchmark
    public void stringReplaceLongStringSeveralMatches(Blackhole blackhole) {
        blackhole.consume(LONG_STRING_SEVERAL_MATCHES.replace("'", "''"));
    }

    @Benchmark
    public void stringUtilsReplaceShortStringNoMatch(Blackhole blackhole) {
        blackhole.consume(StringUtils.replace(SHORT_STRING_NO_MATCH, "'", "''"));
    }

    @Benchmark
    public void stringUtilsReplaceLongStringNoMatch(Blackhole blackhole) {
        blackhole.consume(StringUtils.replace(LONG_STRING_NO_MATCH, "'", "''"));
    }

    @Benchmark
    public void stringUtilsReplaceShortStringOneMatch(Blackhole blackhole) {
        blackhole.consume(StringUtils.replace(SHORT_STRING_ONE_MATCH, "'", "''"));
    }

    @Benchmark
    public void stringUtilsReplaceLongStringOneMatch(Blackhole blackhole) {
        blackhole.consume(StringUtils.replace(LONG_STRING_ONE_MATCH, "'", "''"));
    }

    @Benchmark
    public void stringUtilsReplaceShortStringSeveralMatches(Blackhole blackhole) {
        blackhole.consume(StringUtils.replace(SHORT_STRING_SEVERAL_MATCHES, "'", "''"));
    }

    @Benchmark
    public void stringUtilsReplaceLongStringSeveralMatches(Blackhole blackhole) {
        blackhole.consume(StringUtils.replace(LONG_STRING_SEVERAL_MATCHES, "'", "''"));
    }
}