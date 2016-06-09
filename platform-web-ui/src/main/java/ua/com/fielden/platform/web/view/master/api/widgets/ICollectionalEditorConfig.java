package ua.com.fielden.platform.web.view.master.api.widgets;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.ISkipValidation;
import ua.com.fielden.platform.web.view.master.api.widgets.collectional.ICollectionalEditorConfig0;

/**
 *
 * A configuration for a widget to edit small collectional properties (entities) that should be represented as association list of entities.
 * <p>
 * In case of HTML this should be <code>iron-list</text> wrapper with local search capability.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ICollectionalEditorConfig<T extends AbstractEntity<?>> extends ICollectionalEditorConfig0<T>, ISkipValidation<ICollectionalEditorConfig0<T>> {
}
