package ua.com.fielden.platform.web.view.master.api.actions.entity.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.actions.impl.AbstractAction;

public class EntityActionWithOptions extends AbstractAction {

    private final List<AbstractAction> actions = new ArrayList<>();

    public EntityActionWithOptions(final MasterActions masterAction, final MasterActions... otherOptions) {
        super(masterAction.name(), "/resources/components/tg-dropdown-switch");
        // TODO Auto-generated constructor stub
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        // TODO Auto-generated method stub
        return null;
    }

}
