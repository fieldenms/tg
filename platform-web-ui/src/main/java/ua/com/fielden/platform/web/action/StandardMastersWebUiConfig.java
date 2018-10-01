package ua.com.fielden.platform.web.action;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.EntityExportAction.PROP_EXPORT_ALL;
import static ua.com.fielden.platform.entity.EntityExportAction.PROP_EXPORT_SELECTED;
import static ua.com.fielden.platform.entity.EntityExportAction.PROP_EXPORT_TOP;
import static ua.com.fielden.platform.entity.EntityExportAction.PROP_NUMBER;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.MOBILE;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.TABLET;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.cell;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutCellBuilder.layout;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.attachment.AttachmentPreviewEntityAction;
import ua.com.fielden.platform.attachment.AttachmentsUploadAction;
import ua.com.fielden.platform.attachment.producers.AttachmentPreviewEntityActionProducer;
import ua.com.fielden.platform.attachment.producers.AttachmentsUploadActionProducer;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.entity.EntityEditActionProducer;
import ua.com.fielden.platform.entity.EntityExportAction;
import ua.com.fielden.platform.entity.EntityExportActionProducer;
import ua.com.fielden.platform.entity.EntityNavigationAction;
import ua.com.fielden.platform.entity.EntityNavigationActionProducer;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.entity.EntityNewActionProducer;
import ua.com.fielden.platform.web.PrefDim;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.layout.api.impl.FlexLayoutConfig;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;
import ua.com.fielden.platform.web.view.master.api.with_master.impl.EntityManipulationMasterBuilder;
import ua.com.fielden.platform.web.view.master.api.with_master.impl.EntityNavigationMaster;
import ua.com.fielden.platform.web.view.master.attachments.AttachmentPreviewEntityMaster;
import ua.com.fielden.platform.web.view.master.attachments.AttachmentsUploadActionMaster;

/**
 * A set of factory methods for various standard platform-level entity masters such as Export to Excel.
 *
 * @author TG Team
 *
 */
public class StandardMastersWebUiConfig {
    public static final int MASTER_ACTION_DEFAULT_WIDTH = 80;
    public static final String MASTER_ACTION_CUSTOM_SPECIFICATION = "'margin: 10px', 'width: %spx'";
    public static final String MASTER_ACTION_SPECIFICATION = format(MASTER_ACTION_CUSTOM_SPECIFICATION, MASTER_ACTION_DEFAULT_WIDTH);

    private StandardMastersWebUiConfig() {}

    public static EntityMaster<EntityNewAction> createEntityNewMaster(final Injector injector) {
        return new EntityMaster<>(EntityNewAction.class,
                EntityNewActionProducer.class,
                new EntityManipulationMasterBuilder<EntityNewAction>()
                /*  */.forEntityWithSaveOnActivate(EntityNewAction.class)
                /*  */.withMaster(null) // the master instance is not passing here, this is generic implementation, and master type is calculated from selection criteria context
                /*  */.done(),
                injector);
    }

    public static EntityMaster<EntityEditAction> createEntityEditMaster(final Injector injector) {
        return new EntityMaster<>(EntityEditAction.class,
                EntityEditActionProducer.class,
                new EntityManipulationMasterBuilder<EntityEditAction>()
                /*  */.forEntityWithSaveOnActivate(EntityEditAction.class)
                /*  */.withMaster(null) // the master instance is not passing here, this is generic implementation, and master type is calculated from currentEntity context
                /*  */.done(),
                injector);
    }

    public static EntityMaster<EntityNavigationAction> createEntityNavigationMaster(final Injector injector) {
        return new EntityMaster<>(EntityNavigationAction.class,
                EntityNavigationActionProducer.class,
                new EntityNavigationMaster(EntityNavigationAction.class, true),
                injector);
    }

    public static EntityMaster<AttachmentPreviewEntityAction> createAttachmentPreviewMaster(final Injector injector) {
        return new EntityMaster<>(AttachmentPreviewEntityAction.class,
                AttachmentPreviewEntityActionProducer.class,
                new AttachmentPreviewEntityMaster(),
                injector);
    }

