package ua.com.fielden.platform.web.view.master.metadata;

import static java.util.Optional.empty;
import static ua.com.fielden.platform.web.centre.EntityCentre.IMPORTS;
import static ua.com.fielden.platform.web.view.master.EntityMaster.ENTITY_TYPE;
import static ua.com.fielden.platform.web.view.master.EntityMaster.flattenedNameOf;
import static ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder.createImports;

import java.util.LinkedHashSet;
import java.util.Optional;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.domain.metadata.DomainExplorerInsertionPoint;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;

public class DomainExplorerInsertionPointMaster implements IMaster<DomainExplorerInsertionPoint> {

    private final IRenderable renderable;

    public DomainExplorerInsertionPointMaster () {

        final LinkedHashSet<String> importPaths = new LinkedHashSet<>();
        importPaths.add("components/tg-domain-explorer");
        importPaths.add("editors/tg-singleline-text-editor");
        importPaths.add("polymer/@polymer/iron-icons/iron-icons");
        importPaths.add("polymer/@polymer/paper-icon-button/paper-icon-button");
        final DomElement searchItemsText = new DomElement("div").attr("slot", "property-action").attr("hidden", "[[!_searchText]]").add(new InnerTextElement("[[_calcMatchedItems(_searchText, _matchedItemOrder, _matchedItemsNumber)]]"))
                .style("font-size: 0.9rem", "color:#757575", "margin-right: 8px");
        final DomElement prevButton = new DomElement("paper-icon-button").attr("slot", "property-action").attr("hidden", "[[!_searchText]]").attr("icon", "icons:expand-less").attr("on-tap", "_goToPrevMatchedItem")
                .style("width:24px","height:24px", "padding:4px", "color:#757575");
        final DomElement nextButton = new DomElement("paper-icon-button").attr("slot", "property-action").attr("hidden", "[[!_searchText]]").attr("icon", "icons:expand-more").attr("on-tap", "_goToNextMatchedItem")
                .style("width:24px","height:24px", "padding:4px", "color:#757575");

        final DomElement domainFilter = new DomElement("tg-singleline-text-editor")
                .attr("id", "domainFilter")
                .attr("class", "filter-element")
                .attr("slot", "filter-element")
                .attr("entity", "{{_currBindingEntity}}")
                .attr("original-entity", "{{_originalBindingEntity}}")
                .attr("previous-modified-properties-holder", "[[_previousModifiedPropertiesHolder]]")
                .attr("property-name", "domainFilter")
                .attr("validation-callback", "[[doNotValidate]]")
                .attr("prop-title", "Type to find domain type or property by title")
                .attr("prop-desc", "Finds domain types or properties that match the search text")
                .attr("current-state", "[[currentState]]")
                .attr("toaster", "[[toaster]]")
                .add(searchItemsText, prevButton, nextButton);

        final DomElement domainExplorerDom = new DomElement("tg-domain-explorer")
                .attr("id", "domainExplorer")
                .attr("entity", "{{_currBindingEntity}}")
                .attr("last-search-text", "{{_searchText}}")
                .attr("matched-item-order", "{{_matchedItemOrder}}")
                .attr("number-of-matched-items", "{{_matchedItemsNumber}}")
                .attr("on-tg-load-sub-domain", "_loadSubDomain")
                .add(domainFilter);

        final StringBuilder prefDimBuilder = new StringBuilder();
        prefDimBuilder.append("{'width': function() {return '100%'}, 'height': function() {return '100%'}, 'widthUnit': '', 'heightUnit': ''}");

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.js")
                .replace(IMPORTS, createImports(importPaths)
                        + "\nimport { TgEntityBinderBehavior } from '/resources/binding/tg-entity-binder-behavior.js';\n")
                .replace(ENTITY_TYPE, flattenedNameOf(DomainExplorerInsertionPoint.class))
                .replace("<!--@tg-entity-master-content-->", domainExplorerDom.toString())
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
        return "self.classList.add('layout');\n"
                + "self._searchText = '';\n"
                + "self._matchedItemOrder = 0;\n"
                + "self._matchedItemsNumber = 0;\n"
                + "self.classList.add('vertical');\n"
                + "self.classList.remove('canLeave');\n"
                + "self._showDataLoadingPromt = function (msg) {\n"
                + "    this.$.domainExplorer.lock = true;\n"
                + "    this._toastGreeting().text = msg;\n"
                + "    this._toastGreeting().hasMore = false;\n"
                + "    this._toastGreeting().showProgress = true;\n"
                + "    this._toastGreeting().msgHeading = 'Info';\n"
                + "    this._toastGreeting().isCritical = false;\n"
                + "    this._toastGreeting().show();\n"
                + "}.bind(self);\n"
                + "self._showDataLoadedPromt = function (msg) {\n"
                + "    this.$.domainExplorer.lock = false;\n"
                + "    this._toastGreeting().text = msg;\n"
                + "    this._toastGreeting().hasMore = false;\n"
                + "    this._toastGreeting().showProgress = false;\n"
                + "    this._toastGreeting().msgHeading = 'Info';\n"
                + "    this._toastGreeting().isCritical = false;\n"
                + "    this._toastGreeting().show();\n"
                + "}.bind(self);\n"
                +"//Locks/Unlocks tg-domain-explorer's lock layer during insertion point action activation\n"
                + "self.disableViewForDescendants = function () {\n"
                + "    TgEntityBinderBehavior.disableViewForDescendants.call(this);\n"
                + "    self._showDataLoadingPromt('Loading domain...');\n"
                + "}.bind(self);\n"
                + "self.enableViewForDescendants = function () {\n"
                + "    TgEntityBinderBehavior.enableViewForDescendants.call(this);\n"
                + "    self._showDataLoadedPromt('Loading completed successfully');\n"
                + "}.bind(self);\n"
                + "//Is Needed to sent data to server.\n"
                + "self._isNecessaryForConversion = function (propertyName) { \n"
                + "    return ['domainTypeName', 'loadedHierarchy', 'domainFilter', 'domainTypeHolderId', 'domainPropertyHolderId'].indexOf(propertyName) >= 0; \n"
                + "}; \n"
                + "self.$.domainFilter._onInput = function () {\n"
                + "    // clear domain explorer filter timer if it is in progress.\n"
                + "    this._cancelDomainExplorerFilterTimer();\n"
                + "    this._domainExplorerFilterTimer = this.async(this._findTreeItems, 500);\n"
                + "}.bind(self);\n"
                + "self._cancelDomainExplorerFilterTimer = function () {\n"
                + "    if (this._domainExplorerFilterTimer) {\n"
                + "        this.cancelAsync(this._domainExplorerFilterTimer);\n"
                + "        this._domainExplorerFilterTimer = null;\n"
                + "    }\n"
                + "}.bind(self);\n"
                + "self._findTreeItems = function () {\n"
                + "    this.$.domainExplorer.find(this.$.domainFilter._editingValue);\n"
                + "}.bind(self);\n"
                + "self._loadSubDomain = function (e) {\n"
                + "    this.save();\n"
                + "}.bind(self);\n"
                + "self._calcMatchedItems= function (_searchText, _matchedItemOrder, _matchedItemsNumber) {\n"
                + "    if (_searchText) {\n"
                + "        return _matchedItemOrder + ' / ' + _matchedItemsNumber;\n"
                + "    }\n"
                + "    return '';\n"
                + "}.bind(self);\n"
                + "self._goToPrevMatchedItem = function (e) {\n"
                + "    this.$.domainExplorer.goToPreviousMatchedItem();"
                + "}.bind(self);\n"
                + "self._goToNextMatchedItem = function (e) {\n"
                + "    this.$.domainExplorer.goToNextMatchedItem();"
                + "}.bind(self);\n";
    }

    @Override
    public IRenderable render() {
        return renderable;
    }

    @Override
    public Optional<Class<? extends IValueMatcherWithContext<DomainExplorerInsertionPoint, ?>>> matcherTypeFor(final String propName) {
        return empty();
    }

    @Override
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        throw new UnsupportedOperationException("Getting of action configuration is not supported.");
    }
}
