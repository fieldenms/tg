package ua.com.fielden.platform.csv.comma;

// $ANTLR 3.1.1 /home/oles/Desktop/antlr-works/csv/Csv.g 2008-11-28 10:58:58

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

public class CsvLexer extends Lexer {
    public static final int NEWLINE = 4;
    public static final int QUOTED = 6;
    public static final int COMMA = 5;
    public static final int EOF = -1;
    public static final int UNQUOTED = 7;

    private List<RecognitionException> exceptions = new ArrayList<RecognitionException>();

    public List<RecognitionException> getExceptions() {
	return exceptions;
    }

    @Override
    public void reportError(final RecognitionException e) {
	super.reportError(e);
	exceptions.add(e);
    }

    // delegates
    // delegators

    public CsvLexer() {
    }

    public CsvLexer(final CharStream input) {
	this(input, new RecognizerSharedState());
    }

    public CsvLexer(final CharStream input, final RecognizerSharedState state) {
	super(input, state);

    }

    public String getGrammarFileName() {
	return "/home/oles/Desktop/antlr-works/csv/Csv.g";
    }

    // $ANTLR start "NEWLINE"
    public final void mNEWLINE() throws RecognitionException {
	try {
	    final int _TYPE = NEWLINE;
	    final int _CHANNEL = DEFAULT_TOKEN_CHANNEL;
	    // /home/oles/Desktop/antlr-works/csv/Csv.g:36:9: ( ( '\\r' )? '\\n' )
	    // /home/oles/Desktop/antlr-works/csv/Csv.g:36:11: ( '\\r' )? '\\n'
	    {
		// /home/oles/Desktop/antlr-works/csv/Csv.g:36:11: ( '\\r' )?
		int alt1 = 2;
		final int la1_0 = input.LA(1);

		if ((la1_0 == '\r')) {
		    alt1 = 1;
		}
		switch (alt1) {
		case 1:
		    // /home/oles/Desktop/antlr-works/csv/Csv.g:36:11: '\\r'
		{
		    match('\r');

		}
		    break;

		}

		match('\n');

	    }

	    state.type = _TYPE;
	    state.channel = _CHANNEL;
	} finally {
	}
    }

    // $ANTLR end "NEWLINE"

    // $ANTLR start "COMMA"
    public final void mCOMMA() throws RecognitionException {
	try {
	    final int _TYPE = COMMA;
	    final int _CHANNEL = DEFAULT_TOKEN_CHANNEL;
	    // /home/oles/Desktop/antlr-works/csv/Csv.g:38:7: ( ( ( ' ' )* ',' ( ' ' )* ) )
	    // /home/oles/Desktop/antlr-works/csv/Csv.g:38:9: ( ( ' ' )* ',' ( ' ' )* )
	    {
		// /home/oles/Desktop/antlr-works/csv/Csv.g:38:9: ( ( ' ' )* ',' ( ' ' )* )
		// /home/oles/Desktop/antlr-works/csv/Csv.g:38:11: ( ' ' )* ',' ( ' ' )*
		{
		    // /home/oles/Desktop/antlr-works/csv/Csv.g:38:11: ( ' ' )*
		    loop2: do {
			int alt2 = 2;
			final int la2_0 = input.LA(1);

			if ((la2_0 == ' ')) {
			    alt2 = 1;
			}

			switch (alt2) {
			case 1:
			    // /home/oles/Desktop/antlr-works/csv/Csv.g:38:11: ' '
			{
			    match(' ');

			}
			    break;

			default:
			    break loop2;
			}
		    } while (true);

		    match(',');
		    // /home/oles/Desktop/antlr-works/csv/Csv.g:38:20: ( ' ' )*
		    loop3: do {
			int alt3 = 2;
			final int la3_0 = input.LA(1);

			if ((la3_0 == ' ')) {
			    alt3 = 1;
			}

			switch (alt3) {
			case 1:
			    // /home/oles/Desktop/antlr-works/csv/Csv.g:38:20: ' '
			{
			    match(' ');

			}
			    break;

			default:
			    break loop3;
			}
		    } while (true);

		}

	    }

	    state.type = _TYPE;
	    state.channel = _CHANNEL;
	} finally {
	}
    }

    // $ANTLR end "COMMA"

