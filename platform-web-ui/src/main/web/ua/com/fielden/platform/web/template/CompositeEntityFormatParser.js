// Generated from /home/lambda/fielden/git/tg/platform-web-ui/src/main/web/ua/com/fielden/platform/web/template/CompositeEntityFormat.g4 by ANTLR 4.13.2
// jshint ignore: start
import antlr4 from '/resources/polymer/antlr4/dist/antlr4.web.mjs';
import CompositeEntityFormatListener from './CompositeEntityFormatListener.js';
const serializedATN = [4,1,7,51,2,0,7,0,2,1,7,1,2,2,7,2,2,3,7,3,2,4,7,4,
2,5,7,5,1,0,1,0,1,0,3,0,16,8,0,1,0,1,0,1,1,4,1,21,8,1,11,1,12,1,22,1,2,1,
2,1,2,1,2,1,3,1,3,3,3,31,8,3,1,3,5,3,34,8,3,10,3,12,3,37,9,3,1,4,1,4,1,4,
1,5,1,5,1,5,1,5,5,5,46,8,5,10,5,12,5,49,9,5,1,5,0,0,6,0,2,4,6,8,10,0,0,51,
0,15,1,0,0,0,2,20,1,0,0,0,4,24,1,0,0,0,6,28,1,0,0,0,8,38,1,0,0,0,10,41,1,
0,0,0,12,16,3,2,1,0,13,16,3,6,3,0,14,16,5,1,0,0,15,12,1,0,0,0,15,13,1,0,
0,0,15,14,1,0,0,0,15,16,1,0,0,0,16,17,1,0,0,0,17,18,5,0,0,1,18,1,1,0,0,0,
19,21,3,4,2,0,20,19,1,0,0,0,21,22,1,0,0,0,22,20,1,0,0,0,22,23,1,0,0,0,23,
3,1,0,0,0,24,25,3,10,5,0,25,26,5,2,0,0,26,27,5,3,0,0,27,5,1,0,0,0,28,35,
3,8,4,0,29,31,5,4,0,0,30,29,1,0,0,0,30,31,1,0,0,0,31,32,1,0,0,0,32,34,3,
8,4,0,33,30,1,0,0,0,34,37,1,0,0,0,35,33,1,0,0,0,35,36,1,0,0,0,36,7,1,0,0,
0,37,35,1,0,0,0,38,39,3,10,5,0,39,40,5,3,0,0,40,9,1,0,0,0,41,42,5,5,0,0,
42,47,5,7,0,0,43,44,5,6,0,0,44,46,5,7,0,0,45,43,1,0,0,0,46,49,1,0,0,0,47,
45,1,0,0,0,47,48,1,0,0,0,48,11,1,0,0,0,49,47,1,0,0,0,5,15,22,30,35,47];


const atn = new antlr4.atn.ATNDeserializer().deserialize(serializedATN);

const decisionsToDFA = atn.decisionToState.map( (ds, index) => new antlr4.dfa.DFA(ds, index) );

const sharedContextCache = new antlr4.atn.PredictionContextCache();

export default class CompositeEntityFormatParser extends antlr4.Parser {

    static grammarFileName = "CompositeEntityFormat.g4";
    static literalNames = [ null, "'z'", "'t'", "'v'", "'s'", "'#'", "'.'" ];
    static symbolicNames = [ null, null, null, null, null, null, null, "I" ];
    static ruleNames = [ "template", "tvTemplate", "tvPart", "vsTemplate", 
                         "vPart", "number" ];

    constructor(input) {
        super(input);
        this._interp = new antlr4.atn.ParserATNSimulator(this, atn, decisionsToDFA, sharedContextCache);
        this.ruleNames = CompositeEntityFormatParser.ruleNames;
        this.literalNames = CompositeEntityFormatParser.literalNames;
        this.symbolicNames = CompositeEntityFormatParser.symbolicNames;
    }



