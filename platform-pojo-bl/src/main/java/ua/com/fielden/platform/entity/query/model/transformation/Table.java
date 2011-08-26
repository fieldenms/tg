package ua.com.fielden.platform.entity.query.model.transformation;

import java.util.ArrayList;
import java.util.List;


public class Table implements IQuerySource {
    private final Class entityType;
    private final String name; // e.g. EQDET
    private final List<TableColumn> columns = new ArrayList<TableColumn>();
    private IQuerySource predecessor;

    public Table(final String name, final Class entityType) {
	this.name = name;
	this.entityType = entityType;
    }

    @Override
    public String alias() {
	int index = 0;
	IQuerySource currSource = this;
	while (currSource != null) {
	    index = index + 1;
	    currSource = currSource.getPredecessor();
	}
	return "T" + index;
    }

    @Override
    public String sql() {
	return name;
    }

    public IQuerySource getPredecessor() {
        return predecessor;
    }

    protected void setPredecessor(final IQuerySource predecessor) {
        this.predecessor = predecessor;
    }

    @Override
    public IQuerySourceItem getSourceItem(final String dotNotatedName) {
	// TODO Auto-generated method stub
	return null;
    }

    public List<TableColumn> getColumns() {
        return columns;
    }
}
