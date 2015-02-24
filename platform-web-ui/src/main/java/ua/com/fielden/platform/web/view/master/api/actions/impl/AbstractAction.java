package ua.com.fielden.platform.web.view.master.api.actions.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.IExecutable;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.EnabledState;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 * The base implementation box for generic information for all actions.
 *
 * The information includes <code>functionalEntityType</code> type, <code>enabledWhen</code> parameter, <code>shortDesc</code> etc.
 *
 * All action implementations (entity-, property-actions) should be based on this one and should be extended by action-specific configuration data.
 *
 * @author TG Team
 *
 */
public abstract class AbstractAction implements IExecutable {
    private final String name;
    private final Class<? extends AbstractEntity<?>> functionalEntityType;
    private final IPreAction preAction;
    private final IPostAction postActionSuccess, postActionError;
    private final EnabledState enabledState;
    private final String icon;
    private final String shortDesc;
    private final String longDesc;
    private final String actionComponentName;
    private final String actionComponentPath;

    /**
     * Creates {@link AbstractAction} from <code>functionalEntityType</code> type and other parameters.
     *
     * @param functionalEntityType
     * @param propertyName
     */
    public AbstractAction(final String name, final String actionComponentPath, final Class<? extends AbstractEntity<?>> functionalEntityType, final IPreAction preAction, final IPostAction postActionSuccess, final IPostAction postActionError, final EnabledState enabledState, final String icon, final String shortDesc, final String longDesc) {
        this.name = name;
        this.actionComponentName = AbstractWidget.extractNameFrom(actionComponentPath);
        this.actionComponentPath = actionComponentPath;
        this.functionalEntityType = functionalEntityType;
        this.preAction = preAction;
        this.postActionSuccess = postActionSuccess;
        this.postActionError = postActionError;
        this.enabledState = enabledState;
        this.icon = icon;
        this.shortDesc = shortDesc;
        this.longDesc = longDesc;
    }

    protected String name() {
        return name;
    }

    protected Class<? extends AbstractEntity<?>> functionalEntityType() {
        return functionalEntityType;
    }

    protected IPreAction preAction() {
        return preAction;
    }

    protected IPostAction postActionSuccess() {
        return postActionSuccess;
    }

    protected IPostAction postActionError() {
        return postActionError;
    }

    protected EnabledState enabledState() {
        return enabledState;
    }

    protected String icon() {
        return icon;
    }

    protected String shortDesc() {
        return shortDesc;
    }

    protected String longDesc() {
        return longDesc;
    }

    protected String actionComponentName() {
        return actionComponentName;
    }

    protected String actionComponentPath() {
        return actionComponentPath;
    }

    protected String enabledStatesString() {
        return EnabledState.ANY.equals(this.enabledState) ? "'EDIT', 'VIEW'" :
                EnabledState.EDIT.equals(this.enabledState) ? "'EDIT'" :
                        EnabledState.VIEW.equals(this.enabledState) ? "'VIEW'" : "'UNDEFINED'";
    }

    @Override
    public JsCode code() {
        final String code =
                "self.actions['" + this.name() + "'] = {\n" + //
                "    user: self.user,\n" + //
                "    entitytype: '" + this.functionalEntityType().getName() + "',\n" + //
                "    shortDesc: '" + this.shortDesc() + "',\n" + //
                (this.longDesc() == null ? "" : "    longDesc: '" + this.longDesc() + "',\n") + //
                (this.icon() == null ? "" : "    icon: '" + this.icon() + "',\n") + //
                "    enabledStates: [" + this.enabledStatesString() + "],\n" + //
                "    preAction: function() {\n" + //
                "        var functionalEntity = {id:null, version:0};\n" + //
                "        var masterEntity = self.currEntity;\n" + //
                "        " + this.preAction().build().toString() + "\n" + //
                // TODO provide convenient API for setting values during preAction building
                // "        functionalEntity.parentEntity = { val: self.currEntity.get('key'), origVal: null };\n" + //
                "        return functionalEntity;\n" + //
                "    },\n" + //
                "    postActionSuccess: function(entity) {\n" + //
                "        console.log('postActionSuccess entity', entity);\n" + //
                "        " + this.postActionSuccess().build().toString() + "\n" + //
                "    },\n" + //
                "    postActionError: function(resultWithError) {\n" + //
                "        console.log('postActionError resultWithError', resultWithError);\n" + //
                "        " + this.postActionError().build().toString() + "\n" + //
                "    }\n" + //
                "};\n\n";//

        return new JsCode(code);
    }
}
