package ua.com.fielden.platform.swing.view;


/**
 * A common view for entity centres providing some basic common functionality such as handling of closing based on the model's list of open masters.
 *
 * @author TG Team
 *
 */

 //TODO fix
public abstract class UvCustomEntityCentre {
//<T extends AbstractEntity, DAO extends IEntityDao2<T>, CRIT extends EntityQueryCriteria<T, DAO>, F extends BaseFrame, M extends UmCustomEntityCentre<T, DAO, CRIT, F>> extends EntityReview<T, DAO, CRIT> implements IUmViewOwner {
//    private static final long serialVersionUID = 1L;
//
//    public UvCustomEntityCentre(final M model, final boolean loadRecordByDefault) {
//	super(model, loadRecordByDefault);
//	getEntityReviewModel().setView(this);
//
//	OpenMasterClickAction.enhanceWithClickAction(getEntityGridInspector().getActualModel().getPropertyColumnMappings(),//
//		model.getEntityType(), //
//		model.getEntityMasterFactory(), //
//		this);
//    }
//
//    public UvCustomEntityCentre(final M model) {
//	this(model, false);
//    }
//
//    @Override
//    public M getEntityReviewModel() {
//	return (M) super.getEntityReviewModel();
//    }
//
//    @Override
//    public <E extends AbstractEntity> void notifyEntityChange(final E entity) {
//	if (entity.isPersisted()) {
//	    SwingUtilitiesEx.invokeLater(new Runnable() {
//		@Override
//		public void run() {
//		    getEntityGridInspector().getActualModel().refresh((T) entity);
//		    getProgressLayer().setLocked(false);
//		}
//	    });
//	}
//    }
//
//    protected void add(final JPanel panel, final Map<String, IPropertyEditor> editors, final String propertyName) {
//	final IPropertyEditor editor = editors.get(propertyName);
//	panel.add(editor.getLabel());
//	panel.add(editor.getEditor(), "growx");
//    }
//
//    protected void add(final JPanel panel, final Map<String, IPropertyEditor> editors, final String propertyName, final String layoutParams) {
//	final IPropertyEditor editor = editors.get(propertyName);
//	panel.add(editor.getLabel());
//	panel.add(editor.getEditor(), layoutParams);
//    }
//
//    protected void addAndSpan(final JPanel panel, final Map<String, IPropertyEditor> editors, final String propertyName) {
//	final IPropertyEditor editor = editors.get(propertyName);
//	panel.add(editor.getLabel());
//	panel.add(editor.getEditor(), "span, wrap");
//    }
//
//    protected void addAndWrap(final JPanel panel, final Map<String, IPropertyEditor> editors, final String propertyName) {
//	final IPropertyEditor editor = editors.get(propertyName);
//	panel.add(editor.getLabel());
//	panel.add(editor.getEditor(), "growx, wrap");
//    }
//
//    @Override
//    public String getInfo() {
//	return "default info";
//    }
}
