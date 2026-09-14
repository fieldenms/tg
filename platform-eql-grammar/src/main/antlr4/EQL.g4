// This grammar was generated. Timestamp: 2026-04-16T13:54:27.531859656+03:00[Europe/Kyiv]

grammar EQL;

start : query EOF;

query :
      select=SELECT alias=AS? join? where? groupBy? orderBy? selectEnd # Select
    | EXPR first=yieldOperand (operators+=arithmeticalOperator rest+=yieldOperand)* MODEL # StandaloneExpression
    | COND standaloneCondition MODEL # StandaloneCondExpr
    | ORDERBY operands+=orderByOperand+ limit=LIMIT? offset=OFFSET? MODEL # StandaloneOrderBy
;

selectEnd :
      model # SelectEnd_Model
    | anyYield # SelectEnd_AnyYield
;

where :
      WHERE condition
;

condition :
      predicate # PredicateCondition
    | left=condition AND right=condition # AndCondition
    | left=condition OR right=condition # OrCondition
    | BEGIN condition END # CompoundCondition
    | NOTBEGIN condition END # NegatedCompoundCondition
;

predicate :
      left=comparisonOperand unaryComparisonOperator # UnaryPredicate
    | left=comparisonOperand comparisonOperator right=comparisonOperand # ComparisonPredicate
    | left=comparisonOperand comparisonOperator quantifiedOperand # QuantifiedComparisonPredicate
    | left=comparisonOperand likeOperator right=comparisonOperand # LikePredicate
    | left=comparisonOperand membershipOperator membershipOperand # MembershipPredicate
    | (token=EXISTS | token=NOTEXISTS | token=EXISTSANYOF | token=NOTEXISTSANYOF | token=EXISTSALLOF | token=NOTEXISTSALLOF | token=CRITCONDITION | token=CONDITION | token=NEGATEDCONDITION) # SingleConditionPredicate
;

unaryComparisonOperator :
      token=ISNULL
    | token=ISNOTNULL
;

likeOperator :
      token=LIKE
    | token=ILIKE
    | token=LIKEWITHCAST
    | token=ILIKEWITHCAST
    | token=NOTLIKE
    | token=NOTLIKEWITHCAST
    | token=NOTILIKEWITHCAST
    | token=NOTILIKE
;

comparisonOperand :
      expr # ComparisonOperand_Expr
    | multiOperand # ComparisonOperand_Multi
;

comparisonOperator :
      token=EQ
    | token=GT
    | token=LT
    | token=GE
    | token=LE
    | token=NE
;

quantifiedOperand :
      token=ALL
    | token=ANY
;

expr :
      BEGINEXPR first=expr (operators+=arithmeticalOperator rest+=expr)* ENDEXPR # ExprCompound
    | singleOperand # Expr_SingleOperand
;

arithmeticalOperator :
      token=ADD
    | token=SUB
    | token=DIV
    | token=MULT
    | token=MOD
;

singleOperand :
      token=PROP # Prop
    | token=EXTPROP # ExtProp
    | (token=VAL | token=IVAL) # Val
    | (token=PARAM | token=IPARAM) # Param
    | token=EXPR # SingleOperand_Expr
    | token=MODEL # SingleOperand_Model
    | funcName=unaryFunctionName argument=expr # UnaryFunction
    | IFNULL nullable=expr THEN other=expr # IfNull
    | NOW # SingleOperand_Now
    | COUNT unit=dateIntervalUnit BETWEEN endDate=expr AND startDate=expr # DateDiffInterval
    | ADDTIMEINTERVALOF left=expr unit=dateIntervalUnit TO right=expr # DateAddInterval
    | ROUND expr to=TO # Round
    | CEIL expr # Ceil
    | FLOOR expr # Floor
    | CONCAT operands+=expr (WITH operands+=expr)* END # Concat
    | CASEWHEN whens+=condition THEN thens+=expr (WHEN whens+=condition THEN thens+=expr)* (OTHERWISE otherwiseOperand=expr)? caseWhenEnd # CaseWhen
;

unaryFunctionName :
      token=UPPERCASE
    | token=LOWERCASE
    | token=SECONDOF
    | token=MINUTEOF
    | token=HOUROF
    | token=DAYOF
    | token=MONTHOF
    | token=YEAROF
    | token=DAYOFWEEKOF
    | token=ABSOF
    | token=DATEOF
;

dateIntervalUnit :
      token=SECONDS
    | token=MINUTES
    | token=HOURS
    | token=DAYS
    | token=MONTHS
    | token=YEARS
;

caseWhenEnd :
      token=END
    | token=ENDASINT
    | token=ENDASBOOL
    | token=ENDASSTR
    | token=ENDASDECIMAL
