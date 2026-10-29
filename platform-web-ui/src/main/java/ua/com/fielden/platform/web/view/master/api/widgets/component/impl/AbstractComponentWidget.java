package ua.com.fielden.platform.web.view.master.api.widgets.component.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.view.master.api.widgets.component.IComponentRefreshCondition;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;
import ua.com.fielden.platform.web.view.master.exceptions.EntityMasterConfigurationException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static ua.com.fielden.platform.reflection.Finder.isPropertyPresent;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;

/// The base widget for both ways of adding an application-provided web component to a master:
/// in place of an editor, with [ComponentWidget], or bound to the entity rather than to a property, with [EntityComponentWidget].
///
/// It records the title and description, the element name, the height and minimum height, the attributes, whether the component is blocked while the master entity is unsaved,
/// and the condition for refreshing the component after a validation of the master entity.
/// The title and description default to those of the property, which a component bound to the entity does not have.
/// The element name defaults to the last segment of the import path.
/// A declared height makes the component resizable, and is rendered as `height` together with `entity-type`, the entity type of the master,
/// under which, together with the element id, the client-side component contract remembers the height set by the user.
/// A declared minimum height is rendered as `min-height`.
/// The declared attributes are rendered after the attributes of a concrete widget, in the order of declaration.
/// An attribute has either a declared value, or is bound to the value of a property of the entity of the master.
///
/// Blocking is declared by default, and is rendered as the `block-when-unsaved` flag.
/// The modification state of the master is rendered regardless of the flag, so that it is available to the component also when blocking changes at run time:
/// `entity-modified` for committed changes, and `entity-edited` for edits in progress.
/// The client-side component contract combines these with the persistence of the bound entity to block interaction.
///
/// The condition for refreshing the component is not rendered: the master collects it under the key of the component, as it collects the selector of the action of the component.
/// Nothing is rendered for refreshing either: the client-side master asks the component to refresh with a method call, finding the component by that key.
///
/// The configuration is validated as it is declared, and errors are reported with [EntityMasterConfigurationException].
/// Attributes and texts are rendered into a single-quoted attribute of the master template, which is a JavaScript template literal,
/// so a straight single quote, a backtick and `${` cannot be rendered and are rejected.
///
public abstract class AbstractComponentWidget extends AbstractWidget {

    public static final String
        ERR_INVALID_IMPORT_PATH = "Invalid import path of %s: it should be a path relative to /resources/, without the .js extension, such as [components/tg-fuel-usage-chart].",
        ERR_INVALID_ELEMENT_NAME = "Invalid element name [%s] of %s: it should start with a lowercase letter, contain a hyphen, and consist of lowercase letters, digits, hyphens, dots and underscores.",
        ERR_INVALID_ATTR_NAME = "Invalid attribute name [%s] of %s: it should be in lowercase dash-case, such as [centre-uuid].",
        ERR_RESERVED_ATTR_NAME = "Attribute [%s] of %s is reserved by the platform.",
        ERR_UNBALANCED_BINDING = "The value of attribute [%s] of %s has an unbalanced binding expression: %s",
        ERR_ATTR_FOR_MISSING_PROPERTY = "Attribute [%s] of %s cannot be bound to property [%s.%s], which does not exist.",
        ERR_INVALID_HEIGHT = "Invalid %s [%s] of %s: it should be a positive number of pixels.",
        ERR_HEIGHT_LESS_THAN_MIN_HEIGHT = "The height [%s] of %s is less than its minimum height [%s].",
        ERR_NULL_REFRESH_CONDITION = "The refresh condition of %s is null.",
        ERR_NULL_TEXT = "The %s of %s is null.",
        ERR_UNRENDERABLE_TEXT = "The %s of %s contains a straight single quote, a backtick or ${, which cannot be rendered: [%s].";

    private static final Pattern IMPORT_PATH = Pattern.compile("[\\w@.-]+(/[\\w@.-]+)*");
    private static final Pattern ELEMENT_NAME = Pattern.compile("[a-z][a-z0-9._]*-[a-z0-9._-]*");
    private static final Pattern ATTR_NAME = Pattern.compile("[a-z][a-z0-9]*(-[a-z0-9]+)*");

    /// The names of attributes rendered by both component widgets, which cannot be declared.
    private static final Set<String> RESERVED_ATTR_NAMES = setOf(
            "debug", "id", "entity", "prop-title", "prop-desc", "property-action-index",
            "entity-type", "height", "min-height",
            "block-when-unsaved", "entity-modified", "entity-edited", "create-context-holder",
            "slot"); // the flex layout of a master assigns slots to its elements

