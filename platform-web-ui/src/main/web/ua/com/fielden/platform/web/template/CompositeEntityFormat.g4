grammar CompositeEntityFormat;

template : (tvTemplate | 'z')? EOF ;
tvTemplate : tvPart+ ;
tvPart : no 't' 'v' ;
no : '#' I ;
I : [0-9] ;