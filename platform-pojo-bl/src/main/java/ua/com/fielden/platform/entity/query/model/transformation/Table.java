package ua.com.fielden.platform.entity.query.model.transformation;

import java.util.List;

import ua.com.fielden.platform.entity.query.model.structure.QueryModelResult.ResultPropertyInfo;


public class Table implements IQuerySource {
    private final Class entityType;
    private List<TableColumn> columns;
    private Object parent;


    public Table(final Class entityType) {
	this.entityType = entityType;
    }


    @Override
    public String alias() {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    public IQuerySourceItem getSourceItem(final String dotNotatedName) {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    public String getSourceItemSql(final String sourceItemName) {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    public boolean hasReferences() {
	// TODO Auto-generated method stub
	return false;
    }


    @Override
    public ResultPropertyInfo getPropInfo(final String dotNotatedPropName) {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    public String getSql() {
	// TODO Auto-generated method stub
	return null;
    }


}
