package edu.ufl.cise.plcsp23;

import java.util.ArrayList;

public class StringLitToken extends Token implements IStringLitToken{
    public StringLitToken(int tokenStart, int tokenLen, char[] inputChars,int line,int col){
        super(Kind.STRING_LIT, tokenStart, tokenLen, inputChars,line, col);
    }

    public String getValue(){
        String tokenStr = getTokenString();
        tokenStr= tokenStr.substring(1,tokenLen-1);//remove quotations enclosing StringLit
        ArrayList<Character> escapeSequenceStr = new ArrayList<Character>();
        //now handle escape sequences;
        char[] str = tokenStr.toCharArray();
        for (int i = 0; i < tokenStr.length() ; i++)
        {
            if (str[i] == 92) //92 is \ in ascii
            {
                //check to see if the next char is part of a legal escape sequence
                if (i+1 <= str.length){
                    char next = str[i+1];
                    if (next == 'b' || next == 't' || next == '"' || next == 92)
                    {
                        i++;//skip a character
                    }
                }
            }
            char c = str[i];
            escapeSequenceStr.add(c);
        }
        String finalValue = "";
        for (int i = 0; i< escapeSequenceStr.size() ; i++)
        {
            finalValue += escapeSequenceStr.get(i);
        }
        return finalValue;
    }
}
