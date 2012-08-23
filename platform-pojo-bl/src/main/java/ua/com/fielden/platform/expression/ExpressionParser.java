package ua.com.fielden.platform.expression;

import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.exception.MismatchedTokenException;
import ua.com.fielden.platform.expression.exception.MissingTokenException;
import ua.com.fielden.platform.expression.exception.NoViableAltException;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.ReservedNameException;
import ua.com.fielden.platform.expression.exception.UnwantedTokenException;

/**
 * The expression language grammar parser, which constructs an AST as it parses the expression.
 *
 * The main method is {@link #parse()} that either returns a root node of the constructed AST or throws {@link RecognitionException}.
 * <p>
 * Here is a usage example:
 *
 * <pre>
 * final ExpressionLexer el = new ExpressionLexer(&quot;9 + (6 + 2) * 6 - (SUM (property.subPropety)+3) / (34 - prop)&quot;);
 *
 * final ExpressionParser parser = new ExpressionParser(el.tokenize());
 * final AstNode root = parser.parse();
 * System.out.println(root.treeToString());
 * </pre>
 *
 * @author TG Team
 *
 */
public class ExpressionParser {
    private final Token[] tokens;
    private int position;

    /**
     * Construct an array of tokens to be parsed.
     *
     * @param tokens
     */
    public ExpressionParser(final Token[] tokens) {
	this.tokens = tokens;
	position = 0;
    }

    /**
     * The main API method to be used for triggering input parsing. Should be invoked only once.
     *
     * @return
     * @throws RecognitionException
     */
    public AstNode parse() throws RecognitionException {
	final AstNode root = sentence();
	if (position < tokens.length) {
	    throw new UnwantedTokenException("Unwanted token " + tokens[position], tokens[position]);
	}
	return root;
    }

    /**
     * Implements parsing of the rule sentence: <code><b>term (op term)*</b></code>
     *
     * @return
     * @throws RecognitionException
     */
    private AstNode sentence() throws RecognitionException {
	final AstNode leftNode = term();
	return operation(leftNode);
    }

    /**
     * Handles parsing of the <code><b>(op term)*</b></code> portion of the rule <code><b>term (op term)*</b></code>.
     * This include correct handling of operation precedence when construction an AST.
     *
     * Produces an AST node representing an operation (currently +, -, /, *) with all its children, which are defined recursively.
     *
     * @param leftOperandNode
     * @return
     * @throws RecognitionException
     */
    private AstNode operation(final AstNode leftOperandNode) throws RecognitionException {
	while (isOpNext()) {
	    final AstNode node = op();
	    final AstNode rightNode = term();

	    final EgTokenCategory cat = EgTokenCategory.byIndex(node.getToken().category.getIndex());

	    // anything that follows PLUS has higher precedence of execution and thus should be represented by a separate AST node
	    // for example, 5 + 2 * 5, 5 + 6 - 2
	    if (cat == EgTokenCategory.PLUS) {
		final AstNode rightOperandNode = operation(rightNode);
		return operation(node.addChild(leftOperandNode).addChild(rightOperandNode));
	    }
	    // anything that follows MINUS except PLUS has higher order of execution and thus should be represented by a separate AST node
	    // for example, 5 - 2 * 2, but in 5 - 4 + 3 token "-" has higher precedence
	    final EgTokenCategory next = lookahead();
	    if (cat == EgTokenCategory.MINUS && (next == EgTokenCategory.MULT || next == EgTokenCategory.DIV)) { // look ahead to determine if the next token is an operation of higher precedence
		final AstNode rightOperandNode = operation(rightNode);
		return operation(node.addChild(leftOperandNode).addChild(rightOperandNode));
	    }

	    return operation(node.addChild(leftOperandNode).addChild(rightNode));
	}

	return leftOperandNode;
    }

    /**
     * Produces an AST node representing a single term that is current in the token stream.
     * Any literal, left parenthesis and functions are considered to be valid terms.
     * <p>
     * The resultant node includes relevant children (if any).
     *
     * @return
     * @throws RecognitionException
     */
    private AstNode term() throws RecognitionException {
	if (position >= tokens.length) {
	    throw new MissingTokenException("Missing token after token " + tokens[position - 1], tokens[position - 1]);
	}
	final EgTokenCategory cat = EgTokenCategory.byIndex(tokens[position].category.getIndex());
	switch (cat) {
	// trying to match literal rule
	case INT:
	case DECIMAL:
	case STRING:
	case DATE_CONST:
	    return literal(cat);
	    // trying to match property rule
	case SELF:
	    return self();
	    // trying to match sub-sentence
	case NAME:
	    return property();
	    // trying to match SELF rule
	case LPAREN:
	    return subSentence();
	    // trying to match functions
	case AVG:
	case SUM:
	case MIN:
	case MAX:
	case COUNT:
	case DAY:
	case MONTH:
	case YEAR:
	case UPPER:
	case LOWER:
	case DAY_DIFF:
	    return function(cat);
	default:
	    throw new NoViableAltException("Unexpected token " + tokens[position], tokens[position]);
	}

    }

