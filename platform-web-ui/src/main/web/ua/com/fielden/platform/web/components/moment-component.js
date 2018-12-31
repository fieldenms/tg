if (typeof module === 'object') {
    window.module = module; module = undefined;
}
Promise.all([
    import('/resources/moment/moment-with-locales.min.js'),
    import('/resources/moment/moment-timezone-with-data.min.js'),
])
.then(([module1, module2]) => {
    if (window.module) {
        module = window.module;
    }
});
