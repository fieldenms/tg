package ua.com.fielden.platform.swing.view;

import java.util.Map;

import javax.swing.JPanel;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.model.IUmViewOwner;
import ua.com.fielden.platform.swing.model.UmCustomEntityCentre;
import ua.com.fielden.platform.swing.review.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.EntityReview;
import ua.com.fielden.platform.swing.review.OpenMasterClickAction;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

/**
 * A common view for entity centres providing some basic common functionality such as handling of closing based on the model's list of open masters.
 *
 * @author TG Team
 *
 */
public abstract class UvCustomEntityCentre<T extends AbstractEntity, DAO extends IEntityDao<T>, CRIT extends EntityQueryCriteria<T, DAO>, F extends BaseFrame, M extends UmCustomEntityCentre<T, DAO, CRIT, F>> extends EntityReview<T, DAO, CRIT> implements IUmViewOwner {
    private static final long serialVersionUID = 1L;

    public UvCustomEntityCentre(final M model, final boolean loadRecordByDefault) {
	super(model, loadRecordByDefault);
	getEntityReviewModel().setView(this);

	OpenMasterClickAction.enhanceWithClickAction(getEntityGridInspector().getActualModel().getPropertyColumnMappings(),//
		model.getEntityType(), //
		model.getEntityMasterFactory(), //
		this);
    }

    public UvCustomEntityCentre(final M model) {
	this(model, false);
    }

    @Override
    public M getEntityReviewModel() {
	return (M) super.getEntityReviewModel();
    }

    @Override
    public <E extends AbstractEntity> void notifyEntityChange(final E entity) {
	if (entity.isPersisted()) {
	    SwingUtilitiesEx.invokeLater(new Runnable() {
		@Override
		public void run() {
		    getEntityGridInspector().getActualModel().refresh((T) entity);
		    getProgressLayer().setLocked(false);
		}
	    });
	}
    }

    protected void add(final JPanel panel, final Map<String, IPropertyEditor> editors, final String propertyName) {
	final IPropertyEditor editor = editors.get(propertyName);
	panel.add(editor.getLabel());
	panel.add(editor.getEditor(), "growx");
    }

    protected void add(final JPanel panel, final Map<String, IPropertyEditor> editors, final String propertyName, final String layoutParams) {
	final IPropertyEditor editor = editors.get(propertyName);
	panel.add(editor.getLabel());
	panel.add(editor.getEditor(), layoutParams);
    }

    protected void addAndSpan(final JPanel panel, final Map<String, IPropertyEditor> editors, final String propertyName) {
	final IPropertyEditor editor = editors.get(propertyName);
	panel.add(editor.getLabel());
	panel.add(editor.getEditor(), "span, wrap");
    }

    protected void addAndWrap(final JPanel panel, final Map<String, IPropertyEditor> editors, final String propertyName) {
	final IPropertyEditor editor = editors.get(propertyName);
	panel.add(editor.getLabel());
	panel.add(editor.getEditor(), "growx, wrap");
    }

    @Override
    public String getInfo() {
	return "default info";
    }
}
