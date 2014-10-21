package ua.com.fielden.platform.web.model;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

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

    /**
     * Returns a functional name of the logic which is covered by this {@link WebModel}. Typically the web model should be named as with convention like
     * "CustomFunctionalityWebModel".
     *
     * @return
     */
    protected String functionalName() {
        final String className = className();
        return className.replaceAll("WebModel", "");
    }

    protected String angularModuleName() {
        return StringUtils.uncapitalize(functionalName()) + "Module";
    }

    protected String className() {
        return this.getClass().getSimpleName();
    }

    protected String jsClassName() {
        return functionalName() + "WebModel";
    }

    protected String controllerName() {
        return functionalName() + "Controller";
    }

    private String createListOfInjectablesWithQuotes() {
        return "\"" + StringUtils.join(injectables, "\", \"") + "\"";
    }

    private String createListOfInjectablesWithoutQuotes() {
        return StringUtils.join(injectables, ", ");
    }

    public String generate() {
        final StringBuilder sb = new StringBuilder();

        sb.append("define(['log', '/resources/custom/" + angularModuleName() + ".js'], function(log, " + angularModuleName() + ") { ");
        sb.append("	var " + jsClassName() + " = function() { ");
        sb.append("     this." + angularModuleName() + " = " + angularModuleName() + ";");

        final Set<Class<?>> registered = new HashSet<Class<?>>();
        for (final AbstractWebAction<?> action : actions) {
            final Class<?> funcEntityClass = action.getFuncEntityClass();
            if (!registered.contains(funcEntityClass)) {
                sb.append("		" + angularModuleName() + ".factory(\"" + funcEntityClass.getSimpleName() + "\", function($resource) { ");
                sb.append("		return $resource(\"users/SU/" + funcEntityClass.getSimpleName() + "\", {}, { ");
                sb.append("				action: { ");
                sb.append("					method: 'POST' ");
                sb.append("				} ");
                sb.append("			}); ");
                sb.append("		}); ");

                registered.add(funcEntityClass);
            }
        }

        sb.append("		" + angularModuleName() + ".controller(\"" + controllerName() + "\", [" + createListOfInjectablesWithQuotes() + ", function("
                + createListOfInjectablesWithoutQuotes() + ") {"
                + createPropInitialisers()
                + createFuncInitialisers()
                + "		}]);");

        sb.append("	};");
        sb.append("	return " + jsClassName() + "; ");
        sb.append("});");

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

        sb.append("$scope.run = function() { ");
        sb.append(action.preAction());
        sb.append(createAction(action.postAction(), action.onError()));
        sb.append("};");
        return sb.toString();
    }

    private String createAction(final String postAction, final String onError) {
        final StringBuilder sb = new StringBuilder();

        sb.append(" functionalEntity.$action(function(result) { ");
        sb.append(" 	// result is a JavaScript representation of the appropriate Java class named Result.\n ");
        sb.append(" 	if (!result.ex) { ");
        sb.append(postAction);
        // sb.append(" 		$scope.pageData.page = result.instance.page; ");
        sb.append(" 	} else { ");
        sb.append(onError);
        // sb.append(" 		alert(result.message); "); // TODO need to provide somehow the way to return "result.message" as an "error"
        sb.append(" 	} ");
        sb.append(" }, function(error) { ");
        sb.append(onError);
        // sb.append(" 	alert(error); ");
        // sb.append(" 	$scope.pageData.page = emptyPage; ");
        sb.append(" }); ");

        return sb.toString();
    }

    private String createPropInitialisers() {
        final StringBuilder sb = new StringBuilder();
        sb.append("		$scope.model = {};");
        for (final String prop : properties) {
            sb.append("			$scope.model." + prop + " = null;");
        }
        return sb.toString();
    }
}
