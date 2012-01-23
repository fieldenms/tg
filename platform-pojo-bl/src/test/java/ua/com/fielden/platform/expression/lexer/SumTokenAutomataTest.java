package ua.com.fielden.platform.expression.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.function.sum.SumTokenAutomata;

public class SumTokenAutomataTest {
    private SumTokenAutomata automata = new SumTokenAutomata();

    @Test
    public void test_full_recognition_of_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
	assertEquals("Incorrect recognition result", "SUM", automata.recognisePartiallyFromStart("SUM(", 0));
	assertEquals("Incorrect recognition result", "SUM", automata.recognisePartiallyFromStart("   SuM (", 0));
	assertEquals("Incorrect recognition result", "SUM", automata.recognisePartiallyFromStart("\t\nsUm\t(", 0));
    }

    @Test
    public void test_recognition_of_partially_correct_sequences() throws NoTransitionAvailable, SequenceRecognitionFailed {
	assertEquals("Incorrect recognition result", "SUM", automata.recognisePartiallyFromStart(" sum ( property )", 0));
	assertEquals("Incorrect recognition result", "SUM", automata.recognisePartiallyFromStart("\tSUM\t (\"", 0));
    }

    @Test
    public void test_recognition_of_incorrect_sequences() {
	try {
	    automata.recognisePartiallyFromStart("", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("+SUM(", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("SUM_(", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("SU M(", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("S UM(", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("SUM", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
    }

}
