package ua.com.fielden.platform.swing.review.report.centre;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.view.BasePanel;

public class CentreConfigurationPanel<DTME extends IDomainTreeManagerAndEnhancer, T extends AbstractEntity, DAO extends IEntityDao<T>> extends BasePanel {

    private static final long serialVersionUID = -5187097528373828177L;

    private final CentreConfigurationModel<DTME, T, DAO> model;

    public CentreConfigurationPanel(final CentreConfigurationModel<DTME, T, DAO> model){
	super(new MigLayout("fill, insets 0", "[fill, grow]"));
	this.model = model;
    }

    public CentreConfigurationModel<DTME, T, DAO> getModel() {
	return model;
    }

    @Override
    public String getInfo() {
	// TODO Auto-generated method stub
	return null;
    }

}
