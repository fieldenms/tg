package ua.com.fielden.platform.expression.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.less_or_eq.LessOrEqualTokenAutomata;

public class LessOrEqualTokenAutomataTest {
    private LessOrEqualTokenAutomata automata = new LessOrEqualTokenAutomata();

    @Test
    public void test_full_recognition_of_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
        assertEquals("Incorrect recognition result", "<=", automata.recognisePartiallyFromStart("<=", 0));
        assertEquals("Incorrect recognition result", "<=", automata.recognisePartiallyFromStart("    <=", 0));
        assertEquals("Incorrect recognition result", "<=", automata.recognisePartiallyFromStart("\t\n<=\t(", 0));
    }

    @Test
    public void test_recognition_of_partially_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
        assertEquals("Incorrect recognition result", "<=", automata.recognisePartiallyFromStart(" <= property )", 0));
        assertEquals("Incorrect recognition result", "<=", automata.recognisePartiallyFromStart("\t<=\t 1 + 3\"", 0));
    }

    @Test
    public void test_recognition_of_incorrect_sequences() {
        try {
            automata.recognisePartiallyFromStart("", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("< =", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("(<=", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("_<=", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("S UM<=", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
    }

}
