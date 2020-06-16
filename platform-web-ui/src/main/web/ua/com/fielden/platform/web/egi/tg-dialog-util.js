export  function createDialog (id) {
    const dialog = document.createElement('tg-custom-action-dialog');
    dialog.setAttribute("id", id);
    dialog.addEventListener("iron-overlay-closed", removeDialogFromDom);
    return dialog;
}

const removeDialogFromDom = function (e) {
    const dialog = e.target;
    document.body.removeChild(dialog);
}