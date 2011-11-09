package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationModel;
import ua.com.fielden.platform.swing.review.wizard.tree.editor.DomainTreeEditorModel;

/**
 * Model for entity centre. This model allows one to configure and view report.
 * 
 * @author TG Team
 *
 * @param <DTME>
 * @param <T>
 * @param <DAO>
 */
public class CentreConfigurationModel<T extends AbstractEntity> extends AbstractConfigurationModel{

    /**
     * The associated {@link GlobalDomainTreeManager} instance.
     */
    private final GlobalDomainTreeManager gdtm;

    /**
     * The entity type for which this {@link CentreConfigurationModel} was created.
     */
    private final Class<T> entityType;

    /**
     * {@link EntityFactory}, needed for {@link DomainTreeEditorModel} creation.
     */
    private final EntityFactory entityFactory;



    //    private IWizard previousWizard;
    //
    //    private IConfigurable previousReview;

    /**
     * Initiates this {@link CentreConfigurationModel} with instance of {@link IGlobalDomainTreeManager}, entity type and {@link EntityFactory}.
     * 
     * @param entityType - the entity type for which this {@link CentreConfigurationModel} will be created.
     * @param gdtm - Associated {@link GlobalDomainTreeManager} instance.
     * @param entityFactory - {@link EntityFactory} needed for wizard model creation.
     */
    public CentreConfigurationModel(final Class<T> entityType, final GlobalDomainTreeManager gdtm, final EntityFactory entityFactory){
	this.entityType = entityType;
	this.gdtm = gdtm;
	this.entityFactory = entityFactory;
    }



    @Override
    protected Result canSetMode(final ReportMode mode) {
	// TODO Implement logic that determines whether report view can be set or not.
	return Result.successful(this);
    }

    //    /**
    //     * Returns the {@link IGlobalDomainTreeManager} instance associated with this centre configuration model.
    //     *
    //     * @return
    //     */
    //    public GlobalDomainTreeManager gdtm(){
    //	return gdtm;
    //    }
    //
    //    /**
    //     * Returns value that indicates the current centre's mode: WIZARD or REPORT.
    //     *
    //     * @return
    //     */
    //    public ReportMode getMode() {
    //	return mode;
    //    }

    //    public void open(){
    //
    //    }
}
