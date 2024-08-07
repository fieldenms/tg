/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://github.com/material-components/material-components-web/blob/master/LICENSE
 */
(function webpackUniversalModuleDefinition(root, factory) {
    if (typeof exports === "object" && typeof module === "object") module.exports = factory(); else if (typeof define === "function" && define.amd) define([], factory); else if (typeof exports === "object") exports["textfield"] = factory(); else root["mdc"] = root["mdc"] || {}, 
    root["mdc"]["textfield"] = factory();
})(window, function() {
    return function(modules) {
        var installedModules = {};
        function __webpack_require__(moduleId) {
            if (installedModules[moduleId]) {
                return installedModules[moduleId].exports;
            }
            var module = installedModules[moduleId] = {
                i: moduleId,
                l: false,
                exports: {}
            };
            modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);
            module.l = true;
            return module.exports;
        }
        __webpack_require__.m = modules;
        __webpack_require__.c = installedModules;
        __webpack_require__.d = function(exports, name, getter) {
            if (!__webpack_require__.o(exports, name)) {
                Object.defineProperty(exports, name, {
                    configurable: false,
                    enumerable: true,
                    get: getter
                });
            }
        };
        __webpack_require__.n = function(module) {
            var getter = module && module.__esModule ? function getDefault() {
                return module["default"];
            } : function getModuleExports() {
                return module;
            };
            __webpack_require__.d(getter, "a", getter);
            return getter;
        };
        __webpack_require__.o = function(object, property) {
            return Object.prototype.hasOwnProperty.call(object, property);
        };
        __webpack_require__.p = "";
        return __webpack_require__(__webpack_require__.s = 163);
    }({
        0: function(module, exports, __webpack_require__) {
            "use strict";
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var MDCFoundation = function() {
                function MDCFoundation(adapter) {
                    if (adapter === void 0) {
                        adapter = {};
                    }
                    this.adapter_ = adapter;
                }
                Object.defineProperty(MDCFoundation, "cssClasses", {
                    get: function get() {
                        return {};
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCFoundation, "strings", {
                    get: function get() {
                        return {};
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCFoundation, "numbers", {
                    get: function get() {
                        return {};
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCFoundation, "defaultAdapter", {
                    get: function get() {
                        return {};
                    },
                    enumerable: true,
                    configurable: true
                });
                MDCFoundation.prototype.init = function() {};
                MDCFoundation.prototype.destroy = function() {};
                return MDCFoundation;
            }();
            exports.MDCFoundation = MDCFoundation;
            exports.default = MDCFoundation;
        },
        1: function(module, exports, __webpack_require__) {
            "use strict";
            var __read = this && this.__read || function(o, n) {
                var m = typeof Symbol === "function" && o[Symbol.iterator];
                if (!m) return o;
                var i = m.call(o), r, ar = [], e;
                try {
                    while ((n === void 0 || n-- > 0) && !(r = i.next()).done) {
                        ar.push(r.value);
                    }
                } catch (error) {
                    e = {
                        error: error
                    };
                } finally {
                    try {
                        if (r && !r.done && (m = i["return"])) m.call(i);
                    } finally {
                        if (e) throw e.error;
                    }
                }
                return ar;
            };
            var __spread = this && this.__spread || function() {
                for (var ar = [], i = 0; i < arguments.length; i++) {
                    ar = ar.concat(__read(arguments[i]));
                }
                return ar;
            };
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var foundation_1 = __webpack_require__(0);
            var MDCComponent = function() {
                function MDCComponent(root, foundation) {
                    var args = [];
                    for (var _i = 2; _i < arguments.length; _i++) {
                        args[_i - 2] = arguments[_i];
                    }
                    this.root_ = root;
                    this.initialize.apply(this, __spread(args));
                    this.foundation_ = foundation === undefined ? this.getDefaultFoundation() : foundation;
                    this.foundation_.init();
                    this.initialSyncWithDOM();
                }
                MDCComponent.attachTo = function(root) {
                    return new MDCComponent(root, new foundation_1.MDCFoundation({}));
                };
                MDCComponent.prototype.initialize = function() {
                    var _args = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        _args[_i] = arguments[_i];
                    }
                };
                MDCComponent.prototype.getDefaultFoundation = function() {
                    throw new Error("Subclasses must override getDefaultFoundation to return a properly configured " + "foundation class");
                };
                MDCComponent.prototype.initialSyncWithDOM = function() {};
                MDCComponent.prototype.destroy = function() {
                    this.foundation_.destroy();
                };
                MDCComponent.prototype.listen = function(evtType, handler) {
                    this.root_.addEventListener(evtType, handler);
                };
                MDCComponent.prototype.unlisten = function(evtType, handler) {
                    this.root_.removeEventListener(evtType, handler);
                };
                MDCComponent.prototype.emit = function(evtType, evtData, shouldBubble) {
                    if (shouldBubble === void 0) {
                        shouldBubble = false;
                    }
                    var evt;
                    if (typeof CustomEvent === "function") {
                        evt = new CustomEvent(evtType, {
                            bubbles: shouldBubble,
                            detail: evtData
                        });
                    } else {
                        evt = document.createEvent("CustomEvent");
                        evt.initCustomEvent(evtType, shouldBubble, false, evtData);
                    }
                    this.root_.dispatchEvent(evt);
                };
                return MDCComponent;
            }();
            exports.MDCComponent = MDCComponent;
            exports.default = MDCComponent;
        },
        12: function(module, exports, __webpack_require__) {
            "use strict";
            var __extends = this && this.__extends || function() {
                var _extendStatics = function extendStatics(d, b) {
                    _extendStatics = Object.setPrototypeOf || {
                        __proto__: []
                    } instanceof Array && function(d, b) {
                        d.__proto__ = b;
                    } || function(d, b) {
                        for (var p in b) {
                            if (b.hasOwnProperty(p)) d[p] = b[p];
                        }
                    };
                    return _extendStatics(d, b);
                };
                return function(d, b) {
                    _extendStatics(d, b);
                    function __() {
                        this.constructor = d;
                    }
                    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
                };
            }();
            var __assign = this && this.__assign || function() {
                __assign = Object.assign || function(t) {
                    for (var s, i = 1, n = arguments.length; i < n; i++) {
                        s = arguments[i];
                        for (var p in s) {
                            if (Object.prototype.hasOwnProperty.call(s, p)) t[p] = s[p];
                        }
                    }
                    return t;
                };
                return __assign.apply(this, arguments);
            };
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var foundation_1 = __webpack_require__(0);
            var constants_1 = __webpack_require__(16);
            var MDCFloatingLabelFoundation = function(_super) {
                __extends(MDCFloatingLabelFoundation, _super);
                function MDCFloatingLabelFoundation(adapter) {
                    var _this = _super.call(this, __assign({}, MDCFloatingLabelFoundation.defaultAdapter, adapter)) || this;
                    _this.shakeAnimationEndHandler_ = function() {
                        return _this.handleShakeAnimationEnd_();
                    };
                    return _this;
                }
                Object.defineProperty(MDCFloatingLabelFoundation, "cssClasses", {
                    get: function get() {
                        return constants_1.cssClasses;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCFloatingLabelFoundation, "defaultAdapter", {
                    get: function get() {
                        return {
                            addClass: function addClass() {
                                return undefined;
                            },
                            removeClass: function removeClass() {
                                return undefined;
                            },
                            getWidth: function getWidth() {
                                return 0;
                            },
                            registerInteractionHandler: function registerInteractionHandler() {
                                return undefined;
                            },
                            deregisterInteractionHandler: function deregisterInteractionHandler() {
                                return undefined;
                            }
                        };
                    },
                    enumerable: true,
                    configurable: true
                });
                MDCFloatingLabelFoundation.prototype.init = function() {
                    this.adapter_.registerInteractionHandler("animationend", this.shakeAnimationEndHandler_);
                };
                MDCFloatingLabelFoundation.prototype.destroy = function() {
                    this.adapter_.deregisterInteractionHandler("animationend", this.shakeAnimationEndHandler_);
                };
                MDCFloatingLabelFoundation.prototype.getWidth = function() {
                    return this.adapter_.getWidth();
                };
                MDCFloatingLabelFoundation.prototype.shake = function(shouldShake) {
                    var LABEL_SHAKE = MDCFloatingLabelFoundation.cssClasses.LABEL_SHAKE;
                    if (shouldShake) {
                        this.adapter_.addClass(LABEL_SHAKE);
                    } else {
                        this.adapter_.removeClass(LABEL_SHAKE);
                    }
                };
                MDCFloatingLabelFoundation.prototype.float = function(shouldFloat) {
                    var _a = MDCFloatingLabelFoundation.cssClasses, LABEL_FLOAT_ABOVE = _a.LABEL_FLOAT_ABOVE, LABEL_SHAKE = _a.LABEL_SHAKE;
                    if (shouldFloat) {
                        this.adapter_.addClass(LABEL_FLOAT_ABOVE);
                    } else {
                        this.adapter_.removeClass(LABEL_FLOAT_ABOVE);
                        this.adapter_.removeClass(LABEL_SHAKE);
                    }
                };
                MDCFloatingLabelFoundation.prototype.handleShakeAnimationEnd_ = function() {
                    var LABEL_SHAKE = MDCFloatingLabelFoundation.cssClasses.LABEL_SHAKE;
                    this.adapter_.removeClass(LABEL_SHAKE);
                };
                return MDCFloatingLabelFoundation;
            }(foundation_1.MDCFoundation);
            exports.MDCFloatingLabelFoundation = MDCFloatingLabelFoundation;
            exports.default = MDCFloatingLabelFoundation;
        },
        14: function(module, exports, __webpack_require__) {
            "use strict";
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var strings = {
                NOTCH_ELEMENT_SELECTOR: ".mdc-notched-outline__notch"
            };
            exports.strings = strings;
            var numbers = {
                NOTCH_ELEMENT_PADDING: 8
            };
            exports.numbers = numbers;
            var cssClasses = {
                NO_LABEL: "mdc-notched-outline--no-label",
                OUTLINE_NOTCHED: "mdc-notched-outline--notched",
                OUTLINE_UPGRADED: "mdc-notched-outline--upgraded"
            };
            exports.cssClasses = cssClasses;
        },
        16: function(module, exports, __webpack_require__) {
            "use strict";
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            exports.cssClasses = {
                LABEL_FLOAT_ABOVE: "mdc-floating-label--float-above",
                LABEL_SHAKE: "mdc-floating-label--shake",
                ROOT: "mdc-floating-label"
            };
        },
        163: function(module, exports, __webpack_require__) {
            "use strict";
            function __export(m) {
                for (var p in m) {
                    if (!exports.hasOwnProperty(p)) exports[p] = m[p];
                }
            }
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            __export(__webpack_require__(164));
            __export(__webpack_require__(85));
            __export(__webpack_require__(168));
            __export(__webpack_require__(169));
            __export(__webpack_require__(170));
        },
        164: function(module, exports, __webpack_require__) {
            "use strict";
            var __extends = this && this.__extends || function() {
                var _extendStatics = function extendStatics(d, b) {
                    _extendStatics = Object.setPrototypeOf || {
                        __proto__: []
                    } instanceof Array && function(d, b) {
                        d.__proto__ = b;
                    } || function(d, b) {
                        for (var p in b) {
                            if (b.hasOwnProperty(p)) d[p] = b[p];
                        }
                    };
                    return _extendStatics(d, b);
                };
                return function(d, b) {
                    _extendStatics(d, b);
                    function __() {
                        this.constructor = d;
                    }
                    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
                };
            }();
            var __assign = this && this.__assign || function() {
                __assign = Object.assign || function(t) {
                    for (var s, i = 1, n = arguments.length; i < n; i++) {
                        s = arguments[i];
                        for (var p in s) {
                            if (Object.prototype.hasOwnProperty.call(s, p)) t[p] = s[p];
                        }
                    }
                    return t;
                };
                return __assign.apply(this, arguments);
            };
            var __importStar = this && this.__importStar || function(mod) {
                if (mod && mod.__esModule) return mod;
                var result = {};
                if (mod != null) for (var k in mod) {
                    if (Object.hasOwnProperty.call(mod, k)) result[k] = mod[k];
                }
                result["default"] = mod;
                return result;
            };
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var component_1 = __webpack_require__(1);
            var ponyfill = __importStar(__webpack_require__(2));
            var component_2 = __webpack_require__(24);
            var component_3 = __webpack_require__(25);
            var component_4 = __webpack_require__(28);
            var component_5 = __webpack_require__(5);
            var foundation_1 = __webpack_require__(4);
            var component_6 = __webpack_require__(83);
            var foundation_2 = __webpack_require__(48);
            var constants_1 = __webpack_require__(84);
            var foundation_3 = __webpack_require__(85);
            var component_7 = __webpack_require__(86);
            var foundation_4 = __webpack_require__(49);
            var component_8 = __webpack_require__(87);
            var MDCTextField = function(_super) {
                __extends(MDCTextField, _super);
                function MDCTextField() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                MDCTextField.attachTo = function(root) {
                    return new MDCTextField(root);
                };
                MDCTextField.prototype.initialize = function(rippleFactory, lineRippleFactory, helperTextFactory, characterCounterFactory, iconFactory, labelFactory, outlineFactory) {
                    if (rippleFactory === void 0) {
                        rippleFactory = function rippleFactory(el, foundation) {
                            return new component_5.MDCRipple(el, foundation);
                        };
                    }
                    if (lineRippleFactory === void 0) {
                        lineRippleFactory = function lineRippleFactory(el) {
                            return new component_3.MDCLineRipple(el);
                        };
                    }
                    if (helperTextFactory === void 0) {
                        helperTextFactory = function helperTextFactory(el) {
                            return new component_7.MDCTextFieldHelperText(el);
                        };
                    }
                    if (characterCounterFactory === void 0) {
                        characterCounterFactory = function characterCounterFactory(el) {
                            return new component_6.MDCTextFieldCharacterCounter(el);
                        };
                    }
                    if (iconFactory === void 0) {
                        iconFactory = function iconFactory(el) {
                            return new component_8.MDCTextFieldIcon(el);
                        };
                    }
                    if (labelFactory === void 0) {
                        labelFactory = function labelFactory(el) {
                            return new component_2.MDCFloatingLabel(el);
                        };
                    }
                    if (outlineFactory === void 0) {
                        outlineFactory = function outlineFactory(el) {
                            return new component_4.MDCNotchedOutline(el);
                        };
                    }
                    this.input_ = this.root_.querySelector(constants_1.strings.INPUT_SELECTOR);
                    var labelElement = this.root_.querySelector(constants_1.strings.LABEL_SELECTOR);
                    this.label_ = labelElement ? labelFactory(labelElement) : null;
                    var lineRippleElement = this.root_.querySelector(constants_1.strings.LINE_RIPPLE_SELECTOR);
                    this.lineRipple_ = lineRippleElement ? lineRippleFactory(lineRippleElement) : null;
                    var outlineElement = this.root_.querySelector(constants_1.strings.OUTLINE_SELECTOR);
                    this.outline_ = outlineElement ? outlineFactory(outlineElement) : null;
                    var helperTextStrings = foundation_4.MDCTextFieldHelperTextFoundation.strings;
                    var nextElementSibling = this.root_.nextElementSibling;
                    var hasHelperLine = nextElementSibling && nextElementSibling.classList.contains(constants_1.cssClasses.HELPER_LINE);
                    var helperTextEl = hasHelperLine && nextElementSibling && nextElementSibling.querySelector(helperTextStrings.ROOT_SELECTOR);
                    this.helperText_ = helperTextEl ? helperTextFactory(helperTextEl) : null;
                    var characterCounterStrings = foundation_2.MDCTextFieldCharacterCounterFoundation.strings;
                    var characterCounterEl = this.root_.querySelector(characterCounterStrings.ROOT_SELECTOR);
                    if (!characterCounterEl && hasHelperLine && nextElementSibling) {
                        characterCounterEl = nextElementSibling.querySelector(characterCounterStrings.ROOT_SELECTOR);
                    }
                    this.characterCounter_ = characterCounterEl ? characterCounterFactory(characterCounterEl) : null;
                    this.leadingIcon_ = null;
                    this.trailingIcon_ = null;
                    var iconElements = this.root_.querySelectorAll(constants_1.strings.ICON_SELECTOR);
                    if (iconElements.length > 0) {
                        if (iconElements.length > 1) {
                            this.leadingIcon_ = iconFactory(iconElements[0]);
                            this.trailingIcon_ = iconFactory(iconElements[1]);
                        } else {
                            if (this.root_.classList.contains(constants_1.cssClasses.WITH_LEADING_ICON)) {
                                this.leadingIcon_ = iconFactory(iconElements[0]);
                            } else {
                                this.trailingIcon_ = iconFactory(iconElements[0]);
                            }
                        }
                    }
                    this.ripple = this.createRipple_(rippleFactory);
                };
                MDCTextField.prototype.destroy = function() {
                    if (this.ripple) {
                        this.ripple.destroy();
                    }
                    if (this.lineRipple_) {
                        this.lineRipple_.destroy();
                    }
                    if (this.helperText_) {
                        this.helperText_.destroy();
                    }
                    if (this.characterCounter_) {
                        this.characterCounter_.destroy();
                    }
                    if (this.leadingIcon_) {
                        this.leadingIcon_.destroy();
                    }
                    if (this.trailingIcon_) {
                        this.trailingIcon_.destroy();
                    }
                    if (this.label_) {
                        this.label_.destroy();
                    }
                    if (this.outline_) {
                        this.outline_.destroy();
                    }
                    _super.prototype.destroy.call(this);
                };
                MDCTextField.prototype.initialSyncWithDOM = function() {
                    this.disabled = this.input_.disabled;
                };
                Object.defineProperty(MDCTextField.prototype, "value", {
                    get: function get() {
                        return this.foundation_.getValue();
                    },
                    set: function set(value) {
                        this.foundation_.setValue(value);
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextField.prototype, "disabled", {
                    get: function get() {
                        return this.foundation_.isDisabled();
                    },
                    set: function set(disabled) {
                        this.foundation_.setDisabled(disabled);
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextField.prototype, "valid", {
                    get: function get() {
                        return this.foundation_.isValid();
                    },
                    set: function set(valid) {
                        this.foundation_.setValid(valid);
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextField.prototype, "required", {
                    get: function get() {
                        return this.input_.required;
                    },
                    set: function set(required) {
                        this.input_.required = required;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextField.prototype, "pattern", {
                    get: function get() {
                        return this.input_.pattern;
                    },
                    set: function set(pattern) {
                        this.input_.pattern = pattern;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextField.prototype, "minLength", {
                    get: function get() {
                        return this.input_.minLength;
                    },
                    set: function set(minLength) {
                        this.input_.minLength = minLength;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextField.prototype, "maxLength", {
                    get: function get() {
                        return this.input_.maxLength;
                    },
                    set: function set(maxLength) {
                        if (maxLength < 0) {
                            this.input_.removeAttribute("maxLength");
                        } else {
                            this.input_.maxLength = maxLength;
                        }
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextField.prototype, "min", {
                    get: function get() {
                        return this.input_.min;
                    },
                    set: function set(min) {
                        this.input_.min = min;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextField.prototype, "max", {
                    get: function get() {
                        return this.input_.max;
                    },
                    set: function set(max) {
                        this.input_.max = max;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextField.prototype, "step", {
                    get: function get() {
                        return this.input_.step;
                    },
                    set: function set(step) {
                        this.input_.step = step;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextField.prototype, "helperTextContent", {
                    set: function set(content) {
                        this.foundation_.setHelperTextContent(content);
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextField.prototype, "leadingIconAriaLabel", {
                    set: function set(label) {
                        this.foundation_.setLeadingIconAriaLabel(label);
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextField.prototype, "leadingIconContent", {
                    set: function set(content) {
                        this.foundation_.setLeadingIconContent(content);
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextField.prototype, "trailingIconAriaLabel", {
                    set: function set(label) {
                        this.foundation_.setTrailingIconAriaLabel(label);
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextField.prototype, "trailingIconContent", {
                    set: function set(content) {
                        this.foundation_.setTrailingIconContent(content);
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextField.prototype, "useNativeValidation", {
                    set: function set(useNativeValidation) {
                        this.foundation_.setUseNativeValidation(useNativeValidation);
                    },
                    enumerable: true,
                    configurable: true
                });
                MDCTextField.prototype.focus = function() {
                    this.input_.focus();
                };
                MDCTextField.prototype.layout = function() {
                    var openNotch = this.foundation_.shouldFloat;
                    this.foundation_.notchOutline(openNotch);
                };
                MDCTextField.prototype.getDefaultFoundation = function() {
                    var adapter = __assign({}, this.getRootAdapterMethods_(), this.getInputAdapterMethods_(), this.getLabelAdapterMethods_(), this.getLineRippleAdapterMethods_(), this.getOutlineAdapterMethods_());
                    return new foundation_3.MDCTextFieldFoundation(adapter, this.getFoundationMap_());
                };
                MDCTextField.prototype.getRootAdapterMethods_ = function() {
                    var _this = this;
                    return {
                        addClass: function addClass(className) {
                            return _this.root_.classList.add(className);
                        },
                        removeClass: function removeClass(className) {
                            return _this.root_.classList.remove(className);
                        },
                        hasClass: function hasClass(className) {
                            return _this.root_.classList.contains(className);
                        },
                        registerTextFieldInteractionHandler: function registerTextFieldInteractionHandler(evtType, handler) {
                            return _this.listen(evtType, handler);
                        },
                        deregisterTextFieldInteractionHandler: function deregisterTextFieldInteractionHandler(evtType, handler) {
                            return _this.unlisten(evtType, handler);
                        },
                        registerValidationAttributeChangeHandler: function registerValidationAttributeChangeHandler(handler) {
                            var getAttributesList = function getAttributesList(mutationsList) {
                                return mutationsList.map(function(mutation) {
                                    return mutation.attributeName;
                                }).filter(function(attributeName) {
                                    return attributeName;
                                });
                            };
                            var observer = new MutationObserver(function(mutationsList) {
                                return handler(getAttributesList(mutationsList));
                            });
                            var config = {
                                attributes: true
                            };
                            observer.observe(_this.input_, config);
                            return observer;
                        },
                        deregisterValidationAttributeChangeHandler: function deregisterValidationAttributeChangeHandler(observer) {
                            return observer.disconnect();
                        }
                    };
                };
                MDCTextField.prototype.getInputAdapterMethods_ = function() {
                    var _this = this;
                    return {
                        getNativeInput: function getNativeInput() {
                            return _this.input_;
                        },
                        isFocused: function isFocused() {
                            return document.activeElement === _this.input_;
                        },
                        registerInputInteractionHandler: function registerInputInteractionHandler(evtType, handler) {
                            return _this.input_.addEventListener(evtType, handler);
                        },
                        deregisterInputInteractionHandler: function deregisterInputInteractionHandler(evtType, handler) {
                            return _this.input_.removeEventListener(evtType, handler);
                        }
                    };
                };
                MDCTextField.prototype.getLabelAdapterMethods_ = function() {
                    var _this = this;
                    return {
                        floatLabel: function floatLabel(shouldFloat) {
                            return _this.label_ && _this.label_.float(shouldFloat);
                        },
                        getLabelWidth: function getLabelWidth() {
                            return _this.label_ ? _this.label_.getWidth() : 0;
                        },
                        hasLabel: function hasLabel() {
                            return Boolean(_this.label_);
                        },
                        shakeLabel: function shakeLabel(shouldShake) {
                            return _this.label_ && _this.label_.shake(shouldShake);
                        }
                    };
                };
                MDCTextField.prototype.getLineRippleAdapterMethods_ = function() {
                    var _this = this;
                    return {
                        activateLineRipple: function activateLineRipple() {
                            if (_this.lineRipple_) {
                                _this.lineRipple_.activate();
                            }
                        },
                        deactivateLineRipple: function deactivateLineRipple() {
                            if (_this.lineRipple_) {
                                _this.lineRipple_.deactivate();
                            }
                        },
                        setLineRippleTransformOrigin: function setLineRippleTransformOrigin(normalizedX) {
                            if (_this.lineRipple_) {
                                _this.lineRipple_.setRippleCenter(normalizedX);
                            }
                        }
                    };
                };
                MDCTextField.prototype.getOutlineAdapterMethods_ = function() {
                    var _this = this;
                    return {
                        closeOutline: function closeOutline() {
                            return _this.outline_ && _this.outline_.closeNotch();
                        },
                        hasOutline: function hasOutline() {
                            return Boolean(_this.outline_);
                        },
                        notchOutline: function notchOutline(labelWidth) {
                            return _this.outline_ && _this.outline_.notch(labelWidth);
                        }
                    };
                };
                MDCTextField.prototype.getFoundationMap_ = function() {
                    return {
                        characterCounter: this.characterCounter_ ? this.characterCounter_.foundation : undefined,
                        helperText: this.helperText_ ? this.helperText_.foundation : undefined,
                        leadingIcon: this.leadingIcon_ ? this.leadingIcon_.foundation : undefined,
                        trailingIcon: this.trailingIcon_ ? this.trailingIcon_.foundation : undefined
                    };
                };
                MDCTextField.prototype.createRipple_ = function(rippleFactory) {
                    var _this = this;
                    var isTextArea = this.root_.classList.contains(constants_1.cssClasses.TEXTAREA);
                    var isOutlined = this.root_.classList.contains(constants_1.cssClasses.OUTLINED);
                    if (isTextArea || isOutlined) {
                        return null;
                    }
                    var adapter = __assign({}, component_5.MDCRipple.createAdapter(this), {
                        isSurfaceActive: function isSurfaceActive() {
                            return ponyfill.matches(_this.input_, ":active");
                        },
                        registerInteractionHandler: function registerInteractionHandler(evtType, handler) {
                            return _this.input_.addEventListener(evtType, handler);
                        },
                        deregisterInteractionHandler: function deregisterInteractionHandler(evtType, handler) {
                            return _this.input_.removeEventListener(evtType, handler);
                        }
                    });
                    return rippleFactory(this.root_, new foundation_1.MDCRippleFoundation(adapter));
                };
                return MDCTextField;
            }(component_1.MDCComponent);
            exports.MDCTextField = MDCTextField;
        },
        165: function(module, exports, __webpack_require__) {
            "use strict";
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var cssClasses = {
                ROOT: "mdc-text-field-character-counter"
            };
            exports.cssClasses = cssClasses;
            var strings = {
                ROOT_SELECTOR: "." + cssClasses.ROOT
            };
            exports.strings = strings;
        },
        166: function(module, exports, __webpack_require__) {
            "use strict";
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var cssClasses = {
                HELPER_TEXT_PERSISTENT: "mdc-text-field-helper-text--persistent",
                HELPER_TEXT_VALIDATION_MSG: "mdc-text-field-helper-text--validation-msg",
                ROOT: "mdc-text-field-helper-text"
            };
            exports.cssClasses = cssClasses;
            var strings = {
                ARIA_HIDDEN: "aria-hidden",
                ROLE: "role",
                ROOT_SELECTOR: "." + cssClasses.ROOT
            };
            exports.strings = strings;
        },
        167: function(module, exports, __webpack_require__) {
            "use strict";
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var strings = {
                ICON_EVENT: "MDCTextField:icon",
                ICON_ROLE: "button"
            };
            exports.strings = strings;
            var cssClasses = {
                ROOT: "mdc-text-field__icon"
            };
            exports.cssClasses = cssClasses;
        },
        168: function(module, exports, __webpack_require__) {
            "use strict";
            function __export(m) {
                for (var p in m) {
                    if (!exports.hasOwnProperty(p)) exports[p] = m[p];
                }
            }
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            __export(__webpack_require__(83));
            __export(__webpack_require__(48));
        },
        169: function(module, exports, __webpack_require__) {
            "use strict";
            function __export(m) {
                for (var p in m) {
                    if (!exports.hasOwnProperty(p)) exports[p] = m[p];
                }
            }
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            __export(__webpack_require__(86));
            __export(__webpack_require__(49));
        },
        170: function(module, exports, __webpack_require__) {
            "use strict";
            function __export(m) {
                for (var p in m) {
                    if (!exports.hasOwnProperty(p)) exports[p] = m[p];
                }
            }
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            __export(__webpack_require__(87));
            __export(__webpack_require__(88));
        },
        18: function(module, exports, __webpack_require__) {
            "use strict";
            var __extends = this && this.__extends || function() {
                var _extendStatics = function extendStatics(d, b) {
                    _extendStatics = Object.setPrototypeOf || {
                        __proto__: []
                    } instanceof Array && function(d, b) {
                        d.__proto__ = b;
                    } || function(d, b) {
                        for (var p in b) {
                            if (b.hasOwnProperty(p)) d[p] = b[p];
                        }
                    };
                    return _extendStatics(d, b);
                };
                return function(d, b) {
                    _extendStatics(d, b);
                    function __() {
                        this.constructor = d;
                    }
                    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
                };
            }();
            var __assign = this && this.__assign || function() {
                __assign = Object.assign || function(t) {
                    for (var s, i = 1, n = arguments.length; i < n; i++) {
                        s = arguments[i];
                        for (var p in s) {
                            if (Object.prototype.hasOwnProperty.call(s, p)) t[p] = s[p];
                        }
                    }
                    return t;
                };
                return __assign.apply(this, arguments);
            };
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var foundation_1 = __webpack_require__(0);
            var constants_1 = __webpack_require__(26);
            var MDCLineRippleFoundation = function(_super) {
                __extends(MDCLineRippleFoundation, _super);
                function MDCLineRippleFoundation(adapter) {
                    var _this = _super.call(this, __assign({}, MDCLineRippleFoundation.defaultAdapter, adapter)) || this;
                    _this.transitionEndHandler_ = function(evt) {
                        return _this.handleTransitionEnd(evt);
                    };
                    return _this;
                }
                Object.defineProperty(MDCLineRippleFoundation, "cssClasses", {
                    get: function get() {
                        return constants_1.cssClasses;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCLineRippleFoundation, "defaultAdapter", {
                    get: function get() {
                        return {
                            addClass: function addClass() {
                                return undefined;
                            },
                            removeClass: function removeClass() {
                                return undefined;
                            },
                            hasClass: function hasClass() {
                                return false;
                            },
                            setStyle: function setStyle() {
                                return undefined;
                            },
                            registerEventHandler: function registerEventHandler() {
                                return undefined;
                            },
                            deregisterEventHandler: function deregisterEventHandler() {
                                return undefined;
                            }
                        };
                    },
                    enumerable: true,
                    configurable: true
                });
                MDCLineRippleFoundation.prototype.init = function() {
                    this.adapter_.registerEventHandler("transitionend", this.transitionEndHandler_);
                };
                MDCLineRippleFoundation.prototype.destroy = function() {
                    this.adapter_.deregisterEventHandler("transitionend", this.transitionEndHandler_);
                };
                MDCLineRippleFoundation.prototype.activate = function() {
                    this.adapter_.removeClass(constants_1.cssClasses.LINE_RIPPLE_DEACTIVATING);
                    this.adapter_.addClass(constants_1.cssClasses.LINE_RIPPLE_ACTIVE);
                };
                MDCLineRippleFoundation.prototype.setRippleCenter = function(xCoordinate) {
                    this.adapter_.setStyle("transform-origin", xCoordinate + "px center");
                };
                MDCLineRippleFoundation.prototype.deactivate = function() {
                    this.adapter_.addClass(constants_1.cssClasses.LINE_RIPPLE_DEACTIVATING);
                };
                MDCLineRippleFoundation.prototype.handleTransitionEnd = function(evt) {
                    var isDeactivating = this.adapter_.hasClass(constants_1.cssClasses.LINE_RIPPLE_DEACTIVATING);
                    if (evt.propertyName === "opacity") {
                        if (isDeactivating) {
                            this.adapter_.removeClass(constants_1.cssClasses.LINE_RIPPLE_ACTIVE);
                            this.adapter_.removeClass(constants_1.cssClasses.LINE_RIPPLE_DEACTIVATING);
                        }
                    }
                };
                return MDCLineRippleFoundation;
            }(foundation_1.MDCFoundation);
            exports.MDCLineRippleFoundation = MDCLineRippleFoundation;
            exports.default = MDCLineRippleFoundation;
        },
        2: function(module, exports, __webpack_require__) {
            "use strict";
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            function closest(element, selector) {
                if (element.closest) {
                    return element.closest(selector);
                }
                var el = element;
                while (el) {
                    if (matches(el, selector)) {
                        return el;
                    }
                    el = el.parentElement;
                }
                return null;
            }
            exports.closest = closest;
            function matches(element, selector) {
                var nativeMatches = element.matches || element.webkitMatchesSelector || element.msMatchesSelector;
                return nativeMatches.call(element, selector);
            }
            exports.matches = matches;
        },
        21: function(module, exports, __webpack_require__) {
            "use strict";
            var __extends = this && this.__extends || function() {
                var _extendStatics = function extendStatics(d, b) {
                    _extendStatics = Object.setPrototypeOf || {
                        __proto__: []
                    } instanceof Array && function(d, b) {
                        d.__proto__ = b;
                    } || function(d, b) {
                        for (var p in b) {
                            if (b.hasOwnProperty(p)) d[p] = b[p];
                        }
                    };
                    return _extendStatics(d, b);
                };
                return function(d, b) {
                    _extendStatics(d, b);
                    function __() {
                        this.constructor = d;
                    }
                    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
                };
            }();
            var __assign = this && this.__assign || function() {
                __assign = Object.assign || function(t) {
                    for (var s, i = 1, n = arguments.length; i < n; i++) {
                        s = arguments[i];
                        for (var p in s) {
                            if (Object.prototype.hasOwnProperty.call(s, p)) t[p] = s[p];
                        }
                    }
                    return t;
                };
                return __assign.apply(this, arguments);
            };
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var foundation_1 = __webpack_require__(0);
            var constants_1 = __webpack_require__(14);
            var MDCNotchedOutlineFoundation = function(_super) {
                __extends(MDCNotchedOutlineFoundation, _super);
                function MDCNotchedOutlineFoundation(adapter) {
                    return _super.call(this, __assign({}, MDCNotchedOutlineFoundation.defaultAdapter, adapter)) || this;
                }
                Object.defineProperty(MDCNotchedOutlineFoundation, "strings", {
                    get: function get() {
                        return constants_1.strings;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCNotchedOutlineFoundation, "cssClasses", {
                    get: function get() {
                        return constants_1.cssClasses;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCNotchedOutlineFoundation, "numbers", {
                    get: function get() {
                        return constants_1.numbers;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCNotchedOutlineFoundation, "defaultAdapter", {
                    get: function get() {
                        return {
                            addClass: function addClass() {
                                return undefined;
                            },
                            removeClass: function removeClass() {
                                return undefined;
                            },
                            setNotchWidthProperty: function setNotchWidthProperty() {
                                return undefined;
                            },
                            removeNotchWidthProperty: function removeNotchWidthProperty() {
                                return undefined;
                            }
                        };
                    },
                    enumerable: true,
                    configurable: true
                });
                MDCNotchedOutlineFoundation.prototype.notch = function(notchWidth) {
                    var OUTLINE_NOTCHED = MDCNotchedOutlineFoundation.cssClasses.OUTLINE_NOTCHED;
                    if (notchWidth > 0) {
                        notchWidth += constants_1.numbers.NOTCH_ELEMENT_PADDING;
                    }
                    this.adapter_.setNotchWidthProperty(notchWidth);
                    this.adapter_.addClass(OUTLINE_NOTCHED);
                };
                MDCNotchedOutlineFoundation.prototype.closeNotch = function() {
                    var OUTLINE_NOTCHED = MDCNotchedOutlineFoundation.cssClasses.OUTLINE_NOTCHED;
                    this.adapter_.removeClass(OUTLINE_NOTCHED);
                    this.adapter_.removeNotchWidthProperty();
                };
                return MDCNotchedOutlineFoundation;
            }(foundation_1.MDCFoundation);
            exports.MDCNotchedOutlineFoundation = MDCNotchedOutlineFoundation;
            exports.default = MDCNotchedOutlineFoundation;
        },
        24: function(module, exports, __webpack_require__) {
            "use strict";
            var __extends = this && this.__extends || function() {
                var _extendStatics = function extendStatics(d, b) {
                    _extendStatics = Object.setPrototypeOf || {
                        __proto__: []
                    } instanceof Array && function(d, b) {
                        d.__proto__ = b;
                    } || function(d, b) {
                        for (var p in b) {
                            if (b.hasOwnProperty(p)) d[p] = b[p];
                        }
                    };
                    return _extendStatics(d, b);
                };
                return function(d, b) {
                    _extendStatics(d, b);
                    function __() {
                        this.constructor = d;
                    }
                    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
                };
            }();
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var component_1 = __webpack_require__(1);
            var foundation_1 = __webpack_require__(12);
            var MDCFloatingLabel = function(_super) {
                __extends(MDCFloatingLabel, _super);
                function MDCFloatingLabel() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                MDCFloatingLabel.attachTo = function(root) {
                    return new MDCFloatingLabel(root);
                };
                MDCFloatingLabel.prototype.shake = function(shouldShake) {
                    this.foundation_.shake(shouldShake);
                };
                MDCFloatingLabel.prototype.float = function(shouldFloat) {
                    this.foundation_.float(shouldFloat);
                };
                MDCFloatingLabel.prototype.getWidth = function() {
                    return this.foundation_.getWidth();
                };
                MDCFloatingLabel.prototype.getDefaultFoundation = function() {
                    var _this = this;
                    var adapter = {
                        addClass: function addClass(className) {
                            return _this.root_.classList.add(className);
                        },
                        removeClass: function removeClass(className) {
                            return _this.root_.classList.remove(className);
                        },
                        getWidth: function getWidth() {
                            return _this.root_.scrollWidth;
                        },
                        registerInteractionHandler: function registerInteractionHandler(evtType, handler) {
                            return _this.listen(evtType, handler);
                        },
                        deregisterInteractionHandler: function deregisterInteractionHandler(evtType, handler) {
                            return _this.unlisten(evtType, handler);
                        }
                    };
                    return new foundation_1.MDCFloatingLabelFoundation(adapter);
                };
                return MDCFloatingLabel;
            }(component_1.MDCComponent);
            exports.MDCFloatingLabel = MDCFloatingLabel;
        },
        25: function(module, exports, __webpack_require__) {
            "use strict";
            var __extends = this && this.__extends || function() {
                var _extendStatics = function extendStatics(d, b) {
                    _extendStatics = Object.setPrototypeOf || {
                        __proto__: []
                    } instanceof Array && function(d, b) {
                        d.__proto__ = b;
                    } || function(d, b) {
                        for (var p in b) {
                            if (b.hasOwnProperty(p)) d[p] = b[p];
                        }
                    };
                    return _extendStatics(d, b);
                };
                return function(d, b) {
                    _extendStatics(d, b);
                    function __() {
                        this.constructor = d;
                    }
                    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
                };
            }();
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var component_1 = __webpack_require__(1);
            var foundation_1 = __webpack_require__(18);
            var MDCLineRipple = function(_super) {
                __extends(MDCLineRipple, _super);
                function MDCLineRipple() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                MDCLineRipple.attachTo = function(root) {
                    return new MDCLineRipple(root);
                };
                MDCLineRipple.prototype.activate = function() {
                    this.foundation_.activate();
                };
                MDCLineRipple.prototype.deactivate = function() {
                    this.foundation_.deactivate();
                };
                MDCLineRipple.prototype.setRippleCenter = function(xCoordinate) {
                    this.foundation_.setRippleCenter(xCoordinate);
                };
                MDCLineRipple.prototype.getDefaultFoundation = function() {
                    var _this = this;
                    var adapter = {
                        addClass: function addClass(className) {
                            return _this.root_.classList.add(className);
                        },
                        removeClass: function removeClass(className) {
                            return _this.root_.classList.remove(className);
                        },
                        hasClass: function hasClass(className) {
                            return _this.root_.classList.contains(className);
                        },
                        setStyle: function setStyle(propertyName, value) {
                            return _this.root_.style.setProperty(propertyName, value);
                        },
                        registerEventHandler: function registerEventHandler(evtType, handler) {
                            return _this.listen(evtType, handler);
                        },
                        deregisterEventHandler: function deregisterEventHandler(evtType, handler) {
                            return _this.unlisten(evtType, handler);
                        }
                    };
                    return new foundation_1.MDCLineRippleFoundation(adapter);
                };
                return MDCLineRipple;
            }(component_1.MDCComponent);
            exports.MDCLineRipple = MDCLineRipple;
        },
        26: function(module, exports, __webpack_require__) {
            "use strict";
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var cssClasses = {
                LINE_RIPPLE_ACTIVE: "mdc-line-ripple--active",
                LINE_RIPPLE_DEACTIVATING: "mdc-line-ripple--deactivating"
            };
            exports.cssClasses = cssClasses;
        },
        28: function(module, exports, __webpack_require__) {
            "use strict";
            var __extends = this && this.__extends || function() {
                var _extendStatics = function extendStatics(d, b) {
                    _extendStatics = Object.setPrototypeOf || {
                        __proto__: []
                    } instanceof Array && function(d, b) {
                        d.__proto__ = b;
                    } || function(d, b) {
                        for (var p in b) {
                            if (b.hasOwnProperty(p)) d[p] = b[p];
                        }
                    };
                    return _extendStatics(d, b);
                };
                return function(d, b) {
                    _extendStatics(d, b);
                    function __() {
                        this.constructor = d;
                    }
                    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
                };
            }();
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var component_1 = __webpack_require__(1);
            var foundation_1 = __webpack_require__(12);
            var constants_1 = __webpack_require__(14);
            var foundation_2 = __webpack_require__(21);
            var MDCNotchedOutline = function(_super) {
                __extends(MDCNotchedOutline, _super);
                function MDCNotchedOutline() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                MDCNotchedOutline.attachTo = function(root) {
                    return new MDCNotchedOutline(root);
                };
                MDCNotchedOutline.prototype.initialSyncWithDOM = function() {
                    this.notchElement_ = this.root_.querySelector(constants_1.strings.NOTCH_ELEMENT_SELECTOR);
                    var label = this.root_.querySelector("." + foundation_1.MDCFloatingLabelFoundation.cssClasses.ROOT);
                    if (label) {
                        label.style.transitionDuration = "0s";
                        this.root_.classList.add(constants_1.cssClasses.OUTLINE_UPGRADED);
                        requestAnimationFrame(function() {
                            label.style.transitionDuration = "";
                        });
                    } else {
                        this.root_.classList.add(constants_1.cssClasses.NO_LABEL);
                    }
                };
                MDCNotchedOutline.prototype.notch = function(notchWidth) {
                    this.foundation_.notch(notchWidth);
                };
                MDCNotchedOutline.prototype.closeNotch = function() {
                    this.foundation_.closeNotch();
                };
                MDCNotchedOutline.prototype.getDefaultFoundation = function() {
                    var _this = this;
                    var adapter = {
                        addClass: function addClass(className) {
                            return _this.root_.classList.add(className);
                        },
                        removeClass: function removeClass(className) {
                            return _this.root_.classList.remove(className);
                        },
                        setNotchWidthProperty: function setNotchWidthProperty(width) {
                            return _this.notchElement_.style.setProperty("width", width + "px");
                        },
                        removeNotchWidthProperty: function removeNotchWidthProperty() {
                            return _this.notchElement_.style.removeProperty("width");
                        }
                    };
                    return new foundation_2.MDCNotchedOutlineFoundation(adapter);
                };
                return MDCNotchedOutline;
            }(component_1.MDCComponent);
            exports.MDCNotchedOutline = MDCNotchedOutline;
        },
        3: function(module, exports, __webpack_require__) {
            "use strict";
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var supportsCssVariables_;
            var supportsPassive_;
            function detectEdgePseudoVarBug(windowObj) {
                var document = windowObj.document;
                var node = document.createElement("div");
                node.className = "mdc-ripple-surface--test-edge-var-bug";
                document.body.appendChild(node);
                var computedStyle = windowObj.getComputedStyle(node);
                var hasPseudoVarBug = computedStyle !== null && computedStyle.borderTopStyle === "solid";
                if (node.parentNode) {
                    node.parentNode.removeChild(node);
                }
                return hasPseudoVarBug;
            }
            function supportsCssVariables(windowObj, forceRefresh) {
                if (forceRefresh === void 0) {
                    forceRefresh = false;
                }
                var CSS = windowObj.CSS;
                var supportsCssVars = supportsCssVariables_;
                if (typeof supportsCssVariables_ === "boolean" && !forceRefresh) {
                    return supportsCssVariables_;
                }
                var supportsFunctionPresent = CSS && typeof CSS.supports === "function";
                if (!supportsFunctionPresent) {
                    return false;
                }
                var explicitlySupportsCssVars = CSS.supports("--css-vars", "yes");
                var weAreFeatureDetectingSafari10plus = CSS.supports("(--css-vars: yes)") && CSS.supports("color", "#00000000");
                if (explicitlySupportsCssVars || weAreFeatureDetectingSafari10plus) {
                    supportsCssVars = !detectEdgePseudoVarBug(windowObj);
                } else {
                    supportsCssVars = false;
                }
                if (!forceRefresh) {
                    supportsCssVariables_ = supportsCssVars;
                }
                return supportsCssVars;
            }
            exports.supportsCssVariables = supportsCssVariables;
            function applyPassive(globalObj, forceRefresh) {
                if (globalObj === void 0) {
                    globalObj = window;
                }
                if (forceRefresh === void 0) {
                    forceRefresh = false;
                }
                if (supportsPassive_ === undefined || forceRefresh) {
                    var isSupported_1 = false;
                    try {
                        globalObj.document.addEventListener("test", function() {
                            return undefined;
                        }, {
                            get passive() {
                                isSupported_1 = true;
                                return isSupported_1;
                            }
                        });
                    } catch (e) {}
                    supportsPassive_ = isSupported_1;
                }
                return supportsPassive_ ? {
                    passive: true
                } : false;
            }
            exports.applyPassive = applyPassive;
            function getNormalizedEventCoords(evt, pageOffset, clientRect) {
                if (!evt) {
                    return {
                        x: 0,
                        y: 0
                    };
                }
                var x = pageOffset.x, y = pageOffset.y;
                var documentX = x + clientRect.left;
                var documentY = y + clientRect.top;
                var normalizedX;
                var normalizedY;
                if (evt.type === "touchstart") {
                    var touchEvent = evt;
                    normalizedX = touchEvent.changedTouches[0].pageX - documentX;
                    normalizedY = touchEvent.changedTouches[0].pageY - documentY;
                } else {
                    var mouseEvent = evt;
                    normalizedX = mouseEvent.pageX - documentX;
                    normalizedY = mouseEvent.pageY - documentY;
                }
                return {
                    x: normalizedX,
                    y: normalizedY
                };
            }
            exports.getNormalizedEventCoords = getNormalizedEventCoords;
        },
        4: function(module, exports, __webpack_require__) {
            "use strict";
            var __extends = this && this.__extends || function() {
                var _extendStatics = function extendStatics(d, b) {
                    _extendStatics = Object.setPrototypeOf || {
                        __proto__: []
                    } instanceof Array && function(d, b) {
                        d.__proto__ = b;
                    } || function(d, b) {
                        for (var p in b) {
                            if (b.hasOwnProperty(p)) d[p] = b[p];
                        }
                    };
                    return _extendStatics(d, b);
                };
                return function(d, b) {
                    _extendStatics(d, b);
                    function __() {
                        this.constructor = d;
                    }
                    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
                };
            }();
            var __assign = this && this.__assign || function() {
                __assign = Object.assign || function(t) {
                    for (var s, i = 1, n = arguments.length; i < n; i++) {
                        s = arguments[i];
                        for (var p in s) {
                            if (Object.prototype.hasOwnProperty.call(s, p)) t[p] = s[p];
                        }
                    }
                    return t;
                };
                return __assign.apply(this, arguments);
            };
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var foundation_1 = __webpack_require__(0);
            var constants_1 = __webpack_require__(6);
            var util_1 = __webpack_require__(3);
            var ACTIVATION_EVENT_TYPES = [ "touchstart", "pointerdown", "mousedown", "keydown" ];
            var POINTER_DEACTIVATION_EVENT_TYPES = [ "touchend", "pointerup", "mouseup", "contextmenu" ];
            var activatedTargets = [];
            var MDCRippleFoundation = function(_super) {
                __extends(MDCRippleFoundation, _super);
                function MDCRippleFoundation(adapter) {
                    var _this = _super.call(this, __assign({}, MDCRippleFoundation.defaultAdapter, adapter)) || this;
                    _this.activationAnimationHasEnded_ = false;
                    _this.activationTimer_ = 0;
                    _this.fgDeactivationRemovalTimer_ = 0;
                    _this.fgScale_ = "0";
                    _this.frame_ = {
                        width: 0,
                        height: 0
                    };
                    _this.initialSize_ = 0;
                    _this.layoutFrame_ = 0;
                    _this.maxRadius_ = 0;
                    _this.unboundedCoords_ = {
                        left: 0,
                        top: 0
                    };
                    _this.activationState_ = _this.defaultActivationState_();
                    _this.activationTimerCallback_ = function() {
                        _this.activationAnimationHasEnded_ = true;
                        _this.runDeactivationUXLogicIfReady_();
                    };
                    _this.activateHandler_ = function(e) {
                        return _this.activate_(e);
                    };
                    _this.deactivateHandler_ = function() {
                        return _this.deactivate_();
                    };
                    _this.focusHandler_ = function() {
                        return _this.handleFocus();
                    };
                    _this.blurHandler_ = function() {
                        return _this.handleBlur();
                    };
                    _this.resizeHandler_ = function() {
                        return _this.layout();
                    };
                    return _this;
                }
                Object.defineProperty(MDCRippleFoundation, "cssClasses", {
                    get: function get() {
                        return constants_1.cssClasses;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCRippleFoundation, "strings", {
                    get: function get() {
                        return constants_1.strings;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCRippleFoundation, "numbers", {
                    get: function get() {
                        return constants_1.numbers;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCRippleFoundation, "defaultAdapter", {
                    get: function get() {
                        return {
                            addClass: function addClass() {
                                return undefined;
                            },
                            browserSupportsCssVars: function browserSupportsCssVars() {
                                return true;
                            },
                            computeBoundingRect: function computeBoundingRect() {
                                return {
                                    top: 0,
                                    right: 0,
                                    bottom: 0,
                                    left: 0,
                                    width: 0,
                                    height: 0
                                };
                            },
                            containsEventTarget: function containsEventTarget() {
                                return true;
                            },
                            deregisterDocumentInteractionHandler: function deregisterDocumentInteractionHandler() {
                                return undefined;
                            },
                            deregisterInteractionHandler: function deregisterInteractionHandler() {
                                return undefined;
                            },
                            deregisterResizeHandler: function deregisterResizeHandler() {
                                return undefined;
                            },
                            getWindowPageOffset: function getWindowPageOffset() {
                                return {
                                    x: 0,
                                    y: 0
                                };
                            },
                            isSurfaceActive: function isSurfaceActive() {
                                return true;
                            },
                            isSurfaceDisabled: function isSurfaceDisabled() {
                                return true;
                            },
                            isUnbounded: function isUnbounded() {
                                return true;
                            },
                            registerDocumentInteractionHandler: function registerDocumentInteractionHandler() {
                                return undefined;
                            },
                            registerInteractionHandler: function registerInteractionHandler() {
                                return undefined;
                            },
                            registerResizeHandler: function registerResizeHandler() {
                                return undefined;
                            },
                            removeClass: function removeClass() {
                                return undefined;
                            },
                            updateCssVariable: function updateCssVariable() {
                                return undefined;
                            }
                        };
                    },
                    enumerable: true,
                    configurable: true
                });
                MDCRippleFoundation.prototype.init = function() {
                    var _this = this;
                    var supportsPressRipple = this.supportsPressRipple_();
                    this.registerRootHandlers_(supportsPressRipple);
                    if (supportsPressRipple) {
                        var _a = MDCRippleFoundation.cssClasses, ROOT_1 = _a.ROOT, UNBOUNDED_1 = _a.UNBOUNDED;
                        requestAnimationFrame(function() {
                            _this.adapter_.addClass(ROOT_1);
                            if (_this.adapter_.isUnbounded()) {
                                _this.adapter_.addClass(UNBOUNDED_1);
                                _this.layoutInternal_();
                            }
                        });
                    }
                };
                MDCRippleFoundation.prototype.destroy = function() {
                    var _this = this;
                    if (this.supportsPressRipple_()) {
                        if (this.activationTimer_) {
                            clearTimeout(this.activationTimer_);
                            this.activationTimer_ = 0;
                            this.adapter_.removeClass(MDCRippleFoundation.cssClasses.FG_ACTIVATION);
                        }
                        if (this.fgDeactivationRemovalTimer_) {
                            clearTimeout(this.fgDeactivationRemovalTimer_);
                            this.fgDeactivationRemovalTimer_ = 0;
                            this.adapter_.removeClass(MDCRippleFoundation.cssClasses.FG_DEACTIVATION);
                        }
                        var _a = MDCRippleFoundation.cssClasses, ROOT_2 = _a.ROOT, UNBOUNDED_2 = _a.UNBOUNDED;
                        requestAnimationFrame(function() {
                            _this.adapter_.removeClass(ROOT_2);
                            _this.adapter_.removeClass(UNBOUNDED_2);
                            _this.removeCssVars_();
                        });
                    }
                    this.deregisterRootHandlers_();
                    this.deregisterDeactivationHandlers_();
                };
                MDCRippleFoundation.prototype.activate = function(evt) {
                    this.activate_(evt);
                };
                MDCRippleFoundation.prototype.deactivate = function() {
                    this.deactivate_();
                };
                MDCRippleFoundation.prototype.layout = function() {
                    var _this = this;
                    if (this.layoutFrame_) {
                        cancelAnimationFrame(this.layoutFrame_);
                    }
                    this.layoutFrame_ = requestAnimationFrame(function() {
                        _this.layoutInternal_();
                        _this.layoutFrame_ = 0;
                    });
                };
                MDCRippleFoundation.prototype.setUnbounded = function(unbounded) {
                    var UNBOUNDED = MDCRippleFoundation.cssClasses.UNBOUNDED;
                    if (unbounded) {
                        this.adapter_.addClass(UNBOUNDED);
                    } else {
                        this.adapter_.removeClass(UNBOUNDED);
                    }
                };
                MDCRippleFoundation.prototype.handleFocus = function() {
                    var _this = this;
                    requestAnimationFrame(function() {
                        return _this.adapter_.addClass(MDCRippleFoundation.cssClasses.BG_FOCUSED);
                    });
                };
                MDCRippleFoundation.prototype.handleBlur = function() {
                    var _this = this;
                    requestAnimationFrame(function() {
                        return _this.adapter_.removeClass(MDCRippleFoundation.cssClasses.BG_FOCUSED);
                    });
                };
                MDCRippleFoundation.prototype.supportsPressRipple_ = function() {
                    return this.adapter_.browserSupportsCssVars();
                };
                MDCRippleFoundation.prototype.defaultActivationState_ = function() {
                    return {
                        activationEvent: undefined,
                        hasDeactivationUXRun: false,
                        isActivated: false,
                        isProgrammatic: false,
                        wasActivatedByPointer: false,
                        wasElementMadeActive: false
                    };
                };
                MDCRippleFoundation.prototype.registerRootHandlers_ = function(supportsPressRipple) {
                    var _this = this;
                    if (supportsPressRipple) {
                        ACTIVATION_EVENT_TYPES.forEach(function(evtType) {
                            _this.adapter_.registerInteractionHandler(evtType, _this.activateHandler_);
                        });
                        if (this.adapter_.isUnbounded()) {
                            this.adapter_.registerResizeHandler(this.resizeHandler_);
                        }
                    }
                    this.adapter_.registerInteractionHandler("focus", this.focusHandler_);
                    this.adapter_.registerInteractionHandler("blur", this.blurHandler_);
                };
                MDCRippleFoundation.prototype.registerDeactivationHandlers_ = function(evt) {
                    var _this = this;
                    if (evt.type === "keydown") {
                        this.adapter_.registerInteractionHandler("keyup", this.deactivateHandler_);
                    } else {
                        POINTER_DEACTIVATION_EVENT_TYPES.forEach(function(evtType) {
                            _this.adapter_.registerDocumentInteractionHandler(evtType, _this.deactivateHandler_);
                        });
                    }
                };
                MDCRippleFoundation.prototype.deregisterRootHandlers_ = function() {
                    var _this = this;
                    ACTIVATION_EVENT_TYPES.forEach(function(evtType) {
                        _this.adapter_.deregisterInteractionHandler(evtType, _this.activateHandler_);
                    });
                    this.adapter_.deregisterInteractionHandler("focus", this.focusHandler_);
                    this.adapter_.deregisterInteractionHandler("blur", this.blurHandler_);
                    if (this.adapter_.isUnbounded()) {
                        this.adapter_.deregisterResizeHandler(this.resizeHandler_);
                    }
                };
                MDCRippleFoundation.prototype.deregisterDeactivationHandlers_ = function() {
                    var _this = this;
                    this.adapter_.deregisterInteractionHandler("keyup", this.deactivateHandler_);
                    POINTER_DEACTIVATION_EVENT_TYPES.forEach(function(evtType) {
                        _this.adapter_.deregisterDocumentInteractionHandler(evtType, _this.deactivateHandler_);
                    });
                };
                MDCRippleFoundation.prototype.removeCssVars_ = function() {
                    var _this = this;
                    var rippleStrings = MDCRippleFoundation.strings;
                    var keys = Object.keys(rippleStrings);
                    keys.forEach(function(key) {
                        if (key.indexOf("VAR_") === 0) {
                            _this.adapter_.updateCssVariable(rippleStrings[key], null);
                        }
                    });
                };
                MDCRippleFoundation.prototype.activate_ = function(evt) {
                    var _this = this;
                    if (this.adapter_.isSurfaceDisabled()) {
                        return;
                    }
                    var activationState = this.activationState_;
                    if (activationState.isActivated) {
                        return;
                    }
                    var previousActivationEvent = this.previousActivationEvent_;
                    var isSameInteraction = previousActivationEvent && evt !== undefined && previousActivationEvent.type !== evt.type;
                    if (isSameInteraction) {
                        return;
                    }
                    activationState.isActivated = true;
                    activationState.isProgrammatic = evt === undefined;
                    activationState.activationEvent = evt;
                    activationState.wasActivatedByPointer = activationState.isProgrammatic ? false : evt !== undefined && (evt.type === "mousedown" || evt.type === "touchstart" || evt.type === "pointerdown");
                    var hasActivatedChild = evt !== undefined && activatedTargets.length > 0 && activatedTargets.some(function(target) {
                        return _this.adapter_.containsEventTarget(target);
                    });
                    if (hasActivatedChild) {
                        this.resetActivationState_();
                        return;
                    }
                    if (evt !== undefined) {
                        activatedTargets.push(evt.target);
                        this.registerDeactivationHandlers_(evt);
                    }
                    activationState.wasElementMadeActive = this.checkElementMadeActive_(evt);
                    if (activationState.wasElementMadeActive) {
                        this.animateActivation_();
                    }
                    requestAnimationFrame(function() {
                        activatedTargets = [];
                        if (!activationState.wasElementMadeActive && evt !== undefined && (evt.key === " " || evt.keyCode === 32)) {
                            activationState.wasElementMadeActive = _this.checkElementMadeActive_(evt);
                            if (activationState.wasElementMadeActive) {
                                _this.animateActivation_();
                            }
                        }
                        if (!activationState.wasElementMadeActive) {
                            _this.activationState_ = _this.defaultActivationState_();
                        }
                    });
                };
                MDCRippleFoundation.prototype.checkElementMadeActive_ = function(evt) {
                    return evt !== undefined && evt.type === "keydown" ? this.adapter_.isSurfaceActive() : true;
                };
                MDCRippleFoundation.prototype.animateActivation_ = function() {
                    var _this = this;
                    var _a = MDCRippleFoundation.strings, VAR_FG_TRANSLATE_START = _a.VAR_FG_TRANSLATE_START, VAR_FG_TRANSLATE_END = _a.VAR_FG_TRANSLATE_END;
                    var _b = MDCRippleFoundation.cssClasses, FG_DEACTIVATION = _b.FG_DEACTIVATION, FG_ACTIVATION = _b.FG_ACTIVATION;
                    var DEACTIVATION_TIMEOUT_MS = MDCRippleFoundation.numbers.DEACTIVATION_TIMEOUT_MS;
                    this.layoutInternal_();
                    var translateStart = "";
                    var translateEnd = "";
                    if (!this.adapter_.isUnbounded()) {
                        var _c = this.getFgTranslationCoordinates_(), startPoint = _c.startPoint, endPoint = _c.endPoint;
                        translateStart = startPoint.x + "px, " + startPoint.y + "px";
                        translateEnd = endPoint.x + "px, " + endPoint.y + "px";
                    }
                    this.adapter_.updateCssVariable(VAR_FG_TRANSLATE_START, translateStart);
                    this.adapter_.updateCssVariable(VAR_FG_TRANSLATE_END, translateEnd);
                    clearTimeout(this.activationTimer_);
                    clearTimeout(this.fgDeactivationRemovalTimer_);
                    this.rmBoundedActivationClasses_();
                    this.adapter_.removeClass(FG_DEACTIVATION);
                    this.adapter_.computeBoundingRect();
                    this.adapter_.addClass(FG_ACTIVATION);
                    this.activationTimer_ = setTimeout(function() {
                        return _this.activationTimerCallback_();
                    }, DEACTIVATION_TIMEOUT_MS);
                };
                MDCRippleFoundation.prototype.getFgTranslationCoordinates_ = function() {
                    var _a = this.activationState_, activationEvent = _a.activationEvent, wasActivatedByPointer = _a.wasActivatedByPointer;
                    var startPoint;
                    if (wasActivatedByPointer) {
                        startPoint = util_1.getNormalizedEventCoords(activationEvent, this.adapter_.getWindowPageOffset(), this.adapter_.computeBoundingRect());
                    } else {
                        startPoint = {
                            x: this.frame_.width / 2,
                            y: this.frame_.height / 2
                        };
                    }
                    startPoint = {
                        x: startPoint.x - this.initialSize_ / 2,
                        y: startPoint.y - this.initialSize_ / 2
                    };
                    var endPoint = {
                        x: this.frame_.width / 2 - this.initialSize_ / 2,
                        y: this.frame_.height / 2 - this.initialSize_ / 2
                    };
                    return {
                        startPoint: startPoint,
                        endPoint: endPoint
                    };
                };
                MDCRippleFoundation.prototype.runDeactivationUXLogicIfReady_ = function() {
                    var _this = this;
                    var FG_DEACTIVATION = MDCRippleFoundation.cssClasses.FG_DEACTIVATION;
                    var _a = this.activationState_, hasDeactivationUXRun = _a.hasDeactivationUXRun, isActivated = _a.isActivated;
                    var activationHasEnded = hasDeactivationUXRun || !isActivated;
                    if (activationHasEnded && this.activationAnimationHasEnded_) {
                        this.rmBoundedActivationClasses_();
                        this.adapter_.addClass(FG_DEACTIVATION);
                        this.fgDeactivationRemovalTimer_ = setTimeout(function() {
                            _this.adapter_.removeClass(FG_DEACTIVATION);
                        }, constants_1.numbers.FG_DEACTIVATION_MS);
                    }
                };
                MDCRippleFoundation.prototype.rmBoundedActivationClasses_ = function() {
                    var FG_ACTIVATION = MDCRippleFoundation.cssClasses.FG_ACTIVATION;
                    this.adapter_.removeClass(FG_ACTIVATION);
                    this.activationAnimationHasEnded_ = false;
                    this.adapter_.computeBoundingRect();
                };
                MDCRippleFoundation.prototype.resetActivationState_ = function() {
                    var _this = this;
                    this.previousActivationEvent_ = this.activationState_.activationEvent;
                    this.activationState_ = this.defaultActivationState_();
                    setTimeout(function() {
                        return _this.previousActivationEvent_ = undefined;
                    }, MDCRippleFoundation.numbers.TAP_DELAY_MS);
                };
                MDCRippleFoundation.prototype.deactivate_ = function() {
                    var _this = this;
                    var activationState = this.activationState_;
                    if (!activationState.isActivated) {
                        return;
                    }
                    var state = __assign({}, activationState);
                    if (activationState.isProgrammatic) {
                        requestAnimationFrame(function() {
                            return _this.animateDeactivation_(state);
                        });
                        this.resetActivationState_();
                    } else {
                        this.deregisterDeactivationHandlers_();
                        requestAnimationFrame(function() {
                            _this.activationState_.hasDeactivationUXRun = true;
                            _this.animateDeactivation_(state);
                            _this.resetActivationState_();
                        });
                    }
                };
                MDCRippleFoundation.prototype.animateDeactivation_ = function(_a) {
                    var wasActivatedByPointer = _a.wasActivatedByPointer, wasElementMadeActive = _a.wasElementMadeActive;
                    if (wasActivatedByPointer || wasElementMadeActive) {
                        this.runDeactivationUXLogicIfReady_();
                    }
                };
                MDCRippleFoundation.prototype.layoutInternal_ = function() {
                    var _this = this;
                    this.frame_ = this.adapter_.computeBoundingRect();
                    var maxDim = Math.max(this.frame_.height, this.frame_.width);
                    var getBoundedRadius = function getBoundedRadius() {
                        var hypotenuse = Math.sqrt(Math.pow(_this.frame_.width, 2) + Math.pow(_this.frame_.height, 2));
                        return hypotenuse + MDCRippleFoundation.numbers.PADDING;
                    };
                    this.maxRadius_ = this.adapter_.isUnbounded() ? maxDim : getBoundedRadius();
                    this.initialSize_ = Math.floor(maxDim * MDCRippleFoundation.numbers.INITIAL_ORIGIN_SCALE);
                    this.fgScale_ = "" + this.maxRadius_ / this.initialSize_;
                    this.updateLayoutCssVars_();
                };
                MDCRippleFoundation.prototype.updateLayoutCssVars_ = function() {
                    var _a = MDCRippleFoundation.strings, VAR_FG_SIZE = _a.VAR_FG_SIZE, VAR_LEFT = _a.VAR_LEFT, VAR_TOP = _a.VAR_TOP, VAR_FG_SCALE = _a.VAR_FG_SCALE;
                    this.adapter_.updateCssVariable(VAR_FG_SIZE, this.initialSize_ + "px");
                    this.adapter_.updateCssVariable(VAR_FG_SCALE, this.fgScale_);
                    if (this.adapter_.isUnbounded()) {
                        this.unboundedCoords_ = {
                            left: Math.round(this.frame_.width / 2 - this.initialSize_ / 2),
                            top: Math.round(this.frame_.height / 2 - this.initialSize_ / 2)
                        };
                        this.adapter_.updateCssVariable(VAR_LEFT, this.unboundedCoords_.left + "px");
                        this.adapter_.updateCssVariable(VAR_TOP, this.unboundedCoords_.top + "px");
                    }
                };
                return MDCRippleFoundation;
            }(foundation_1.MDCFoundation);
            exports.MDCRippleFoundation = MDCRippleFoundation;
            exports.default = MDCRippleFoundation;
        },
        48: function(module, exports, __webpack_require__) {
            "use strict";
            var __extends = this && this.__extends || function() {
                var _extendStatics = function extendStatics(d, b) {
                    _extendStatics = Object.setPrototypeOf || {
                        __proto__: []
                    } instanceof Array && function(d, b) {
                        d.__proto__ = b;
                    } || function(d, b) {
                        for (var p in b) {
                            if (b.hasOwnProperty(p)) d[p] = b[p];
                        }
                    };
                    return _extendStatics(d, b);
                };
                return function(d, b) {
                    _extendStatics(d, b);
                    function __() {
                        this.constructor = d;
                    }
                    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
                };
            }();
            var __assign = this && this.__assign || function() {
                __assign = Object.assign || function(t) {
                    for (var s, i = 1, n = arguments.length; i < n; i++) {
                        s = arguments[i];
                        for (var p in s) {
                            if (Object.prototype.hasOwnProperty.call(s, p)) t[p] = s[p];
                        }
                    }
                    return t;
                };
                return __assign.apply(this, arguments);
            };
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var foundation_1 = __webpack_require__(0);
            var constants_1 = __webpack_require__(165);
            var MDCTextFieldCharacterCounterFoundation = function(_super) {
                __extends(MDCTextFieldCharacterCounterFoundation, _super);
                function MDCTextFieldCharacterCounterFoundation(adapter) {
                    return _super.call(this, __assign({}, MDCTextFieldCharacterCounterFoundation.defaultAdapter, adapter)) || this;
                }
                Object.defineProperty(MDCTextFieldCharacterCounterFoundation, "cssClasses", {
                    get: function get() {
                        return constants_1.cssClasses;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextFieldCharacterCounterFoundation, "strings", {
                    get: function get() {
                        return constants_1.strings;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextFieldCharacterCounterFoundation, "defaultAdapter", {
                    get: function get() {
                        return {
                            setContent: function setContent() {
                                return undefined;
                            }
                        };
                    },
                    enumerable: true,
                    configurable: true
                });
                MDCTextFieldCharacterCounterFoundation.prototype.setCounterValue = function(currentLength, maxLength) {
                    currentLength = Math.min(currentLength, maxLength);
                    this.adapter_.setContent(currentLength + " / " + maxLength);
                };
                return MDCTextFieldCharacterCounterFoundation;
            }(foundation_1.MDCFoundation);
            exports.MDCTextFieldCharacterCounterFoundation = MDCTextFieldCharacterCounterFoundation;
            exports.default = MDCTextFieldCharacterCounterFoundation;
        },
        49: function(module, exports, __webpack_require__) {
            "use strict";
            var __extends = this && this.__extends || function() {
                var _extendStatics = function extendStatics(d, b) {
                    _extendStatics = Object.setPrototypeOf || {
                        __proto__: []
                    } instanceof Array && function(d, b) {
                        d.__proto__ = b;
                    } || function(d, b) {
                        for (var p in b) {
                            if (b.hasOwnProperty(p)) d[p] = b[p];
                        }
                    };
                    return _extendStatics(d, b);
                };
                return function(d, b) {
                    _extendStatics(d, b);
                    function __() {
                        this.constructor = d;
                    }
                    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
                };
            }();
            var __assign = this && this.__assign || function() {
                __assign = Object.assign || function(t) {
                    for (var s, i = 1, n = arguments.length; i < n; i++) {
                        s = arguments[i];
                        for (var p in s) {
                            if (Object.prototype.hasOwnProperty.call(s, p)) t[p] = s[p];
                        }
                    }
                    return t;
                };
                return __assign.apply(this, arguments);
            };
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var foundation_1 = __webpack_require__(0);
            var constants_1 = __webpack_require__(166);
            var MDCTextFieldHelperTextFoundation = function(_super) {
                __extends(MDCTextFieldHelperTextFoundation, _super);
                function MDCTextFieldHelperTextFoundation(adapter) {
                    return _super.call(this, __assign({}, MDCTextFieldHelperTextFoundation.defaultAdapter, adapter)) || this;
                }
                Object.defineProperty(MDCTextFieldHelperTextFoundation, "cssClasses", {
                    get: function get() {
                        return constants_1.cssClasses;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextFieldHelperTextFoundation, "strings", {
                    get: function get() {
                        return constants_1.strings;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextFieldHelperTextFoundation, "defaultAdapter", {
                    get: function get() {
                        return {
                            addClass: function addClass() {
                                return undefined;
                            },
                            removeClass: function removeClass() {
                                return undefined;
                            },
                            hasClass: function hasClass() {
                                return false;
                            },
                            setAttr: function setAttr() {
                                return undefined;
                            },
                            removeAttr: function removeAttr() {
                                return undefined;
                            },
                            setContent: function setContent() {
                                return undefined;
                            }
                        };
                    },
                    enumerable: true,
                    configurable: true
                });
                MDCTextFieldHelperTextFoundation.prototype.setContent = function(content) {
                    this.adapter_.setContent(content);
                };
                MDCTextFieldHelperTextFoundation.prototype.setPersistent = function(isPersistent) {
                    if (isPersistent) {
                        this.adapter_.addClass(constants_1.cssClasses.HELPER_TEXT_PERSISTENT);
                    } else {
                        this.adapter_.removeClass(constants_1.cssClasses.HELPER_TEXT_PERSISTENT);
                    }
                };
                MDCTextFieldHelperTextFoundation.prototype.setValidation = function(isValidation) {
                    if (isValidation) {
                        this.adapter_.addClass(constants_1.cssClasses.HELPER_TEXT_VALIDATION_MSG);
                    } else {
                        this.adapter_.removeClass(constants_1.cssClasses.HELPER_TEXT_VALIDATION_MSG);
                    }
                };
                MDCTextFieldHelperTextFoundation.prototype.showToScreenReader = function() {
                    this.adapter_.removeAttr(constants_1.strings.ARIA_HIDDEN);
                };
                MDCTextFieldHelperTextFoundation.prototype.setValidity = function(inputIsValid) {
                    var helperTextIsPersistent = this.adapter_.hasClass(constants_1.cssClasses.HELPER_TEXT_PERSISTENT);
                    var helperTextIsValidationMsg = this.adapter_.hasClass(constants_1.cssClasses.HELPER_TEXT_VALIDATION_MSG);
                    var validationMsgNeedsDisplay = helperTextIsValidationMsg && !inputIsValid;
                    if (validationMsgNeedsDisplay) {
                        this.adapter_.setAttr(constants_1.strings.ROLE, "alert");
                    } else {
                        this.adapter_.removeAttr(constants_1.strings.ROLE);
                    }
                    if (!helperTextIsPersistent && !validationMsgNeedsDisplay) {
                        this.hide_();
                    }
                };
                MDCTextFieldHelperTextFoundation.prototype.hide_ = function() {
                    this.adapter_.setAttr(constants_1.strings.ARIA_HIDDEN, "true");
                };
                return MDCTextFieldHelperTextFoundation;
            }(foundation_1.MDCFoundation);
            exports.MDCTextFieldHelperTextFoundation = MDCTextFieldHelperTextFoundation;
            exports.default = MDCTextFieldHelperTextFoundation;
        },
        5: function(module, exports, __webpack_require__) {
            "use strict";
            var __extends = this && this.__extends || function() {
                var _extendStatics = function extendStatics(d, b) {
                    _extendStatics = Object.setPrototypeOf || {
                        __proto__: []
                    } instanceof Array && function(d, b) {
                        d.__proto__ = b;
                    } || function(d, b) {
                        for (var p in b) {
                            if (b.hasOwnProperty(p)) d[p] = b[p];
                        }
                    };
                    return _extendStatics(d, b);
                };
                return function(d, b) {
                    _extendStatics(d, b);
                    function __() {
                        this.constructor = d;
                    }
                    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
                };
            }();
            var __importStar = this && this.__importStar || function(mod) {
                if (mod && mod.__esModule) return mod;
                var result = {};
                if (mod != null) for (var k in mod) {
                    if (Object.hasOwnProperty.call(mod, k)) result[k] = mod[k];
                }
                result["default"] = mod;
                return result;
            };
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var component_1 = __webpack_require__(1);
            var ponyfill_1 = __webpack_require__(2);
            var foundation_1 = __webpack_require__(4);
            var util = __importStar(__webpack_require__(3));
            var MDCRipple = function(_super) {
                __extends(MDCRipple, _super);
                function MDCRipple() {
                    var _this = _super !== null && _super.apply(this, arguments) || this;
                    _this.disabled = false;
                    return _this;
                }
                MDCRipple.attachTo = function(root, opts) {
                    if (opts === void 0) {
                        opts = {
                            isUnbounded: undefined
                        };
                    }
                    var ripple = new MDCRipple(root);
                    if (opts.isUnbounded !== undefined) {
                        ripple.unbounded = opts.isUnbounded;
                    }
                    return ripple;
                };
                MDCRipple.createAdapter = function(instance) {
                    return {
                        addClass: function addClass(className) {
                            return instance.root_.classList.add(className);
                        },
                        browserSupportsCssVars: function browserSupportsCssVars() {
                            return util.supportsCssVariables(window);
                        },
                        computeBoundingRect: function computeBoundingRect() {
                            return instance.root_.getBoundingClientRect();
                        },
                        containsEventTarget: function containsEventTarget(target) {
                            return instance.root_.contains(target);
                        },
                        deregisterDocumentInteractionHandler: function deregisterDocumentInteractionHandler(evtType, handler) {
                            return document.documentElement.removeEventListener(evtType, handler, util.applyPassive());
                        },
                        deregisterInteractionHandler: function deregisterInteractionHandler(evtType, handler) {
                            return instance.root_.removeEventListener(evtType, handler, util.applyPassive());
                        },
                        deregisterResizeHandler: function deregisterResizeHandler(handler) {
                            return window.removeEventListener("resize", handler);
                        },
                        getWindowPageOffset: function getWindowPageOffset() {
                            return {
                                x: window.pageXOffset,
                                y: window.pageYOffset
                            };
                        },
                        isSurfaceActive: function isSurfaceActive() {
                            return ponyfill_1.matches(instance.root_, ":active");
                        },
                        isSurfaceDisabled: function isSurfaceDisabled() {
                            return Boolean(instance.disabled);
                        },
                        isUnbounded: function isUnbounded() {
                            return Boolean(instance.unbounded);
                        },
                        registerDocumentInteractionHandler: function registerDocumentInteractionHandler(evtType, handler) {
                            return document.documentElement.addEventListener(evtType, handler, util.applyPassive());
                        },
                        registerInteractionHandler: function registerInteractionHandler(evtType, handler) {
                            return instance.root_.addEventListener(evtType, handler, util.applyPassive());
                        },
                        registerResizeHandler: function registerResizeHandler(handler) {
                            return window.addEventListener("resize", handler);
                        },
                        removeClass: function removeClass(className) {
                            return instance.root_.classList.remove(className);
                        },
                        updateCssVariable: function updateCssVariable(varName, value) {
                            return instance.root_.style.setProperty(varName, value);
                        }
                    };
                };
                Object.defineProperty(MDCRipple.prototype, "unbounded", {
                    get: function get() {
                        return Boolean(this.unbounded_);
                    },
                    set: function set(unbounded) {
                        this.unbounded_ = Boolean(unbounded);
                        this.setUnbounded_();
                    },
                    enumerable: true,
                    configurable: true
                });
                MDCRipple.prototype.activate = function() {
                    this.foundation_.activate();
                };
                MDCRipple.prototype.deactivate = function() {
                    this.foundation_.deactivate();
                };
                MDCRipple.prototype.layout = function() {
                    this.foundation_.layout();
                };
                MDCRipple.prototype.getDefaultFoundation = function() {
                    return new foundation_1.MDCRippleFoundation(MDCRipple.createAdapter(this));
                };
                MDCRipple.prototype.initialSyncWithDOM = function() {
                    var root = this.root_;
                    this.unbounded = "mdcRippleIsUnbounded" in root.dataset;
                };
                MDCRipple.prototype.setUnbounded_ = function() {
                    this.foundation_.setUnbounded(Boolean(this.unbounded_));
                };
                return MDCRipple;
            }(component_1.MDCComponent);
            exports.MDCRipple = MDCRipple;
        },
        6: function(module, exports, __webpack_require__) {
            "use strict";
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            exports.cssClasses = {
                BG_FOCUSED: "mdc-ripple-upgraded--background-focused",
                FG_ACTIVATION: "mdc-ripple-upgraded--foreground-activation",
                FG_DEACTIVATION: "mdc-ripple-upgraded--foreground-deactivation",
                ROOT: "mdc-ripple-upgraded",
                UNBOUNDED: "mdc-ripple-upgraded--unbounded"
            };
            exports.strings = {
                VAR_FG_SCALE: "--mdc-ripple-fg-scale",
                VAR_FG_SIZE: "--mdc-ripple-fg-size",
                VAR_FG_TRANSLATE_END: "--mdc-ripple-fg-translate-end",
                VAR_FG_TRANSLATE_START: "--mdc-ripple-fg-translate-start",
                VAR_LEFT: "--mdc-ripple-left",
                VAR_TOP: "--mdc-ripple-top"
            };
            exports.numbers = {
                DEACTIVATION_TIMEOUT_MS: 225,
                FG_DEACTIVATION_MS: 150,
                INITIAL_ORIGIN_SCALE: .6,
                PADDING: 10,
                TAP_DELAY_MS: 300
            };
        },
        83: function(module, exports, __webpack_require__) {
            "use strict";
            var __extends = this && this.__extends || function() {
                var _extendStatics = function extendStatics(d, b) {
                    _extendStatics = Object.setPrototypeOf || {
                        __proto__: []
                    } instanceof Array && function(d, b) {
                        d.__proto__ = b;
                    } || function(d, b) {
                        for (var p in b) {
                            if (b.hasOwnProperty(p)) d[p] = b[p];
                        }
                    };
                    return _extendStatics(d, b);
                };
                return function(d, b) {
                    _extendStatics(d, b);
                    function __() {
                        this.constructor = d;
                    }
                    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
                };
            }();
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var component_1 = __webpack_require__(1);
            var foundation_1 = __webpack_require__(48);
            var MDCTextFieldCharacterCounter = function(_super) {
                __extends(MDCTextFieldCharacterCounter, _super);
                function MDCTextFieldCharacterCounter() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                MDCTextFieldCharacterCounter.attachTo = function(root) {
                    return new MDCTextFieldCharacterCounter(root);
                };
                Object.defineProperty(MDCTextFieldCharacterCounter.prototype, "foundation", {
                    get: function get() {
                        return this.foundation_;
                    },
                    enumerable: true,
                    configurable: true
                });
                MDCTextFieldCharacterCounter.prototype.getDefaultFoundation = function() {
                    var _this = this;
                    var adapter = {
                        setContent: function setContent(content) {
                            _this.root_.textContent = content;
                        }
                    };
                    return new foundation_1.MDCTextFieldCharacterCounterFoundation(adapter);
                };
                return MDCTextFieldCharacterCounter;
            }(component_1.MDCComponent);
            exports.MDCTextFieldCharacterCounter = MDCTextFieldCharacterCounter;
        },
        84: function(module, exports, __webpack_require__) {
            "use strict";
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var strings = {
                ARIA_CONTROLS: "aria-controls",
                ICON_SELECTOR: ".mdc-text-field__icon",
                INPUT_SELECTOR: ".mdc-text-field__input",
                LABEL_SELECTOR: ".mdc-floating-label",
                LINE_RIPPLE_SELECTOR: ".mdc-line-ripple",
                OUTLINE_SELECTOR: ".mdc-notched-outline"
            };
            exports.strings = strings;
            var cssClasses = {
                DENSE: "mdc-text-field--dense",
                DISABLED: "mdc-text-field--disabled",
                FOCUSED: "mdc-text-field--focused",
                FULLWIDTH: "mdc-text-field--fullwidth",
                HELPER_LINE: "mdc-text-field-helper-line",
                INVALID: "mdc-text-field--invalid",
                NO_LABEL: "mdc-text-field--no-label",
                OUTLINED: "mdc-text-field--outlined",
                ROOT: "mdc-text-field",
                TEXTAREA: "mdc-text-field--textarea",
                WITH_LEADING_ICON: "mdc-text-field--with-leading-icon",
                WITH_TRAILING_ICON: "mdc-text-field--with-trailing-icon"
            };
            exports.cssClasses = cssClasses;
            var numbers = {
                DENSE_LABEL_SCALE: .923,
                LABEL_SCALE: .75
            };
            exports.numbers = numbers;
            var VALIDATION_ATTR_WHITELIST = [ "pattern", "min", "max", "required", "step", "minlength", "maxlength" ];
            exports.VALIDATION_ATTR_WHITELIST = VALIDATION_ATTR_WHITELIST;
            var ALWAYS_FLOAT_TYPES = [ "color", "date", "datetime-local", "month", "range", "time", "week" ];
            exports.ALWAYS_FLOAT_TYPES = ALWAYS_FLOAT_TYPES;
        },
        85: function(module, exports, __webpack_require__) {
            "use strict";
            var __extends = this && this.__extends || function() {
                var _extendStatics = function extendStatics(d, b) {
                    _extendStatics = Object.setPrototypeOf || {
                        __proto__: []
                    } instanceof Array && function(d, b) {
                        d.__proto__ = b;
                    } || function(d, b) {
                        for (var p in b) {
                            if (b.hasOwnProperty(p)) d[p] = b[p];
                        }
                    };
                    return _extendStatics(d, b);
                };
                return function(d, b) {
                    _extendStatics(d, b);
                    function __() {
                        this.constructor = d;
                    }
                    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
                };
            }();
            var __assign = this && this.__assign || function() {
                __assign = Object.assign || function(t) {
                    for (var s, i = 1, n = arguments.length; i < n; i++) {
                        s = arguments[i];
                        for (var p in s) {
                            if (Object.prototype.hasOwnProperty.call(s, p)) t[p] = s[p];
                        }
                    }
                    return t;
                };
                return __assign.apply(this, arguments);
            };
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var foundation_1 = __webpack_require__(0);
            var constants_1 = __webpack_require__(84);
            var POINTERDOWN_EVENTS = [ "mousedown", "touchstart" ];
            var INTERACTION_EVENTS = [ "click", "keydown" ];
            var MDCTextFieldFoundation = function(_super) {
                __extends(MDCTextFieldFoundation, _super);
                function MDCTextFieldFoundation(adapter, foundationMap) {
                    if (foundationMap === void 0) {
                        foundationMap = {};
                    }
                    var _this = _super.call(this, __assign({}, MDCTextFieldFoundation.defaultAdapter, adapter)) || this;
                    _this.isFocused_ = false;
                    _this.receivedUserInput_ = false;
                    _this.isValid_ = true;
                    _this.useNativeValidation_ = true;
                    _this.helperText_ = foundationMap.helperText;
                    _this.characterCounter_ = foundationMap.characterCounter;
                    _this.leadingIcon_ = foundationMap.leadingIcon;
                    _this.trailingIcon_ = foundationMap.trailingIcon;
                    _this.inputFocusHandler_ = function() {
                        return _this.activateFocus();
                    };
                    _this.inputBlurHandler_ = function() {
                        return _this.deactivateFocus();
                    };
                    _this.inputInputHandler_ = function() {
                        return _this.handleInput();
                    };
                    _this.setPointerXOffset_ = function(evt) {
                        return _this.setTransformOrigin(evt);
                    };
                    _this.textFieldInteractionHandler_ = function() {
                        return _this.handleTextFieldInteraction();
                    };
                    _this.validationAttributeChangeHandler_ = function(attributesList) {
                        return _this.handleValidationAttributeChange(attributesList);
                    };
                    return _this;
                }
                Object.defineProperty(MDCTextFieldFoundation, "cssClasses", {
                    get: function get() {
                        return constants_1.cssClasses;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextFieldFoundation, "strings", {
                    get: function get() {
                        return constants_1.strings;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextFieldFoundation, "numbers", {
                    get: function get() {
                        return constants_1.numbers;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextFieldFoundation.prototype, "shouldAlwaysFloat_", {
                    get: function get() {
                        var type = this.getNativeInput_().type;
                        return constants_1.ALWAYS_FLOAT_TYPES.indexOf(type) >= 0;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextFieldFoundation.prototype, "shouldFloat", {
                    get: function get() {
                        return this.shouldAlwaysFloat_ || this.isFocused_ || Boolean(this.getValue()) || this.isBadInput_();
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextFieldFoundation.prototype, "shouldShake", {
                    get: function get() {
                        return !this.isFocused_ && !this.isValid() && Boolean(this.getValue());
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextFieldFoundation, "defaultAdapter", {
                    get: function get() {
                        return {
                            addClass: function addClass() {
                                return undefined;
                            },
                            removeClass: function removeClass() {
                                return undefined;
                            },
                            hasClass: function hasClass() {
                                return true;
                            },
                            registerTextFieldInteractionHandler: function registerTextFieldInteractionHandler() {
                                return undefined;
                            },
                            deregisterTextFieldInteractionHandler: function deregisterTextFieldInteractionHandler() {
                                return undefined;
                            },
                            registerInputInteractionHandler: function registerInputInteractionHandler() {
                                return undefined;
                            },
                            deregisterInputInteractionHandler: function deregisterInputInteractionHandler() {
                                return undefined;
                            },
                            registerValidationAttributeChangeHandler: function registerValidationAttributeChangeHandler() {
                                return new MutationObserver(function() {
                                    return undefined;
                                });
                            },
                            deregisterValidationAttributeChangeHandler: function deregisterValidationAttributeChangeHandler() {
                                return undefined;
                            },
                            getNativeInput: function getNativeInput() {
                                return null;
                            },
                            isFocused: function isFocused() {
                                return false;
                            },
                            activateLineRipple: function activateLineRipple() {
                                return undefined;
                            },
                            deactivateLineRipple: function deactivateLineRipple() {
                                return undefined;
                            },
                            setLineRippleTransformOrigin: function setLineRippleTransformOrigin() {
                                return undefined;
                            },
                            shakeLabel: function shakeLabel() {
                                return undefined;
                            },
                            floatLabel: function floatLabel() {
                                return undefined;
                            },
                            hasLabel: function hasLabel() {
                                return false;
                            },
                            getLabelWidth: function getLabelWidth() {
                                return 0;
                            },
                            hasOutline: function hasOutline() {
                                return false;
                            },
                            notchOutline: function notchOutline() {
                                return undefined;
                            },
                            closeOutline: function closeOutline() {
                                return undefined;
                            }
                        };
                    },
                    enumerable: true,
                    configurable: true
                });
                MDCTextFieldFoundation.prototype.init = function() {
                    var _this = this;
                    if (this.adapter_.isFocused()) {
                        this.inputFocusHandler_();
                    } else if (this.adapter_.hasLabel() && this.shouldFloat) {
                        this.notchOutline(true);
                        this.adapter_.floatLabel(true);
                    }
                    this.adapter_.registerInputInteractionHandler("focus", this.inputFocusHandler_);
                    this.adapter_.registerInputInteractionHandler("blur", this.inputBlurHandler_);
                    this.adapter_.registerInputInteractionHandler("input", this.inputInputHandler_);
                    POINTERDOWN_EVENTS.forEach(function(evtType) {
                        _this.adapter_.registerInputInteractionHandler(evtType, _this.setPointerXOffset_);
                    });
                    INTERACTION_EVENTS.forEach(function(evtType) {
                        _this.adapter_.registerTextFieldInteractionHandler(evtType, _this.textFieldInteractionHandler_);
                    });
                    this.validationObserver_ = this.adapter_.registerValidationAttributeChangeHandler(this.validationAttributeChangeHandler_);
                    this.setCharacterCounter_(this.getValue().length);
                };
                MDCTextFieldFoundation.prototype.destroy = function() {
                    var _this = this;
                    this.adapter_.deregisterInputInteractionHandler("focus", this.inputFocusHandler_);
                    this.adapter_.deregisterInputInteractionHandler("blur", this.inputBlurHandler_);
                    this.adapter_.deregisterInputInteractionHandler("input", this.inputInputHandler_);
                    POINTERDOWN_EVENTS.forEach(function(evtType) {
                        _this.adapter_.deregisterInputInteractionHandler(evtType, _this.setPointerXOffset_);
                    });
                    INTERACTION_EVENTS.forEach(function(evtType) {
                        _this.adapter_.deregisterTextFieldInteractionHandler(evtType, _this.textFieldInteractionHandler_);
                    });
                    this.adapter_.deregisterValidationAttributeChangeHandler(this.validationObserver_);
                };
                MDCTextFieldFoundation.prototype.handleTextFieldInteraction = function() {
                    var nativeInput = this.adapter_.getNativeInput();
                    if (nativeInput && nativeInput.disabled) {
                        return;
                    }
                    this.receivedUserInput_ = true;
                };
                MDCTextFieldFoundation.prototype.handleValidationAttributeChange = function(attributesList) {
                    var _this = this;
                    attributesList.some(function(attributeName) {
                        if (constants_1.VALIDATION_ATTR_WHITELIST.indexOf(attributeName) > -1) {
                            _this.styleValidity_(true);
                            return true;
                        }
                        return false;
                    });
                    if (attributesList.indexOf("maxlength") > -1) {
                        this.setCharacterCounter_(this.getValue().length);
                    }
                };
                MDCTextFieldFoundation.prototype.notchOutline = function(openNotch) {
                    if (!this.adapter_.hasOutline()) {
                        return;
                    }
                    if (openNotch) {
                        var isDense = this.adapter_.hasClass(constants_1.cssClasses.DENSE);
                        var labelScale = isDense ? constants_1.numbers.DENSE_LABEL_SCALE : constants_1.numbers.LABEL_SCALE;
                        var labelWidth = this.adapter_.getLabelWidth() * labelScale;
                        this.adapter_.notchOutline(labelWidth);
                    } else {
                        this.adapter_.closeOutline();
                    }
                };
                MDCTextFieldFoundation.prototype.activateFocus = function() {
                    this.isFocused_ = true;
                    this.styleFocused_(this.isFocused_);
                    this.adapter_.activateLineRipple();
                    if (this.adapter_.hasLabel()) {
                        this.notchOutline(this.shouldFloat);
                        this.adapter_.floatLabel(this.shouldFloat);
                        this.adapter_.shakeLabel(this.shouldShake);
                    }
                    if (this.helperText_) {
                        this.helperText_.showToScreenReader();
                    }
                };
                MDCTextFieldFoundation.prototype.setTransformOrigin = function(evt) {
                    var touches = evt.touches;
                    var targetEvent = touches ? touches[0] : evt;
                    var targetClientRect = targetEvent.target.getBoundingClientRect();
                    var normalizedX = targetEvent.clientX - targetClientRect.left;
                    this.adapter_.setLineRippleTransformOrigin(normalizedX);
                };
                MDCTextFieldFoundation.prototype.handleInput = function() {
                    this.autoCompleteFocus();
                    this.setCharacterCounter_(this.getValue().length);
                };
                MDCTextFieldFoundation.prototype.autoCompleteFocus = function() {
                    if (!this.receivedUserInput_) {
                        this.activateFocus();
                    }
                };
                MDCTextFieldFoundation.prototype.deactivateFocus = function() {
                    this.isFocused_ = false;
                    this.adapter_.deactivateLineRipple();
                    var isValid = this.isValid();
                    this.styleValidity_(isValid);
                    this.styleFocused_(this.isFocused_);
                    if (this.adapter_.hasLabel()) {
                        this.notchOutline(this.shouldFloat);
                        this.adapter_.floatLabel(this.shouldFloat);
                        this.adapter_.shakeLabel(this.shouldShake);
                    }
                    if (!this.shouldFloat) {
                        this.receivedUserInput_ = false;
                    }
                };
                MDCTextFieldFoundation.prototype.getValue = function() {
                    return this.getNativeInput_().value;
                };
                MDCTextFieldFoundation.prototype.setValue = function(value) {
                    if (this.getValue() !== value) {
                        this.getNativeInput_().value = value;
                    }
                    this.setCharacterCounter_(value.length);
                    var isValid = this.isValid();
                    this.styleValidity_(isValid);
                    if (this.adapter_.hasLabel()) {
                        this.notchOutline(this.shouldFloat);
                        this.adapter_.floatLabel(this.shouldFloat);
                        this.adapter_.shakeLabel(this.shouldShake);
                    }
                };
                MDCTextFieldFoundation.prototype.isValid = function() {
                    return this.useNativeValidation_ ? this.isNativeInputValid_() : this.isValid_;
                };
                MDCTextFieldFoundation.prototype.setValid = function(isValid) {
                    this.isValid_ = isValid;
                    this.styleValidity_(isValid);
                    var shouldShake = !isValid && !this.isFocused_;
                    if (this.adapter_.hasLabel()) {
                        this.adapter_.shakeLabel(shouldShake);
                    }
                };
                MDCTextFieldFoundation.prototype.setUseNativeValidation = function(useNativeValidation) {
                    this.useNativeValidation_ = useNativeValidation;
                };
                MDCTextFieldFoundation.prototype.isDisabled = function() {
                    return this.getNativeInput_().disabled;
                };
                MDCTextFieldFoundation.prototype.setDisabled = function(disabled) {
                    this.getNativeInput_().disabled = disabled;
                    this.styleDisabled_(disabled);
                };
                MDCTextFieldFoundation.prototype.setHelperTextContent = function(content) {
                    if (this.helperText_) {
                        this.helperText_.setContent(content);
                    }
                };
                MDCTextFieldFoundation.prototype.setLeadingIconAriaLabel = function(label) {
                    if (this.leadingIcon_) {
                        this.leadingIcon_.setAriaLabel(label);
                    }
                };
                MDCTextFieldFoundation.prototype.setLeadingIconContent = function(content) {
                    if (this.leadingIcon_) {
                        this.leadingIcon_.setContent(content);
                    }
                };
                MDCTextFieldFoundation.prototype.setTrailingIconAriaLabel = function(label) {
                    if (this.trailingIcon_) {
                        this.trailingIcon_.setAriaLabel(label);
                    }
                };
                MDCTextFieldFoundation.prototype.setTrailingIconContent = function(content) {
                    if (this.trailingIcon_) {
                        this.trailingIcon_.setContent(content);
                    }
                };
                MDCTextFieldFoundation.prototype.setCharacterCounter_ = function(currentLength) {
                    if (!this.characterCounter_) {
                        return;
                    }
                    var maxLength = this.getNativeInput_().maxLength;
                    if (maxLength === -1) {
                        throw new Error("MDCTextFieldFoundation: Expected maxlength html property on text input or textarea.");
                    }
                    this.characterCounter_.setCounterValue(currentLength, maxLength);
                };
                MDCTextFieldFoundation.prototype.isBadInput_ = function() {
                    return this.getNativeInput_().validity.badInput || false;
                };
                MDCTextFieldFoundation.prototype.isNativeInputValid_ = function() {
                    return this.getNativeInput_().validity.valid;
                };
                MDCTextFieldFoundation.prototype.styleValidity_ = function(isValid) {
                    var INVALID = MDCTextFieldFoundation.cssClasses.INVALID;
                    if (isValid) {
                        this.adapter_.removeClass(INVALID);
                    } else {
                        this.adapter_.addClass(INVALID);
                    }
                    if (this.helperText_) {
                        this.helperText_.setValidity(isValid);
                    }
                };
                MDCTextFieldFoundation.prototype.styleFocused_ = function(isFocused) {
                    var FOCUSED = MDCTextFieldFoundation.cssClasses.FOCUSED;
                    if (isFocused) {
                        this.adapter_.addClass(FOCUSED);
                    } else {
                        this.adapter_.removeClass(FOCUSED);
                    }
                };
                MDCTextFieldFoundation.prototype.styleDisabled_ = function(isDisabled) {
                    var _a = MDCTextFieldFoundation.cssClasses, DISABLED = _a.DISABLED, INVALID = _a.INVALID;
                    if (isDisabled) {
                        this.adapter_.addClass(DISABLED);
                        this.adapter_.removeClass(INVALID);
                    } else {
                        this.adapter_.removeClass(DISABLED);
                    }
                    if (this.leadingIcon_) {
                        this.leadingIcon_.setDisabled(isDisabled);
                    }
                    if (this.trailingIcon_) {
                        this.trailingIcon_.setDisabled(isDisabled);
                    }
                };
                MDCTextFieldFoundation.prototype.getNativeInput_ = function() {
                    var nativeInput = this.adapter_ ? this.adapter_.getNativeInput() : null;
                    return nativeInput || {
                        disabled: false,
                        maxLength: -1,
                        type: "input",
                        validity: {
                            badInput: false,
                            valid: true
                        },
                        value: ""
                    };
                };
                return MDCTextFieldFoundation;
            }(foundation_1.MDCFoundation);
            exports.MDCTextFieldFoundation = MDCTextFieldFoundation;
            exports.default = MDCTextFieldFoundation;
        },
        86: function(module, exports, __webpack_require__) {
            "use strict";
            var __extends = this && this.__extends || function() {
                var _extendStatics = function extendStatics(d, b) {
                    _extendStatics = Object.setPrototypeOf || {
                        __proto__: []
                    } instanceof Array && function(d, b) {
                        d.__proto__ = b;
                    } || function(d, b) {
                        for (var p in b) {
                            if (b.hasOwnProperty(p)) d[p] = b[p];
                        }
                    };
                    return _extendStatics(d, b);
                };
                return function(d, b) {
                    _extendStatics(d, b);
                    function __() {
                        this.constructor = d;
                    }
                    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
                };
            }();
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var component_1 = __webpack_require__(1);
            var foundation_1 = __webpack_require__(49);
            var MDCTextFieldHelperText = function(_super) {
                __extends(MDCTextFieldHelperText, _super);
                function MDCTextFieldHelperText() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                MDCTextFieldHelperText.attachTo = function(root) {
                    return new MDCTextFieldHelperText(root);
                };
                Object.defineProperty(MDCTextFieldHelperText.prototype, "foundation", {
                    get: function get() {
                        return this.foundation_;
                    },
                    enumerable: true,
                    configurable: true
                });
                MDCTextFieldHelperText.prototype.getDefaultFoundation = function() {
                    var _this = this;
                    var adapter = {
                        addClass: function addClass(className) {
                            return _this.root_.classList.add(className);
                        },
                        removeClass: function removeClass(className) {
                            return _this.root_.classList.remove(className);
                        },
                        hasClass: function hasClass(className) {
                            return _this.root_.classList.contains(className);
                        },
                        setAttr: function setAttr(attr, value) {
                            return _this.root_.setAttribute(attr, value);
                        },
                        removeAttr: function removeAttr(attr) {
                            return _this.root_.removeAttribute(attr);
                        },
                        setContent: function setContent(content) {
                            _this.root_.textContent = content;
                        }
                    };
                    return new foundation_1.MDCTextFieldHelperTextFoundation(adapter);
                };
                return MDCTextFieldHelperText;
            }(component_1.MDCComponent);
            exports.MDCTextFieldHelperText = MDCTextFieldHelperText;
        },
        87: function(module, exports, __webpack_require__) {
            "use strict";
            var __extends = this && this.__extends || function() {
                var _extendStatics = function extendStatics(d, b) {
                    _extendStatics = Object.setPrototypeOf || {
                        __proto__: []
                    } instanceof Array && function(d, b) {
                        d.__proto__ = b;
                    } || function(d, b) {
                        for (var p in b) {
                            if (b.hasOwnProperty(p)) d[p] = b[p];
                        }
                    };
                    return _extendStatics(d, b);
                };
                return function(d, b) {
                    _extendStatics(d, b);
                    function __() {
                        this.constructor = d;
                    }
                    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
                };
            }();
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var component_1 = __webpack_require__(1);
            var foundation_1 = __webpack_require__(88);
            var MDCTextFieldIcon = function(_super) {
                __extends(MDCTextFieldIcon, _super);
                function MDCTextFieldIcon() {
                    return _super !== null && _super.apply(this, arguments) || this;
                }
                MDCTextFieldIcon.attachTo = function(root) {
                    return new MDCTextFieldIcon(root);
                };
                Object.defineProperty(MDCTextFieldIcon.prototype, "foundation", {
                    get: function get() {
                        return this.foundation_;
                    },
                    enumerable: true,
                    configurable: true
                });
                MDCTextFieldIcon.prototype.getDefaultFoundation = function() {
                    var _this = this;
                    var adapter = {
                        getAttr: function getAttr(attr) {
                            return _this.root_.getAttribute(attr);
                        },
                        setAttr: function setAttr(attr, value) {
                            return _this.root_.setAttribute(attr, value);
                        },
                        removeAttr: function removeAttr(attr) {
                            return _this.root_.removeAttribute(attr);
                        },
                        setContent: function setContent(content) {
                            _this.root_.textContent = content;
                        },
                        registerInteractionHandler: function registerInteractionHandler(evtType, handler) {
                            return _this.listen(evtType, handler);
                        },
                        deregisterInteractionHandler: function deregisterInteractionHandler(evtType, handler) {
                            return _this.unlisten(evtType, handler);
                        },
                        notifyIconAction: function notifyIconAction() {
                            return _this.emit(foundation_1.MDCTextFieldIconFoundation.strings.ICON_EVENT, {}, true);
                        }
                    };
                    return new foundation_1.MDCTextFieldIconFoundation(adapter);
                };
                return MDCTextFieldIcon;
            }(component_1.MDCComponent);
            exports.MDCTextFieldIcon = MDCTextFieldIcon;
        },
        88: function(module, exports, __webpack_require__) {
            "use strict";
            var __extends = this && this.__extends || function() {
                var _extendStatics = function extendStatics(d, b) {
                    _extendStatics = Object.setPrototypeOf || {
                        __proto__: []
                    } instanceof Array && function(d, b) {
                        d.__proto__ = b;
                    } || function(d, b) {
                        for (var p in b) {
                            if (b.hasOwnProperty(p)) d[p] = b[p];
                        }
                    };
                    return _extendStatics(d, b);
                };
                return function(d, b) {
                    _extendStatics(d, b);
                    function __() {
                        this.constructor = d;
                    }
                    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
                };
            }();
            var __assign = this && this.__assign || function() {
                __assign = Object.assign || function(t) {
                    for (var s, i = 1, n = arguments.length; i < n; i++) {
                        s = arguments[i];
                        for (var p in s) {
                            if (Object.prototype.hasOwnProperty.call(s, p)) t[p] = s[p];
                        }
                    }
                    return t;
                };
                return __assign.apply(this, arguments);
            };
            Object.defineProperty(exports, "__esModule", {
                value: true
            });
            var foundation_1 = __webpack_require__(0);
            var constants_1 = __webpack_require__(167);
            var INTERACTION_EVENTS = [ "click", "keydown" ];
            var MDCTextFieldIconFoundation = function(_super) {
                __extends(MDCTextFieldIconFoundation, _super);
                function MDCTextFieldIconFoundation(adapter) {
                    var _this = _super.call(this, __assign({}, MDCTextFieldIconFoundation.defaultAdapter, adapter)) || this;
                    _this.savedTabIndex_ = null;
                    _this.interactionHandler_ = function(evt) {
                        return _this.handleInteraction(evt);
                    };
                    return _this;
                }
                Object.defineProperty(MDCTextFieldIconFoundation, "strings", {
                    get: function get() {
                        return constants_1.strings;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextFieldIconFoundation, "cssClasses", {
                    get: function get() {
                        return constants_1.cssClasses;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(MDCTextFieldIconFoundation, "defaultAdapter", {
                    get: function get() {
                        return {
                            getAttr: function getAttr() {
                                return null;
                            },
                            setAttr: function setAttr() {
                                return undefined;
                            },
                            removeAttr: function removeAttr() {
                                return undefined;
                            },
                            setContent: function setContent() {
                                return undefined;
                            },
                            registerInteractionHandler: function registerInteractionHandler() {
                                return undefined;
                            },
                            deregisterInteractionHandler: function deregisterInteractionHandler() {
                                return undefined;
                            },
                            notifyIconAction: function notifyIconAction() {
                                return undefined;
                            }
                        };
                    },
                    enumerable: true,
                    configurable: true
                });
                MDCTextFieldIconFoundation.prototype.init = function() {
                    var _this = this;
                    this.savedTabIndex_ = this.adapter_.getAttr("tabindex");
                    INTERACTION_EVENTS.forEach(function(evtType) {
                        _this.adapter_.registerInteractionHandler(evtType, _this.interactionHandler_);
                    });
                };
                MDCTextFieldIconFoundation.prototype.destroy = function() {
                    var _this = this;
                    INTERACTION_EVENTS.forEach(function(evtType) {
                        _this.adapter_.deregisterInteractionHandler(evtType, _this.interactionHandler_);
                    });
                };
                MDCTextFieldIconFoundation.prototype.setDisabled = function(disabled) {
                    if (!this.savedTabIndex_) {
                        return;
                    }
                    if (disabled) {
                        this.adapter_.setAttr("tabindex", "-1");
                        this.adapter_.removeAttr("role");
                    } else {
                        this.adapter_.setAttr("tabindex", this.savedTabIndex_);
                        this.adapter_.setAttr("role", constants_1.strings.ICON_ROLE);
                    }
                };
                MDCTextFieldIconFoundation.prototype.setAriaLabel = function(label) {
                    this.adapter_.setAttr("aria-label", label);
                };
                MDCTextFieldIconFoundation.prototype.setContent = function(content) {
                    this.adapter_.setContent(content);
                };
                MDCTextFieldIconFoundation.prototype.handleInteraction = function(evt) {
                    var isEnterKey = evt.key === "Enter" || evt.keyCode === 13;
                    if (evt.type === "click" || isEnterKey) {
                        this.adapter_.notifyIconAction();
                    }
                };
                return MDCTextFieldIconFoundation;
            }(foundation_1.MDCFoundation);
            exports.MDCTextFieldIconFoundation = MDCTextFieldIconFoundation;
            exports.default = MDCTextFieldIconFoundation;
        }
    });
});
//# sourceMappingURL=mdc.textfield.js.map