	template() {
	    let localctx = new TemplateContext(this, this._ctx, this.state);
	    this.enterRule(localctx, 0, CompositeEntityFormatParser.RULE_template);
	    try {
	        this.enterOuterAlt(localctx, 1);
	        this.state = 15;
	        this._errHandler.sync(this);
	        var la_ = this._interp.adaptivePredict(this._input,0,this._ctx);
	        if(la_===1) {
	            this.state = 12;
	            this.tvTemplate();

	        } else if(la_===2) {
	            this.state = 13;
	            this.vsTemplate();

	        } else if(la_===3) {
	            this.state = 14;
	            localctx.zed = this.match(CompositeEntityFormatParser.T__0);

	        }
	        this.state = 17;
	        this.match(CompositeEntityFormatParser.EOF);
	    } catch (re) {
	    	if(re instanceof antlr4.error.RecognitionException) {
		        localctx.exception = re;
		        this._errHandler.reportError(this, re);
		        this._errHandler.recover(this, re);
		    } else {
		    	throw re;
		    }
	    } finally {
	        this.exitRule();
	    }
	    return localctx;
	}



	tvTemplate() {
	    let localctx = new TvTemplateContext(this, this._ctx, this.state);
	    this.enterRule(localctx, 2, CompositeEntityFormatParser.RULE_tvTemplate);
	    var _la = 0;
	    try {
	        this.enterOuterAlt(localctx, 1);
	        this.state = 20; 
	        this._errHandler.sync(this);
	        _la = this._input.LA(1);
	        do {
	            this.state = 19;
	            this.tvPart();
	            this.state = 22; 
	            this._errHandler.sync(this);
	            _la = this._input.LA(1);
	        } while(_la===5);
	    } catch (re) {
	    	if(re instanceof antlr4.error.RecognitionException) {
		        localctx.exception = re;
		        this._errHandler.reportError(this, re);
		        this._errHandler.recover(this, re);
		    } else {
		    	throw re;
		    }
	    } finally {
	        this.exitRule();
	    }
	    return localctx;
	}



	tvPart() {
	    let localctx = new TvPartContext(this, this._ctx, this.state);
	    this.enterRule(localctx, 4, CompositeEntityFormatParser.RULE_tvPart);
	    try {
	        this.enterOuterAlt(localctx, 1);
	        this.state = 24;
	        this.number();
	        this.state = 25;
	        this.match(CompositeEntityFormatParser.T__1);
	        this.state = 26;
	        this.match(CompositeEntityFormatParser.T__2);
	    } catch (re) {
	    	if(re instanceof antlr4.error.RecognitionException) {
		        localctx.exception = re;
		        this._errHandler.reportError(this, re);
		        this._errHandler.recover(this, re);
		    } else {
		    	throw re;
		    }
	    } finally {
	        this.exitRule();
	    }
	    return localctx;
	}



	vsTemplate() {
	    let localctx = new VsTemplateContext(this, this._ctx, this.state);
	    this.enterRule(localctx, 6, CompositeEntityFormatParser.RULE_vsTemplate);
	    var _la = 0;
	    try {
	        this.enterOuterAlt(localctx, 1);
	        this.state = 28;
	        this.vPart();
	        this.state = 35;
	        this._errHandler.sync(this);
	        _la = this._input.LA(1);
	        while(_la===4 || _la===5) {
	            this.state = 30;
	            this._errHandler.sync(this);
	            _la = this._input.LA(1);
	            if(_la===4) {
	                this.state = 29;
	                this.match(CompositeEntityFormatParser.T__3);
	            }

	            this.state = 32;
	            this.vPart();
	            this.state = 37;
	            this._errHandler.sync(this);
	            _la = this._input.LA(1);
	        }
	    } catch (re) {
	    	if(re instanceof antlr4.error.RecognitionException) {
		        localctx.exception = re;
		        this._errHandler.reportError(this, re);
		        this._errHandler.recover(this, re);
		    } else {
		    	throw re;
		    }
	    } finally {
	        this.exitRule();
	    }
	    return localctx;
	}



	vPart() {
	    let localctx = new VPartContext(this, this._ctx, this.state);
	    this.enterRule(localctx, 8, CompositeEntityFormatParser.RULE_vPart);
	    try {
	        this.enterOuterAlt(localctx, 1);
	        this.state = 38;
	        this.number();
	        this.state = 39;
	        this.match(CompositeEntityFormatParser.T__2);
	    } catch (re) {
	    	if(re instanceof antlr4.error.RecognitionException) {
		        localctx.exception = re;
		        this._errHandler.reportError(this, re);
		        this._errHandler.recover(this, re);
		    } else {
		    	throw re;
		    }
	    } finally {
	        this.exitRule();
	    }
	    return localctx;
	}



