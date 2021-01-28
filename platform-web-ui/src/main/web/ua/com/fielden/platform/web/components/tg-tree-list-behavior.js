const calculateNumberOfOpenedItems = function (entity, from, count) {
    let length = 0;
    if (entity.entity.hasChildren && entity.opened) {
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
            return entity;
        });
        return parent;
    })
};


const makeParentVisible = function (entity) {
    let parent = entity.parent;
    while (parent) {
        parent.visible = true;
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
    return {
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
};

const refreshTree = function () {
    const props = ["opened", "highlight"];
    this._entities.forEach((entity, idx) => {
        if (this.isEntityRendered(idx)) {
            props.forEach(prop => this.notifyPath("_entities." + idx + "." + prop)); 
        }
    });
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
        }
    },
    
    observers: ["_modelChanged(model.*)"],

    filter: function (text) {
        this._lastFilterText = text;
        this._filterSubTree(text, this._treeModel, true);
        this.splice("_entities", 0, this._entities.length, ...composeChildren.bind(this)(this._treeModel, true));
        this.debounce("refreshTree", refreshTree.bind(this));
    },

    expandSubTree: function(parentItem, refreshLoaded) {
        parentItem.opened = true;
        if (parentItem.entity.hasChildren && refreshLoaded && parentItem.entity.subtreeRefreshable) {
            parentItem.children = [generateLoadingIndicator(parentItem)];
        }
        parentItem.children.forEach(treeEntity => {
            if (treeEntity.entity.hasChildren && (refreshLoaded || wasLoaded(treeEntity))) {
                this.expandSubTree(treeEntity, refreshLoaded);
            }
        });
    },

    expandSubTreeView: function (idx, parentItem) {
        if (parentItem.entity.hasChildren) {
            const numOfItemsToDelete = calculateNumberOfOpenedItems(parentItem);
            this.expandSubTree(parentItem, true);
            this.notifyPath("_entities." + idx + ".opened", true);
            this.splice("_entities", idx + 1 + parentItem.additionalInfoNodes.length, numOfItemsToDelete, ...getChildrenToAdd.bind(this)(parentItem, true, true));
            this.debounce("refreshTree", refreshTree.bind(this));
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

    collapseSubTreeView: function(idx, parentItem) {
        if (parentItem.entity.hasChildren && parentItem.opened) {
            const numOfItemsToDelete = calculateNumberOfOpenedItems(parentItem);
            parentItem.children.forEach(child => this.collapseSubTree(child));
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
                this.splice("_entities", idx + 1 + entity.additionalInfoNodes.length, calculateNumberOfOpenedItems(entity));
                this.set("_entities." + idx + ".opened", false);
            } else {
                this.set("_entities." + idx + ".opened", true);
                this.splice("_entities", idx + 1 + entity.additionalInfoNodes.length, 0, ...getChildrenToAdd.bind(this)(entity, true, false));
            }
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
        let paddingLeft = 0;
        let parent = entity.entity ? entity.parent : entity.relatedTo.parent;
        while (parent) {
            paddingLeft += 32;
            parent = parent.parent;
        }
        return "padding-left: " + paddingLeft + "px";
    },
    
    _filterSubTree: function (text, subtree, expand) {
        subtree.forEach(treeEntity => {
            if (treeEntity.entity.key.toLowerCase().search(text.toLowerCase()) >= 0) {
                treeEntity.visible = true;
                treeEntity.highlight = text ? true : false;
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
                this._filterSubTree(text, treeEntity.children, expand);
            }
        });
    },

    _regenerateModel: function () {
        this._treeModel = generateChildrenModel(this.model, null, this.additionalInfoCb); 
        this._entities = this._treeModel.slice();
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
            this._lastFilterText && this._filterSubTree(this._lastFilterText, parentItem.children, false);
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
                this._lastFilterText && this._filterSubTree(this._lastFilterText, parentItem.children.slice(splice.index, splice.index + splice.addedCount), false);
                this.fire("tg-tree-model-changed", parentItem);
                this.splice("_entities", indexForSplice, numOfItemsToDelete, ...getChildrenToAdd.bind(this)(parentItem, true, false, splice.index, splice.addedCount));
                this.resizeTree();
            });
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
    }
};