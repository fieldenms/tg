(function() {
    "use strict";

    var directives = angular.module("tg.directives", ["matchmedia-ng"]);
    //The top most layout tag.
    directives.directive("tgMediaLayouts", ["matchmedia", function(matchmedia){
        return {
            restrict: "A",
            require: "tgMediaLayouts",
            controller : function() {
                this.layouts = {};
                //This controller function object may have only three variables : desktop, tablet, phone.
                //Those are needed for switching handler. Switching handler must switch between those layouts.
            },
            link: function(scope, element, attr, tgMediaLayouts) {
                var unSub = {},
                    media, orientation, currentLayout,
                //A callback function that handles layout switching
                    layout = function (mediaQueryList) {
                        if (media && orientation &&
                            (tgMediaLayouts.layouts[media + "." + orientation] || tgMediaLayouts.layouts[media])) {
                            if (mediaQueryList.matches) {
                                setUpLayout();
                            }
                        }
                    },
                //Set up specified layout.
                    setUpLayout = function () {
                        var layoutToSet = tgMediaLayouts.layouts[media + "." + orientation] || tgMediaLayouts.layouts[media];
                        if (layoutToSet !== currentLayout) {
                            removeCurrentLayout();
                            layoutToSet.transclude(function (layoutElement, layoutScope) {
                                layoutToSet.scope = layoutScope;
                                layoutToSet.element = layoutElement;
                                element.append(layoutElement);
                                currentLayout = layoutToSet;
                            });
                        }
                    },
                //Helper function that removes current layout.
                    removeCurrentLayout = function () {
                        if (currentLayout && currentLayout.element) {
                            currentLayout.element.remove();
                            delete currentLayout.element;
                        }
                        if (currentLayout && currentLayout.scope) {
                            currentLayout.scope.$destroy();
                            delete currentLayout.scope;
                        }
                    };

                //Listens when media phone is on or off.
                unSub["phone"] = matchmedia.onPhone(function(mediaQueryList) {
                    media = "phone";
                    layout(mediaQueryList);
                }, scope);
                //Listens when media tablet is on or off.
                unSub["tablet"] = matchmedia.onTablet( function(mediaQueryList){
                    media = "tablet";
                    layout(mediaQueryList);
                }, scope);
                //Listens when media desktop is on or off.
                unSub["desktop"] = matchmedia.onDesktop( function(mediaQueryList){
                    media = "desktop";
                    layout(mediaQueryList);
                }, scope);
                //Listens when orientation portrait is on or off.
                unSub["portrait"] = matchmedia.onPortrait(function (mediaQueryList) {
                    orientation = "portrait";
                    layout(mediaQueryList);
                }, scope);
                //Listens when orientation landscape is on or off.
                unSub["landscape"] = matchmedia.onLandscape(function (mediaQueryList) {
                    orientation = "landscape";
                    layout(mediaQueryList);
                }, scope);
                scope.$on('$destroy', function () {
                    unSub['phone']();
                    unSub['tablet']();
                    unSub['desktop']();
                    unSub['portrait']();
                    unSub['landscape']();
                });
            }
        }
    }]);
    //The media tag.
    directives.directive("tgWhenMedia", function() {
        return {
            restrict: "A",
            transclude : "element",
            require: "^tgMediaLayouts",
            link: function (scope, iElement, iAttrs, tgMediaLayouts, transclude) {
                tgMediaLayouts.layouts[iAttrs.tgWhenMedia + (iAttrs.tgWhenOrientation ? "." + iAttrs.tgWhenOrientation : "")] = {transclude: transclude};
            }
        }
    });
})();
