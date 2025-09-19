package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.tooltip.IWithTooltip;

/// A contract to configure the EGI cell column so that its content wraps text automatically and the row height adjusts dynamically.
///
public interface IResultSetBuilder4bWordWrap<T extends AbstractEntity<?>> extends IWithTooltip<T> {

    /// Enables word wrapping in an EGI cell and automatically adjusts its height.
    ///
    IWithTooltip<T> withWordWrap();
}
