package ua.com.fielden.platform.web.view.master.api.actions.impl;

import static java.lang.String.format;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;

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
    private final String indent = "                    ";

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
                wrap0("self.actions['%s'] = {", name(), () -> name()) + //
                wrap0("    user: self.user,") + //
                wrap0("    entitytype: '%s',", functionalEntityType(), () -> functionalEntityType().getName()) + //
                wrap0("    shortDesc: '%s',", shortDesc()) + //
                wrap1("    longDesc: '%s',", longDesc()) + //
                wrap1("    icon: '%s',", icon()) + //
                wrap0("    enabledStates: [%s],", enabledStatesString()) + //
                wrap0("    preAction: function() {") + //
                wrap0("        var functionalEntity = {id:null, version:0};") + //
                wrap0("        var masterEntity = self.currEntity;") + //
                wrap0("        functionalEntity.key = { val: 'NoMatter', origVal: null };") + //
                wrap0("        // THE PLACE FOR CUSTOM LOGIC:") + //
                wrap1("        %s", preAction, () -> preAction.build().toString()) + //
                //             TODO provide convenient API for setting values during preAction building
                //    "        functionalEntity.parentEntity = { val: self.currEntity.get('key'), origVal: null };") + //
                wrap0("        return functionalEntity;") + //
                wrap0("    },") + //
                wrap0("    postActionSuccess: function(entity) {") + //
                wrap0("        console.log('postActionSuccess entity', entity);") + //
                wrap1("        %s", postActionSuccess, () -> this.postActionSuccess.build().toString()) + //
                wrap0("    },") + //
                wrap0("    postActionError: function(resultWithError) {") + //
                wrap0("        console.log('postActionError resultWithError', resultWithError);") + //
                wrap1("        %s", postActionError, () -> postActionError.build().toString()) + //
                wrap0("    }") + //
                wrap0("};");//

        return new JsCode(code);
    }

    private String wrap0(final String code) {
        return indent + format(code) + "\n";
    }

    private String wrap0(final String code, final Object smth) {
        return wrap0(code, smth, () -> smth.toString());
    }

    /**
     * Wraps the code into indentation and line-ending symbols.
     *
     * <code>smth</code> is required parameter. If <code>null</code> -- {@link NullPointerException} will throw.
     *
     * @param code
     * @param smth
     * @param f
     * @return
     */
    private String wrap0(final String code, final Object smth, final Supplier<String> f) {
        if (smth == null) {
            throw new NullPointerException();
        }
        return wrap_1(code, f.get());
    }

    private String wrap1(final String code, final Object smth) {
        return wrap1(code, smth, () -> smth.toString());
    }

    /**
     * Wraps the code into indentation and line-ending symbols.
     *
     * <code>smth</code> is the optional parameter. If <code>null</code> -- all line will be disregarded.
     *
     * @param code
     * @param smth
     * @param f
     * @return
     */
    private String wrap1(final String code, final Object smth, final Supplier<String> f) {
        if (smth == null) {
            return "";
        }
        return wrap_1(code, f.get());
    }

    private String wrap_1(final String code, final String insertionCode) {
        return StringUtils.isEmpty(insertionCode) ? "" : indent + format(code, insertionCode) + "\n";
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
