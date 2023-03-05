package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.ast.*;


import java.util.ArrayList;
import java.util.List;

public class Parser implements IParser{

    public ArrayList<IToken> tokenList;
    IToken t;
    private int current = 0; //keeps track of position


    //constructor -> passes in only one time
//    public Parser(ArrayList<IToken> tokenList) throws PLCException {
//        this.tokenList = tokenList;
//        t = tokenList.get(current);
//
//    }

    public Parser(ArrayList<IToken> tokenList) throws LexicalException {
        this.tokenList = tokenList;
        t = tokenList.get(current);

    }

    //consume the token and move on to the next.
    void consume(){
        current++;
        t = tokenList.get(current);
    }

    void error() throws PLCException{
        throw new SyntaxException("Syntax error occured");
    }

    //Singular token.
    protected boolean isKind(IToken.Kind kind){
        return (t.getKind() == kind);
    }

    //multiple kinds of isKind tokens
    protected boolean isKind(IToken.Kind... kinds){

        for(IToken.Kind k:kinds){

            if(k == t.getKind()){
                return true;
            }
        }
        return false;
    }

    //Match the token and move on to the next.
    protected void match(IToken.Kind... kinds) throws PLCException{

        for(IToken.Kind k:kinds){

            if(k == t.getKind()){
                consume();
                return;
            }

        }

        error();
    }

    //helper functions
    public IToken peek(){
        return tokenList.get(current);
    }
    public IToken previous(){
        return tokenList.get(current-1);
    }
    public boolean isAtEnd(){
        return (peek().getKind() == IToken.Kind.EOF);
    }


    //no error given anymore, go with implementing grammar functions.
    public AST parse() throws PLCException{

        return Expression();

    }

    //Program -> Type IDENT LPAREN ParamList RPAREN Block
    Program Program() throws PLCException{

        Type type = Type.getType(t); //get the type
        consume();
        match(IToken.Kind.IDENT);
        Ident ident = new Ident(previous());
        match(IToken.Kind.LPAREN);
        List<NameDef> paramList = null;
        match(IToken.Kind.RPAREN);
        Block block = null;

        return new Program(t, type, ident, paramList, block);

    }


    //Expression
    Expr Expression() throws PLCException {

        //conditional_expr
        if(isKind(IToken.Kind.RES_if)){
            return ConditionalExpression();
        }
        //Or_expr
        else{

            return OrExpression();
        }
    }

    //Using match to error check
    Expr ConditionalExpression() throws PLCException{

        match(IToken.Kind.RES_if);//match if
        Expr guard = Expression();
        match(IToken.Kind.QUESTION); //match ?
        Expr trueCase = Expression();
        match(IToken.Kind.QUESTION); //match ?
        Expr falseCase = Expression();
        return new ConditionalExpr(t, guard, trueCase, falseCase);
    }

    //LogicalOrExpression
    Expr OrExpression() throws PLCException {

        Expr left = null;
        Expr right = null;
        left = AndExpression();

        while(isKind(IToken.Kind.BITOR, IToken.Kind.OR)){
            IToken.Kind op = t.getKind();
            consume();
            right = AndExpression();
            left = new BinaryExpr(previous(), left, op, right);
        }
        return left;
    }

    //LogicalAndExpression
    Expr AndExpression() throws PLCException {
        Expr left = null;
        Expr right = null;
        left =  CompareExpr();

        while(isKind(IToken.Kind.BITAND,IToken.Kind.AND)){
            IToken.Kind op = t.getKind();
            consume();
            right = CompareExpr();
            left = new BinaryExpr (previous(), left, op, right);
        }
        return left;

    }

    //Comparison Expression
    Expr CompareExpr() throws PLCException {

        Expr left = null;
        Expr right = null;
        left = PowExpr();

        while(isKind(IToken.Kind.EQ, IToken.Kind.GT,IToken.Kind.LT, IToken.Kind.LE,IToken.Kind.GE)){
            IToken.Kind op = t.getKind();
            consume();
            right = PowExpr();
            left = new BinaryExpr (previous(), left, op, right);
        }
        return left;
    }

    //Power Expression
    Expr PowExpr() throws PLCException {
        Expr left = null;
        Expr right = null;
        left = AdditiveExpr();

        while(isKind(IToken.Kind.EXP)){
            IToken.Kind op = t.getKind();
            consume();
            right = PowExpr();
            left = new BinaryExpr (previous(), left, op, right);
        }
        return left;
    }

    //Additive Expression
    Expr AdditiveExpr() throws PLCException {
        Expr left = null;
        Expr right = null;
        left = MultExpr();

        while(isKind(IToken.Kind.PLUS, IToken.Kind.MINUS)){
            IToken.Kind op = t.getKind();
            consume();
            right = MultExpr();
            left = new BinaryExpr (previous(), left, op, right);
        }
        return left;
    }

    //Multiplicative Expression
    Expr MultExpr() throws PLCException {
        Expr left = null;
        Expr right = null;
        left = UnaryExpression();

        while(isKind(IToken.Kind.TIMES, IToken.Kind.DIV, IToken.Kind.MOD)){
            IToken.Kind op = t.getKind();
            consume();
            right = UnaryExpression();
            left = new BinaryExpr (previous(), left, op, right);
        }
        return left;
    }

    //Unary Expressions
    Expr UnaryExpression() throws PLCException{

        Expr right = null;
        if(isKind(IToken.Kind.BANG, IToken.Kind.RES_atan, IToken.Kind.MINUS, IToken.Kind.RES_sin,IToken.Kind.RES_cos)){
            IToken unaryToken = t; // reassign
            consume();
            right = UnaryExpression();
            return new UnaryExpr(previous(),unaryToken.getKind(),right);
        }

        return PrimaryExpr();

    }

    //Primary Expressions
    Expr PrimaryExpr() throws PLCException {


        switch (t.getKind()) {

            case NUM_LIT -> {
                consume();
                return new NumLitExpr(previous());
            }
            case STRING_LIT -> {
                consume();
                return new StringLitExpr(previous());
            }

            case IDENT ->{
                consume();
                return new IdentExpr(previous());
            }

            case RES_Z -> {
                consume();
                return new ZExpr(previous());
            }

            case RES_rand -> {
                consume();
                return new RandomExpr(previous());
            }

            case LPAREN -> {

               match(IToken.Kind.LPAREN);
               Expr e = Expression();
               match(IToken.Kind.RPAREN);
               return e;

            }

            default -> {
                error();

            }
        }
        return null;
    }
}