    // $ANTLR start "QUOTED"
    public final void mQUOTED() throws RecognitionException {
	try {
	    final int _TYPE = QUOTED;
	    final int _CHANNEL = DEFAULT_TOKEN_CHANNEL;
	    // /home/oles/Desktop/antlr-works/csv/Csv.g:40:8: ( ( '\"' ( options {greedy=false; } : . )+ '\"' )+ )
	    // /home/oles/Desktop/antlr-works/csv/Csv.g:40:10: ( '\"' ( options {greedy=false; } : . )+ '\"' )+
	    {
		// /home/oles/Desktop/antlr-works/csv/Csv.g:40:10: ( '\"' ( options {greedy=false; } : . )+ '\"' )+
		int cnt5 = 0;
		loop5: do {
		    int alt5 = 2;
		    final int la5_0 = input.LA(1);

		    if ((la5_0 == '\"')) {
			alt5 = 1;
		    }

		    switch (alt5) {
		    case 1:
			// /home/oles/Desktop/antlr-works/csv/Csv.g:40:11: '\"' ( options {greedy=false; } : . )+ '\"'
		    {
			match('\"');
			// /home/oles/Desktop/antlr-works/csv/Csv.g:40:15: ( options {greedy=false; } : . )+
			int cnt4 = 0;
			loop4: do {
			    int alt4 = 2;
			    final int la4_0 = input.LA(1);

			    if ((la4_0 == '\"')) {
				alt4 = 2;
			    } else if (((la4_0 >= '\u0000' && la4_0 <= '!') || (la4_0 >= '#' && la4_0 <= '\uFFFF'))) {
				alt4 = 1;
			    }

			    switch (alt4) {
			    case 1:
				// /home/oles/Desktop/antlr-works/csv/Csv.g:40:42: .
			    {
				matchAny();

			    }
				break;

			    default:
				if (cnt4 >= 1) {
				    break loop4;
				}
				final EarlyExitException eee = new EarlyExitException(4, input);
				throw eee;
			    }
			    cnt4++;
			} while (true);

			match('\"');

		    }
			break;

		    default:
			if (cnt5 >= 1) {
			    break loop5;
			}
			final EarlyExitException eee = new EarlyExitException(5, input);
			throw eee;
		    }
		    cnt5++;
		} while (true);

		final StringBuffer txt = new StringBuffer(getText());
		// Remove first and last double-quote
		txt.deleteCharAt(0);
		txt.deleteCharAt(txt.length() - 1);
		// "" -> "
		int probe;
		while ((probe = txt.lastIndexOf("\"\"")) >= 0) {
		    txt.deleteCharAt(probe);
		}
		setText(txt.toString());

	    }

	    state.type = _TYPE;
	    state.channel = _CHANNEL;
	} finally {
	}
    }

    // $ANTLR end "QUOTED"

    // $ANTLR start "UNQUOTED"
    public final void mUNQUOTED() throws RecognitionException {
	try {
	    final int _TYPE = UNQUOTED;
	    final int _CHANNEL = DEFAULT_TOKEN_CHANNEL;
	    // /home/oles/Desktop/antlr-works/csv/Csv.g:56:2: ( (~ ( '\\r' | '\\n' | ',' ) )+ )
	    // /home/oles/Desktop/antlr-works/csv/Csv.g:56:4: (~ ( '\\r' | '\\n' | ',' ) )+
	    {
		// /home/oles/Desktop/antlr-works/csv/Csv.g:56:4: (~ ( '\\r' | '\\n' | ',' ) )+
		int cnt6 = 0;
		loop6: do {
		    int alt6 = 2;
		    final int la6_0 = input.LA(1);

		    if (((la6_0 >= '\u0000' && la6_0 <= '\t') || (la6_0 >= '\u000B' && la6_0 <= '\f') || (la6_0 >= '\u000E' && la6_0 <= '+') || (la6_0 >= '-' && la6_0 <= '\uFFFF'))) {
			alt6 = 1;
		    }

		    switch (alt6) {
		    case 1:
			// /home/oles/Desktop/antlr-works/csv/Csv.g:56:4: ~ ( '\\r' | '\\n' | ',' )
		    {
			if ((input.LA(1) >= '\u0000' && input.LA(1) <= '\t') || (input.LA(1) >= '\u000B' && input.LA(1) <= '\f') || (input.LA(1) >= '\u000E' && input.LA(1) <= '+')
				|| (input.LA(1) >= '-' && input.LA(1) <= '\uFFFF')) {
			    input.consume();

			} else {
			    final MismatchedSetException mse = new MismatchedSetException(null, input);
			    recover(mse);
			    throw mse;
			}

		    }
			break;

		    default:
			if (cnt6 >= 1) {
			    break loop6;
			}
			final EarlyExitException eee = new EarlyExitException(6, input);
			throw eee;
		    }
		    cnt6++;
		} while (true);

	    }

	    state.type = _TYPE;
	    state.channel = _CHANNEL;
	} finally {
	}
    }

