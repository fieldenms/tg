// Generated from /home/jhou/workspace-java/tg/platform-web-ui/src/main/web/ua/com/fielden/platform/web/template/CompositeEntityFormat.g4 by ANTLR 4.13.2
// jshint ignore: start
import antlr4 from '/resources/polymer/antlr4/dist/antlr4.web.mjs';


const serializedATN = [4,0,7,29,6,-1,2,0,7,0,2,1,7,1,2,2,7,2,2,3,7,3,2,4,
7,4,2,5,7,5,2,6,7,6,1,0,1,0,1,1,1,1,1,2,1,2,1,3,1,3,1,4,1,4,1,5,1,5,1,6,
1,6,0,0,7,1,1,3,2,5,3,7,4,9,5,11,6,13,7,1,0,1,1,0,48,57,28,0,1,1,0,0,0,0,
3,1,0,0,0,0,5,1,0,0,0,0,7,1,0,0,0,0,9,1,0,0,0,0,11,1,0,0,0,0,13,1,0,0,0,
1,15,1,0,0,0,3,17,1,0,0,0,5,19,1,0,0,0,7,21,1,0,0,0,9,23,1,0,0,0,11,25,1,
0,0,0,13,27,1,0,0,0,15,16,5,122,0,0,16,2,1,0,0,0,17,18,5,116,0,0,18,4,1,
0,0,0,19,20,5,118,0,0,20,6,1,0,0,0,21,22,5,115,0,0,22,8,1,0,0,0,23,24,5,
35,0,0,24,10,1,0,0,0,25,26,5,46,0,0,26,12,1,0,0,0,27,28,7,0,0,0,28,14,1,
0,0,0,1,0,0];


const atn = new antlr4.atn.ATNDeserializer().deserialize(serializedATN);

const decisionsToDFA = atn.decisionToState.map( (ds, index) => new antlr4.dfa.DFA(ds, index) );

export default class CompositeEntityFormatLexer extends antlr4.Lexer {

    static grammarFileName = "CompositeEntityFormat.g4";
    static channelNames = [ "DEFAULT_TOKEN_CHANNEL", "HIDDEN" ];
	static modeNames = [ "DEFAULT_MODE" ];
	static literalNames = [ null, "'z'", "'t'", "'v'", "'s'", "'#'", "'.'" ];
	static symbolicNames = [ null, null, null, null, null, null, null, "I" ];
	static ruleNames = [ "T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "I" ];

    constructor(input) {
        super(input)
        this._interp = new antlr4.atn.LexerATNSimulator(this, atn, decisionsToDFA, new antlr4.atn.PredictionContextCache());
    }
}

CompositeEntityFormatLexer.EOF = antlr4.Token.EOF;
CompositeEntityFormatLexer.T__0 = 1;
CompositeEntityFormatLexer.T__1 = 2;
CompositeEntityFormatLexer.T__2 = 3;
CompositeEntityFormatLexer.T__3 = 4;
CompositeEntityFormatLexer.T__4 = 5;
CompositeEntityFormatLexer.T__5 = 6;
CompositeEntityFormatLexer.I = 7;



