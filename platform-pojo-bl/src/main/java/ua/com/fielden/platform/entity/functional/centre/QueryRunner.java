package ua.com.fielden.platform.entity.functional.centre;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.functional.IFunctionalEntity;
import ua.com.fielden.platform.entity.functional.paginator.Page;
import ua.com.fielden.platform.entity.validation.annotation.CompanionObject;

/**
 * A functional entity that is responsible for running queries on the entity centre.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(IQueryRunner.class)
public class QueryRunner extends AbstractEntity<String> implements IFunctionalEntity{
    private static final long serialVersionUID = 2994126659101256859L;

    @IsProperty
    @MapTo
    @Title(value = "Centre Query", desc = "Centre Query")
    private QueryEntity query;

    @IsProperty
    @MapTo
    @Title(value = "Result page", desc = "Result page")
    private Page page;

    @Observable
//    @EntityExists(Page.class)
    public QueryRunner setPage(final Page page) {
	this.page = page;
	return this;
    }

    @IsProperty
    @MapTo
    @Title(value = "Page capacity", desc = "Page capacity")
    private Integer pageCapacity;

    @Observable
    public QueryRunner setPageCapacity(final Integer pageCapacity) {
	this.pageCapacity = pageCapacity;
	return this;
    }

    public Integer getPageCapacity() {
	return pageCapacity;
    }

    public Page getPage() {
	return page;
    }

    @Observable
//    @EntityExists(QueryEntity.class)
    public QueryRunner setQuery(final QueryEntity query) {
	this.query = query;
	return this;
    }

    public QueryEntity getQuery() {
	return query;
    }
}