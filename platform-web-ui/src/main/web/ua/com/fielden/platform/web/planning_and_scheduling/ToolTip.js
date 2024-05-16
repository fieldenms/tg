(function() {
    var showTimeout = 700,
        hideTimeout = 5000,
        showTimeoutToClear,
        hideTimeoutToClear,
        currentMousePosX,
        currentMousePosY,
        toolTip,
        getTip = function() {
            if(!toolTip) {
                $(document.body).append('<div id="tooltip" class="tooltip-style" style="cursor:default; pointer-events: none; position: absolute;top: 0px; left: 0px"></div>');
                toolTip = $("#tooltip").hide();
            }
            return toolTip;
        },
        clearTimeouts = function() {
            clearTimeout(showTimeoutToClear);
            clearTimeout(hideTimeoutToClear);
        },
        getTipText = function (node) {
            var currentNode = node;
            while (currentNode && typeof currentNode.getToolTipText !== "function") {
                currentNode = currentNode.parentNode;
            }
            return currentNode && currentNode.getToolTipText();
        },
        ToolTipSupport = {};

    $(document).mouseover(function(event) {
        var toolTipText;
        if(!event.which) {
            toolTipText = getTipText(event.target);
            if (toolTipText) {
                getTip().html(toolTipText);
                showTimeoutToClear = setTimeout(function () {
                    getTip().fadeIn();
                    getTip().css({top: currentMousePosY, left: currentMousePosX + 10});
                    hideTimeoutToClear = setTimeout(function () {
                        getTip().fadeOut();
                    }, hideTimeout);
                }, showTimeout);
            }
        }

    }).mouseout(function() {
        clearTimeouts();
        getTip().fadeOut();
    }).mousemove(function(event) {
        if(event.which) {
            clearTimeouts();
            getTip().fadeOut();
        }
        currentMousePosX = event.pageX;
        currentMousePosY = event.pageY;
    });

    ToolTipSupport.setShowTimeout = function(startTimeout) {
        showTimeout = startTimeout;
    };

    ToolTipSupport.setHideTimeout = function(endTimeout) {
        hideTimeout = endTimeout;
    };
})();