    /**
     * Produces an AST node that contains only an operation token (i.e. without its arguments) in the current position.
     * If the current position does not point to one of the operation tokens then an exception is thrown.
     *
     * @return
     * @throws RecognitionException
     */
    private AstNode op() throws RecognitionException {
	final EgTokenCategory cat = EgTokenCategory.byIndex(tokens[position].category.getIndex());
	switch (cat) {
	// trying to match literal rule
	case PLUS:
	case MINUS:
	case MULT:
	case DIV:
	    return new AstNode(match(cat));
	default:
	    throw new NoViableAltException("Unexpected token '" + tokens[position].text + "' instead of an operation.", tokens[position]);
	}
    }

    private boolean isOpNext() {
	if (position >= tokens.length) {
	    return false;
	}
	final EgTokenCategory cat = EgTokenCategory.byIndex(tokens[position].category.getIndex());
	switch (cat) {
	// trying to match literal rule
	case PLUS:
	case MINUS:
	case MULT:
	case DIV:
	    return true;
	default:
	    return false;
	}
    }

    /**
     * A convenient method for looking ahead into the token stream without actually changing the current position.
     *
     * @return
     */
    private EgTokenCategory lookahead() {
	if (position >= tokens.length) {
	    return EgTokenCategory.EOF;
	}
	return EgTokenCategory.byIndex(tokens[position].category.getIndex());
    }

    private AstNode literal(final EgTokenCategory cat) throws RecognitionException {
	return new AstNode(match(cat));
    }

    /**
     * Produces an AST node representing the name of some property, which could be in a property dot notation format.
     *
     * @return
     * @throws RecognitionException
     */
    private AstNode property() throws RecognitionException {
	final Token propToken = match(EgTokenCategory.NAME);
	if (EgTokenCategory.SELF.name().equalsIgnoreCase(propToken.text)) {
	    throw new ReservedNameException("SELF is a keyword, should not be used as some property name.", propToken);
	}
	return new AstNode(propToken);
    }

    /**
     * Produces an AST node representing SELF keyword.
     *
     * @return
     * @throws RecognitionException
     */
    private AstNode self() throws RecognitionException {
	return new AstNode(match(EgTokenCategory.SELF)) {
	    @Override
	    protected boolean canBeAddedTo(final AstNode intendedParentNode, final StringBuilder reason) {
		final boolean originalResult = super.canBeAddedTo(intendedParentNode, reason);
		if (originalResult && intendedParentNode.getToken().category != EgTokenCategory.COUNT) {
		    reason.append("Token " + EgTokenCategory.SELF + " can only be used as part of " + EgTokenCategory.COUNT + ".");
		    return false;
		}
		return originalResult;
	    }
	};
    }

    /**
     * Produces an AST node representing a subsentence or expression wrapped in parentheses.
     *
     * @return
     * @throws RecognitionException
     */
    private AstNode subSentence() throws RecognitionException {
	match(EgTokenCategory.LPAREN);
	final AstNode node = sentence();
	match(EgTokenCategory.RPAREN);
	return node;
    }

    /**
     * Produces an AST node based on the token category, which is expected to be one of the supported functions.
     * The resultant AST node has all its relevant children determined recursively.
     *
     * @param cat
     * @return
     * @throws RecognitionException
     */
    private AstNode function(final EgTokenCategory cat) throws RecognitionException {
	switch (cat) {
	// trying to match single argument function
	case AVG:
	case SUM:
	case MIN:
	case MAX:
	case COUNT:
	case DAY:
	case MONTH:
	case YEAR:
	case UPPER:
	case LOWER:
	    return new AstNode(match(cat)).addChild(subSentence());
	    // trying to match two argument function
	case DAY_DIFF:
	    final AstNode node = new AstNode(match(EgTokenCategory.DAY_DIFF));
	    match(EgTokenCategory.LPAREN);
	    final AstNode leftArgNode = sentence();
	    match(EgTokenCategory.COMMA);
	    final AstNode rightArgNode = sentence();
	    match(EgTokenCategory.RPAREN);
	    return node.addChild(leftArgNode).addChild(rightArgNode);
	default:
	    throw new NoViableAltException("Could not parse starting from token " + tokens[position] + " at position " + position, tokens[position]);
	}

    }

    /**
     * Tries to match the token of a given category.
     * Moves the position in the token steam after the matched token and returns the matched token for further processing.
     *
     * @param cat
     * @return
     * @throws RecognitionException
     */
    private Token match(final EgTokenCategory cat) throws RecognitionException {
	// check if the current position still points to some token in the stream
	if (position >= tokens.length) {
	    throw new NoViableAltException("Expecting token " + cat + ", but found end of input.", tokens[position - 1]);
	}

	// retrieve the token to be returned
	final Token token = tokens[position];

	// just in case ensure that the retrieved token matches the requested category
	if (token.category.getIndex() != cat.index) {
	    throw new MismatchedTokenException("Could not match " + token + " to category " + cat.toString(), token);
	}

	// move to the next position and return the token
	position++;
	return token;
    }

    /**
     * Returns the current position in the token stream.
     *
     * @return
     */
    public int getPosition() {
	return position;
    }
}
