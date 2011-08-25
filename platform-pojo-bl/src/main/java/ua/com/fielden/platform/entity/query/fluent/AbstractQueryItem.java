package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.model.structure.IQueryItem;
import ua.com.fielden.platform.entity.query.model.transformation.IQuerySourceItem;

public class AbstractQueryItem implements IQueryItem {

    private IQuerySourceItem source;

    public AbstractQueryItem(final IQuerySourceItem source) {
	this.source = source;
	source.addReference(this);
    }

    @Override
    public IQuerySourceItem getSource() {
	return source;
    }
}
