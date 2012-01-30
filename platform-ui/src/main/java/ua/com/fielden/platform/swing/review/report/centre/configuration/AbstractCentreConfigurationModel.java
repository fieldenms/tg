package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentreModel;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationModel;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;

public abstract class AbstractCentreConfigurationModel<T extends AbstractEntity, DTM extends ICentreDomainTreeManager> extends AbstractConfigurationModel {

    abstract protected AbstractEntityCentreModel<T, DTM> createEntityCentreModel();

    abstract protected DomainTreeEditorModel<T> createDomainTreeEditorModel();

}
