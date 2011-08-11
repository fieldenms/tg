/**
 *
 */
package ua.com.fielden.platform.example.entities;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompleted;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.pagination.EmptyPage;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.swing.review.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.utils.MiscUtilities;

import com.google.inject.Inject;

import static ua.com.fielden.platform.equery.equery.select;

/**
 * Criteria for the example purpose.
 *
 * @author Yura, Oleh
 *
 */
public class WheelsetQueryCriteria extends EntityQueryCriteria<Wheelset, IWheelsetDao> {
    private static final long serialVersionUID = -8996933936459527591L;

    private final IWheelsetDao wheelsetDao;

    @IsProperty(String.class)
    @EntityType(Wheelset.class)
    private List<String> rotables = new ArrayList<String>();

    @IsProperty(String.class)
    @EntityType(RotableClass.class)
    private List<String> rotableClasses = new ArrayList<String>();

    @IsProperty
    private boolean hideBogies;

    @IsProperty
    private boolean hideWheelsets;

    @IsProperty
    private boolean compatibleOnly;

    @Inject
    public WheelsetQueryCriteria(final IWheelsetDao wheelsetDao, final IEntityAggregatesDao entityAggregatesDao, final IValueMatcherFactory factory) {
	super(wheelsetDao, entityAggregatesDao, factory);
	this.wheelsetDao = wheelsetDao;
    }

    @Override
    protected ICompleted createQuery() {
	final ICompleted q = select(Wheelset.class)//
	.where().prop("key").like().val(MiscUtilities.prepare(rotables))//
	.and().prop("rotableClass.key").like().val(MiscUtilities.prepare(rotableClasses));
	return q;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected IPage<Wheelset> firstPage(final IQueryOrderedModel<Wheelset> query, final int pageSize) {
	if (hideBogies && hideWheelsets) {
	    return new EmptyPage<Wheelset>();
	} else {
	    final IEntityDao dao = wheelsetDao;
	    return dao.firstPage(query, pageSize);
	}
    }

    @Override
    public void defaultValues() {
	setHideBogies(false);
	setHideWheelsets(false);
	setCompatibleOnly(false);
	setRotables(new ArrayList<String>());
	setRotableClasses(new ArrayList<String>());
    }

    public List<String> getRotables() {
	return rotables;
    }

    @Observable
    public void setRotables(final List<String> rotables) {
	this.rotables = rotables;
    }

    public boolean isHideBogies() {
	return hideBogies;
    }

    @Observable
    public void setHideBogies(final boolean hideBogies) {
	this.hideBogies = hideBogies;
    }

    public List<String> getRotableClasses() {
	return rotableClasses;
    }

    @Observable
    public void setRotableClasses(final List<String> rotableClasses) {
	this.rotableClasses = rotableClasses;
    }

    public boolean isHideWheelsets() {
	return hideWheelsets;
    }

    @Observable
    public void setHideWheelsets(final boolean hideWheelsets) {
	this.hideWheelsets = hideWheelsets;
    }

    public boolean isCompatibleOnly() {
	return compatibleOnly;
    }

    @Observable
    public void setCompatibleOnly(final boolean compatibleOnly) {
	this.compatibleOnly = compatibleOnly;
    }

    @Override
    public boolean isDefaultEnabled() {
	return true;
    }

    @Override
    protected fetch<Wheelset> createFetchModel() {
	// TODO Auto-generated method stub
	return null;
    }
}
