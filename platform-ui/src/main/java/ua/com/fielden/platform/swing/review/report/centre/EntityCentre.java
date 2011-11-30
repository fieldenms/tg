package ua.com.fielden.platform.swing.review.report.centre;

import static org.apache.commons.lang.StringUtils.isEmpty;

import javax.swing.JComponent;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.development.AbstractEntityReview;

public class EntityCentre<T extends AbstractEntity> extends AbstractEntityReview<T, ICentreDomainTreeManager> {

    private static final long serialVersionUID = -6079569752962700417L;

    public EntityCentre(final EntityCentreModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
    }

    @Override
    protected void initView() {

	//Creating the main components of the entity centre.
	final JComponent toolBar = createToolBar();
	final JComponent criteriaPanel = createCriteriaPanel();
	final JComponent actionPanel = createActionPanel();
	final JComponent review = createReview();

	//Setting the entity centre components' layout.
	final String rowConstraints = (toolBar == null ? "" : "[fill]") + (criteriaPanel == null ? "" : "[fill]")
		/*                */+ (actionPanel == null ? "" : "[fill]") + (review == null ? "" : "[:400:, fill, grow]");

	setLayout(new MigLayout("fill, insets 5", "[:400: ,fill, grow]", isEmpty(rowConstraints) ? "[fill, grow]" : rowConstraints));

	add(toolBar, "wrap");
	add(criteriaPanel, "wrap");
	add(actionPanel, "wrap");
	add(review);
    }

    protected JComponent createReview() {
	// TODO Auto-generated method stub
	return null;
    }

    protected JComponent createActionPanel() {
	// TODO Auto-generated method stub
	return null;
    }

    protected JComponent createCriteriaPanel() {
	// TODO Auto-generated method stub
	return null;
    }

    protected JComponent createToolBar() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public EntityCentreModel<T> getModel() {
	return (EntityCentreModel<T>)super.getModel();
    }

}
