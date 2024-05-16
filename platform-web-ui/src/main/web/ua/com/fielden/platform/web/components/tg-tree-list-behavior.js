import { searchRegExp } from '/resources/editors/tg-highlighter.js';

const calculateNumberOfOpenedItems = function (entity, from, count) {
    let length = 0;
    if (entity && entity.entity.hasChildren && entity.opened) {
        const children = (typeof from !== 'undefined' && typeof count !== 'undefined') ? 
            entity.children.slice(from, from + count) : entity.children
        children.forEach(child => {
            if (child.visible) {
                length += calculateNumberOfOpenedItems(child) + 1;
                length += child.additionalInfoNodes.length;
            }
        });
    }
    return length;
};

const getParentsPath = function (entity) {
    const path = [];
    let parent = entity;
    while (parent) {
        path.push(parent.entity);
        parent = parent.parent;
    }
    return path.reverse();
};

const getChildrenToAdd = function (entity, shouldFireLoad, expandAll, from, count) {
    const childrenToAdd = [];
    if (entity.opened && entity.entity.hasChildren) {
        let subChildrenToAdd;
        if (entity.children.length === 1 && entity.children[0].loaderIndicator && shouldFireLoad) {
            this.fire("tg-load-subtree", {parentPath: getParentsPath(entity), loadAll: expandAll});
            subChildrenToAdd = entity.children;
        } else if (typeof from !== 'undefined' && typeof count !== 'undefined') {
            subChildrenToAdd = entity.children.slice(from , from + count);
        } else {
            subChildrenToAdd = entity.children
        }
        childrenToAdd.push(...composeChildren.bind(this)(subChildrenToAdd, shouldFireLoad, expandAll));
    }
    return childrenToAdd;
};

const composeChildren = function (entities, shouldFireLoad, expandAll) {
    const list = [];
    entities.filter(entity => entity.visible).forEach(entity => {
        list.push(entity);
        list.push(...entity.additionalInfoNodes);
        list.push(...getChildrenToAdd.bind(this)(entity, shouldFireLoad, expandAll));
    });
    return list;
};

const generateChildrenModel = function (children, parentEntity, additionalInfoCb) {
    return children.map( child => {
        const parent = {
            entity: child,
            parent: parentEntity,
            opened: false,
            visible: true,
            highlight: false,
            selected: false,
            over: false
        };
        parent.entity.__model = () => parent;
        if (child.hasChildren) {
            if (child.children && child.children.length > 0) {
                parent.children = generateChildrenModel(child.children, parent, additionalInfoCb);
            } else {
                parent.children = [generateLoadingIndicator(parent)];
            }
        }
        parent.additionalInfoNodes = (additionalInfoCb ? additionalInfoCb(parent) : []).map(entity => {
            entity.relatedTo = parent;
            entity.over = false;
            entity.selected = false;
            return entity;
        });
        return parent;
    });
};


const makeParentVisible = function (entity) {
    let parent = entity.parent;
    while (parent) {
        parent.visible = true;
        parent = parent.parent;
    }
};

/**
 * Returns the oldest closed ancestor of specified entity.
 * 
 * @param {Object} entity - treeItem for which the oldest closed ancestor should be find.
 */
const firstClosedParentFromTop = function (entity) {
    const parents = [];
    let parent = entity && entity.parent;
    while (parent) {
        parents.unshift(parent);
        parent = parent.parent;
    }
    return parents.find(item => !item.opened);
}

/**
 * Expands all ancestors of specified entity.
 * 
 * @param {Object} entity - the entity which ancestors should be expanded. 
 */
const expandAncestors = function (entity) {
    let parent = entity && entity.parent;
    while (parent) {
        parent.opened = true;
        parent = parent.parent;
    }
};

const hasMatchedAncestor = function (entity) {
    let parent = entity.parent;
    while (parent) {
        if (parent.visible && parent.highlight) {
            return true;
        }
        parent = parent.parent;
    }
    return false;
}

const wasLoaded = function (entity) {
    return entity.entity.hasChildren && entity.children && entity.children.length > 0 && !(entity.children.length === 1 && entity.children[0].loaderIndicator);
};

