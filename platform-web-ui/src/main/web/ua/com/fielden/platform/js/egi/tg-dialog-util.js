export  function createDialog (id) {
    const dialog = document.createElement('tg-custom-action-dialog');
    dialog.setAttribute("id", id);
    return dialog;
}