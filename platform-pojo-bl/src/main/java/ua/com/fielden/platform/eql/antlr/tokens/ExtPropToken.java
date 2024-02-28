// This file was generated. Timestamp: 2024-02-23T13:09:00.175601485+02:00[Europe/Kyiv]
package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public final class ExtPropToken extends CommonToken {

    public final String propPath;

    public ExtPropToken(final String propPath) {
        super(EQLLexer.EXTPROP, "extProp");
        this.propPath = propPath;
    }

}
