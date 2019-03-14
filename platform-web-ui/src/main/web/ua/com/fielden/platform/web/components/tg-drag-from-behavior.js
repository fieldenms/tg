import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

let elementToDrag = null;

function dragStartListener (e) {
    elementToDrag = this.getElementToDragFrom().cloneNode(true);
    elementToDrag.style.position = "absolute";
    elementToDrag.style.top = "-100%";
    elementToDrag.style.right = "-100%";
    document.body.appendChild(elementToDrag);
    const dataToDrag = this.getDataToDragFrom();
    Object.keys(dataToDrag).forEach(typeKey => {
        e.dataTransfer.setData(typeKey, dataToDrag[typeKey]);    
    });
    e.dataTransfer.effectAllowed = "copyMove";
    e.dataTransfer.setDragImage(elementToDrag, this.shiftDragX, this.shiftDragY);
}

function dragEndListener(e) {
    Polymer.dom(document.body).removeChild(elementToDrag);
    Polymer.dom.flush();
    elementToDrag = null;
}

export const TgDragFromBehavior = {

    ready: function () {
        this.addEventListener("dragstart", dragStartListener.bind(this));
        this.addEventListener("dragend", dragEndListener.bind(this));
    },
};