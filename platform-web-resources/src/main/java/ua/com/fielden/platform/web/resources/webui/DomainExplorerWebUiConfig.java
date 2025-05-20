package ua.com.fielden.platform.web.resources.webui;

import com.google.inject.Injector;
import ua.com.fielden.platform.domain.metadata.DomainExplorer;
import ua.com.fielden.platform.domain.metadata.DomainExplorerInsertionPoint;
import ua.com.fielden.platform.ui.menu.metadata.MiDomainExplorer;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.metadata.DomainExplorerInsertionPointMaster;

import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;

public class DomainExplorerWebUiConfig {

    public final EntityCentre<DomainExplorer> centre;

    public static DomainExplorerWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new DomainExplorerWebUiConfig(injector, builder);
    }

    private DomainExplorerWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        centre = createCentre(injector);
        builder.register(centre);

        builder.register(createDomainExplorerInsertionPointMaster(injector));
    }

    private EntityMaster<DomainExplorerInsertionPoint> createDomainExplorerInsertionPointMaster(final Injector injector) {
        return new EntityMaster<>(DomainExplorerInsertionPoint.class,
                null, new DomainExplorerInsertionPointMaster(),injector);
    }

    /**
     * Creates entity centre for {@link DomainExplorer}.
     *
     * @param injector
     * @return created entity centre
     */
    private EntityCentre<DomainExplorer> createCentre(final Injector injector) {
        final EntityCentreConfig<DomainExplorer> ecc = EntityCentreBuilder.centreFor(DomainExplorer.class)
                .runAutomatically()
                .hideEgi()
                .addProp("this").order(1).asc().minWidth(100)
                .addInsertionPoint(
                        action(DomainExplorerInsertionPoint.class)
                             .withContext(context().withSelectionCrit().build())
                             .withNoParentCentreRefresh()
                             .build(),
                         InsertionPoints.ALTERNATIVE_VIEW)
                .build();

        final EntityCentre<DomainExplorer> entityCentre = new EntityCentre<>(MiDomainExplorer.class, "MiDomainExplorer", ecc, injector, null);
        return entityCentre;
    }
}
