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
        parent.additionalInfoNodes = additionalInfoCb(parent).map(entity => {
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

const refreshTree = function (treeList) {
    const props = ["opened", "highlight"];
    this._entities.forEach((entity, idx) => {
        if (treeList._isIndexRendered(idx)) {
            props.forEach(prop => this.notifyPath("_entities." + idx + "." + prop)); 
        }
    });
};

export const TgTreeListBehavior = {

    

};