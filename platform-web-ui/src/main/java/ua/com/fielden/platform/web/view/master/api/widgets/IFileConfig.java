package ua.com.fielden.platform.web.view.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.widgets.file.IFileConfig0;

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
public interface IFileConfig<T extends AbstractEntity<?>> extends IFileConfig0<T> {
    /**
     * This declaration indicates that an asynchronous validation to a corresponding property should be skipped.
     * This should be done for optimisation reasons only in relation to properties that have heavy validation.
     * It should be understood the actual validation would anyway take place upon entity saving.
     *
     * @param matcher
     * @return
     */
    IFileConfig0<T> skipValidation();
}
