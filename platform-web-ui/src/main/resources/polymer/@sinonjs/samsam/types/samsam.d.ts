import createMatcher = require("./create-matcher");
declare var deepEqualCyclic: (a: any, b: any) => boolean;
import identical = require("./identical");
import isArguments = require("./is-arguments");
import isElement = require("./is-element");
import isMap = require("./is-map");
import isNegZero = require("./is-neg-zero");
import isSet = require("./is-set");
import match = require("./match");
export { createMatcher, deepEqualCyclic as deepEqual, identical, isArguments, isElement, isMap, isNegZero, isSet, match };
