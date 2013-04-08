package ua.com.fielden.platform.swing.review.development;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView.ConfigureAction;
import ua.com.fielden.platform.swing.review.report.interfaces.IReview;

public abstract class AbstractEntityReview<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends SelectableAndLoadBasePanel implements IReview {

    private static final long serialVersionUID = -8984113615241551583L;

    private final AbstractEntityReviewModel<T, CDTME> model;

    private final AbstractConfigurationView<?, ?> owner;

    private final ConfigureAction configureAction/*, saveAction, saveAsAction, saveAsDefaultAction, loadDefaultAction, removeAction*/;

    public AbstractEntityReview(final AbstractEntityReviewModel<T, CDTME> model, final AbstractConfigurationView<? extends AbstractEntityReview<T, CDTME>, ?> owner){
	this.model = model;
	this.owner = owner;
	this.configureAction = createConfigureAction();
    }

    @Override
    public final ConfigureAction getConfigureAction(){
	return configureAction;
    }

    public AbstractConfigurationView<?, ?> getOwner() {
	return owner;
    }

    @Override
    public String getInfo() {
	return "Entity centre";
    }

    /**
     * Returns the {@link AbstractEntityReviewModel} for this entity review.
     *
     * @return
     */
    public AbstractEntityReviewModel<T, CDTME> getModel() {
	return model;
    }

    abstract protected ConfigureAction createConfigureAction();
}
