package ua.com.fielden.platform.web.view.master.api.actions.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.IExecutable;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

/**
 * The base implementation box for generic information for all actions based on <i>functional entities</i>.
 *
 * The information includes <code>functionalEntityType</code> type, <code>enabledWhen</code> parameter, <code>shortDesc</code> etc.
 *
 * All action implementations (entity-, property-actions) should be based on this one and should be extended by action-specific configuration data.
 *
 * @author TG Team
 *
 */
public abstract class AbstractFunctionalAction extends AbstractAction implements IExecutable {
    private final Class<? extends AbstractEntity<?>> functionalEntityType;
    private IPreAction preAction;
    private IPostAction postActionSuccess, postActionError;
    private final String indent = "\t\t    ";

    /**
     * Creates {@link AbstractFunctionalAction} from <code>functionalEntityType</code> type and other parameters.
     *
     * @param functionalEntityType
     * @param propertyName
     */
    public AbstractFunctionalAction(final String name, final String actionComponentPath, final Class<? extends AbstractEntity<?>> functionalEntityType) {
        super(name, actionComponentPath);

        this.functionalEntityType = functionalEntityType;
    }

    protected Class<? extends AbstractEntity<?>> functionalEntityType() {
        return functionalEntityType;
    }

    public void setPreAction(final IPreAction preAction) {
        this.preAction = preAction;
    }

    public void setPostActionSuccess(final IPostAction postActionSuccess) {
        this.postActionSuccess = postActionSuccess;
    }

    public void setPostActionError(final IPostAction postActionError) {
        this.postActionError = postActionError;
    }

    @Override
    public JsCode code() {
        final String code =
                indent + "self.actions['" + this.name() + "'] = {\n" + //
                indent + "    user: self.user,\n" + //
                indent + "    entitytype: '" + this.functionalEntityType().getName() + "',\n" + //
                indent + "    shortDesc: '" + this.shortDesc() + "',\n" + //
                (this.longDesc() == null ? "" : indent + "    longDesc: '" + this.longDesc() + "',\n") + //
                (this.icon() == null ? "" : indent + "    icon: '" + this.icon() + "',\n") + //
                indent + "    enabledStates: [" + this.enabledStatesString() + "],\n" + //
                indent + "    preAction: function() {\n" + //
                indent + "        var functionalEntity = {id:null, version:0};\n" + //
                indent + "        var masterEntity = self.currEntity;\n" + //
                indent + "        " + this.preAction.build().toString() + "\n" + //
                // TODO provide convenient API for setting values during preAction building
                // "        functionalEntity.parentEntity = { val: self.currEntity.get('key'), origVal: null };\n" + //
                indent + "        return functionalEntity;\n" + //
                indent + "    },\n" + //
                indent + "    postActionSuccess: function(entity) {\n" + //
                indent + "        console.log('postActionSuccess entity', entity);\n" + //
                indent + "        " + this.postActionSuccess.build().toString() + "\n" + //
                indent + "    },\n" + //
                indent + "    postActionError: function(resultWithError) {\n" + //
                indent + "        console.log('postActionError resultWithError', resultWithError);\n" + //
                indent + "        " + this.postActionError.build().toString() + "\n" + //
                indent + "    }\n" + //
                indent + "};\n";//

        return new JsCode(code);
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();

        final String actionSelector = "actions['" + this.name() + "']";

        attrs.put("preAction", "{{" + actionSelector + ".preAction}}");
        attrs.put("postActionSuccess", "{{" + actionSelector + ".postActionSuccess}}");
        attrs.put("postActionError", "{{" + actionSelector + ".postActionError}}");

        return attrs;
    }
}