    public static EntityMaster<EntityExportAction> createExportMaster(final Injector injector) {
        final FlexLayoutConfig CELL_LAYOUT = layout().flex().end();

        final String layout = cell(
                cell(cell(CELL_LAYOUT))
               .cell(cell(CELL_LAYOUT))
               .cell(cell(CELL_LAYOUT))
               .cell(cell(CELL_LAYOUT), layout().withStyle("padding-left", "32px").end()),
               layout().withStyle("padding", "20px").end()).toString();

        final String MASTER_ACTION_LAYOUT_SPECIFICATION = "'horizontal', 'padding: 10px', 'wrap', 'justify-content: center'";
        final String buttonPanelLayout = format("[%s, [%s], [%s]]", MASTER_ACTION_LAYOUT_SPECIFICATION, MASTER_ACTION_SPECIFICATION, MASTER_ACTION_SPECIFICATION);
        final IMaster<EntityExportAction> masterConfig = new SimpleMasterBuilder<EntityExportAction>()
                .forEntity(EntityExportAction.class)
                .addProp(PROP_EXPORT_ALL).asCheckbox().also()
                .addProp(PROP_EXPORT_SELECTED).asCheckbox().also()
                .addProp(PROP_EXPORT_TOP).asCheckbox().also()
                .addProp(PROP_NUMBER).asSpinner()
                .also()
                .addAction(MasterActions.REFRESH)
                /*      */.shortDesc("CANCEL")
                /*      */.longDesc("Cancel action")
                .addAction(MasterActions.SAVE)
                /*      */.shortDesc("EXPORT")
                /*      */.longDesc("Start exporting")
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), buttonPanelLayout)
                .setLayoutFor(DESKTOP, Optional.empty(), layout)
                .setLayoutFor(TABLET, Optional.empty(), layout)
                .setLayoutFor(MOBILE, Optional.empty(), layout)
                .done();

        return new EntityMaster<>(EntityExportAction.class, EntityExportActionProducer.class, masterConfig, injector);
    }

    public static EntityMaster<AttachmentsUploadAction> createAttachmentsUploadMaster(
            final Injector injector,
            final PrefDim dims,
            final int fileSizeLimitKb,
            final String mimeType,
            final String... moreMimeTypes) {
        final IMaster<AttachmentsUploadAction> masterConfig = new AttachmentsUploadActionMaster(dims, fileSizeLimitKb, mimeType, moreMimeTypes);
        return new EntityMaster<>(AttachmentsUploadAction.class, AttachmentsUploadActionProducer.class, masterConfig, injector);
    }

    // TODO once it will be necessary, uncomment this code to implement generic EDIT / NEW actions with 'no parent centre refresh' capability:
//    public static EntityMaster<EntityNewActionWithNoParentCentreRefresh> createEntityNewMasterWithNoParentCentreRefresh(final Injector injector) {
//        return new EntityMaster<EntityNewAction>(EntityNewActionWithNoParentCentreRefresh.class,
//                EntityNewActionWithNoParentCentreRefreshProducer.class,
//                new EntityManipulationMasterBuilder<EntityNewActionWithNoParentCentreRefresh>()
//                /*  */.forEntityWithSaveOnActivate(EntityNewActionWithNoParentCentreRefresh.class)
//                /*  */.withMasterAndWithNoParentCentreRefresh(null) // the master instance is not passing here, this is generic implementation, and master type is calculated from selection criteria context
//                /*  */.done(),
//                injector);
//    }
//
//    public static EntityMaster<EntityEditActionWithNoParentCentreRefresh> createEntityEditMasterWithNoParentCentreRefresh(final Injector injector) {
//        return new EntityMaster<EntityEditActionWithNoParentCentreRefresh>(EntityEditActionWithNoParentCentreRefresh.class,
//                EntityEditActionWithNoParentCentreRefreshProducer.class,
//                new EntityManipulationMasterBuilder<EntityEditActionWithNoParentCentreRefresh>()
//                /*  */.forEntityWithSaveOnActivate(EntityEditActionWithNoParentCentreRefresh.class)
//                /*  */.withMasterAndWithNoParentCentreRefresh(null) // the master instance is not passing here, this is generic implementation, and master type is calculated from currentEntity context
//                /*  */.done(),
//                injector);
//    }
}
