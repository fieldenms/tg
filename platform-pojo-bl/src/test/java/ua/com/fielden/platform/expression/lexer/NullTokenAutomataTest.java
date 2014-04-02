package ua.com.fielden.platform.expression.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.null_.NullTokenAutomata;

public class NullTokenAutomataTest {

    private NullTokenAutomata automata() {
        return new NullTokenAutomata();
    }

    @Test
    public void test_full_recognition_of_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
        assertEquals("Incorrect recognition result", "NULL", automata().recognisePartiallyFromStart("NULL", 0));
        assertEquals("Incorrect recognition result", "NULL", automata().recognisePartiallyFromStart("    nUlL  ", 0));
        assertEquals("Incorrect recognition result", "NULL", automata().recognisePartiallyFromStart("\t\nNull\t)", 0));
        assertEquals("Incorrect recognition result", "NULL", automata().recognisePartiallyFromStart(" NULL))", 0));
        assertEquals("Incorrect recognition result", "NULL", automata().recognisePartiallyFromStart(" NULL+", 0));
        assertEquals("Incorrect recognition result", "NULL", automata().recognisePartiallyFromStart("NULL/", 0));
    }

    @Test
    public void test_recognition_of_incorrect_sequences() {
        try {
            automata().recognisePartiallyFromStart("", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata().recognisePartiallyFromStart("(NULL", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata().recognisePartiallyFromStart("(NULL_", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata().recognisePartiallyFromStart("NULLed", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata().recognisePartiallyFromStart("NULL1", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata().recognisePartiallyFromStart("NULL.", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata().recognisePartiallyFromStart("NULL.lala", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata().recognisePartiallyFromStart("_NULL", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata().recognisePartiallyFromStart("NU LL ", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
    }

}
