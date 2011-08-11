grammar Tsv;

@lexer::members {

List<RecognitionException> exceptions = new ArrayList<RecognitionException>();

public List<RecognitionException> getExceptions() {
  return exceptions;
}

@Override
public void reportError(RecognitionException e) {
  super.reportError(e);
  exceptions.add(e);
}

}

line returns [List<String> result]
scope { List fields; } 
@init { $line::fields = new ArrayList(); }
	: (
	    (NEWLINE) => NEWLINE
	    | field (COMMA  field)* NEWLINE
	  )
	  { $result = $line::fields; }
	;

field
	: ( f=QUOTED
	  | f=UNQUOTED
	  | // nothing
	  )
 	{ $line::fields.add(($f == null) ? "" : $f.text); }
	;
	
NEWLINE	:	'\r'? '\n';

COMMA	:	( ' '* '\t' ' '*);

QUOTED	: ('"' ( options {greedy=false;}: . )+ '"')+
	  {
	  	StringBuffer txt = new StringBuffer(getText()); 
	  	// Remove first and last double-quote
	  	txt.deleteCharAt(0);
	  	txt.deleteCharAt(txt.length()-1);
	  	// "" -> "
	  	int probe;
	  	while ((probe = txt.lastIndexOf("\"\"")) >= 0) {
	  		txt.deleteCharAt(probe);
	  	}
	  	setText(txt.toString()); 
	  };
	
// Anything except a line-breaking character is allowed.
UNQUOTED	
	:	~('\r' | '\n' | '\t')+;



