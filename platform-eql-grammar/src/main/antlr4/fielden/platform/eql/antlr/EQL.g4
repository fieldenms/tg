// This grammar was generated. Timestamp: 2024-02-23T12:29:12.113332856+02:00[Europe/Kyiv]

grammar EQL;

start : query EOF;

query :
      select # Query_Select
    | expression # Query_Expression
;

select :
      SELECT AS? join? where? groupBy? anyYield
    | SELECT AS? join? where? groupBy? model
;

where :
      WHERE condition
;

condition :
      predicate
    | left=condition AND right=condition
    | left=condition OR right=condition
    | BEGIN condition END
;

predicate :
      unaryPredicate # Predicate_UnaryPredicate
    | comparisonPredicate # Predicate_ComparisonPredicate
    | quantifiedComparisonPredicate # Predicate_QuantifiedComparisonPredicate
    | likePredicate # Predicate_LikePredicate
    | membershipPredicate # Predicate_MembershipPredicate
    | singleConditionPredicate # Predicate_SingleConditionPredicate
;

unaryPredicate :
      left=comparisonOperand unaryComparisonOperator
;

comparisonPredicate :
      left=comparisonOperand op=comparisonOperator right=comparisonOperand
;

quantifiedComparisonPredicate :
      left=comparisonOperand op=comparisonOperator quantifiedOperand
;

likePredicate :
      left=comparisonOperand op=likeOperator right=comparisonOperand
;

membershipPredicate :
      left=comparisonOperand op=membershipOperator membershipOperand
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
      singleOperand # ComparisonOperand_SingleOperand
    | expr # ComparisonOperand_Expr
    | multiOperand # ComparisonOperand_MultiOperand
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
      BEGINEXPR exprBody ENDEXPR
;

exprBody :
      singleOperandOrExpr (arithmeticalOperator singleOperandOrExpr)*
;

singleOperandOrExpr :
      singleOperand
    | expr
;

arithmeticalOperator :
      token=ADD
    | token=SUB
    | token=DIV
    | token=MULT
    | token=MOD
;

singleOperand :
      prop
    | extProp
    | val
    | param
    | EXPR
    | MODEL
    | unaryFunction
    | ifNull
    | NOW
    | dateDiffInterval
    | dateAddInterval
    | round
    | concat
    | caseWhen
;

unaryFunction :
      funcName=unaryFunctionName argument=singleOperandOrExpr
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

ifNull :
      IFNULL nullable=singleOperandOrExpr THEN other=singleOperandOrExpr
;

dateDiffInterval :
      COUNT unit=dateDiffIntervalUnit BETWEEN startDate=singleOperandOrExpr AND endDate=singleOperandOrExpr
;

dateDiffIntervalUnit :
      token=SECONDS
    | token=MINUTES
    | token=HOURS
    | token=DAYS
    | token=MONTHS
    | token=YEARS
;

dateAddInterval :
      ADDTIMEINTERVALOF left=singleOperandOrExpr unit=dateAddIntervalUnit TO right=singleOperandOrExpr
;

dateAddIntervalUnit :
      token=SECONDS
    | token=MINUTES
    | token=HOURS
    | token=DAYS
    | token=MONTHS
    | token=YEARS
;

round :
      ROUND singleOperandOrExpr TO
;

concat :
      CONCAT singleOperandOrExpr (WITH singleOperandOrExpr)* END
;

caseWhen :
      CASEWHEN condition THEN singleOperandOrExpr (WHEN condition THEN singleOperandOrExpr)* (OTHERWISE otherwiseOperand=singleOperandOrExpr)? caseWhenEnd
;

caseWhenEnd :
      token=END
    | token=ENDASINT
    | token=ENDASBOOL
    | token=ENDASSTR
    | token=ENDASDECIMAL
;

prop :
      token=PROP
;

extProp :
      token=EXTPROP
;

val :
      token=VAL
    | token=IVAL
;

param :
      token=PARAM
    | token=IPARAM
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

singleConditionPredicate :
      token=EXISTS
    | token=NOTEXISTS
    | token=EXISTSANYOF
    | token=NOTEXISTSANYOF
    | token=EXISTSALLOF
    | token=NOTEXISTSALLOF
    | token=CRITCONDITION
    | token=CONDITION
    | token=NEGATEDCONDITION
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
      GROUPBY operand=singleOperandOrExpr groupBy?
;

anyYield :
      yield1 # AnyYield_Yield1
    | yieldMany # AnyYield_YieldMany
;

yield1 :
      YIELD operand=yieldOperand model_=yield1Model
;

yieldMany :
      YIELDALL? aliasedYield+ model_=yieldManyModel
;

aliasedYield :
      YIELD operand=yieldOperand alias=yieldAlias
;

yieldOperand :
      singleOperandOrExpr
    | COUNTALL
    | yieldOperandFunction
;

yieldOperandFunction :
      funcName=yieldOperandFunctionName argument=singleOperandOrExpr
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

expression :
      EXPR MODEL
;