    private final Class<? extends AbstractEntity<?>> entityType;
    private Optional<String> declaredTitle = empty();
    private Optional<String> declaredDesc = empty();
    private Optional<String> elementName = empty();
    private Optional<Integer> height = empty();
    private Optional<Integer> minHeight = empty();
    private final Map<String, String> attrs = new LinkedHashMap<>();
    private boolean blockWhenUnsaved = true;
    private Optional<Class<? extends IComponentRefreshCondition<?>>> refreshCondition = empty();

    /// Creates a widget for the component at `importPath`, which is resolved as `/resources/<importPath>.js`, in a master for `entityType`.
    ///
    protected AbstractComponentWidget(final String importPath, final Pair<String, String> titleDesc, final Class<? extends AbstractEntity<?>> entityType, final String propertyName) {
        super(validImportPath(importPath, entityType, propertyName), titleDesc, propertyName);
        this.entityType = entityType;
    }

    /// Declares the title of the component, rendered as `prop-title`, in place of the title of the property.
    ///
    public void withTitle(final String title) {
        validateText("title", title);
        this.declaredTitle = of(title);
    }

    /// Declares the description of the component, rendered as `prop-desc`, in place of the description of the property.
    ///
    public void withDesc(final String desc) {
        validateText("description", desc);
        this.declaredDesc = of(desc);
    }

    /// The declared title, or otherwise the title of the property, which is `null` for a component bound to the entity.
    ///
    @Override
    protected String title() {
        return declaredTitle.orElseGet(super::title);
    }

    /// The declared description, or otherwise the description of the property, which is `null` for a component bound to the entity.
    ///
    @Override
    protected String desc() {
        return declaredDesc.orElseGet(super::desc);
    }

    /// Overrides the element name that is otherwise derived from the import path.
    ///
    public void withElementName(final String elementName) {
        this.elementName = of(validElementName(elementName));
    }

    /// Declares the height of the component in pixels, which makes the component resizable.
    ///
    public void withHeight(final int height) {
        this.height = of(validHeight("height", height));
        validateHeightNotLessThanMinHeight();
    }

    /// Declares the minimum height of the component in pixels.
    ///
    public void withMinHeight(final int minHeight) {
        this.minHeight = of(validHeight("minimum height", minHeight));
        validateHeightNotLessThanMinHeight();
    }

    /// Declares an attribute of the component element.
    /// Attributes are rendered in the order of their first declaration.
    /// Declaring an attribute again replaces its value, which lets a declaration override an attribute set elsewhere, such as in a shared configuration.
    ///
    public void withAttr(final String name, final String value) {
        validateAttrName(name);
        validateText("value of attribute [%s]".formatted(name), value);
        if (countMatches(value, "[[") != countMatches(value, "]]") || countMatches(value, "{{") != countMatches(value, "}}")) {
            throw new EntityMasterConfigurationException(ERR_UNBALANCED_BINDING.formatted(name, description(), value));
        }
        attrs.put(name, value);
    }

    /// Declares an attribute of the component element that is bound to the value of property `propPath` of the entity of the master, which may be dot-notated.
    /// The value follows the entity, as attribute `entity` does, and is `null` while there is no entity.
    /// The attribute is ordered and replaced as one declared with [#withAttr(String, String)].
    ///
    public void withPropAttr(final String name, final String propPath) {
        validateAttrName(name);
        if (propPath == null || propPath.isEmpty() || !isPropertyPresent(entityType, propPath)) {
            throw new EntityMasterConfigurationException(ERR_ATTR_FOR_MISSING_PROPERTY.formatted(name, description(), entityType.getSimpleName(), propPath));
        }
        // the path is passed to the master as a string literal, as a binding path cannot be used for a property of an entity
        attrs.put(name, "[[_propertyValue(_currEntity, \"" + propPath + "\")]]");
    }

    /// Keeps the component interactive while the entity is unsaved.
    ///
    public void skipBlockingWhenUnsaved() {
        this.blockWhenUnsaved = false;
    }

    /// Declares the condition for refreshing the component after a validation of the master entity.
    ///
    public void refreshWhen(final Class<? extends IComponentRefreshCondition<?>> condition) {
        if (condition == null) {
            throw new EntityMasterConfigurationException(ERR_NULL_REFRESH_CONDITION.formatted(description()));
        }
        this.refreshCondition = of(condition);
    }

    /// The declared condition for refreshing the component after a validation of the master entity, if any.
    ///
    public Optional<Class<? extends IComponentRefreshCondition<?>>> refreshCondition() {
        return refreshCondition;
    }

    /// The names of attributes rendered by a concrete widget in addition to those of both component widgets, which cannot be declared.
    ///
    protected Set<String> additionalReservedAttrNames() {
        return setOf();
    }