;

multiOperand :
      token=ANYOFPROPS
    | token=ALLOFPROPS
    | token=ANYOFVALUES
    | token=ALLOFVALUES
    | token=ANYOFPARAMS
    | token=ANYOFIPARAMS
    | token=ALLOFPARAMS
    | token=ALLOFIPARAMS
    | token=ANYOFMODELS
    | token=ALLOFMODELS
    | token=ANYOFEXPRESSIONS
    | token=ALLOFEXPRESSIONS
;

membershipOperator :
      token=IN
    | token=NOTIN
;

membershipOperand :
      token=VALUES
    | token=PROPS
    | token=PARAMS
    | token=IPARAMS
    | token=MODEL
;

join :
      joinOperator alias=AS? joinCondition join?
;

joinOperator :
      token=JOIN
    | token=LEFTJOIN
;

joinCondition :
      ON condition
;

groupBy :
      (GROUPBY operands+=expr)+
;

anyYield :
      YIELDALL aliasedYield* yieldManyModel # YieldAll
    | YIELD firstYield=yieldOperand yieldTail # YieldSome
;

yieldTail :
      yield1Model # Yield1Tail
    | firstAlias=yieldAlias restYields+=aliasedYield* yieldManyModel # YieldManyTail
;

aliasedYield :
      YIELD yieldOperand yieldAlias
;

yieldOperand :
      singleOperand # YieldOperand_SingleOperand
    | BEGINEXPR first=yieldOperand (operators+=arithmeticalOperator rest+=yieldOperand)* ENDEXPR # YieldOperandExpr
    | COUNTALL # YieldOperand_CountAll
    | funcName=yieldOperandFunctionName argument=expr # YieldOperandFunction
    | CONCATOF argExpr=expr yieldOperandConcatOfOrderBy? SEPARATOR separator=yieldOperandConcatOfSeparator # YieldOperandConcatOf
;

yieldOperandFunctionName :
      token=MAXOF
    | token=MINOF
    | token=SUMOF
    | token=COUNTOF
    | token=AVGOF
    | token=SUMOFDISTINCT
    | token=COUNTOFDISTINCT
    | token=AVGOFDISTINCT
;

yieldOperandConcatOfSeparator :
      token=VAL
    | token=PARAM
;

yieldOperandConcatOfOrderBy :
      ORDERBY operands+=yieldOperandConcatOfOrderByOperand+
;

yieldOperandConcatOfOrderByOperand :
      expr order # YieldOperandConcatOfOrderByOperand_Expr
    | token=ORDER # YieldOperandConcatOfOrderByOperand_OrderingModel
;

yieldAlias :
      token=AS
    | token=ASREQUIRED
;

yield1Model :
      token=MODELASENTITY
    | token=MODELASPRIMITIVE
;

yieldManyModel :
      token=MODELASENTITY
    | token=MODELASAGGREGATE
;

model :
      token=MODEL
    | token=MODELASENTITY
    | token=MODELASAGGREGATE
;

standaloneCondition :
      predicate # StandaloneCondition_Predicate
    | left=standaloneCondition AND right=standaloneCondition # AndStandaloneCondition
    | left=standaloneCondition OR right=standaloneCondition # OrStandaloneCondition
;

orderBy :
      ORDERBY operands+=orderByOperand+ limit=LIMIT? offset=OFFSET?
;

orderByOperand :
      expr order # OrderByOperand_Expr
    | yield=YIELD order # OrderByOperand_Yield
    | token=ORDER # OrderByOperand_OrderingModel
;

order :
      token=ASC
    | token=DESC
;

