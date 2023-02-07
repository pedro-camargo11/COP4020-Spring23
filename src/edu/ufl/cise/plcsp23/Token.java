package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.IToken.SourceLocation;

public class Token implements IToken{

    Kind kind;
    int tokenStart;
    int tokenLen;
    char[] inputChars;
    int col = 0;
    int line = 0;

    public Token (Kind kind, int tokenStart, int tokenLen, char[] input)
    {
        this.kind = kind;
        this.tokenStart = tokenStart;
        this.tokenLen = tokenLen;
        inputChars = input;
    }
    
    public Kind getKind()
    {
        return kind;
    }

    public String getTokenString()
    {
        //String tokenStr= inputChars.toString();
        String tokenStr = new String(inputChars);
        String cutStr = tokenStr.substring(tokenStart,tokenStart+tokenLen);
        return cutStr;
    }

    public SourceLocation getSourceLocation ()
    {
        return new SourceLocation(line, col);
    }
}
