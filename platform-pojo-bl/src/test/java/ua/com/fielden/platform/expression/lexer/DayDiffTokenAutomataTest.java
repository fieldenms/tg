package ua.com.fielden.platform.expression.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.function.day_diff.DayDiffTokenAutomata;

public class DayDiffTokenAutomataTest {
    private DayDiffTokenAutomata automata = new DayDiffTokenAutomata();

    @Test
    public void test_full_recognition_of_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
        assertEquals("Incorrect recognition result", "DAY_DIFF", automata.recognisePartiallyFromStart("DAY_DIFF(", 0));
        assertEquals("Incorrect recognition result", "DAY_DIFF", automata.recognisePartiallyFromStart("   DaY_DifF (", 0));
        assertEquals("Incorrect recognition result", "DAY_DIFF", automata.recognisePartiallyFromStart("\t\nDAy_dIFf\t(", 0));
    }

    @Test
    public void test_recognition_of_partially_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
        assertEquals("Incorrect recognition result", "DAY_DIFF", automata.recognisePartiallyFromStart(" DAY_DIFF ( property )", 0));
        assertEquals("Incorrect recognition result", "DAY_DIFF", automata.recognisePartiallyFromStart("\tDAY_DIFF\t (\"", 0));
    }

    @Test
    public void test_recognition_of_incorrect_sequences() {
        try {
            automata.recognisePartiallyFromStart("", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("+DAY_DIFF(", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("DAY_DIFF_(", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("DAY_ DIFF(", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("D AY_DIFF(", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("DAY_DIFF  ", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("DAY_DIFF  d(", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("DAY_DIFF)", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
    }

}
