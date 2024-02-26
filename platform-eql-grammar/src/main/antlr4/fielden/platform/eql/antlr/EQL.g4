// This grammar was generated. Timestamp: 2024-02-26T13:31:52.325163479+02:00[Europe/Kyiv]

grammar EQL;

start : query EOF;

query :
      select # Query_Select
    | standaloneExpression # Query_StandaloneExpression
    | standaloneCondExpr # Query_StandaloneCondExpr
    | orderBy # Query_OrderBy
;

select :
      selectFrom # Select_SelectFrom
    | sourcelessSelect # Select_SourcelessSelect
;

selectFrom :
      selectSource alias=AS? join? where? groupBy? selectEnd
;

selectSource :
      token=SELECT
;

sourcelessSelect :
      SELECT groupBy? selectEnd
;

selectEnd :
      anyYield # SelectEnd_AnyYield
    | model # SelectEnd_Model
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

standaloneExpression :
      EXPR operand=yieldOperand (arithmeticalOperator yieldOperand)* MODEL
;

standaloneCondExpr :
      COND standaloneCondition MODEL
;

standaloneCondition :
      predicate
    | left=standaloneCondition AND right=standaloneCondition
    | left=standaloneCondition OR right=standaloneCondition
;

orderBy :
      ORDERBY orderByOperand+ MODEL
;

orderByOperand :
      singleOperandOrExpr order
    | YIELD order
    | ORDER
;

order :
      token=ASC
    | token=DESC
;

NOTLIKE : 'notLike' ;
VAL : 'val' ;
PARAMS : 'params' ;
IN : 'in' ;
MONTHS : 'months' ;
MODELASENTITY : 'modelAsEntity' ;
NOTLIKEWITHCAST : 'notLikeWithCast' ;
OTHERWISE : 'otherwise' ;
GT : 'gt' ;
NOW : 'now' ;
UPPERCASE : 'upperCase' ;
LE : 'le' ;
NOTEXISTSANYOF : 'notExistsAnyOf' ;
MINUTES : 'minutes' ;
WHEN : 'when' ;
ALLOFVALUES : 'allOfValues' ;
NOTIN : 'notIn' ;
NE : 'ne' ;
ANYOFEXPRESSIONS : 'anyOfExpressions' ;
YIELDALL : 'yieldAll' ;
COND : 'cond' ;
SECONDOF : 'secondOf' ;
MONTHOF : 'monthOf' ;
SUB : 'sub' ;
ANYOFPARAMS : 'anyOfParams' ;
NOTILIKEWITHCAST : 'notILikeWithCast' ;
CONDITION : 'condition' ;
PROP : 'prop' ;
SUMOFDISTINCT : 'sumOfDistinct' ;
HOUROF : 'hourOf' ;
AVGOF : 'avgOf' ;
ENDEXPR : 'endExpr' ;
LIKEWITHCAST : 'likeWithCast' ;
LT : 'lt' ;
SELECT : 'select' ;
ANYOFMODELS : 'anyOfModels' ;
JOIN : 'join' ;
EQ : 'eq' ;
MODELASAGGREGATE : 'modelAsAggregate' ;
ENDASBOOL : 'endAsBool' ;
AS : 'as' ;
TO : 'to' ;
ISNOTNULL : 'isNotNull' ;
WITH : 'with' ;
DAYS : 'days' ;
IPARAM : 'iParam' ;
END : 'end' ;
BETWEEN : 'between' ;
ALL : 'all' ;
LEFTJOIN : 'leftJoin' ;
MULT : 'mult' ;
PARAM : 'param' ;
COUNTOFDISTINCT : 'countOfDistinct' ;
AND : 'and' ;
EXISTSALLOF : 'existsAllOf' ;
ANYOFPROPS : 'anyOfProps' ;
SECONDS : 'seconds' ;
NOTEXISTSALLOF : 'notExistsAllOf' ;
IVAL : 'iVal' ;
ON : 'on' ;
EXTPROP : 'extProp' ;
PROPS : 'props' ;
YIELD : 'yield' ;
COUNT : 'count' ;
DESC : 'desc' ;
MODEL : 'model' ;
EXISTSANYOF : 'existsAnyOf' ;
ADD : 'add' ;
ENDASSTR : 'endAsStr' ;
OR : 'or' ;
VALUES : 'values' ;
ALLOFEXPRESSIONS : 'allOfExpressions' ;
ADDTIMEINTERVALOF : 'addTimeIntervalOf' ;
ILIKE : 'iLike' ;
YEARS : 'years' ;
CONCAT : 'concat' ;
ASC : 'asc' ;
ORDER : 'order' ;
DATEOF : 'dateOf' ;
NOTILIKE : 'notILike' ;
MINOF : 'minOf' ;
ALLOFPARAMS : 'allOfParams' ;
DAYOF : 'dayOf' ;
BEGIN : 'begin' ;
LIKE : 'like' ;
COUNTALL : 'countAll' ;
GE : 'ge' ;
ANYOFIPARAMS : 'anyOfIParams' ;
NEGATEDCONDITION : 'negatedCondition' ;
ALLOFPROPS : 'allOfProps' ;
NOTEXISTS : 'notExists' ;
EXPR : 'expr' ;
THEN : 'then' ;
ALLOFIPARAMS : 'allOfIParams' ;
DAYOFWEEKOF : 'dayOfWeekOf' ;
EXISTS : 'exists' ;
ORDERBY : 'orderBy' ;
MOD : 'mod' ;
ROUND : 'round' ;
ENDASDECIMAL : 'endAsDecimal' ;
MODELASPRIMITIVE : 'modelAsPrimitive' ;
IPARAMS : 'iParams' ;
ABSOF : 'absOf' ;
ENDASINT : 'endAsInt' ;
ALLOFMODELS : 'allOfModels' ;
SUMOF : 'sumOf' ;
GROUPBY : 'groupBy' ;
HOURS : 'hours' ;
ANY : 'any' ;
AVGOFDISTINCT : 'avgOfDistinct' ;
BEGINEXPR : 'beginExpr' ;
YEAROF : 'yearOf' ;
CASEWHEN : 'caseWhen' ;
MAXOF : 'maxOf' ;
CRITCONDITION : 'critCondition' ;
WHERE : 'where' ;
LOWERCASE : 'lowerCase' ;
MINUTEOF : 'minuteOf' ;
ISNULL : 'isNull' ;
DIV : 'div' ;
ANYOFVALUES : 'anyOfValues' ;
COUNTOF : 'countOf' ;
ASREQUIRED : 'asRequired' ;
IFNULL : 'ifNull' ;
ILIKEWITHCAST : 'iLikeWithCast' ;

WHITESPACE : [ \r\t\n]+ -> skip ;
COMMENT : '//' .*? '\n' -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;

