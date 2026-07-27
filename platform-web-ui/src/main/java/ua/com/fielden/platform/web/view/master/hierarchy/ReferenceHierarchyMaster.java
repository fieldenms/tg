package ua.com.fielden.platform.web.view.master.hierarchy;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchy;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.ref_hierarchy.ReferenceHierarchyWebUiConfig;
import ua.com.fielden.platform.web.view.master.api.IMaster;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static ua.com.fielden.platform.web.centre.EntityCentre.IMPORTS;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.view.master.EntityMaster.ENTITY_TYPE;
import static ua.com.fielden.platform.web.view.master.EntityMaster.flattenedNameOf;
import static ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder.createImports;

/**
 * Master implementation for reference hierarchy.
 *
 * @author TG Team
 *
 */
public class ReferenceHierarchyMaster implements IMaster<ReferenceHierarchy> {

    private final List<EntityActionConfig> actions = new ArrayList<>();
    private final IRenderable renderable;

    public ReferenceHierarchyMaster () {

        final LinkedHashSet<String> importPaths = new LinkedHashSet<>();
        importPaths.add("components/tg-reference-hierarchy");
        importPaths.add("editors/tg-singleline-text-editor");
        importPaths.add("editors/tg-boolean-editor");
        importPaths.add("actions/tg-ui-action");


        this.actions.add(EntityActionBuilder.editAction().withContext(context().withCurrentEntity().build())
                .icon("editor:mode-edit")
                .longDesc("Opens master for editing this entity")
                .withNoParentCentreRefresh()
                .build());
        this.actions.add(ReferenceHierarchyWebUiConfig.mkAction(context().withCurrentEntity().build()));

        final DomElement hierarchyFilter = new DomElement("tg-singleline-text-editor")
                .attr("id", "referenceHierarchyFilter")
                .attr("class", "filter-element")
                .attr("slot", "filter-element")
                .attr("entity", "{{_currBindingEntity}}")
                .attr("original-entity", "{{_originalBindingEntity}}")
                .attr("previous-modified-properties-holder", "[[_previousModifiedPropertiesHolder]]")
                .attr("property-name", "referenceHierarchyFilter")
                .attr("validation-callback", "[[doNotValidate]]")
                .attr("prop-title", "Type to filter reference hierarchy")
                .attr("prop-desc", "Display types or instances those matched entered text")
                .attr("current-state", "[[currentState]]")
                .attr("toaster", "[[toaster]]");

        final var activeOnlyTitleAndDesc = TitlesDescsGetter.getTitleAndDesc("activeOnly", ReferenceHierarchy.class);
        final DomElement activeOnlyEditor = new DomElement("tg-boolean-editor")
                .attr("id", "activeOnlyEditor")
                .attr("class", "active-only-editor")
                .attr("slot", "active-only-editor")
                .attr("entity", "{{_currBindingEntity}}")
                .attr("original-entity", "{{_originalBindingEntity}}")
                .attr("previous-modified-properties-holder", "[[_previousModifiedPropertiesHolder]]")
                .attr("property-name", "activeOnly")
                .attr("validation-callback", "[[doNotValidate]]")
                .attr("prop-title", activeOnlyTitleAndDesc.getKey())
                .attr("prop-desc", activeOnlyTitleAndDesc.getValue())
                .attr("current-state", "[[currentState]]")
                .attr("toaster", "[[toaster]]");

        final DomElement referenceHierarchyDom = new DomElement("tg-reference-hierarchy")
                .attr("id", "refrenceHierarchy")
                .attr("entity", "{{_currBindingEntity}}")
                .attr("on-tg-load-refrence-hierarchy", "_loadSubReferenceHierarchy")
                .attr("centre-uuid", "[[uuid]]")
                .add(hierarchyFilter)
                .add(activeOnlyEditor);

        //Generating action's DOM and JS functions
        final StringBuilder customActionObjects = new StringBuilder();
        final String prefix = ",\n";
        final int prefixLength = prefix.length();
        for (int actionIdx = 0; actionIdx < this.actions.size(); actionIdx++) {
            final EntityActionConfig action = this.actions.get(actionIdx);
            final FunctionalActionElement el = FunctionalActionElement.newEntityActionForMaster(action, actionIdx);
            importPaths.add(el.importPath());
            referenceHierarchyDom.add(el.render().attr("hidden", null).clazz("primary-action").attr("slot", "reference-hierarchy-action"));
            customActionObjects.append(prefix + el.createActionObject());
        }
        final String customActionObjectsString = customActionObjects.toString();

        final StringBuilder prefDimBuilder = new StringBuilder();
        prefDimBuilder.append("{'width': function() {return '50%'}, 'height': function() {return '70%'}, 'widthUnit': '', 'heightUnit': ''}");

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.js")
                .replace(IMPORTS, createImports(importPaths)+
                        "\nimport { TgEntityBinderBehavior } from '/resources/binding/tg-entity-binder-behavior.js';\n")
                .replace(ENTITY_TYPE, flattenedNameOf(ReferenceHierarchy.class))
                .replace("<!--@tg-entity-master-content-->", referenceHierarchyDom.toString())
                .replace("//generatedPrimaryActions", customActionObjectsString.length() > prefixLength ? customActionObjectsString.substring(prefixLength)
                        : customActionObjectsString)
                .replace("//@ready-callback", readyCallback())
                .replace("@prefDim", prefDimBuilder.toString())
                .replace("@noUiValue", "false")
                .replace("@saveOnActivationValue", "true");

        renderable = new IRenderable() {
            @Override
            public DomElement render() {
                return new InnerTextElement(entityMasterStr);
            }
        };
    }

