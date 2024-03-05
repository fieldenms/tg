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
		AVGOFDISTINCT=23, BEGIN=24, BEGINEXPR=25, BEGINYIELDEXPR=26, BETWEEN=27, 
		CASEWHEN=28, CONCAT=29, COND=30, CONDITION=31, COUNT=32, COUNTALL=33, 
		COUNTOF=34, COUNTOFDISTINCT=35, CRITCONDITION=36, DATEOF=37, DAYOF=38, 
		DAYOFWEEKOF=39, DAYS=40, DESC=41, DIV=42, END=43, ENDASBOOL=44, ENDASDECIMAL=45, 
		ENDASINT=46, ENDASSTR=47, ENDEXPR=48, ENDYIELDEXPR=49, EQ=50, EXISTS=51, 
		EXISTSALLOF=52, EXISTSANYOF=53, EXPR=54, EXTPROP=55, GE=56, GROUPBY=57, 
		GT=58, HOUROF=59, HOURS=60, IFNULL=61, ILIKE=62, ILIKEWITHCAST=63, IN=64, 
		IPARAM=65, IPARAMS=66, ISNOTNULL=67, ISNULL=68, IVAL=69, JOIN=70, LE=71, 
		LEFTJOIN=72, LIKE=73, LIKEWITHCAST=74, LOWERCASE=75, LT=76, MAXOF=77, 
		MINOF=78, MINUTEOF=79, MINUTES=80, MOD=81, MODEL=82, MODELASAGGREGATE=83, 
		MODELASENTITY=84, MODELASPRIMITIVE=85, MONTHOF=86, MONTHS=87, MULT=88, 
		NE=89, NEGATEDCONDITION=90, NOTBEGIN=91, NOTEXISTS=92, NOTEXISTSALLOF=93, 
		NOTEXISTSANYOF=94, NOTILIKE=95, NOTILIKEWITHCAST=96, NOTIN=97, NOTLIKE=98, 
		NOTLIKEWITHCAST=99, NOW=100, ON=101, OR=102, ORDER=103, ORDERBY=104, OTHERWISE=105, 
		PARAM=106, PARAMS=107, PROP=108, PROPS=109, ROUND=110, SECONDOF=111, SECONDS=112, 
		SELECT=113, SUB=114, SUMOF=115, SUMOFDISTINCT=116, THEN=117, TO=118, UPPERCASE=119, 
		VAL=120, VALUES=121, WHEN=122, WHERE=123, WITH=124, YEAROF=125, YEARS=126, 
		YIELD=127, YIELDALL=128, WHITESPACE=129, COMMENT=130, BLOCK_COMMENT=131;
	public static final int
		RULE_start = 0, RULE_query = 1, RULE_selectEnd = 2, RULE_where = 3, RULE_condition = 4, 
		RULE_predicate = 5, RULE_unaryComparisonOperator = 6, RULE_likeOperator = 7, 
		RULE_comparisonOperand = 8, RULE_comparisonOperator = 9, RULE_quantifiedOperand = 10, 
		RULE_exprBody = 11, RULE_arithmeticalOperator = 12, RULE_singleOperand = 13, 
		RULE_unaryFunctionName = 14, RULE_dateIntervalUnit = 15, RULE_caseWhenEnd = 16, 
		RULE_multiOperand = 17, RULE_membershipOperator = 18, RULE_membershipOperand = 19, 
		RULE_join = 20, RULE_joinOperator = 21, RULE_joinCondition = 22, RULE_groupBy = 23, 
		RULE_anyYield = 24, RULE_aliasedYield = 25, RULE_yieldOperand = 26, RULE_yieldOperandFunctionName = 27, 
		RULE_yieldAlias = 28, RULE_yield1Model = 29, RULE_yieldManyModel = 30, 
		RULE_model = 31, RULE_standaloneCondition = 32, RULE_orderByOperand = 33, 
		RULE_order = 34;
	private static String[] makeRuleNames() {
		return new String[] {
			"start", "query", "selectEnd", "where", "condition", "predicate", "unaryComparisonOperator", 
			"likeOperator", "comparisonOperand", "comparisonOperator", "quantifiedOperand", 
			"exprBody", "arithmeticalOperator", "singleOperand", "unaryFunctionName", 
			"dateIntervalUnit", "caseWhenEnd", "multiOperand", "membershipOperator", 
			"membershipOperand", "join", "joinOperator", "joinCondition", "groupBy", 
			"anyYield", "aliasedYield", "yieldOperand", "yieldOperandFunctionName", 
			"yieldAlias", "yield1Model", "yieldManyModel", "model", "standaloneCondition", 
			"orderByOperand", "order"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'absOf'", "'add'", "'addTimeIntervalOf'", "'all'", "'allOfExpressions'", 
			"'allOfIParams'", "'allOfModels'", "'allOfParams'", "'allOfProps'", "'allOfValues'", 
			"'and'", "'any'", "'anyOfExpressions'", "'anyOfIParams'", "'anyOfModels'", 
			"'anyOfParams'", "'anyOfProps'", "'anyOfValues'", "'as'", "'asc'", "'asRequired'", 
			"'avgOf'", "'avgOfDistinct'", "'begin'", "'beginExpr'", "'beginYieldExpr'", 
			"'between'", "'caseWhen'", "'concat'", "'cond'", "'condition'", "'count'", 
			"'countAll'", "'countOf'", "'countOfDistinct'", "'critCondition'", "'dateOf'", 
			"'dayOf'", "'dayOfWeekOf'", "'days'", "'desc'", "'div'", "'end'", "'endAsBool'", 
			"'endAsDecimal'", "'endAsInt'", "'endAsStr'", "'endExpr'", "'endYieldExpr'", 
			"'eq'", "'exists'", "'existsAllOf'", "'existsAnyOf'", "'expr'", "'extProp'", 
			"'ge'", "'groupBy'", "'gt'", "'hourOf'", "'hours'", "'ifNull'", "'iLike'", 
			"'iLikeWithCast'", "'in'", "'iParam'", "'iParams'", "'isNotNull'", "'isNull'", 
			"'iVal'", "'join'", "'le'", "'leftJoin'", "'like'", "'likeWithCast'", 
			"'lowerCase'", "'lt'", "'maxOf'", "'minOf'", "'minuteOf'", "'minutes'", 
			"'mod'", "'model'", "'modelAsAggregate'", "'modelAsEntity'", "'modelAsPrimitive'", 
			"'monthOf'", "'months'", "'mult'", "'ne'", "'negatedCondition'", "'notBegin'", 
			"'notExists'", "'notExistsAllOf'", "'notExistsAnyOf'", "'notILike'", 
			"'notILikeWithCast'", "'notIn'", "'notLike'", "'notLikeWithCast'", "'now'", 
			"'on'", "'or'", "'order'", "'orderBy'", "'otherwise'", "'param'", "'params'", 
			"'prop'", "'props'", "'round'", "'secondOf'", "'seconds'", "'select'", 
			"'sub'", "'sumOf'", "'sumOfDistinct'", "'then'", "'to'", "'upperCase'", 
			"'val'", "'values'", "'when'", "'where'", "'with'", "'yearOf'", "'years'", 
			"'yield'", "'yieldAll'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "ABSOF", "ADD", "ADDTIMEINTERVALOF", "ALL", "ALLOFEXPRESSIONS", 
			"ALLOFIPARAMS", "ALLOFMODELS", "ALLOFPARAMS", "ALLOFPROPS", "ALLOFVALUES", 
			"AND", "ANY", "ANYOFEXPRESSIONS", "ANYOFIPARAMS", "ANYOFMODELS", "ANYOFPARAMS", 
			"ANYOFPROPS", "ANYOFVALUES", "AS", "ASC", "ASREQUIRED", "AVGOF", "AVGOFDISTINCT", 
			"BEGIN", "BEGINEXPR", "BEGINYIELDEXPR", "BETWEEN", "CASEWHEN", "CONCAT", 
			"COND", "CONDITION", "COUNT", "COUNTALL", "COUNTOF", "COUNTOFDISTINCT", 
			"CRITCONDITION", "DATEOF", "DAYOF", "DAYOFWEEKOF", "DAYS", "DESC", "DIV", 
			"END", "ENDASBOOL", "ENDASDECIMAL", "ENDASINT", "ENDASSTR", "ENDEXPR", 
			"ENDYIELDEXPR", "EQ", "EXISTS", "EXISTSALLOF", "EXISTSANYOF", "EXPR", 
			"EXTPROP", "GE", "GROUPBY", "GT", "HOUROF", "HOURS", "IFNULL", "ILIKE", 
			"ILIKEWITHCAST", "IN", "IPARAM", "IPARAMS", "ISNOTNULL", "ISNULL", "IVAL", 
			"JOIN", "LE", "LEFTJOIN", "LIKE", "LIKEWITHCAST", "LOWERCASE", "LT", 
			"MAXOF", "MINOF", "MINUTEOF", "MINUTES", "MOD", "MODEL", "MODELASAGGREGATE", 
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
			setState(70);
			query();
			setState(71);
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
		public OrderByOperandContext orderByOperand;
		public List<OrderByOperandContext> operands = new ArrayList<OrderByOperandContext>();
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
		public YieldOperandContext first;
		public ArithmeticalOperatorContext arithmeticalOperator;
		public List<ArithmeticalOperatorContext> operators = new ArrayList<ArithmeticalOperatorContext>();
		public YieldOperandContext yieldOperand;
		public List<YieldOperandContext> rest = new ArrayList<YieldOperandContext>();
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
	public static class SelectContext extends QueryContext {
		public Token select;
		public Token alias;
		public SelectEndContext selectEnd() {
			return getRuleContext(SelectEndContext.class,0);
		}
		public TerminalNode SELECT() { return getToken(EQLParser.SELECT, 0); }
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
		public SelectContext(QueryContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitSelect(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QueryContext query() throws RecognitionException {
		QueryContext _localctx = new QueryContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_query);
		int _la;
		try {
			int _alt;
			setState(111);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SELECT:
				_localctx = new SelectContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(73);
				((SelectContext)_localctx).select = match(SELECT);
				setState(75);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(74);
					((SelectContext)_localctx).alias = match(AS);
					}
				}

				setState(78);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==JOIN || _la==LEFTJOIN) {
					{
					setState(77);
					join();
					}
				}

				setState(81);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WHERE) {
					{
					setState(80);
					where();
					}
				}

				setState(84);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==GROUPBY) {
					{
					setState(83);
					groupBy();
					}
				}

				setState(86);
				selectEnd();
				}
				break;
			case EXPR:
				_localctx = new StandaloneExpressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(87);
				match(EXPR);
				setState(88);
				((StandaloneExpressionContext)_localctx).first = yieldOperand();
				setState(94);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ADD || _la==DIV || ((((_la - 81)) & ~0x3f) == 0 && ((1L << (_la - 81)) & ((1L << (MOD - 81)) | (1L << (MULT - 81)) | (1L << (SUB - 81)))) != 0)) {
					{
					{
					setState(89);
					((StandaloneExpressionContext)_localctx).arithmeticalOperator = arithmeticalOperator();
					((StandaloneExpressionContext)_localctx).operators.add(((StandaloneExpressionContext)_localctx).arithmeticalOperator);
					setState(90);
					((StandaloneExpressionContext)_localctx).yieldOperand = yieldOperand();
					((StandaloneExpressionContext)_localctx).rest.add(((StandaloneExpressionContext)_localctx).yieldOperand);
					}
					}
					setState(96);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(97);
				match(MODEL);
				}
				break;
			case COND:
				_localctx = new StandaloneCondExprContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(99);
				match(COND);
				setState(100);
				standaloneCondition(0);
				setState(101);
				match(MODEL);
				}
				break;
			case ORDERBY:
				_localctx = new OrderByContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(103);
				match(ORDERBY);
				setState(105); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(104);
						((OrderByContext)_localctx).orderByOperand = orderByOperand();
						((OrderByContext)_localctx).operands.add(((OrderByContext)_localctx).orderByOperand);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(107); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(109);
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
		enterRule(_localctx, 4, RULE_selectEnd);
		try {
			setState(115);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				_localctx = new SelectEnd_ModelContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(113);
				model();
				}
				break;
			case 2:
				_localctx = new SelectEnd_AnyYieldContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(114);
				anyYield();
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
		enterRule(_localctx, 6, RULE_where);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			match(WHERE);
			setState(118);
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
		int _startState = 8;
		enterRecursionRule(_localctx, 8, RULE_condition, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(130);
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

				setState(121);
				predicate();
				}
				break;
			case BEGIN:
				{
				_localctx = new CompoundConditionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(122);
				match(BEGIN);
				setState(123);
				condition(0);
				setState(124);
				match(END);
				}
				break;
			case NOTBEGIN:
				{
				_localctx = new NegatedCompoundConditionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(126);
				match(NOTBEGIN);
				setState(127);
				condition(0);
				setState(128);
				match(END);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(140);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(138);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
					case 1:
						{
						_localctx = new AndConditionContext(new ConditionContext(_parentctx, _parentState));
						((AndConditionContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_condition);
						setState(132);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(133);
						match(AND);
						setState(134);
						((AndConditionContext)_localctx).right = condition(5);
						}
						break;
					case 2:
						{
						_localctx = new OrConditionContext(new ConditionContext(_parentctx, _parentState));
						((OrConditionContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_condition);
						setState(135);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(136);
						match(OR);
						setState(137);
						((OrConditionContext)_localctx).right = condition(4);
						}
						break;
					}
					} 
				}
				setState(142);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
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
		enterRule(_localctx, 10, RULE_predicate);
		try {
			setState(173);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				_localctx = new UnaryPredicateContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(143);
				((UnaryPredicateContext)_localctx).left = comparisonOperand();
				setState(144);
				unaryComparisonOperator();
				}
				break;
			case 2:
				_localctx = new ComparisonPredicateContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(146);
				((ComparisonPredicateContext)_localctx).left = comparisonOperand();
				setState(147);
				comparisonOperator();
				setState(148);
				((ComparisonPredicateContext)_localctx).right = comparisonOperand();
				}
				break;
			case 3:
				_localctx = new QuantifiedComparisonPredicateContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(150);
				((QuantifiedComparisonPredicateContext)_localctx).left = comparisonOperand();
				setState(151);
				comparisonOperator();
				setState(152);
				quantifiedOperand();
				}
				break;
			case 4:
				_localctx = new LikePredicateContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(154);
				((LikePredicateContext)_localctx).left = comparisonOperand();
				setState(155);
				likeOperator();
				setState(156);
				((LikePredicateContext)_localctx).right = comparisonOperand();
				}
				break;
			case 5:
				_localctx = new MembershipPredicateContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(158);
				((MembershipPredicateContext)_localctx).left = comparisonOperand();
				setState(159);
				membershipOperator();
				setState(160);
				membershipOperand();
				}
				break;
			case 6:
				_localctx = new SingleConditionPredicateContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(171);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case EXISTS:
					{
					setState(162);
					((SingleConditionPredicateContext)_localctx).token = match(EXISTS);
					}
					break;
				case NOTEXISTS:
					{
					setState(163);
					((SingleConditionPredicateContext)_localctx).token = match(NOTEXISTS);
					}
					break;
				case EXISTSANYOF:
					{
					setState(164);
					((SingleConditionPredicateContext)_localctx).token = match(EXISTSANYOF);
					}
					break;
				case NOTEXISTSANYOF:
					{
					setState(165);
					((SingleConditionPredicateContext)_localctx).token = match(NOTEXISTSANYOF);
					}
					break;
				case EXISTSALLOF:
					{
					setState(166);
					((SingleConditionPredicateContext)_localctx).token = match(EXISTSALLOF);
					}
					break;
				case NOTEXISTSALLOF:
					{
					setState(167);
					((SingleConditionPredicateContext)_localctx).token = match(NOTEXISTSALLOF);
					}
					break;
				case CRITCONDITION:
					{
					setState(168);
					((SingleConditionPredicateContext)_localctx).token = match(CRITCONDITION);
					}
					break;
				case CONDITION:
					{
					setState(169);
					((SingleConditionPredicateContext)_localctx).token = match(CONDITION);
					}
					break;
				case NEGATEDCONDITION:
					{
					setState(170);
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
		enterRule(_localctx, 12, RULE_unaryComparisonOperator);
		try {
			setState(177);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ISNULL:
				enterOuterAlt(_localctx, 1);
				{
				setState(175);
				((UnaryComparisonOperatorContext)_localctx).token = match(ISNULL);
				}
				break;
			case ISNOTNULL:
				enterOuterAlt(_localctx, 2);
				{
				setState(176);
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
		enterRule(_localctx, 14, RULE_likeOperator);
		try {
			setState(187);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LIKE:
				enterOuterAlt(_localctx, 1);
				{
				setState(179);
				((LikeOperatorContext)_localctx).token = match(LIKE);
				}
				break;
			case ILIKE:
				enterOuterAlt(_localctx, 2);
				{
				setState(180);
				((LikeOperatorContext)_localctx).token = match(ILIKE);
				}
				break;
			case LIKEWITHCAST:
				enterOuterAlt(_localctx, 3);
				{
				setState(181);
				((LikeOperatorContext)_localctx).token = match(LIKEWITHCAST);
				}
				break;
			case ILIKEWITHCAST:
				enterOuterAlt(_localctx, 4);
				{
				setState(182);
				((LikeOperatorContext)_localctx).token = match(ILIKEWITHCAST);
				}
				break;
			case NOTLIKE:
				enterOuterAlt(_localctx, 5);
				{
				setState(183);
				((LikeOperatorContext)_localctx).token = match(NOTLIKE);
				}
				break;
			case NOTLIKEWITHCAST:
				enterOuterAlt(_localctx, 6);
				{
				setState(184);
				((LikeOperatorContext)_localctx).token = match(NOTLIKEWITHCAST);
				}
				break;
			case NOTILIKEWITHCAST:
				enterOuterAlt(_localctx, 7);
				{
				setState(185);
				((LikeOperatorContext)_localctx).token = match(NOTILIKEWITHCAST);
				}
				break;
			case NOTILIKE:
				enterOuterAlt(_localctx, 8);
				{
				setState(186);
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
		enterRule(_localctx, 16, RULE_comparisonOperand);
		try {
			setState(191);
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
				setState(189);
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
				setState(190);
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
		enterRule(_localctx, 18, RULE_comparisonOperator);
		try {
			setState(199);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EQ:
				enterOuterAlt(_localctx, 1);
				{
				setState(193);
				((ComparisonOperatorContext)_localctx).token = match(EQ);
				}
				break;
			case GT:
				enterOuterAlt(_localctx, 2);
				{
				setState(194);
				((ComparisonOperatorContext)_localctx).token = match(GT);
				}
				break;
			case LT:
				enterOuterAlt(_localctx, 3);
				{
				setState(195);
				((ComparisonOperatorContext)_localctx).token = match(LT);
				}
				break;
			case GE:
				enterOuterAlt(_localctx, 4);
				{
				setState(196);
				((ComparisonOperatorContext)_localctx).token = match(GE);
				}
				break;
			case LE:
				enterOuterAlt(_localctx, 5);
				{
				setState(197);
				((ComparisonOperatorContext)_localctx).token = match(LE);
				}
				break;
			case NE:
				enterOuterAlt(_localctx, 6);
				{
				setState(198);
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
		enterRule(_localctx, 20, RULE_quantifiedOperand);
		try {
			setState(203);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ALL:
				enterOuterAlt(_localctx, 1);
				{
				setState(201);
				((QuantifiedOperandContext)_localctx).token = match(ALL);
				}
				break;
			case ANY:
				enterOuterAlt(_localctx, 2);
				{
				setState(202);
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
		public SingleOperandContext first;
		public ArithmeticalOperatorContext arithmeticalOperator;
		public List<ArithmeticalOperatorContext> operators = new ArrayList<ArithmeticalOperatorContext>();
		public SingleOperandContext singleOperand;
		public List<SingleOperandContext> rest = new ArrayList<SingleOperandContext>();
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
		enterRule(_localctx, 22, RULE_exprBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(205);
			((ExprBodyContext)_localctx).first = singleOperand();
			setState(211);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ADD || _la==DIV || ((((_la - 81)) & ~0x3f) == 0 && ((1L << (_la - 81)) & ((1L << (MOD - 81)) | (1L << (MULT - 81)) | (1L << (SUB - 81)))) != 0)) {
				{
				{
				setState(206);
				((ExprBodyContext)_localctx).arithmeticalOperator = arithmeticalOperator();
				((ExprBodyContext)_localctx).operators.add(((ExprBodyContext)_localctx).arithmeticalOperator);
				setState(207);
				((ExprBodyContext)_localctx).singleOperand = singleOperand();
				((ExprBodyContext)_localctx).rest.add(((ExprBodyContext)_localctx).singleOperand);
				}
				}
				setState(213);
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
		enterRule(_localctx, 24, RULE_arithmeticalOperator);
		try {
			setState(219);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ADD:
				enterOuterAlt(_localctx, 1);
				{
				setState(214);
				((ArithmeticalOperatorContext)_localctx).token = match(ADD);
				}
				break;
			case SUB:
				enterOuterAlt(_localctx, 2);
				{
				setState(215);
				((ArithmeticalOperatorContext)_localctx).token = match(SUB);
				}
				break;
			case DIV:
				enterOuterAlt(_localctx, 3);
				{
				setState(216);
				((ArithmeticalOperatorContext)_localctx).token = match(DIV);
				}
				break;
			case MULT:
				enterOuterAlt(_localctx, 4);
				{
				setState(217);
				((ArithmeticalOperatorContext)_localctx).token = match(MULT);
				}
				break;
			case MOD:
				enterOuterAlt(_localctx, 5);
				{
				setState(218);
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
		public DateIntervalUnitContext unit;
		public SingleOperandContext right;
		public TerminalNode ADDTIMEINTERVALOF() { return getToken(EQLParser.ADDTIMEINTERVALOF, 0); }
		public TerminalNode TO() { return getToken(EQLParser.TO, 0); }
		public List<SingleOperandContext> singleOperand() {
			return getRuleContexts(SingleOperandContext.class);
		}
		public SingleOperandContext singleOperand(int i) {
			return getRuleContext(SingleOperandContext.class,i);
		}
		public DateIntervalUnitContext dateIntervalUnit() {
			return getRuleContext(DateIntervalUnitContext.class,0);
		}
		public DateAddIntervalContext(SingleOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitDateAddInterval(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DateDiffIntervalContext extends SingleOperandContext {
		public DateIntervalUnitContext unit;
		public SingleOperandContext endDate;
		public SingleOperandContext startDate;
		public TerminalNode COUNT() { return getToken(EQLParser.COUNT, 0); }
		public TerminalNode BETWEEN() { return getToken(EQLParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(EQLParser.AND, 0); }
		public DateIntervalUnitContext dateIntervalUnit() {
			return getRuleContext(DateIntervalUnitContext.class,0);
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
		public ConditionContext condition;
		public List<ConditionContext> whens = new ArrayList<ConditionContext>();
		public SingleOperandContext singleOperand;
		public List<SingleOperandContext> thens = new ArrayList<SingleOperandContext>();
		public SingleOperandContext otherwiseOperand;
		public TerminalNode CASEWHEN() { return getToken(EQLParser.CASEWHEN, 0); }
		public List<TerminalNode> THEN() { return getTokens(EQLParser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(EQLParser.THEN, i);
		}
		public CaseWhenEndContext caseWhenEnd() {
			return getRuleContext(CaseWhenEndContext.class,0);
		}
		public List<ConditionContext> condition() {
			return getRuleContexts(ConditionContext.class);
		}
		public ConditionContext condition(int i) {
			return getRuleContext(ConditionContext.class,i);
		}
		public List<SingleOperandContext> singleOperand() {
			return getRuleContexts(SingleOperandContext.class);
		}
		public SingleOperandContext singleOperand(int i) {
			return getRuleContext(SingleOperandContext.class,i);
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
		public SingleOperandContext singleOperand;
		public List<SingleOperandContext> operands = new ArrayList<SingleOperandContext>();
		public TerminalNode CONCAT() { return getToken(EQLParser.CONCAT, 0); }
		public TerminalNode END() { return getToken(EQLParser.END, 0); }
		public List<SingleOperandContext> singleOperand() {
			return getRuleContexts(SingleOperandContext.class);
		}
		public SingleOperandContext singleOperand(int i) {
			return getRuleContext(SingleOperandContext.class,i);
		}
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
		public Token to;
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
		enterRule(_localctx, 26, RULE_singleOperand);
		int _la;
		try {
			setState(294);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PROP:
				_localctx = new PropContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(221);
				((PropContext)_localctx).token = match(PROP);
				}
				break;
			case EXTPROP:
				_localctx = new ExtPropContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(222);
				((ExtPropContext)_localctx).token = match(EXTPROP);
				}
				break;
			case IVAL:
			case VAL:
				_localctx = new ValContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(225);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case VAL:
					{
					setState(223);
					((ValContext)_localctx).token = match(VAL);
					}
					break;
				case IVAL:
					{
					setState(224);
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
				setState(229);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case PARAM:
					{
					setState(227);
					((ParamContext)_localctx).token = match(PARAM);
					}
					break;
				case IPARAM:
					{
					setState(228);
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
				setState(231);
				((SingleOperand_ExprContext)_localctx).token = match(EXPR);
				}
				break;
			case MODEL:
				_localctx = new SingleOperand_ModelContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(232);
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
				setState(233);
				((UnaryFunctionContext)_localctx).funcName = unaryFunctionName();
				setState(234);
				((UnaryFunctionContext)_localctx).argument = singleOperand();
				}
				break;
			case IFNULL:
				_localctx = new IfNullContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(236);
				match(IFNULL);
				setState(237);
				((IfNullContext)_localctx).nullable = singleOperand();
				setState(238);
				match(THEN);
				setState(239);
				((IfNullContext)_localctx).other = singleOperand();
				}
				break;
			case NOW:
				_localctx = new SingleOperand_NowContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(241);
				match(NOW);
				}
				break;
			case COUNT:
				_localctx = new DateDiffIntervalContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(242);
				match(COUNT);
				setState(243);
				((DateDiffIntervalContext)_localctx).unit = dateIntervalUnit();
				setState(244);
				match(BETWEEN);
				setState(245);
				((DateDiffIntervalContext)_localctx).endDate = singleOperand();
				setState(246);
				match(AND);
				setState(247);
				((DateDiffIntervalContext)_localctx).startDate = singleOperand();
				}
				break;
			case ADDTIMEINTERVALOF:
				_localctx = new DateAddIntervalContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(249);
				match(ADDTIMEINTERVALOF);
				setState(250);
				((DateAddIntervalContext)_localctx).left = singleOperand();
				setState(251);
				((DateAddIntervalContext)_localctx).unit = dateIntervalUnit();
				setState(252);
				match(TO);
				setState(253);
				((DateAddIntervalContext)_localctx).right = singleOperand();
				}
				break;
			case ROUND:
				_localctx = new RoundContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(255);
				match(ROUND);
				setState(256);
				singleOperand();
				setState(257);
				((RoundContext)_localctx).to = match(TO);
				}
				break;
			case CONCAT:
				_localctx = new ConcatContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(259);
				match(CONCAT);
				setState(260);
				((ConcatContext)_localctx).singleOperand = singleOperand();
				((ConcatContext)_localctx).operands.add(((ConcatContext)_localctx).singleOperand);
				setState(265);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WITH) {
					{
					{
					setState(261);
					match(WITH);
					setState(262);
					((ConcatContext)_localctx).singleOperand = singleOperand();
					((ConcatContext)_localctx).operands.add(((ConcatContext)_localctx).singleOperand);
					}
					}
					setState(267);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(268);
				match(END);
				}
				break;
			case CASEWHEN:
				_localctx = new CaseWhenContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(270);
				match(CASEWHEN);
				setState(271);
				((CaseWhenContext)_localctx).condition = condition(0);
				((CaseWhenContext)_localctx).whens.add(((CaseWhenContext)_localctx).condition);
				setState(272);
				match(THEN);
				setState(273);
				((CaseWhenContext)_localctx).singleOperand = singleOperand();
				((CaseWhenContext)_localctx).thens.add(((CaseWhenContext)_localctx).singleOperand);
				setState(281);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WHEN) {
					{
					{
					setState(274);
					match(WHEN);
					setState(275);
					((CaseWhenContext)_localctx).condition = condition(0);
					((CaseWhenContext)_localctx).whens.add(((CaseWhenContext)_localctx).condition);
					setState(276);
					match(THEN);
					setState(277);
					((CaseWhenContext)_localctx).singleOperand = singleOperand();
					((CaseWhenContext)_localctx).thens.add(((CaseWhenContext)_localctx).singleOperand);
					}
					}
					setState(283);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(286);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OTHERWISE) {
					{
					setState(284);
					match(OTHERWISE);
					setState(285);
					((CaseWhenContext)_localctx).otherwiseOperand = singleOperand();
					}
				}

				setState(288);
				caseWhenEnd();
				}
				break;
			case BEGINEXPR:
				_localctx = new ExprContext(_localctx);
				enterOuterAlt(_localctx, 15);
				{
				setState(290);
				match(BEGINEXPR);
				setState(291);
				exprBody();
				setState(292);
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
		enterRule(_localctx, 28, RULE_unaryFunctionName);
		try {
			setState(307);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case UPPERCASE:
				enterOuterAlt(_localctx, 1);
				{
				setState(296);
				((UnaryFunctionNameContext)_localctx).token = match(UPPERCASE);
				}
				break;
			case LOWERCASE:
				enterOuterAlt(_localctx, 2);
				{
				setState(297);
				((UnaryFunctionNameContext)_localctx).token = match(LOWERCASE);
				}
				break;
			case SECONDOF:
				enterOuterAlt(_localctx, 3);
				{
				setState(298);
				((UnaryFunctionNameContext)_localctx).token = match(SECONDOF);
				}
				break;
			case MINUTEOF:
				enterOuterAlt(_localctx, 4);
				{
				setState(299);
				((UnaryFunctionNameContext)_localctx).token = match(MINUTEOF);
				}
				break;
			case HOUROF:
				enterOuterAlt(_localctx, 5);
				{
				setState(300);
				((UnaryFunctionNameContext)_localctx).token = match(HOUROF);
				}
				break;
			case DAYOF:
				enterOuterAlt(_localctx, 6);
				{
				setState(301);
				((UnaryFunctionNameContext)_localctx).token = match(DAYOF);
				}
				break;
			case MONTHOF:
				enterOuterAlt(_localctx, 7);
				{
				setState(302);
				((UnaryFunctionNameContext)_localctx).token = match(MONTHOF);
				}
				break;
			case YEAROF:
				enterOuterAlt(_localctx, 8);
				{
				setState(303);
				((UnaryFunctionNameContext)_localctx).token = match(YEAROF);
				}
				break;
			case DAYOFWEEKOF:
				enterOuterAlt(_localctx, 9);
				{
				setState(304);
				((UnaryFunctionNameContext)_localctx).token = match(DAYOFWEEKOF);
				}
				break;
			case ABSOF:
				enterOuterAlt(_localctx, 10);
				{
				setState(305);
				((UnaryFunctionNameContext)_localctx).token = match(ABSOF);
				}
				break;
			case DATEOF:
				enterOuterAlt(_localctx, 11);
				{
				setState(306);
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

	public static class DateIntervalUnitContext extends ParserRuleContext {
		public Token token;
		public TerminalNode SECONDS() { return getToken(EQLParser.SECONDS, 0); }
		public TerminalNode MINUTES() { return getToken(EQLParser.MINUTES, 0); }
		public TerminalNode HOURS() { return getToken(EQLParser.HOURS, 0); }
		public TerminalNode DAYS() { return getToken(EQLParser.DAYS, 0); }
		public TerminalNode MONTHS() { return getToken(EQLParser.MONTHS, 0); }
		public TerminalNode YEARS() { return getToken(EQLParser.YEARS, 0); }
		public DateIntervalUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dateIntervalUnit; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitDateIntervalUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DateIntervalUnitContext dateIntervalUnit() throws RecognitionException {
		DateIntervalUnitContext _localctx = new DateIntervalUnitContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_dateIntervalUnit);
		try {
			setState(315);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SECONDS:
				enterOuterAlt(_localctx, 1);
				{
				setState(309);
				((DateIntervalUnitContext)_localctx).token = match(SECONDS);
				}
				break;
			case MINUTES:
				enterOuterAlt(_localctx, 2);
				{
				setState(310);
				((DateIntervalUnitContext)_localctx).token = match(MINUTES);
				}
				break;
			case HOURS:
				enterOuterAlt(_localctx, 3);
				{
				setState(311);
				((DateIntervalUnitContext)_localctx).token = match(HOURS);
				}
				break;
			case DAYS:
				enterOuterAlt(_localctx, 4);
				{
				setState(312);
				((DateIntervalUnitContext)_localctx).token = match(DAYS);
				}
				break;
			case MONTHS:
				enterOuterAlt(_localctx, 5);
				{
				setState(313);
				((DateIntervalUnitContext)_localctx).token = match(MONTHS);
				}
				break;
			case YEARS:
				enterOuterAlt(_localctx, 6);
				{
				setState(314);
				((DateIntervalUnitContext)_localctx).token = match(YEARS);
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
		enterRule(_localctx, 32, RULE_caseWhenEnd);
		try {
			setState(322);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case END:
				enterOuterAlt(_localctx, 1);
				{
				setState(317);
				((CaseWhenEndContext)_localctx).token = match(END);
				}
				break;
			case ENDASINT:
				enterOuterAlt(_localctx, 2);
				{
				setState(318);
				((CaseWhenEndContext)_localctx).token = match(ENDASINT);
				}
				break;
			case ENDASBOOL:
				enterOuterAlt(_localctx, 3);
				{
				setState(319);
				((CaseWhenEndContext)_localctx).token = match(ENDASBOOL);
				}
				break;
			case ENDASSTR:
				enterOuterAlt(_localctx, 4);
				{
				setState(320);
				((CaseWhenEndContext)_localctx).token = match(ENDASSTR);
				}
				break;
			case ENDASDECIMAL:
				enterOuterAlt(_localctx, 5);
				{
				setState(321);
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
		enterRule(_localctx, 34, RULE_multiOperand);
		try {
			setState(336);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ANYOFPROPS:
				enterOuterAlt(_localctx, 1);
				{
				setState(324);
				((MultiOperandContext)_localctx).token = match(ANYOFPROPS);
				}
				break;
			case ALLOFPROPS:
				enterOuterAlt(_localctx, 2);
				{
				setState(325);
				((MultiOperandContext)_localctx).token = match(ALLOFPROPS);
				}
				break;
			case ANYOFVALUES:
				enterOuterAlt(_localctx, 3);
				{
				setState(326);
				((MultiOperandContext)_localctx).token = match(ANYOFVALUES);
				}
				break;
			case ALLOFVALUES:
				enterOuterAlt(_localctx, 4);
				{
				setState(327);
				((MultiOperandContext)_localctx).token = match(ALLOFVALUES);
				}
				break;
			case ANYOFPARAMS:
				enterOuterAlt(_localctx, 5);
				{
				setState(328);
				((MultiOperandContext)_localctx).token = match(ANYOFPARAMS);
				}
				break;
			case ANYOFIPARAMS:
				enterOuterAlt(_localctx, 6);
				{
				setState(329);
				((MultiOperandContext)_localctx).token = match(ANYOFIPARAMS);
				}
				break;
			case ALLOFPARAMS:
				enterOuterAlt(_localctx, 7);
				{
				setState(330);
				((MultiOperandContext)_localctx).token = match(ALLOFPARAMS);
				}
				break;
			case ALLOFIPARAMS:
				enterOuterAlt(_localctx, 8);
				{
				setState(331);
				((MultiOperandContext)_localctx).token = match(ALLOFIPARAMS);
				}
				break;
			case ANYOFMODELS:
				enterOuterAlt(_localctx, 9);
				{
				setState(332);
				((MultiOperandContext)_localctx).token = match(ANYOFMODELS);
				}
				break;
			case ALLOFMODELS:
				enterOuterAlt(_localctx, 10);
				{
				setState(333);
				((MultiOperandContext)_localctx).token = match(ALLOFMODELS);
				}
				break;
			case ANYOFEXPRESSIONS:
				enterOuterAlt(_localctx, 11);
				{
				setState(334);
				((MultiOperandContext)_localctx).token = match(ANYOFEXPRESSIONS);
				}
				break;
			case ALLOFEXPRESSIONS:
				enterOuterAlt(_localctx, 12);
				{
				setState(335);
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
		enterRule(_localctx, 36, RULE_membershipOperator);
		try {
			setState(340);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IN:
				enterOuterAlt(_localctx, 1);
				{
				setState(338);
				((MembershipOperatorContext)_localctx).token = match(IN);
				}
				break;
			case NOTIN:
				enterOuterAlt(_localctx, 2);
				{
				setState(339);
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
		enterRule(_localctx, 38, RULE_membershipOperand);
		try {
			setState(347);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VALUES:
				enterOuterAlt(_localctx, 1);
				{
				setState(342);
				((MembershipOperandContext)_localctx).token = match(VALUES);
				}
				break;
			case PROPS:
				enterOuterAlt(_localctx, 2);
				{
				setState(343);
				((MembershipOperandContext)_localctx).token = match(PROPS);
				}
				break;
			case PARAMS:
				enterOuterAlt(_localctx, 3);
				{
				setState(344);
				((MembershipOperandContext)_localctx).token = match(PARAMS);
				}
				break;
			case IPARAMS:
				enterOuterAlt(_localctx, 4);
				{
				setState(345);
				((MembershipOperandContext)_localctx).token = match(IPARAMS);
				}
				break;
			case MODEL:
				enterOuterAlt(_localctx, 5);
				{
				setState(346);
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
		enterRule(_localctx, 40, RULE_join);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(349);
			joinOperator();
			setState(351);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(350);
				((JoinContext)_localctx).alias = match(AS);
				}
			}

			setState(353);
			joinCondition();
			setState(355);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==JOIN || _la==LEFTJOIN) {
				{
				setState(354);
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
		enterRule(_localctx, 42, RULE_joinOperator);
		try {
			setState(359);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case JOIN:
				enterOuterAlt(_localctx, 1);
				{
				setState(357);
				((JoinOperatorContext)_localctx).token = match(JOIN);
				}
				break;
			case LEFTJOIN:
				enterOuterAlt(_localctx, 2);
				{
				setState(358);
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
		enterRule(_localctx, 44, RULE_joinCondition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(361);
			match(ON);
			setState(362);
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
		public SingleOperandContext singleOperand;
		public List<SingleOperandContext> operands = new ArrayList<SingleOperandContext>();
		public List<TerminalNode> GROUPBY() { return getTokens(EQLParser.GROUPBY); }
		public TerminalNode GROUPBY(int i) {
			return getToken(EQLParser.GROUPBY, i);
		}
		public List<SingleOperandContext> singleOperand() {
			return getRuleContexts(SingleOperandContext.class);
		}
		public SingleOperandContext singleOperand(int i) {
			return getRuleContext(SingleOperandContext.class,i);
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
		enterRule(_localctx, 46, RULE_groupBy);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(366); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(364);
				match(GROUPBY);
				setState(365);
				((GroupByContext)_localctx).singleOperand = singleOperand();
				((GroupByContext)_localctx).operands.add(((GroupByContext)_localctx).singleOperand);
				}
				}
				setState(368); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==GROUPBY );
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
		public TerminalNode YIELD() { return getToken(EQLParser.YIELD, 0); }
		public Yield1ModelContext yield1Model() {
			return getRuleContext(Yield1ModelContext.class,0);
		}
		public YieldOperandContext yieldOperand() {
			return getRuleContext(YieldOperandContext.class,0);
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
		enterRule(_localctx, 48, RULE_anyYield);
		int _la;
		try {
			setState(384);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
			case 1:
				_localctx = new Yield1Context(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(370);
				match(YIELD);
				setState(371);
				((Yield1Context)_localctx).operand = yieldOperand();
				setState(372);
				yield1Model();
				}
				break;
			case 2:
				_localctx = new YieldManyContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(375);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==YIELDALL) {
					{
					setState(374);
					match(YIELDALL);
					}
				}

				setState(380);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==YIELD) {
					{
					{
					setState(377);
					aliasedYield();
					}
					}
					setState(382);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(383);
				yieldManyModel();
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
		enterRule(_localctx, 50, RULE_aliasedYield);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(386);
			match(YIELD);
			setState(387);
			((AliasedYieldContext)_localctx).operand = yieldOperand();
			setState(388);
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
	public static class YieldOperand_SingleOperandContext extends YieldOperandContext {
		public SingleOperandContext singleOperand() {
			return getRuleContext(SingleOperandContext.class,0);
		}
		public YieldOperand_SingleOperandContext(YieldOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitYieldOperand_SingleOperand(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class YieldOperandExprContext extends YieldOperandContext {
		public YieldOperandContext first;
		public ArithmeticalOperatorContext arithmeticalOperator;
		public List<ArithmeticalOperatorContext> operators = new ArrayList<ArithmeticalOperatorContext>();
		public YieldOperandContext yieldOperand;
		public List<YieldOperandContext> rest = new ArrayList<YieldOperandContext>();
		public TerminalNode BEGINYIELDEXPR() { return getToken(EQLParser.BEGINYIELDEXPR, 0); }
		public TerminalNode ENDYIELDEXPR() { return getToken(EQLParser.ENDYIELDEXPR, 0); }
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
		public YieldOperandExprContext(YieldOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitYieldOperandExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final YieldOperandContext yieldOperand() throws RecognitionException {
		YieldOperandContext _localctx = new YieldOperandContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_yieldOperand);
		int _la;
		try {
			setState(407);
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
				_localctx = new YieldOperand_SingleOperandContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(390);
				singleOperand();
				}
				break;
			case BEGINYIELDEXPR:
				_localctx = new YieldOperandExprContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(391);
				match(BEGINYIELDEXPR);
				setState(392);
				((YieldOperandExprContext)_localctx).first = yieldOperand();
				setState(398);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ADD || _la==DIV || ((((_la - 81)) & ~0x3f) == 0 && ((1L << (_la - 81)) & ((1L << (MOD - 81)) | (1L << (MULT - 81)) | (1L << (SUB - 81)))) != 0)) {
					{
					{
					setState(393);
					((YieldOperandExprContext)_localctx).arithmeticalOperator = arithmeticalOperator();
					((YieldOperandExprContext)_localctx).operators.add(((YieldOperandExprContext)_localctx).arithmeticalOperator);
					setState(394);
					((YieldOperandExprContext)_localctx).yieldOperand = yieldOperand();
					((YieldOperandExprContext)_localctx).rest.add(((YieldOperandExprContext)_localctx).yieldOperand);
					}
					}
					setState(400);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(401);
				match(ENDYIELDEXPR);
				}
				break;
			case COUNTALL:
				_localctx = new YieldOperand_CountAllContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(403);
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
				enterOuterAlt(_localctx, 4);
				{
				setState(404);
				((YieldOperandFunctionContext)_localctx).funcName = yieldOperandFunctionName();
				setState(405);
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
		enterRule(_localctx, 54, RULE_yieldOperandFunctionName);
		try {
			setState(417);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MAXOF:
				enterOuterAlt(_localctx, 1);
				{
				setState(409);
				((YieldOperandFunctionNameContext)_localctx).token = match(MAXOF);
				}
				break;
			case MINOF:
				enterOuterAlt(_localctx, 2);
				{
				setState(410);
				((YieldOperandFunctionNameContext)_localctx).token = match(MINOF);
				}
				break;
			case SUMOF:
				enterOuterAlt(_localctx, 3);
				{
				setState(411);
				((YieldOperandFunctionNameContext)_localctx).token = match(SUMOF);
				}
				break;
			case COUNTOF:
				enterOuterAlt(_localctx, 4);
				{
				setState(412);
				((YieldOperandFunctionNameContext)_localctx).token = match(COUNTOF);
				}
				break;
			case AVGOF:
				enterOuterAlt(_localctx, 5);
				{
				setState(413);
				((YieldOperandFunctionNameContext)_localctx).token = match(AVGOF);
				}
				break;
			case SUMOFDISTINCT:
				enterOuterAlt(_localctx, 6);
				{
				setState(414);
				((YieldOperandFunctionNameContext)_localctx).token = match(SUMOFDISTINCT);
				}
				break;
			case COUNTOFDISTINCT:
				enterOuterAlt(_localctx, 7);
				{
				setState(415);
				((YieldOperandFunctionNameContext)_localctx).token = match(COUNTOFDISTINCT);
				}
				break;
			case AVGOFDISTINCT:
				enterOuterAlt(_localctx, 8);
				{
				setState(416);
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
		enterRule(_localctx, 56, RULE_yieldAlias);
		try {
			setState(421);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AS:
				enterOuterAlt(_localctx, 1);
				{
				setState(419);
				((YieldAliasContext)_localctx).token = match(AS);
				}
				break;
			case ASREQUIRED:
				enterOuterAlt(_localctx, 2);
				{
				setState(420);
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
		enterRule(_localctx, 58, RULE_yield1Model);
		try {
			setState(425);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MODELASENTITY:
				enterOuterAlt(_localctx, 1);
				{
				setState(423);
				((Yield1ModelContext)_localctx).token = match(MODELASENTITY);
				}
				break;
			case MODELASPRIMITIVE:
				enterOuterAlt(_localctx, 2);
				{
				setState(424);
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
		enterRule(_localctx, 60, RULE_yieldManyModel);
		try {
			setState(429);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MODELASENTITY:
				enterOuterAlt(_localctx, 1);
				{
				setState(427);
				((YieldManyModelContext)_localctx).token = match(MODELASENTITY);
				}
				break;
			case MODELASAGGREGATE:
				enterOuterAlt(_localctx, 2);
				{
				setState(428);
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
		enterRule(_localctx, 62, RULE_model);
		try {
			setState(434);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MODEL:
				enterOuterAlt(_localctx, 1);
				{
				setState(431);
				((ModelContext)_localctx).token = match(MODEL);
				}
				break;
			case MODELASENTITY:
				enterOuterAlt(_localctx, 2);
				{
				setState(432);
				((ModelContext)_localctx).token = match(MODELASENTITY);
				}
				break;
			case MODELASAGGREGATE:
				enterOuterAlt(_localctx, 3);
				{
				setState(433);
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
		int _startState = 64;
		enterRecursionRule(_localctx, 64, RULE_standaloneCondition, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			_localctx = new StandaloneCondition_PredicateContext(_localctx);
			_ctx = _localctx;
			_prevctx = _localctx;

			setState(437);
			predicate();
			}
			_ctx.stop = _input.LT(-1);
			setState(447);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,47,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(445);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,46,_ctx) ) {
					case 1:
						{
						_localctx = new AndStandaloneConditionContext(new StandaloneConditionContext(_parentctx, _parentState));
						((AndStandaloneConditionContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_standaloneCondition);
						setState(439);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(440);
						match(AND);
						setState(441);
						((AndStandaloneConditionContext)_localctx).right = standaloneCondition(3);
						}
						break;
					case 2:
						{
						_localctx = new OrStandaloneConditionContext(new StandaloneConditionContext(_parentctx, _parentState));
						((OrStandaloneConditionContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_standaloneCondition);
						setState(442);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(443);
						match(OR);
						setState(444);
						((OrStandaloneConditionContext)_localctx).right = standaloneCondition(2);
						}
						break;
					}
					} 
				}
				setState(449);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,47,_ctx);
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
		public Token yield;
		public OrderContext order() {
			return getRuleContext(OrderContext.class,0);
		}
		public TerminalNode YIELD() { return getToken(EQLParser.YIELD, 0); }
		public OrderByOperand_YieldContext(OrderByOperandContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof EQLVisitor ) return ((EQLVisitor<? extends T>)visitor).visitOrderByOperand_Yield(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class OrderByOperand_OrderingModelContext extends OrderByOperandContext {
		public Token token;
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
		enterRule(_localctx, 66, RULE_orderByOperand);
		try {
			setState(456);
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
				setState(450);
				singleOperand();
				setState(451);
				order();
				}
				break;
			case YIELD:
				_localctx = new OrderByOperand_YieldContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(453);
				((OrderByOperand_YieldContext)_localctx).yield = match(YIELD);
				setState(454);
				order();
				}
				break;
			case ORDER:
				_localctx = new OrderByOperand_OrderingModelContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(455);
				((OrderByOperand_OrderingModelContext)_localctx).token = match(ORDER);
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
		enterRule(_localctx, 68, RULE_order);
		try {
			setState(460);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ASC:
				enterOuterAlt(_localctx, 1);
				{
				setState(458);
				((OrderContext)_localctx).token = match(ASC);
				}
				break;
			case DESC:
				enterOuterAlt(_localctx, 2);
				{
				setState(459);
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
		case 4:
			return condition_sempred((ConditionContext)_localctx, predIndex);
		case 32:
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\u0085\u01d1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\3\2\3\2\3\2\3\3\3\3\5\3N\n\3\3\3\5\3Q\n\3\3\3"+
		"\5\3T\n\3\3\3\5\3W\n\3\3\3\3\3\3\3\3\3\3\3\3\3\7\3_\n\3\f\3\16\3b\13\3"+
		"\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\6\3l\n\3\r\3\16\3m\3\3\3\3\5\3r\n\3\3"+
		"\4\3\4\5\4v\n\4\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\5"+
		"\6\u0085\n\6\3\6\3\6\3\6\3\6\3\6\3\6\7\6\u008d\n\6\f\6\16\6\u0090\13\6"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\5\7\u00ae\n\7\5\7\u00b0\n\7"+
		"\3\b\3\b\5\b\u00b4\n\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\5\t\u00be\n\t\3"+
		"\n\3\n\5\n\u00c2\n\n\3\13\3\13\3\13\3\13\3\13\3\13\5\13\u00ca\n\13\3\f"+
		"\3\f\5\f\u00ce\n\f\3\r\3\r\3\r\3\r\7\r\u00d4\n\r\f\r\16\r\u00d7\13\r\3"+
		"\16\3\16\3\16\3\16\3\16\5\16\u00de\n\16\3\17\3\17\3\17\3\17\5\17\u00e4"+
		"\n\17\3\17\3\17\5\17\u00e8\n\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17"+
		"\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17"+
		"\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\7\17\u010a\n\17\f\17"+
		"\16\17\u010d\13\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3"+
		"\17\7\17\u011a\n\17\f\17\16\17\u011d\13\17\3\17\3\17\5\17\u0121\n\17\3"+
		"\17\3\17\3\17\3\17\3\17\3\17\5\17\u0129\n\17\3\20\3\20\3\20\3\20\3\20"+
		"\3\20\3\20\3\20\3\20\3\20\3\20\5\20\u0136\n\20\3\21\3\21\3\21\3\21\3\21"+
		"\3\21\5\21\u013e\n\21\3\22\3\22\3\22\3\22\3\22\5\22\u0145\n\22\3\23\3"+
		"\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\5\23\u0153\n\23"+
		"\3\24\3\24\5\24\u0157\n\24\3\25\3\25\3\25\3\25\3\25\5\25\u015e\n\25\3"+
		"\26\3\26\5\26\u0162\n\26\3\26\3\26\5\26\u0166\n\26\3\27\3\27\5\27\u016a"+
		"\n\27\3\30\3\30\3\30\3\31\3\31\6\31\u0171\n\31\r\31\16\31\u0172\3\32\3"+
		"\32\3\32\3\32\3\32\5\32\u017a\n\32\3\32\7\32\u017d\n\32\f\32\16\32\u0180"+
		"\13\32\3\32\5\32\u0183\n\32\3\33\3\33\3\33\3\33\3\34\3\34\3\34\3\34\3"+
		"\34\3\34\7\34\u018f\n\34\f\34\16\34\u0192\13\34\3\34\3\34\3\34\3\34\3"+
		"\34\3\34\5\34\u019a\n\34\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\5\35"+
		"\u01a4\n\35\3\36\3\36\5\36\u01a8\n\36\3\37\3\37\5\37\u01ac\n\37\3 \3 "+
		"\5 \u01b0\n \3!\3!\3!\5!\u01b5\n!\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\""+
		"\7\"\u01c0\n\"\f\"\16\"\u01c3\13\"\3#\3#\3#\3#\3#\3#\5#\u01cb\n#\3$\3"+
		"$\5$\u01cf\n$\3$\2\4\nB%\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&("+
		"*,.\60\62\64\668:<>@BDF\2\2\2\u022e\2H\3\2\2\2\4q\3\2\2\2\6u\3\2\2\2\b"+
		"w\3\2\2\2\n\u0084\3\2\2\2\f\u00af\3\2\2\2\16\u00b3\3\2\2\2\20\u00bd\3"+
		"\2\2\2\22\u00c1\3\2\2\2\24\u00c9\3\2\2\2\26\u00cd\3\2\2\2\30\u00cf\3\2"+
		"\2\2\32\u00dd\3\2\2\2\34\u0128\3\2\2\2\36\u0135\3\2\2\2 \u013d\3\2\2\2"+
		"\"\u0144\3\2\2\2$\u0152\3\2\2\2&\u0156\3\2\2\2(\u015d\3\2\2\2*\u015f\3"+
		"\2\2\2,\u0169\3\2\2\2.\u016b\3\2\2\2\60\u0170\3\2\2\2\62\u0182\3\2\2\2"+
		"\64\u0184\3\2\2\2\66\u0199\3\2\2\28\u01a3\3\2\2\2:\u01a7\3\2\2\2<\u01ab"+
		"\3\2\2\2>\u01af\3\2\2\2@\u01b4\3\2\2\2B\u01b6\3\2\2\2D\u01ca\3\2\2\2F"+
		"\u01ce\3\2\2\2HI\5\4\3\2IJ\7\2\2\3J\3\3\2\2\2KM\7s\2\2LN\7\25\2\2ML\3"+
		"\2\2\2MN\3\2\2\2NP\3\2\2\2OQ\5*\26\2PO\3\2\2\2PQ\3\2\2\2QS\3\2\2\2RT\5"+
		"\b\5\2SR\3\2\2\2ST\3\2\2\2TV\3\2\2\2UW\5\60\31\2VU\3\2\2\2VW\3\2\2\2W"+
		"X\3\2\2\2Xr\5\6\4\2YZ\78\2\2Z`\5\66\34\2[\\\5\32\16\2\\]\5\66\34\2]_\3"+
		"\2\2\2^[\3\2\2\2_b\3\2\2\2`^\3\2\2\2`a\3\2\2\2ac\3\2\2\2b`\3\2\2\2cd\7"+
		"T\2\2dr\3\2\2\2ef\7 \2\2fg\5B\"\2gh\7T\2\2hr\3\2\2\2ik\7j\2\2jl\5D#\2"+
		"kj\3\2\2\2lm\3\2\2\2mk\3\2\2\2mn\3\2\2\2no\3\2\2\2op\7T\2\2pr\3\2\2\2"+
		"qK\3\2\2\2qY\3\2\2\2qe\3\2\2\2qi\3\2\2\2r\5\3\2\2\2sv\5@!\2tv\5\62\32"+
		"\2us\3\2\2\2ut\3\2\2\2v\7\3\2\2\2wx\7}\2\2xy\5\n\6\2y\t\3\2\2\2z{\b\6"+
		"\1\2{\u0085\5\f\7\2|}\7\32\2\2}~\5\n\6\2~\177\7-\2\2\177\u0085\3\2\2\2"+
		"\u0080\u0081\7]\2\2\u0081\u0082\5\n\6\2\u0082\u0083\7-\2\2\u0083\u0085"+
		"\3\2\2\2\u0084z\3\2\2\2\u0084|\3\2\2\2\u0084\u0080\3\2\2\2\u0085\u008e"+
		"\3\2\2\2\u0086\u0087\f\6\2\2\u0087\u0088\7\r\2\2\u0088\u008d\5\n\6\7\u0089"+
		"\u008a\f\5\2\2\u008a\u008b\7h\2\2\u008b\u008d\5\n\6\6\u008c\u0086\3\2"+
		"\2\2\u008c\u0089\3\2\2\2\u008d\u0090\3\2\2\2\u008e\u008c\3\2\2\2\u008e"+
		"\u008f\3\2\2\2\u008f\13\3\2\2\2\u0090\u008e\3\2\2\2\u0091\u0092\5\22\n"+
		"\2\u0092\u0093\5\16\b\2\u0093\u00b0\3\2\2\2\u0094\u0095\5\22\n\2\u0095"+
		"\u0096\5\24\13\2\u0096\u0097\5\22\n\2\u0097\u00b0\3\2\2\2\u0098\u0099"+
		"\5\22\n\2\u0099\u009a\5\24\13\2\u009a\u009b\5\26\f\2\u009b\u00b0\3\2\2"+
		"\2\u009c\u009d\5\22\n\2\u009d\u009e\5\20\t\2\u009e\u009f\5\22\n\2\u009f"+
		"\u00b0\3\2\2\2\u00a0\u00a1\5\22\n\2\u00a1\u00a2\5&\24\2\u00a2\u00a3\5"+
		"(\25\2\u00a3\u00b0\3\2\2\2\u00a4\u00ae\7\65\2\2\u00a5\u00ae\7^\2\2\u00a6"+
		"\u00ae\7\67\2\2\u00a7\u00ae\7`\2\2\u00a8\u00ae\7\66\2\2\u00a9\u00ae\7"+
		"_\2\2\u00aa\u00ae\7&\2\2\u00ab\u00ae\7!\2\2\u00ac\u00ae\7\\\2\2\u00ad"+
		"\u00a4\3\2\2\2\u00ad\u00a5\3\2\2\2\u00ad\u00a6\3\2\2\2\u00ad\u00a7\3\2"+
		"\2\2\u00ad\u00a8\3\2\2\2\u00ad\u00a9\3\2\2\2\u00ad\u00aa\3\2\2\2\u00ad"+
		"\u00ab\3\2\2\2\u00ad\u00ac\3\2\2\2\u00ae\u00b0\3\2\2\2\u00af\u0091\3\2"+
		"\2\2\u00af\u0094\3\2\2\2\u00af\u0098\3\2\2\2\u00af\u009c\3\2\2\2\u00af"+
		"\u00a0\3\2\2\2\u00af\u00ad\3\2\2\2\u00b0\r\3\2\2\2\u00b1\u00b4\7F\2\2"+
		"\u00b2\u00b4\7E\2\2\u00b3\u00b1\3\2\2\2\u00b3\u00b2\3\2\2\2\u00b4\17\3"+
		"\2\2\2\u00b5\u00be\7K\2\2\u00b6\u00be\7@\2\2\u00b7\u00be\7L\2\2\u00b8"+
		"\u00be\7A\2\2\u00b9\u00be\7d\2\2\u00ba\u00be\7e\2\2\u00bb\u00be\7b\2\2"+
		"\u00bc\u00be\7a\2\2\u00bd\u00b5\3\2\2\2\u00bd\u00b6\3\2\2\2\u00bd\u00b7"+
		"\3\2\2\2\u00bd\u00b8\3\2\2\2\u00bd\u00b9\3\2\2\2\u00bd\u00ba\3\2\2\2\u00bd"+
		"\u00bb\3\2\2\2\u00bd\u00bc\3\2\2\2\u00be\21\3\2\2\2\u00bf\u00c2\5\34\17"+
		"\2\u00c0\u00c2\5$\23\2\u00c1\u00bf\3\2\2\2\u00c1\u00c0\3\2\2\2\u00c2\23"+
		"\3\2\2\2\u00c3\u00ca\7\64\2\2\u00c4\u00ca\7<\2\2\u00c5\u00ca\7N\2\2\u00c6"+
		"\u00ca\7:\2\2\u00c7\u00ca\7I\2\2\u00c8\u00ca\7[\2\2\u00c9\u00c3\3\2\2"+
		"\2\u00c9\u00c4\3\2\2\2\u00c9\u00c5\3\2\2\2\u00c9\u00c6\3\2\2\2\u00c9\u00c7"+
		"\3\2\2\2\u00c9\u00c8\3\2\2\2\u00ca\25\3\2\2\2\u00cb\u00ce\7\6\2\2\u00cc"+
		"\u00ce\7\16\2\2\u00cd\u00cb\3\2\2\2\u00cd\u00cc\3\2\2\2\u00ce\27\3\2\2"+
		"\2\u00cf\u00d5\5\34\17\2\u00d0\u00d1\5\32\16\2\u00d1\u00d2\5\34\17\2\u00d2"+
		"\u00d4\3\2\2\2\u00d3\u00d0\3\2\2\2\u00d4\u00d7\3\2\2\2\u00d5\u00d3\3\2"+
		"\2\2\u00d5\u00d6\3\2\2\2\u00d6\31\3\2\2\2\u00d7\u00d5\3\2\2\2\u00d8\u00de"+
		"\7\4\2\2\u00d9\u00de\7t\2\2\u00da\u00de\7,\2\2\u00db\u00de\7Z\2\2\u00dc"+
		"\u00de\7S\2\2\u00dd\u00d8\3\2\2\2\u00dd\u00d9\3\2\2\2\u00dd\u00da\3\2"+
		"\2\2\u00dd\u00db\3\2\2\2\u00dd\u00dc\3\2\2\2\u00de\33\3\2\2\2\u00df\u0129"+
		"\7n\2\2\u00e0\u0129\79\2\2\u00e1\u00e4\7z\2\2\u00e2\u00e4\7G\2\2\u00e3"+
		"\u00e1\3\2\2\2\u00e3\u00e2\3\2\2\2\u00e4\u0129\3\2\2\2\u00e5\u00e8\7l"+
		"\2\2\u00e6\u00e8\7C\2\2\u00e7\u00e5\3\2\2\2\u00e7\u00e6\3\2\2\2\u00e8"+
		"\u0129\3\2\2\2\u00e9\u0129\78\2\2\u00ea\u0129\7T\2\2\u00eb\u00ec\5\36"+
		"\20\2\u00ec\u00ed\5\34\17\2\u00ed\u0129\3\2\2\2\u00ee\u00ef\7?\2\2\u00ef"+
		"\u00f0\5\34\17\2\u00f0\u00f1\7w\2\2\u00f1\u00f2\5\34\17\2\u00f2\u0129"+
		"\3\2\2\2\u00f3\u0129\7f\2\2\u00f4\u00f5\7\"\2\2\u00f5\u00f6\5 \21\2\u00f6"+
		"\u00f7\7\35\2\2\u00f7\u00f8\5\34\17\2\u00f8\u00f9\7\r\2\2\u00f9\u00fa"+
		"\5\34\17\2\u00fa\u0129\3\2\2\2\u00fb\u00fc\7\5\2\2\u00fc\u00fd\5\34\17"+
		"\2\u00fd\u00fe\5 \21\2\u00fe\u00ff\7x\2\2\u00ff\u0100\5\34\17\2\u0100"+
		"\u0129\3\2\2\2\u0101\u0102\7p\2\2\u0102\u0103\5\34\17\2\u0103\u0104\7"+
		"x\2\2\u0104\u0129\3\2\2\2\u0105\u0106\7\37\2\2\u0106\u010b\5\34\17\2\u0107"+
		"\u0108\7~\2\2\u0108\u010a\5\34\17\2\u0109\u0107\3\2\2\2\u010a\u010d\3"+
		"\2\2\2\u010b\u0109\3\2\2\2\u010b\u010c\3\2\2\2\u010c\u010e\3\2\2\2\u010d"+
		"\u010b\3\2\2\2\u010e\u010f\7-\2\2\u010f\u0129\3\2\2\2\u0110\u0111\7\36"+
		"\2\2\u0111\u0112\5\n\6\2\u0112\u0113\7w\2\2\u0113\u011b\5\34\17\2\u0114"+
		"\u0115\7|\2\2\u0115\u0116\5\n\6\2\u0116\u0117\7w\2\2\u0117\u0118\5\34"+
		"\17\2\u0118\u011a\3\2\2\2\u0119\u0114\3\2\2\2\u011a\u011d\3\2\2\2\u011b"+
		"\u0119\3\2\2\2\u011b\u011c\3\2\2\2\u011c\u0120\3\2\2\2\u011d\u011b\3\2"+
		"\2\2\u011e\u011f\7k\2\2\u011f\u0121\5\34\17\2\u0120\u011e\3\2\2\2\u0120"+
		"\u0121\3\2\2\2\u0121\u0122\3\2\2\2\u0122\u0123\5\"\22\2\u0123\u0129\3"+
		"\2\2\2\u0124\u0125\7\33\2\2\u0125\u0126\5\30\r\2\u0126\u0127\7\62\2\2"+
		"\u0127\u0129\3\2\2\2\u0128\u00df\3\2\2\2\u0128\u00e0\3\2\2\2\u0128\u00e3"+
		"\3\2\2\2\u0128\u00e7\3\2\2\2\u0128\u00e9\3\2\2\2\u0128\u00ea\3\2\2\2\u0128"+
		"\u00eb\3\2\2\2\u0128\u00ee\3\2\2\2\u0128\u00f3\3\2\2\2\u0128\u00f4\3\2"+
		"\2\2\u0128\u00fb\3\2\2\2\u0128\u0101\3\2\2\2\u0128\u0105\3\2\2\2\u0128"+
		"\u0110\3\2\2\2\u0128\u0124\3\2\2\2\u0129\35\3\2\2\2\u012a\u0136\7y\2\2"+
		"\u012b\u0136\7M\2\2\u012c\u0136\7q\2\2\u012d\u0136\7Q\2\2\u012e\u0136"+
		"\7=\2\2\u012f\u0136\7(\2\2\u0130\u0136\7X\2\2\u0131\u0136\7\177\2\2\u0132"+
		"\u0136\7)\2\2\u0133\u0136\7\3\2\2\u0134\u0136\7\'\2\2\u0135\u012a\3\2"+
		"\2\2\u0135\u012b\3\2\2\2\u0135\u012c\3\2\2\2\u0135\u012d\3\2\2\2\u0135"+
		"\u012e\3\2\2\2\u0135\u012f\3\2\2\2\u0135\u0130\3\2\2\2\u0135\u0131\3\2"+
		"\2\2\u0135\u0132\3\2\2\2\u0135\u0133\3\2\2\2\u0135\u0134\3\2\2\2\u0136"+
		"\37\3\2\2\2\u0137\u013e\7r\2\2\u0138\u013e\7R\2\2\u0139\u013e\7>\2\2\u013a"+
		"\u013e\7*\2\2\u013b\u013e\7Y\2\2\u013c\u013e\7\u0080\2\2\u013d\u0137\3"+
		"\2\2\2\u013d\u0138\3\2\2\2\u013d\u0139\3\2\2\2\u013d\u013a\3\2\2\2\u013d"+
		"\u013b\3\2\2\2\u013d\u013c\3\2\2\2\u013e!\3\2\2\2\u013f\u0145\7-\2\2\u0140"+
		"\u0145\7\60\2\2\u0141\u0145\7.\2\2\u0142\u0145\7\61\2\2\u0143\u0145\7"+
		"/\2\2\u0144\u013f\3\2\2\2\u0144\u0140\3\2\2\2\u0144\u0141\3\2\2\2\u0144"+
		"\u0142\3\2\2\2\u0144\u0143\3\2\2\2\u0145#\3\2\2\2\u0146\u0153\7\23\2\2"+
		"\u0147\u0153\7\13\2\2\u0148\u0153\7\24\2\2\u0149\u0153\7\f\2\2\u014a\u0153"+
		"\7\22\2\2\u014b\u0153\7\20\2\2\u014c\u0153\7\n\2\2\u014d\u0153\7\b\2\2"+
		"\u014e\u0153\7\21\2\2\u014f\u0153\7\t\2\2\u0150\u0153\7\17\2\2\u0151\u0153"+
		"\7\7\2\2\u0152\u0146\3\2\2\2\u0152\u0147\3\2\2\2\u0152\u0148\3\2\2\2\u0152"+
		"\u0149\3\2\2\2\u0152\u014a\3\2\2\2\u0152\u014b\3\2\2\2\u0152\u014c\3\2"+
		"\2\2\u0152\u014d\3\2\2\2\u0152\u014e\3\2\2\2\u0152\u014f\3\2\2\2\u0152"+
		"\u0150\3\2\2\2\u0152\u0151\3\2\2\2\u0153%\3\2\2\2\u0154\u0157\7B\2\2\u0155"+
		"\u0157\7c\2\2\u0156\u0154\3\2\2\2\u0156\u0155\3\2\2\2\u0157\'\3\2\2\2"+
		"\u0158\u015e\7{\2\2\u0159\u015e\7o\2\2\u015a\u015e\7m\2\2\u015b\u015e"+
		"\7D\2\2\u015c\u015e\7T\2\2\u015d\u0158\3\2\2\2\u015d\u0159\3\2\2\2\u015d"+
		"\u015a\3\2\2\2\u015d\u015b\3\2\2\2\u015d\u015c\3\2\2\2\u015e)\3\2\2\2"+
		"\u015f\u0161\5,\27\2\u0160\u0162\7\25\2\2\u0161\u0160\3\2\2\2\u0161\u0162"+
		"\3\2\2\2\u0162\u0163\3\2\2\2\u0163\u0165\5.\30\2\u0164\u0166\5*\26\2\u0165"+
		"\u0164\3\2\2\2\u0165\u0166\3\2\2\2\u0166+\3\2\2\2\u0167\u016a\7H\2\2\u0168"+
		"\u016a\7J\2\2\u0169\u0167\3\2\2\2\u0169\u0168\3\2\2\2\u016a-\3\2\2\2\u016b"+
		"\u016c\7g\2\2\u016c\u016d\5\n\6\2\u016d/\3\2\2\2\u016e\u016f\7;\2\2\u016f"+
		"\u0171\5\34\17\2\u0170\u016e\3\2\2\2\u0171\u0172\3\2\2\2\u0172\u0170\3"+
		"\2\2\2\u0172\u0173\3\2\2\2\u0173\61\3\2\2\2\u0174\u0175\7\u0081\2\2\u0175"+
		"\u0176\5\66\34\2\u0176\u0177\5<\37\2\u0177\u0183\3\2\2\2\u0178\u017a\7"+
		"\u0082\2\2\u0179\u0178\3\2\2\2\u0179\u017a\3\2\2\2\u017a\u017e\3\2\2\2"+
		"\u017b\u017d\5\64\33\2\u017c\u017b\3\2\2\2\u017d\u0180\3\2\2\2\u017e\u017c"+
		"\3\2\2\2\u017e\u017f\3\2\2\2\u017f\u0181\3\2\2\2\u0180\u017e\3\2\2\2\u0181"+
		"\u0183\5> \2\u0182\u0174\3\2\2\2\u0182\u0179\3\2\2\2\u0183\63\3\2\2\2"+
		"\u0184\u0185\7\u0081\2\2\u0185\u0186\5\66\34\2\u0186\u0187\5:\36\2\u0187"+
		"\65\3\2\2\2\u0188\u019a\5\34\17\2\u0189\u018a\7\34\2\2\u018a\u0190\5\66"+
		"\34\2\u018b\u018c\5\32\16\2\u018c\u018d\5\66\34\2\u018d\u018f\3\2\2\2"+
		"\u018e\u018b\3\2\2\2\u018f\u0192\3\2\2\2\u0190\u018e\3\2\2\2\u0190\u0191"+
		"\3\2\2\2\u0191\u0193\3\2\2\2\u0192\u0190\3\2\2\2\u0193\u0194\7\63\2\2"+
		"\u0194\u019a\3\2\2\2\u0195\u019a\7#\2\2\u0196\u0197\58\35\2\u0197\u0198"+
		"\5\34\17\2\u0198\u019a\3\2\2\2\u0199\u0188\3\2\2\2\u0199\u0189\3\2\2\2"+
		"\u0199\u0195\3\2\2\2\u0199\u0196\3\2\2\2\u019a\67\3\2\2\2\u019b\u01a4"+
		"\7O\2\2\u019c\u01a4\7P\2\2\u019d\u01a4\7u\2\2\u019e\u01a4\7$\2\2\u019f"+
		"\u01a4\7\30\2\2\u01a0\u01a4\7v\2\2\u01a1\u01a4\7%\2\2\u01a2\u01a4\7\31"+
		"\2\2\u01a3\u019b\3\2\2\2\u01a3\u019c\3\2\2\2\u01a3\u019d\3\2\2\2\u01a3"+
		"\u019e\3\2\2\2\u01a3\u019f\3\2\2\2\u01a3\u01a0\3\2\2\2\u01a3\u01a1\3\2"+
		"\2\2\u01a3\u01a2\3\2\2\2\u01a49\3\2\2\2\u01a5\u01a8\7\25\2\2\u01a6\u01a8"+
		"\7\27\2\2\u01a7\u01a5\3\2\2\2\u01a7\u01a6\3\2\2\2\u01a8;\3\2\2\2\u01a9"+
		"\u01ac\7V\2\2\u01aa\u01ac\7W\2\2\u01ab\u01a9\3\2\2\2\u01ab\u01aa\3\2\2"+
		"\2\u01ac=\3\2\2\2\u01ad\u01b0\7V\2\2\u01ae\u01b0\7U\2\2\u01af\u01ad\3"+
		"\2\2\2\u01af\u01ae\3\2\2\2\u01b0?\3\2\2\2\u01b1\u01b5\7T\2\2\u01b2\u01b5"+
		"\7V\2\2\u01b3\u01b5\7U\2\2\u01b4\u01b1\3\2\2\2\u01b4\u01b2\3\2\2\2\u01b4"+
		"\u01b3\3\2\2\2\u01b5A\3\2\2\2\u01b6\u01b7\b\"\1\2\u01b7\u01b8\5\f\7\2"+
		"\u01b8\u01c1\3\2\2\2\u01b9\u01ba\f\4\2\2\u01ba\u01bb\7\r\2\2\u01bb\u01c0"+
		"\5B\"\5\u01bc\u01bd\f\3\2\2\u01bd\u01be\7h\2\2\u01be\u01c0\5B\"\4\u01bf"+
		"\u01b9\3\2\2\2\u01bf\u01bc\3\2\2\2\u01c0\u01c3\3\2\2\2\u01c1\u01bf\3\2"+
		"\2\2\u01c1\u01c2\3\2\2\2\u01c2C\3\2\2\2\u01c3\u01c1\3\2\2\2\u01c4\u01c5"+
		"\5\34\17\2\u01c5\u01c6\5F$\2\u01c6\u01cb\3\2\2\2\u01c7\u01c8\7\u0081\2"+
		"\2\u01c8\u01cb\5F$\2\u01c9\u01cb\7i\2\2\u01ca\u01c4\3\2\2\2\u01ca\u01c7"+
		"\3\2\2\2\u01ca\u01c9\3\2\2\2\u01cbE\3\2\2\2\u01cc\u01cf\7\26\2\2\u01cd"+
		"\u01cf\7+\2\2\u01ce\u01cc\3\2\2\2\u01ce\u01cd\3\2\2\2\u01cfG\3\2\2\2\64"+
		"MPSV`mqu\u0084\u008c\u008e\u00ad\u00af\u00b3\u00bd\u00c1\u00c9\u00cd\u00d5"+
		"\u00dd\u00e3\u00e7\u010b\u011b\u0120\u0128\u0135\u013d\u0144\u0152\u0156"+
		"\u015d\u0161\u0165\u0169\u0172\u0179\u017e\u0182\u0190\u0199\u01a3\u01a7"+
		"\u01ab\u01af\u01b4\u01bf\u01c1\u01ca\u01ce";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}