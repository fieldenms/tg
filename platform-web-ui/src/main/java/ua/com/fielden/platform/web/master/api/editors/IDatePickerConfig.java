package ua.com.fielden.platform.web.master.api.editors;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.helpers.IAlso;
import ua.com.fielden.platform.web.master.api.helpers.ILayoutConfig;

/**
* A configuration for an date picker component (without a time portion) that gets associated with a property of type <code>Date</code> that resembles date only.
*
* As a note, the actual <code>input</code> field could have date and step attributes assigned.
* Such as <code>type="datetime-local"</code> and <code>step=86400</code> for a one day increment on the associated spinner (tested in Chrome).
*
* @author TG Team
*
* @param <T>
*/
public interface IDatePickerConfig<T extends AbstractEntity<?>> extends IAlso<T>, ILayoutConfig {

}
