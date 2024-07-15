package ua.com.fielden.strings;

import static java.util.regex.Pattern.compile;
import static org.openjdk.jmh.annotations.Threads.MAX;

import java.util.regex.Pattern;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
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
public class StringReplaceVsStringUtilsRemoveVsRegExUtilsRemoveAllBenchmark {

    private static final String COMMON_SUFFIX = ".common-properties", DUMMY_SUFFIX = ".dummy-property";
    private static final String _COMMON_SUFFIX = "\\.common-properties", _DUMMY_SUFFIX = "\\.dummy-property";
    private static final Pattern REMOVE_OR_PATTERN = compile(String.format("%s|%s", _COMMON_SUFFIX, _DUMMY_SUFFIX));

    
    private static final String SHORT_STRING_NO_MATCH = "abc";
    private static final String SHORT_STRING_ONE_MATCH = "a" + COMMON_SUFFIX + "bc";
    private static final String SHORT_STRING_SEVERAL_MATCHES = COMMON_SUFFIX + "a" + DUMMY_SUFFIX + "b" + COMMON_SUFFIX + "c" + DUMMY_SUFFIX;
    private static final String LONG_STRING_NO_MATCH = "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc";
    private static final String LONG_STRING_ONE_MATCH = "abcabcabcabcabcabcabcabcabcabcabca" + DUMMY_SUFFIX + "bcabcabcabcabcabcabcabcabcabcabcabcabc";
    private static final String LONG_STRING_SEVERAL_MATCHES = "abcabca" + DUMMY_SUFFIX + "bcabcabcabcabcabc" + COMMON_SUFFIX + "abcabcabca" + DUMMY_SUFFIX + "bcabcabcabcabcabca" + COMMON_SUFFIX + "bcabcabcabcabcabcabc";

    
    ///////////////////////////////////////
    //////////// String.replaceAll ////////
    ///////////////////////////////////////

