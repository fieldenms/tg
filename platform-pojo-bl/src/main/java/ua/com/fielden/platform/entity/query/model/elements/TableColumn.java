package ua.com.fielden.platform.entity.query.model.elements;

import ua.com.fielden.platform.entity.query.model.structure.IQueryItem;
import ua.com.fielden.platform.entity.query.model.structure.IQuerySource;
import ua.com.fielden.platform.entity.query.model.structure.IQuerySourceItem;


public class TableColumn implements IQuerySourceItem {
    private final String name;
    private IQuerySource source;

    public TableColumn(final String name) {
	this.name = name;
    }

    @Override
    public void addReference(final IQueryItem referencingItem) {
	// TODO Auto-generated method stub

    }

    @Override
    public void removeReference(final IQueryItem referencingItem) {
	// TODO Auto-generated method stub

    }

}
