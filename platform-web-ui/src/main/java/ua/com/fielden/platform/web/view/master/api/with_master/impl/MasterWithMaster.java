package ua.com.fielden.platform.web.view.master.api.with_master.impl;

import static java.lang.String.format;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/**
 * An entity master that embeds a single Entity Master.
 *
 * @author TG Team
 *
 * @param <T>
 */
class MasterWithMaster<T extends AbstractEntity<?>> extends AbstractMasterWithMaster<T> {

    MasterWithMaster(final Class<T> entityType, final EntityMaster<?> entityMaster, final boolean shouldRefreshParentCentreAfterSave) {
        super(entityType, entityMaster.getEntityType(), shouldRefreshParentCentreAfterSave);
    }

    @Override
    protected String getAttributes(final Class<? extends AbstractEntity<?>> entityType, final String bindingEntityName, final boolean shouldRefreshParentCentreAfterSave) {
        final StringBuilder attrs = new StringBuilder();

        //////////////////////////////////////////////////////////////////////////////////////
        //// attributes should always be added with comma and space at the end, i.e. ", " ////
        //// this suffix is used to remove the last comma, which prevents JSON conversion ////
        //////////////////////////////////////////////////////////////////////////////////////
        attrs.append("{");
        attrs.append("\"entityType\":\"" + entityType.getName() + "\", "
                + "\"currentState\":\"EDIT\", "
                + "\"centreUuid\": this.centreUuid, "
                + "\"shouldRefreshParentCentreAfterSave\": " + shouldRefreshParentCentreAfterSave + ", ");
        attrs.append("}");

        return attrs.toString();
    }

    @Override
    protected String getElementName(final Class<? extends AbstractEntity<?>> entityType) {
        return format("'tg-%s-master'", entityType.getSimpleName());
    }

    @Override
    protected String getImportUri(final Class<? extends AbstractEntity<?>> entityType) {
        return format("'/master_ui/%s'", entityType.getName());
    }

}
