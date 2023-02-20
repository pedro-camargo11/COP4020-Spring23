package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.ast.AST;


import java.util.ArrayList;

public class Parser implements IParser{

    public ArrayList<IToken> tokenList;
    IToken t;
    private int current = 0; //keeps track of position

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

        Expr();

//        if(isAtEnd()){
//
//            throw new SyntaxException("Not correct syntax");
//        }
        return null;
    }

    //Expression
    void Expr() throws PLCException {

        //conditional_expr
        if(isKind(IToken.Kind.RES_if)){
            ConditionalExpression();
        }
        //Or_expr
        else{

            OrExpression();
        }
    }

    void ConditionalExpression(){

        consume();

    }

    //LogicalOrExpression
    void OrExpression() throws PLCException {

        AndExpression();

        while(isKind(IToken.Kind.BITOR, IToken.Kind.OR)){
            consume();
            AndExpression();
        }
        return;
    }

    //LogicalAndExpression
    void AndExpression() throws PLCException {

        CompareExpr();

        while(isKind(IToken.Kind.BITAND,IToken.Kind.AND)){
            consume();
            CompareExpr();
        }

        return;


    }

    //Comparison Expression
    void CompareExpr() throws PLCException {

        PowExpr();

        while(isKind(IToken.Kind.EQ, IToken.Kind.GT,IToken.Kind.LT, IToken.Kind.LE,IToken.Kind.GT)){
            consume();
            PowExpr();
        }

        return;
    }

    //Power Expression
    void PowExpr() throws PLCException {

        AdditiveExpr();

        while(isKind(IToken.Kind.EXP)){
            consume();
            AdditiveExpr();
        }

        return;
    }

    //Additive Expression
    void AdditiveExpr() throws PLCException {

        MultExpr();

        while(isKind(IToken.Kind.PLUS, IToken.Kind.MINUS)){

            consume();
            MultExpr();

        }

        return;

    }

    //Multiplicative Expression
    void MultExpr() throws PLCException {

        UnaryExpr();

        while(isKind(IToken.Kind.TIMES, IToken.Kind.DIV, IToken.Kind.MOD)){

            consume();
            UnaryExpr();

        }
    }

    //Unary Expressions
    void UnaryExpr() throws PLCException{

        if(isKind(IToken.Kind.BANG, IToken.Kind.RES_atan, IToken.Kind.MINUS, IToken.Kind.RES_sin,IToken.Kind.RES_cos)){

            consume();
            UnaryExpr();
        }
        else{

            PrimaryExpr();
        }

    }

    //Primary Expressions
    void PrimaryExpr() throws PLCException {

        if(isKind(IToken.Kind.STRING_LIT, IToken.Kind.NUM_LIT, IToken.Kind.RES_rand, IToken.Kind.RES_Z)){

            consume();
        }
        else if(isKind(IToken.Kind.LPAREN)){

            consume();

            Expr();

            if(isKind(IToken.Kind.RPAREN)){

                consume();

            }
        }
        else{

            error();
        }

    }




}
