package ua.com.fielden.platform.expression;


/***
 * Class modelling the lexeme (aka token) concept.
 *
 * @author TG Team
 */
public class Token {
    public final ILexemeCategory category;
    public final String text;
    public final Integer beginIndex;
    public final Integer endIndex;

    /**
     * Primary constructor, which creates a token with category, text and position information.
     *
     * @param category
     * @param text
     * @param startIndex
     * @param endIndex
     */
    public Token(final ILexemeCategory category, final String text, final Integer startIndex, final Integer endIndex) {
	if (category == null || text == null) {
	    throw new IllegalArgumentException("Both token category and text should be specified.");
	}
	this.beginIndex = startIndex;
	this.endIndex = endIndex;
	this.category = category;
	this.text = text;
    }

    /**
     * This constructor should be used in cases where token position in the original text is irrelevant or not known.
     *
     * @param category
     * @param text
     */
    public Token(final ILexemeCategory category, final String text) {
	this(category, text, null, null);
    }

    public String toString() {
	return "<'" + text + "'," + category.getName() + ">";
    }

    @Override
    public int hashCode() {
        return category.hashCode() * 13 + text.hashCode() * 29;
    }

    @Override
    public boolean equals(final Object obj) {
	if (obj == this) {
            return true;
        }
        if (!(obj instanceof Token)) {
            return false;
        }

        final Token that = (Token) obj;

        return category.equals(that.category) && text.equals(that.text);
    }
}