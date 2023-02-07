package edu.ufl.cise.plcsp23;

public class StringLitToken extends Token implements IStringLitToken{
    public StringLitToken(int tokenStart, int tokenLen, char[] inputChars){
        super(Kind.STRING_LIT, tokenStart, tokenLen, inputChars);
    }

    public String getValue(){
        return getTokenString();
    }
}