    /// Validates `text`, described as `what` in error messages, for rendering as an attribute value.
    ///
    protected void validateText(final String what, final String text) {
        if (text == null) {
            throw new EntityMasterConfigurationException(ERR_NULL_TEXT.formatted(what, description()));
        }
        if (text.contains("'") || text.contains("`") || text.contains("${")) {
            throw new EntityMasterConfigurationException(ERR_UNRENDERABLE_TEXT.formatted(what, description(), text));
        }
    }

    /// The element name, which is validated when it is derived from the import path, as a declared element name may replace it.
    ///
    @Override
    protected String elementName() {
        return elementName.orElseGet(() -> validElementName(super.elementName()));
    }

    /// Adds the declared height and minimum height of the component to `elementAttrs`, as CSS lengths in pixels.
    /// The height is accompanied by the entity type of the master, which, together with the element id, identifies the height set by the user.
    ///
    protected void addHeightAttributes(final Map<String, Object> elementAttrs) {
        height.ifPresent(value -> {
            elementAttrs.put("entity-type", entityType.getName());
            elementAttrs.put("height", value + "px");
        });
        minHeight.ifPresent(value -> elementAttrs.put("min-height", value + "px"));
    }

    /// Adds the attributes for the unsaved state of the master entity to `elementAttrs`:
    /// the `block-when-unsaved` flag, which is rendered only if blocking is declared, and the modification state of the master.
    ///
    protected void addUnsavedStateAttributes(final Map<String, Object> elementAttrs) {
        elementAttrs.put("block-when-unsaved", blockWhenUnsaved); // a `false` boolean attribute is not rendered
        elementAttrs.put("entity-modified", "[[_bindingEntityModified]]");
        elementAttrs.put("entity-edited", "[[_editedPropsExist]]");
    }

    /// Adds the context of the master to `elementAttrs`: `create-context-holder`, the function of the master that creates the contexts of its property and entity actions.
    /// A component uses it to provide the master entity, including its unsaved changes, as the context of what it runs, such as an embedded centre.
    ///
    protected void addMasterContextAttribute(final Map<String, Object> elementAttrs) {
        elementAttrs.put("create-context-holder", "[[_createContextHolder]]");
    }

    @Override
    protected Map<String, Object> createCustomAttributes() {
        final Map<String, Object> customAttrs = super.createCustomAttributes();
        customAttrs.putAll(attrs);
        return customAttrs;
    }

    private void validateAttrName(final String name) {
        if (name == null || !ATTR_NAME.matcher(name).matches()) {
            throw new EntityMasterConfigurationException(ERR_INVALID_ATTR_NAME.formatted(name, description()));
        }
        if (RESERVED_ATTR_NAMES.contains(name) || additionalReservedAttrNames().contains(name)) {
            throw new EntityMasterConfigurationException(ERR_RESERVED_ATTR_NAME.formatted(name, description()));
        }
    }

    private int validHeight(final String what, final int value) {
        if (value <= 0) {
            throw new EntityMasterConfigurationException(ERR_INVALID_HEIGHT.formatted(what, value, description()));
        }
        return value;
    }

    private void validateHeightNotLessThanMinHeight() {
        if (height.isPresent() && minHeight.isPresent() && height.get() < minHeight.get()) {
            throw new EntityMasterConfigurationException(ERR_HEIGHT_LESS_THAN_MIN_HEIGHT.formatted(height.get(), description(), minHeight.get()));
        }
    }

    private String validElementName(final String name) {
        if (name == null || !ELEMENT_NAME.matcher(name).matches()) {
            throw new EntityMasterConfigurationException(ERR_INVALID_ELEMENT_NAME.formatted(name, description()));
        }
        return name;
    }

    private static String validImportPath(final String importPath, final Class<? extends AbstractEntity<?>> entityType, final String propertyName) {
        if (importPath == null || !IMPORT_PATH.matcher(importPath).matches() || importPath.endsWith(".js")) {
            throw new EntityMasterConfigurationException(ERR_INVALID_IMPORT_PATH.formatted(description(importPath, entityType, propertyName)));
        }
        return importPath;
    }

    /// Describes this component in error messages.
    ///
    private String description() {
        return description(importPath(), entityType, propertyName());
    }

    private static String description(final String importPath, final Class<? extends AbstractEntity<?>> entityType, final String propertyName) {
        return propertyName == null
               ? "component [%s] in the entity master for [%s]".formatted(importPath, entityType.getSimpleName())
               : "component [%s] for property [%s.%s]".formatted(importPath, entityType.getSimpleName(), propertyName);
    }

}
