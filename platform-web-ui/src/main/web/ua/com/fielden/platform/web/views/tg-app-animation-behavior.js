import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

export const TgAppAnimationBehavior = {
/**
     * Is called before moving on to page that implements this behavior. Prev - the name of previously selected page. 
     */
    configureEntryAnimation: function (prev) {},
    
    /**
     * Is called before moving out of the page that implements this behavior. Next - the name of next selected page.
     */
    configureExitAnimation: function (next) {}

};