const generateLoadingIndicator = function (parent) {
    const loaderIndicator = {
        entity: {
            key: "Loading data...",
            hasChildren: false,
        },
        opened: false,
        visible: true,
        highlight: false,
        parent: parent,
        additionalInfoNodes: [],
        loaderIndicator: true,
        selected: false,
        over: false
    };
    loaderIndicator.entity.__model = () => loaderIndicator;
    return loaderIndicator;
};

const refreshTree = function (from, to) {
    const props = ["opened", "highlight", "selected"];
    const actFrom = from || 0;
    const actTo = to || this._entities.length;
    for (let idx = actFrom; idx < actTo; idx++) {
        if (this.isEntityRendered(idx)) {
            props.forEach(prop => this.notifyPath("_entities." + idx + "." + prop)); 
        }
    }
};

export const TgTreeListBehavior = {

    properties: {

        /**
         * Represents tree model
         */
        model: {
            type: Array,
            observer: "_modelChanged"
        },
        
        /**
         * This is a callback that supplies additional information nodes. This additional nodes are needed to keep tree item 
         * node hight the same throught the whole tree. It is important because iron-list requires all node to have the same 
         * hight in order render list node correctly. This callback will return list of items those will be related to passed
         * in entity. Then this additional nodes will be rendered under the "related to" node. 
         */
        additionalInfoCb: Function,

        /**
         * The last text that was used to find tree items in tree
         */
        lastSearchText: {
            type: String,
            value: "",
            notify: true
        },

        /**
         * Currently matched item, among _matchedTreeItems, to which tree view was scrolled to.
         */
        currentMatchedItem: {
            type: Object,
            value: null,
            notify: true,
            observer: "_curentMatchedItemChanged"
        },

        leftOffset: {
            type: Number,
            value: 0
        },
        
        /**
         * The tree model that holds some visual specific properties and is created from model.
         */
        _treeModel: {
            type: Array
        },

        /**
         * Represents the linear tree model.
         */
        _entities: {
            type: Array
        },
        
        _lastFilterText: {
            type: String,
            value: ""
        },

        _lastSearchText: {
            type: String,
            value: ""
        },

        _matchedTreeItems: {
            type: Array,
            value: () => []
        }

    },
    
    observers: ["_modelChanged(model.*)"],

    filter: function (text) {
        this._lastFilterText = text;
        this._filterSubTree(searchRegExp(text), !!text, this._treeModel, true);
        this.splice("_entities", 0, this._entities.length, ...composeChildren.bind(this)(this._treeModel, true));
        this.async(refreshTree.bind(this), 1);
    },

    /**
     * Searches the tree item with key that matches the specified text.
     * 
     * @param {String} text - pattern to find among tree items of treeModel.
     */
    find: function (text) {
        this._lastSearchText = text;
        this._matchedTreeItems = this._find(searchRegExp(text), !!text, this._treeModel);
        this.splice("_entities", 0, this._entities.length, ...composeChildren.bind(this)(this._treeModel, true));
        this.lastSearchText = text;
        this.currentMatchedItem = null;
        this.async(() => {
            refreshTree.bind(this)();
            this.currentMatchedItem = this._matchedTreeItems[0];
            this.scrollToItem(this.currentMatchedItem, true);
        }, 1);
    },

    goToNextMatchedItem: function () {
        this.goToMatchedItem(1);
    },

    goToPreviousMatchedItem: function () {
        this.goToMatchedItem(-1);
    },

    goToMatchedItem: function (inc) {
        const matchedItemIndex = this._matchedTreeItems.indexOf(this.currentMatchedItem);
        let nextMatchedItem = null;
        if (matchedItemIndex >= 0) {
            const nextIdx = matchedItemIndex + inc < 0 ? this._matchedTreeItems.length - 1 : 
                        (matchedItemIndex + inc >= this._matchedTreeItems.length ? 0 : matchedItemIndex + inc);
            nextMatchedItem = this._matchedTreeItems[nextIdx];
        } else {
            if (this._matchedTreeItems.length > 0) {
                nextMatchedItem = this._matchedTreeItems[0];
            } else  {
                nextMatchedItem = null;
            }
        }
        const topClosedParent = firstClosedParentFromTop(nextMatchedItem);
        if (nextMatchedItem && topClosedParent) {
            const idx = this._entities.indexOf(topClosedParent);
            if (idx >= 0) {
                expandAncestors(nextMatchedItem);
                this.splice("_entities", idx + 1 + topClosedParent.additionalInfoNodes.length, 0, ...getChildrenToAdd.bind(this)(topClosedParent, true, true));
            }
        }
        this.async(() => {
            refreshTree.bind(this)();
            this.scrollToItem(nextMatchedItem, false);
            this.currentMatchedItem = nextMatchedItem;
        });
    },

    expandAll: function () {
        const entities = this._entities.filter(entity => entity.parent === null && entity.entity.hasChildren);
        entities.forEach(entity => {
            this.expandSubTree(entity);
        });
        this.splice("_entities", 0, this._entities.length, ...composeChildren.bind(this)(this._treeModel, true, false));
        this.async(refreshTree.bind(this), 1);
    },

    expandSubTree: function(parentItem, refreshLoaded) {
        if (parentItem.entity.hasChildren) {
            parentItem.opened = true;
            if (refreshLoaded && parentItem.entity.subtreeRefreshable) {
                parentItem.children = [generateLoadingIndicator(parentItem)];
            }
            parentItem.children.forEach(treeEntity => {
                if (treeEntity.entity.hasChildren && (refreshLoaded || wasLoaded(treeEntity))) {
                    this.expandSubTree(treeEntity, refreshLoaded);
                }
            });
        }
    },

    expandSubTreeView: function (parentItem, refreshLoaded) {
        const idx = this._entities.indexOf(parentItem);
        if (idx >= 0 && parentItem.entity.hasChildren) {
            const numOfItemsToDelete = calculateNumberOfOpenedItems(parentItem);
            this.expandSubTree(parentItem, refreshLoaded);
            this.notifyPath("_entities." + idx + ".opened", true);
            this.splice("_entities", idx + 1 + parentItem.additionalInfoNodes.length, numOfItemsToDelete, ...getChildrenToAdd.bind(this)(parentItem, true, true));
            this.async(refreshTree.bind(this), 1);
        }
    },

    collapseSubTree: function (parentItem) {
        if (parentItem.entity.hasChildren) {
            if (parentItem.entity.subtreeRefreshable) {
                parentItem.children = [generateLoadingIndicator(parentItem)];
            } else {
                parentItem.children.forEach(child => this.collapseSubTree(child));
            }
            parentItem.opened = false;
        }
    },

    collapseAll: function () {
        const entities = this._entities.slice().filter(entity => entity.parent === null);
        entities.forEach(entity => {
            this.collapseSubTree(entity);
        });
        this._entities = composeChildren.bind(this)(this._treeModel, true, false);
        this.async(refreshTree.bind(this), 1);
    },

    collapseSubTreeView: function(parentItem) {
        const idx = this._entities.indexOf(parentItem);
        if (parentItem.entity.hasChildren && parentItem.opened) {
            const numOfItemsToDelete = calculateNumberOfOpenedItems(parentItem);
            this.collapseSubTree(parentItem);
            this.notifyPath("_entities." + idx + ".opened", false);
            this.splice("_entities", idx + 1 + parentItem.additionalInfoNodes.length, numOfItemsToDelete, ...getChildrenToAdd.bind(this)(parentItem, true, false));
            this.async(refreshTree.bind(this), 1);
        }
    },

    collapseSubTreeViewExceptParent: function(parentItem) {
        const idx = this._entities.indexOf(parentItem);
        if (parentItem.entity.hasChildren && parentItem.opened) {
            const numOfItemsToDelete = calculateNumberOfOpenedItems(parentItem);
            parentItem.children.forEach(item => this.collapseSubTree(item));
            this.splice("_entities", idx + 1 + parentItem.additionalInfoNodes.length, numOfItemsToDelete, ...getChildrenToAdd.bind(this)(parentItem, true, false));
            this.debounce("refreshTree", refreshTree.bind(this));
        }
    },
    
    reloadSubtreeFull: function (idx, entity) {
        if (entity.entity.hasChildren) {
            const numOfItemsToDelete = calculateNumberOfOpenedItems(entity);
            entity.children = [generateLoadingIndicator(entity)];
            if (!entity.opened) {
                this.set("_entities." + idx + ".opened", true);
            }
            this.splice("_entities", idx + 1 + entity.additionalInfoNodes.length, numOfItemsToDelete, ...getChildrenToAdd.bind(this)(entity, false, false));
            this.fire("tg-load-subtree", {parentPath: getParentsPath(entity), loadAll: true});
        }
    },
    
    removeSubtreeFull: function (idx, entity) {
        if (entity.entity.hasChildren) {
            if (entity.opened) {
                const numOfItemsToDelete = calculateNumberOfOpenedItems(entity);
                entity.children = [generateLoadingIndicator(entity)];
                this.splice("_entities", idx + 1 + entity.additionalInfoNodes.length, numOfItemsToDelete, ...getChildrenToAdd.bind(this)(entity, true, false));
            }
        }
    },

    toggle: function (idx) {
        const entity = this._entities[idx];
        if (entity) {
            if (entity.opened) {
                const numOfOpenedItems = calculateNumberOfOpenedItems(entity);
                this.set("_entities." + idx + ".opened", false);
                this.splice("_entities", idx + 1 + entity.additionalInfoNodes.length, numOfOpenedItems);
            } else {
                this.set("_entities." + idx + ".opened", true);
                this.splice("_entities", idx + 1 + entity.additionalInfoNodes.length, 0, ...getChildrenToAdd.bind(this)(entity, true, false));
            }
            refreshTree.bind(this)(idx);
        }
    },

    setSelected: function (idx, selected) {
        const entity = this._entities[idx];
        if (entity && entity.selected !== selected) {
            this.set("_entities." + idx + ".selected", selected);
            entity.additionalInfoNodes && entity.additionalInfoNodes.forEach((item, additionalInfoIdx) => {
                this.set("_entities." + (additionalInfoIdx + idx + 1) + ".selected", selected);
            });
            this.fire("tg-tree-item-selected", {entity: entity, select: selected});
        }
    },

    setOver: function (idx, over) {
        const entity = this._entities[idx];
        if (entity) {
            this.set("_entities." + idx + ".over", over);
            entity.additionalInfoNodes && entity.additionalInfoNodes.forEach((item, additionalInfoIdx) => {
                this.set("_entities." + (additionalInfoIdx + idx + 1) + ".over", over);
            });
        }
    },

    itemStyle: function (entity) {
        let paddingLeft = this.leftOffset || 0;
        let parent = entity.entity ? entity.parent : entity.relatedTo.parent;
        while (parent) {
            paddingLeft += 32;
            parent = parent.parent;
        }
        return "padding-left: " + paddingLeft + "px";
    },
    
    _filterSubTree: function (text, notEmpty, subtree, expand) {
        subtree.forEach(treeEntity => {
            if (treeEntity.entity.key.search(text) >= 0) {
                treeEntity.visible = true;
                treeEntity.highlight = notEmpty ? true : false;
                makeParentVisible(treeEntity);
            } else if (hasMatchedAncestor(treeEntity)) {
                treeEntity.visible = true;
                treeEntity.highlight = false;
            } else {
                treeEntity.visible = false;
                treeEntity.highlight = false;
            }
            if (treeEntity.entity.hasChildren && wasLoaded(treeEntity)) {
                treeEntity.opened = expand;
                this._filterSubTree(text, notEmpty, treeEntity.children, expand);
            }
        });
    },

    /**
     * Highlights tree items in subtree and their children those matches the specified text. 
     * Also it expands ancestors if some of their child matches the specified text.  
     *   
     * @param {String} text - text to find
     * @param {Array} subtree - list of items to search in
     * @param {Boolen} notEmpty - determines whether search text is notEmpty
     * 
     * returns the list of matched items.
     */
    _find: function (text, notEmpty, subtree) {
        let matchedItems = [];
        subtree.forEach(treeEntity => {
            if (notEmpty && treeEntity.entity.key.search(text) >= 0) {
                treeEntity.highlight = true;
                matchedItems.push(treeEntity);
                expandAncestors(treeEntity);
            } else {
                treeEntity.highlight = false;
            }
            if (treeEntity.entity.hasChildren && wasLoaded(treeEntity)) {
                matchedItems.push(...this._find(text, notEmpty, treeEntity.children));
            }
        });
        return matchedItems;
    },

    _regenerateModel: function () {
        this._treeModel = generateChildrenModel(this.model, null, this.additionalInfoCb); 
        this._entities = composeChildren.bind(this)(this._treeModel, true, false);
    },

    /**
     * Reacts on changes of tree model and updates list model.
     */
    _modelChanged: function (change) {
        if (change.path === "model") {
            this._regenerateModel();
            if (this._lastFilterText) {
                this.filter(this._lastFilterText);
            }
            if (this._lastSearchText) {
                this.find(this._lastSearchText);
            }
        } else if (change.path && change.path.endsWith("children")) {
            this._childrenModelChanged(change);
        } else if (change.path && change.path.endsWith("splices")) {
            this._childrenSplices(change);
        }
    },

    _childrenModelChanged: function(change) {
        const path = change.path.substring(0, change.path.lastIndexOf(".")).replace("model", "_treeModel").replace(/#/g, "");
        const parentItem = this.get(path);
        const modelIdx = this._entities.indexOf(parentItem);
        if (parentItem) {
            const numOfItemsToDelete = calculateNumberOfOpenedItems(parentItem);
            parentItem.children = generateChildrenModel(change.value, parentItem, this.additionalInfoCb);
            this._lastFilterText && this._filterSubTree(searchRegExp(this._lastFilterText), !!this._lastFilterText, parentItem.children, false);
            this.fire("tg-tree-model-changed", parentItem);
            this.splice("_entities", modelIdx + 1 + parentItem.additionalInfoNodes.length, numOfItemsToDelete, ...getChildrenToAdd.bind(this)(parentItem, true, false));
            this.resizeTree();
        }
    },

    _childrenSplices: function (change) {
        const path = change.path.substring(0, change.path.lastIndexOf(".children.splices")).replace("model", "_treeModel").replace(/#/g, "");
        const parentItem = this.get(path);
        if (parentItem) {
            change.value.indexSplices.forEach(splice => {
                const indexForSplice = this._entities.indexOf(parentItem.children[splice.index]);
                const numOfItemsToDelete = calculateNumberOfOpenedItems(parentItem, splice.index, splice.removed.length);
                parentItem.children.splice(splice.index, splice.removed.length, ...generateChildrenModel(splice.object.slice(splice.index, splice.index + splice.addedCount), parentItem, this.additionalInfoCb));
                this._lastFilterText && this._filterSubTree(searchRegExp(this._lastFilterText), !!this._lastFilterText, parentItem.children.slice(splice.index, splice.index + splice.addedCount), false);
                this.fire("tg-tree-model-changed", parentItem);
                this.splice("_entities", indexForSplice, numOfItemsToDelete, ...getChildrenToAdd.bind(this)(parentItem, true, false, splice.index, splice.addedCount));
                this.resizeTree();
            });
        }
    },

    _curentMatchedItemChanged: function (newValue, oldValue) {
        if (newValue) {
            this.setOver(this._entities.indexOf(newValue), true);
        } 
        if (oldValue){
            this.setOver(this._entities.indexOf(oldValue), false);
        }
    },

    /**
     * Resizes the tree component.
     */
    resizeTree: function () {
        this.notifyResize();
    },

    /**
     * Determines whether entity specified with index was rendered or not
     */
    isEntityRendered: function (index) {
        return false;
    },

    /**
     * Scrolls current view to the specified item if the the specified item is out of bounds of visible items.
     * 
     * @param {Object} treeItem - tree node to which view should be scrolled to.
     * @param {Boolean} force - forces to scroll to item even if the specified item is in bounds of visible items.
     */
    scrollToItem: function (treeItem, force) {

    }
};