package ua.com.fielden.platform.expression.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.case_when.case_.CaseTokenAutomata;

public class CaseTokenAutomataTest {
    private CaseTokenAutomata automata = new CaseTokenAutomata();

    @Test
    public void test_full_recognition_of_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
        assertEquals("Incorrect recognition result", "CASE", automata.recognisePartiallyFromStart("CASE ", 0));
        assertEquals("Incorrect recognition result", "CASE", automata.recognisePartiallyFromStart("    cAsE  ", 0));
        assertEquals("Incorrect recognition result", "CASE", automata.recognisePartiallyFromStart("\t\nCASE\t(", 0));
    }

    @Test
    public void test_recognition_of_partially_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
        assertEquals("Incorrect recognition result", "CASE", automata.recognisePartiallyFromStart(" CASE property )", 0));
        assertEquals("Incorrect recognition result", "CASE", automata.recognisePartiallyFromStart("\tCASE\t 1 + 3\"", 0));
    }

    @Test
    public void test_recognition_of_incorrect_sequences() {
        try {
            automata.recognisePartiallyFromStart("", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("(CASE", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart(" CASE", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("_CASE", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("C ASE ", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
    }

}
