package ua.com.fielden.platform.entity.functional.centre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.CompanionObject;

/**
 * Represents the property that must be fetched from db.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Fetch property", desc = "Represents property to fetch.")
@CompanionObject(IFetchProp.class)
public class FetchProp extends AbstractEntity<String> {
    private static final long serialVersionUID = -1714991290359035787L;

    @IsProperty(String.class)
    @Title(value = "Summary properties", desc = "Summary properties")
    private List<String> summary = new ArrayList<>();

    @IsProperty
    @MapTo
    @Title(value = "Property ordering", desc = "Property ordering")
    private Ordering ordering;

    @Observable
    public FetchProp setOrdering(final Ordering ordering) {
	this.ordering = ordering;
	return this;
    }

    public Ordering getOrdering() {
	return ordering;
    }

    @Observable
    public FetchProp setSummary(final List<String> summary) {
	this.summary.clear();
	this.summary.addAll(summary);
	return this;
    }

    public List<String> getSummary() {
	return Collections.unmodifiableList(summary);
    }
}