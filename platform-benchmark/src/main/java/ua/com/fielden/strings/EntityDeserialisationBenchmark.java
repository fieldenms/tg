package ua.com.fielden.strings;

import static org.openjdk.jmh.annotations.Threads.MAX;

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
@Threads(MAX)
public class StringSplitVsGuavaSplitterVsPatternSplitBenchmark {

    private static final Splitter SPLITTER = Splitter.on(Reflector.DOT_SPLITTER_PATTERN);
    private static final Splitter SPLITTER_BY_DOT = Splitter.on('.');
    private static final String SHORT_STRING_NO_MATCH = "abc";
    private static final String SHORT_STRING_ONE_MATCH = "a.bc";
    private static final String SHORT_STRING_SEVERAL_MATCHES = ".a.b.c.";
    private static final String LONG_STRING_NO_MATCH = "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc";
    private static final String LONG_STRING_ONE_MATCH = "abcabcabcabcabcabcabcabcabcabcabca.bcabcabcabcabcabcabcabcabcabcabcabcabc";
    private static final String LONG_STRING_SEVERAL_MATCHES = "abcabca.bcabcabcabcabcabc.abcabcabca.bcabcabcabcabcabca.bcabcabcabcabcabcabc";

    @Benchmark
    public void stringSplittingShortStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_NO_MATCH.split("\\."));
    }

    @Benchmark
    public void stringSplittingShortStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_ONE_MATCH.split("\\."));
    }

    @Benchmark
    public void stringSplittingShortStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_SEVERAL_MATCHES.split("\\."));
    }

    @Benchmark
    public void stringSplittingLongStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(LONG_STRING_NO_MATCH.split("\\."));
    }

    @Benchmark
    public void stringSplittingLongStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(LONG_STRING_ONE_MATCH.split("\\."));
    }

    @Benchmark
    public void stringSplittingLongStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(LONG_STRING_SEVERAL_MATCHES.split("\\."));
    }

    @Benchmark
    public void guavaSplittingShortStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(SPLITTER.splitToList(SHORT_STRING_NO_MATCH));
    }

    @Benchmark
    public void guavaSplittingShortStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(SPLITTER.splitToList(SHORT_STRING_ONE_MATCH));
    }

    @Benchmark
    public void guavaSplittingShortStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(SPLITTER.splitToList(SHORT_STRING_SEVERAL_MATCHES));
    }

    @Benchmark
    public void guavaSplittingLongStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(SPLITTER.splitToList(LONG_STRING_NO_MATCH));
    }

    @Benchmark
    public void guavaSplittingLongStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(SPLITTER.splitToList(LONG_STRING_ONE_MATCH));
    }

    @Benchmark
    public void guavaSplittingLongStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(SPLITTER.splitToList(LONG_STRING_SEVERAL_MATCHES));
    }

    @Benchmark
    public void guavaSplittingByCharShortStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(SPLITTER_BY_DOT.splitToList(SHORT_STRING_NO_MATCH));
    }

    @Benchmark
    public void guavaSplittingByCharShortStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(SPLITTER_BY_DOT.splitToList(SHORT_STRING_ONE_MATCH));
    }

    @Benchmark
    public void guavaSplittingByCharShortStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(SPLITTER_BY_DOT.splitToList(SHORT_STRING_SEVERAL_MATCHES));
    }

    @Benchmark
    public void guavaSplittingByCharLongStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(SPLITTER_BY_DOT.splitToList(LONG_STRING_NO_MATCH));
    }

    @Benchmark
    public void guavaSplittingByCharLongStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(SPLITTER_BY_DOT.splitToList(LONG_STRING_ONE_MATCH));
    }

    @Benchmark
    public void guavaSplittingByCharLongStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(SPLITTER_BY_DOT.splitToList(LONG_STRING_SEVERAL_MATCHES));
    }

    @Benchmark
    public void patternSplittingShortStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(Reflector.DOT_SPLITTER_PATTERN.split(SHORT_STRING_NO_MATCH));
    }

    @Benchmark
    public void patternSplittingShortStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(Reflector.DOT_SPLITTER_PATTERN.split(SHORT_STRING_ONE_MATCH));
    }

    @Benchmark
    public void patternSplittingShortStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(Reflector.DOT_SPLITTER_PATTERN.split(SHORT_STRING_SEVERAL_MATCHES));
    }

    @Benchmark
    public void patternSplittingLongStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(Reflector.DOT_SPLITTER_PATTERN.split(LONG_STRING_NO_MATCH));
    }

    @Benchmark
    public void patternSplittingLongStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(Reflector.DOT_SPLITTER_PATTERN.split(LONG_STRING_ONE_MATCH));
    }

    @Benchmark
    public void patternSplittingLongStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(Reflector.DOT_SPLITTER_PATTERN.split(LONG_STRING_SEVERAL_MATCHES));
    }

}