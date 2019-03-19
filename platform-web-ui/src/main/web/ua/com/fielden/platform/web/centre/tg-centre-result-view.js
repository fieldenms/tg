<link rel="import" href="/resources/polymer/polymer/polymer.html">
<link rel="import" href="/resources/polymer/paper-styles/paper-styles-classes.html">

<dom-module id="tg-centre-result-view">
    <style>
        :host {
            min-height: 100%;
            @apply(--layout-horizontal);
            @apply(--layout-justified);
        }
    </style>
    <template>
        <content></content>
    </template>
    <script>
        Polymer({
            is: 'tg-centre-result-view',
            
            attached: function () {
                this.classList.add('centreResultView');
            },
                
            
            /**
             * Finds parent centre for this result view. It should exist, otherwise exception will be thrown.
             */
            _findParentCentre: function () {
                let parent = this;
                while (parent && !parent.classList.contains('generatedCentre')) {
                    parent = parent.parentElement;
                }
                if (!parent) {
                    throw 'No parent centre exists for this result view.';
                } else if (parent === this) {
                    throw 'This result view somehow contains generatedCentre class.';
                }
                return parent;
            }
        });
    </script>
</dom-module>