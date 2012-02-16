package ua.com.fielden.platform.expression.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.expression.automata.AbstractState;
import ua.com.fielden.platform.expression.automata.NoTransitionAvailable;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.lexer.name.NameTokenAutomata;

public class NameTokenAutomataTest {
    private NameTokenAutomata automata = new NameTokenAutomata();

    @Test
    public void test_correctness_of_a_single_valid_transition() throws NoTransitionAvailable {
	final AbstractState s = automata.start('a');
	assertEquals("Invalid next state.", automata.getState("S2"), s);
	assertEquals("Invalid recognised sequence", "a", automata.getRecognisedSequence());
    }

    @Test
    public void test_white_space_ignoring() throws NoTransitionAvailable {
	AbstractState s = automata.start(' ');
	assertEquals("Invalid next state.", automata.getState("S0"), s);
	s = automata.start('\t');
	assertEquals("Invalid next state.", automata.getState("S0"), s);
	s = automata.start('\n');
	assertEquals("Invalid next state.", automata.getState("S0"), s);
	s = automata.start('\r');
	assertEquals("Invalid next state.", automata.getState("S0"), s);
	assertNull("Invalid recognised sequence", automata.getRecognisedSequence());
    }

    @Test
    public void test_recognition_of_single_property_with_white_spaces_around() throws NoTransitionAvailable {
	AbstractState s = automata.start(' ');
	assertEquals("Invalid next state.", automata.getState("S0"), s);
	s = s.accept('\t');
	assertEquals("Invalid next state.", automata.getState("S0"), s);
	s = s.accept('a');
	assertEquals("Invalid next state.", automata.getState("S2"), s);
	s = s.accept('b');
	assertEquals("Invalid next state.", automata.getState("S2"), s);
	s = s.accept(' ');
	assertEquals("Invalid next state.", automata.getState("S4"), s);
	s = s.accept('\n');
	assertEquals("Invalid next state.", automata.getState("S4"), s);
	assertEquals("Invalid recognised sequence", " \tab \n", automata.getRecognisedSequence());
    }

    @Test
    public void test_recognition_of_nested_properties_with_white_spaces() throws NoTransitionAvailable {
	AbstractState s = automata.start(' ');
	assertEquals("Invalid next state.", automata.getState("S0"), s);
	s = s.accept('a');
	assertEquals("Invalid next state.", automata.getState("S2"), s);
	s = s.accept('b');
	assertEquals("Invalid next state.", automata.getState("S2"), s);
	s = s.accept(' ');
	assertEquals("Invalid next state.", automata.getState("S4"), s);
	s = s.accept('.');
	assertEquals("Invalid next state.", automata.getState("S3"), s);
	s = s.accept('\n');
	assertEquals("Invalid next state.", automata.getState("S3"), s);
	s = s.accept('Z');
	assertEquals("Invalid next state.", automata.getState("S2"), s);
	s = s.accept('A');
	assertEquals("Invalid next state.", automata.getState("S2"), s);

	assertEquals("Invalid recognised sequence", " ab .\nZA", automata.getRecognisedSequence());
	assertEquals("Invalid recognised sequence", "ab.ZA", " ab .\nZA".replace(" ", "").replace("\n", ""));
    }

    @Test
    public void test_recognition_of_incorrectly_formed_single_property() throws NoTransitionAvailable {
	AbstractState s = automata.start('a');
	assertEquals("Invalid next state.", automata.getState("S2"), s);
	s = s.accept('a');
	assertEquals("Invalid next state.", automata.getState("S2"), s);
	s = s.accept('2');
	assertEquals("Invalid next state.", automata.getState("S2"), s);
	s = s.accept(' ');
	assertEquals("Invalid next state.", automata.getState("S4"), s);
	try {
	    s = s.accept('b');
	    fail("Exception is expected due to invalid transition symbol");
	} catch (final NoTransitionAvailable ex) {
	}

	assertEquals("Invalid recognised sequence", "aa2 ", automata.getRecognisedSequence());
    }

    @Test
    public void test_recognition_of_incorrectly_formed_nested_property() throws NoTransitionAvailable {
	AbstractState s = automata.start('a');
	assertEquals("Invalid next state.", automata.getState("S2"), s);
	s = s.accept('a');
	assertEquals("Invalid next state.", automata.getState("S2"), s);
	s = s.accept('2');
	assertEquals("Invalid next state.", automata.getState("S2"), s);
	s = s.accept(' ');
	assertEquals("Invalid next state.", automata.getState("S4"), s);
	s = s.accept('.');
	assertEquals("Invalid next state.", automata.getState("S3"), s);
	s = s.accept('\t');
	assertEquals("Invalid next state.", automata.getState("S3"), s);
	// test intermediate recognition and pretendant results
	assertEquals("Invalid recognised sequence", "aa2 ", automata.getRecognisedSequence());
	assertEquals("Invalid pretendant sequence", ".\t", automata.getPretendantSequence());
	// let's keep recognising the input...
	s = s.accept('b');
	assertEquals("Invalid next state.", automata.getState("S2"), s);
	assertEquals("Invalid pretendant sequence", "", automata.getPretendantSequence());
	s = s.accept(' ');
	assertEquals("Invalid next state.", automata.getState("S4"), s);
	assertEquals("Invalid pretendant sequence", "", automata.getPretendantSequence());
	try {
	    s = s.accept('2');
	    fail("Exception is expected due to invalid transition symbol");
	} catch (final NoTransitionAvailable ex) {
	}

	assertEquals("Invalid recognised sequence", "aa2 .\tb ", automata.getRecognisedSequence());
	assertEquals("Invalid pretendant sequence", "", automata.getPretendantSequence());
    }

