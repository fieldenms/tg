package ua.com.fielden.platform.web.master.api.editors;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.helpers.IAlso;
import ua.com.fielden.platform.web.master.api.helpers.ILayoutConfig;

/**
 * A configuration for a widget to edit <code>BigDecimal</code> properties.
 * <p>
 * In case of HTML a corresponding widget should either be an <code>input</text> with <code>type="number"</code> and <code>step="0.01"</code>, or a custom component.
 *
 * @author TG Team
 *
 * @param <T>
 */

public interface IDecimalConfig<T extends AbstractEntity<?>> extends IAlso<T>, ILayoutConfig {

}
