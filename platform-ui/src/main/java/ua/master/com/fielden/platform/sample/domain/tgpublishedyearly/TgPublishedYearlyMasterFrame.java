package ua.master.com.fielden.platform.sample.domain.tgpublishedyearly;

import org.jfree.ui.RefineryUtilities;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.domaintree.master.IMasterDomainTreeManager;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.sample.domain.ITgPublishedYearly;
import ua.com.fielden.platform.sample.domain.TgPublishedYearly;
import ua.com.fielden.platform.swing.components.NotificationLayer.MessageType;
import ua.com.fielden.platform.swing.model.FrameTitleUpdater;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.model.IUmViewOwner;
import ua.com.fielden.platform.swing.model.callback.IPostInitCallback;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.swing.view.IEntityMasterCache;
import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * A frame holding a master view for entity {@link TgPublishedYearly}.
 * 
 * @author Developers
 * 
 */
public class TgPublishedYearlyMasterFrame extends BaseFrame {

    private final TgPublishedYearlyMasterModel model;
    private final TgPublishedYearlyMasterView view;

    public TgPublishedYearlyMasterFrame(//
    final IEntityProducer<TgPublishedYearly> entityProducer,//
            final IEntityMasterCache cache,//
            final TgPublishedYearly entityObject,//
            final ITgPublishedYearly companionObject,//
            final IValueMatcherFactory valueMatcherFactory,//
            final IUmViewOwner owner,//
            final IMasterDomainTreeManager masterManager,//
            final ICriteriaGenerator criteriaGenerator,//
            final IPostInitCallback<TgPublishedYearly, ITgPublishedYearly> postInitCallback) {
        setIconImage(ResourceLoader.getImage("images/tg-icon.png"));
        model = new TgPublishedYearlyMasterModel(entityProducer, cache, entityObject, companionObject, valueMatcherFactory, owner, new FrameTitleUpdater(this), masterManager, criteriaGenerator, postInitCallback);

        add(view = new TgPublishedYearlyMasterView(model));

        setTitle(view.getCaption() + ": " + model.getEntity().getKey() + " -- " + model.getEntity().getDesc());

        pack();
        RefineryUtilities.centerFrameOnScreen(this);
    }

    @Override
    protected void notify(final ICloseGuard guard) {
        view.notify(guard.whyCannotClose(), MessageType.WARNING);
    }

    public void enforceNewState() {
        model.getNewAction().actionPerformed(null);
    }

    public void enforceEditState() {
        model.getEditAction().actionPerformed(null);
    }

}