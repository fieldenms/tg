package ua.com.fielden.platform.entity.query.model.structure;

import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity.query.model.structure.QueryModelResult.ResultPropertyInfo;

// this is also what is required at the very end -- produce IQuerySource for the given model
public class QueryModelSource implements IQuerySource {

    private QueryModel model;

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

}
