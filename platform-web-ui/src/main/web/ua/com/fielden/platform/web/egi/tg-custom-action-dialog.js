import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-overlay-behavior/iron-overlay-behavior.js';
import '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js'
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-icons/av-icons.js';

import '/resources/polymer/@polymer/paper-styles/color.js';
import '/resources/polymer/@polymer/paper-spinner/paper-spinner.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/polymer/@polymer/paper-styles/paper-styles-classes.js';

import '/resources/element_loader/tg-element-loader.js';
import '/resources/components/tg-toast.js';
import '/resources/images/tg-icons.js';

import '/app/tg-app-config.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import {TgFocusRestorationBehavior} from '/resources/actions/tg-focus-restoration-behavior.js'
import {TgTooltipBehavior} from '/resources/components/tg-tooltip-behavior.js';
import {TgBackButtonBehavior} from '/resources/views/tg-back-button-behavior.js'
import {tearDownEvent, isInHierarchy} from '/resources/reflection/tg-polymer-utils.js';