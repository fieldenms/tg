package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFromNone;

final class FromNone<ET extends AbstractEntity<?>> //
		extends Completed<ET> //
		implements IFromNone<ET> {

    public FromNone(final Tokens tokens) {
        super(tokens);
    }
}