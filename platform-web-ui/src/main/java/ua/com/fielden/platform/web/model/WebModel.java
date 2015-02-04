package ua.com.fielden.platform.web.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    protected WebModel() {
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
        return this;
    }

    /**
     * Returns the list of needed html imports for this model.
     *
     * @return
     */
    public List<String> getImports() {
        final List<String> imports = new ArrayList<>();
        if (!actions.isEmpty()) {
            imports.add("/resources/polymer/core-ajax/core-ajax.html");
        }
        return imports;
    }

    /**
     * Returns the list of actions to be included into web view.
     *
     * @return
     */
    public Set<AbstractWebAction<?>> getActions() {
        return Collections.unmodifiableSet(actions);
    }

    public String generate() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append(createProperties());
        sb.append(createActions());
        sb.append("}");
        return sb.toString();
    }

    private String createActions() {
        final StringBuilder sb = new StringBuilder();
        final AbstractWebAction<?>[] actionArray = actions.toArray(new AbstractWebAction[0]);
        for (int actionIndex = 0; actionIndex < actionArray.length; actionIndex++) {
            final AbstractWebAction<?> action = actionArray[actionIndex];
            sb.append(createAction(action, actionIndex));
        }
        return sb.toString();
    }

    private String createAction(final AbstractWebAction<?> action, final int actionIndex) {
        final StringBuilder sb = new StringBuilder();
        sb.append(createOnAction(action));
        sb.append(createOnResponse(action));
        sb.append(createOnError(action, actionIndex));
        return sb.toString();
    }

    private String createOnError(final AbstractWebAction<?> action, final int actionIndex) {
        final StringBuilder sb = new StringBuilder();
        sb.append("\tonError_" + action.hashCode() + ": function(e, detail, sender) {\n");
        sb.append("\t\t" + action.onError() + "\n");
        sb.append("\t}" + (actionIndex < actions.size() ? ",\n" : "\n"));
        return sb.toString();
    }

    private String createOnResponse(final AbstractWebAction<?> action) {
        final StringBuilder sb = new StringBuilder();
        sb.append("\tonResponse_" + action.hashCode() + ": function(e, detail, sender) {\n");
        sb.append("\t\t" + action.postAction() + "\n");
        sb.append("\t},\n");
        return sb.toString();
    }

    private String createOnAction(final AbstractWebAction<?> action) {
        final StringBuilder sb = new StringBuilder();
        sb.append("\tonAction_" + action.hashCode() + ": function() {\n");
        sb.append("\t\tvar actionRunner = this.$.id_" + action.hashCode() + ";\n");
        sb.append("\t\t" + action.preAction() + "\n");
        sb.append("\t\tactionRunner.body = JSON.stringify(functionalEntity);\n");
        sb.append("\t\tactionRunner.go();\n");
        sb.append("\t},\n");
        return sb.toString();
    }

    private String createProperties() {
        final StringBuilder sb = new StringBuilder();
        for (final String prop : properties) {
            sb.append("\t" + prop + ":  null" + (actions.isEmpty() ? "\n" : ",\n"));
        }
        return sb.toString();
    }
}