ABSOF : 'absOf' ;
ADD : 'add' ;
ADDTIMEINTERVALOF : 'addTimeIntervalOf' ;
ALL : 'all' ;
ALLOFEXPRESSIONS : 'allOfExpressions' ;
ALLOFIPARAMS : 'allOfIParams' ;
ALLOFMODELS : 'allOfModels' ;
ALLOFPARAMS : 'allOfParams' ;
ALLOFPROPS : 'allOfProps' ;
ALLOFVALUES : 'allOfValues' ;
AND : 'and' ;
ANY : 'any' ;
ANYOFEXPRESSIONS : 'anyOfExpressions' ;
ANYOFIPARAMS : 'anyOfIParams' ;
ANYOFMODELS : 'anyOfModels' ;
ANYOFPARAMS : 'anyOfParams' ;
ANYOFPROPS : 'anyOfProps' ;
ANYOFVALUES : 'anyOfValues' ;
AS : 'as' ;
ASC : 'asc' ;
ASREQUIRED : 'asRequired' ;
AVGOF : 'avgOf' ;
AVGOFDISTINCT : 'avgOfDistinct' ;
BEGIN : 'begin' ;
BEGINEXPR : 'beginExpr' ;
BETWEEN : 'between' ;
CASEWHEN : 'caseWhen' ;
CEIL : 'ceil' ;
CONCAT : 'concat' ;
CONCATOF : 'concatOf' ;
COND : 'cond' ;
CONDITION : 'condition' ;
COUNT : 'count' ;
COUNTALL : 'countAll' ;
COUNTOF : 'countOf' ;
COUNTOFDISTINCT : 'countOfDistinct' ;
CRITCONDITION : 'critCondition' ;
DATEOF : 'dateOf' ;
DAYOF : 'dayOf' ;
DAYOFWEEKOF : 'dayOfWeekOf' ;
DAYS : 'days' ;
DESC : 'desc' ;
DIV : 'div' ;
END : 'end' ;
ENDASBOOL : 'endAsBool' ;
ENDASDECIMAL : 'endAsDecimal' ;
ENDASINT : 'endAsInt' ;
ENDASSTR : 'endAsStr' ;
ENDEXPR : 'endExpr' ;
EQ : 'eq' ;
EXISTS : 'exists' ;
EXISTSALLOF : 'existsAllOf' ;
EXISTSANYOF : 'existsAnyOf' ;
EXPR : 'expr' ;
EXTPROP : 'extProp' ;
FLOOR : 'floor' ;
GE : 'ge' ;
GROUPBY : 'groupBy' ;
GT : 'gt' ;
HOUROF : 'hourOf' ;
HOURS : 'hours' ;
IFNULL : 'ifNull' ;
ILIKE : 'iLike' ;
ILIKEWITHCAST : 'iLikeWithCast' ;
IN : 'in' ;
IPARAM : 'iParam' ;
IPARAMS : 'iParams' ;
ISNOTNULL : 'isNotNull' ;
ISNULL : 'isNull' ;
IVAL : 'iVal' ;
JOIN : 'join' ;
LE : 'le' ;
LEFTJOIN : 'leftJoin' ;
LIKE : 'like' ;
LIKEWITHCAST : 'likeWithCast' ;
LIMIT : 'limit' ;
LOWERCASE : 'lowerCase' ;
LT : 'lt' ;
MAXOF : 'maxOf' ;
MINOF : 'minOf' ;
MINUTEOF : 'minuteOf' ;
MINUTES : 'minutes' ;
MOD : 'mod' ;
MODEL : 'model' ;
MODELASAGGREGATE : 'modelAsAggregate' ;
MODELASENTITY : 'modelAsEntity' ;
MODELASPRIMITIVE : 'modelAsPrimitive' ;
MONTHOF : 'monthOf' ;
MONTHS : 'months' ;
MULT : 'mult' ;
NE : 'ne' ;
NEGATEDCONDITION : 'negatedCondition' ;
NOTBEGIN : 'notBegin' ;
NOTEXISTS : 'notExists' ;
NOTEXISTSALLOF : 'notExistsAllOf' ;
NOTEXISTSANYOF : 'notExistsAnyOf' ;
NOTILIKE : 'notILike' ;
NOTILIKEWITHCAST : 'notILikeWithCast' ;
NOTIN : 'notIn' ;
NOTLIKE : 'notLike' ;
NOTLIKEWITHCAST : 'notLikeWithCast' ;
NOW : 'now' ;
OFFSET : 'offset' ;
ON : 'on' ;
OR : 'or' ;
ORDER : 'order' ;
ORDERBY : 'orderBy' ;
OTHERWISE : 'otherwise' ;
PARAM : 'param' ;
PARAMS : 'params' ;
PROP : 'prop' ;
PROPS : 'props' ;
ROUND : 'round' ;
SECONDOF : 'secondOf' ;
SECONDS : 'seconds' ;
SELECT : 'select' ;
SEPARATOR : 'separator' ;
SUB : 'sub' ;
SUMOF : 'sumOf' ;
SUMOFDISTINCT : 'sumOfDistinct' ;
THEN : 'then' ;
TO : 'to' ;
UPPERCASE : 'upperCase' ;
VAL : 'val' ;
VALUES : 'values' ;
WHEN : 'when' ;
WHERE : 'where' ;
WITH : 'with' ;
YEAROF : 'yearOf' ;
YEARS : 'years' ;
YIELD : 'yield' ;
YIELDALL : 'yieldAll' ;

WHITESPACE : [ \r\t\n]+ -> channel(HIDDEN) ;
COMMENT : '//' .*? '\n' -> channel(HIDDEN) ;
BLOCK_COMMENT : '/*' .*? '*/' -> channel(HIDDEN) ;