	number() {
	    let localctx = new NumberContext(this, this._ctx, this.state);
	    this.enterRule(localctx, 10, CompositeEntityFormatParser.RULE_number);
	    var _la = 0;
	    try {
	        this.enterOuterAlt(localctx, 1);
	        this.state = 41;
	        this.match(CompositeEntityFormatParser.T__4);
	        this.state = 42;
	        localctx._I = this.match(CompositeEntityFormatParser.I);
	        localctx.numbers.push(localctx._I);
	        this.state = 47;
	        this._errHandler.sync(this);
	        _la = this._input.LA(1);
	        while(_la===6) {
	            this.state = 43;
	            this.match(CompositeEntityFormatParser.T__5);
	            this.state = 44;
	            localctx._I = this.match(CompositeEntityFormatParser.I);
	            localctx.numbers.push(localctx._I);
	            this.state = 49;
	            this._errHandler.sync(this);
	            _la = this._input.LA(1);
	        }
	    } catch (re) {
	    	if(re instanceof antlr4.error.RecognitionException) {
		        localctx.exception = re;
		        this._errHandler.reportError(this, re);
		        this._errHandler.recover(this, re);
		    } else {
		    	throw re;
		    }
	    } finally {
	        this.exitRule();
	    }
	    return localctx;
	}


}

CompositeEntityFormatParser.EOF = antlr4.Token.EOF;
CompositeEntityFormatParser.T__0 = 1;
CompositeEntityFormatParser.T__1 = 2;
CompositeEntityFormatParser.T__2 = 3;
CompositeEntityFormatParser.T__3 = 4;
CompositeEntityFormatParser.T__4 = 5;
CompositeEntityFormatParser.T__5 = 6;
CompositeEntityFormatParser.I = 7;

CompositeEntityFormatParser.RULE_template = 0;
CompositeEntityFormatParser.RULE_tvTemplate = 1;
CompositeEntityFormatParser.RULE_tvPart = 2;
CompositeEntityFormatParser.RULE_vsTemplate = 3;
CompositeEntityFormatParser.RULE_vPart = 4;
CompositeEntityFormatParser.RULE_number = 5;

class TemplateContext extends antlr4.ParserRuleContext {

    constructor(parser, parent, invokingState) {
        if(parent===undefined) {
            parent = null;
        }
        if(invokingState===undefined || invokingState===null) {
            invokingState = -1;
        }
        super(parent, invokingState);
        this.parser = parser;
        this.ruleIndex = CompositeEntityFormatParser.RULE_template;
        this.zed = null;
    }

	EOF() {
	    return this.getToken(CompositeEntityFormatParser.EOF, 0);
	};

	tvTemplate() {
	    return this.getTypedRuleContext(TvTemplateContext,0);
	};

	vsTemplate() {
	    return this.getTypedRuleContext(VsTemplateContext,0);
	};

	enterRule(listener) {
	    if(listener instanceof CompositeEntityFormatListener ) {
	        listener.enterTemplate(this);
		}
	}

	exitRule(listener) {
	    if(listener instanceof CompositeEntityFormatListener ) {
	        listener.exitTemplate(this);
		}
	}


}



class TvTemplateContext extends antlr4.ParserRuleContext {

    constructor(parser, parent, invokingState) {
        if(parent===undefined) {
            parent = null;
        }
        if(invokingState===undefined || invokingState===null) {
            invokingState = -1;
        }
        super(parent, invokingState);
        this.parser = parser;
        this.ruleIndex = CompositeEntityFormatParser.RULE_tvTemplate;
    }

	tvPart = function(i) {
	    if(i===undefined) {
	        i = null;
	    }
	    if(i===null) {
	        return this.getTypedRuleContexts(TvPartContext);
	    } else {
	        return this.getTypedRuleContext(TvPartContext,i);
	    }
	};

