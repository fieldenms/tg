package ua.com.fielden.platform.web.centre.api.impl.helpers;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.multi.IEntityMultiActionSelector;

/// Stub [IEntityMultiActionSelector] for testing.
/// Always selects the first sub-action.
///
public class PropertyActionSelectorForTest implements IEntityMultiActionSelector {

    @Override
    public int getActionFor(final AbstractEntity<?> entity) {
        return 0;
    }

}