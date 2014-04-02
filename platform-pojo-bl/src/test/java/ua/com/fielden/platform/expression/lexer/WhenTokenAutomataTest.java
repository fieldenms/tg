package ua.com.fielden.platform.expression.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.case_when.when.WhenTokenAutomata;

public class WhenTokenAutomataTest {
    private WhenTokenAutomata automata = new WhenTokenAutomata();

    @Test
    public void test_full_recognition_of_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
        assertEquals("Incorrect recognition result", "WHEN", automata.recognisePartiallyFromStart("WHEN ", 0));
        assertEquals("Incorrect recognition result", "WHEN", automata.recognisePartiallyFromStart("    WheN  ", 0));
        assertEquals("Incorrect recognition result", "WHEN", automata.recognisePartiallyFromStart("\t\nWHEN\t(", 0));
        assertEquals("Incorrect recognition result", "WHEN", automata.recognisePartiallyFromStart("\t\nWHEN(", 0));
    }

    @Test
    public void test_recognition_of_partially_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
        assertEquals("Incorrect recognition result", "WHEN", automata.recognisePartiallyFromStart(" WHEN property )", 0));
        assertEquals("Incorrect recognition result", "WHEN", automata.recognisePartiallyFromStart("\tWHEN\t 1 + 3\"", 0));
    }

    @Test
    public void test_recognition_of_incorrect_sequences() {
        try {
            automata.recognisePartiallyFromStart("", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("(WHEN", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart(" WHEN", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("_WHEN", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("WH EN ", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
    }

}