    @Test
    public void ensure_that_failed_automata_cannot_be_used_without_being_reset() throws NoTransitionAvailable {
	AbstractState s = automata.start('a');
	s = s.accept(' ');
	try {
	    s = s.accept('b');
	    fail("Exception is expected due to invalid transition symbol");
	} catch (final NoTransitionAvailable ex) {
	}
	try {
	    s = s.accept('b');
	    fail("Exception is expected due to invalid automata state.");
	} catch (final IllegalStateException ex) {
	    assertEquals("Invalid exception message", "Automata has terminated with recognition exception and cannot continue recognition without being reset.", ex.getMessage());
	}
    }

    @Test
    public void test_full_recognition_of_correct_sequences() throws SequenceRecognitionFailed {
	assertEquals("Incorrect recognition result", "property1.subProperty1", automata.recognisePartiallyFromStart("property1.subProperty1", 0));
	assertEquals("Incorrect recognition result", "property1.subProperty1", automata.recognisePartiallyFromStart("   property1    .    subProperty1  ", 0));
	assertEquals("Incorrect recognition result", "property1.sub2Property1", automata.recognisePartiallyFromStart("\t\nproperty1.    sub2Property1\t", 0));
    }

    @Test
    public void test_recognition_of_partially_correct_sequences() throws SequenceRecognitionFailed {
	assertEquals("Incorrect recognition result", "property1.subProperty1", automata.recognisePartiallyFromStart("property1.subProperty1+", 0));
	assertEquals("Incorrect recognition result", "property1", automata.recognisePartiallyFromStart("property1.1subProperty1", 0));
	assertEquals("Incorrect recognition result", "property1.subProperty1", automata.recognisePartiallyFromStart("   property1    .    subProperty1  sd", 0));
	assertEquals("Incorrect recognition result", "property1.sub2Property1", automata.recognisePartiallyFromStart("\t\nproperty1.    sub2Property1\tbn", 0));
    }

    @Test
    public void test_full_recognition_of_correct_sequences_with_parent_paths() throws SequenceRecognitionFailed {
	assertEquals("Incorrect recognition result", "←.←.property1.subProperty1", automata.recognisePartiallyFromStart("←.←.property1.subProperty1", 0));
	assertEquals("Incorrect recognition result", "←.←.property1.subProperty1", automata.recognisePartiallyFromStart(" ← . ←. property1   .    subProperty1  ", 0));
	assertEquals("Incorrect recognition result", "←.property1.sub2Property1", automata.recognisePartiallyFromStart("\t←\n .property1.    sub2Property1\t", 0));
    }

    @Test
    public void test_recognition_result_of_patially_correct_sequences_with_parent_paths() throws SequenceRecognitionFailed {
	assertEquals("Incorrect recognition result", "←.property1.subProperty1", automata.recognisePartiallyFromStart("←.property1.subProperty1+", 0));
	assertEquals("Incorrect recognition result", "←.property1", automata.recognisePartiallyFromStart("← .property1.1subProperty1", 0));
	assertEquals("Incorrect recognition result", "←.property1.subProperty1", automata.recognisePartiallyFromStart(" ←.  property1    .    subProperty1  sd", 0));
	assertEquals("Incorrect recognition result", "←.property1.sub2Property1", automata.recognisePartiallyFromStart("\t←\n.property1.    sub2Property1\tbn", 0));
	assertEquals("Incorrect recognition result", "←.property1", automata.recognisePartiallyFromStart("← .property1.←.1subProperty1", 0));
    }

    @Test
    public void test_recognition_of_incorrect_sequences_with_parent_paths() throws SequenceRecognitionFailed {
	try {
	    automata.recognisePartiallyFromStart("←", 0);
	    fail("The property name should not be recognised.");
	} catch (final Exception ex) {
	}
	try {
	    automata.recognisePartiallyFromStart("←.", 0);
	    fail("The property name should not be recognised.");
	} catch (final Exception ex) {	}
	try {
	    automata.recognisePartiallyFromStart("←.←", 0);
	    fail("The property name should not be recognised.");
	} catch (final Exception ex) {	}
    }

    @Test
    public void test_recognition_of_incorrect_sequences() {
	try {
	    automata.recognisePartiallyFromStart("", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("+property1.subProperty1", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart(" .  property1    .    subProperty1  sd", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
	try {
	    automata.recognisePartiallyFromStart("\t56\nproperty1.    sub2Property1\tbn", 0);
	    fail("Should have failed");
	} catch (final SequenceRecognitionFailed e) {
	}
    }
}