	enterRule(listener) {
	    if(listener instanceof CompositeEntityFormatListener ) {
	        listener.enterTvTemplate(this);
		}
	}

	exitRule(listener) {
	    if(listener instanceof CompositeEntityFormatListener ) {
	        listener.exitTvTemplate(this);
		}
	}


}



class TvPartContext extends antlr4.ParserRuleContext {

    constructor(parser, parent, invokingState) {
        if(parent===undefined) {
            parent = null;
        }
        if(invokingState===undefined || invokingState===null) {
            invokingState = -1;
        }
        super(parent, invokingState);
        this.parser = parser;
        this.ruleIndex = CompositeEntityFormatParser.RULE_tvPart;
    }

	number() {
	    return this.getTypedRuleContext(NumberContext,0);
	};

	enterRule(listener) {
	    if(listener instanceof CompositeEntityFormatListener ) {
	        listener.enterTvPart(this);
		}
	}

	exitRule(listener) {
	    if(listener instanceof CompositeEntityFormatListener ) {
	        listener.exitTvPart(this);
		}
	}


}



class VsTemplateContext extends antlr4.ParserRuleContext {

    constructor(parser, parent, invokingState) {
        if(parent===undefined) {
            parent = null;
        }
        if(invokingState===undefined || invokingState===null) {
            invokingState = -1;
        }
        super(parent, invokingState);
        this.parser = parser;
        this.ruleIndex = CompositeEntityFormatParser.RULE_vsTemplate;
    }

	vPart = function(i) {
	    if(i===undefined) {
	        i = null;
	    }
	    if(i===null) {
	        return this.getTypedRuleContexts(VPartContext);
	    } else {
	        return this.getTypedRuleContext(VPartContext,i);
	    }
	};

	enterRule(listener) {
	    if(listener instanceof CompositeEntityFormatListener ) {
	        listener.enterVsTemplate(this);
		}
	}

	exitRule(listener) {
	    if(listener instanceof CompositeEntityFormatListener ) {
	        listener.exitVsTemplate(this);
		}
	}


}



class VPartContext extends antlr4.ParserRuleContext {

    constructor(parser, parent, invokingState) {
        if(parent===undefined) {
            parent = null;
        }
        if(invokingState===undefined || invokingState===null) {
            invokingState = -1;
        }
        super(parent, invokingState);
        this.parser = parser;
        this.ruleIndex = CompositeEntityFormatParser.RULE_vPart;
    }

	number() {
	    return this.getTypedRuleContext(NumberContext,0);
	};

	enterRule(listener) {
	    if(listener instanceof CompositeEntityFormatListener ) {
	        listener.enterVPart(this);
		}
	}

	exitRule(listener) {
	    if(listener instanceof CompositeEntityFormatListener ) {
	        listener.exitVPart(this);
		}
	}


}



class NumberContext extends antlr4.ParserRuleContext {

    constructor(parser, parent, invokingState) {
        if(parent===undefined) {
            parent = null;
        }
        if(invokingState===undefined || invokingState===null) {
            invokingState = -1;
        }
        super(parent, invokingState);
        this.parser = parser;
        this.ruleIndex = CompositeEntityFormatParser.RULE_number;
        this._I = null;
        this.numbers = [];
    }

	I = function(i) {
		if(i===undefined) {
			i = null;
		}
	    if(i===null) {
	        return this.getTokens(CompositeEntityFormatParser.I);
	    } else {
	        return this.getToken(CompositeEntityFormatParser.I, i);
	    }
	};


	enterRule(listener) {
	    if(listener instanceof CompositeEntityFormatListener ) {
	        listener.enterNumber(this);
		}
	}

	exitRule(listener) {
	    if(listener instanceof CompositeEntityFormatListener ) {
	        listener.exitNumber(this);
		}
	}


}




CompositeEntityFormatParser.TemplateContext = TemplateContext; 
CompositeEntityFormatParser.TvTemplateContext = TvTemplateContext; 
CompositeEntityFormatParser.TvPartContext = TvPartContext; 
CompositeEntityFormatParser.VsTemplateContext = VsTemplateContext; 
CompositeEntityFormatParser.VPartContext = VPartContext; 
CompositeEntityFormatParser.NumberContext = NumberContext; 
