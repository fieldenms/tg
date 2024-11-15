grammar CompositeEntityFormat;

template : (tvTemplate | vsTemplate | 'z')? EOF ;

tvTemplate : tvPart+ ;
tvPart : no 't' 'v' ;

vsTemplate : vPart ('s' vPart)* ;
vPart : no 'v' ;

no : '#' I ;
I : [0-9] ;