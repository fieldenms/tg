package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundCondition;
import ua.com.fielden.platform.equery.interfaces.IOthers.IExpArgument;
import ua.com.fielden.platform.equery.interfaces.IOthers.ISearchCondition;
import ua.com.fielden.platform.equery.interfaces.IOthers.IWhere;
import ua.com.fielden.platform.equery.interfaces.IOthers.IWhereAtGroup1;

final class Where extends AbstractWhere<ISearchCondition, ICompoundCondition> implements IWhere {

    private final AbstractOpenGroup<IWhereAtGroup1> openGroupImpl;

    Where(final QueryTokens queryTokens) {
	super(queryTokens);
	this.openGroupImpl = new AbstractOpenGroup<IWhereAtGroup1>(queryTokens) {
	    @Override
	    IWhereAtGroup1 createOpenGroup(final QueryTokens queryTokens) {
		return new WhereAtGroup1(queryTokens);
	    }
	};
    }

    @Override
    ISearchCondition createSearchCondition(final QueryTokens queryTokens) {
	return new SearchCondition(queryTokens);
    }

    @Override
    ICompoundCondition createLogicalCondition(final QueryTokens queryTokens) {
	return new CompoundCondition(queryTokens);
    }

    @Override
    public IWhereAtGroup1 begin() {
	return openGroupImpl.begin();
    }

    @Override
    public IWhereAtGroup1 notBegin() {
	return openGroupImpl.notBegin();
    }

    public static void main(final String[] args) {
	equery.select(AbstractEntity.class, "a")
	    .join(AbstractEntity.class, "a")
	    .on()
	    .prop("a")
	    	.eq()
	    .beginExp()
	    	.prop("1").subtract().prop("2")
	    .endExp()
	    	.and()
	    .beginExp().beginExp().beginExp().beginExp().param("1").add().param("2").endExp().add().beginExp().param("1").divide().prop("2").endExp().endExp().endExp().endExp().isFalse()
	    .and()
	    .notExists(null)
	    .where()
	    .countDays().between().prop("1").and().prop("2").eq().beginExp().countDays().between().model(null).and().model(null).endExp().and()
	    .prop("1").ne().param("a")
	.and().beginExp().prop("1").add().beginExp().prop("1").add().prop("2").add().beginExp().beginExp().param("1").divide().param("2").endExp().divide().beginExp().param("3").endExp().endExp().endExp().subtract().prop("2").endExp().eq().prop("a")
	.and().beginExp().prop("2").endExp().eq().beginExp().prop("1").subtract().prop("2").endExp()
	.and().begin().exp("asa").isNull().end()
	.and().beginExp().prop("dsd").endExp().eq().param("ss").and().prop("a").like().beginExp().val(1).add().val(3).endExp().and()
	.begin().beginExp().prop("1").subtract().prop("2").endExp().eq().prop("3").and()
		.begin().beginExp().prop("1").subtract().val(2).endExp().isNotNull().and()
			.begin().exists(null).and().beginExp().prop("1").divide().val(2).multiply().val(3).endExp().eq().beginExp().prop("1").add().prop("2").endExp()
			.end()
		.end()
	.end().and()
	.countDays().between().beginExp().beginExp().beginExp().val(1).subtract().val(2).endExp().endExp().endExp()
	.and()
	.beginExp().beginExp().beginExp().param("1").divide().param("2").endExp().endExp().endExp()
	.eq()
	.countDays().between().prop("1").and().now()
	.and()
	.beginExp().param("1").subtract().upperCase().prop("1").endExp().eq().val(1)
	.and()
	.ifNull().prop("1").then().upperCase().val("AaAv")
		.eq()
	.beginExp().ifNull().beginExp().prop("1").add().prop("2").endExp().then().now().subtract().val(1).endExp()
	//.and().caseWhen().beginExp().param("1").add().prop("2").endExp().eq().param("1").
	.groupByProp("1")
	.groupBy().exp("aaa")
	.groupBy().prop("a")
	.groupBy().beginExp().param("11").add().prop("2").multiply().val(100).endExp()
	.yield().countDays().between().model(null).and().beginExp().beginExp().beginExp().beginExp().param("1").endExp().endExp().endExp().endExp().as("myExp")
	.yield().beginExp().beginExp().ifNull().prop("1").then().now().endExp().endExp().as("mySecondExp")
	.yield().beginExp().averageOf().beginExp().prop("1").add().param("2").endExp().add().val(3).endExp().as("avg_from1+3")
	.yield().averageOf().beginExp().beginExp().prop("1").add().prop("2").endExp().endExp().as("avg_of_1+2")
	.yield().beginExp().beginExp().averageOf().beginExp().beginExp().beginExp().beginExp().countDays().between().prop("1").and().val(100).endExp().endExp().endExp().endExp().add().val(2).endExp().divide().val(100).endExp().as("alias")
	.yield().beginExp().beginExp().sumOf().prop("1").add().averageOf().prop("2").add().beginExp().averageOf().beginExp().beginExp().beginExp().beginExp().val(2).endExp().endExp().endExp().endExp().endExp().endExp().add().averageOf().beginExp().val(2).add().prop("p1").endExp().endExp().as("alias")
	.yield().caseWhen().prop("1").eq().prop("2").and().val(1).ge().val(2).then().val(1).end().as("aaa")
	.yield().round().prop("a").to(10)
	.model();

    }

    @Override
    public IExpArgument<ISearchCondition> beginExp() {
	// TODO Auto-generated method stub
	return null;
    }
}
