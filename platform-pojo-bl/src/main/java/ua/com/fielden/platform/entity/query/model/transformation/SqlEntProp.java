package ua.com.fielden.platform.entity.query.model.transformation;



public class SqlEntProp implements ISqlSingleOperand {
    //private final String name;
    private final IQuerySourceItem source;

    public SqlEntProp(final IQuerySourceItem source) {
	this.source = source;
    }

    @Override
    public String sql() {
	// TODO Auto-generated method stub
	return null;
    }

}