    // $ANTLR end "UNQUOTED"

    public void mTokens() throws RecognitionException {
	// /home/oles/Desktop/antlr-works/csv/Csv.g:1:8: ( NEWLINE | COMMA | QUOTED | UNQUOTED )
	int alt7 = 4;
	alt7 = dfa7.predict(input);
	switch (alt7) {
	case 1:
	    // /home/oles/Desktop/antlr-works/csv/Csv.g:1:10: NEWLINE
	{
	    mNEWLINE();

	}
	    break;
	case 2:
	    // /home/oles/Desktop/antlr-works/csv/Csv.g:1:18: COMMA
	{
	    mCOMMA();

	}
	    break;
	case 3:
	    // /home/oles/Desktop/antlr-works/csv/Csv.g:1:24: QUOTED
	{
	    mQUOTED();

	}
	    break;
	case 4:
	    // /home/oles/Desktop/antlr-works/csv/Csv.g:1:31: UNQUOTED
	{
	    mUNQUOTED();

	}
	    break;

	}

    }

    private DFA7 dfa7 = new DFA7(this);
    private static final String DFA7_EOTS = "\2\uffff\1\5\1\uffff\1\5\1\uffff\1\5\1\uffff\2\7";
    private static final String DFA7_EOFS = "\12\uffff";
    private static final String DFA7_MINS = "\1\0\1\uffff\1\40\1\uffff\1\0\1\uffff\1\0\1\uffff\2\0";
    private static final String DFA7_MAXS = "\1\uffff\1\uffff\1\54\1\uffff\1\uffff\1\uffff\1\uffff\1\uffff\2" + "\uffff";
    private static final String DFA7_ACCEPTS = "\1\uffff\1\1\1\uffff\1\2\1\uffff\1\4\1\uffff\1\3\2\uffff";
    private static final String DFA7_SPECIALS = "\1\2\3\uffff\1\3\1\uffff\1\0\1\uffff\1\4\1\1}>";
    private static final String[] DFA7_TRANSITIONS = { "\12\5\1\1\2\5\1\1\22\5\1\2\1\5\1\4\11\5\1\3\uffd3\5", "", "\1\2\13\uffff\1\3", "", "\12\6\1\7\2\6\1\7\36\6\1\7\uffd3\6",
	    "", "\12\6\1\7\2\6\1\7\24\6\1\10\11\6\1\7\uffd3\6", "", "\12\6\1\uffff\2\6\1\uffff\24\6\1\11\11\6\1\uffff\uffd3\6",
	    "\12\6\1\uffff\2\6\1\uffff\24\6\1\11\11\6\1\uffff\uffd3\6" };

    private static final short[] DFA7_EOT = DFA.unpackEncodedString(DFA7_EOTS);
    private static final short[] DFA7_EOF = DFA.unpackEncodedString(DFA7_EOFS);
    private static final char[] DFA7_MIN = DFA.unpackEncodedStringToUnsignedChars(DFA7_MINS);
    private static final char[] DFA7_MAX = DFA.unpackEncodedStringToUnsignedChars(DFA7_MAXS);
    private static final short[] DFA7_ACCEPT = DFA.unpackEncodedString(DFA7_ACCEPTS);
    private static final short[] DFA7_SPECIAL = DFA.unpackEncodedString(DFA7_SPECIALS);
    private static final short[][] DFA7_TRANSITION;

    static {
	final int numStates = DFA7_TRANSITIONS.length;
	DFA7_TRANSITION = new short[numStates][];
	for (int i = 0; i < numStates; i++) {
	    DFA7_TRANSITION[i] = DFA.unpackEncodedString(DFA7_TRANSITIONS[i]);
	}
    }

    class DFA7 extends DFA {

