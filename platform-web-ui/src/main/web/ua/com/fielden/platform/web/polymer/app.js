(function () {

    var template = document.querySelector('#app_template'),
        DEFAULT_ROUTE = 'custom/my-profile',
        scaffold,
        pages;

    template.pages = [
        {
            name: 'My Profile',
            hash: 'custom/my-profile',
            url: '/resources/my-profile.html',
            lazyLoad: false
        }, {
            name: 'Custom View',
            hash: 'custom/custom-view',
            url: '/resources/custom/custom-view.html',
            lazyLoad: true
        }
        /*, {
            name: 'Personnel',
            hash: 'centre/personnel',
            url: '/resources/centre/entity-centre.html',
            attributes: '{centreName: "fielden.main.menu.tablecodes.MiPerson"}',
            lazyLoad: true
        }*/
    ];

    template.addEventListener('template-bound', function (e) {
        scaffold = document.querySelector('#scaffold');
        pages = document.querySelector('#pages');
        /*var keys = document.querySelector('#keys');

            // Allow selecting pages by num keypad. Dynamically add
            // [1, template.pages.length] to key mappings.
            var keysToAdd = Array.apply(null, template.pages).map(function (x, i) {
                return i + 1;
            }).reduce(function (x, y) {
                return x + ' ' + y;
            });
            keys.keys += ' ' + keysToAdd;*/

        this.route = this.route || DEFAULT_ROUTE; // Select initial route.
    });

    template.menuItemSelected = function (e, detail, sender) {
        if (detail.isSelected) {
            scaffold = document.querySelector('#scaffold');
        }
    };

    template.pageSelected = function (e, detail, sender) {
        var elementToLoad = pages.selectedItem.querySelector('load-element');
        if (!elementToLoad.wasLoaded) {
            pages.selectedItem.querySelector('load-element').load();
        }
    };
})();