TO : 'to' ;
ALLOFVALUES : 'allOfValues' ;
LOWERCASE : 'lowerCase' ;
EXTPROP : 'extProp' ;
DIV : 'div' ;
WHERE : 'where' ;
DAYOF : 'dayOf' ;
UPPERCASE : 'upperCase' ;
MAXOF : 'maxOf' ;
ENDEXPR : 'endExpr' ;
SECONDS : 'seconds' ;
COUNTOFDISTINCT : 'countOfDistinct' ;
ADDTIMEINTERVALOF : 'addTimeIntervalOf' ;
ENDASBOOL : 'endAsBool' ;
MODELASENTITY : 'modelAsEntity' ;
MINUTEOF : 'minuteOf' ;
VAL : 'val' ;
EXISTSANYOF : 'existsAnyOf' ;
YIELD : 'yield' ;
OR : 'or' ;
YEAROF : 'yearOf' ;
IFNULL : 'ifNull' ;
ILIKE : 'iLike' ;
NOW : 'now' ;
YIELDALL : 'yieldAll' ;
AVGOFDISTINCT : 'avgOfDistinct' ;
ANYOFPARAMS : 'anyOfParams' ;
NOTLIKE : 'notLike' ;
ABSOF : 'absOf' ;
EXPR : 'expr' ;
SUB : 'sub' ;
AS : 'as' ;
END : 'end' ;
NOTILIKE : 'notILike' ;
ANYOFEXPRESSIONS : 'anyOfExpressions' ;
MINOF : 'minOf' ;
BEGINEXPR : 'beginExpr' ;
MOD : 'mod' ;
NE : 'ne' ;
WHEN : 'when' ;
IN : 'in' ;
NOTEXISTSALLOF : 'notExistsAllOf' ;
PARAM : 'param' ;
JOIN : 'join' ;
ILIKEWITHCAST : 'iLikeWithCast' ;
SELECT : 'select' ;
SECONDOF : 'secondOf' ;
MINUTES : 'minutes' ;
LE : 'le' ;
DAYS : 'days' ;
ENDASDECIMAL : 'endAsDecimal' ;
MONTHS : 'months' ;
BEGIN : 'begin' ;
COUNTOF : 'countOf' ;
EXISTS : 'exists' ;
WITH : 'with' ;
ANYOFPROPS : 'anyOfProps' ;
PARAMS : 'params' ;
ROUND : 'round' ;
YEARS : 'years' ;
ANYOFMODELS : 'anyOfModels' ;
NOTEXISTSANYOF : 'notExistsAnyOf' ;
ALL : 'all' ;
IPARAM : 'iParam' ;
MULT : 'mult' ;
ON : 'on' ;
NEGATEDCONDITION : 'negatedCondition' ;
ALLOFIPARAMS : 'allOfIParams' ;
NOTILIKEWITHCAST : 'notILikeWithCast' ;
EQ : 'eq' ;
COUNT : 'count' ;
NOTEXISTS : 'notExists' ;
PROPS : 'props' ;
SUMOF : 'sumOf' ;
ASREQUIRED : 'asRequired' ;
IVAL : 'iVal' ;
IPARAMS : 'iParams' ;
MODEL : 'model' ;
DATEOF : 'dateOf' ;
NOTLIKEWITHCAST : 'notLikeWithCast' ;
LIKE : 'like' ;
SUMOFDISTINCT : 'sumOfDistinct' ;
VALUES : 'values' ;
THEN : 'then' ;
ALLOFEXPRESSIONS : 'allOfExpressions' ;
EXISTSALLOF : 'existsAllOf' ;
OTHERWISE : 'otherwise' ;
GROUPBY : 'groupBy' ;
ALLOFPARAMS : 'allOfParams' ;
LEFTJOIN : 'leftJoin' ;
PROP : 'prop' ;
ALLOFPROPS : 'allOfProps' ;
BETWEEN : 'between' ;
COUNTALL : 'countAll' ;
MODELASAGGREGATE : 'modelAsAggregate' ;
CONCAT : 'concat' ;
ANYOFIPARAMS : 'anyOfIParams' ;
ENDASINT : 'endAsInt' ;
MODELASPRIMITIVE : 'modelAsPrimitive' ;
AND : 'and' ;
ISNULL : 'isNull' ;
MONTHOF : 'monthOf' ;
NOTIN : 'notIn' ;
AVGOF : 'avgOf' ;
DAYOFWEEKOF : 'dayOfWeekOf' ;
LIKEWITHCAST : 'likeWithCast' ;
ALLOFMODELS : 'allOfModels' ;
LT : 'lt' ;
ANY : 'any' ;
ENDASSTR : 'endAsStr' ;
HOUROF : 'hourOf' ;
ADD : 'add' ;
CONDITION : 'condition' ;
GE : 'ge' ;
ISNOTNULL : 'isNotNull' ;
CRITCONDITION : 'critCondition' ;
ANYOFVALUES : 'anyOfValues' ;
GT : 'gt' ;
HOURS : 'hours' ;
CASEWHEN : 'caseWhen' ;

WHITESPACE : [ \r\t\n]+ -> skip ;
COMMENT : '//' .*? '\n' -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;


