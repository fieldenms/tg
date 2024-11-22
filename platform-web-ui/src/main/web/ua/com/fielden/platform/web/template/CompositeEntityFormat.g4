grammar CompositeEntityFormat;

template : (tvTemplate | vsTemplate | zed='z')? EOF ;

tvTemplate : tvPart+ ;
tvPart : no 't' 'v' ;

vsTemplate : vPart ('s'? vPart)* ;
vPart : no 'v' ;

no : '#' numbers+=I ('.' numbers+=I)* ;
I : [1-9] ;
