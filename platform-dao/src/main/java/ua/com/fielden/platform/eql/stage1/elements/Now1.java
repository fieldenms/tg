package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.elements.Now2;

public class Now1 extends ZeroOperandFunction1<Now2> {
    public Now1() {
        super("now");
    }

    @Override
    public Now2 transform(final PropsResolutionContext resolutionContext) {
        return new Now2();
    }
}