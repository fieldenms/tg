package ua.com.fielden.platform.expression.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.function.self.SelfTokenAutomata;

public class SelfTokenAutomataTest {
    private SelfTokenAutomata automata = new SelfTokenAutomata();

    @Test
    public void test_full_recognition_of_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
        assertEquals("Incorrect recognition result", "SELF", automata.recognisePartiallyFromStart("SELF )", 0));
        assertEquals("Incorrect recognition result", "SELF", automata.recognisePartiallyFromStart("   SeLf )", 0));
        assertEquals("Incorrect recognition result", "SELF", automata.recognisePartiallyFromStart("   SeLf)", 0));
        assertEquals("Incorrect recognition result", "SELF", automata.recognisePartiallyFromStart("\t\nsELf\t)", 0));
    }

    @Test
    public void test_recognition_of_partially_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
        assertEquals("Incorrect recognition result", "SELF", automata.recognisePartiallyFromStart(" self ) ", 0));
        assertEquals("Incorrect recognition result", "SELF", automata.recognisePartiallyFromStart("\tSELF\t )\"", 0));
    }

    @Test
    public void test_recognition_of_incorrect_sequences() {
        try {
            automata.recognisePartiallyFromStart("", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("+SELF)", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("SELF_)", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("SE LF)", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("S ELF)", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("SELF  b", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
    }

}
