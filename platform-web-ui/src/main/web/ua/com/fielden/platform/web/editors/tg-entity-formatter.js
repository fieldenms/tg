import { TgReflector } from '/app/tg-reflector.js';

import antlr4 from '/resources/polymer/antlr4/dist/antlr4.web.mjs';
import CompositeEntityFormatLexer from '/resources/template/CompositeEntityFormatLexer.js';
import CompositeEntityFormatParser from '/resources/template/CompositeEntityFormatParser.js';
import CompositeEntityFormatListener from '/resources/template/CompositeEntityFormatListener.js';

/**
 * Determines key member name for 'strNumber' (starting from 1).
 */
function getKeyMemberName (entity, strNumber, reflector) {
    const keyNumberIdx = +strNumber;
    if (!isNaN(keyNumberIdx)) {
        const entityType = entity.type();
        const keyName = entityType.compositeKeyNames()[keyNumberIdx - 1];
        if (keyName) {
            return keyName;
        } else {
            throw { msg: `Key with index ${strNumber} does not exist in ${reflector.simpleClassName(entity.type().fullClassName())} entity type.` };
        }
    } else {
        throw {msg: `${strNumber} should be a number.`};
    }
}

export function composeEntityValue (entity, template) {
    if (entity.type().isCompositeEntity()) {
        return createCompositeTitle(entity, template, new TgReflector());
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

/**
 * Removes default ANTLR console-based error reporting and adds our custom.
 */
function customiseErrorHandling (processor) {
    processor.removeErrorListeners(); // removes console error reporter
    processor.addErrorListener({
        syntaxError: (recognizer, offendingSymbol, line, column, msg, err) => {
            throw { msg: `Error at position ${column}: ${msg}.` }; // re-throw the error object with the specified position in a message
        }
    });
};

/**
 * Splits 'str' into two substrings: the first one ends just before the first occurence of 'separator', the second one - after it.
 * If 'separator' doesn't occur in 'str', the first substring is 'str' and the second one - an empty string.
 */
function splitOnFirst(str, separator) {
    const idx = str.indexOf(separator);
    if (idx < 0) {
        return [str, ''];
    } else {
        return [str.substring(0, idx), str.substring(idx + 1)]
    }
}

/**
 * Determines gluing separator between two dot-notated key members [prevMemberPath, currMemberPath] of 'entity'.
 */
function determineSeparator (prevMemberPath, currMemberPath, entity, reflector) {
    if (!prevMemberPath) {
        return undefined;
    } else {
        // Function to determine common path prefix between two dot-notated paths.
        const commonPrefix = (path1, path2, acc) => {
            if (path1 === '' || path2 === '') {
                return acc;
            } else {
                // Function to determine prefix for path before first dot or to the end of string if there is none.
                const [path1Head, path1Tail] = splitOnFirst(path1, '.');
                const [path2Head, path2Tail] = splitOnFirst(path2, '.');
                if (path1Head === path2Head) {
                    return commonPrefix(path1Tail, path2Tail, acc === '' ? path1Head : acc + '.' + path1Head);
                } else {
                    return acc;
                }
            }
        };
        // Function to get penult property name for 'prop'; including '' (aka root) for non-dot-notated ones.
        const penultPropOf = prop => reflector.isDotNotated(prop) ? prop.substring(0, prop.lastIndexOf('.')) : '';
        return entity.get(commonPrefix(penultPropOf(currMemberPath), penultPropOf(prevMemberPath), '')).type().compositeKeySeparator();
    }
};

/**
 * Creates an array of { separator, title?, value } objects with ordered key member (including nested) representations
 *  for the 'entity' according to 'template'.
 *
 * For #itv templates there will always be 'Title: Value' pairs glued together with ' ' separator (no separator for first member).
 * For #iv[s] templates only 'Value' will be present but glued together with appropriate (see entity definition) separator (no separator for first member too).
 *
 * See specification in 'tg-entity-editor.html' web test suites (and 'tg-entity-formatter.html' too).
 *
 * Example entities:
 * [[Locomotive] / [Electrical Equipment]] [[Batteries]:[Lithium-Ion]]
 * [[Wagon] / [Electrical Equipment]][[][]]
 * [[Wagon][]] [[Batteries]:[Lead-Acid]]
 *
 * Example templates:
 * '' = '#1tv#2tv'
 * 'z' = '#1v#2v' = '#1vs#2v'
 * '#1.2tv#2tv'
 * '#1.2v#2v' = '#1.2vs#2v'
 * '#2.2v#1.2v#1.1v#2.1v'
 * '#2.2tv#1.2tv#1.1tv#2.1tv'
 *
 * Notes:
 * 1. tv and v[s] templates can not be mixed;
 * 2. 's' token in v[s] can be omitted; preserve it for clarity if needed
 */
function createCompositeTitle (entity, template, reflector) {
    // Create lexer, parser and tree from 'template'.
    const input = template;
    const chars = new antlr4.InputStream(input);
    const lexer = new CompositeEntityFormatLexer(chars);
    customiseErrorHandling(lexer);
    const tokens = new antlr4.CommonTokenStream(lexer);
    const parser = new CompositeEntityFormatParser(tokens);
    customiseErrorHandling(parser);
    const tree = parser.template();

    // Define state for a custom ANTLR listener.
    const members = []; // resulting key member objects
    let currMemberPath, prevMemberPath, currMemberValue; // currently / previously processed key member dot-notation paths; and current value

    // Define custom ANTLR listener.
    class Listener extends CompositeEntityFormatListener {

        /**
         * Root 'template' rule processor that handles '' and 'z' template cases.
         */
        exitTemplate (ctx) {
            // Function to add all resulting key member objects to 'members' for all first-level keys.
            const pushAllKeys = titlePart => createCompositeTitle( // works recursively by generating and processing #i[t]v or #iv template
                entity,
                entity.type().compositeKeyNames()
                    .map((n, index) => `#${index + 1}${titlePart}v`)
                    .join(''),
                reflector
            ).forEach(member => members.push(member));

            if (ctx.children.length === 1) { // <EOF> -- means '' template
                pushAllKeys('t'); // generates '#itv' template
                if (members.length === 1) {
                    delete members[0].title;
                }
            } else if (ctx.zed) {
                pushAllKeys(''); // generates '#iv' template
            }
        }

        /**
         * 'number' rule processor that handles [currMemberPath, currMemberValue] determination (current / previous key member dot-notation paths; and current value's string representation).
         */
        exitNumber (ctx) {
            // Function to construct dot-notation path to a key member and its value's string representation.
            const constructPathAndValue = (value, numbers, acc) => {
                const convertedValue = reflector.tg_toString(value, entity.type(), acc);
                if (!convertedValue) { // convertedValue is empty (most likely '')
                    return [undefined, undefined]; // return undefined to indicate the need to skip this key member
                } else if (numbers.length === 0) { // numbers are empty -- processing ended successfully
                    return [acc, convertedValue];
                } else {
                    const [numbersHead, ...numbersTail] = numbers;
                    const nameRoot = getKeyMemberName(value, numbersHead.text, reflector);
                    // Proceed to the next key member
                    return constructPathAndValue(value.get(nameRoot), numbersTail, acc === '' ? nameRoot : acc + '.' + nameRoot);
                }
            };
            if (currMemberPath) {
                prevMemberPath = currMemberPath;
            }
            [currMemberPath, currMemberValue] = constructPathAndValue(entity, ctx.numbers, '' /* acc */);
        }

        /**
         * 'tvPart' rule processor that creates key member object (if string value representation is not empty).
         */
        exitTvPart (ctx) {
            if (currMemberValue) {
                const currMember = {};
                if (prevMemberPath) {
                    currMember.separator = ' ';
                }
                currMember.title = entity.type().prop(currMemberPath).title();
                currMember.value = currMemberValue;
                members.push(currMember);
            }
        }

        /**
         * 'vPart' rule processor that creates key member object (if string value representation is not empty).
         */
        exitVPart (ctx) {
            if (currMemberValue) {
                const currMember = {};
                const separator = determineSeparator(prevMemberPath, currMemberPath, entity, reflector);
                if (separator) {
                    currMember.separator = separator;
                }
                currMember.value = currMemberValue;
                members.push(currMember);
            }
        }
    }

    // Traverse tree and fill 'members' in.
    const listener = new Listener();
    antlr4.tree.ParseTreeWalker.DEFAULT.walk(listener, tree);

    return members;
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
