package ua.com.fielden.platform.web.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.helpers.IAlso;
import ua.com.fielden.platform.web.master.api.helpers.ILayoutConfig;

/**
 *
 * A configuration for a widget to represent a file upload/download entry.
 * So, this is not really a widget for representing a property of type <code>File</code>, but actually
 * <p>
 * A custom HTML5 widget needs to be developed to support file upload/download functionality.
 * Refer <a href="https://github.com/fieldenms/tg/issues/125">Issue #125</a> for more details.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IFileConfig<T extends AbstractEntity<?>> extends IAlso<T> {

}
