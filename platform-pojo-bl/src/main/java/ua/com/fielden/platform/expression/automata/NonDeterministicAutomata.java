package ua.com.fielden.platform.expression.automata;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Non-deterministic automata implementation. The automata starts by invocation of method {@link #start(char)},
 * which either throws an exception or returns the next state.
 * The returned state should be used for further input processing by invoking its method {@link AbstractState#accept(char)}.
 * <p>
 * Each state specified for an instance of automata maintains a back link,
 * to ensure easy tracking of the current state, the last processed symbol and the processed sequence using the automata instance.
 * <p>
 * Method {@link #recognisePartiallyFromStart(String)} provides a convenient way to recognise a lexeme from the beginning of the provided input and ignore the rest of the input.
 *
 * @author TG Team
 *
 */

public class NonDeterministicAutomata {

    public enum TEXT_POST_PROCESSING {
	NONE, REMOVE_WS, TRIM;
    }

    public static final char EOF = (char) -1;

    private final Map<String, AbstractState> states = new HashMap<String, AbstractState>();
    private final AbstractState initState;
    private AbstractState currState;
    private AbstractState lastFinalState;
    private StringBuilder recognised;
    private StringBuilder pretendant;
    private NoTransitionAvailable failure;
    final TEXT_POST_PROCESSING postProcessingAction;

    private int charsRecognised = 0;;

    public NonDeterministicAutomata(final TEXT_POST_PROCESSING postProcessingAction, final AbstractState initState, final AbstractState... states) {
	if (initState == null) {
	    throw new IllegalArgumentException("Initial state is required.");
	}

	this.postProcessingAction = postProcessingAction;
	this.initState = initState;
	this.states.put(initState.name, initState);
	currState = initState; // do not use setCurrSate as it buffers the input
	initState.setAutomata(this);
	for (final AbstractState state : states) {
	    state.setAutomata(this);
	    this.states.put(state.name, state);
	}

	reset();
    }

    private final void reset() {
	charsRecognised = 0;
	currState = initState;
	this.failure = null;
	recognised = new StringBuilder();
	pretendant = new StringBuilder();
	lastFinalState = currState.isFinal ? currState : null;
    }

    public final AbstractState start(final char symbol) throws NoTransitionAvailable {
	reset();
	return currState.accept(symbol);
    }

    final void setCurrSate(final AbstractState state) {
	currState = state;
	pretendant.append(state.getTransitionSymbol());
	if (currState.isFinal) {
	    lastFinalState = currState;
	    recognised = new StringBuilder(recognised).append(pretendant);
	    charsRecognised = recognised.length();
	    pretendant = new StringBuilder();
	}
    }

    public final AbstractState getState(final String name) {
	return states.get(name);
    }

    public final String getRecognisedSequence() {
	//if (lastFinalState != null) {
	if (currState != null && currState.isFinal) {
	    return recognised.toString();
	}
	return null;
    }

    public final String getPretendantSequence() {
	return pretendant.toString();
    }

    public final NoTransitionAvailable getFailure() {
        return failure;
    }

    final void setFailure(final NoTransitionAvailable failure) {
	if (this.failure != null && failure == null) {
	    throw new IllegalStateException("Recognition failure should not be reset outside of the automata.");
	}
        this.failure = failure;
    }

    /**
     * Attempts to recognise a token from the beginning of the input text.
     * The recognitions may result in failure or a complete/partial recognition of the input text.
     *
     * @param input -- the input text, which is processed.
     * @param posInOriginalSequence -- the starting positions of the input text in the original sequence (input is usually a substring of some other character sequence).
     * @return -- the portion of the input, which has been recognised and thus forms a valid lexeme (token).
     * @throws SequenceRecognitionFailed -- indicates failure to recognise any lexeme, the exception contains the value of posInOriginalSequence to facilitate error reporting.
     */
    public String recognisePartiallyFromStart(final String input, final Integer posInOriginalSequence) throws SequenceRecognitionFailed {
	final char[] in = input.toCharArray();
	String result = null;
	try {
	    final char startSymbol = in.length == 0 ? EOF : in[0];
	    AbstractState state = start(startSymbol);
	    for (int index = 1; index <= in.length; index++) {
		final char symbol = index < in.length ? in[index] : EOF;
		state = state.accept(symbol);
	    }
	    result = getRecognisedSequence();
	} catch (final NoTransitionAvailable ex) {
	    result = getRecognisedSequence();
	    if (StringUtils.isEmpty(result)) {
		throw new SequenceRecognitionFailed(pretendant.toString(), ex, posInOriginalSequence);
	    }
	}
	if (result == null) {
	    //throw new SequenceRecognitionFailed(pretendant.toString());
	    throw new IllegalStateException("Most likely some of the states do not handle EOF.");
	}

	switch(postProcessingAction) {
	case REMOVE_WS: return result.replaceAll("\\s", "");
	case TRIM: return result.trim();
	default: return result;
	}
    }

    public AbstractState getInitState() {
        return initState;
    }

    public int getCharsRecognised() {
        return charsRecognised;
    }

    protected void setCharsRecognised(final int charsRead) {
        this.charsRecognised = charsRead;
    }
}
