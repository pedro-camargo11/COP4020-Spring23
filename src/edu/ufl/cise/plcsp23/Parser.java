package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.ast.AST;

import java.util.ArrayList;

public class Parser implements IParser{

    public ArrayList<IToken> tokenList;

    //constructor
    public Parser(ArrayList<IToken> tokenList) throws PLCException {
        this.tokenList = tokenList;
    }

    IToken t = null;

    protected boolean isKind(IToken.Kind kind){
        return (t.getKind() == kind);
    }

    //AST parse() throws PLCException{
    //    return null;
    //}

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
