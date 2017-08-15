package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;

final class CompoundCondition0<ET extends AbstractEntity<?>> extends Completed<ET> implements ICompoundCondition0<ET> {

//    private AbstractLogicalCondition<IWhere0<ET>> getLogicalCondition() {
//    	return new AbstractLogicalCondition<IWhere0<ET>>() {
//
//            @Override
//            IWhere0<ET> getParent() {
//                return new Where0<ET>();
//            }
//        };
//    }

    @Override
    public IWhere0<ET> and() {
    	return copy(new Where0<ET>(), getTokens().and());
    	//return getLogicalCondition().and();
    }

    @Override
    public IWhere0<ET> or() {
    	return copy(new Where0<ET>(), getTokens().or());
    	//return getLogicalCondition().or();
    }
}