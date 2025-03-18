import { Plugin } from '../../prosemirror-state/dist/index.js';

/**
Input rules are regular expressions describing a piece of text
that, when typed, causes something to happen. This might be
changing two dashes into an emdash, wrapping a paragraph starting
with `"> "` into a blockquote, or something entirely different.
*/
class InputRule {
    /**
    Create an input rule. The rule applies when the user typed
    something and the text directly in front of the cursor matches
    `match`, which should end with `$`.
    
    The `handler` can be a string, in which case the matched text, or
    the first matched group in the regexp, is replaced by that
    string.
    
    Or a it can be a function, which will be called with the match
    array produced by
    [`RegExp.exec`](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/RegExp/exec),
    as well as the start and end of the matched range, and which can
    return a [transaction](https://prosemirror.net/docs/ref/#state.Transaction) that describes the
    rule's effect, or null to indicate the input was not handled.
    */
    constructor(
    /**
    @internal
    */
    match, handler, options = {}) {
        this.match = match;
        this.match = match;
        this.handler = typeof handler == "string" ? stringHandler(handler) : handler;
        this.undoable = options.undoable !== false;
        this.inCode = options.inCode || false;
        this.inCodeMark = options.inCodeMark !== false;
    }
}
function stringHandler(string) {
    return function (state, match, start, end) {
        let insert = string;
        if (match[1]) {
            let offset = match[0].lastIndexOf(match[1]);
            insert += match[0].slice(offset + match[1].length);
            start += offset;
            let cutOff = start - end;
            if (cutOff > 0) {
                insert = match[0].slice(offset - cutOff, offset) + insert;
                start = end;
            }
        }
        return state.tr.insertText(insert, start, end);
    };
}
const MAX_MATCH = 500;
/**
Create an input rules plugin. When enabled, it will cause text
input that matches any of the given rules to trigger the rule's
action.
*/
function inputRules({ rules }) {
    let plugin = new Plugin({
        state: {
            init() { return null; },
            apply(tr, prev) {
                let stored = tr.getMeta(this);
                if (stored)
                    return stored;
                return tr.selectionSet || tr.docChanged ? null : prev;
            }
        },
        props: {
            handleTextInput(view, from, to, text) {
                return run(view, from, to, text, rules, plugin);
            },
            handleDOMEvents: {
                compositionend: (view) => {
                    setTimeout(() => {
                        let { $cursor } = view.state.selection;
                        if ($cursor)
                            run(view, $cursor.pos, $cursor.pos, "", rules, plugin);
                    });
                }
            }
        },
        isInputRules: true
    });
    return plugin;
}
function run(view, from, to, text, rules, plugin) {
    if (view.composing)
        return false;
    let state = view.state, $from = state.doc.resolve(from);
    let textBefore = $from.parent.textBetween(Math.max(0, $from.parentOffset - MAX_MATCH), $from.parentOffset, null, "\ufffc") + text;
    for (let i = 0; i < rules.length; i++) {
        let rule = rules[i];
        if (!rule.inCodeMark && $from.marks().some(m => m.type.spec.code))
            continue;
        if ($from.parent.type.spec.code) {
            if (!rule.inCode)
                continue;
        }
        else if (rule.inCode === "only") {
            continue;
        }
        let match = rule.match.exec(textBefore);
        let tr = match && match[0].length >= text.length &&
            rule.handler(state, match, from - (match[0].length - text.length), to);
        if (!tr)
            continue;
        if (rule.undoable)
            tr.setMeta(plugin, { transform: tr, from, to, text });
        view.dispatch(tr);
        return true;
    }
    return false;
}
/**
This is a command that will undo an input rule, if applying such a
rule was the last thing that the user did.
*/
const undoInputRule = (state, dispatch) => {
    let plugins = state.plugins;
    for (let i = 0; i < plugins.length; i++) {
        let plugin = plugins[i], undoable;
        if (plugin.spec.isInputRules && (undoable = plugin.getState(state))) {
            if (dispatch) {
                let tr = state.tr, toUndo = undoable.transform;
                for (let j = toUndo.steps.length - 1; j >= 0; j--)
                    tr.step(toUndo.steps[j].invert(toUndo.docs[j]));
                if (undoable.text) {
                    let marks = tr.doc.resolve(undoable.from).marks();
                    tr.replaceWith(undoable.from, undoable.to, state.schema.text(undoable.text, marks));
                }
                else {
                    tr.delete(undoable.from, undoable.to);
                }
                dispatch(tr);
            }
            return true;
        }
    }
    return false;
};

/**
Converts double dashes to an emdash.
*/
new InputRule(/--$/, "—", { inCodeMark: false });
/**
Converts three dots to an ellipsis character.
*/
new InputRule(/\.\.\.$/, "…", { inCodeMark: false });
/**
“Smart” opening double quotes.
*/
new InputRule(/(?:^|[\s\{\[\(\<'"\u2018\u201C])(")$/, "“", { inCodeMark: false });
/**
“Smart” closing double quotes.
*/
new InputRule(/"$/, "”", { inCodeMark: false });
/**
“Smart” opening single quotes.
*/
new InputRule(/(?:^|[\s\{\[\(\<'"\u2018\u201C])(')$/, "‘", { inCodeMark: false });
/**
“Smart” closing single quotes.
*/
new InputRule(/'$/, "’", { inCodeMark: false });

export { InputRule, inputRules, undoInputRule };
