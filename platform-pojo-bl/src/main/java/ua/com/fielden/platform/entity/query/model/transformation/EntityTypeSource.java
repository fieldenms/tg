package ua.com.fielden.platform.entity.query.model.transformation;

import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.query.model.structure.QueryModelResult.ResultPropertyInfo;

public class EntityTypeSource implements IQuerySource {

    private Class entityType;
    private int sourcePosition;
    private int masterLevel;

    private final SortedMap<String/*propName*/, ResultPropertyInfo> propertiesWithColumnsInfo = new TreeMap<String, ResultPropertyInfo>();

    public EntityTypeSource(final Class entityType, final int sourcePosition, final int masterLevel) {
	this.entityType = entityType;
	this.sourcePosition = sourcePosition;
	this.masterLevel = masterLevel;

	//populate map with respective info
    }

    @Override
    public ResultPropertyInfo getPropInfo(final String dotNotatedPropName) {
	return propertiesWithColumnsInfo.get(dotNotatedPropName);
    }

    @Override
    public String getSql() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IQuerySourceItem getSourceItem(final String dotNotatedName) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean hasReferences() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public String alias() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getSourceItemSql(final String sourceItemName) {
	// TODO Auto-generated method stub
	return null;
    }

}
