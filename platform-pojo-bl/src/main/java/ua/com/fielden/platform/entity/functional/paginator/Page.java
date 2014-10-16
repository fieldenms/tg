package ua.com.fielden.platform.entity.functional.paginator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.CompanionObject;

/**
 * Represents the page of data that is to be transferred to the client as the result of query runner.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Page title", desc = "Page description")
@CompanionObject(IPage.class)
public class Page extends AbstractEntity<String> {
    private static final long serialVersionUID = -2991967873227384089L;

    /**
     * The query result.
     */
    @IsProperty(AbstractEntity.class)
    @Title(value = "Result list", desc = "Result list")
    private List<AbstractEntity<?>> results = new ArrayList<>();

    /**
     * Represents totals (might be null).
     */
    @IsProperty
    @MapTo
    @Title(value = "Summary", desc = "Summary")
    private AbstractEntity<?> summary;

    /**
     * The page number of the retrieved data.
     */
    @IsProperty
    @MapTo
    @Title(value = "Page number", desc = "Page number")
    private Integer pageNo;

    /**
     * The number of pages that can be retrieved.
     */
    @IsProperty
    @MapTo
    @Title(value = "Number of pages", desc = "Number of pages")
    private Integer numberOfPages;

    @Observable
    public Page setNumberOfPages(final Integer numberOfPages) {
	this.numberOfPages = numberOfPages;
	return this;
    }

    public Integer getNumberOfPages() {
	return numberOfPages;
    }

    @Observable
    public Page setPageNo(final Integer pageNo) {
	this.pageNo = pageNo;
	return this;
    }

    public Integer getPageNo() {
	return pageNo;
    }

    @Observable
    public Page setSummary(final AbstractEntity<?> summary) {
	this.summary = summary;
	return this;
    }

    public AbstractEntity<?> getSummary() {
	return summary;
    }

    @Observable
    public Page setResult(final List<AbstractEntity<?>> results) {
	this.results.clear();
	this.results.addAll(results);
	return this;
    }

    public List<AbstractEntity<?>> getResult() {
	return Collections.unmodifiableList(results);
    }
}