package ua.com.fielden.strings;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import com.google.common.base.Splitter;

import ua.com.fielden.platform.reflection.Reflector;

@Fork(value = 3, jvmArgsAppend = "-Djmh.stack.lines=3")
@Warmup(iterations = 5)
@Measurement(iterations = 5)
//@BenchmarkMode(Mode.AverageTime)
@Threads(4)
public class StringSplittingBenchmark {

    private static final Splitter SPLITTER = Splitter.on(Reflector.DOT_SPLITTER_PATTERN);
    private static final Splitter SPLITTER_BY_DOT = Splitter.on('.');
    private static final String SHORT_STRING_NO_MATCH = "abc";
    private static final String SHORT_STRING_ONE_MATCH = "a.bc";
    private static final String SHORT_STRING_SEVERAL_MATCHES = ".a.b.c.";
    private static final String LONG_STRING_NO_MATCH = "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc";
    private static final String LONG_STRING_ONE_MATCH = "abcabcabcabcabcabcabcabcabcabcabca.bcabcabcabcabcabcabcabcabcabcabcabcabc";
    private static final String LONG_STRING_SEVERAL_MATCHES = "abcabca.bcabcabcabcabcabc.abcabcabca.bcabcabcabcabcabca.bcabcabcabcabcabcabc";

    @Benchmark
    public void stringSplittingShortStringNoMatch(Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_NO_MATCH.split("\\."));
    }

    @Benchmark
    public void stringSplittingShortStringOneMatch(Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_ONE_MATCH.split("\\."));
    }

    @Benchmark
    public void stringSplittingShortStringSeveralMatches(Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_SEVERAL_MATCHES.split("\\."));
    }

    @Benchmark
    public void stringSplittingLongStringNoMatch(Blackhole blackhole) {
        blackhole.consume(LONG_STRING_NO_MATCH.split("\\."));
    }

    @Benchmark
    public void stringSplittingLongStringOneMatch(Blackhole blackhole) {
        blackhole.consume(LONG_STRING_ONE_MATCH.split("\\."));
    }

    @Benchmark
    public void stringSplittingLongStringSeveralMatches(Blackhole blackhole) {
        blackhole.consume(LONG_STRING_SEVERAL_MATCHES.split("\\."));
    }

    @Benchmark
    public void guavaSplittingShortStringNoMatch(Blackhole blackhole) {
        blackhole.consume(SPLITTER.splitToList(SHORT_STRING_NO_MATCH));
    }

    @Benchmark
    public void guavaSplittingShortStringOneMatch(Blackhole blackhole) {
        blackhole.consume(SPLITTER.splitToList(SHORT_STRING_ONE_MATCH));
    }

    @Benchmark
    public void guavaSplittingShortStringSeveralMatches(Blackhole blackhole) {
        blackhole.consume(SPLITTER.splitToList(SHORT_STRING_SEVERAL_MATCHES));
    }

    @Benchmark
    public void guavaSplittingLongStringNoMatch(Blackhole blackhole) {
        blackhole.consume(SPLITTER.splitToList(LONG_STRING_NO_MATCH));
    }

    @Benchmark
    public void guavaSplittingLongStringOneMatch(Blackhole blackhole) {
        blackhole.consume(SPLITTER.splitToList(LONG_STRING_ONE_MATCH));
    }

    @Benchmark
    public void guavaSplittingLongStringSeveralMatches(Blackhole blackhole) {
        blackhole.consume(SPLITTER.splitToList(LONG_STRING_SEVERAL_MATCHES));
    }

    @Benchmark
    public void guavaSplittingByCharShortStringNoMatch(Blackhole blackhole) {
        blackhole.consume(SPLITTER_BY_DOT.splitToList(SHORT_STRING_NO_MATCH));
    }

    @Benchmark
    public void guavaSplittingByCharShortStringOneMatch(Blackhole blackhole) {
        blackhole.consume(SPLITTER_BY_DOT.splitToList(SHORT_STRING_ONE_MATCH));
    }

    @Benchmark
    public void guavaSplittingByCharShortStringSeveralMatches(Blackhole blackhole) {
        blackhole.consume(SPLITTER_BY_DOT.splitToList(SHORT_STRING_SEVERAL_MATCHES));
    }

    @Benchmark
    public void guavaSplittingByCharLongStringNoMatch(Blackhole blackhole) {
        blackhole.consume(SPLITTER_BY_DOT.splitToList(LONG_STRING_NO_MATCH));
    }

    @Benchmark
    public void guavaSplittingByCharLongStringOneMatch(Blackhole blackhole) {
        blackhole.consume(SPLITTER_BY_DOT.splitToList(LONG_STRING_ONE_MATCH));
    }

    @Benchmark
    public void guavaSplittingByCharLongStringSeveralMatches(Blackhole blackhole) {
        blackhole.consume(SPLITTER_BY_DOT.splitToList(LONG_STRING_SEVERAL_MATCHES));
    }

    @Benchmark
    public void patternSplittingShortStringNoMatch(Blackhole blackhole) {
        blackhole.consume(Reflector.DOT_SPLITTER_PATTERN.split(SHORT_STRING_NO_MATCH));
    }

    @Benchmark
    public void patternSplittingShortStringOneMatch(Blackhole blackhole) {
        blackhole.consume(Reflector.DOT_SPLITTER_PATTERN.split(SHORT_STRING_ONE_MATCH));
    }

    @Benchmark
    public void patternSplittingShortStringSeveralMatches(Blackhole blackhole) {
        blackhole.consume(Reflector.DOT_SPLITTER_PATTERN.split(SHORT_STRING_SEVERAL_MATCHES));
    }

    @Benchmark
    public void patternSplittingLongStringNoMatch(Blackhole blackhole) {
        blackhole.consume(Reflector.DOT_SPLITTER_PATTERN.split(LONG_STRING_NO_MATCH));
    }

    @Benchmark
    public void patternSplittingLongStringOneMatch(Blackhole blackhole) {
        blackhole.consume(Reflector.DOT_SPLITTER_PATTERN.split(LONG_STRING_ONE_MATCH));
    }

    @Benchmark
    public void patternSplittingLongStringSeveralMatches(Blackhole blackhole) {
        blackhole.consume(Reflector.DOT_SPLITTER_PATTERN.split(LONG_STRING_SEVERAL_MATCHES));
    }

}