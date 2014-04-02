package ua.com.fielden.platform.expression.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.function.hours.HoursTokenAutomata;

public class HoursTokenAutomataTest {
    private HoursTokenAutomata automata = new HoursTokenAutomata();

    @Test
    public void test_full_recognition_of_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
        assertEquals("Incorrect recognition result", "HOURS", automata.recognisePartiallyFromStart("HOURS(", 0));
        assertEquals("Incorrect recognition result", "HOURS", automata.recognisePartiallyFromStart("   HouRS (", 0));
        assertEquals("Incorrect recognition result", "HOURS", automata.recognisePartiallyFromStart("\t\nHOUrs\t(", 0));
    }

    @Test
    public void test_recognition_of_incorrect_sequences() {
        try {
            automata.recognisePartiallyFromStart("", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("+HOURS(", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("HOURS_(", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("HOU RS(", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("HOURS  ", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("HOURS  d(", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
        try {
            automata.recognisePartiallyFromStart("HOURS)", 0);
            fail("Should have failed");
        } catch (final SequenceRecognitionFailed e) {
        }
    }

}
