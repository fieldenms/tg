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
        super("", "/resources/components/tg-dropdown-switch");
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
        actions.forEach(action -> button.add(action.render()));
        return button;
    }
}
