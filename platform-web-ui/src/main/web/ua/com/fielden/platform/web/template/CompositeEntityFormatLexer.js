// Generated from /home/jhou/workspace-java/tg/platform-web-ui/src/main/web/ua/com/fielden/platform/web/template/CompositeEntityFormat.g4 by ANTLR 4.13.2
// jshint ignore: start
import antlr4 from 'antlr4';


const serializedATN = [4,0,5,21,6,-1,2,0,7,0,2,1,7,1,2,2,7,2,2,3,7,3,2,4,
7,4,1,0,1,0,1,1,1,1,1,2,1,2,1,3,1,3,1,4,1,4,0,0,5,1,1,3,2,5,3,7,4,9,5,1,
0,1,1,0,48,57,20,0,1,1,0,0,0,0,3,1,0,0,0,0,5,1,0,0,0,0,7,1,0,0,0,0,9,1,0,
0,0,1,11,1,0,0,0,3,13,1,0,0,0,5,15,1,0,0,0,7,17,1,0,0,0,9,19,1,0,0,0,11,
12,5,122,0,0,12,2,1,0,0,0,13,14,5,116,0,0,14,4,1,0,0,0,15,16,5,118,0,0,16,
6,1,0,0,0,17,18,5,35,0,0,18,8,1,0,0,0,19,20,7,0,0,0,20,10,1,0,0,0,1,0,0];


const atn = new antlr4.atn.ATNDeserializer().deserialize(serializedATN);

const decisionsToDFA = atn.decisionToState.map( (ds, index) => new antlr4.dfa.DFA(ds, index) );

export default class CompositeEntityFormatLexer extends antlr4.Lexer {

    static grammarFileName = "CompositeEntityFormat.g4";
    static channelNames = [ "DEFAULT_TOKEN_CHANNEL", "HIDDEN" ];
	static modeNames = [ "DEFAULT_MODE" ];
	static literalNames = [ null, "'z'", "'t'", "'v'", "'#'" ];
	static symbolicNames = [ null, null, null, null, null, "I" ];
	static ruleNames = [ "T__0", "T__1", "T__2", "T__3", "I" ];

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
CompositeEntityFormatLexer.I = 5;



