package edu.ufl.cise.plcsp23;

public class StringLitToken extends Token implements IStringLitToken{
    public StringLitToken(int tokenStart, int tokenLen, char[] inputChars,int line,int col){
        super(Kind.STRING_LIT, tokenStart, tokenLen, inputChars,line, col);
    }

    public String getValue(){
        return getTokenString();
    }
}
