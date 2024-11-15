// Generated from /home/jhou/workspace-java/tg/platform-web-ui/src/main/web/ua/com/fielden/platform/web/template/CompositeEntityFormat.g4 by ANTLR 4.13.2
// jshint ignore: start
import antlr4 from 'antlr4';
import CompositeEntityFormatListener from './CompositeEntityFormatListener.js';
import CompositeEntityFormatVisitor from './CompositeEntityFormatVisitor.js';

const serializedATN = [4,1,5,27,2,0,7,0,2,1,7,1,2,2,7,2,2,3,7,3,1,0,1,0,
3,0,11,8,0,1,0,1,0,1,1,4,1,16,8,1,11,1,12,1,17,1,2,1,2,1,2,1,2,1,3,1,3,1,
3,1,3,0,0,4,0,2,4,6,0,0,25,0,10,1,0,0,0,2,15,1,0,0,0,4,19,1,0,0,0,6,23,1,
0,0,0,8,11,3,2,1,0,9,11,5,1,0,0,10,8,1,0,0,0,10,9,1,0,0,0,10,11,1,0,0,0,
11,12,1,0,0,0,12,13,5,0,0,1,13,1,1,0,0,0,14,16,3,4,2,0,15,14,1,0,0,0,16,
17,1,0,0,0,17,15,1,0,0,0,17,18,1,0,0,0,18,3,1,0,0,0,19,20,3,6,3,0,20,21,
5,2,0,0,21,22,5,3,0,0,22,5,1,0,0,0,23,24,5,4,0,0,24,25,5,5,0,0,25,7,1,0,
0,0,2,10,17];


const atn = new antlr4.atn.ATNDeserializer().deserialize(serializedATN);

const decisionsToDFA = atn.decisionToState.map( (ds, index) => new antlr4.dfa.DFA(ds, index) );

const sharedContextCache = new antlr4.atn.PredictionContextCache();

export default class CompositeEntityFormatParser extends antlr4.Parser {

    static grammarFileName = "CompositeEntityFormat.g4";
    static literalNames = [ null, "'z'", "'t'", "'v'", "'#'" ];
    static symbolicNames = [ null, null, null, null, null, "I" ];
    static ruleNames = [ "template", "tvTemplate", "tvPart", "no" ];

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
	        this.state = 10;
	        this._errHandler.sync(this);
	        switch (this._input.LA(1)) {
	        case 4:
	        	this.state = 8;
	        	this.tvTemplate();
	        	break;
	        case 1:
	        	this.state = 9;
	        	this.match(CompositeEntityFormatParser.T__0);
	        	break;
	        case -1:
	        	break;
	        default:
	        	break;
	        }
	        this.state = 12;
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
	        this.state = 15; 
	        this._errHandler.sync(this);
	        _la = this._input.LA(1);
	        do {
	            this.state = 14;
	            this.tvPart();
	            this.state = 17; 
	            this._errHandler.sync(this);
	            _la = this._input.LA(1);
	        } while(_la===4);
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
	        this.state = 19;
	        this.no();
	        this.state = 20;
	        this.match(CompositeEntityFormatParser.T__1);
	        this.state = 21;
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



	no() {
	    let localctx = new NoContext(this, this._ctx, this.state);
	    this.enterRule(localctx, 6, CompositeEntityFormatParser.RULE_no);
	    try {
	        this.enterOuterAlt(localctx, 1);
	        this.state = 23;
	        this.match(CompositeEntityFormatParser.T__3);
	        this.state = 24;
	        this.match(CompositeEntityFormatParser.I);
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
CompositeEntityFormatParser.I = 5;

CompositeEntityFormatParser.RULE_template = 0;
CompositeEntityFormatParser.RULE_tvTemplate = 1;
CompositeEntityFormatParser.RULE_tvPart = 2;
CompositeEntityFormatParser.RULE_no = 3;

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
    }

	EOF() {
	    return this.getToken(CompositeEntityFormatParser.EOF, 0);
	};

	tvTemplate() {
	    return this.getTypedRuleContext(TvTemplateContext,0);
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

	accept(visitor) {
	    if ( visitor instanceof CompositeEntityFormatVisitor ) {
	        return visitor.visitTemplate(this);
	    } else {
	        return visitor.visitChildren(this);
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

	accept(visitor) {
	    if ( visitor instanceof CompositeEntityFormatVisitor ) {
	        return visitor.visitTvTemplate(this);
	    } else {
	        return visitor.visitChildren(this);
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

	no() {
	    return this.getTypedRuleContext(NoContext,0);
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

	accept(visitor) {
	    if ( visitor instanceof CompositeEntityFormatVisitor ) {
	        return visitor.visitTvPart(this);
	    } else {
	        return visitor.visitChildren(this);
	    }
	}


}



class NoContext extends antlr4.ParserRuleContext {

    constructor(parser, parent, invokingState) {
        if(parent===undefined) {
            parent = null;
        }
        if(invokingState===undefined || invokingState===null) {
            invokingState = -1;
        }
        super(parent, invokingState);
        this.parser = parser;
        this.ruleIndex = CompositeEntityFormatParser.RULE_no;
    }

	I() {
	    return this.getToken(CompositeEntityFormatParser.I, 0);
	};

	enterRule(listener) {
	    if(listener instanceof CompositeEntityFormatListener ) {
	        listener.enterNo(this);
		}
	}

	exitRule(listener) {
	    if(listener instanceof CompositeEntityFormatListener ) {
	        listener.exitNo(this);
		}
	}

	accept(visitor) {
	    if ( visitor instanceof CompositeEntityFormatVisitor ) {
	        return visitor.visitNo(this);
	    } else {
	        return visitor.visitChildren(this);
	    }
	}


}




CompositeEntityFormatParser.TemplateContext = TemplateContext; 
CompositeEntityFormatParser.TvTemplateContext = TvTemplateContext; 
CompositeEntityFormatParser.TvPartContext = TvPartContext; 
CompositeEntityFormatParser.NoContext = NoContext; 
