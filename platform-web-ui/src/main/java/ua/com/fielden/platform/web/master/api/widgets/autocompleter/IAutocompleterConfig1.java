package ua.com.fielden.platform.web.master.api.widgets.autocompleter;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.actions.IPropertyActionConfig;
import ua.com.fielden.platform.web.master.api.helpers.IAlso;
import ua.com.fielden.platform.web.master.api.helpers.ILayoutConfig;

public interface IAutocompleterConfig1<T extends AbstractEntity<?>> extends IAlso<T>, ILayoutConfig, IPropertyActionConfig<T> {
}