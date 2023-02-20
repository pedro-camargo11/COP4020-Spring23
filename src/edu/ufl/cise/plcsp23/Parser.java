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

    protected boolean isKind(IToken.Kind kind){
        return (t.getKind() == kind);
    }

    //no error given anymore, go with implementing grammar functions.
    AST parse() throws PLCException{
        return null;
    }

    void Expression() {
        //conditional_expr
        //if ()
        //or_expr
    }
    void ConditionalExpression(){

    }
    void OrExpression(){

    }


}
