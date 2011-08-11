package ua.com.fielden.platform.expression.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.string.StringTokenAutomata;

public class StringTokenAutomataTest {
    private StringTokenAutomata automata = new StringTokenAutomata();

    @Test
    public void test_full_recognition_of_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
	assertEquals("Incorrect recognition result", "\"023 6 word 89\"", automata.recognisePartiallyFromStart("\"023 6 word 89\"", 0));
	assertEquals("Incorrect recognition result", "\"023 6 word 89\"", automata.recognisePartiallyFromStart("   \"023 6 word 89\" ", 0));
	assertEquals("Incorrect recognition result", "\"w1 w2 \"", automata.recognisePartiallyFromStart("\t\n\"w1 w2 \"\t", 0));
    }

    @Test
    public void test_recognition_of_partially_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
	assertEquals("Incorrect recognition result", "\"word\"", automata.recognisePartiallyFromStart("\"word\"\" ", 0));
	assertEquals("Incorrect recognition result", "\"word\"", automata.recognisePartiallyFromStart(" \"word\" +", 0));
	assertEquals("Incorrect recognition result", "\"word\"", automata.recognisePartiallyFromStart("\"word\"\"word\"", 0));
    }

    @Test
    public void test_recognition_of_incorrect_sequences() {
	try {
	    automata.recognisePartiallyFromStart("", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("\"word", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	    assertNotNull("No transition exception should have been present.", e.transitionException);
	    assertEquals("Incorrect transition exception error.", "Missing closing '\"'", e.transitionException.getMessage());
	}
	try {
	    automata.recognisePartiallyFromStart("\"word\\t\"", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("word\"", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
    }

}
