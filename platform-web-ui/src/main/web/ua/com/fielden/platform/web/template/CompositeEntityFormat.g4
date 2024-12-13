grammar CompositeEntityFormat;

template : (tvTemplate | vsTemplate | zed='z')? EOF ;

tvTemplate : tvPart+ ;
tvPart : number 't' 'v' ;

vsTemplate : vPart ('s'? vPart)* ;
vPart : number 'v' ;

number : '#' numbers+=I ('.' numbers+=I)* ;
I : [1-9] ;
