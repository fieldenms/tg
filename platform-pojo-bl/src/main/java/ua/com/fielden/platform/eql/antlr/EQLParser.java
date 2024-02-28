// Generated from EQL.g4 by ANTLR 4.9.3
package ua.com.fielden.platform.eql.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class EQLParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		ABSOF=1, ADD=2, ADDTIMEINTERVALOF=3, ALL=4, ALLOFEXPRESSIONS=5, ALLOFIPARAMS=6, 
		ALLOFMODELS=7, ALLOFPARAMS=8, ALLOFPROPS=9, ALLOFVALUES=10, AND=11, ANY=12, 
		ANYOFEXPRESSIONS=13, ANYOFIPARAMS=14, ANYOFMODELS=15, ANYOFPARAMS=16, 
		ANYOFPROPS=17, ANYOFVALUES=18, AS=19, ASC=20, ASREQUIRED=21, AVGOF=22, 
		AVGOFDISTINCT=23, BEGIN=24, BEGINEXPR=25, BETWEEN=26, CASEWHEN=27, CONCAT=28, 
		COND=29, CONDITION=30, COUNT=31, COUNTALL=32, COUNTOF=33, COUNTOFDISTINCT=34, 
		CRITCONDITION=35, DATEOF=36, DAYOF=37, DAYOFWEEKOF=38, DAYS=39, DESC=40, 
		DIV=41, END=42, ENDASBOOL=43, ENDASDECIMAL=44, ENDASINT=45, ENDASSTR=46, 
		ENDEXPR=47, EQ=48, EXISTS=49, EXISTSALLOF=50, EXISTSANYOF=51, EXPR=52, 
		EXTPROP=53, GE=54, GROUPBY=55, GT=56, HOUROF=57, HOURS=58, IFNULL=59, 
		ILIKE=60, ILIKEWITHCAST=61, IN=62, IPARAM=63, IPARAMS=64, ISNOTNULL=65, 
		ISNULL=66, IVAL=67, JOIN=68, LE=69, LEFTJOIN=70, LIKE=71, LIKEWITHCAST=72, 
		LOWERCASE=73, LT=74, MAXOF=75, MINOF=76, MINUTEOF=77, MINUTES=78, MOD=79, 
		MODEL=80, MODELASAGGREGATE=81, MODELASENTITY=82, MODELASPRIMITIVE=83, 
		MONTHOF=84, MONTHS=85, MULT=86, NE=87, NEGATEDCONDITION=88, NOTBEGIN=89, 
		NOTEXISTS=90, NOTEXISTSALLOF=91, NOTEXISTSANYOF=92, NOTILIKE=93, NOTILIKEWITHCAST=94, 
		NOTIN=95, NOTLIKE=96, NOTLIKEWITHCAST=97, NOW=98, ON=99, OR=100, ORDER=101, 
		ORDERBY=102, OTHERWISE=103, PARAM=104, PARAMS=105, PROP=106, PROPS=107, 
		ROUND=108, SECONDOF=109, SECONDS=110, SELECT=111, SUB=112, SUMOF=113, 
		SUMOFDISTINCT=114, THEN=115, TO=116, UPPERCASE=117, VAL=118, VALUES=119, 
		WHEN=120, WHERE=121, WITH=122, YEAROF=123, YEARS=124, YIELD=125, YIELDALL=126, 
		WHITESPACE=127, COMMENT=128, BLOCK_COMMENT=129;
	public static final int
		RULE_start = 0, RULE_query = 1, RULE_select = 2, RULE_selectSource = 3, 
		RULE_selectEnd = 4, RULE_where = 5, RULE_condition = 6, RULE_predicate = 7, 
		RULE_unaryComparisonOperator = 8, RULE_likeOperator = 9, RULE_comparisonOperand = 10, 
		RULE_comparisonOperator = 11, RULE_quantifiedOperand = 12, RULE_exprBody = 13, 
		RULE_arithmeticalOperator = 14, RULE_singleOperand = 15, RULE_unaryFunctionName = 16, 
		RULE_dateDiffIntervalUnit = 17, RULE_dateAddIntervalUnit = 18, RULE_caseWhenEnd = 19, 
		RULE_multiOperand = 20, RULE_membershipOperator = 21, RULE_membershipOperand = 22, 
		RULE_join = 23, RULE_joinOperator = 24, RULE_joinCondition = 25, RULE_groupBy = 26, 
		RULE_anyYield = 27, RULE_aliasedYield = 28, RULE_yieldOperand = 29, RULE_yieldOperandFunctionName = 30, 
		RULE_yieldAlias = 31, RULE_yield1Model = 32, RULE_yieldManyModel = 33, 
		RULE_model = 34, RULE_standaloneCondition = 35, RULE_orderByOperand = 36, 
		RULE_order = 37;
	private static String[] makeRuleNames() {
		return new String[] {
			"start", "query", "select", "selectSource", "selectEnd", "where", "condition", 
			"predicate", "unaryComparisonOperator", "likeOperator", "comparisonOperand", 
			"comparisonOperator", "quantifiedOperand", "exprBody", "arithmeticalOperator", 
			"singleOperand", "unaryFunctionName", "dateDiffIntervalUnit", "dateAddIntervalUnit", 
			"caseWhenEnd", "multiOperand", "membershipOperator", "membershipOperand", 
			"join", "joinOperator", "joinCondition", "groupBy", "anyYield", "aliasedYield", 
			"yieldOperand", "yieldOperandFunctionName", "yieldAlias", "yield1Model", 
			"yieldManyModel", "model", "standaloneCondition", "orderByOperand", "order"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'absOf'", "'add'", "'addTimeIntervalOf'", "'all'", "'allOfExpressions'", 
			"'allOfIParams'", "'allOfModels'", "'allOfParams'", "'allOfProps'", "'allOfValues'", 
			"'and'", "'any'", "'anyOfExpressions'", "'anyOfIParams'", "'anyOfModels'", 
			"'anyOfParams'", "'anyOfProps'", "'anyOfValues'", "'as'", "'asc'", "'asRequired'", 
			"'avgOf'", "'avgOfDistinct'", "'begin'", "'beginExpr'", "'between'", 
			"'caseWhen'", "'concat'", "'cond'", "'condition'", "'count'", "'countAll'", 
			"'countOf'", "'countOfDistinct'", "'critCondition'", "'dateOf'", "'dayOf'", 
			"'dayOfWeekOf'", "'days'", "'desc'", "'div'", "'end'", "'endAsBool'", 
			"'endAsDecimal'", "'endAsInt'", "'endAsStr'", "'endExpr'", "'eq'", "'exists'", 
			"'existsAllOf'", "'existsAnyOf'", "'expr'", "'extProp'", "'ge'", "'groupBy'", 
			"'gt'", "'hourOf'", "'hours'", "'ifNull'", "'iLike'", "'iLikeWithCast'", 
			"'in'", "'iParam'", "'iParams'", "'isNotNull'", "'isNull'", "'iVal'", 
			"'join'", "'le'", "'leftJoin'", "'like'", "'likeWithCast'", "'lowerCase'", 
			"'lt'", "'maxOf'", "'minOf'", "'minuteOf'", "'minutes'", "'mod'", "'model'", 
			"'modelAsAggregate'", "'modelAsEntity'", "'modelAsPrimitive'", "'monthOf'", 
			"'months'", "'mult'", "'ne'", "'negatedCondition'", "'notBegin'", "'notExists'", 
			"'notExistsAllOf'", "'notExistsAnyOf'", "'notILike'", "'notILikeWithCast'", 
			"'notIn'", "'notLike'", "'notLikeWithCast'", "'now'", "'on'", "'or'", 
			"'order'", "'orderBy'", "'otherwise'", "'param'", "'params'", "'prop'", 
			"'props'", "'round'", "'secondOf'", "'seconds'", "'select'", "'sub'", 
			"'sumOf'", "'sumOfDistinct'", "'then'", "'to'", "'upperCase'", "'val'", 
			"'values'", "'when'", "'where'", "'with'", "'yearOf'", "'years'", "'yield'", 
			"'yieldAll'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "ABSOF", "ADD", "ADDTIMEINTERVALOF", "ALL", "ALLOFEXPRESSIONS", 
			"ALLOFIPARAMS", "ALLOFMODELS", "ALLOFPARAMS", "ALLOFPROPS", "ALLOFVALUES", 
			"AND", "ANY", "ANYOFEXPRESSIONS", "ANYOFIPARAMS", "ANYOFMODELS", "ANYOFPARAMS", 
			"ANYOFPROPS", "ANYOFVALUES", "AS", "ASC", "ASREQUIRED", "AVGOF", "AVGOFDISTINCT", 
			"BEGIN", "BEGINEXPR", "BETWEEN", "CASEWHEN", "CONCAT", "COND", "CONDITION", 
			"COUNT", "COUNTALL", "COUNTOF", "COUNTOFDISTINCT", "CRITCONDITION", "DATEOF", 
			"DAYOF", "DAYOFWEEKOF", "DAYS", "DESC", "DIV", "END", "ENDASBOOL", "ENDASDECIMAL", 
			"ENDASINT", "ENDASSTR", "ENDEXPR", "EQ", "EXISTS", "EXISTSALLOF", "EXISTSANYOF", 
			"EXPR", "EXTPROP", "GE", "GROUPBY", "GT", "HOUROF", "HOURS", "IFNULL", 
			"ILIKE", "ILIKEWITHCAST", "IN", "IPARAM", "IPARAMS", "ISNOTNULL", "ISNULL", 
			"IVAL", "JOIN", "LE", "LEFTJOIN", "LIKE", "LIKEWITHCAST", "LOWERCASE", 
			"LT", "MAXOF", "MINOF", "MINUTEOF", "MINUTES", "MOD", "MODEL", "MODELASAGGREGATE", 
			"MODELASENTITY", "MODELASPRIMITIVE", "MONTHOF", "MONTHS", "MULT", "NE", 
			"NEGATEDCONDITION", "NOTBEGIN", "NOTEXISTS", "NOTEXISTSALLOF", "NOTEXISTSANYOF", 
			"NOTILIKE", "NOTILIKEWITHCAST", "NOTIN", "NOTLIKE", "NOTLIKEWITHCAST", 
			"NOW", "ON", "OR", "ORDER", "ORDERBY", "OTHERWISE", "PARAM", "PARAMS", 
			"PROP", "PROPS", "ROUND", "SECONDOF", "SECONDS", "SELECT", "SUB", "SUMOF", 
			"SUMOFDISTINCT", "THEN", "TO", "UPPERCASE", "VAL", "VALUES", "WHEN", 
			"WHERE", "WITH", "YEAROF", "YEARS", "YIELD", "YIELDALL", "WHITESPACE", 
			"COMMENT", "BLOCK_COMMENT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "EQL.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public EQLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class StartContext extends ParserRuleContext {
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode EOF() { return getToken(EQLParser.EOF, 0); }
		public StartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_start; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitStart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StartContext start() throws RecognitionException {
		StartContext _localctx = new StartContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_start);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(76);
			query();
			setState(77);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QueryContext extends ParserRuleContext {
		public QueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_query; }
	 
		public QueryContext() { }
		public void copyFrom(QueryContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class OrderByContext extends QueryContext {
		public TerminalNode ORDERBY() { return getToken(EQLParser.ORDERBY, 0); }
		public TerminalNode MODEL() { return getToken(EQLParser.MODEL, 0); }
		public List<OrderByOperandContext> orderByOperand() {
			return getRuleContexts(OrderByOperandContext.class);
		}
		public OrderByOperandContext orderByOperand(int i) {
			return getRuleContext(OrderByOperandContext.class,i);
		}
		public OrderByContext(QueryContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitOrderBy(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class StandaloneExpressionContext extends QueryContext {
		public YieldOperandContext operand;
		public TerminalNode EXPR() { return getToken(EQLParser.EXPR, 0); }
		public TerminalNode MODEL() { return getToken(EQLParser.MODEL, 0); }
		public List<YieldOperandContext> yieldOperand() {
			return getRuleContexts(YieldOperandContext.class);
		}
		public YieldOperandContext yieldOperand(int i) {
			return getRuleContext(YieldOperandContext.class,i);
		}
		public List<ArithmeticalOperatorContext> arithmeticalOperator() {
			return getRuleContexts(ArithmeticalOperatorContext.class);
		}
		public ArithmeticalOperatorContext arithmeticalOperator(int i) {
			return getRuleContext(ArithmeticalOperatorContext.class,i);
		}
		public StandaloneExpressionContext(QueryContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitStandaloneExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class StandaloneCondExprContext extends QueryContext {
		public TerminalNode COND() { return getToken(EQLParser.COND, 0); }
		public StandaloneConditionContext standaloneCondition() {
			return getRuleContext(StandaloneConditionContext.class,0);
		}
		public TerminalNode MODEL() { return getToken(EQLParser.MODEL, 0); }
		public StandaloneCondExprContext(QueryContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitStandaloneCondExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Query_SelectContext extends QueryContext {
		public SelectContext select() {
			return getRuleContext(SelectContext.class,0);
		}
		public Query_SelectContext(QueryContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitQuery_Select(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QueryContext query() throws RecognitionException {
		QueryContext _localctx = new QueryContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_query);
		int _la;
		try {
			int _alt;
			setState(104);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SELECT:
				_localctx = new Query_SelectContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(79);
				select();
				}
				break;
			case EXPR:
				_localctx = new StandaloneExpressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(80);
				match(EXPR);
				setState(81);
				((StandaloneExpressionContext)_localctx).operand = yieldOperand();
				setState(87);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ADD || _la==DIV || ((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & ((1L << (MOD - 79)) | (1L << (MULT - 79)) | (1L << (SUB - 79)))) != 0)) {
					{
					{
					setState(82);
					arithmeticalOperator();
					setState(83);
					yieldOperand();
					}
					}
					setState(89);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(90);
				match(MODEL);
				}
				break;
			case COND:
				_localctx = new StandaloneCondExprContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(92);
				match(COND);
				setState(93);
				standaloneCondition(0);
				setState(94);
				match(MODEL);
				}
				break;
			case ORDERBY:
				_localctx = new OrderByContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(96);
				match(ORDERBY);
				setState(98); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(97);
						orderByOperand();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(100); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(102);
				match(MODEL);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SelectContext extends ParserRuleContext {
		public SelectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select; }
	 
		public SelectContext() { }
		public void copyFrom(SelectContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class SelectFromContext extends SelectContext {
		public Token alias;
		public SelectSourceContext selectSource() {
			return getRuleContext(SelectSourceContext.class,0);
		}
		public SelectEndContext selectEnd() {
			return getRuleContext(SelectEndContext.class,0);
		}
		public JoinContext join() {
			return getRuleContext(JoinContext.class,0);
		}
		public WhereContext where() {
			return getRuleContext(WhereContext.class,0);
		}
		public GroupByContext groupBy() {
			return getRuleContext(GroupByContext.class,0);
		}
		public TerminalNode AS() { return getToken(EQLParser.AS, 0); }
		public SelectFromContext(SelectContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitSelectFrom(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SourcelessSelectContext extends SelectContext {
		public TerminalNode SELECT() { return getToken(EQLParser.SELECT, 0); }
		public SelectEndContext selectEnd() {
			return getRuleContext(SelectEndContext.class,0);
		}
		public GroupByContext groupBy() {
			return getRuleContext(GroupByContext.class,0);
		}
		public SourcelessSelectContext(SelectContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitSourcelessSelect(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectContext select() throws RecognitionException {
		SelectContext _localctx = new SelectContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_select);
		int _la;
		try {
			setState(126);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				_localctx = new SelectFromContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(106);
				selectSource();
				setState(108);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(107);
					((SelectFromContext)_localctx).alias = match(AS);
					}
				}

				setState(111);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==JOIN || _la==LEFTJOIN) {
					{
					setState(110);
					join();
					}
				}

				setState(114);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WHERE) {
					{
					setState(113);
					where();
					}
				}

				setState(117);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==GROUPBY) {
					{
					setState(116);
					groupBy();
					}
				}

				setState(119);
				selectEnd();
				}
				break;
			case 2:
				_localctx = new SourcelessSelectContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(121);
				match(SELECT);
				setState(123);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==GROUPBY) {
					{
					setState(122);
					groupBy();
					}
				}

				setState(125);
				selectEnd();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SelectSourceContext extends ParserRuleContext {
		public Token token;
		public TerminalNode SELECT() { return getToken(EQLParser.SELECT, 0); }
		public SelectSourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectSource; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitSelectSource(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectSourceContext selectSource() throws RecognitionException {
		SelectSourceContext _localctx = new SelectSourceContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_selectSource);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(128);
			((SelectSourceContext)_localctx).token = match(SELECT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SelectEndContext extends ParserRuleContext {
		public SelectEndContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectEnd; }
	 
		public SelectEndContext() { }
		public void copyFrom(SelectEndContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class SelectEnd_ModelContext extends SelectEndContext {
		public ModelContext model() {
			return getRuleContext(ModelContext.class,0);
		}
		public SelectEnd_ModelContext(SelectEndContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitSelectEnd_Model(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SelectEnd_AnyYieldContext extends SelectEndContext {
		public AnyYieldContext anyYield() {
			return getRuleContext(AnyYieldContext.class,0);
		}
		public SelectEnd_AnyYieldContext(SelectEndContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitSelectEnd_AnyYield(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectEndContext selectEnd() throws RecognitionException {
		SelectEndContext _localctx = new SelectEndContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_selectEnd);
		try {
			setState(132);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case YIELD:
			case YIELDALL:
				_localctx = new SelectEnd_AnyYieldContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(130);
				anyYield();
				}
				break;
			case MODEL:
			case MODELASAGGREGATE:
			case MODELASENTITY:
				_localctx = new SelectEnd_ModelContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(131);
				model();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WhereContext extends ParserRuleContext {
		public TerminalNode WHERE() { return getToken(EQLParser.WHERE, 0); }
		public ConditionContext condition() {
			return getRuleContext(ConditionContext.class,0);
		}
		public WhereContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_where; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitWhere(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhereContext where() throws RecognitionException {
		WhereContext _localctx = new WhereContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_where);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(134);
			match(WHERE);
			setState(135);
			condition(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConditionContext extends ParserRuleContext {
		public ConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_condition; }
	 
		public ConditionContext() { }
		public void copyFrom(ConditionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class PredicateConditionContext extends ConditionContext {
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public PredicateConditionContext(ConditionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitPredicateCondition(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CompoundConditionContext extends ConditionContext {
		public TerminalNode BEGIN() { return getToken(EQLParser.BEGIN, 0); }
		public ConditionContext condition() {
			return getRuleContext(ConditionContext.class,0);
		}
		public TerminalNode END() { return getToken(EQLParser.END, 0); }
		public CompoundConditionContext(ConditionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitCompoundCondition(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class OrConditionContext extends ConditionContext {
		public ConditionContext left;
		public ConditionContext right;
		public TerminalNode OR() { return getToken(EQLParser.OR, 0); }
		public List<ConditionContext> condition() {
			return getRuleContexts(ConditionContext.class);
		}
		public ConditionContext condition(int i) {
			return getRuleContext(ConditionContext.class,i);
		}
		public OrConditionContext(ConditionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitOrCondition(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NegatedCompoundConditionContext extends ConditionContext {
		public TerminalNode NOTBEGIN() { return getToken(EQLParser.NOTBEGIN, 0); }
		public ConditionContext condition() {
			return getRuleContext(ConditionContext.class,0);
		}
		public TerminalNode END() { return getToken(EQLParser.END, 0); }
		public NegatedCompoundConditionContext(ConditionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitNegatedCompoundCondition(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AndConditionContext extends ConditionContext {
		public ConditionContext left;
		public ConditionContext right;
		public TerminalNode AND() { return getToken(EQLParser.AND, 0); }
		public List<ConditionContext> condition() {
			return getRuleContexts(ConditionContext.class);
		}
		public ConditionContext condition(int i) {
			return getRuleContext(ConditionContext.class,i);
		}
		public AndConditionContext(ConditionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitAndCondition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConditionContext condition() throws RecognitionException {
		return condition(0);
	}

	private ConditionContext condition(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ConditionContext _localctx = new ConditionContext(_ctx, _parentState);
		ConditionContext _prevctx = _localctx;
		int _startState = 12;
		enterRecursionRule(_localctx, 12, RULE_condition, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(147);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ABSOF:
			case ADDTIMEINTERVALOF:
			case ALLOFEXPRESSIONS:
			case ALLOFIPARAMS:
			case ALLOFMODELS:
			case ALLOFPARAMS:
			case ALLOFPROPS:
			case ALLOFVALUES:
			case ANYOFEXPRESSIONS:
			case ANYOFIPARAMS:
			case ANYOFMODELS:
			case ANYOFPARAMS:
			case ANYOFPROPS:
			case ANYOFVALUES:
			case BEGINEXPR:
			case CASEWHEN:
			case CONCAT:
			case CONDITION:
			case COUNT:
			case CRITCONDITION:
			case DATEOF:
			case DAYOF:
			case DAYOFWEEKOF:
			case EXISTS:
			case EXISTSALLOF:
			case EXISTSANYOF:
			case EXPR:
			case EXTPROP:
			case HOUROF:
			case IFNULL:
			case IPARAM:
			case IVAL:
			case LOWERCASE:
			case MINUTEOF:
			case MODEL:
			case MONTHOF:
			case NEGATEDCONDITION:
			case NOTEXISTS:
			case NOTEXISTSALLOF:
			case NOTEXISTSANYOF:
			case NOW:
			case PARAM:
			case PROP:
			case ROUND:
			case SECONDOF:
			case UPPERCASE:
			case VAL:
			case YEAROF:
				{
				_localctx = new PredicateConditionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(138);
				predicate();
				}
				break;
			case BEGIN:
				{
				_localctx = new CompoundConditionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(139);
				match(BEGIN);
				setState(140);
				condition(0);
				setState(141);
				match(END);
				}
				break;
			case NOTBEGIN:
				{
				_localctx = new NegatedCompoundConditionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(143);
				match(NOTBEGIN);
				setState(144);
				condition(0);
				setState(145);
				match(END);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(157);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(155);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
					case 1:
						{
						_localctx = new AndConditionContext(new ConditionContext(_parentctx, _parentState));
						((AndConditionContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_condition);
						setState(149);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(150);
						match(AND);
						setState(151);
						((AndConditionContext)_localctx).right = condition(5);
						}
						break;
					case 2:
						{
						_localctx = new OrConditionContext(new ConditionContext(_parentctx, _parentState));
						((OrConditionContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_condition);
						setState(152);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(153);
						match(OR);
						setState(154);
						((OrConditionContext)_localctx).right = condition(4);
						}
						break;
					}
					} 
				}
				setState(159);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class PredicateContext extends ParserRuleContext {
		public PredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate; }
	 
		public PredicateContext() { }
		public void copyFrom(PredicateContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class SingleConditionPredicateContext extends PredicateContext {
		public Token token;
		public TerminalNode EXISTS() { return getToken(EQLParser.EXISTS, 0); }
		public TerminalNode NOTEXISTS() { return getToken(EQLParser.NOTEXISTS, 0); }
		public TerminalNode EXISTSANYOF() { return getToken(EQLParser.EXISTSANYOF, 0); }
		public TerminalNode NOTEXISTSANYOF() { return getToken(EQLParser.NOTEXISTSANYOF, 0); }
		public TerminalNode EXISTSALLOF() { return getToken(EQLParser.EXISTSALLOF, 0); }
		public TerminalNode NOTEXISTSALLOF() { return getToken(EQLParser.NOTEXISTSALLOF, 0); }
		public TerminalNode CRITCONDITION() { return getToken(EQLParser.CRITCONDITION, 0); }
		public TerminalNode CONDITION() { return getToken(EQLParser.CONDITION, 0); }
		public TerminalNode NEGATEDCONDITION() { return getToken(EQLParser.NEGATEDCONDITION, 0); }
		public SingleConditionPredicateContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitSingleConditionPredicate(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class QuantifiedComparisonPredicateContext extends PredicateContext {
		public ComparisonOperandContext left;
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public QuantifiedOperandContext quantifiedOperand() {
			return getRuleContext(QuantifiedOperandContext.class,0);
		}
		public ComparisonOperandContext comparisonOperand() {
			return getRuleContext(ComparisonOperandContext.class,0);
		}
		public QuantifiedComparisonPredicateContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitQuantifiedComparisonPredicate(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class MembershipPredicateContext extends PredicateContext {
		public ComparisonOperandContext left;
		public MembershipOperatorContext membershipOperator() {
			return getRuleContext(MembershipOperatorContext.class,0);
		}
		public MembershipOperandContext membershipOperand() {
			return getRuleContext(MembershipOperandContext.class,0);
		}
		public ComparisonOperandContext comparisonOperand() {
			return getRuleContext(ComparisonOperandContext.class,0);
		}
		public MembershipPredicateContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitMembershipPredicate(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LikePredicateContext extends PredicateContext {
		public ComparisonOperandContext left;
		public ComparisonOperandContext right;
		public LikeOperatorContext likeOperator() {
			return getRuleContext(LikeOperatorContext.class,0);
		}
		public List<ComparisonOperandContext> comparisonOperand() {
			return getRuleContexts(ComparisonOperandContext.class);
		}
		public ComparisonOperandContext comparisonOperand(int i) {
			return getRuleContext(ComparisonOperandContext.class,i);
		}
		public LikePredicateContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitLikePredicate(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ComparisonPredicateContext extends PredicateContext {
		public ComparisonOperandContext left;
		public ComparisonOperandContext right;
		public ComparisonOperatorContext comparisonOperator() {
			return getRuleContext(ComparisonOperatorContext.class,0);
		}
		public List<ComparisonOperandContext> comparisonOperand() {
			return getRuleContexts(ComparisonOperandContext.class);
		}
		public ComparisonOperandContext comparisonOperand(int i) {
			return getRuleContext(ComparisonOperandContext.class,i);
		}
		public ComparisonPredicateContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitComparisonPredicate(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UnaryPredicateContext extends PredicateContext {
		public ComparisonOperandContext left;
		public UnaryComparisonOperatorContext unaryComparisonOperator() {
			return getRuleContext(UnaryComparisonOperatorContext.class,0);
		}
		public ComparisonOperandContext comparisonOperand() {
			return getRuleContext(ComparisonOperandContext.class,0);
		}
		public UnaryPredicateContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitUnaryPredicate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateContext predicate() throws RecognitionException {
		PredicateContext _localctx = new PredicateContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_predicate);
		try {
			setState(190);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				_localctx = new UnaryPredicateContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(160);
				((UnaryPredicateContext)_localctx).left = comparisonOperand();
				setState(161);
				unaryComparisonOperator();
				}
				break;
			case 2:
				_localctx = new ComparisonPredicateContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(163);
				((ComparisonPredicateContext)_localctx).left = comparisonOperand();
				setState(164);
				comparisonOperator();
				setState(165);
				((ComparisonPredicateContext)_localctx).right = comparisonOperand();
				}
				break;
			case 3:
				_localctx = new QuantifiedComparisonPredicateContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(167);
				((QuantifiedComparisonPredicateContext)_localctx).left = comparisonOperand();
				setState(168);
				comparisonOperator();
				setState(169);
				quantifiedOperand();
				}
				break;
			case 4:
				_localctx = new LikePredicateContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(171);
				((LikePredicateContext)_localctx).left = comparisonOperand();
				setState(172);
				likeOperator();
				setState(173);
				((LikePredicateContext)_localctx).right = comparisonOperand();
				}
				break;
			case 5:
				_localctx = new MembershipPredicateContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(175);
				((MembershipPredicateContext)_localctx).left = comparisonOperand();
				setState(176);
				membershipOperator();
				setState(177);
				membershipOperand();
				}
				break;
			case 6:
				_localctx = new SingleConditionPredicateContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(188);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case EXISTS:
					{
					setState(179);
					((SingleConditionPredicateContext)_localctx).token = match(EXISTS);
					}
					break;
				case NOTEXISTS:
					{
					setState(180);
					((SingleConditionPredicateContext)_localctx).token = match(NOTEXISTS);
					}
					break;
				case EXISTSANYOF:
					{
					setState(181);
					((SingleConditionPredicateContext)_localctx).token = match(EXISTSANYOF);
					}
					break;
				case NOTEXISTSANYOF:
					{
					setState(182);
					((SingleConditionPredicateContext)_localctx).token = match(NOTEXISTSANYOF);
					}
					break;
				case EXISTSALLOF:
					{
					setState(183);
					((SingleConditionPredicateContext)_localctx).token = match(EXISTSALLOF);
					}
					break;
				case NOTEXISTSALLOF:
					{
					setState(184);
					((SingleConditionPredicateContext)_localctx).token = match(NOTEXISTSALLOF);
					}
					break;
				case CRITCONDITION:
					{
					setState(185);
					((SingleConditionPredicateContext)_localctx).token = match(CRITCONDITION);
					}
					break;
				case CONDITION:
					{
					setState(186);
					((SingleConditionPredicateContext)_localctx).token = match(CONDITION);
					}
					break;
				case NEGATEDCONDITION:
					{
					setState(187);
					((SingleConditionPredicateContext)_localctx).token = match(NEGATEDCONDITION);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UnaryComparisonOperatorContext extends ParserRuleContext {
		public Token token;
		public TerminalNode ISNULL() { return getToken(EQLParser.ISNULL, 0); }
		public TerminalNode ISNOTNULL() { return getToken(EQLParser.ISNOTNULL, 0); }
		public UnaryComparisonOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unaryComparisonOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitUnaryComparisonOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnaryComparisonOperatorContext unaryComparisonOperator() throws RecognitionException {
		UnaryComparisonOperatorContext _localctx = new UnaryComparisonOperatorContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_unaryComparisonOperator);
		try {
			setState(194);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ISNULL:
				enterOuterAlt(_localctx, 1);
				{
				setState(192);
				((UnaryComparisonOperatorContext)_localctx).token = match(ISNULL);
				}
				break;
			case ISNOTNULL:
				enterOuterAlt(_localctx, 2);
				{
				setState(193);
				((UnaryComparisonOperatorContext)_localctx).token = match(ISNOTNULL);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LikeOperatorContext extends ParserRuleContext {
		public Token token;
		public TerminalNode LIKE() { return getToken(EQLParser.LIKE, 0); }
		public TerminalNode ILIKE() { return getToken(EQLParser.ILIKE, 0); }
		public TerminalNode LIKEWITHCAST() { return getToken(EQLParser.LIKEWITHCAST, 0); }
		public TerminalNode ILIKEWITHCAST() { return getToken(EQLParser.ILIKEWITHCAST, 0); }
		public TerminalNode NOTLIKE() { return getToken(EQLParser.NOTLIKE, 0); }
		public TerminalNode NOTLIKEWITHCAST() { return getToken(EQLParser.NOTLIKEWITHCAST, 0); }
		public TerminalNode NOTILIKEWITHCAST() { return getToken(EQLParser.NOTILIKEWITHCAST, 0); }
		public TerminalNode NOTILIKE() { return getToken(EQLParser.NOTILIKE, 0); }
		public LikeOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_likeOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitLikeOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LikeOperatorContext likeOperator() throws RecognitionException {
		LikeOperatorContext _localctx = new LikeOperatorContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_likeOperator);
		try {
			setState(204);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LIKE:
				enterOuterAlt(_localctx, 1);
				{
				setState(196);
				((LikeOperatorContext)_localctx).token = match(LIKE);
				}
				break;
			case ILIKE:
				enterOuterAlt(_localctx, 2);
				{
				setState(197);
				((LikeOperatorContext)_localctx).token = match(ILIKE);
				}
				break;
			case LIKEWITHCAST:
				enterOuterAlt(_localctx, 3);
				{
				setState(198);
				((LikeOperatorContext)_localctx).token = match(LIKEWITHCAST);
				}
				break;
			case ILIKEWITHCAST:
				enterOuterAlt(_localctx, 4);
				{
				setState(199);
				((LikeOperatorContext)_localctx).token = match(ILIKEWITHCAST);
				}
				break;
			case NOTLIKE:
				enterOuterAlt(_localctx, 5);
				{
				setState(200);
				((LikeOperatorContext)_localctx).token = match(NOTLIKE);
				}
				break;
			case NOTLIKEWITHCAST:
				enterOuterAlt(_localctx, 6);
				{
				setState(201);
				((LikeOperatorContext)_localctx).token = match(NOTLIKEWITHCAST);
				}
				break;
			case NOTILIKEWITHCAST:
				enterOuterAlt(_localctx, 7);
				{
				setState(202);
				((LikeOperatorContext)_localctx).token = match(NOTILIKEWITHCAST);
				}
				break;
			case NOTILIKE:
				enterOuterAlt(_localctx, 8);
				{
				setState(203);
				((LikeOperatorContext)_localctx).token = match(NOTILIKE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ComparisonOperandContext extends ParserRuleContext {
		public ComparisonOperandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparisonOperand; }
	 
		public ComparisonOperandContext() { }
		public void copyFrom(ComparisonOperandContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ComparisonOperand_SingleContext extends ComparisonOperandContext {
		public SingleOperandContext singleOperand() {
			return getRuleContext(SingleOperandContext.class,0);
		}
		public ComparisonOperand_SingleContext(ComparisonOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitComparisonOperand_Single(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ComparisonOperand_MultiContext extends ComparisonOperandContext {
		public MultiOperandContext multiOperand() {
			return getRuleContext(MultiOperandContext.class,0);
		}
		public ComparisonOperand_MultiContext(ComparisonOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitComparisonOperand_Multi(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparisonOperandContext comparisonOperand() throws RecognitionException {
		ComparisonOperandContext _localctx = new ComparisonOperandContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_comparisonOperand);
		try {
			setState(208);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ABSOF:
			case ADDTIMEINTERVALOF:
			case BEGINEXPR:
			case CASEWHEN:
			case CONCAT:
			case COUNT:
			case DATEOF:
			case DAYOF:
			case DAYOFWEEKOF:
			case EXPR:
			case EXTPROP:
			case HOUROF:
			case IFNULL:
			case IPARAM:
			case IVAL:
			case LOWERCASE:
			case MINUTEOF:
			case MODEL:
			case MONTHOF:
			case NOW:
			case PARAM:
			case PROP:
			case ROUND:
			case SECONDOF:
			case UPPERCASE:
			case VAL:
			case YEAROF:
				_localctx = new ComparisonOperand_SingleContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(206);
				singleOperand();
				}
				break;
			case ALLOFEXPRESSIONS:
			case ALLOFIPARAMS:
			case ALLOFMODELS:
			case ALLOFPARAMS:
			case ALLOFPROPS:
			case ALLOFVALUES:
			case ANYOFEXPRESSIONS:
			case ANYOFIPARAMS:
			case ANYOFMODELS:
			case ANYOFPARAMS:
			case ANYOFPROPS:
			case ANYOFVALUES:
				_localctx = new ComparisonOperand_MultiContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(207);
				multiOperand();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ComparisonOperatorContext extends ParserRuleContext {
		public Token token;
		public TerminalNode EQ() { return getToken(EQLParser.EQ, 0); }
		public TerminalNode GT() { return getToken(EQLParser.GT, 0); }
		public TerminalNode LT() { return getToken(EQLParser.LT, 0); }
		public TerminalNode GE() { return getToken(EQLParser.GE, 0); }
		public TerminalNode LE() { return getToken(EQLParser.LE, 0); }
		public TerminalNode NE() { return getToken(EQLParser.NE, 0); }
		public ComparisonOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparisonOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitComparisonOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparisonOperatorContext comparisonOperator() throws RecognitionException {
		ComparisonOperatorContext _localctx = new ComparisonOperatorContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_comparisonOperator);
		try {
			setState(216);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EQ:
				enterOuterAlt(_localctx, 1);
				{
				setState(210);
				((ComparisonOperatorContext)_localctx).token = match(EQ);
				}
				break;
			case GT:
				enterOuterAlt(_localctx, 2);
				{
				setState(211);
				((ComparisonOperatorContext)_localctx).token = match(GT);
				}
				break;
			case LT:
				enterOuterAlt(_localctx, 3);
				{
				setState(212);
				((ComparisonOperatorContext)_localctx).token = match(LT);
				}
				break;
			case GE:
				enterOuterAlt(_localctx, 4);
				{
				setState(213);
				((ComparisonOperatorContext)_localctx).token = match(GE);
				}
				break;
			case LE:
				enterOuterAlt(_localctx, 5);
				{
				setState(214);
				((ComparisonOperatorContext)_localctx).token = match(LE);
				}
				break;
			case NE:
				enterOuterAlt(_localctx, 6);
				{
				setState(215);
				((ComparisonOperatorContext)_localctx).token = match(NE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QuantifiedOperandContext extends ParserRuleContext {
		public Token token;
		public TerminalNode ALL() { return getToken(EQLParser.ALL, 0); }
		public TerminalNode ANY() { return getToken(EQLParser.ANY, 0); }
		public QuantifiedOperandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_quantifiedOperand; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitQuantifiedOperand(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QuantifiedOperandContext quantifiedOperand() throws RecognitionException {
		QuantifiedOperandContext _localctx = new QuantifiedOperandContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_quantifiedOperand);
		try {
			setState(220);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ALL:
				enterOuterAlt(_localctx, 1);
				{
				setState(218);
				((QuantifiedOperandContext)_localctx).token = match(ALL);
				}
				break;
			case ANY:
				enterOuterAlt(_localctx, 2);
				{
				setState(219);
				((QuantifiedOperandContext)_localctx).token = match(ANY);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExprBodyContext extends ParserRuleContext {
		public List<SingleOperandContext> singleOperand() {
			return getRuleContexts(SingleOperandContext.class);
		}
		public SingleOperandContext singleOperand(int i) {
			return getRuleContext(SingleOperandContext.class,i);
		}
		public List<ArithmeticalOperatorContext> arithmeticalOperator() {
			return getRuleContexts(ArithmeticalOperatorContext.class);
		}
		public ArithmeticalOperatorContext arithmeticalOperator(int i) {
			return getRuleContext(ArithmeticalOperatorContext.class,i);
		}
		public ExprBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exprBody; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitExprBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprBodyContext exprBody() throws RecognitionException {
		ExprBodyContext _localctx = new ExprBodyContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_exprBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(222);
			singleOperand();
			setState(228);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ADD || _la==DIV || ((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & ((1L << (MOD - 79)) | (1L << (MULT - 79)) | (1L << (SUB - 79)))) != 0)) {
				{
				{
				setState(223);
				arithmeticalOperator();
				setState(224);
				singleOperand();
				}
				}
				setState(230);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArithmeticalOperatorContext extends ParserRuleContext {
		public Token token;
		public TerminalNode ADD() { return getToken(EQLParser.ADD, 0); }
		public TerminalNode SUB() { return getToken(EQLParser.SUB, 0); }
		public TerminalNode DIV() { return getToken(EQLParser.DIV, 0); }
		public TerminalNode MULT() { return getToken(EQLParser.MULT, 0); }
		public TerminalNode MOD() { return getToken(EQLParser.MOD, 0); }
		public ArithmeticalOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arithmeticalOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitArithmeticalOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArithmeticalOperatorContext arithmeticalOperator() throws RecognitionException {
		ArithmeticalOperatorContext _localctx = new ArithmeticalOperatorContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_arithmeticalOperator);
		try {
			setState(236);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ADD:
				enterOuterAlt(_localctx, 1);
				{
				setState(231);
				((ArithmeticalOperatorContext)_localctx).token = match(ADD);
				}
				break;
			case SUB:
				enterOuterAlt(_localctx, 2);
				{
				setState(232);
				((ArithmeticalOperatorContext)_localctx).token = match(SUB);
				}
				break;
			case DIV:
				enterOuterAlt(_localctx, 3);
				{
				setState(233);
				((ArithmeticalOperatorContext)_localctx).token = match(DIV);
				}
				break;
			case MULT:
				enterOuterAlt(_localctx, 4);
				{
				setState(234);
				((ArithmeticalOperatorContext)_localctx).token = match(MULT);
				}
				break;
			case MOD:
				enterOuterAlt(_localctx, 5);
				{
				setState(235);
				((ArithmeticalOperatorContext)_localctx).token = match(MOD);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SingleOperandContext extends ParserRuleContext {
		public SingleOperandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleOperand; }
	 
		public SingleOperandContext() { }
		public void copyFrom(SingleOperandContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ValContext extends SingleOperandContext {
		public Token token;
		public TerminalNode VAL() { return getToken(EQLParser.VAL, 0); }
		public TerminalNode IVAL() { return getToken(EQLParser.IVAL, 0); }
		public ValContext(SingleOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitVal(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PropContext extends SingleOperandContext {
		public Token token;
		public TerminalNode PROP() { return getToken(EQLParser.PROP, 0); }
		public PropContext(SingleOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitProp(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DateAddIntervalContext extends SingleOperandContext {
		public SingleOperandContext left;
		public DateAddIntervalUnitContext unit;
		public SingleOperandContext right;
		public TerminalNode ADDTIMEINTERVALOF() { return getToken(EQLParser.ADDTIMEINTERVALOF, 0); }
		public TerminalNode TO() { return getToken(EQLParser.TO, 0); }
		public List<SingleOperandContext> singleOperand() {
			return getRuleContexts(SingleOperandContext.class);
		}
		public SingleOperandContext singleOperand(int i) {
			return getRuleContext(SingleOperandContext.class,i);
		}
		public DateAddIntervalUnitContext dateAddIntervalUnit() {
			return getRuleContext(DateAddIntervalUnitContext.class,0);
		}
		public DateAddIntervalContext(SingleOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitDateAddInterval(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DateDiffIntervalContext extends SingleOperandContext {
		public DateDiffIntervalUnitContext unit;
		public SingleOperandContext startDate;
		public SingleOperandContext endDate;
		public TerminalNode COUNT() { return getToken(EQLParser.COUNT, 0); }
		public TerminalNode BETWEEN() { return getToken(EQLParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(EQLParser.AND, 0); }
		public DateDiffIntervalUnitContext dateDiffIntervalUnit() {
			return getRuleContext(DateDiffIntervalUnitContext.class,0);
		}
		public List<SingleOperandContext> singleOperand() {
			return getRuleContexts(SingleOperandContext.class);
		}
		public SingleOperandContext singleOperand(int i) {
			return getRuleContext(SingleOperandContext.class,i);
		}
		public DateDiffIntervalContext(SingleOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitDateDiffInterval(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CaseWhenContext extends SingleOperandContext {
		public SingleOperandContext otherwiseOperand;
		public TerminalNode CASEWHEN() { return getToken(EQLParser.CASEWHEN, 0); }
		public List<ConditionContext> condition() {
			return getRuleContexts(ConditionContext.class);
		}
		public ConditionContext condition(int i) {
			return getRuleContext(ConditionContext.class,i);
		}
		public List<TerminalNode> THEN() { return getTokens(EQLParser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(EQLParser.THEN, i);
		}
		public List<SingleOperandContext> singleOperand() {
			return getRuleContexts(SingleOperandContext.class);
		}
		public SingleOperandContext singleOperand(int i) {
			return getRuleContext(SingleOperandContext.class,i);
		}
		public CaseWhenEndContext caseWhenEnd() {
			return getRuleContext(CaseWhenEndContext.class,0);
		}
		public List<TerminalNode> WHEN() { return getTokens(EQLParser.WHEN); }
		public TerminalNode WHEN(int i) {
			return getToken(EQLParser.WHEN, i);
		}
		public TerminalNode OTHERWISE() { return getToken(EQLParser.OTHERWISE, 0); }
		public CaseWhenContext(SingleOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitCaseWhen(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SingleOperand_ExprContext extends SingleOperandContext {
		public Token token;
		public TerminalNode EXPR() { return getToken(EQLParser.EXPR, 0); }
		public SingleOperand_ExprContext(SingleOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitSingleOperand_Expr(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class IfNullContext extends SingleOperandContext {
		public SingleOperandContext nullable;
		public SingleOperandContext other;
		public TerminalNode IFNULL() { return getToken(EQLParser.IFNULL, 0); }
		public TerminalNode THEN() { return getToken(EQLParser.THEN, 0); }
		public List<SingleOperandContext> singleOperand() {
			return getRuleContexts(SingleOperandContext.class);
		}
		public SingleOperandContext singleOperand(int i) {
			return getRuleContext(SingleOperandContext.class,i);
		}
		public IfNullContext(SingleOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitIfNull(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ConcatContext extends SingleOperandContext {
		public TerminalNode CONCAT() { return getToken(EQLParser.CONCAT, 0); }
		public List<SingleOperandContext> singleOperand() {
			return getRuleContexts(SingleOperandContext.class);
		}
		public SingleOperandContext singleOperand(int i) {
			return getRuleContext(SingleOperandContext.class,i);
		}
		public TerminalNode END() { return getToken(EQLParser.END, 0); }
		public List<TerminalNode> WITH() { return getTokens(EQLParser.WITH); }
		public TerminalNode WITH(int i) {
			return getToken(EQLParser.WITH, i);
		}
		public ConcatContext(SingleOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitConcat(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UnaryFunctionContext extends SingleOperandContext {
		public UnaryFunctionNameContext funcName;
		public SingleOperandContext argument;
		public UnaryFunctionNameContext unaryFunctionName() {
			return getRuleContext(UnaryFunctionNameContext.class,0);
		}
		public SingleOperandContext singleOperand() {
			return getRuleContext(SingleOperandContext.class,0);
		}
		public UnaryFunctionContext(SingleOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitUnaryFunction(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExprContext extends SingleOperandContext {
		public TerminalNode BEGINEXPR() { return getToken(EQLParser.BEGINEXPR, 0); }
		public ExprBodyContext exprBody() {
			return getRuleContext(ExprBodyContext.class,0);
		}
		public TerminalNode ENDEXPR() { return getToken(EQLParser.ENDEXPR, 0); }
		public ExprContext(SingleOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExtPropContext extends SingleOperandContext {
		public Token token;
		public TerminalNode EXTPROP() { return getToken(EQLParser.EXTPROP, 0); }
		public ExtPropContext(SingleOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitExtProp(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ParamContext extends SingleOperandContext {
		public Token token;
		public TerminalNode PARAM() { return getToken(EQLParser.PARAM, 0); }
		public TerminalNode IPARAM() { return getToken(EQLParser.IPARAM, 0); }
		public ParamContext(SingleOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitParam(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SingleOperand_NowContext extends SingleOperandContext {
		public TerminalNode NOW() { return getToken(EQLParser.NOW, 0); }
		public SingleOperand_NowContext(SingleOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitSingleOperand_Now(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RoundContext extends SingleOperandContext {
		public TerminalNode ROUND() { return getToken(EQLParser.ROUND, 0); }
		public SingleOperandContext singleOperand() {
			return getRuleContext(SingleOperandContext.class,0);
		}
		public TerminalNode TO() { return getToken(EQLParser.TO, 0); }
		public RoundContext(SingleOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitRound(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SingleOperand_ModelContext extends SingleOperandContext {
		public Token token;
		public TerminalNode MODEL() { return getToken(EQLParser.MODEL, 0); }
		public SingleOperand_ModelContext(SingleOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitSingleOperand_Model(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleOperandContext singleOperand() throws RecognitionException {
		SingleOperandContext _localctx = new SingleOperandContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_singleOperand);
		int _la;
		try {
			setState(311);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PROP:
				_localctx = new PropContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(238);
				((PropContext)_localctx).token = match(PROP);
				}
				break;
			case EXTPROP:
				_localctx = new ExtPropContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(239);
				((ExtPropContext)_localctx).token = match(EXTPROP);
				}
				break;
			case IVAL:
			case VAL:
				_localctx = new ValContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(242);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case VAL:
					{
					setState(240);
					((ValContext)_localctx).token = match(VAL);
					}
					break;
				case IVAL:
					{
					setState(241);
					((ValContext)_localctx).token = match(IVAL);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case IPARAM:
			case PARAM:
				_localctx = new ParamContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(246);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case PARAM:
					{
					setState(244);
					((ParamContext)_localctx).token = match(PARAM);
					}
					break;
				case IPARAM:
					{
					setState(245);
					((ParamContext)_localctx).token = match(IPARAM);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case EXPR:
				_localctx = new SingleOperand_ExprContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(248);
				((SingleOperand_ExprContext)_localctx).token = match(EXPR);
				}
				break;
			case MODEL:
				_localctx = new SingleOperand_ModelContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(249);
				((SingleOperand_ModelContext)_localctx).token = match(MODEL);
				}
				break;
			case ABSOF:
			case DATEOF:
			case DAYOF:
			case DAYOFWEEKOF:
			case HOUROF:
			case LOWERCASE:
			case MINUTEOF:
			case MONTHOF:
			case SECONDOF:
			case UPPERCASE:
			case YEAROF:
				_localctx = new UnaryFunctionContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(250);
				((UnaryFunctionContext)_localctx).funcName = unaryFunctionName();
				setState(251);
				((UnaryFunctionContext)_localctx).argument = singleOperand();
				}
				break;
			case IFNULL:
				_localctx = new IfNullContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(253);
				match(IFNULL);
				setState(254);
				((IfNullContext)_localctx).nullable = singleOperand();
				setState(255);
				match(THEN);
				setState(256);
				((IfNullContext)_localctx).other = singleOperand();
				}
				break;
			case NOW:
				_localctx = new SingleOperand_NowContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(258);
				match(NOW);
				}
				break;
			case COUNT:
				_localctx = new DateDiffIntervalContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(259);
				match(COUNT);
				setState(260);
				((DateDiffIntervalContext)_localctx).unit = dateDiffIntervalUnit();
				setState(261);
				match(BETWEEN);
				setState(262);
				((DateDiffIntervalContext)_localctx).startDate = singleOperand();
				setState(263);
				match(AND);
				setState(264);
				((DateDiffIntervalContext)_localctx).endDate = singleOperand();
				}
				break;
			case ADDTIMEINTERVALOF:
				_localctx = new DateAddIntervalContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(266);
				match(ADDTIMEINTERVALOF);
				setState(267);
				((DateAddIntervalContext)_localctx).left = singleOperand();
				setState(268);
				((DateAddIntervalContext)_localctx).unit = dateAddIntervalUnit();
				setState(269);
				match(TO);
				setState(270);
				((DateAddIntervalContext)_localctx).right = singleOperand();
				}
				break;
			case ROUND:
				_localctx = new RoundContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(272);
				match(ROUND);
				setState(273);
				singleOperand();
				setState(274);
				match(TO);
				}
				break;
			case CONCAT:
				_localctx = new ConcatContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(276);
				match(CONCAT);
				setState(277);
				singleOperand();
				setState(282);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WITH) {
					{
					{
					setState(278);
					match(WITH);
					setState(279);
					singleOperand();
					}
					}
					setState(284);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(285);
				match(END);
				}
				break;
			case CASEWHEN:
				_localctx = new CaseWhenContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(287);
				match(CASEWHEN);
				setState(288);
				condition(0);
				setState(289);
				match(THEN);
				setState(290);
				singleOperand();
				setState(298);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WHEN) {
					{
					{
					setState(291);
					match(WHEN);
					setState(292);
					condition(0);
					setState(293);
					match(THEN);
					setState(294);
					singleOperand();
					}
					}
					setState(300);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(303);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OTHERWISE) {
					{
					setState(301);
					match(OTHERWISE);
					setState(302);
					((CaseWhenContext)_localctx).otherwiseOperand = singleOperand();
					}
				}

				setState(305);
				caseWhenEnd();
				}
				break;
			case BEGINEXPR:
				_localctx = new ExprContext(_localctx);
				enterOuterAlt(_localctx, 15);
				{
				setState(307);
				match(BEGINEXPR);
				setState(308);
				exprBody();
				setState(309);
				match(ENDEXPR);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UnaryFunctionNameContext extends ParserRuleContext {
		public Token token;
		public TerminalNode UPPERCASE() { return getToken(EQLParser.UPPERCASE, 0); }
		public TerminalNode LOWERCASE() { return getToken(EQLParser.LOWERCASE, 0); }
		public TerminalNode SECONDOF() { return getToken(EQLParser.SECONDOF, 0); }
		public TerminalNode MINUTEOF() { return getToken(EQLParser.MINUTEOF, 0); }
		public TerminalNode HOUROF() { return getToken(EQLParser.HOUROF, 0); }
		public TerminalNode DAYOF() { return getToken(EQLParser.DAYOF, 0); }
		public TerminalNode MONTHOF() { return getToken(EQLParser.MONTHOF, 0); }
		public TerminalNode YEAROF() { return getToken(EQLParser.YEAROF, 0); }
		public TerminalNode DAYOFWEEKOF() { return getToken(EQLParser.DAYOFWEEKOF, 0); }
		public TerminalNode ABSOF() { return getToken(EQLParser.ABSOF, 0); }
		public TerminalNode DATEOF() { return getToken(EQLParser.DATEOF, 0); }
		public UnaryFunctionNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unaryFunctionName; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitUnaryFunctionName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnaryFunctionNameContext unaryFunctionName() throws RecognitionException {
		UnaryFunctionNameContext _localctx = new UnaryFunctionNameContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_unaryFunctionName);
		try {
			setState(324);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case UPPERCASE:
				enterOuterAlt(_localctx, 1);
				{
				setState(313);
				((UnaryFunctionNameContext)_localctx).token = match(UPPERCASE);
				}
				break;
			case LOWERCASE:
				enterOuterAlt(_localctx, 2);
				{
				setState(314);
				((UnaryFunctionNameContext)_localctx).token = match(LOWERCASE);
				}
				break;
			case SECONDOF:
				enterOuterAlt(_localctx, 3);
				{
				setState(315);
				((UnaryFunctionNameContext)_localctx).token = match(SECONDOF);
				}
				break;
			case MINUTEOF:
				enterOuterAlt(_localctx, 4);
				{
				setState(316);
				((UnaryFunctionNameContext)_localctx).token = match(MINUTEOF);
				}
				break;
			case HOUROF:
				enterOuterAlt(_localctx, 5);
				{
				setState(317);
				((UnaryFunctionNameContext)_localctx).token = match(HOUROF);
				}
				break;
			case DAYOF:
				enterOuterAlt(_localctx, 6);
				{
				setState(318);
				((UnaryFunctionNameContext)_localctx).token = match(DAYOF);
				}
				break;
			case MONTHOF:
				enterOuterAlt(_localctx, 7);
				{
				setState(319);
				((UnaryFunctionNameContext)_localctx).token = match(MONTHOF);
				}
				break;
			case YEAROF:
				enterOuterAlt(_localctx, 8);
				{
				setState(320);
				((UnaryFunctionNameContext)_localctx).token = match(YEAROF);
				}
				break;
			case DAYOFWEEKOF:
				enterOuterAlt(_localctx, 9);
				{
				setState(321);
				((UnaryFunctionNameContext)_localctx).token = match(DAYOFWEEKOF);
				}
				break;
			case ABSOF:
				enterOuterAlt(_localctx, 10);
				{
				setState(322);
				((UnaryFunctionNameContext)_localctx).token = match(ABSOF);
				}
				break;
			case DATEOF:
				enterOuterAlt(_localctx, 11);
				{
				setState(323);
				((UnaryFunctionNameContext)_localctx).token = match(DATEOF);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DateDiffIntervalUnitContext extends ParserRuleContext {
		public Token token;
		public TerminalNode SECONDS() { return getToken(EQLParser.SECONDS, 0); }
		public TerminalNode MINUTES() { return getToken(EQLParser.MINUTES, 0); }
		public TerminalNode HOURS() { return getToken(EQLParser.HOURS, 0); }
		public TerminalNode DAYS() { return getToken(EQLParser.DAYS, 0); }
		public TerminalNode MONTHS() { return getToken(EQLParser.MONTHS, 0); }
		public TerminalNode YEARS() { return getToken(EQLParser.YEARS, 0); }
		public DateDiffIntervalUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateDiffIntervalUnit; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitDateDiffIntervalUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DateDiffIntervalUnitContext dateDiffIntervalUnit() throws RecognitionException {
		DateDiffIntervalUnitContext _localctx = new DateDiffIntervalUnitContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_dateDiffIntervalUnit);
		try {
			setState(332);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SECONDS:
				enterOuterAlt(_localctx, 1);
				{
				setState(326);
				((DateDiffIntervalUnitContext)_localctx).token = match(SECONDS);
				}
				break;
			case MINUTES:
				enterOuterAlt(_localctx, 2);
				{
				setState(327);
				((DateDiffIntervalUnitContext)_localctx).token = match(MINUTES);
				}
				break;
			case HOURS:
				enterOuterAlt(_localctx, 3);
				{
				setState(328);
				((DateDiffIntervalUnitContext)_localctx).token = match(HOURS);
				}
				break;
			case DAYS:
				enterOuterAlt(_localctx, 4);
				{
				setState(329);
				((DateDiffIntervalUnitContext)_localctx).token = match(DAYS);
				}
				break;
			case MONTHS:
				enterOuterAlt(_localctx, 5);
				{
				setState(330);
				((DateDiffIntervalUnitContext)_localctx).token = match(MONTHS);
				}
				break;
			case YEARS:
				enterOuterAlt(_localctx, 6);
				{
				setState(331);
				((DateDiffIntervalUnitContext)_localctx).token = match(YEARS);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DateAddIntervalUnitContext extends ParserRuleContext {
		public Token token;
		public TerminalNode SECONDS() { return getToken(EQLParser.SECONDS, 0); }
		public TerminalNode MINUTES() { return getToken(EQLParser.MINUTES, 0); }
		public TerminalNode HOURS() { return getToken(EQLParser.HOURS, 0); }
		public TerminalNode DAYS() { return getToken(EQLParser.DAYS, 0); }
		public TerminalNode MONTHS() { return getToken(EQLParser.MONTHS, 0); }
		public TerminalNode YEARS() { return getToken(EQLParser.YEARS, 0); }
		public DateAddIntervalUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateAddIntervalUnit; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitDateAddIntervalUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DateAddIntervalUnitContext dateAddIntervalUnit() throws RecognitionException {
		DateAddIntervalUnitContext _localctx = new DateAddIntervalUnitContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_dateAddIntervalUnit);
		try {
			setState(340);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SECONDS:
				enterOuterAlt(_localctx, 1);
				{
				setState(334);
				((DateAddIntervalUnitContext)_localctx).token = match(SECONDS);
				}
				break;
			case MINUTES:
				enterOuterAlt(_localctx, 2);
				{
				setState(335);
				((DateAddIntervalUnitContext)_localctx).token = match(MINUTES);
				}
				break;
			case HOURS:
				enterOuterAlt(_localctx, 3);
				{
				setState(336);
				((DateAddIntervalUnitContext)_localctx).token = match(HOURS);
				}
				break;
			case DAYS:
				enterOuterAlt(_localctx, 4);
				{
				setState(337);
				((DateAddIntervalUnitContext)_localctx).token = match(DAYS);
				}
				break;
			case MONTHS:
				enterOuterAlt(_localctx, 5);
				{
				setState(338);
				((DateAddIntervalUnitContext)_localctx).token = match(MONTHS);
				}
				break;
			case YEARS:
				enterOuterAlt(_localctx, 6);
				{
				setState(339);
				((DateAddIntervalUnitContext)_localctx).token = match(YEARS);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CaseWhenEndContext extends ParserRuleContext {
		public Token token;
		public TerminalNode END() { return getToken(EQLParser.END, 0); }
		public TerminalNode ENDASINT() { return getToken(EQLParser.ENDASINT, 0); }
		public TerminalNode ENDASBOOL() { return getToken(EQLParser.ENDASBOOL, 0); }
		public TerminalNode ENDASSTR() { return getToken(EQLParser.ENDASSTR, 0); }
		public TerminalNode ENDASDECIMAL() { return getToken(EQLParser.ENDASDECIMAL, 0); }
		public CaseWhenEndContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseWhenEnd; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitCaseWhenEnd(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CaseWhenEndContext caseWhenEnd() throws RecognitionException {
		CaseWhenEndContext _localctx = new CaseWhenEndContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_caseWhenEnd);
		try {
			setState(347);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case END:
				enterOuterAlt(_localctx, 1);
				{
				setState(342);
				((CaseWhenEndContext)_localctx).token = match(END);
				}
				break;
			case ENDASINT:
				enterOuterAlt(_localctx, 2);
				{
				setState(343);
				((CaseWhenEndContext)_localctx).token = match(ENDASINT);
				}
				break;
			case ENDASBOOL:
				enterOuterAlt(_localctx, 3);
				{
				setState(344);
				((CaseWhenEndContext)_localctx).token = match(ENDASBOOL);
				}
				break;
			case ENDASSTR:
				enterOuterAlt(_localctx, 4);
				{
				setState(345);
				((CaseWhenEndContext)_localctx).token = match(ENDASSTR);
				}
				break;
			case ENDASDECIMAL:
				enterOuterAlt(_localctx, 5);
				{
				setState(346);
				((CaseWhenEndContext)_localctx).token = match(ENDASDECIMAL);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MultiOperandContext extends ParserRuleContext {
		public Token token;
		public TerminalNode ANYOFPROPS() { return getToken(EQLParser.ANYOFPROPS, 0); }
		public TerminalNode ALLOFPROPS() { return getToken(EQLParser.ALLOFPROPS, 0); }
		public TerminalNode ANYOFVALUES() { return getToken(EQLParser.ANYOFVALUES, 0); }
		public TerminalNode ALLOFVALUES() { return getToken(EQLParser.ALLOFVALUES, 0); }
		public TerminalNode ANYOFPARAMS() { return getToken(EQLParser.ANYOFPARAMS, 0); }
		public TerminalNode ANYOFIPARAMS() { return getToken(EQLParser.ANYOFIPARAMS, 0); }
		public TerminalNode ALLOFPARAMS() { return getToken(EQLParser.ALLOFPARAMS, 0); }
		public TerminalNode ALLOFIPARAMS() { return getToken(EQLParser.ALLOFIPARAMS, 0); }
		public TerminalNode ANYOFMODELS() { return getToken(EQLParser.ANYOFMODELS, 0); }
		public TerminalNode ALLOFMODELS() { return getToken(EQLParser.ALLOFMODELS, 0); }
		public TerminalNode ANYOFEXPRESSIONS() { return getToken(EQLParser.ANYOFEXPRESSIONS, 0); }
		public TerminalNode ALLOFEXPRESSIONS() { return getToken(EQLParser.ALLOFEXPRESSIONS, 0); }
		public MultiOperandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multiOperand; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitMultiOperand(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultiOperandContext multiOperand() throws RecognitionException {
		MultiOperandContext _localctx = new MultiOperandContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_multiOperand);
		try {
			setState(361);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ANYOFPROPS:
				enterOuterAlt(_localctx, 1);
				{
				setState(349);
				((MultiOperandContext)_localctx).token = match(ANYOFPROPS);
				}
				break;
			case ALLOFPROPS:
				enterOuterAlt(_localctx, 2);
				{
				setState(350);
				((MultiOperandContext)_localctx).token = match(ALLOFPROPS);
				}
				break;
			case ANYOFVALUES:
				enterOuterAlt(_localctx, 3);
				{
				setState(351);
				((MultiOperandContext)_localctx).token = match(ANYOFVALUES);
				}
				break;
			case ALLOFVALUES:
				enterOuterAlt(_localctx, 4);
				{
				setState(352);
				((MultiOperandContext)_localctx).token = match(ALLOFVALUES);
				}
				break;
			case ANYOFPARAMS:
				enterOuterAlt(_localctx, 5);
				{
				setState(353);
				((MultiOperandContext)_localctx).token = match(ANYOFPARAMS);
				}
				break;
			case ANYOFIPARAMS:
				enterOuterAlt(_localctx, 6);
				{
				setState(354);
				((MultiOperandContext)_localctx).token = match(ANYOFIPARAMS);
				}
				break;
			case ALLOFPARAMS:
				enterOuterAlt(_localctx, 7);
				{
				setState(355);
				((MultiOperandContext)_localctx).token = match(ALLOFPARAMS);
				}
				break;
			case ALLOFIPARAMS:
				enterOuterAlt(_localctx, 8);
				{
				setState(356);
				((MultiOperandContext)_localctx).token = match(ALLOFIPARAMS);
				}
				break;
			case ANYOFMODELS:
				enterOuterAlt(_localctx, 9);
				{
				setState(357);
				((MultiOperandContext)_localctx).token = match(ANYOFMODELS);
				}
				break;
			case ALLOFMODELS:
				enterOuterAlt(_localctx, 10);
				{
				setState(358);
				((MultiOperandContext)_localctx).token = match(ALLOFMODELS);
				}
				break;
			case ANYOFEXPRESSIONS:
				enterOuterAlt(_localctx, 11);
				{
				setState(359);
				((MultiOperandContext)_localctx).token = match(ANYOFEXPRESSIONS);
				}
				break;
			case ALLOFEXPRESSIONS:
				enterOuterAlt(_localctx, 12);
				{
				setState(360);
				((MultiOperandContext)_localctx).token = match(ALLOFEXPRESSIONS);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MembershipOperatorContext extends ParserRuleContext {
		public Token token;
		public TerminalNode IN() { return getToken(EQLParser.IN, 0); }
		public TerminalNode NOTIN() { return getToken(EQLParser.NOTIN, 0); }
		public MembershipOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_membershipOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitMembershipOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MembershipOperatorContext membershipOperator() throws RecognitionException {
		MembershipOperatorContext _localctx = new MembershipOperatorContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_membershipOperator);
		try {
			setState(365);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IN:
				enterOuterAlt(_localctx, 1);
				{
				setState(363);
				((MembershipOperatorContext)_localctx).token = match(IN);
				}
				break;
			case NOTIN:
				enterOuterAlt(_localctx, 2);
				{
				setState(364);
				((MembershipOperatorContext)_localctx).token = match(NOTIN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MembershipOperandContext extends ParserRuleContext {
		public Token token;
		public TerminalNode VALUES() { return getToken(EQLParser.VALUES, 0); }
		public TerminalNode PROPS() { return getToken(EQLParser.PROPS, 0); }
		public TerminalNode PARAMS() { return getToken(EQLParser.PARAMS, 0); }
		public TerminalNode IPARAMS() { return getToken(EQLParser.IPARAMS, 0); }
		public TerminalNode MODEL() { return getToken(EQLParser.MODEL, 0); }
		public MembershipOperandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_membershipOperand; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitMembershipOperand(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MembershipOperandContext membershipOperand() throws RecognitionException {
		MembershipOperandContext _localctx = new MembershipOperandContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_membershipOperand);
		try {
			setState(372);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VALUES:
				enterOuterAlt(_localctx, 1);
				{
				setState(367);
				((MembershipOperandContext)_localctx).token = match(VALUES);
				}
				break;
			case PROPS:
				enterOuterAlt(_localctx, 2);
				{
				setState(368);
				((MembershipOperandContext)_localctx).token = match(PROPS);
				}
				break;
			case PARAMS:
				enterOuterAlt(_localctx, 3);
				{
				setState(369);
				((MembershipOperandContext)_localctx).token = match(PARAMS);
				}
				break;
			case IPARAMS:
				enterOuterAlt(_localctx, 4);
				{
				setState(370);
				((MembershipOperandContext)_localctx).token = match(IPARAMS);
				}
				break;
			case MODEL:
				enterOuterAlt(_localctx, 5);
				{
				setState(371);
				((MembershipOperandContext)_localctx).token = match(MODEL);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class JoinContext extends ParserRuleContext {
		public Token alias;
		public JoinOperatorContext joinOperator() {
			return getRuleContext(JoinOperatorContext.class,0);
		}
		public JoinConditionContext joinCondition() {
			return getRuleContext(JoinConditionContext.class,0);
		}
		public JoinContext join() {
			return getRuleContext(JoinContext.class,0);
		}
		public TerminalNode AS() { return getToken(EQLParser.AS, 0); }
		public JoinContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_join; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitJoin(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JoinContext join() throws RecognitionException {
		JoinContext _localctx = new JoinContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_join);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(374);
			joinOperator();
			setState(376);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(375);
				((JoinContext)_localctx).alias = match(AS);
				}
			}

			setState(378);
			joinCondition();
			setState(380);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==JOIN || _la==LEFTJOIN) {
				{
				setState(379);
				join();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class JoinOperatorContext extends ParserRuleContext {
		public Token token;
		public TerminalNode JOIN() { return getToken(EQLParser.JOIN, 0); }
		public TerminalNode LEFTJOIN() { return getToken(EQLParser.LEFTJOIN, 0); }
		public JoinOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitJoinOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JoinOperatorContext joinOperator() throws RecognitionException {
		JoinOperatorContext _localctx = new JoinOperatorContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_joinOperator);
		try {
			setState(384);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case JOIN:
				enterOuterAlt(_localctx, 1);
				{
				setState(382);
				((JoinOperatorContext)_localctx).token = match(JOIN);
				}
				break;
			case LEFTJOIN:
				enterOuterAlt(_localctx, 2);
				{
				setState(383);
				((JoinOperatorContext)_localctx).token = match(LEFTJOIN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class JoinConditionContext extends ParserRuleContext {
		public TerminalNode ON() { return getToken(EQLParser.ON, 0); }
		public ConditionContext condition() {
			return getRuleContext(ConditionContext.class,0);
		}
		public JoinConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinCondition; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitJoinCondition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JoinConditionContext joinCondition() throws RecognitionException {
		JoinConditionContext _localctx = new JoinConditionContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_joinCondition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(386);
			match(ON);
			setState(387);
			condition(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GroupByContext extends ParserRuleContext {
		public SingleOperandContext operand;
		public TerminalNode GROUPBY() { return getToken(EQLParser.GROUPBY, 0); }
		public SingleOperandContext singleOperand() {
			return getRuleContext(SingleOperandContext.class,0);
		}
		public GroupByContext groupBy() {
			return getRuleContext(GroupByContext.class,0);
		}
		public GroupByContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupBy; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitGroupBy(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupByContext groupBy() throws RecognitionException {
		GroupByContext _localctx = new GroupByContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_groupBy);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(389);
			match(GROUPBY);
			setState(390);
			((GroupByContext)_localctx).operand = singleOperand();
			setState(392);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==GROUPBY) {
				{
				setState(391);
				groupBy();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AnyYieldContext extends ParserRuleContext {
		public AnyYieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_anyYield; }
	 
		public AnyYieldContext() { }
		public void copyFrom(AnyYieldContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class YieldManyContext extends AnyYieldContext {
		public YieldManyModelContext model_;
		public YieldManyModelContext yieldManyModel() {
			return getRuleContext(YieldManyModelContext.class,0);
		}
		public TerminalNode YIELDALL() { return getToken(EQLParser.YIELDALL, 0); }
		public List<AliasedYieldContext> aliasedYield() {
			return getRuleContexts(AliasedYieldContext.class);
		}
		public AliasedYieldContext aliasedYield(int i) {
			return getRuleContext(AliasedYieldContext.class,i);
		}
		public YieldManyContext(AnyYieldContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitYieldMany(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class Yield1Context extends AnyYieldContext {
		public YieldOperandContext operand;
		public Yield1ModelContext model_;
		public TerminalNode YIELD() { return getToken(EQLParser.YIELD, 0); }
		public YieldOperandContext yieldOperand() {
			return getRuleContext(YieldOperandContext.class,0);
		}
		public Yield1ModelContext yield1Model() {
			return getRuleContext(Yield1ModelContext.class,0);
		}
		public Yield1Context(AnyYieldContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitYield1(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnyYieldContext anyYield() throws RecognitionException {
		AnyYieldContext _localctx = new AnyYieldContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_anyYield);
		int _la;
		try {
			setState(408);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
			case 1:
				_localctx = new Yield1Context(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(394);
				match(YIELD);
				setState(395);
				((Yield1Context)_localctx).operand = yieldOperand();
				setState(396);
				((Yield1Context)_localctx).model_ = yield1Model();
				}
				break;
			case 2:
				_localctx = new YieldManyContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(399);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==YIELDALL) {
					{
					setState(398);
					match(YIELDALL);
					}
				}

				setState(402); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(401);
					aliasedYield();
					}
					}
					setState(404); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==YIELD );
				setState(406);
				((YieldManyContext)_localctx).model_ = yieldManyModel();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AliasedYieldContext extends ParserRuleContext {
		public YieldOperandContext operand;
		public YieldAliasContext alias;
		public TerminalNode YIELD() { return getToken(EQLParser.YIELD, 0); }
		public YieldOperandContext yieldOperand() {
			return getRuleContext(YieldOperandContext.class,0);
		}
		public YieldAliasContext yieldAlias() {
			return getRuleContext(YieldAliasContext.class,0);
		}
		public AliasedYieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aliasedYield; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitAliasedYield(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AliasedYieldContext aliasedYield() throws RecognitionException {
		AliasedYieldContext _localctx = new AliasedYieldContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_aliasedYield);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(410);
			match(YIELD);
			setState(411);
			((AliasedYieldContext)_localctx).operand = yieldOperand();
			setState(412);
			((AliasedYieldContext)_localctx).alias = yieldAlias();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class YieldOperandContext extends ParserRuleContext {
		public YieldOperandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_yieldOperand; }
	 
		public YieldOperandContext() { }
		public void copyFrom(YieldOperandContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class YieldOperand_SingleOperandOrExprContext extends YieldOperandContext {
		public SingleOperandContext singleOperand() {
			return getRuleContext(SingleOperandContext.class,0);
		}
		public YieldOperand_SingleOperandOrExprContext(YieldOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitYieldOperand_SingleOperandOrExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class YieldOperand_CountAllContext extends YieldOperandContext {
		public TerminalNode COUNTALL() { return getToken(EQLParser.COUNTALL, 0); }
		public YieldOperand_CountAllContext(YieldOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitYieldOperand_CountAll(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class YieldOperandFunctionContext extends YieldOperandContext {
		public YieldOperandFunctionNameContext funcName;
		public SingleOperandContext argument;
		public YieldOperandFunctionNameContext yieldOperandFunctionName() {
			return getRuleContext(YieldOperandFunctionNameContext.class,0);
		}
		public SingleOperandContext singleOperand() {
			return getRuleContext(SingleOperandContext.class,0);
		}
		public YieldOperandFunctionContext(YieldOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitYieldOperandFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final YieldOperandContext yieldOperand() throws RecognitionException {
		YieldOperandContext _localctx = new YieldOperandContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_yieldOperand);
		try {
			setState(419);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ABSOF:
			case ADDTIMEINTERVALOF:
			case BEGINEXPR:
			case CASEWHEN:
			case CONCAT:
			case COUNT:
			case DATEOF:
			case DAYOF:
			case DAYOFWEEKOF:
			case EXPR:
			case EXTPROP:
			case HOUROF:
			case IFNULL:
			case IPARAM:
			case IVAL:
			case LOWERCASE:
			case MINUTEOF:
			case MODEL:
			case MONTHOF:
			case NOW:
			case PARAM:
			case PROP:
			case ROUND:
			case SECONDOF:
			case UPPERCASE:
			case VAL:
			case YEAROF:
				_localctx = new YieldOperand_SingleOperandOrExprContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(414);
				singleOperand();
				}
				break;
			case COUNTALL:
				_localctx = new YieldOperand_CountAllContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(415);
				match(COUNTALL);
				}
				break;
			case AVGOF:
			case AVGOFDISTINCT:
			case COUNTOF:
			case COUNTOFDISTINCT:
			case MAXOF:
			case MINOF:
			case SUMOF:
			case SUMOFDISTINCT:
				_localctx = new YieldOperandFunctionContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(416);
				((YieldOperandFunctionContext)_localctx).funcName = yieldOperandFunctionName();
				setState(417);
				((YieldOperandFunctionContext)_localctx).argument = singleOperand();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class YieldOperandFunctionNameContext extends ParserRuleContext {
		public Token token;
		public TerminalNode MAXOF() { return getToken(EQLParser.MAXOF, 0); }
		public TerminalNode MINOF() { return getToken(EQLParser.MINOF, 0); }
		public TerminalNode SUMOF() { return getToken(EQLParser.SUMOF, 0); }
		public TerminalNode COUNTOF() { return getToken(EQLParser.COUNTOF, 0); }
		public TerminalNode AVGOF() { return getToken(EQLParser.AVGOF, 0); }
		public TerminalNode SUMOFDISTINCT() { return getToken(EQLParser.SUMOFDISTINCT, 0); }
		public TerminalNode COUNTOFDISTINCT() { return getToken(EQLParser.COUNTOFDISTINCT, 0); }
		public TerminalNode AVGOFDISTINCT() { return getToken(EQLParser.AVGOFDISTINCT, 0); }
		public YieldOperandFunctionNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_yieldOperandFunctionName; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitYieldOperandFunctionName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final YieldOperandFunctionNameContext yieldOperandFunctionName() throws RecognitionException {
		YieldOperandFunctionNameContext _localctx = new YieldOperandFunctionNameContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_yieldOperandFunctionName);
		try {
			setState(429);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MAXOF:
				enterOuterAlt(_localctx, 1);
				{
				setState(421);
				((YieldOperandFunctionNameContext)_localctx).token = match(MAXOF);
				}
				break;
			case MINOF:
				enterOuterAlt(_localctx, 2);
				{
				setState(422);
				((YieldOperandFunctionNameContext)_localctx).token = match(MINOF);
				}
				break;
			case SUMOF:
				enterOuterAlt(_localctx, 3);
				{
				setState(423);
				((YieldOperandFunctionNameContext)_localctx).token = match(SUMOF);
				}
				break;
			case COUNTOF:
				enterOuterAlt(_localctx, 4);
				{
				setState(424);
				((YieldOperandFunctionNameContext)_localctx).token = match(COUNTOF);
				}
				break;
			case AVGOF:
				enterOuterAlt(_localctx, 5);
				{
				setState(425);
				((YieldOperandFunctionNameContext)_localctx).token = match(AVGOF);
				}
				break;
			case SUMOFDISTINCT:
				enterOuterAlt(_localctx, 6);
				{
				setState(426);
				((YieldOperandFunctionNameContext)_localctx).token = match(SUMOFDISTINCT);
				}
				break;
			case COUNTOFDISTINCT:
				enterOuterAlt(_localctx, 7);
				{
				setState(427);
				((YieldOperandFunctionNameContext)_localctx).token = match(COUNTOFDISTINCT);
				}
				break;
			case AVGOFDISTINCT:
				enterOuterAlt(_localctx, 8);
				{
				setState(428);
				((YieldOperandFunctionNameContext)_localctx).token = match(AVGOFDISTINCT);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class YieldAliasContext extends ParserRuleContext {
		public Token token;
		public TerminalNode AS() { return getToken(EQLParser.AS, 0); }
		public TerminalNode ASREQUIRED() { return getToken(EQLParser.ASREQUIRED, 0); }
		public YieldAliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_yieldAlias; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitYieldAlias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final YieldAliasContext yieldAlias() throws RecognitionException {
		YieldAliasContext _localctx = new YieldAliasContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_yieldAlias);
		try {
			setState(433);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AS:
				enterOuterAlt(_localctx, 1);
				{
				setState(431);
				((YieldAliasContext)_localctx).token = match(AS);
				}
				break;
			case ASREQUIRED:
				enterOuterAlt(_localctx, 2);
				{
				setState(432);
				((YieldAliasContext)_localctx).token = match(ASREQUIRED);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Yield1ModelContext extends ParserRuleContext {
		public Token token;
		public TerminalNode MODELASENTITY() { return getToken(EQLParser.MODELASENTITY, 0); }
		public TerminalNode MODELASPRIMITIVE() { return getToken(EQLParser.MODELASPRIMITIVE, 0); }
		public Yield1ModelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_yield1Model; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitYield1Model(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Yield1ModelContext yield1Model() throws RecognitionException {
		Yield1ModelContext _localctx = new Yield1ModelContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_yield1Model);
		try {
			setState(437);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MODELASENTITY:
				enterOuterAlt(_localctx, 1);
				{
				setState(435);
				((Yield1ModelContext)_localctx).token = match(MODELASENTITY);
				}
				break;
			case MODELASPRIMITIVE:
				enterOuterAlt(_localctx, 2);
				{
				setState(436);
				((Yield1ModelContext)_localctx).token = match(MODELASPRIMITIVE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class YieldManyModelContext extends ParserRuleContext {
		public Token token;
		public TerminalNode MODELASENTITY() { return getToken(EQLParser.MODELASENTITY, 0); }
		public TerminalNode MODELASAGGREGATE() { return getToken(EQLParser.MODELASAGGREGATE, 0); }
		public YieldManyModelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_yieldManyModel; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitYieldManyModel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final YieldManyModelContext yieldManyModel() throws RecognitionException {
		YieldManyModelContext _localctx = new YieldManyModelContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_yieldManyModel);
		try {
			setState(441);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MODELASENTITY:
				enterOuterAlt(_localctx, 1);
				{
				setState(439);
				((YieldManyModelContext)_localctx).token = match(MODELASENTITY);
				}
				break;
			case MODELASAGGREGATE:
				enterOuterAlt(_localctx, 2);
				{
				setState(440);
				((YieldManyModelContext)_localctx).token = match(MODELASAGGREGATE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ModelContext extends ParserRuleContext {
		public Token token;
		public TerminalNode MODEL() { return getToken(EQLParser.MODEL, 0); }
		public TerminalNode MODELASENTITY() { return getToken(EQLParser.MODELASENTITY, 0); }
		public TerminalNode MODELASAGGREGATE() { return getToken(EQLParser.MODELASAGGREGATE, 0); }
		public ModelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_model; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitModel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ModelContext model() throws RecognitionException {
		ModelContext _localctx = new ModelContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_model);
		try {
			setState(446);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MODEL:
				enterOuterAlt(_localctx, 1);
				{
				setState(443);
				((ModelContext)_localctx).token = match(MODEL);
				}
				break;
			case MODELASENTITY:
				enterOuterAlt(_localctx, 2);
				{
				setState(444);
				((ModelContext)_localctx).token = match(MODELASENTITY);
				}
				break;
			case MODELASAGGREGATE:
				enterOuterAlt(_localctx, 3);
				{
				setState(445);
				((ModelContext)_localctx).token = match(MODELASAGGREGATE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StandaloneConditionContext extends ParserRuleContext {
		public StandaloneConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_standaloneCondition; }
	 
		public StandaloneConditionContext() { }
		public void copyFrom(StandaloneConditionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class AndStandaloneConditionContext extends StandaloneConditionContext {
		public StandaloneConditionContext left;
		public StandaloneConditionContext right;
		public TerminalNode AND() { return getToken(EQLParser.AND, 0); }
		public List<StandaloneConditionContext> standaloneCondition() {
			return getRuleContexts(StandaloneConditionContext.class);
		}
		public StandaloneConditionContext standaloneCondition(int i) {
			return getRuleContext(StandaloneConditionContext.class,i);
		}
		public AndStandaloneConditionContext(StandaloneConditionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitAndStandaloneCondition(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class StandaloneCondition_PredicateContext extends StandaloneConditionContext {
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public StandaloneCondition_PredicateContext(StandaloneConditionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitStandaloneCondition_Predicate(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class OrStandaloneConditionContext extends StandaloneConditionContext {
		public StandaloneConditionContext left;
		public StandaloneConditionContext right;
		public TerminalNode OR() { return getToken(EQLParser.OR, 0); }
		public List<StandaloneConditionContext> standaloneCondition() {
			return getRuleContexts(StandaloneConditionContext.class);
		}
		public StandaloneConditionContext standaloneCondition(int i) {
			return getRuleContext(StandaloneConditionContext.class,i);
		}
		public OrStandaloneConditionContext(StandaloneConditionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitOrStandaloneCondition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StandaloneConditionContext standaloneCondition() throws RecognitionException {
		return standaloneCondition(0);
	}

	private StandaloneConditionContext standaloneCondition(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		StandaloneConditionContext _localctx = new StandaloneConditionContext(_ctx, _parentState);
		StandaloneConditionContext _prevctx = _localctx;
		int _startState = 70;
		enterRecursionRule(_localctx, 70, RULE_standaloneCondition, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			_localctx = new StandaloneCondition_PredicateContext(_localctx);
			_ctx = _localctx;
			_prevctx = _localctx;

			setState(449);
			predicate();
			}
			_ctx.stop = _input.LT(-1);
			setState(459);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,49,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(457);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
					case 1:
						{
						_localctx = new AndStandaloneConditionContext(new StandaloneConditionContext(_parentctx, _parentState));
						((AndStandaloneConditionContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_standaloneCondition);
						setState(451);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(452);
						match(AND);
						setState(453);
						((AndStandaloneConditionContext)_localctx).right = standaloneCondition(3);
						}
						break;
					case 2:
						{
						_localctx = new OrStandaloneConditionContext(new StandaloneConditionContext(_parentctx, _parentState));
						((OrStandaloneConditionContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_standaloneCondition);
						setState(454);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(455);
						match(OR);
						setState(456);
						((OrStandaloneConditionContext)_localctx).right = standaloneCondition(2);
						}
						break;
					}
					} 
				}
				setState(461);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,49,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class OrderByOperandContext extends ParserRuleContext {
		public OrderByOperandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderByOperand; }
	 
		public OrderByOperandContext() { }
		public void copyFrom(OrderByOperandContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class OrderByOperand_SingleContext extends OrderByOperandContext {
		public SingleOperandContext singleOperand() {
			return getRuleContext(SingleOperandContext.class,0);
		}
		public OrderContext order() {
			return getRuleContext(OrderContext.class,0);
		}
		public OrderByOperand_SingleContext(OrderByOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitOrderByOperand_Single(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class OrderByOperand_YieldContext extends OrderByOperandContext {
		public TerminalNode YIELD() { return getToken(EQLParser.YIELD, 0); }
		public OrderContext order() {
			return getRuleContext(OrderContext.class,0);
		}
		public OrderByOperand_YieldContext(OrderByOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitOrderByOperand_Yield(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class OrderByOperand_OrderingModelContext extends OrderByOperandContext {
		public TerminalNode ORDER() { return getToken(EQLParser.ORDER, 0); }
		public OrderByOperand_OrderingModelContext(OrderByOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitOrderByOperand_OrderingModel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderByOperandContext orderByOperand() throws RecognitionException {
		OrderByOperandContext _localctx = new OrderByOperandContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_orderByOperand);
		try {
			setState(468);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ABSOF:
			case ADDTIMEINTERVALOF:
			case BEGINEXPR:
			case CASEWHEN:
			case CONCAT:
			case COUNT:
			case DATEOF:
			case DAYOF:
			case DAYOFWEEKOF:
			case EXPR:
			case EXTPROP:
			case HOUROF:
			case IFNULL:
			case IPARAM:
			case IVAL:
			case LOWERCASE:
			case MINUTEOF:
			case MODEL:
			case MONTHOF:
			case NOW:
			case PARAM:
			case PROP:
			case ROUND:
			case SECONDOF:
			case UPPERCASE:
			case VAL:
			case YEAROF:
				_localctx = new OrderByOperand_SingleContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(462);
				singleOperand();
				setState(463);
				order();
				}
				break;
			case YIELD:
				_localctx = new OrderByOperand_YieldContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(465);
				match(YIELD);
				setState(466);
				order();
				}
				break;
			case ORDER:
				_localctx = new OrderByOperand_OrderingModelContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(467);
				match(ORDER);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OrderContext extends ParserRuleContext {
		public Token token;
		public TerminalNode ASC() { return getToken(EQLParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(EQLParser.DESC, 0); }
		public OrderContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_order; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitOrder(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderContext order() throws RecognitionException {
		OrderContext _localctx = new OrderContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_order);
		try {
			setState(472);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ASC:
				enterOuterAlt(_localctx, 1);
				{
				setState(470);
				((OrderContext)_localctx).token = match(ASC);
				}
				break;
			case DESC:
				enterOuterAlt(_localctx, 2);
				{
				setState(471);
				((OrderContext)_localctx).token = match(DESC);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 6:
			return condition_sempred((ConditionContext)_localctx, predIndex);
		case 35:
			return standaloneCondition_sempred((StandaloneConditionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean condition_sempred(ConditionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 4);
		case 1:
			return precpred(_ctx, 3);
		}
		return true;
	}
	private boolean standaloneCondition_sempred(StandaloneConditionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return precpred(_ctx, 2);
		case 3:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\u0083\u01dd\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\3\2\3\2\3\2\3\3\3\3\3\3\3"+
		"\3\3\3\3\3\7\3X\n\3\f\3\16\3[\13\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\6\3"+
		"e\n\3\r\3\16\3f\3\3\3\3\5\3k\n\3\3\4\3\4\5\4o\n\4\3\4\5\4r\n\4\3\4\5\4"+
		"u\n\4\3\4\5\4x\n\4\3\4\3\4\3\4\3\4\5\4~\n\4\3\4\5\4\u0081\n\4\3\5\3\5"+
		"\3\6\3\6\5\6\u0087\n\6\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b"+
		"\3\b\5\b\u0096\n\b\3\b\3\b\3\b\3\b\3\b\3\b\7\b\u009e\n\b\f\b\16\b\u00a1"+
		"\13\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t"+
		"\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\5\t\u00bf\n\t\5\t\u00c1"+
		"\n\t\3\n\3\n\5\n\u00c5\n\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13"+
		"\u00cf\n\13\3\f\3\f\5\f\u00d3\n\f\3\r\3\r\3\r\3\r\3\r\3\r\5\r\u00db\n"+
		"\r\3\16\3\16\5\16\u00df\n\16\3\17\3\17\3\17\3\17\7\17\u00e5\n\17\f\17"+
		"\16\17\u00e8\13\17\3\20\3\20\3\20\3\20\3\20\5\20\u00ef\n\20\3\21\3\21"+
		"\3\21\3\21\5\21\u00f5\n\21\3\21\3\21\5\21\u00f9\n\21\3\21\3\21\3\21\3"+
		"\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3"+
		"\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3"+
		"\21\7\21\u011b\n\21\f\21\16\21\u011e\13\21\3\21\3\21\3\21\3\21\3\21\3"+
		"\21\3\21\3\21\3\21\3\21\3\21\7\21\u012b\n\21\f\21\16\21\u012e\13\21\3"+
		"\21\3\21\5\21\u0132\n\21\3\21\3\21\3\21\3\21\3\21\3\21\5\21\u013a\n\21"+
		"\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\5\22\u0147\n\22"+
		"\3\23\3\23\3\23\3\23\3\23\3\23\5\23\u014f\n\23\3\24\3\24\3\24\3\24\3\24"+
		"\3\24\5\24\u0157\n\24\3\25\3\25\3\25\3\25\3\25\5\25\u015e\n\25\3\26\3"+
		"\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\5\26\u016c\n\26"+
		"\3\27\3\27\5\27\u0170\n\27\3\30\3\30\3\30\3\30\3\30\5\30\u0177\n\30\3"+
		"\31\3\31\5\31\u017b\n\31\3\31\3\31\5\31\u017f\n\31\3\32\3\32\5\32\u0183"+
		"\n\32\3\33\3\33\3\33\3\34\3\34\3\34\5\34\u018b\n\34\3\35\3\35\3\35\3\35"+
		"\3\35\5\35\u0192\n\35\3\35\6\35\u0195\n\35\r\35\16\35\u0196\3\35\3\35"+
		"\5\35\u019b\n\35\3\36\3\36\3\36\3\36\3\37\3\37\3\37\3\37\3\37\5\37\u01a6"+
		"\n\37\3 \3 \3 \3 \3 \3 \3 \3 \5 \u01b0\n \3!\3!\5!\u01b4\n!\3\"\3\"\5"+
		"\"\u01b8\n\"\3#\3#\5#\u01bc\n#\3$\3$\3$\5$\u01c1\n$\3%\3%\3%\3%\3%\3%"+
		"\3%\3%\3%\7%\u01cc\n%\f%\16%\u01cf\13%\3&\3&\3&\3&\3&\3&\5&\u01d7\n&\3"+
		"\'\3\'\5\'\u01db\n\'\3\'\2\4\16H(\2\4\6\b\n\f\16\20\22\24\26\30\32\34"+
		"\36 \"$&(*,.\60\62\64\668:<>@BDFHJL\2\2\2\u023c\2N\3\2\2\2\4j\3\2\2\2"+
		"\6\u0080\3\2\2\2\b\u0082\3\2\2\2\n\u0086\3\2\2\2\f\u0088\3\2\2\2\16\u0095"+
		"\3\2\2\2\20\u00c0\3\2\2\2\22\u00c4\3\2\2\2\24\u00ce\3\2\2\2\26\u00d2\3"+
		"\2\2\2\30\u00da\3\2\2\2\32\u00de\3\2\2\2\34\u00e0\3\2\2\2\36\u00ee\3\2"+
		"\2\2 \u0139\3\2\2\2\"\u0146\3\2\2\2$\u014e\3\2\2\2&\u0156\3\2\2\2(\u015d"+
		"\3\2\2\2*\u016b\3\2\2\2,\u016f\3\2\2\2.\u0176\3\2\2\2\60\u0178\3\2\2\2"+
		"\62\u0182\3\2\2\2\64\u0184\3\2\2\2\66\u0187\3\2\2\28\u019a\3\2\2\2:\u019c"+
		"\3\2\2\2<\u01a5\3\2\2\2>\u01af\3\2\2\2@\u01b3\3\2\2\2B\u01b7\3\2\2\2D"+
		"\u01bb\3\2\2\2F\u01c0\3\2\2\2H\u01c2\3\2\2\2J\u01d6\3\2\2\2L\u01da\3\2"+
		"\2\2NO\5\4\3\2OP\7\2\2\3P\3\3\2\2\2Qk\5\6\4\2RS\7\66\2\2SY\5<\37\2TU\5"+
		"\36\20\2UV\5<\37\2VX\3\2\2\2WT\3\2\2\2X[\3\2\2\2YW\3\2\2\2YZ\3\2\2\2Z"+
		"\\\3\2\2\2[Y\3\2\2\2\\]\7R\2\2]k\3\2\2\2^_\7\37\2\2_`\5H%\2`a\7R\2\2a"+
		"k\3\2\2\2bd\7h\2\2ce\5J&\2dc\3\2\2\2ef\3\2\2\2fd\3\2\2\2fg\3\2\2\2gh\3"+
		"\2\2\2hi\7R\2\2ik\3\2\2\2jQ\3\2\2\2jR\3\2\2\2j^\3\2\2\2jb\3\2\2\2k\5\3"+
		"\2\2\2ln\5\b\5\2mo\7\25\2\2nm\3\2\2\2no\3\2\2\2oq\3\2\2\2pr\5\60\31\2"+
		"qp\3\2\2\2qr\3\2\2\2rt\3\2\2\2su\5\f\7\2ts\3\2\2\2tu\3\2\2\2uw\3\2\2\2"+
		"vx\5\66\34\2wv\3\2\2\2wx\3\2\2\2xy\3\2\2\2yz\5\n\6\2z\u0081\3\2\2\2{}"+
		"\7q\2\2|~\5\66\34\2}|\3\2\2\2}~\3\2\2\2~\177\3\2\2\2\177\u0081\5\n\6\2"+
		"\u0080l\3\2\2\2\u0080{\3\2\2\2\u0081\7\3\2\2\2\u0082\u0083\7q\2\2\u0083"+
		"\t\3\2\2\2\u0084\u0087\58\35\2\u0085\u0087\5F$\2\u0086\u0084\3\2\2\2\u0086"+
		"\u0085\3\2\2\2\u0087\13\3\2\2\2\u0088\u0089\7{\2\2\u0089\u008a\5\16\b"+
		"\2\u008a\r\3\2\2\2\u008b\u008c\b\b\1\2\u008c\u0096\5\20\t\2\u008d\u008e"+
		"\7\32\2\2\u008e\u008f\5\16\b\2\u008f\u0090\7,\2\2\u0090\u0096\3\2\2\2"+
		"\u0091\u0092\7[\2\2\u0092\u0093\5\16\b\2\u0093\u0094\7,\2\2\u0094\u0096"+
		"\3\2\2\2\u0095\u008b\3\2\2\2\u0095\u008d\3\2\2\2\u0095\u0091\3\2\2\2\u0096"+
		"\u009f\3\2\2\2\u0097\u0098\f\6\2\2\u0098\u0099\7\r\2\2\u0099\u009e\5\16"+
		"\b\7\u009a\u009b\f\5\2\2\u009b\u009c\7f\2\2\u009c\u009e\5\16\b\6\u009d"+
		"\u0097\3\2\2\2\u009d\u009a\3\2\2\2\u009e\u00a1\3\2\2\2\u009f\u009d\3\2"+
		"\2\2\u009f\u00a0\3\2\2\2\u00a0\17\3\2\2\2\u00a1\u009f\3\2\2\2\u00a2\u00a3"+
		"\5\26\f\2\u00a3\u00a4\5\22\n\2\u00a4\u00c1\3\2\2\2\u00a5\u00a6\5\26\f"+
		"\2\u00a6\u00a7\5\30\r\2\u00a7\u00a8\5\26\f\2\u00a8\u00c1\3\2\2\2\u00a9"+
		"\u00aa\5\26\f\2\u00aa\u00ab\5\30\r\2\u00ab\u00ac\5\32\16\2\u00ac\u00c1"+
		"\3\2\2\2\u00ad\u00ae\5\26\f\2\u00ae\u00af\5\24\13\2\u00af\u00b0\5\26\f"+
		"\2\u00b0\u00c1\3\2\2\2\u00b1\u00b2\5\26\f\2\u00b2\u00b3\5,\27\2\u00b3"+
		"\u00b4\5.\30\2\u00b4\u00c1\3\2\2\2\u00b5\u00bf\7\63\2\2\u00b6\u00bf\7"+
		"\\\2\2\u00b7\u00bf\7\65\2\2\u00b8\u00bf\7^\2\2\u00b9\u00bf\7\64\2\2\u00ba"+
		"\u00bf\7]\2\2\u00bb\u00bf\7%\2\2\u00bc\u00bf\7 \2\2\u00bd\u00bf\7Z\2\2"+
		"\u00be\u00b5\3\2\2\2\u00be\u00b6\3\2\2\2\u00be\u00b7\3\2\2\2\u00be\u00b8"+
		"\3\2\2\2\u00be\u00b9\3\2\2\2\u00be\u00ba\3\2\2\2\u00be\u00bb\3\2\2\2\u00be"+
		"\u00bc\3\2\2\2\u00be\u00bd\3\2\2\2\u00bf\u00c1\3\2\2\2\u00c0\u00a2\3\2"+
		"\2\2\u00c0\u00a5\3\2\2\2\u00c0\u00a9\3\2\2\2\u00c0\u00ad\3\2\2\2\u00c0"+
		"\u00b1\3\2\2\2\u00c0\u00be\3\2\2\2\u00c1\21\3\2\2\2\u00c2\u00c5\7D\2\2"+
		"\u00c3\u00c5\7C\2\2\u00c4\u00c2\3\2\2\2\u00c4\u00c3\3\2\2\2\u00c5\23\3"+
		"\2\2\2\u00c6\u00cf\7I\2\2\u00c7\u00cf\7>\2\2\u00c8\u00cf\7J\2\2\u00c9"+
		"\u00cf\7?\2\2\u00ca\u00cf\7b\2\2\u00cb\u00cf\7c\2\2\u00cc\u00cf\7`\2\2"+
		"\u00cd\u00cf\7_\2\2\u00ce\u00c6\3\2\2\2\u00ce\u00c7\3\2\2\2\u00ce\u00c8"+
		"\3\2\2\2\u00ce\u00c9\3\2\2\2\u00ce\u00ca\3\2\2\2\u00ce\u00cb\3\2\2\2\u00ce"+
		"\u00cc\3\2\2\2\u00ce\u00cd\3\2\2\2\u00cf\25\3\2\2\2\u00d0\u00d3\5 \21"+
		"\2\u00d1\u00d3\5*\26\2\u00d2\u00d0\3\2\2\2\u00d2\u00d1\3\2\2\2\u00d3\27"+
		"\3\2\2\2\u00d4\u00db\7\62\2\2\u00d5\u00db\7:\2\2\u00d6\u00db\7L\2\2\u00d7"+
		"\u00db\78\2\2\u00d8\u00db\7G\2\2\u00d9\u00db\7Y\2\2\u00da\u00d4\3\2\2"+
		"\2\u00da\u00d5\3\2\2\2\u00da\u00d6\3\2\2\2\u00da\u00d7\3\2\2\2\u00da\u00d8"+
		"\3\2\2\2\u00da\u00d9\3\2\2\2\u00db\31\3\2\2\2\u00dc\u00df\7\6\2\2\u00dd"+
		"\u00df\7\16\2\2\u00de\u00dc\3\2\2\2\u00de\u00dd\3\2\2\2\u00df\33\3\2\2"+
		"\2\u00e0\u00e6\5 \21\2\u00e1\u00e2\5\36\20\2\u00e2\u00e3\5 \21\2\u00e3"+
		"\u00e5\3\2\2\2\u00e4\u00e1\3\2\2\2\u00e5\u00e8\3\2\2\2\u00e6\u00e4\3\2"+
		"\2\2\u00e6\u00e7\3\2\2\2\u00e7\35\3\2\2\2\u00e8\u00e6\3\2\2\2\u00e9\u00ef"+
		"\7\4\2\2\u00ea\u00ef\7r\2\2\u00eb\u00ef\7+\2\2\u00ec\u00ef\7X\2\2\u00ed"+
		"\u00ef\7Q\2\2\u00ee\u00e9\3\2\2\2\u00ee\u00ea\3\2\2\2\u00ee\u00eb\3\2"+
		"\2\2\u00ee\u00ec\3\2\2\2\u00ee\u00ed\3\2\2\2\u00ef\37\3\2\2\2\u00f0\u013a"+
		"\7l\2\2\u00f1\u013a\7\67\2\2\u00f2\u00f5\7x\2\2\u00f3\u00f5\7E\2\2\u00f4"+
		"\u00f2\3\2\2\2\u00f4\u00f3\3\2\2\2\u00f5\u013a\3\2\2\2\u00f6\u00f9\7j"+
		"\2\2\u00f7\u00f9\7A\2\2\u00f8\u00f6\3\2\2\2\u00f8\u00f7\3\2\2\2\u00f9"+
		"\u013a\3\2\2\2\u00fa\u013a\7\66\2\2\u00fb\u013a\7R\2\2\u00fc\u00fd\5\""+
		"\22\2\u00fd\u00fe\5 \21\2\u00fe\u013a\3\2\2\2\u00ff\u0100\7=\2\2\u0100"+
		"\u0101\5 \21\2\u0101\u0102\7u\2\2\u0102\u0103\5 \21\2\u0103\u013a\3\2"+
		"\2\2\u0104\u013a\7d\2\2\u0105\u0106\7!\2\2\u0106\u0107\5$\23\2\u0107\u0108"+
		"\7\34\2\2\u0108\u0109\5 \21\2\u0109\u010a\7\r\2\2\u010a\u010b\5 \21\2"+
		"\u010b\u013a\3\2\2\2\u010c\u010d\7\5\2\2\u010d\u010e\5 \21\2\u010e\u010f"+
		"\5&\24\2\u010f\u0110\7v\2\2\u0110\u0111\5 \21\2\u0111\u013a\3\2\2\2\u0112"+
		"\u0113\7n\2\2\u0113\u0114\5 \21\2\u0114\u0115\7v\2\2\u0115\u013a\3\2\2"+
		"\2\u0116\u0117\7\36\2\2\u0117\u011c\5 \21\2\u0118\u0119\7|\2\2\u0119\u011b"+
		"\5 \21\2\u011a\u0118\3\2\2\2\u011b\u011e\3\2\2\2\u011c\u011a\3\2\2\2\u011c"+
		"\u011d\3\2\2\2\u011d\u011f\3\2\2\2\u011e\u011c\3\2\2\2\u011f\u0120\7,"+
		"\2\2\u0120\u013a\3\2\2\2\u0121\u0122\7\35\2\2\u0122\u0123\5\16\b\2\u0123"+
		"\u0124\7u\2\2\u0124\u012c\5 \21\2\u0125\u0126\7z\2\2\u0126\u0127\5\16"+
		"\b\2\u0127\u0128\7u\2\2\u0128\u0129\5 \21\2\u0129\u012b\3\2\2\2\u012a"+
		"\u0125\3\2\2\2\u012b\u012e\3\2\2\2\u012c\u012a\3\2\2\2\u012c\u012d\3\2"+
		"\2\2\u012d\u0131\3\2\2\2\u012e\u012c\3\2\2\2\u012f\u0130\7i\2\2\u0130"+
		"\u0132\5 \21\2\u0131\u012f\3\2\2\2\u0131\u0132\3\2\2\2\u0132\u0133\3\2"+
		"\2\2\u0133\u0134\5(\25\2\u0134\u013a\3\2\2\2\u0135\u0136\7\33\2\2\u0136"+
		"\u0137\5\34\17\2\u0137\u0138\7\61\2\2\u0138\u013a\3\2\2\2\u0139\u00f0"+
		"\3\2\2\2\u0139\u00f1\3\2\2\2\u0139\u00f4\3\2\2\2\u0139\u00f8\3\2\2\2\u0139"+
		"\u00fa\3\2\2\2\u0139\u00fb\3\2\2\2\u0139\u00fc\3\2\2\2\u0139\u00ff\3\2"+
		"\2\2\u0139\u0104\3\2\2\2\u0139\u0105\3\2\2\2\u0139\u010c\3\2\2\2\u0139"+
		"\u0112\3\2\2\2\u0139\u0116\3\2\2\2\u0139\u0121\3\2\2\2\u0139\u0135\3\2"+
		"\2\2\u013a!\3\2\2\2\u013b\u0147\7w\2\2\u013c\u0147\7K\2\2\u013d\u0147"+
		"\7o\2\2\u013e\u0147\7O\2\2\u013f\u0147\7;\2\2\u0140\u0147\7\'\2\2\u0141"+
		"\u0147\7V\2\2\u0142\u0147\7}\2\2\u0143\u0147\7(\2\2\u0144\u0147\7\3\2"+
		"\2\u0145\u0147\7&\2\2\u0146\u013b\3\2\2\2\u0146\u013c\3\2\2\2\u0146\u013d"+
		"\3\2\2\2\u0146\u013e\3\2\2\2\u0146\u013f\3\2\2\2\u0146\u0140\3\2\2\2\u0146"+
		"\u0141\3\2\2\2\u0146\u0142\3\2\2\2\u0146\u0143\3\2\2\2\u0146\u0144\3\2"+
		"\2\2\u0146\u0145\3\2\2\2\u0147#\3\2\2\2\u0148\u014f\7p\2\2\u0149\u014f"+
		"\7P\2\2\u014a\u014f\7<\2\2\u014b\u014f\7)\2\2\u014c\u014f\7W\2\2\u014d"+
		"\u014f\7~\2\2\u014e\u0148\3\2\2\2\u014e\u0149\3\2\2\2\u014e\u014a\3\2"+
		"\2\2\u014e\u014b\3\2\2\2\u014e\u014c\3\2\2\2\u014e\u014d\3\2\2\2\u014f"+
		"%\3\2\2\2\u0150\u0157\7p\2\2\u0151\u0157\7P\2\2\u0152\u0157\7<\2\2\u0153"+
		"\u0157\7)\2\2\u0154\u0157\7W\2\2\u0155\u0157\7~\2\2\u0156\u0150\3\2\2"+
		"\2\u0156\u0151\3\2\2\2\u0156\u0152\3\2\2\2\u0156\u0153\3\2\2\2\u0156\u0154"+
		"\3\2\2\2\u0156\u0155\3\2\2\2\u0157\'\3\2\2\2\u0158\u015e\7,\2\2\u0159"+
		"\u015e\7/\2\2\u015a\u015e\7-\2\2\u015b\u015e\7\60\2\2\u015c\u015e\7.\2"+
		"\2\u015d\u0158\3\2\2\2\u015d\u0159\3\2\2\2\u015d\u015a\3\2\2\2\u015d\u015b"+
		"\3\2\2\2\u015d\u015c\3\2\2\2\u015e)\3\2\2\2\u015f\u016c\7\23\2\2\u0160"+
		"\u016c\7\13\2\2\u0161\u016c\7\24\2\2\u0162\u016c\7\f\2\2\u0163\u016c\7"+
		"\22\2\2\u0164\u016c\7\20\2\2\u0165\u016c\7\n\2\2\u0166\u016c\7\b\2\2\u0167"+
		"\u016c\7\21\2\2\u0168\u016c\7\t\2\2\u0169\u016c\7\17\2\2\u016a\u016c\7"+
		"\7\2\2\u016b\u015f\3\2\2\2\u016b\u0160\3\2\2\2\u016b\u0161\3\2\2\2\u016b"+
		"\u0162\3\2\2\2\u016b\u0163\3\2\2\2\u016b\u0164\3\2\2\2\u016b\u0165\3\2"+
		"\2\2\u016b\u0166\3\2\2\2\u016b\u0167\3\2\2\2\u016b\u0168\3\2\2\2\u016b"+
		"\u0169\3\2\2\2\u016b\u016a\3\2\2\2\u016c+\3\2\2\2\u016d\u0170\7@\2\2\u016e"+
		"\u0170\7a\2\2\u016f\u016d\3\2\2\2\u016f\u016e\3\2\2\2\u0170-\3\2\2\2\u0171"+
		"\u0177\7y\2\2\u0172\u0177\7m\2\2\u0173\u0177\7k\2\2\u0174\u0177\7B\2\2"+
		"\u0175\u0177\7R\2\2\u0176\u0171\3\2\2\2\u0176\u0172\3\2\2\2\u0176\u0173"+
		"\3\2\2\2\u0176\u0174\3\2\2\2\u0176\u0175\3\2\2\2\u0177/\3\2\2\2\u0178"+
		"\u017a\5\62\32\2\u0179\u017b\7\25\2\2\u017a\u0179\3\2\2\2\u017a\u017b"+
		"\3\2\2\2\u017b\u017c\3\2\2\2\u017c\u017e\5\64\33\2\u017d\u017f\5\60\31"+
		"\2\u017e\u017d\3\2\2\2\u017e\u017f\3\2\2\2\u017f\61\3\2\2\2\u0180\u0183"+
		"\7F\2\2\u0181\u0183\7H\2\2\u0182\u0180\3\2\2\2\u0182\u0181\3\2\2\2\u0183"+
		"\63\3\2\2\2\u0184\u0185\7e\2\2\u0185\u0186\5\16\b\2\u0186\65\3\2\2\2\u0187"+
		"\u0188\79\2\2\u0188\u018a\5 \21\2\u0189\u018b\5\66\34\2\u018a\u0189\3"+
		"\2\2\2\u018a\u018b\3\2\2\2\u018b\67\3\2\2\2\u018c\u018d\7\177\2\2\u018d"+
		"\u018e\5<\37\2\u018e\u018f\5B\"\2\u018f\u019b\3\2\2\2\u0190\u0192\7\u0080"+
		"\2\2\u0191\u0190\3\2\2\2\u0191\u0192\3\2\2\2\u0192\u0194\3\2\2\2\u0193"+
		"\u0195\5:\36\2\u0194\u0193\3\2\2\2\u0195\u0196\3\2\2\2\u0196\u0194\3\2"+
		"\2\2\u0196\u0197\3\2\2\2\u0197\u0198\3\2\2\2\u0198\u0199\5D#\2\u0199\u019b"+
		"\3\2\2\2\u019a\u018c\3\2\2\2\u019a\u0191\3\2\2\2\u019b9\3\2\2\2\u019c"+
		"\u019d\7\177\2\2\u019d\u019e\5<\37\2\u019e\u019f\5@!\2\u019f;\3\2\2\2"+
		"\u01a0\u01a6\5 \21\2\u01a1\u01a6\7\"\2\2\u01a2\u01a3\5> \2\u01a3\u01a4"+
		"\5 \21\2\u01a4\u01a6\3\2\2\2\u01a5\u01a0\3\2\2\2\u01a5\u01a1\3\2\2\2\u01a5"+
		"\u01a2\3\2\2\2\u01a6=\3\2\2\2\u01a7\u01b0\7M\2\2\u01a8\u01b0\7N\2\2\u01a9"+
		"\u01b0\7s\2\2\u01aa\u01b0\7#\2\2\u01ab\u01b0\7\30\2\2\u01ac\u01b0\7t\2"+
		"\2\u01ad\u01b0\7$\2\2\u01ae\u01b0\7\31\2\2\u01af\u01a7\3\2\2\2\u01af\u01a8"+
		"\3\2\2\2\u01af\u01a9\3\2\2\2\u01af\u01aa\3\2\2\2\u01af\u01ab\3\2\2\2\u01af"+
		"\u01ac\3\2\2\2\u01af\u01ad\3\2\2\2\u01af\u01ae\3\2\2\2\u01b0?\3\2\2\2"+
		"\u01b1\u01b4\7\25\2\2\u01b2\u01b4\7\27\2\2\u01b3\u01b1\3\2\2\2\u01b3\u01b2"+
		"\3\2\2\2\u01b4A\3\2\2\2\u01b5\u01b8\7T\2\2\u01b6\u01b8\7U\2\2\u01b7\u01b5"+
		"\3\2\2\2\u01b7\u01b6\3\2\2\2\u01b8C\3\2\2\2\u01b9\u01bc\7T\2\2\u01ba\u01bc"+
		"\7S\2\2\u01bb\u01b9\3\2\2\2\u01bb\u01ba\3\2\2\2\u01bcE\3\2\2\2\u01bd\u01c1"+
		"\7R\2\2\u01be\u01c1\7T\2\2\u01bf\u01c1\7S\2\2\u01c0\u01bd\3\2\2\2\u01c0"+
		"\u01be\3\2\2\2\u01c0\u01bf\3\2\2\2\u01c1G\3\2\2\2\u01c2\u01c3\b%\1\2\u01c3"+
		"\u01c4\5\20\t\2\u01c4\u01cd\3\2\2\2\u01c5\u01c6\f\4\2\2\u01c6\u01c7\7"+
		"\r\2\2\u01c7\u01cc\5H%\5\u01c8\u01c9\f\3\2\2\u01c9\u01ca\7f\2\2\u01ca"+
		"\u01cc\5H%\4\u01cb\u01c5\3\2\2\2\u01cb\u01c8\3\2\2\2\u01cc\u01cf\3\2\2"+
		"\2\u01cd\u01cb\3\2\2\2\u01cd\u01ce\3\2\2\2\u01ceI\3\2\2\2\u01cf\u01cd"+
		"\3\2\2\2\u01d0\u01d1\5 \21\2\u01d1\u01d2\5L\'\2\u01d2\u01d7\3\2\2\2\u01d3"+
		"\u01d4\7\177\2\2\u01d4\u01d7\5L\'\2\u01d5\u01d7\7g\2\2\u01d6\u01d0\3\2"+
		"\2\2\u01d6\u01d3\3\2\2\2\u01d6\u01d5\3\2\2\2\u01d7K\3\2\2\2\u01d8\u01db"+
		"\7\26\2\2\u01d9\u01db\7*\2\2\u01da\u01d8\3\2\2\2\u01da\u01d9\3\2\2\2\u01db"+
		"M\3\2\2\2\66Yfjnqtw}\u0080\u0086\u0095\u009d\u009f\u00be\u00c0\u00c4\u00ce"+
		"\u00d2\u00da\u00de\u00e6\u00ee\u00f4\u00f8\u011c\u012c\u0131\u0139\u0146"+
		"\u014e\u0156\u015d\u016b\u016f\u0176\u017a\u017e\u0182\u018a\u0191\u0196"+
		"\u019a\u01a5\u01af\u01b3\u01b7\u01bb\u01c0\u01cb\u01cd\u01d6\u01da";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}