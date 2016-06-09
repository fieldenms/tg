(function(win){
    // Get TG object. If this object wasn't initialise yet then initialisse it and set to TG variable on window, and local tg varibale.
    var tg = win.TG = win.TG || {};
    // Initialising AbstractEntity object if it doesn't exists.
    tg.AbstractEntity = tg.AbstractEntity || {
        test : function() {
            return "hello";
        }
    }
     
})(window);