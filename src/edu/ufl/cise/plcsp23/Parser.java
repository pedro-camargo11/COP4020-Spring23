package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.ast.*;


import java.util.ArrayList;

public class Parser implements IParser{

    public ArrayList<IToken> tokenList;
    IToken t;
    private int current = 0; //keeps track of position
    Expr parsed;

    //constructor -> passes in only one time
    public Parser(ArrayList<IToken> tokenList) throws PLCException {
        this.tokenList = tokenList;
        t = tokenList.get(current);

    }

    //consume the token and move on to the next.
    void consume(){
        current++;
        t = tokenList.get(current);
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

    void error() throws PLCException{
        throw new SyntaxException("Syntax error occured");
    }
    //no error given anymore, go with implementing grammar functions.
    public AST parse() throws PLCException{

        return Expression();

//        if(isAtEnd()){
//
//            throw new SyntaxException("Not correct syntax");
//        }
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

    //conditional hasn't been implemented yet
    Expr ConditionalExpression(){

        consume();
        return null;

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

        while(isKind(IToken.Kind.EQ, IToken.Kind.GT,IToken.Kind.LT, IToken.Kind.LE,IToken.Kind.GT)){
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
            right = AdditiveExpr();
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
                consume();
                return Expression();
            }
            case RPAREN -> {
                consume();
            }
            default -> {
                error();
            }
        }
        return null;
    }
}