	public DFA7(final BaseRecognizer recognizer) {
	    this.recognizer = recognizer;
	    this.decisionNumber = 7;
	    this.eot = DFA7_EOT;
	    this.eof = DFA7_EOF;
	    this.min = DFA7_MIN;
	    this.max = DFA7_MAX;
	    this.accept = DFA7_ACCEPT;
	    this.special = DFA7_SPECIAL;
	    this.transition = DFA7_TRANSITION;
	}

	public String getDescription() {
	    return "1:1: Tokens : ( NEWLINE | COMMA | QUOTED | UNQUOTED );";
	}

	public int specialStateTransition(int s, final IntStream input) throws NoViableAltException {
	    final IntStream _input = input;
	    final int _s = s;
	    switch (s) {
	    case 0:
		s = specialStateTransition0(_input);

		if (s >= 0) {
		    return s;
		}
		break;
	    case 1:
		s = specialStateTransition1(_input);

		if (s >= 0) {
		    return s;
		}
		break;
	    case 2:
		s = specialStateTransition2(_input);

		if (s >= 0) {
		    return s;
		}
		break;
	    case 3:
		s = specialStateTransition3(_input);

		if (s >= 0) {
		    return s;
		}
		break;
	    case 4:
		s = specialStateTransition1(_input);

		if (s >= 0) {
		    return s;
		}
		break;
	    }
	    final NoViableAltException nvae = new NoViableAltException(getDescription(), 7, _s, _input);
	    error(nvae);
	    throw nvae;
	}

	private int specialStateTransition3(final IntStream _input) {
	    final int la7_4 = _input.LA(1);

	    int s = -1;
	    if (((la7_4 >= '\u0000' && la7_4 <= '\t') || (la7_4 >= '\u000B' && la7_4 <= '\f') || (la7_4 >= '\u000E' && la7_4 <= '+') || (la7_4 >= '-' && la7_4 <= '\uFFFF'))) {
		s = 6;
	    } else if ((la7_4 == '\n' || la7_4 == '\r' || la7_4 == ',')) {
		s = 7;
	    } else {
		s = 5;
	    }
	    return s;
	}

	private int specialStateTransition2(final IntStream _input) {
	    final int la7_0 = _input.LA(1);

	    int s = -1;
	    if ((la7_0 == '\n' || la7_0 == '\r')) {
		s = 1;
	    } else if ((la7_0 == ' ')) {
		s = 2;
	    } else if ((la7_0 == ',')) {
		s = 3;
	    } else if ((la7_0 == '\"')) {
		s = 4;
	    } else if (((la7_0 >= '\u0000' && la7_0 <= '\t') || (la7_0 >= '\u000B' && la7_0 <= '\f') || (la7_0 >= '\u000E' && la7_0 <= '\u001F') || la7_0 == '!'
		    || (la7_0 >= '#' && la7_0 <= '+') || (la7_0 >= '-' && la7_0 <= '\uFFFF'))) {
		s = 5;
	    }
	    return s;
	}

	private int specialStateTransition1(final IntStream _input) {
	    final int la7_9 = _input.LA(1);

	    int s = -1;
	    if ((la7_9 == '\"')) {
		s = 9;
	    } else if (((la7_9 >= '\u0000' && la7_9 <= '\t') || (la7_9 >= '\u000B' && la7_9 <= '\f') || (la7_9 >= '\u000E' && la7_9 <= '!') || (la7_9 >= '#' && la7_9 <= '+') || (la7_9 >= '-' && la7_9 <= '\uFFFF'))) {
		s = 6;
	    } else {
		s = 7;
	    }
	    return s;
	}

	private int specialStateTransition0(final IntStream _input) {
	    final int la7_6 = _input.LA(1);

	    int s = -1;
	    if ((la7_6 == '\"')) {
		s = 8;
	    } else if (((la7_6 >= '\u0000' && la7_6 <= '\t') || (la7_6 >= '\u000B' && la7_6 <= '\f') || (la7_6 >= '\u000E' && la7_6 <= '!') || (la7_6 >= '#' && la7_6 <= '+') || (la7_6 >= '-' && la7_6 <= '\uFFFF'))) {
		s = 6;
	    } else if ((la7_6 == '\n' || la7_6 == '\r' || la7_6 == ',')) {
		s = 7;
	    } else {
		s = 5;
	    }
	    return s;
	}
    }

}