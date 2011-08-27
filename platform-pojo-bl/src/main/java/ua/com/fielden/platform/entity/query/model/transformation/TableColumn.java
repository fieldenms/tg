package ua.com.fielden.platform.entity.query.model.transformation;

public class TableColumn implements IQuerySourceItem {
    private final String name;
    private final String column;
    private Table table;

    public TableColumn(final String name, final String column, final Table table) {
	this.name = name;
	this.column = column;
	this.table = table;
    }

    @Override
    public String name() {
	return name; //purchPrice, model
    }

    @Override
    public String sqlBody() {
	return table.sqlAlias() + "." + column; //T1.PURCH_PRICE, T1.ID_VEH_MODEL
    }

    @Override
    public String sqlAlias() {
	return column;
    }

//    @Override
//    public IQuerySource getSource() {
//	return table;
//    }
}
