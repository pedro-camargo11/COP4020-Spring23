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

        //checking getTokenString
        System.out.println(t.getTokenString());
    }

    //consume the token and move on to the next.
    void consume(){

        //needs more to it... I think

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
        if(peek().getKind() == IToken.Kind.RES_if){
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

        Expr x = AndExpression();

        while(isKind(IToken.Kind.BITOR, IToken.Kind.OR)){
            consume();
            return AndExpression();
        }
        return x;
    }

    //LogicalAndExpression
    Expr AndExpression() throws PLCException {

        Expr x =  CompareExpr();

        while(isKind(IToken.Kind.BITAND,IToken.Kind.AND)){
            consume();
            return CompareExpr();
        }
        return x;

    }

    //Comparison Expression
    Expr CompareExpr() throws PLCException {

        Expr x = PowExpr();

        while(isKind(IToken.Kind.EQ, IToken.Kind.GT,IToken.Kind.LT, IToken.Kind.LE,IToken.Kind.GT)){
            consume();
            return PowExpr();
        }

        return x;
    }

    //Power Expression
    Expr PowExpr() throws PLCException {

        Expr x = AdditiveExpr();

        while(isKind(IToken.Kind.EXP)){
            consume();
            return AdditiveExpr();
        }
        return x;
    }

    //Additive Expression
    Expr AdditiveExpr() throws PLCException {

        Expr x = MultExpr();

        while(isKind(IToken.Kind.PLUS, IToken.Kind.MINUS)){

            consume();
             return MultExpr();

        }
        return x;
    }

    //Multiplicative Expression
    Expr MultExpr() throws PLCException {

        Expr x = UnaryExpr();

        while(isKind(IToken.Kind.TIMES, IToken.Kind.DIV, IToken.Kind.MOD)){

            consume();
            return UnaryExpr();

        }
        return x;
    }

    //Unary Expressions
    Expr UnaryExpr() throws PLCException{

        if(isKind(IToken.Kind.BANG, IToken.Kind.RES_atan, IToken.Kind.MINUS, IToken.Kind.RES_sin,IToken.Kind.RES_cos)){

            consume();
            return UnaryExpr();
        }
        else{

            return PrimaryExpr();
        }

    }

    //Primary Expressions
    Expr PrimaryExpr() throws PLCException {

        switch (t.getKind()) {

            case NUM_LIT -> {
                return new NumLitExpr(t);
            }
            case STRING_LIT -> {
                return new StringLitExpr(t);
            }

            case IDENT -> {
                return new IdentExpr(t);
            }

            case RES_Z -> {
                return new ZExpr(t);
            }

            case RES_rand -> {
                return new RandomExpr(t);
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
