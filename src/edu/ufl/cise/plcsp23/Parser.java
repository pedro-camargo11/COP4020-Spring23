package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.ast.*;
import edu.ufl.cise.plcsp23.ast.Dimension;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Parser implements IParser{

    public ArrayList<IToken> tokenList;
    IToken t;
    private int current = 0; //keeps track of position


    //constructor -> passes in only one time
//    public Parser(ArrayList<IToken> tokenList) throws PLCException {
//        this.tokenList = tokenList;
//        t = tokenList.get(current);
//
//    }

    public Parser(ArrayList<IToken> tokenList) throws LexicalException {
        this.tokenList = tokenList;
        t = tokenList.get(current);

    }

    //consume the token and move on to the next.
    void consume(){
        current++;
        t = tokenList.get(current);
    }

    void error() throws SyntaxException{
        throw new SyntaxException("Syntax error occured");
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

    //Match the token and move on to the next.
    protected void match(IToken.Kind... kinds) throws PLCException{

        for(IToken.Kind k:kinds){

            if(k == t.getKind()){
                consume();
                return;
            }

        }

        error();
    }

    //helper functions
    public IToken previous(){
        return tokenList.get(current-1);
    }

    //no error given anymore, go with implementing grammar functions.
    public AST parse() throws PLCException{

        return Program();

    }

    //Program -> Type IDENT LPAREN ParamList RPAREN Block (entry point)
    Program Program() throws PLCException{

        Type type = Type(); //get the type
        match(IToken.Kind.IDENT);
        Ident ident = new Ident(previous());
        match(IToken.Kind.LPAREN);
        List<NameDef> paramList = ParamList();
        match(IToken.Kind.RPAREN);
        Block block = Block();

        return new Program(t, type, ident, paramList, block);

    }

    //Block Syntax: LCURLY DeclarationList StatementList RCURLY
    Block Block() throws PLCException{

        match(IToken.Kind.LCURLY);
        List<Declaration> declarationList = DecList();
        List<Statement> statementList = StatementList();
        match(IToken.Kind.RCURLY);
        return new Block(t, declarationList, statementList);
    }

    List<Declaration> DecList() throws PLCException{


        List<Declaration> decList = new ArrayList<>();

        //when there is an empty string (epsilon) -> return null
        if(isKind(IToken.Kind.RCURLY, IToken.Kind.IDENT, IToken.Kind.RES_write, IToken.Kind.RES_while)){
            return decList;
        }

        //while the next token is a DOT, keep adding to the list
        while(isKind(IToken.Kind.RES_int, IToken.Kind.RES_string, IToken.Kind.RES_image, IToken.Kind.RES_pixel, IToken.Kind.RES_void)){
            Declaration dec = Declaration();
            decList.add(dec);

            //check to see if it's a DOT, if not then throw an error
            if(isKind(IToken.Kind.DOT)){
                match(IToken.Kind.DOT);
            }
        }

        return decList;
    }

    List<Statement> StatementList() throws PLCException {

        List<Statement> statementList = new ArrayList<>();

        //when there is an empty string (epsilon) -> return null
        if(isKind(IToken.Kind.RCURLY)){
            return statementList;
        }

        while (!(isKind(IToken.Kind.RCURLY))) {
            Statement statement = Statement();

            //error check
            if (!isKind(IToken.Kind.DOT)) {
                error();
            }

            match(IToken.Kind.DOT);
            statementList.add(statement);
        }

        return statementList;
    }

    List<NameDef> ParamList() throws PLCException{

        List<NameDef> paramList = new ArrayList<>();
        //when there is an empty string (epsilon) -> return null
        if(isKind(IToken.Kind.RPAREN)){
            return paramList;
        }

        //when there is a non-empty string -> NameDef(,NameDef)*
        paramList.add(NameDef());
        while(isKind(IToken.Kind.COMMA)){
            consume();
            paramList.add(NameDef());
        }
        return paramList;
    }

    //NameDef Syntax: type IDENT | Type Dimension IDENT
    NameDef NameDef() throws PLCException{

        Type type = Type();
        if(isKind(IToken.Kind.LSQUARE)){
            Dimension dimension = Dimension();
            match(IToken.Kind.IDENT);
            Ident ident = new Ident(previous());
            return new NameDef(t, type, dimension, ident);
        }
        else{
            match(IToken.Kind.IDENT);
            Ident ident = new Ident(previous());
            return new NameDef(t, type,null ,ident);
        }


    }

    //Dimension Syntax: [Expr, Expr]
    Dimension Dimension() throws PLCException{

        match(IToken.Kind.LSQUARE);
        Expr expr = Expression();
        match(IToken.Kind.COMMA);
        Expr expr2 = Expression();
        match(IToken.Kind.RSQUARE);
        return new Dimension(t, expr, expr2);

    }

    //get the type and consume the token: image, pixel, int, string, void
    Type Type() throws PLCException{
        if (isKind(IToken.Kind.RES_image, IToken.Kind.RES_pixel, IToken.Kind.RES_int, IToken.Kind.RES_string, IToken.Kind.RES_void)) {
            Type type = Type.getType(t);
            consume();
            return type;
        }
        else {
            error();
            return null;
        }
    }

    //Declaration Syntax: NameDef | NameDef ASSIGN Expr
    Declaration Declaration() throws PLCException{

        NameDef namedef = NameDef();

        if(isKind(IToken.Kind.ASSIGN)){
            match(IToken.Kind.ASSIGN);
            Expr expr = Expression();
            return new Declaration(t, namedef, expr);
        }
        else{
            consume();
            return new Declaration(previous(), namedef, null);
        }


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

    //Using match to error check
    Expr ConditionalExpression() throws PLCException{

        match(IToken.Kind.RES_if);//match if
        Expr guard = Expression();
        match(IToken.Kind.QUESTION); //match ?
        Expr trueCase = Expression();
        match(IToken.Kind.QUESTION); //match ?
        Expr falseCase = Expression();
        return new ConditionalExpr(t, guard, trueCase, falseCase);
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

        while(isKind(IToken.Kind.EQ, IToken.Kind.GT,IToken.Kind.LT, IToken.Kind.LE,IToken.Kind.GE)){
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
            right = PowExpr();
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

        return UnaryExpressionPostfix();

    }

    //UnaryExprPostfix::= PrimaryExpr (PixelSelector | ε ) (ChannelSelector | ε )
    Expr UnaryExpressionPostfix() throws PLCException {
        IToken firstToken = t;
        Expr primary = PrimaryExpr();
        if (isKind(IToken.Kind.LSQUARE)) {

            PixelSelector pixelSelector = selector();

            if(!(isKind(IToken.Kind.DOT))){
                ColorChannel channelSelector = channel();
                return new UnaryExprPostfix(firstToken, primary, pixelSelector, channelSelector);
            }
            else{
                return new UnaryExprPostfix(firstToken, primary, pixelSelector, null);
            }

        }

        return primary;
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

            //Parenthesis ()
            case LPAREN -> {

               match(IToken.Kind.LPAREN);
               Expr e = Expression();
               match(IToken.Kind.RPAREN);
               return e;

            }

            //Predeclared Variables
            case RES_x,RES_y,RES_a,RES_r -> {
                consume();
                return new PredeclaredVarExpr(previous());
            }

            //Expanded Pixel
            case LSQUARE -> {

                IToken firstToken = t;
                match(IToken.Kind.LSQUARE);
                Expr expr1 = Expression();
                match(IToken.Kind.COMMA);
                Expr expr2 = Expression();
                match(IToken.Kind.COMMA);
                Expr expr3 = Expression();
                match(IToken.Kind.RSQUARE);

                //return t because of match() function
                return new ExpandedPixelExpr(firstToken, expr1, expr2, expr3);

            }

            //Pixel Function Expression
            case RES_x_cart, RES_y_cart, RES_a_polar, RES_r_polar -> {

                IToken firstToken = t;
                IToken.Kind kind = t.getKind();

                consume();

                PixelSelector selector = selector();

                //return t because of match() function
                return new PixelFuncExpr(firstToken, kind, selector);
            }

            default -> error();
        }
        return null;
    }

    //Channel Selector
    ColorChannel channel() throws PLCException{

        match(IToken.Kind.COLON);
        ColorChannel color = ColorChannel.getColor(t);
        consume();

        return color;

    }

    //Pixel Selector function
    PixelSelector selector() throws PLCException{
        //maybe we have to save first token here to pass to pixel selector constructor !!!!!
        IToken firstToken = t;
        match (IToken.Kind.LSQUARE);
        Expr expr1 = Expression();
        match(IToken.Kind.COMMA);
        Expr expr2 = Expression();
        match(IToken.Kind.RSQUARE);

        return new PixelSelector(firstToken,expr1, expr2);

    }

    //LValue ::= IDENT (PixelSelector | ε ) (ChannelSelector | ε )
    LValue LValue() throws PLCException {
        IToken firstToken = t;
        Ident ident = new Ident(t);
        consume();
        PixelSelector pixelSelector = null;
        ColorChannel channelSelector = null;
        //if token is a '[' we have a pixel selector
        if (isKind(Token.Kind.LSQUARE)) {
            pixelSelector = selector();
        }
        //if token is ":" it is a channel selector
        if (isKind(IToken.Kind.COLON)){
            match(IToken.Kind.COLON);
            channelSelector = channel();
        }
        return new LValue (firstToken, ident, pixelSelector, channelSelector);
    }

    //Statement::= LValue = Expr | write Expr | while Expr Block
    Statement Statement() throws PLCException {
        IToken firstToken = t;
        //case for an assignment statement
        if (isKind(Token.Kind.IDENT)) {
            LValue lValue = LValue();
            match(IToken.Kind.ASSIGN);
            Expr expr = Expression();
            return new AssignmentStatement(firstToken, lValue, expr);
        }
        //case for a write statement
        else if (isKind(Token.Kind.RES_write)){
            match(IToken.Kind.RES_write);
            Expr expr = Expression();
            return new WriteStatement(firstToken, expr);
        }
        //case for a while statement
        else if (isKind(Token.Kind.RES_while)){
            match(IToken.Kind.RES_while);
            Expr expr = Expression();
            Block block = Block();
            return new WhileStatement(firstToken, expr, block);
        }
        return null;
    }




}
