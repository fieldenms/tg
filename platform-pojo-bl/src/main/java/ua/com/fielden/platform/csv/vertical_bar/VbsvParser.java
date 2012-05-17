package ua.com.fielden.platform.csv.vertical_bar;
// $ANTLR 3.3 Nov 30, 2010 12:45:30 /home/oleh/workspace/trident-genesis/platform-pojo-bl/src/main/java/ua/com/fielden/platform/csv/vertical_bar/Vbsv.g 2012-05-15 10:56:33

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

import ua.com.fielden.platform.csv.IParser;
public class VbsvParser extends Parser implements IParser{
    public static final String[] tokenNames = new String[] {
	"<invalid>", "<EOR>", "<DOWN>", "<UP>", "NEWLINE", "COMMA", "QUOTED", "UNQUOTED"
    };
    public static final int EOF=-1;
    public static final int NEWLINE=4;
    public static final int COMMA=5;
    public static final int QUOTED=6;
    public static final int UNQUOTED=7;

    // delegates
    // delegators


    public VbsvParser(final TokenStream input) {
	this(input, new RecognizerSharedState());
    }
    public VbsvParser(final TokenStream input, final RecognizerSharedState state) {
	super(input, state);

    }


    @Override
    public String[] getTokenNames() { return VbsvParser.tokenNames; }
    @Override
    public String getGrammarFileName() { return "/home/oleh/workspace/trident-genesis/platform-pojo-bl/src/main/java/ua/com/fielden/platform/csv/vertical_bar/Vbsv.g"; }


    protected static class line_scope {
	List fields;
    }
    protected Stack line_stack = new Stack();


    // $ANTLR start "line"
    // /home/oleh/workspace/trident-genesis/platform-pojo-bl/src/main/java/ua/com/fielden/platform/csv/vertical_bar/Vbsv.g:19:1: line returns [List<String> result] : ( ( NEWLINE )=> NEWLINE | field ( COMMA field )* NEWLINE ) ;
    public final List<String> line() throws RecognitionException {
	line_stack.push(new line_scope());
	List<String> result = null;

	((line_scope)line_stack.peek()).fields = new ArrayList();
	try {
	    // /home/oleh/workspace/trident-genesis/platform-pojo-bl/src/main/java/ua/com/fielden/platform/csv/vertical_bar/Vbsv.g:22:2: ( ( ( NEWLINE )=> NEWLINE | field ( COMMA field )* NEWLINE ) )
	    // /home/oleh/workspace/trident-genesis/platform-pojo-bl/src/main/java/ua/com/fielden/platform/csv/vertical_bar/Vbsv.g:22:4: ( ( NEWLINE )=> NEWLINE | field ( COMMA field )* NEWLINE )
	    {
		// /home/oleh/workspace/trident-genesis/platform-pojo-bl/src/main/java/ua/com/fielden/platform/csv/vertical_bar/Vbsv.g:22:4: ( ( NEWLINE )=> NEWLINE | field ( COMMA field )* NEWLINE )
		int alt2=2;
		final int LA2_0 = input.LA(1);

		if ( (LA2_0==NEWLINE) ) {
		    final int LA2_1 = input.LA(2);

		    if ( (synpred1_Vbsv()) ) {
			alt2=1;
		    }
		    else if ( (true) ) {
			alt2=2;
		    }
		    else {
			if (state.backtracking>0) {state.failed=true; return result;}
			final NoViableAltException nvae =
				new NoViableAltException("", 2, 1, input);

			throw nvae;
		    }
		}
		else if ( ((LA2_0>=COMMA && LA2_0<=UNQUOTED)) ) {
		    alt2=2;
		}
		else {
		    if (state.backtracking>0) {state.failed=true; return result;}
		    final NoViableAltException nvae =
			    new NoViableAltException("", 2, 0, input);

		    throw nvae;
		}
		switch (alt2) {
		case 1 :
		    // /home/oleh/workspace/trident-genesis/platform-pojo-bl/src/main/java/ua/com/fielden/platform/csv/vertical_bar/Vbsv.g:23:6: ( NEWLINE )=> NEWLINE
		{
		    match(input,NEWLINE,FOLLOW_NEWLINE_in_line47); if (state.failed) return result;

		}
		break;
		case 2 :
		    // /home/oleh/workspace/trident-genesis/platform-pojo-bl/src/main/java/ua/com/fielden/platform/csv/vertical_bar/Vbsv.g:24:8: field ( COMMA field )* NEWLINE
		{
		    pushFollow(FOLLOW_field_in_line56);
		    field();

		    state._fsp--;
		    if (state.failed) return result;
		    // /home/oleh/workspace/trident-genesis/platform-pojo-bl/src/main/java/ua/com/fielden/platform/csv/vertical_bar/Vbsv.g:24:14: ( COMMA field )*
		    loop1:
			do {
			    int alt1=2;
			    final int LA1_0 = input.LA(1);

			    if ( (LA1_0==COMMA) ) {
				alt1=1;
			    }


			    switch (alt1) {
			    case 1 :
				// /home/oleh/workspace/trident-genesis/platform-pojo-bl/src/main/java/ua/com/fielden/platform/csv/vertical_bar/Vbsv.g:24:15: COMMA field
			    {
				match(input,COMMA,FOLLOW_COMMA_in_line59); if (state.failed) return result;
				pushFollow(FOLLOW_field_in_line62);
				field();

				state._fsp--;
				if (state.failed) return result;

			    }
			    break;

			    default :
				break loop1;
			    }
			} while (true);

		    match(input,NEWLINE,FOLLOW_NEWLINE_in_line66); if (state.failed) return result;

		}
		break;

		}

		if ( state.backtracking==0 ) {
		    result = ((line_scope)line_stack.peek()).fields;
		}

	    }

	}
	catch (final RecognitionException re) {
	    reportError(re);
	    recover(input,re);
	}
	finally {
	    line_stack.pop();
	}
	return result;
    }
    // $ANTLR end "line"


