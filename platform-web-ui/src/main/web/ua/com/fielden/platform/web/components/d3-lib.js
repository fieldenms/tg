import '/resources/d3/d3.min.js';	
 
SVGElement.prototype.getTransformToElement = SVGElement.prototype.getTransformToElement || function (elem) {	
    return elem.getScreenCTM().inverse().multiply(this.getScreenCTM());	
};	
    