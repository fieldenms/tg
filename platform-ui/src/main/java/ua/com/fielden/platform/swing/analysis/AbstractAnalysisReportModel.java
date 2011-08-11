package ua.com.fielden.platform.swing.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.categorychart.AnalysisDoubleClickEvent;
import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.models.builders.PropertyTableModelBuilder;
import ua.com.fielden.platform.swing.ei.CriteriaInspectorModel;
import ua.com.fielden.platform.swing.model.UmCustomEntityCentre;
import ua.com.fielden.platform.swing.review.CriteriaPropertyBinder;
import ua.com.fielden.platform.swing.review.DetailsDynamicEntityReview;
import ua.com.fielden.platform.swing.review.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.EntityReview;
import ua.com.fielden.platform.swing.review.EntityReviewModel;
import ua.com.fielden.platform.swing.review.PropertyColumnMappingsPersistentObject;
import ua.com.fielden.platform.swing.view.BaseFrame;

import com.thoughtworks.xstream.XStream;

public abstract class AbstractAnalysisReportModel<T extends AbstractEntity, DAO extends IEntityDao<T>> {

    /**
     * The criteria model for retrieving aggregated data
     */
    private final EntityReviewModel<T, DAO, ? extends EntityQueryCriteria<T, DAO>> centerModel;
    /**
     * Holds all details frame associated with entity and details report.
     */
    private final Map<String, Map<Object, DetailsFrame>> detailsFrames;

    //holds information about report name and analysis report name.
    private final String name;
    private final String reportName;

    public AbstractAnalysisReportModel(final EntityReviewModel<T, DAO, ? extends EntityQueryCriteria<T, DAO>> centerModel, final Map<String, Map<Object, DetailsFrame>> detailsFrame, final String name, final String reportName) {
	this.centerModel = centerModel;
	this.detailsFrames = detailsFrame;
	this.reportName = reportName;
	this.name = name;
    }

    public abstract Object runAnalysisQuery(final int pageSize);

    public abstract void runDoubleClickAction(final AnalysisDoubleClickEvent doubleClickEvent);

    /**
     * Returns the entity center's name to which this analysis report belongs to.
     * 
     * @return
     */
    public final String getReportName() {
	return reportName;
    }

    /**
     * Returns analysis report name.
     * 
     * @return
     */
    public final String getName() {
	return name;
    }

    /**
     * Returns the entity class for which entity center was created.
     * 
     * @return
     */
    public final Class<T> getEntityClass() {
	return centerModel.getCriteria().getDao().getEntityType();
    }

    /**
     * Returns the entity center model that is associated with this analysis report model.
     * 
     * @return
     */
    public final EntityReviewModel<T, DAO, ? extends EntityQueryCriteria<T, DAO>> getCenterModel() {
	return centerModel;
    }

    /**
     * Adds new pop up window specified with {@code frame} to this analysis report.
     * 
     * @param frame
     */
    public final void addDetailsFrame(final DetailsFrame frame) {
	Map<Object, DetailsFrame> details = detailsFrames.get(name);
	if (details != null) {
	    details.put(frame.getAssociatedEntity(), frame);
	} else {
	    details = new HashMap<Object, DetailsFrame>();
	    details.put(frame.getAssociatedEntity(), frame);
	    detailsFrames.put(name, details);
	}
    }

    /**
     * Removes specified pop up window.
     * 
     * @param frame
     */
    public final void removeDetailsFrame(final DetailsFrame frame) {
	final Map<Object, DetailsFrame> details = detailsFrames.get(name);
	if (details != null) {
	    details.remove(frame.getAssociatedEntity());
	}
    }

    /**
     * Returns list of pop up windows associated with this analysis report.
     * 
     * @return
     */
    public final List<DetailsFrame> getDetailsFrames() {
	final Map<Object, DetailsFrame> details = detailsFrames.get(name);
	if (details == null) {
	    return new ArrayList<DetailsFrame>();
	}
	return Collections.unmodifiableList(new ArrayList<DetailsFrame>(details.values()));
    }

    /**
     * Returns pop up window associated with this analysis report and specified object
     * 
     * @param associatedObject
     * @return
     */
    public final DetailsFrame getDetailsFrame(final Object associatedObject) {
	final Map<Object, DetailsFrame> details = detailsFrames.get(name);
	if (details == null) {
	    return null;
	}
	return details.get(associatedObject);
    }

    /**
     * Performs before close actions. (e. g. removes all pop up windows associated with this analysis report).
     */
    public void closeAnalysiReview() {
	detailsFrames.remove(name);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected final EntityReview<T, DAO, EntityQueryCriteria<T, DAO>> getDetailsEntityReview(final EntityQueryCriteria<T, DAO> criteria) {
	PropertyColumnMappingsPersistentObject pObj = getColumnMappingsPersistentObject();
	final XStream serializer = new XStream();
	final String persistedObject = serializer.toXML(pObj);
	pObj = (PropertyColumnMappingsPersistentObject) serializer.fromXML(persistedObject);
	//null, criteria, null, builder(), new CriteriaPropertyBinder<VehicleAvailabilityDetails>(), entityMasterFactory
	final UmCustomEntityCentre<T, DAO, EntityQueryCriteria<T, DAO>, BaseFrame> reviewModel = new UmCustomEntityCentre<T, DAO, EntityQueryCriteria<T, DAO>, BaseFrame>(null, criteria, null, createPropertyTableModelBuilder(pObj), new CriteriaPropertyBinder<EntityQueryCriteria<T, DAO>>(), getCenterModel().getEntityMasterFactory()) {
	    @Override
	    protected CriteriaInspectorModel<T, DAO, EntityQueryCriteria<T, DAO>> createInspectorModel(final EntityQueryCriteria<T, DAO> criteria) {
		return new CriteriaInspectorModel<T, DAO, EntityQueryCriteria<T, DAO>>(criteria);
	    }

	    @Override
	    protected BaseFrame createFrame(final T entity) {
		throw new UnsupportedOperationException("Editing or manual creation of " + TitlesDescsGetter.getEntityTitleAndDesc(getCriteria().getDao().getEntityType()).getKey()
			+ " is not supported.");
	    }
	};
	reviewModel.initSorterWith(new ArrayList(pObj.getPropertyColumnMappings()), pObj.getSortKeys(), pObj.getIsSortable());
	return new DetailsDynamicEntityReview<T, DAO>(reviewModel, true);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private PropertyTableModelBuilder createPropertyTableModelBuilder(final PropertyColumnMappingsPersistentObject obj) {
	final PropertyTableModelBuilder propertyTableModelBuilder = new PropertyTableModelBuilder(getCenterModel().getEntityType());
	propertyTableModelBuilder.getPropertyColumnMappings().addAll(obj.getPropertyColumnMappings());
	return propertyTableModelBuilder;
    }

    private PropertyColumnMappingsPersistentObject getColumnMappingsPersistentObject() {
	return new PropertyColumnMappingsPersistentObject(new ArrayList<AbstractPropertyColumnMapping>(getCenterModel().getEntityReview().getEntityGridInspector().getCurrentColumnsState()),//
	getCenterModel().getEntityReview().getCurrentSortKeyState(),//
	getCenterModel().getEntityReview().getCurrentSortableColumns());
    }

}
