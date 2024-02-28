// This file was generated. Timestamp: 2024-02-23T13:09:00.174656203+02:00[Europe/Kyiv]
package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public final class PropToken extends CommonToken {

    public final String propPath;

    public PropToken(String propPath) {
        super(EQLLexer.PROP, "prop");
        this.propPath = propPath;
    }

}
