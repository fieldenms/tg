(function () {
    var template = document.querySelector('#app_template'),
        DEFAULT_ITEM = 'My Profile';

    template.menuItems = [
        {
            name: 'My Profile',
            url: '/resources/my-profile.html'
        }, {
            name: 'Personnel',
            url: '/centre/fielden.main.menu.tablecodes.MiPerson'
        }, {
            name: 'Custom View',
            url: '/resources/custom/custom-view.html'
        }
    ];

    template.addEventListener('template-bound', function (e) {
        /*var keys = document.querySelector('#keys');

            // Allow selecting pages by num keypad. Dynamically add
            // [1, template.pages.length] to key mappings.
            var keysToAdd = Array.apply(null, template.pages).map(function (x, i) {
                return i + 1;
            }).reduce(function (x, y) {
                return x + ' ' + y;
            });
            keys.keys += ' ' + keysToAdd;*/

        this.selectedItem = this.selectedItem || DEFAULT_ITEM; // Select initial route.
    });

    template.menuItemSelected = function (e, detail, sender) {
        var scaffold;
        if (detail.isSelected) {
            scaffold = document.querySelector('#scaffold');
            scaffold.closeDrawer();
        };
    };
})();