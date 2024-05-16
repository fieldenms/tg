import {TgReflector} from '/app/tg-reflector.js';

const states = {
    's0': (entity, template, idx, reflector, titles) => {
        if (!template) {
            return createCompositeTitleWithoutTemplate(entity, titles, reflector);
        } else if (template && template.length === 1 && template[0] === 'z') {
            return composeDefaultValueObject(entity, reflector, titles);
        } else if (template && template[idx] === '#') {
            return 's1';
        } else {
            throw {msg: `Unknown template symbol: ${template[idx]} in template: ${template} at ${idx} position`}
        }
    },
    's1': (entity, template, idx, reflector, titles) => {
        return parseNumberAndReturnState(entity, template, idx, reflector, titles, 's2');
    },
    's2': (entity, template, idx, reflector, titles) => {
        if (template[idx] === 't') {
            const entityType = entity.type();
            titles[titles.length - 1].title = entityType.prop(titles[titles.length - 1].keyName).title();
            return 's3'
        } else if (template[idx] === 'v') {
            const name = titles[titles.length - 1].keyName;
            titles[titles.length - 1].value = reflector.tg_toString(entity.get(name), entity.type(), name);
            if (template.length - 1 === idx) {
                return;
            }
            return 's7';
        } else {
            throw {msg: `'t' or 'v' should be at ${idx} in ${template}`};
        }
    },
    's3': (entity, template, idx, reflector, titles) => {
        return parseValueAndReturnState(entity, template, idx, reflector, titles,'s4');
    },
    's4': (entity, template, idx, reflector, titles) => {
        return parseNumberSignAndReturnState(template, idx, 's5');
    },
    's5': (entity, template, idx, reflector, titles) => {
        return parseNumberAndReturnState(entity, template, idx, reflector, titles, 's6');
    },
    's6': (entity, template, idx, reflector, titles) => {
        if (template[idx] === 't') {
            const entityType = entity.type();
            titles[titles.length - 1].title = entityType.prop(titles[titles.length - 1].keyName).title();
            return 's3';
        } else {
            throw {msg: `'t' should be at ${idx} in ${template}`};
        }
    },
    's7': (entity, template, idx, reflector, titles) => {
        if (template[idx] === 's') {
            entity.type();
            titles[titles.length - 1].separator = entity.type().compositeKeySeparator();
            return 's8';
        } else {
            throw {msg: `'s' should be at ${idx} in ${template}`};
        }
    },
    's8': (entity, template, idx, reflector, titles) => {
        return parseNumberSignAndReturnState(template, idx, 's9');
    },
    's9':  (entity, template, idx, reflector, titles) => {
        return parseNumberAndReturnState(entity, template, idx, reflector, titles, 's10');
    },
    's10': (entity, template, idx, reflector, titles) => {
        return parseValueAndReturnState(entity, template, idx, reflector, titles,'s7');
    }
}

function parseValueAndReturnState(entity, template, idx, reflector, titles, state) {
    if (template[idx] === 'v') {
        const name = titles[titles.length - 1].keyName;
        titles[titles.length - 1].value = reflector.tg_toString(entity.get(name), entity.type(), name);
        if (template.length - 1 === idx) {
            return;
        }
        return state;
    } else {
        throw {msg: `'v' should be at ${idx} in ${template}`};
    }
}

function parseNumberSignAndReturnState (template, idx, state) {
    if (template[idx] === '#') {
        return state;
    } else {
        throw {msg: `'#' should be at ${idx} in ${template}`};
    }
}

function parseNumberAndReturnState (entity, template, idx, reflector, titles, state){
    const keyNumberIdx = +template[idx];
    if (!isNaN(keyNumberIdx)) {
        const entityType = entity.type();
        const keyName = entityType.compositeKeyNames()[keyNumberIdx - 1];
        if (keyName) {
            titles.push({keyName: keyName});
            return state;
        } else {
            throw {msg: `Key with index: ${keyNumberIdx} specified at ${idx} does not exist in the ${entity.type().fullClassName()} entity`};
        }
    } else {
        throw {msg: `${template[idx]} at ${idx} should be a number in ${template}.`};
    }
}

export function composeEntityValue (entity, template) {
    if (entity.type().isCompositeEntity()) {
        return createCompositeTitle(entity, template, new TgReflector());
    }
    return createSimpleTitle(entity, true);
}

export function composeDefaultEntityValue(entity) {
    if (entity.type().isCompositeEntity()) {
        const titles = [];
        createCompositeTitleWithoutTemplate(entity, titles, new TgReflector());
        return titles;
    }
    return createSimpleTitle(entity, true);
}

export function composeDefaultUnconvertedEntityValue(entity) {
    if (entity.type().isCompositeEntity()) {
        const titles = [];
        createCompositeTitleWithoutTemplate(entity, titles);
        return titles;
    }
    return createSimpleTitle(entity, false);
}

function createCompositeTitle (entity, template, reflector) {
    const titles = [];
    let idx = 0;
    let state = states['s0'](entity, template, idx, reflector, titles);
    while(state) {
        idx += 1;
        state = states[state](entity, template, idx, reflector, titles);
    }
    return titles;
}

function composeDefaultValueObject(entity, reflector, titles) {
    const entityType = entity.type();
    const compositeKeySeparator = entityType.compositeKeySeparator();
    entityType.compositeKeyNames().forEach(keyName => {
        if (entity.get(keyName) !== null) {
            titles.push({
                value: reflector.tg_toString(entity.get(keyName), entity.type(), keyName),
                separator: compositeKeySeparator
            });
        }
    });
    if (titles.length > 0) {
        delete titles[titles.length - 1].separator;
    }
}

function createCompositeTitleWithoutTemplate (entity, titles, reflector) {
    const entityType = entity.type();
    entityType.compositeKeyNames().forEach(keyName => {
        if (entity.get(keyName) !== null) {
            titles.push({
                title: entityType.prop(keyName).title(),
                value: reflector ? reflector.tg_toString(entity.get(keyName), entity.type(), keyName) : entity.get(keyName),
                propertyName: keyName,
                type: entity.type()
            });
        }
    });
    if (titles.length === 1) {
        delete titles[0].title;
    }
}

function createSimpleTitle (entity, convert) {
    return [{
        value: convert ? entity.toString() : entity.get("key"),
        propertyName: "key",
        type: entity.type()
    }]; // entity never empty
}