    private String readyCallback() {
        return """
                self.classList.add('layout');
                self.classList.add('vertical');
                self.classList.remove('canLeave');
                self._showDataLoadingPromt = function (msg) {
                    this.$.refrenceHierarchy.lock = true;
                    this._toastGreeting().text = msg;
                    this._toastGreeting().hasMore = false;
                    this._toastGreeting().showProgress = true;
                    this._toastGreeting().msgHeading = 'Info';
                    this._toastGreeting().isCritical = false;
                    this._toastGreeting().show();
                }.bind(self);
                self._showDataLoadedPromt = function (msg) {
                    this.$.refrenceHierarchy.lock = false;
                    this._toastGreeting().text = msg;
                    this._toastGreeting().hasMore = false;
                    this._toastGreeting().showProgress = false;
                    this._toastGreeting().msgHeading = 'Info';
                    this._toastGreeting().isCritical = false;
                    this._toastGreeting().show();
                }.bind(self);
                //Locks/Unlocks tg-reference-hierarchy's lock layer during insertion point activation
                self.disableViewForDescendants = function () {
                    TgEntityBinderBehavior.disableViewForDescendants.call(this);
                    self._showDataLoadingPromt('Loading reference hierarchy...');
                }.bind(self);
                self.enableViewForDescendants = function () {
                    TgEntityBinderBehavior.enableViewForDescendants.call(this);
                    self._showDataLoadedPromt('Loading completed successfully');
                }.bind(self);
                //Need for security marix editors binding.
                self._isNecessaryForConversion = function (propertyName) {\s
                    return ['referenceHierarchyFilter','refEntityId', 'refEntityType', 'entityType', 'loadedHierarchy', 'pageSize', 'pageNumber', 'pageCount', 'loadedLevel'].indexOf(propertyName) >= 0;\s
                };\s
                self.$.referenceHierarchyFilter._onInput = function () {
                    // clear hierarchy filter timer if it is in progress.
                    this._cancelHierarchyFilterTimer();
                    this._hierarchyFilterTimer = this.async(this._filterHierarchy, 500);
                }.bind(self);
                self._cancelHierarchyFilterTimer = function () {
                    if (this._hierarchyFilterTimer) {
                        this.cancelAsync(this._hierarchyFilterTimer);
                        this._hierarchyFilterTimer = null;
                    }
                }.bind(self);
                self._filterHierarchy = function () {
                    this.$.refrenceHierarchy.filterHierarchy(this.$.referenceHierarchyFilter._editingValue);
                }.bind(self);
                self._loadSubReferenceHierarchy = function (e) {
                    this.save();
                }.bind(self);
                self.$.activeOnlyEditor.addEventListener('change', function (e) {
                    this.$.refrenceHierarchy.reload();
                }.bind(self));
                """;
    }

    @Override
    public Optional<Class<? extends IValueMatcherWithContext<ReferenceHierarchy, ?>>> matcherTypeFor(final String propName) {
        return empty();
    }

    @Override
    public IRenderable render() {
        return renderable;
    }

    @Override
    public Stream<EntityActionConfig> streamActionConfigs() {
        return actions.stream();
    }

    @Override
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        if (FunctionalActionKind.PRIMARY_RESULT_SET == actionKind) {
            return this.actions.get(actionNumber);
        }
        throw new UnsupportedOperationException("Getting of action configuration is not supported.");
    }

}
