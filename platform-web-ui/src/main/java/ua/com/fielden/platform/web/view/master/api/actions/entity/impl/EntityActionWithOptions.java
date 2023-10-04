package ua.com.fielden.platform.web.view.master.api.actions.entity.impl;

import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.interfaces.IExecutable;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.impl.AbstractAction;

public class EntityActionWithOptions extends AbstractAction implements IRenderable, IExecutable{

    private final List<DefaultEntityAction> actions = new ArrayList<>();

    public EntityActionWithOptions(final DefaultEntityAction action, final DefaultEntityAction... optionalActions) {
        super("", "components/tg-option-button/tg-option-button");
        actions.add(action);
        actions.addAll(listOf(optionalActions));
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        return null;
    }

    @Override
    public JsCode code() {
        return new JsCode(actions.stream().map(action -> action.code().toString()).reduce((rest, curr) -> rest + curr).orElse(""));
    }

    @Override
    public DomElement render() {
        final DomElement button = new DomElement(this.actionComponentName());
        for(int idx = 0; idx < actions.size(); idx++) {
            final DefaultEntityAction action = actions.get(idx);
            final DomElement actionElement = action.render().attr("slot", "option-item").attr("action-type", "menuitem");
            if (idx == 0 ) {
                actionElement.attr("default-option", true);
            }
            button.add(actionElement);
        }
        return button;
    }
}
