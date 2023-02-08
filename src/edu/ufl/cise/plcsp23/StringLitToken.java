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
                i++;
                //check to see if the next char is part of a legal escape sequence
                if (i <= str.length){
                    char next = str[i];
                    if (next == 'b' || next == 't' || next == '"' || next == 92 || next == 'n' || next == 'r')
                    {
                        switch(next){

                            case 'b' -> escapeSequenceStr.add('\b');
                            case 't' -> escapeSequenceStr.add('\t');
                            case 'n' -> escapeSequenceStr.add('\n');
                            case 'r' -> escapeSequenceStr.add('\r');
                            case '"' -> escapeSequenceStr.add('"');
                            case '\\' -> escapeSequenceStr.add('\\');
                        }
                    }
                }
            }
            else {
                char c = str[i];
                escapeSequenceStr.add(c);
            }
        }
        String finalValue = "";
        for (int i = 0; i< escapeSequenceStr.size() ; i++)
        {
            finalValue += escapeSequenceStr.get(i);
        }
        return finalValue;
    }
}