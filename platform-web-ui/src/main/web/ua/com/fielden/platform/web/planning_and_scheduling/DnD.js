(function() {
    var toolTip,
        dragObject,
        dragStarted = false,
        no_drop = $('<img id="tipimg" src = "no-drop24.png"/>'),
        drop = $('<img id="tipimg" src = "drop-24.png"/>'),
        dragSupportList = [],
        DnD = {},
        indexOfDnDElemFor = function(elem) {
            var index;
            for(index = 0; index < dragSupportList; index += 1) {
                if (dragSupportList[index].elem === elem) {
                    return index;
                }
            }
            return -1;
        },
        getTip = function() {
            if(!toolTip) {
                $(document.body).append('<div id="tip" style="cursor:default; pointer-events: none; position: absolute;top: 0px; left: 0px"></div>');
                toolTip = $("#tip").hide();
            }
            return toolTip;
        };

    DnD.addDnDSupportFor = function(elem, dragFromSupport, dragToSupport) {
        var dNdElem = {
                elem: elem,
                dragFromSupport: dragFromSupport,
                dragToSupport : dragToSupport
            },
            mouseDown = false;

        dragSupportList.push(dNdElem);
        $(elem).on("mousedown touchstart", function (event) {
            if (dragFromSupport) {
                mouseDown = true;
            }
        }).on("mousemove touchmove", function (event) {
            var image, newImage;
            if (mouseDown && !dragStarted && dragFromSupport && dragFromSupport.canDragFrom(event)) {
                dragObject = dragFromSupport.getDragObject(event);
                dragStarted = true;
                getTip().show().text(dragObject.mark);
            }
            if (dragStarted) {
                image = $("#tipimg", getTip());
                if(dragToSupport && dragToSupport.canDropTo(dragObject, event)) {
                    newImage = drop;
                } else {
                    newImage = no_drop;
                }
                if(image.attr("src") !== newImage.attr("src")) {
                    image.remove();
                    getTip().prepend(newImage);
                }
            }
        }).on("mouseout touchleave", function() {
            mouseDown = false;
            if(dragStarted) {
                $("#tipimg", getTip()).remove();
                getTip().prepend(no_drop);
            }
        }).on("mouseup touchend", function (event){
            mouseDown = false;
            if (dragStarted && dragObject) {
                if(dragToSupport && dragToSupport.canDropTo(dragObject, event)) {
                    dragToSupport.dropTo(dragObject, event);
                }
                dragStarted = false;
                dragObject = null;
            }
        });
    };

    DnD.removeDnDSupportFrom = function (elem) {
        var elemIndex = indexOfDnDElemFor(elem);
        if (elemIndex >= 0){
            dragSupportList.splice(elemIndex, 1);
            $(elem).off("mousedown mousemove mouseout mouseup touchstart touchmove touchend touchleave")
        }
    };

    $(document).mousemove(function(event) {
        if(dragStarted) {
            getTip().css({top: event.pageY - getTip().height() + 12, left:event.pageX - 12});
        }
    }).mouseup(function(event) {
        dragStarted = false;
        dragObject = null;
        getTip().hide();
    });

    window.DnD = DnD;
})();
