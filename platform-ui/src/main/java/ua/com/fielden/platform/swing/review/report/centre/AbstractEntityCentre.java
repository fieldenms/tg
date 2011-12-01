package ua.com.fielden.platform.swing.review.report.centre;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.development.AbstractEntityReview;

public abstract class AbstractEntityCentre<T extends AbstractEntity> extends AbstractEntityReview<T, ICentreDomainTreeManager> {

    private static final long serialVersionUID = -6079569752962700417L;

    public AbstractEntityCentre(final EntityCentreModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
    }

    @Override
    public EntityCentreModel<T> getModel() {
	return (EntityCentreModel<T>)super.getModel();
    }

    @Override
    protected void initView() {

	final List<JComponent> components = new ArrayList<JComponent>();
	final StringBuffer rowConstraints = new StringBuffer("");
	rowConstraints.append(addToComponents(components, "[fill]", createToolBar()));
	rowConstraints.append(addToComponents(components, "[fill]", createCriteriaPanel()));
	rowConstraints.append(addToComponents(components, "[fill]", createActionPanel()));
	rowConstraints.append(addToComponents(components, "[:400:, fill, grow]", createReview()));

	setLayout(new MigLayout("fill, insets 5", "[:400: ,fill, grow]", isEmpty(rowConstraints.toString()) ? "[fill, grow]" : rowConstraints.toString()));

	for(int componentIndex = 0; componentIndex < components.size() - 1; componentIndex++){
	    add(components.get(componentIndex), "wrap");
	}
	add(components.get(components.size()-1));
    }

    abstract protected JComponent createReview();

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

    protected final String addToComponents(final List<JComponent> components, final String constraint, final JComponent component) {
	if(component != null){
	    components.add(component);
	    return constraint;
	}
	return "";
    }
}