    @Benchmark
    public void replaceAllShortStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_NO_MATCH.replaceAll(DUMMY_SUFFIX, "").replaceAll(COMMON_SUFFIX, ""));
    }

    @Benchmark
    public void replaceAllShortStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_ONE_MATCH.replaceAll(DUMMY_SUFFIX, "").replaceAll(COMMON_SUFFIX, ""));
    }

    @Benchmark
    public void replaceAllShortStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_SEVERAL_MATCHES.replaceAll(DUMMY_SUFFIX, "").replaceAll(COMMON_SUFFIX, ""));
    }

    @Benchmark
    public void replaceAllLongStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(LONG_STRING_NO_MATCH.replaceAll(DUMMY_SUFFIX, "").replaceAll(COMMON_SUFFIX, ""));
    }

    @Benchmark
    public void replaceAllLongStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(LONG_STRING_ONE_MATCH.replaceAll(DUMMY_SUFFIX, "").replaceAll(COMMON_SUFFIX, ""));
    }

    @Benchmark
    public void replaceAllLongStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(LONG_STRING_SEVERAL_MATCHES.replaceAll(DUMMY_SUFFIX, "").replaceAll(COMMON_SUFFIX, ""));
    }


    ///////////////////////////////////////
    ///////////// String.replace //////////
    ///////////////////////////////////////

    @Benchmark
    public void replaceShortStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_NO_MATCH.replace(DUMMY_SUFFIX, "").replace(COMMON_SUFFIX, ""));
    }

    @Benchmark
    public void replaceShortStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_ONE_MATCH.replace(DUMMY_SUFFIX, "").replace(COMMON_SUFFIX, ""));
    }

    @Benchmark
    public void replaceShortStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(SHORT_STRING_SEVERAL_MATCHES.replace(DUMMY_SUFFIX, "").replace(COMMON_SUFFIX, ""));
    }

    @Benchmark
    public void replaceLongStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(LONG_STRING_NO_MATCH.replace(DUMMY_SUFFIX, "").replace(COMMON_SUFFIX, ""));
    }

    @Benchmark
    public void replaceLongStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(LONG_STRING_ONE_MATCH.replace(DUMMY_SUFFIX, "").replace(COMMON_SUFFIX, ""));
    }

    @Benchmark
    public void replaceLongStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(LONG_STRING_SEVERAL_MATCHES.replace(DUMMY_SUFFIX, "").replace(COMMON_SUFFIX, ""));
    }


    ///////////////////////////////////////
    /////////// StringUtils.remove ////////
    ///////////////////////////////////////

    @Benchmark
    public void removeShortStringNoMatch(final Blackhole blackhole) {
        final String removeOne = StringUtils.remove(SHORT_STRING_NO_MATCH, DUMMY_SUFFIX);
        final String removeSecond = StringUtils.remove(removeOne, COMMON_SUFFIX);
        blackhole.consume(removeSecond);
    }

    @Benchmark
    public void removeShortStringOneMatch(final Blackhole blackhole) {
        final String removeOne = StringUtils.remove(SHORT_STRING_ONE_MATCH, DUMMY_SUFFIX);
        final String removeSecond = StringUtils.remove(removeOne, COMMON_SUFFIX);
        blackhole.consume(removeSecond);

    }

    @Benchmark
    public void removeShortStringSeveralMatches(final Blackhole blackhole) {
        final String removeOne = StringUtils.remove(SHORT_STRING_SEVERAL_MATCHES, DUMMY_SUFFIX);
        final String removeSecond = StringUtils.remove(removeOne, COMMON_SUFFIX);
        blackhole.consume(removeSecond);
    }

    @Benchmark
    public void removeLongStringNoMatch(final Blackhole blackhole) {
        final String removeOne = StringUtils.remove(LONG_STRING_NO_MATCH, DUMMY_SUFFIX);
        final String removeSecond = StringUtils.remove(removeOne, COMMON_SUFFIX);
        blackhole.consume(removeSecond);
    }

    @Benchmark
    public void removeLongStringOneMatch(final Blackhole blackhole) {
        final String removeOne = StringUtils.remove(LONG_STRING_ONE_MATCH, DUMMY_SUFFIX);
        final String removeSecond = StringUtils.remove(removeOne, COMMON_SUFFIX);
        blackhole.consume(removeSecond);
    }

    @Benchmark
    public void removeLongStringSeveralMatches(final Blackhole blackhole) {
        final String removeOne = StringUtils.remove(LONG_STRING_SEVERAL_MATCHES, DUMMY_SUFFIX);
        final String removeSecond = StringUtils.remove(removeOne, COMMON_SUFFIX);
        blackhole.consume(removeSecond);
    }


    ///////////////////////////////////////
    ////////// RegExUtils.removeAll //////
    ///////////////////////////////////////

    @Benchmark
    public void removeWithRegExShortStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(RegExUtils.removeAll(SHORT_STRING_NO_MATCH, REMOVE_OR_PATTERN));
    }

    @Benchmark
    public void removeWithRegExShortStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(RegExUtils.removeAll(SHORT_STRING_ONE_MATCH, REMOVE_OR_PATTERN));
    }

    @Benchmark
    public void removeWithRegExShortStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(RegExUtils.removeAll(SHORT_STRING_SEVERAL_MATCHES, REMOVE_OR_PATTERN));
    }

    @Benchmark
    public void removeWithRegExLongStringNoMatch(final Blackhole blackhole) {
        blackhole.consume(RegExUtils.removeAll(LONG_STRING_NO_MATCH, REMOVE_OR_PATTERN));
    }

    @Benchmark
    public void removeWithRegExLongStringOneMatch(final Blackhole blackhole) {
        blackhole.consume(RegExUtils.removeAll(LONG_STRING_ONE_MATCH, REMOVE_OR_PATTERN));
    }

    @Benchmark
    public void removeWithRegExLongStringSeveralMatches(final Blackhole blackhole) {
        blackhole.consume(RegExUtils.removeAll(LONG_STRING_SEVERAL_MATCHES, REMOVE_OR_PATTERN));
    }
}