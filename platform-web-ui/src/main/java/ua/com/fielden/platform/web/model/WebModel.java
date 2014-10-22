package ua.com.fielden.platform.web.model;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.web.WebUtils;
import ua.com.fielden.platform.web.action.AbstractWebAction;

/**
 * Represents the contract for web UI models.
 *
 * @author TG Team
 *
 */
public class WebModel {

    private final Set<String> properties = new HashSet<>();
    private final Set<AbstractWebAction<?>> actions = new HashSet<>();
    private final Set<String> injectables = new LinkedHashSet<>();
    private static String appSpecificResourcePath;

    protected WebModel() {
        // add default (and necessary) injectables: $scope (perhaps other stuff?)
        injectables.add("$scope");
    }

    /**
     * Adds property to the web ui model.
     *
     * @param prop
     * @return
     */
    protected final WebModel addProperty(final String prop) {
        properties.add(prop);
        return this;
    }

    /**
     * Adds the instance of {@link AbstractWebAction} to the model. One can add several actions with the same functional entity class.
     *
     * @param action
     * @return
     */
    protected final WebModel addAction(final AbstractWebAction<?> action) {
        actions.add(action);
        injectables.add(action.getFuncEntityClass().getSimpleName());
        return this;
    }

    private String createListOfInjectablesWithQuotes() {
        return "\"" + StringUtils.join(injectables, "\", \"") + "\"";
    }

    private String createListOfInjectablesWithoutQuotes() {
        return StringUtils.join(injectables, ", ");
    }

    public String generate() {
        final StringBuilder sb = new StringBuilder();
        System.out.println("WebUtils.angularModulePath(this) == " + WebUtils.angularModulePath(this, appSpecificResourcePath));

        sb.append("define(['log', '" + WebUtils.angularModulePath(this, appSpecificResourcePath) + ".js'], function(log, " + WebUtils.angularModuleName(this) + ") { \n");

        final Set<Class<?>> registered = new HashSet<Class<?>>();
        for (final AbstractWebAction<?> action : actions) {
            final Class<?> funcEntityClass = action.getFuncEntityClass();
            if (!registered.contains(funcEntityClass)) {
                sb.append("		" + WebUtils.angularModuleName(this) + ".factory(\"" + funcEntityClass.getSimpleName() + "\", function($resource) { \n");
                sb.append("		return $resource(\"users/SU/" + funcEntityClass.getSimpleName() + "\", {}, { \n");
                sb.append("				action: { \n");
                sb.append("					method: 'POST' \n");
                sb.append("				} \n");
                sb.append("			}); \n");
                sb.append("		}); \n");

                registered.add(funcEntityClass);
            }
        }

        sb.append("		" + WebUtils.angularModuleName(this) + ".controller(\"" + WebUtils.controllerName(this) + "\", [" + createListOfInjectablesWithQuotes() + ", function( \n"
                + createListOfInjectablesWithoutQuotes() + ") { \n"
                + createPropInitialisers()
                + createFuncInitialisers()
                + "		}]); \n");

        sb.append("	return " + WebUtils.angularModuleName(this) + "; \n");
        sb.append("}); \n");

        return sb.toString();
    }

    private String createFuncInitialisers() {
        final StringBuilder sb = new StringBuilder();

        for (final AbstractWebAction<?> action : actions) {
            sb.append(createFuncInitialiser(action));
        }
        return sb.toString();
    }

    private String createFuncInitialiser(final AbstractWebAction<?> action) {
        final StringBuilder sb = new StringBuilder();

        sb.append("$scope.run = function() { \n");
        sb.append(action.preAction());
        sb.append(createAction(action.postAction(), action.onError()));
        sb.append("}; \n");
        return sb.toString();
    }

    private String createAction(final String postAction, final String onError) {
        final StringBuilder sb = new StringBuilder();

        sb.append(" functionalEntity.$action(function(result) { \n");
        sb.append(" 	// result is a JavaScript representation of the appropriate Java class named Result. \n");
        sb.append(" 	if (!result.ex) { \n");
        sb.append(postAction);
        // sb.append(" 		$scope.pageData.page = result.instance.page; ");
        sb.append(" 	} else { \n");
        sb.append(onError);
        // sb.append(" 		alert(result.message); "); // TODO need to provide somehow the way to return "result.message" as an "error"
        sb.append(" 	} \n");
        sb.append(" }, function(error) { \n");
        sb.append(onError);
        // sb.append(" 	alert(error); ");
        // sb.append(" 	$scope.pageData.page = emptyPage; ");
        sb.append(" }); \n");

        return sb.toString();
    }

    private String createPropInitialisers() {
        final StringBuilder sb = new StringBuilder();
        sb.append("		$scope.model = {}; \n");
        for (final String prop : properties) {
            sb.append("			$scope.model." + prop + " = null; \n");
        }
        return sb.toString();
    }

    public static void setAppSpecificResourcePath(final String appSpecificResourcePath) {
	WebModel.appSpecificResourcePath = appSpecificResourcePath;
    }
}