    // $ANTLR start "field"
    // /home/oleh/workspace/trident-genesis/platform-pojo-bl/src/main/java/ua/com/fielden/platform/csv/vertical_bar/Vbsv.g:29:1: field : (f= QUOTED | f= UNQUOTED | ) ;
    public final void field() throws RecognitionException {
	Token f=null;

	try {
	    // /home/oleh/workspace/trident-genesis/platform-pojo-bl/src/main/java/ua/com/fielden/platform/csv/vertical_bar/Vbsv.g:30:2: ( (f= QUOTED | f= UNQUOTED | ) )
	    // /home/oleh/workspace/trident-genesis/platform-pojo-bl/src/main/java/ua/com/fielden/platform/csv/vertical_bar/Vbsv.g:30:4: (f= QUOTED | f= UNQUOTED | )
	    {
		// /home/oleh/workspace/trident-genesis/platform-pojo-bl/src/main/java/ua/com/fielden/platform/csv/vertical_bar/Vbsv.g:30:4: (f= QUOTED | f= UNQUOTED | )
		int alt3=3;
		switch ( input.LA(1) ) {
		case QUOTED:
		{
		    alt3=1;
		}
		break;
		case UNQUOTED:
		{
		    alt3=2;
		}
		break;
		case NEWLINE:
		case COMMA:
		{
		    alt3=3;
		}
		break;
		default:
		    if (state.backtracking>0) {state.failed=true; return ;}
		    final NoViableAltException nvae =
			    new NoViableAltException("", 3, 0, input);

		    throw nvae;
		}

		switch (alt3) {
		case 1 :
		    // /home/oleh/workspace/trident-genesis/platform-pojo-bl/src/main/java/ua/com/fielden/platform/csv/vertical_bar/Vbsv.g:30:6: f= QUOTED
		    {
			f=(Token)match(input,QUOTED,FOLLOW_QUOTED_in_field91); if (state.failed) return ;

		    }
		    break;
		case 2 :
		    // /home/oleh/workspace/trident-genesis/platform-pojo-bl/src/main/java/ua/com/fielden/platform/csv/vertical_bar/Vbsv.g:31:6: f= UNQUOTED
		{
		    f=(Token)match(input,UNQUOTED,FOLLOW_UNQUOTED_in_field100); if (state.failed) return ;

		}
		break;
		case 3 :
		    // /home/oleh/workspace/trident-genesis/platform-pojo-bl/src/main/java/ua/com/fielden/platform/csv/vertical_bar/Vbsv.g:33:4:
		{
		}
		break;

		}

		if ( state.backtracking==0 ) {
		    ((line_scope)line_stack.peek()).fields.add((f == null) ? "" : (f!=null?f.getText():null));
		}

	    }

	}
	catch (final RecognitionException re) {
	    reportError(re);
	    recover(input,re);
	}
	finally {
	}
	return ;
    }
    // $ANTLR end "field"

    // $ANTLR start synpred1_Vbsv
    public final void synpred1_Vbsv_fragment() throws RecognitionException {
	// /home/oleh/workspace/trident-genesis/platform-pojo-bl/src/main/java/ua/com/fielden/platform/csv/vertical_bar/Vbsv.g:23:6: ( NEWLINE )
	// /home/oleh/workspace/trident-genesis/platform-pojo-bl/src/main/java/ua/com/fielden/platform/csv/vertical_bar/Vbsv.g:23:7: NEWLINE
	{
	    match(input,NEWLINE,FOLLOW_NEWLINE_in_synpred1_Vbsv42); if (state.failed) return ;

	}
    }
    // $ANTLR end synpred1_Vbsv

    // Delegated rules

    public final boolean synpred1_Vbsv() {
	state.backtracking++;
	final int start = input.mark();
	try {
	    synpred1_Vbsv_fragment(); // can never throw exception
	} catch (final RecognitionException re) {
	    System.err.println("impossible: "+re);
	}
	final boolean success = !state.failed;
	input.rewind(start);
	state.backtracking--;
	state.failed=false;
	return success;
    }




    public static final BitSet FOLLOW_NEWLINE_in_line47 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_field_in_line56 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_COMMA_in_line59 = new BitSet(new long[]{0x00000000000000F0L});
    public static final BitSet FOLLOW_field_in_line62 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_NEWLINE_in_line66 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUOTED_in_field91 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UNQUOTED_in_field100 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NEWLINE_in_synpred1_Vbsv42 = new BitSet(new long[]{0x0000000000000002L});

}