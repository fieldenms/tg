package ua.com.fielden.platform.web.master.api.widgets.autocompleter;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.actions.IPropertyActionConfig;
import ua.com.fielden.platform.web.master.api.helpers.IAlso;

public interface IAutocompleterConfig2<T extends AbstractEntity<?>> extends IAlso<T>, IPropertyActionConfig<T> {
}