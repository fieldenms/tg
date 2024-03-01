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
		RULE_start = 0, RULE_query = 1, RULE_selectEnd = 2, RULE_where = 3, RULE_condition = 4, 
		RULE_predicate = 5, RULE_unaryComparisonOperator = 6, RULE_likeOperator = 7, 
		RULE_comparisonOperand = 8, RULE_comparisonOperator = 9, RULE_quantifiedOperand = 10, 
		RULE_exprBody = 11, RULE_arithmeticalOperator = 12, RULE_singleOperand = 13, 
		RULE_unaryFunctionName = 14, RULE_dateDiffIntervalUnit = 15, RULE_dateAddIntervalUnit = 16, 
		RULE_caseWhenEnd = 17, RULE_multiOperand = 18, RULE_membershipOperator = 19, 
		RULE_membershipOperand = 20, RULE_join = 21, RULE_joinOperator = 22, RULE_joinCondition = 23, 
		RULE_groupBy = 24, RULE_anyYield = 25, RULE_aliasedYield = 26, RULE_yieldOperand = 27, 
		RULE_yieldOperandFunctionName = 28, RULE_yieldAlias = 29, RULE_yield1Model = 30, 
		RULE_yieldManyModel = 31, RULE_model = 32, RULE_standaloneCondition = 33, 
		RULE_orderByOperand = 34, RULE_order = 35;
	private static String[] makeRuleNames() {
		return new String[] {
			"start", "query", "selectEnd", "where", "condition", "predicate", "unaryComparisonOperator", 
			"likeOperator", "comparisonOperand", "comparisonOperator", "quantifiedOperand", 
			"exprBody", "arithmeticalOperator", "singleOperand", "unaryFunctionName", 
			"dateDiffIntervalUnit", "dateAddIntervalUnit", "caseWhenEnd", "multiOperand", 
			"membershipOperator", "membershipOperand", "join", "joinOperator", "joinCondition", 
			"groupBy", "anyYield", "aliasedYield", "yieldOperand", "yieldOperandFunctionName", 
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
			setState(72);
			query();
			setState(73);
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
			setState(113);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SELECT:
				_localctx = new SelectContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(75);
				((SelectContext)_localctx).select = match(SELECT);
				setState(77);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(76);
					((SelectContext)_localctx).alias = match(AS);
					}
				}

				setState(80);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==JOIN || _la==LEFTJOIN) {
					{
					setState(79);
					join();
					}
				}

				setState(83);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WHERE) {
					{
					setState(82);
					where();
					}
				}

				setState(86);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==GROUPBY) {
					{
					setState(85);
					groupBy();
					}
				}

				setState(88);
				selectEnd();
				}
				break;
			case EXPR:
				_localctx = new StandaloneExpressionContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(89);
				match(EXPR);
				setState(90);
				((StandaloneExpressionContext)_localctx).operand = yieldOperand();
				setState(96);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==ADD || _la==DIV || ((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & ((1L << (MOD - 79)) | (1L << (MULT - 79)) | (1L << (SUB - 79)))) != 0)) {
					{
					{
					setState(91);
					arithmeticalOperator();
					setState(92);
					yieldOperand();
					}
					}
					setState(98);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(99);
				match(MODEL);
				}
				break;
			case COND:
				_localctx = new StandaloneCondExprContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(101);
				match(COND);
				setState(102);
				standaloneCondition(0);
				setState(103);
				match(MODEL);
				}
				break;
			case ORDERBY:
				_localctx = new OrderByContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(105);
				match(ORDERBY);
				setState(107); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(106);
						orderByOperand();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(109); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(111);
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
			setState(117);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				_localctx = new SelectEnd_ModelContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(115);
				model();
				}
				break;
			case 2:
				_localctx = new SelectEnd_AnyYieldContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(116);
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
			setState(119);
			match(WHERE);
			setState(120);
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
			setState(132);
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

				setState(123);
				predicate();
				}
				break;
			case BEGIN:
				{
				_localctx = new CompoundConditionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(124);
				match(BEGIN);
				setState(125);
				condition(0);
				setState(126);
				match(END);
				}
				break;
			case NOTBEGIN:
				{
				_localctx = new NegatedCompoundConditionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(128);
				match(NOTBEGIN);
				setState(129);
				condition(0);
				setState(130);
				match(END);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(142);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(140);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
					case 1:
						{
						_localctx = new AndConditionContext(new ConditionContext(_parentctx, _parentState));
						((AndConditionContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_condition);
						setState(134);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(135);
						match(AND);
						setState(136);
						((AndConditionContext)_localctx).right = condition(5);
						}
						break;
					case 2:
						{
						_localctx = new OrConditionContext(new ConditionContext(_parentctx, _parentState));
						((OrConditionContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_condition);
						setState(137);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(138);
						match(OR);
						setState(139);
						((OrConditionContext)_localctx).right = condition(4);
						}
						break;
					}
					} 
				}
				setState(144);
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
			setState(175);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				_localctx = new UnaryPredicateContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(145);
				((UnaryPredicateContext)_localctx).left = comparisonOperand();
				setState(146);
				unaryComparisonOperator();
				}
				break;
			case 2:
				_localctx = new ComparisonPredicateContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(148);
				((ComparisonPredicateContext)_localctx).left = comparisonOperand();
				setState(149);
				comparisonOperator();
				setState(150);
				((ComparisonPredicateContext)_localctx).right = comparisonOperand();
				}
				break;
			case 3:
				_localctx = new QuantifiedComparisonPredicateContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(152);
				((QuantifiedComparisonPredicateContext)_localctx).left = comparisonOperand();
				setState(153);
				comparisonOperator();
				setState(154);
				quantifiedOperand();
				}
				break;
			case 4:
				_localctx = new LikePredicateContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(156);
				((LikePredicateContext)_localctx).left = comparisonOperand();
				setState(157);
				likeOperator();
				setState(158);
				((LikePredicateContext)_localctx).right = comparisonOperand();
				}
				break;
			case 5:
				_localctx = new MembershipPredicateContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(160);
				((MembershipPredicateContext)_localctx).left = comparisonOperand();
				setState(161);
				membershipOperator();
				setState(162);
				membershipOperand();
				}
				break;
			case 6:
				_localctx = new SingleConditionPredicateContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(173);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case EXISTS:
					{
					setState(164);
					((SingleConditionPredicateContext)_localctx).token = match(EXISTS);
					}
					break;
				case NOTEXISTS:
					{
					setState(165);
					((SingleConditionPredicateContext)_localctx).token = match(NOTEXISTS);
					}
					break;
				case EXISTSANYOF:
					{
					setState(166);
					((SingleConditionPredicateContext)_localctx).token = match(EXISTSANYOF);
					}
					break;
				case NOTEXISTSANYOF:
					{
					setState(167);
					((SingleConditionPredicateContext)_localctx).token = match(NOTEXISTSANYOF);
					}
					break;
				case EXISTSALLOF:
					{
					setState(168);
					((SingleConditionPredicateContext)_localctx).token = match(EXISTSALLOF);
					}
					break;
				case NOTEXISTSALLOF:
					{
					setState(169);
					((SingleConditionPredicateContext)_localctx).token = match(NOTEXISTSALLOF);
					}
					break;
				case CRITCONDITION:
					{
					setState(170);
					((SingleConditionPredicateContext)_localctx).token = match(CRITCONDITION);
					}
					break;
				case CONDITION:
					{
					setState(171);
					((SingleConditionPredicateContext)_localctx).token = match(CONDITION);
					}
					break;
				case NEGATEDCONDITION:
					{
					setState(172);
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
			setState(179);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ISNULL:
				enterOuterAlt(_localctx, 1);
				{
				setState(177);
				((UnaryComparisonOperatorContext)_localctx).token = match(ISNULL);
				}
				break;
			case ISNOTNULL:
				enterOuterAlt(_localctx, 2);
				{
				setState(178);
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
			setState(189);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LIKE:
				enterOuterAlt(_localctx, 1);
				{
				setState(181);
				((LikeOperatorContext)_localctx).token = match(LIKE);
				}
				break;
			case ILIKE:
				enterOuterAlt(_localctx, 2);
				{
				setState(182);
				((LikeOperatorContext)_localctx).token = match(ILIKE);
				}
				break;
			case LIKEWITHCAST:
				enterOuterAlt(_localctx, 3);
				{
				setState(183);
				((LikeOperatorContext)_localctx).token = match(LIKEWITHCAST);
				}
				break;
			case ILIKEWITHCAST:
				enterOuterAlt(_localctx, 4);
				{
				setState(184);
				((LikeOperatorContext)_localctx).token = match(ILIKEWITHCAST);
				}
				break;
			case NOTLIKE:
				enterOuterAlt(_localctx, 5);
				{
				setState(185);
				((LikeOperatorContext)_localctx).token = match(NOTLIKE);
				}
				break;
			case NOTLIKEWITHCAST:
				enterOuterAlt(_localctx, 6);
				{
				setState(186);
				((LikeOperatorContext)_localctx).token = match(NOTLIKEWITHCAST);
				}
				break;
			case NOTILIKEWITHCAST:
				enterOuterAlt(_localctx, 7);
				{
				setState(187);
				((LikeOperatorContext)_localctx).token = match(NOTILIKEWITHCAST);
				}
				break;
			case NOTILIKE:
				enterOuterAlt(_localctx, 8);
				{
				setState(188);
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
			setState(193);
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
				setState(191);
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
				setState(192);
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
			setState(201);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EQ:
				enterOuterAlt(_localctx, 1);
				{
				setState(195);
				((ComparisonOperatorContext)_localctx).token = match(EQ);
				}
				break;
			case GT:
				enterOuterAlt(_localctx, 2);
				{
				setState(196);
				((ComparisonOperatorContext)_localctx).token = match(GT);
				}
				break;
			case LT:
				enterOuterAlt(_localctx, 3);
				{
				setState(197);
				((ComparisonOperatorContext)_localctx).token = match(LT);
				}
				break;
			case GE:
				enterOuterAlt(_localctx, 4);
				{
				setState(198);
				((ComparisonOperatorContext)_localctx).token = match(GE);
				}
				break;
			case LE:
				enterOuterAlt(_localctx, 5);
				{
				setState(199);
				((ComparisonOperatorContext)_localctx).token = match(LE);
				}
				break;
			case NE:
				enterOuterAlt(_localctx, 6);
				{
				setState(200);
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
			setState(205);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ALL:
				enterOuterAlt(_localctx, 1);
				{
				setState(203);
				((QuantifiedOperandContext)_localctx).token = match(ALL);
				}
				break;
			case ANY:
				enterOuterAlt(_localctx, 2);
				{
				setState(204);
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
		enterRule(_localctx, 22, RULE_exprBody);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(207);
			singleOperand();
			setState(213);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ADD || _la==DIV || ((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & ((1L << (MOD - 79)) | (1L << (MULT - 79)) | (1L << (SUB - 79)))) != 0)) {
				{
				{
				setState(208);
				arithmeticalOperator();
				setState(209);
				singleOperand();
				}
				}
				setState(215);
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
			setState(221);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ADD:
				enterOuterAlt(_localctx, 1);
				{
				setState(216);
				((ArithmeticalOperatorContext)_localctx).token = match(ADD);
				}
				break;
			case SUB:
				enterOuterAlt(_localctx, 2);
				{
				setState(217);
				((ArithmeticalOperatorContext)_localctx).token = match(SUB);
				}
				break;
			case DIV:
				enterOuterAlt(_localctx, 3);
				{
				setState(218);
				((ArithmeticalOperatorContext)_localctx).token = match(DIV);
				}
				break;
			case MULT:
				enterOuterAlt(_localctx, 4);
				{
				setState(219);
				((ArithmeticalOperatorContext)_localctx).token = match(MULT);
				}
				break;
			case MOD:
				enterOuterAlt(_localctx, 5);
				{
				setState(220);
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
		enterRule(_localctx, 26, RULE_singleOperand);
		int _la;
		try {
			setState(296);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PROP:
				_localctx = new PropContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(223);
				((PropContext)_localctx).token = match(PROP);
				}
				break;
			case EXTPROP:
				_localctx = new ExtPropContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(224);
				((ExtPropContext)_localctx).token = match(EXTPROP);
				}
				break;
			case IVAL:
			case VAL:
				_localctx = new ValContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(227);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case VAL:
					{
					setState(225);
					((ValContext)_localctx).token = match(VAL);
					}
					break;
				case IVAL:
					{
					setState(226);
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
				setState(231);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case PARAM:
					{
					setState(229);
					((ParamContext)_localctx).token = match(PARAM);
					}
					break;
				case IPARAM:
					{
					setState(230);
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
				setState(233);
				((SingleOperand_ExprContext)_localctx).token = match(EXPR);
				}
				break;
			case MODEL:
				_localctx = new SingleOperand_ModelContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(234);
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
				setState(235);
				((UnaryFunctionContext)_localctx).funcName = unaryFunctionName();
				setState(236);
				((UnaryFunctionContext)_localctx).argument = singleOperand();
				}
				break;
			case IFNULL:
				_localctx = new IfNullContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(238);
				match(IFNULL);
				setState(239);
				((IfNullContext)_localctx).nullable = singleOperand();
				setState(240);
				match(THEN);
				setState(241);
				((IfNullContext)_localctx).other = singleOperand();
				}
				break;
			case NOW:
				_localctx = new SingleOperand_NowContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(243);
				match(NOW);
				}
				break;
			case COUNT:
				_localctx = new DateDiffIntervalContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(244);
				match(COUNT);
				setState(245);
				((DateDiffIntervalContext)_localctx).unit = dateDiffIntervalUnit();
				setState(246);
				match(BETWEEN);
				setState(247);
				((DateDiffIntervalContext)_localctx).startDate = singleOperand();
				setState(248);
				match(AND);
				setState(249);
				((DateDiffIntervalContext)_localctx).endDate = singleOperand();
				}
				break;
			case ADDTIMEINTERVALOF:
				_localctx = new DateAddIntervalContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(251);
				match(ADDTIMEINTERVALOF);
				setState(252);
				((DateAddIntervalContext)_localctx).left = singleOperand();
				setState(253);
				((DateAddIntervalContext)_localctx).unit = dateAddIntervalUnit();
				setState(254);
				match(TO);
				setState(255);
				((DateAddIntervalContext)_localctx).right = singleOperand();
				}
				break;
			case ROUND:
				_localctx = new RoundContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(257);
				match(ROUND);
				setState(258);
				singleOperand();
				setState(259);
				match(TO);
				}
				break;
			case CONCAT:
				_localctx = new ConcatContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(261);
				match(CONCAT);
				setState(262);
				singleOperand();
				setState(267);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WITH) {
					{
					{
					setState(263);
					match(WITH);
					setState(264);
					singleOperand();
					}
					}
					setState(269);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(270);
				match(END);
				}
				break;
			case CASEWHEN:
				_localctx = new CaseWhenContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(272);
				match(CASEWHEN);
				setState(273);
				condition(0);
				setState(274);
				match(THEN);
				setState(275);
				singleOperand();
				setState(283);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WHEN) {
					{
					{
					setState(276);
					match(WHEN);
					setState(277);
					condition(0);
					setState(278);
					match(THEN);
					setState(279);
					singleOperand();
					}
					}
					setState(285);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(288);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OTHERWISE) {
					{
					setState(286);
					match(OTHERWISE);
					setState(287);
					((CaseWhenContext)_localctx).otherwiseOperand = singleOperand();
					}
				}

				setState(290);
				caseWhenEnd();
				}
				break;
			case BEGINEXPR:
				_localctx = new ExprContext(_localctx);
				enterOuterAlt(_localctx, 15);
				{
				setState(292);
				match(BEGINEXPR);
				setState(293);
				exprBody();
				setState(294);
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
			setState(309);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case UPPERCASE:
				enterOuterAlt(_localctx, 1);
				{
				setState(298);
				((UnaryFunctionNameContext)_localctx).token = match(UPPERCASE);
				}
				break;
			case LOWERCASE:
				enterOuterAlt(_localctx, 2);
				{
				setState(299);
				((UnaryFunctionNameContext)_localctx).token = match(LOWERCASE);
				}
				break;
			case SECONDOF:
				enterOuterAlt(_localctx, 3);
				{
				setState(300);
				((UnaryFunctionNameContext)_localctx).token = match(SECONDOF);
				}
				break;
			case MINUTEOF:
				enterOuterAlt(_localctx, 4);
				{
				setState(301);
				((UnaryFunctionNameContext)_localctx).token = match(MINUTEOF);
				}
				break;
			case HOUROF:
				enterOuterAlt(_localctx, 5);
				{
				setState(302);
				((UnaryFunctionNameContext)_localctx).token = match(HOUROF);
				}
				break;
			case DAYOF:
				enterOuterAlt(_localctx, 6);
				{
				setState(303);
				((UnaryFunctionNameContext)_localctx).token = match(DAYOF);
				}
				break;
			case MONTHOF:
				enterOuterAlt(_localctx, 7);
				{
				setState(304);
				((UnaryFunctionNameContext)_localctx).token = match(MONTHOF);
				}
				break;
			case YEAROF:
				enterOuterAlt(_localctx, 8);
				{
				setState(305);
				((UnaryFunctionNameContext)_localctx).token = match(YEAROF);
				}
				break;
			case DAYOFWEEKOF:
				enterOuterAlt(_localctx, 9);
				{
				setState(306);
				((UnaryFunctionNameContext)_localctx).token = match(DAYOFWEEKOF);
				}
				break;
			case ABSOF:
				enterOuterAlt(_localctx, 10);
				{
				setState(307);
				((UnaryFunctionNameContext)_localctx).token = match(ABSOF);
				}
				break;
			case DATEOF:
				enterOuterAlt(_localctx, 11);
				{
				setState(308);
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
		enterRule(_localctx, 30, RULE_dateDiffIntervalUnit);
		try {
			setState(317);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SECONDS:
				enterOuterAlt(_localctx, 1);
				{
				setState(311);
				((DateDiffIntervalUnitContext)_localctx).token = match(SECONDS);
				}
				break;
			case MINUTES:
				enterOuterAlt(_localctx, 2);
				{
				setState(312);
				((DateDiffIntervalUnitContext)_localctx).token = match(MINUTES);
				}
				break;
			case HOURS:
				enterOuterAlt(_localctx, 3);
				{
				setState(313);
				((DateDiffIntervalUnitContext)_localctx).token = match(HOURS);
				}
				break;
			case DAYS:
				enterOuterAlt(_localctx, 4);
				{
				setState(314);
				((DateDiffIntervalUnitContext)_localctx).token = match(DAYS);
				}
				break;
			case MONTHS:
				enterOuterAlt(_localctx, 5);
				{
				setState(315);
				((DateDiffIntervalUnitContext)_localctx).token = match(MONTHS);
				}
				break;
			case YEARS:
				enterOuterAlt(_localctx, 6);
				{
				setState(316);
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
		enterRule(_localctx, 32, RULE_dateAddIntervalUnit);
		try {
			setState(325);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SECONDS:
				enterOuterAlt(_localctx, 1);
				{
				setState(319);
				((DateAddIntervalUnitContext)_localctx).token = match(SECONDS);
				}
				break;
			case MINUTES:
				enterOuterAlt(_localctx, 2);
				{
				setState(320);
				((DateAddIntervalUnitContext)_localctx).token = match(MINUTES);
				}
				break;
			case HOURS:
				enterOuterAlt(_localctx, 3);
				{
				setState(321);
				((DateAddIntervalUnitContext)_localctx).token = match(HOURS);
				}
				break;
			case DAYS:
				enterOuterAlt(_localctx, 4);
				{
				setState(322);
				((DateAddIntervalUnitContext)_localctx).token = match(DAYS);
				}
				break;
			case MONTHS:
				enterOuterAlt(_localctx, 5);
				{
				setState(323);
				((DateAddIntervalUnitContext)_localctx).token = match(MONTHS);
				}
				break;
			case YEARS:
				enterOuterAlt(_localctx, 6);
				{
				setState(324);
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
		enterRule(_localctx, 34, RULE_caseWhenEnd);
		try {
			setState(332);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case END:
				enterOuterAlt(_localctx, 1);
				{
				setState(327);
				((CaseWhenEndContext)_localctx).token = match(END);
				}
				break;
			case ENDASINT:
				enterOuterAlt(_localctx, 2);
				{
				setState(328);
				((CaseWhenEndContext)_localctx).token = match(ENDASINT);
				}
				break;
			case ENDASBOOL:
				enterOuterAlt(_localctx, 3);
				{
				setState(329);
				((CaseWhenEndContext)_localctx).token = match(ENDASBOOL);
				}
				break;
			case ENDASSTR:
				enterOuterAlt(_localctx, 4);
				{
				setState(330);
				((CaseWhenEndContext)_localctx).token = match(ENDASSTR);
				}
				break;
			case ENDASDECIMAL:
				enterOuterAlt(_localctx, 5);
				{
				setState(331);
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
		enterRule(_localctx, 36, RULE_multiOperand);
		try {
			setState(346);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ANYOFPROPS:
				enterOuterAlt(_localctx, 1);
				{
				setState(334);
				((MultiOperandContext)_localctx).token = match(ANYOFPROPS);
				}
				break;
			case ALLOFPROPS:
				enterOuterAlt(_localctx, 2);
				{
				setState(335);
				((MultiOperandContext)_localctx).token = match(ALLOFPROPS);
				}
				break;
			case ANYOFVALUES:
				enterOuterAlt(_localctx, 3);
				{
				setState(336);
				((MultiOperandContext)_localctx).token = match(ANYOFVALUES);
				}
				break;
			case ALLOFVALUES:
				enterOuterAlt(_localctx, 4);
				{
				setState(337);
				((MultiOperandContext)_localctx).token = match(ALLOFVALUES);
				}
				break;
			case ANYOFPARAMS:
				enterOuterAlt(_localctx, 5);
				{
				setState(338);
				((MultiOperandContext)_localctx).token = match(ANYOFPARAMS);
				}
				break;
			case ANYOFIPARAMS:
				enterOuterAlt(_localctx, 6);
				{
				setState(339);
				((MultiOperandContext)_localctx).token = match(ANYOFIPARAMS);
				}
				break;
			case ALLOFPARAMS:
				enterOuterAlt(_localctx, 7);
				{
				setState(340);
				((MultiOperandContext)_localctx).token = match(ALLOFPARAMS);
				}
				break;
			case ALLOFIPARAMS:
				enterOuterAlt(_localctx, 8);
				{
				setState(341);
				((MultiOperandContext)_localctx).token = match(ALLOFIPARAMS);
				}
				break;
			case ANYOFMODELS:
				enterOuterAlt(_localctx, 9);
				{
				setState(342);
				((MultiOperandContext)_localctx).token = match(ANYOFMODELS);
				}
				break;
			case ALLOFMODELS:
				enterOuterAlt(_localctx, 10);
				{
				setState(343);
				((MultiOperandContext)_localctx).token = match(ALLOFMODELS);
				}
				break;
			case ANYOFEXPRESSIONS:
				enterOuterAlt(_localctx, 11);
				{
				setState(344);
				((MultiOperandContext)_localctx).token = match(ANYOFEXPRESSIONS);
				}
				break;
			case ALLOFEXPRESSIONS:
				enterOuterAlt(_localctx, 12);
				{
				setState(345);
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
		enterRule(_localctx, 38, RULE_membershipOperator);
		try {
			setState(350);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IN:
				enterOuterAlt(_localctx, 1);
				{
				setState(348);
				((MembershipOperatorContext)_localctx).token = match(IN);
				}
				break;
			case NOTIN:
				enterOuterAlt(_localctx, 2);
				{
				setState(349);
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
		enterRule(_localctx, 40, RULE_membershipOperand);
		try {
			setState(357);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case VALUES:
				enterOuterAlt(_localctx, 1);
				{
				setState(352);
				((MembershipOperandContext)_localctx).token = match(VALUES);
				}
				break;
			case PROPS:
				enterOuterAlt(_localctx, 2);
				{
				setState(353);
				((MembershipOperandContext)_localctx).token = match(PROPS);
				}
				break;
			case PARAMS:
				enterOuterAlt(_localctx, 3);
				{
				setState(354);
				((MembershipOperandContext)_localctx).token = match(PARAMS);
				}
				break;
			case IPARAMS:
				enterOuterAlt(_localctx, 4);
				{
				setState(355);
				((MembershipOperandContext)_localctx).token = match(IPARAMS);
				}
				break;
			case MODEL:
				enterOuterAlt(_localctx, 5);
				{
				setState(356);
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
		enterRule(_localctx, 42, RULE_join);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(359);
			joinOperator();
			setState(361);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(360);
				((JoinContext)_localctx).alias = match(AS);
				}
			}

			setState(363);
			joinCondition();
			setState(365);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==JOIN || _la==LEFTJOIN) {
				{
				setState(364);
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
		enterRule(_localctx, 44, RULE_joinOperator);
		try {
			setState(369);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case JOIN:
				enterOuterAlt(_localctx, 1);
				{
				setState(367);
				((JoinOperatorContext)_localctx).token = match(JOIN);
				}
				break;
			case LEFTJOIN:
				enterOuterAlt(_localctx, 2);
				{
				setState(368);
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
		enterRule(_localctx, 46, RULE_joinCondition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(371);
			match(ON);
			setState(372);
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
		enterRule(_localctx, 48, RULE_groupBy);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(374);
			match(GROUPBY);
			setState(375);
			((GroupByContext)_localctx).operand = singleOperand();
			setState(377);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==GROUPBY) {
				{
				setState(376);
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
		enterRule(_localctx, 50, RULE_anyYield);
		int _la;
		try {
			setState(393);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
			case 1:
				_localctx = new Yield1Context(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(379);
				match(YIELD);
				setState(380);
				((Yield1Context)_localctx).operand = yieldOperand();
				setState(381);
				yield1Model();
				}
				break;
			case 2:
				_localctx = new YieldManyContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(384);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==YIELDALL) {
					{
					setState(383);
					match(YIELDALL);
					}
				}

				setState(389);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==YIELD) {
					{
					{
					setState(386);
					aliasedYield();
					}
					}
					setState(391);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(392);
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
		enterRule(_localctx, 52, RULE_aliasedYield);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(395);
			match(YIELD);
			setState(396);
			((AliasedYieldContext)_localctx).operand = yieldOperand();
			setState(397);
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

	public final YieldOperandContext yieldOperand() throws RecognitionException {
		YieldOperandContext _localctx = new YieldOperandContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_yieldOperand);
		try {
			setState(404);
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
				setState(399);
				singleOperand();
				}
				break;
			case COUNTALL:
				_localctx = new YieldOperand_CountAllContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(400);
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
				setState(401);
				((YieldOperandFunctionContext)_localctx).funcName = yieldOperandFunctionName();
				setState(402);
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
		enterRule(_localctx, 56, RULE_yieldOperandFunctionName);
		try {
			setState(414);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MAXOF:
				enterOuterAlt(_localctx, 1);
				{
				setState(406);
				((YieldOperandFunctionNameContext)_localctx).token = match(MAXOF);
				}
				break;
			case MINOF:
				enterOuterAlt(_localctx, 2);
				{
				setState(407);
				((YieldOperandFunctionNameContext)_localctx).token = match(MINOF);
				}
				break;
			case SUMOF:
				enterOuterAlt(_localctx, 3);
				{
				setState(408);
				((YieldOperandFunctionNameContext)_localctx).token = match(SUMOF);
				}
				break;
			case COUNTOF:
				enterOuterAlt(_localctx, 4);
				{
				setState(409);
				((YieldOperandFunctionNameContext)_localctx).token = match(COUNTOF);
				}
				break;
			case AVGOF:
				enterOuterAlt(_localctx, 5);
				{
				setState(410);
				((YieldOperandFunctionNameContext)_localctx).token = match(AVGOF);
				}
				break;
			case SUMOFDISTINCT:
				enterOuterAlt(_localctx, 6);
				{
				setState(411);
				((YieldOperandFunctionNameContext)_localctx).token = match(SUMOFDISTINCT);
				}
				break;
			case COUNTOFDISTINCT:
				enterOuterAlt(_localctx, 7);
				{
				setState(412);
				((YieldOperandFunctionNameContext)_localctx).token = match(COUNTOFDISTINCT);
				}
				break;
			case AVGOFDISTINCT:
				enterOuterAlt(_localctx, 8);
				{
				setState(413);
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
		enterRule(_localctx, 58, RULE_yieldAlias);
		try {
			setState(418);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AS:
				enterOuterAlt(_localctx, 1);
				{
				setState(416);
				((YieldAliasContext)_localctx).token = match(AS);
				}
				break;
			case ASREQUIRED:
				enterOuterAlt(_localctx, 2);
				{
				setState(417);
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
		enterRule(_localctx, 60, RULE_yield1Model);
		try {
			setState(422);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MODELASENTITY:
				enterOuterAlt(_localctx, 1);
				{
				setState(420);
				((Yield1ModelContext)_localctx).token = match(MODELASENTITY);
				}
				break;
			case MODELASPRIMITIVE:
				enterOuterAlt(_localctx, 2);
				{
				setState(421);
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
		enterRule(_localctx, 62, RULE_yieldManyModel);
		try {
			setState(426);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MODELASENTITY:
				enterOuterAlt(_localctx, 1);
				{
				setState(424);
				((YieldManyModelContext)_localctx).token = match(MODELASENTITY);
				}
				break;
			case MODELASAGGREGATE:
				enterOuterAlt(_localctx, 2);
				{
				setState(425);
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
		enterRule(_localctx, 64, RULE_model);
		try {
			setState(431);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MODEL:
				enterOuterAlt(_localctx, 1);
				{
				setState(428);
				((ModelContext)_localctx).token = match(MODEL);
				}
				break;
			case MODELASENTITY:
				enterOuterAlt(_localctx, 2);
				{
				setState(429);
				((ModelContext)_localctx).token = match(MODELASENTITY);
				}
				break;
			case MODELASAGGREGATE:
				enterOuterAlt(_localctx, 3);
				{
				setState(430);
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
		int _startState = 66;
		enterRecursionRule(_localctx, 66, RULE_standaloneCondition, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			_localctx = new StandaloneCondition_PredicateContext(_localctx);
			_ctx = _localctx;
			_prevctx = _localctx;

			setState(434);
			predicate();
			}
			_ctx.stop = _input.LT(-1);
			setState(444);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,47,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(442);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,46,_ctx) ) {
					case 1:
						{
						_localctx = new AndStandaloneConditionContext(new StandaloneConditionContext(_parentctx, _parentState));
						((AndStandaloneConditionContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_standaloneCondition);
						setState(436);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(437);
						match(AND);
						setState(438);
						((AndStandaloneConditionContext)_localctx).right = standaloneCondition(3);
						}
						break;
					case 2:
						{
						_localctx = new OrStandaloneConditionContext(new StandaloneConditionContext(_parentctx, _parentState));
						((OrStandaloneConditionContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_standaloneCondition);
						setState(439);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(440);
						match(OR);
						setState(441);
						((OrStandaloneConditionContext)_localctx).right = standaloneCondition(2);
						}
						break;
					}
					} 
				}
				setState(446);
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
		enterRule(_localctx, 68, RULE_orderByOperand);
		try {
			setState(453);
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
				setState(447);
				singleOperand();
				setState(448);
				order();
				}
				break;
			case YIELD:
				_localctx = new OrderByOperand_YieldContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(450);
				match(YIELD);
				setState(451);
				order();
				}
				break;
			case ORDER:
				_localctx = new OrderByOperand_OrderingModelContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(452);
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
		enterRule(_localctx, 70, RULE_order);
		try {
			setState(457);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ASC:
				enterOuterAlt(_localctx, 1);
				{
				setState(455);
				((OrderContext)_localctx).token = match(ASC);
				}
				break;
			case DESC:
				enterOuterAlt(_localctx, 2);
				{
				setState(456);
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
		case 33:
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\u0083\u01ce\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\3\2\3\2\3\2\3\3\3\3\5\3P\n\3\3\3\5\3S\n"+
		"\3\3\3\5\3V\n\3\3\3\5\3Y\n\3\3\3\3\3\3\3\3\3\3\3\3\3\7\3a\n\3\f\3\16\3"+
		"d\13\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\6\3n\n\3\r\3\16\3o\3\3\3\3\5\3"+
		"t\n\3\3\4\3\4\5\4x\n\4\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6"+
		"\3\6\5\6\u0087\n\6\3\6\3\6\3\6\3\6\3\6\3\6\7\6\u008f\n\6\f\6\16\6\u0092"+
		"\13\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\5\7\u00b0\n\7\5\7\u00b2"+
		"\n\7\3\b\3\b\5\b\u00b6\n\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\5\t\u00c0\n"+
		"\t\3\n\3\n\5\n\u00c4\n\n\3\13\3\13\3\13\3\13\3\13\3\13\5\13\u00cc\n\13"+
		"\3\f\3\f\5\f\u00d0\n\f\3\r\3\r\3\r\3\r\7\r\u00d6\n\r\f\r\16\r\u00d9\13"+
		"\r\3\16\3\16\3\16\3\16\3\16\5\16\u00e0\n\16\3\17\3\17\3\17\3\17\5\17\u00e6"+
		"\n\17\3\17\3\17\5\17\u00ea\n\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17"+
		"\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17"+
		"\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\7\17\u010c\n\17\f\17"+
		"\16\17\u010f\13\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3"+
		"\17\7\17\u011c\n\17\f\17\16\17\u011f\13\17\3\17\3\17\5\17\u0123\n\17\3"+
		"\17\3\17\3\17\3\17\3\17\3\17\5\17\u012b\n\17\3\20\3\20\3\20\3\20\3\20"+
		"\3\20\3\20\3\20\3\20\3\20\3\20\5\20\u0138\n\20\3\21\3\21\3\21\3\21\3\21"+
		"\3\21\5\21\u0140\n\21\3\22\3\22\3\22\3\22\3\22\3\22\5\22\u0148\n\22\3"+
		"\23\3\23\3\23\3\23\3\23\5\23\u014f\n\23\3\24\3\24\3\24\3\24\3\24\3\24"+
		"\3\24\3\24\3\24\3\24\3\24\3\24\5\24\u015d\n\24\3\25\3\25\5\25\u0161\n"+
		"\25\3\26\3\26\3\26\3\26\3\26\5\26\u0168\n\26\3\27\3\27\5\27\u016c\n\27"+
		"\3\27\3\27\5\27\u0170\n\27\3\30\3\30\5\30\u0174\n\30\3\31\3\31\3\31\3"+
		"\32\3\32\3\32\5\32\u017c\n\32\3\33\3\33\3\33\3\33\3\33\5\33\u0183\n\33"+
		"\3\33\7\33\u0186\n\33\f\33\16\33\u0189\13\33\3\33\5\33\u018c\n\33\3\34"+
		"\3\34\3\34\3\34\3\35\3\35\3\35\3\35\3\35\5\35\u0197\n\35\3\36\3\36\3\36"+
		"\3\36\3\36\3\36\3\36\3\36\5\36\u01a1\n\36\3\37\3\37\5\37\u01a5\n\37\3"+
		" \3 \5 \u01a9\n \3!\3!\5!\u01ad\n!\3\"\3\"\3\"\5\"\u01b2\n\"\3#\3#\3#"+
		"\3#\3#\3#\3#\3#\3#\7#\u01bd\n#\f#\16#\u01c0\13#\3$\3$\3$\3$\3$\3$\5$\u01c8"+
		"\n$\3%\3%\5%\u01cc\n%\3%\2\4\nD&\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36"+
		" \"$&(*,.\60\62\64\668:<>@BDFH\2\2\2\u022d\2J\3\2\2\2\4s\3\2\2\2\6w\3"+
		"\2\2\2\by\3\2\2\2\n\u0086\3\2\2\2\f\u00b1\3\2\2\2\16\u00b5\3\2\2\2\20"+
		"\u00bf\3\2\2\2\22\u00c3\3\2\2\2\24\u00cb\3\2\2\2\26\u00cf\3\2\2\2\30\u00d1"+
		"\3\2\2\2\32\u00df\3\2\2\2\34\u012a\3\2\2\2\36\u0137\3\2\2\2 \u013f\3\2"+
		"\2\2\"\u0147\3\2\2\2$\u014e\3\2\2\2&\u015c\3\2\2\2(\u0160\3\2\2\2*\u0167"+
		"\3\2\2\2,\u0169\3\2\2\2.\u0173\3\2\2\2\60\u0175\3\2\2\2\62\u0178\3\2\2"+
		"\2\64\u018b\3\2\2\2\66\u018d\3\2\2\28\u0196\3\2\2\2:\u01a0\3\2\2\2<\u01a4"+
		"\3\2\2\2>\u01a8\3\2\2\2@\u01ac\3\2\2\2B\u01b1\3\2\2\2D\u01b3\3\2\2\2F"+
		"\u01c7\3\2\2\2H\u01cb\3\2\2\2JK\5\4\3\2KL\7\2\2\3L\3\3\2\2\2MO\7q\2\2"+
		"NP\7\25\2\2ON\3\2\2\2OP\3\2\2\2PR\3\2\2\2QS\5,\27\2RQ\3\2\2\2RS\3\2\2"+
		"\2SU\3\2\2\2TV\5\b\5\2UT\3\2\2\2UV\3\2\2\2VX\3\2\2\2WY\5\62\32\2XW\3\2"+
		"\2\2XY\3\2\2\2YZ\3\2\2\2Zt\5\6\4\2[\\\7\66\2\2\\b\58\35\2]^\5\32\16\2"+
		"^_\58\35\2_a\3\2\2\2`]\3\2\2\2ad\3\2\2\2b`\3\2\2\2bc\3\2\2\2ce\3\2\2\2"+
		"db\3\2\2\2ef\7R\2\2ft\3\2\2\2gh\7\37\2\2hi\5D#\2ij\7R\2\2jt\3\2\2\2km"+
		"\7h\2\2ln\5F$\2ml\3\2\2\2no\3\2\2\2om\3\2\2\2op\3\2\2\2pq\3\2\2\2qr\7"+
		"R\2\2rt\3\2\2\2sM\3\2\2\2s[\3\2\2\2sg\3\2\2\2sk\3\2\2\2t\5\3\2\2\2ux\5"+
		"B\"\2vx\5\64\33\2wu\3\2\2\2wv\3\2\2\2x\7\3\2\2\2yz\7{\2\2z{\5\n\6\2{\t"+
		"\3\2\2\2|}\b\6\1\2}\u0087\5\f\7\2~\177\7\32\2\2\177\u0080\5\n\6\2\u0080"+
		"\u0081\7,\2\2\u0081\u0087\3\2\2\2\u0082\u0083\7[\2\2\u0083\u0084\5\n\6"+
		"\2\u0084\u0085\7,\2\2\u0085\u0087\3\2\2\2\u0086|\3\2\2\2\u0086~\3\2\2"+
		"\2\u0086\u0082\3\2\2\2\u0087\u0090\3\2\2\2\u0088\u0089\f\6\2\2\u0089\u008a"+
		"\7\r\2\2\u008a\u008f\5\n\6\7\u008b\u008c\f\5\2\2\u008c\u008d\7f\2\2\u008d"+
		"\u008f\5\n\6\6\u008e\u0088\3\2\2\2\u008e\u008b\3\2\2\2\u008f\u0092\3\2"+
		"\2\2\u0090\u008e\3\2\2\2\u0090\u0091\3\2\2\2\u0091\13\3\2\2\2\u0092\u0090"+
		"\3\2\2\2\u0093\u0094\5\22\n\2\u0094\u0095\5\16\b\2\u0095\u00b2\3\2\2\2"+
		"\u0096\u0097\5\22\n\2\u0097\u0098\5\24\13\2\u0098\u0099\5\22\n\2\u0099"+
		"\u00b2\3\2\2\2\u009a\u009b\5\22\n\2\u009b\u009c\5\24\13\2\u009c\u009d"+
		"\5\26\f\2\u009d\u00b2\3\2\2\2\u009e\u009f\5\22\n\2\u009f\u00a0\5\20\t"+
		"\2\u00a0\u00a1\5\22\n\2\u00a1\u00b2\3\2\2\2\u00a2\u00a3\5\22\n\2\u00a3"+
		"\u00a4\5(\25\2\u00a4\u00a5\5*\26\2\u00a5\u00b2\3\2\2\2\u00a6\u00b0\7\63"+
		"\2\2\u00a7\u00b0\7\\\2\2\u00a8\u00b0\7\65\2\2\u00a9\u00b0\7^\2\2\u00aa"+
		"\u00b0\7\64\2\2\u00ab\u00b0\7]\2\2\u00ac\u00b0\7%\2\2\u00ad\u00b0\7 \2"+
		"\2\u00ae\u00b0\7Z\2\2\u00af\u00a6\3\2\2\2\u00af\u00a7\3\2\2\2\u00af\u00a8"+
		"\3\2\2\2\u00af\u00a9\3\2\2\2\u00af\u00aa\3\2\2\2\u00af\u00ab\3\2\2\2\u00af"+
		"\u00ac\3\2\2\2\u00af\u00ad\3\2\2\2\u00af\u00ae\3\2\2\2\u00b0\u00b2\3\2"+
		"\2\2\u00b1\u0093\3\2\2\2\u00b1\u0096\3\2\2\2\u00b1\u009a\3\2\2\2\u00b1"+
		"\u009e\3\2\2\2\u00b1\u00a2\3\2\2\2\u00b1\u00af\3\2\2\2\u00b2\r\3\2\2\2"+
		"\u00b3\u00b6\7D\2\2\u00b4\u00b6\7C\2\2\u00b5\u00b3\3\2\2\2\u00b5\u00b4"+
		"\3\2\2\2\u00b6\17\3\2\2\2\u00b7\u00c0\7I\2\2\u00b8\u00c0\7>\2\2\u00b9"+
		"\u00c0\7J\2\2\u00ba\u00c0\7?\2\2\u00bb\u00c0\7b\2\2\u00bc\u00c0\7c\2\2"+
		"\u00bd\u00c0\7`\2\2\u00be\u00c0\7_\2\2\u00bf\u00b7\3\2\2\2\u00bf\u00b8"+
		"\3\2\2\2\u00bf\u00b9\3\2\2\2\u00bf\u00ba\3\2\2\2\u00bf\u00bb\3\2\2\2\u00bf"+
		"\u00bc\3\2\2\2\u00bf\u00bd\3\2\2\2\u00bf\u00be\3\2\2\2\u00c0\21\3\2\2"+
		"\2\u00c1\u00c4\5\34\17\2\u00c2\u00c4\5&\24\2\u00c3\u00c1\3\2\2\2\u00c3"+
		"\u00c2\3\2\2\2\u00c4\23\3\2\2\2\u00c5\u00cc\7\62\2\2\u00c6\u00cc\7:\2"+
		"\2\u00c7\u00cc\7L\2\2\u00c8\u00cc\78\2\2\u00c9\u00cc\7G\2\2\u00ca\u00cc"+
		"\7Y\2\2\u00cb\u00c5\3\2\2\2\u00cb\u00c6\3\2\2\2\u00cb\u00c7\3\2\2\2\u00cb"+
		"\u00c8\3\2\2\2\u00cb\u00c9\3\2\2\2\u00cb\u00ca\3\2\2\2\u00cc\25\3\2\2"+
		"\2\u00cd\u00d0\7\6\2\2\u00ce\u00d0\7\16\2\2\u00cf\u00cd\3\2\2\2\u00cf"+
		"\u00ce\3\2\2\2\u00d0\27\3\2\2\2\u00d1\u00d7\5\34\17\2\u00d2\u00d3\5\32"+
		"\16\2\u00d3\u00d4\5\34\17\2\u00d4\u00d6\3\2\2\2\u00d5\u00d2\3\2\2\2\u00d6"+
		"\u00d9\3\2\2\2\u00d7\u00d5\3\2\2\2\u00d7\u00d8\3\2\2\2\u00d8\31\3\2\2"+
		"\2\u00d9\u00d7\3\2\2\2\u00da\u00e0\7\4\2\2\u00db\u00e0\7r\2\2\u00dc\u00e0"+
		"\7+\2\2\u00dd\u00e0\7X\2\2\u00de\u00e0\7Q\2\2\u00df\u00da\3\2\2\2\u00df"+
		"\u00db\3\2\2\2\u00df\u00dc\3\2\2\2\u00df\u00dd\3\2\2\2\u00df\u00de\3\2"+
		"\2\2\u00e0\33\3\2\2\2\u00e1\u012b\7l\2\2\u00e2\u012b\7\67\2\2\u00e3\u00e6"+
		"\7x\2\2\u00e4\u00e6\7E\2\2\u00e5\u00e3\3\2\2\2\u00e5\u00e4\3\2\2\2\u00e6"+
		"\u012b\3\2\2\2\u00e7\u00ea\7j\2\2\u00e8\u00ea\7A\2\2\u00e9\u00e7\3\2\2"+
		"\2\u00e9\u00e8\3\2\2\2\u00ea\u012b\3\2\2\2\u00eb\u012b\7\66\2\2\u00ec"+
		"\u012b\7R\2\2\u00ed\u00ee\5\36\20\2\u00ee\u00ef\5\34\17\2\u00ef\u012b"+
		"\3\2\2\2\u00f0\u00f1\7=\2\2\u00f1\u00f2\5\34\17\2\u00f2\u00f3\7u\2\2\u00f3"+
		"\u00f4\5\34\17\2\u00f4\u012b\3\2\2\2\u00f5\u012b\7d\2\2\u00f6\u00f7\7"+
		"!\2\2\u00f7\u00f8\5 \21\2\u00f8\u00f9\7\34\2\2\u00f9\u00fa\5\34\17\2\u00fa"+
		"\u00fb\7\r\2\2\u00fb\u00fc\5\34\17\2\u00fc\u012b\3\2\2\2\u00fd\u00fe\7"+
		"\5\2\2\u00fe\u00ff\5\34\17\2\u00ff\u0100\5\"\22\2\u0100\u0101\7v\2\2\u0101"+
		"\u0102\5\34\17\2\u0102\u012b\3\2\2\2\u0103\u0104\7n\2\2\u0104\u0105\5"+
		"\34\17\2\u0105\u0106\7v\2\2\u0106\u012b\3\2\2\2\u0107\u0108\7\36\2\2\u0108"+
		"\u010d\5\34\17\2\u0109\u010a\7|\2\2\u010a\u010c\5\34\17\2\u010b\u0109"+
		"\3\2\2\2\u010c\u010f\3\2\2\2\u010d\u010b\3\2\2\2\u010d\u010e\3\2\2\2\u010e"+
		"\u0110\3\2\2\2\u010f\u010d\3\2\2\2\u0110\u0111\7,\2\2\u0111\u012b\3\2"+
		"\2\2\u0112\u0113\7\35\2\2\u0113\u0114\5\n\6\2\u0114\u0115\7u\2\2\u0115"+
		"\u011d\5\34\17\2\u0116\u0117\7z\2\2\u0117\u0118\5\n\6\2\u0118\u0119\7"+
		"u\2\2\u0119\u011a\5\34\17\2\u011a\u011c\3\2\2\2\u011b\u0116\3\2\2\2\u011c"+
		"\u011f\3\2\2\2\u011d\u011b\3\2\2\2\u011d\u011e\3\2\2\2\u011e\u0122\3\2"+
		"\2\2\u011f\u011d\3\2\2\2\u0120\u0121\7i\2\2\u0121\u0123\5\34\17\2\u0122"+
		"\u0120\3\2\2\2\u0122\u0123\3\2\2\2\u0123\u0124\3\2\2\2\u0124\u0125\5$"+
		"\23\2\u0125\u012b\3\2\2\2\u0126\u0127\7\33\2\2\u0127\u0128\5\30\r\2\u0128"+
		"\u0129\7\61\2\2\u0129\u012b\3\2\2\2\u012a\u00e1\3\2\2\2\u012a\u00e2\3"+
		"\2\2\2\u012a\u00e5\3\2\2\2\u012a\u00e9\3\2\2\2\u012a\u00eb\3\2\2\2\u012a"+
		"\u00ec\3\2\2\2\u012a\u00ed\3\2\2\2\u012a\u00f0\3\2\2\2\u012a\u00f5\3\2"+
		"\2\2\u012a\u00f6\3\2\2\2\u012a\u00fd\3\2\2\2\u012a\u0103\3\2\2\2\u012a"+
		"\u0107\3\2\2\2\u012a\u0112\3\2\2\2\u012a\u0126\3\2\2\2\u012b\35\3\2\2"+
		"\2\u012c\u0138\7w\2\2\u012d\u0138\7K\2\2\u012e\u0138\7o\2\2\u012f\u0138"+
		"\7O\2\2\u0130\u0138\7;\2\2\u0131\u0138\7\'\2\2\u0132\u0138\7V\2\2\u0133"+
		"\u0138\7}\2\2\u0134\u0138\7(\2\2\u0135\u0138\7\3\2\2\u0136\u0138\7&\2"+
		"\2\u0137\u012c\3\2\2\2\u0137\u012d\3\2\2\2\u0137\u012e\3\2\2\2\u0137\u012f"+
		"\3\2\2\2\u0137\u0130\3\2\2\2\u0137\u0131\3\2\2\2\u0137\u0132\3\2\2\2\u0137"+
		"\u0133\3\2\2\2\u0137\u0134\3\2\2\2\u0137\u0135\3\2\2\2\u0137\u0136\3\2"+
		"\2\2\u0138\37\3\2\2\2\u0139\u0140\7p\2\2\u013a\u0140\7P\2\2\u013b\u0140"+
		"\7<\2\2\u013c\u0140\7)\2\2\u013d\u0140\7W\2\2\u013e\u0140\7~\2\2\u013f"+
		"\u0139\3\2\2\2\u013f\u013a\3\2\2\2\u013f\u013b\3\2\2\2\u013f\u013c\3\2"+
		"\2\2\u013f\u013d\3\2\2\2\u013f\u013e\3\2\2\2\u0140!\3\2\2\2\u0141\u0148"+
		"\7p\2\2\u0142\u0148\7P\2\2\u0143\u0148\7<\2\2\u0144\u0148\7)\2\2\u0145"+
		"\u0148\7W\2\2\u0146\u0148\7~\2\2\u0147\u0141\3\2\2\2\u0147\u0142\3\2\2"+
		"\2\u0147\u0143\3\2\2\2\u0147\u0144\3\2\2\2\u0147\u0145\3\2\2\2\u0147\u0146"+
		"\3\2\2\2\u0148#\3\2\2\2\u0149\u014f\7,\2\2\u014a\u014f\7/\2\2\u014b\u014f"+
		"\7-\2\2\u014c\u014f\7\60\2\2\u014d\u014f\7.\2\2\u014e\u0149\3\2\2\2\u014e"+
		"\u014a\3\2\2\2\u014e\u014b\3\2\2\2\u014e\u014c\3\2\2\2\u014e\u014d\3\2"+
		"\2\2\u014f%\3\2\2\2\u0150\u015d\7\23\2\2\u0151\u015d\7\13\2\2\u0152\u015d"+
		"\7\24\2\2\u0153\u015d\7\f\2\2\u0154\u015d\7\22\2\2\u0155\u015d\7\20\2"+
		"\2\u0156\u015d\7\n\2\2\u0157\u015d\7\b\2\2\u0158\u015d\7\21\2\2\u0159"+
		"\u015d\7\t\2\2\u015a\u015d\7\17\2\2\u015b\u015d\7\7\2\2\u015c\u0150\3"+
		"\2\2\2\u015c\u0151\3\2\2\2\u015c\u0152\3\2\2\2\u015c\u0153\3\2\2\2\u015c"+
		"\u0154\3\2\2\2\u015c\u0155\3\2\2\2\u015c\u0156\3\2\2\2\u015c\u0157\3\2"+
		"\2\2\u015c\u0158\3\2\2\2\u015c\u0159\3\2\2\2\u015c\u015a\3\2\2\2\u015c"+
		"\u015b\3\2\2\2\u015d\'\3\2\2\2\u015e\u0161\7@\2\2\u015f\u0161\7a\2\2\u0160"+
		"\u015e\3\2\2\2\u0160\u015f\3\2\2\2\u0161)\3\2\2\2\u0162\u0168\7y\2\2\u0163"+
		"\u0168\7m\2\2\u0164\u0168\7k\2\2\u0165\u0168\7B\2\2\u0166\u0168\7R\2\2"+
		"\u0167\u0162\3\2\2\2\u0167\u0163\3\2\2\2\u0167\u0164\3\2\2\2\u0167\u0165"+
		"\3\2\2\2\u0167\u0166\3\2\2\2\u0168+\3\2\2\2\u0169\u016b\5.\30\2\u016a"+
		"\u016c\7\25\2\2\u016b\u016a\3\2\2\2\u016b\u016c\3\2\2\2\u016c\u016d\3"+
		"\2\2\2\u016d\u016f\5\60\31\2\u016e\u0170\5,\27\2\u016f\u016e\3\2\2\2\u016f"+
		"\u0170\3\2\2\2\u0170-\3\2\2\2\u0171\u0174\7F\2\2\u0172\u0174\7H\2\2\u0173"+
		"\u0171\3\2\2\2\u0173\u0172\3\2\2\2\u0174/\3\2\2\2\u0175\u0176\7e\2\2\u0176"+
		"\u0177\5\n\6\2\u0177\61\3\2\2\2\u0178\u0179\79\2\2\u0179\u017b\5\34\17"+
		"\2\u017a\u017c\5\62\32\2\u017b\u017a\3\2\2\2\u017b\u017c\3\2\2\2\u017c"+
		"\63\3\2\2\2\u017d\u017e\7\177\2\2\u017e\u017f\58\35\2\u017f\u0180\5> "+
		"\2\u0180\u018c\3\2\2\2\u0181\u0183\7\u0080\2\2\u0182\u0181\3\2\2\2\u0182"+
		"\u0183\3\2\2\2\u0183\u0187\3\2\2\2\u0184\u0186\5\66\34\2\u0185\u0184\3"+
		"\2\2\2\u0186\u0189\3\2\2\2\u0187\u0185\3\2\2\2\u0187\u0188\3\2\2\2\u0188"+
		"\u018a\3\2\2\2\u0189\u0187\3\2\2\2\u018a\u018c\5@!\2\u018b\u017d\3\2\2"+
		"\2\u018b\u0182\3\2\2\2\u018c\65\3\2\2\2\u018d\u018e\7\177\2\2\u018e\u018f"+
		"\58\35\2\u018f\u0190\5<\37\2\u0190\67\3\2\2\2\u0191\u0197\5\34\17\2\u0192"+
		"\u0197\7\"\2\2\u0193\u0194\5:\36\2\u0194\u0195\5\34\17\2\u0195\u0197\3"+
		"\2\2\2\u0196\u0191\3\2\2\2\u0196\u0192\3\2\2\2\u0196\u0193\3\2\2\2\u0197"+
		"9\3\2\2\2\u0198\u01a1\7M\2\2\u0199\u01a1\7N\2\2\u019a\u01a1\7s\2\2\u019b"+
		"\u01a1\7#\2\2\u019c\u01a1\7\30\2\2\u019d\u01a1\7t\2\2\u019e\u01a1\7$\2"+
		"\2\u019f\u01a1\7\31\2\2\u01a0\u0198\3\2\2\2\u01a0\u0199\3\2\2\2\u01a0"+
		"\u019a\3\2\2\2\u01a0\u019b\3\2\2\2\u01a0\u019c\3\2\2\2\u01a0\u019d\3\2"+
		"\2\2\u01a0\u019e\3\2\2\2\u01a0\u019f\3\2\2\2\u01a1;\3\2\2\2\u01a2\u01a5"+
		"\7\25\2\2\u01a3\u01a5\7\27\2\2\u01a4\u01a2\3\2\2\2\u01a4\u01a3\3\2\2\2"+
		"\u01a5=\3\2\2\2\u01a6\u01a9\7T\2\2\u01a7\u01a9\7U\2\2\u01a8\u01a6\3\2"+
		"\2\2\u01a8\u01a7\3\2\2\2\u01a9?\3\2\2\2\u01aa\u01ad\7T\2\2\u01ab\u01ad"+
		"\7S\2\2\u01ac\u01aa\3\2\2\2\u01ac\u01ab\3\2\2\2\u01adA\3\2\2\2\u01ae\u01b2"+
		"\7R\2\2\u01af\u01b2\7T\2\2\u01b0\u01b2\7S\2\2\u01b1\u01ae\3\2\2\2\u01b1"+
		"\u01af\3\2\2\2\u01b1\u01b0\3\2\2\2\u01b2C\3\2\2\2\u01b3\u01b4\b#\1\2\u01b4"+
		"\u01b5\5\f\7\2\u01b5\u01be\3\2\2\2\u01b6\u01b7\f\4\2\2\u01b7\u01b8\7\r"+
		"\2\2\u01b8\u01bd\5D#\5\u01b9\u01ba\f\3\2\2\u01ba\u01bb\7f\2\2\u01bb\u01bd"+
		"\5D#\4\u01bc\u01b6\3\2\2\2\u01bc\u01b9\3\2\2\2\u01bd\u01c0\3\2\2\2\u01be"+
		"\u01bc\3\2\2\2\u01be\u01bf\3\2\2\2\u01bfE\3\2\2\2\u01c0\u01be\3\2\2\2"+
		"\u01c1\u01c2\5\34\17\2\u01c2\u01c3\5H%\2\u01c3\u01c8\3\2\2\2\u01c4\u01c5"+
		"\7\177\2\2\u01c5\u01c8\5H%\2\u01c6\u01c8\7g\2\2\u01c7\u01c1\3\2\2\2\u01c7"+
		"\u01c4\3\2\2\2\u01c7\u01c6\3\2\2\2\u01c8G\3\2\2\2\u01c9\u01cc\7\26\2\2"+
		"\u01ca\u01cc\7*\2\2\u01cb\u01c9\3\2\2\2\u01cb\u01ca\3\2\2\2\u01ccI\3\2"+
		"\2\2\64ORUXbosw\u0086\u008e\u0090\u00af\u00b1\u00b5\u00bf\u00c3\u00cb"+
		"\u00cf\u00d7\u00df\u00e5\u00e9\u010d\u011d\u0122\u012a\u0137\u013f\u0147"+
		"\u014e\u015c\u0160\u0167\u016b\u016f\u0173\u017b\u0182\u0187\u018b\u0196"+
		"\u01a0\u01a4\u01a8\u01ac\u01b1\u01bc\u01be\u01c7\u01cb";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}