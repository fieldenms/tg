package ua.com.fielden.platform.expression.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.function.years.YearsTokenAutomata;

public class YearsTokenAutomataTest {
    private YearsTokenAutomata automata = new YearsTokenAutomata();

    @Test
    public void test_full_recognition_of_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
        assertEquals("Incorrect recognition result", "YEARS", automata.recognisePartiallyFromStart("YEARS(", 0));
        assertEquals("Incorrect recognition result", "YEARS", automata.recognisePartiallyFromStart("   YeARs (", 0));
        assertEquals("Incorrect recognition result", "YEARS", automata.recognisePartiallyFromStart("\t\nyEArS\t(", 0));
    }

    @Test
    public void test_recognition_of_partially_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
        assertEquals("Incorrect recognition result", "YEARS", automata.recognisePartiallyFromStart(" YEARS ( property )", 0));
        assertEquals("Incorrect recognition result", "YEARS", automata.recognisePartiallyFromStart("\tyears\t (\"", 0));
    }

    @Test
    public void test_recognition_of_incorrect_sequences() {
        try {
            automata.recognisePartiallyFromStart("", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("+YEARS(", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("YEARS_(", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("YE ARS(", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("YEARS  ", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("YEARS  d(", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("YEARS)", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
    }

}
