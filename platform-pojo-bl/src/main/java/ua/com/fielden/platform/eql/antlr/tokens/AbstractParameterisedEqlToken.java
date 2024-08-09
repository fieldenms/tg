package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;

abstract class AbstractParameterisedEqlToken extends CommonToken {

    /**
     * @param text  name of this token (excluding parameters)
     */
    AbstractParameterisedEqlToken(final int type, final String text) {
        super(type, text);
    }

    public abstract String parametersText();

    @Override
    public String getText() {
        return "%s(%s)".formatted(text, parametersText());
    }

}
