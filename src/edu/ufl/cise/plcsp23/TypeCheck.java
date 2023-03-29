package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.ast.*;

import java.util.*;


public class TypeCheck implements ASTVisitor {

    //programType for type checking return
    Type programType;


    public TypeCheck(){
        symbolTable = new SymbolTable();
    }

    SymbolTable symbolTable;





    //Create a symbol table to store the scope and declaration.
    public static class SymbolTable {
        int currScope;
        int nextScope;

        Stack<Integer> scopeStack;
        HashMap<String,NameDef> symbolTable;

        public SymbolTable(){
            currScope = 0;
            nextScope = 1;

            scopeStack = new Stack<Integer>();
            symbolTable = new HashMap<String,NameDef>();

            scopeStack.push(currScope);
        }

        public void enterScope(){
            currScope = nextScope;
            nextScope++;
            scopeStack.push(currScope);
        }

        public void leaveScope(){
            scopeStack.pop();
            currScope = scopeStack.peek();
        }

        //insert the declaration into the symbol table.
        public boolean insert(String name, NameDef nameDef){
            if (symbolTable.containsKey(name)) {
                return false;
            }
            symbolTable.put(name, nameDef);
            return true;
        }


        public NameDef lookup(String name) {
            return symbolTable.get(name);
        }


    }

    //helper method used to perform checks in visitor methods:
    public void check (boolean checkBool, AST node, String message) throws TypeCheckException{
        if (!checkBool) {
            throw new TypeCheckException(message + node.getFirstToken().getSourceLocation().toString());
        }

    }

    public boolean assignmentCompatible (Type targetType, Type rhs)
    {
        return (targetType == rhs
                || targetType == Type.IMAGE && rhs == Type.PIXEL
                || targetType == Type.IMAGE && rhs == Type.STRING
                || targetType == Type.PIXEL && rhs == Type.INT
                || targetType == Type.INT && rhs == Type.PIXEL
                || targetType == Type.STRING && rhs == Type.INT
                || targetType == Type.STRING && rhs == Type.PIXEL
                || targetType == Type.STRING && rhs == Type.IMAGE);
    }


    @Override
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {

        Type lvalueType = (Type) statementAssign.getLv().visit(this, arg);
        Type exprType = (Type) statementAssign.getE().visit(this, arg);

        check(assignmentCompatible(lvalueType, exprType), statementAssign, "Type mismatch in AssignmentStatement");

        return statementAssign;
    }
    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpression, Object arg) throws PLCException {

        Type leftType = (Type) binaryExpression.getLeft().visit(this, arg);
        Type rightType = (Type) binaryExpression.getRight().visit(this, arg);
        Type resultType = null;

        IToken.Kind op = binaryExpression.getOp();

        switch(op){
            case BANG, BITAND ->{
                if (leftType == Type.PIXEL && rightType == Type.PIXEL) {
                    resultType = Type.PIXEL;
                } else {
                    check(false, binaryExpression, "Type mismatch in BinaryExpression");
                }
            }
            case OR, AND , LT, GT, LE, GE-> {
                if (leftType == Type.INT && rightType == Type.INT) {
                    resultType = Type.INT;
                } else {
                    check(false, binaryExpression, "Type mismatch in BinaryExpression");
                }
            }

            case EQ ->{
                if (leftType == Type.INT && rightType == Type.INT) {
                    resultType = Type.INT;
                } else if (leftType == Type.PIXEL && rightType == Type.PIXEL) {
                    resultType = Type.INT;
                } else if (leftType == Type.IMAGE && rightType == Type.IMAGE) {
                    resultType = Type.INT;
                } else if (leftType == Type.STRING && rightType == Type.STRING) {
                    resultType = Type.INT;
                } else {
                    check(false, binaryExpression, "Type mismatch in BinaryExpression");
                }
            }

            case EXP ->{
                if (leftType == Type.INT && rightType == Type.INT) {
                    resultType = Type.INT;
                }
                if (leftType == Type.PIXEL && rightType == Type.INT) {
                    resultType = Type.PIXEL;
                }
                else {
                    check(false, binaryExpression, "Type mismatch in BinaryExpression");
                }
            }

            case PLUS -> {
                if (leftType == Type.INT && rightType == Type.INT) {
                    resultType = Type.INT;
                } else if (leftType == Type.PIXEL && rightType == Type.PIXEL) {
                    resultType = Type.PIXEL;
                } else if (leftType == Type.IMAGE && rightType == Type.IMAGE) {
                    resultType = Type.IMAGE;
                } else if (leftType == Type.STRING && rightType == Type.STRING) {
                    resultType = Type.STRING;
                } else {
                    check(false, binaryExpression, "Type mismatch in BinaryExpression");
                }
            }

            case MINUS -> {
                if (leftType == Type.INT && rightType == Type.INT) {
                    resultType = Type.INT;
                } else if (leftType == Type.PIXEL && rightType == Type.PIXEL) {
                    resultType = Type.PIXEL;
                } else if (leftType == Type.IMAGE && rightType == Type.IMAGE) {
                    resultType = Type.IMAGE;
                } else {
                    check(false, binaryExpression, "Type mismatch in BinaryExpression");
                }
            }

            case TIMES, DIV, MOD -> {
                if (leftType == Type.INT && rightType == Type.INT) {
                    return Type.INT;
                } else if (leftType == Type.PIXEL && rightType == Type.PIXEL) {
                    resultType = Type.PIXEL;
                } else if (leftType == Type.IMAGE && rightType == Type.IMAGE) {
                    resultType = Type.IMAGE;
                } else if (leftType == Type.PIXEL && rightType == Type.INT) {
                    resultType = Type.PIXEL;
                } else if (leftType == Type.IMAGE && rightType == Type.INT) {
                    resultType = Type.IMAGE;
                } else {
                    check(false, binaryExpression, "Type mismatch in BinaryExpression");
                }
            }
            default -> throw new TypeCheckException("Type mismatch in BinaryExpression");

        }

        binaryExpression.setType(resultType);
        return resultType;

    }


    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException {
        //DecList is properly typed
        List<Declaration> decList = block.getDecList();
        for (Declaration dec : decList) {
            dec.visit(this, arg);
        }

        //StatementList is properly typed
        List<Statement> statementList = block.getStatementList();
        for (Statement statement : statementList) {
            statement.visit(this, arg);
        }

        return block;
    }

    //Conditional Expression
    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException{

        Type gaurdType = (Type) conditionalExpr.getGuard().visit(this, arg);
        Type trueType = (Type) conditionalExpr.getTrueCase().visit(this, arg);
        Type falseType = (Type) conditionalExpr.getFalseCase().visit(this, arg);
        Type resultType = null;

        if(gaurdType == Type.INT){

            if (trueType == falseType) {

                resultType = trueType;
            }
            else{

                throw new TypeCheckException("Type mismatch in ConditionalExpr" + conditionalExpr.getFirstToken().getSourceLocation().column());
            }
        }
        else{

            throw new TypeCheckException("Type mismatch in ConditionalExpr" + conditionalExpr.getFirstToken().getSourceLocation().column());
        }

        return resultType;

    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        //make sure nameDef is properly typed
        NameDef nameDef = declaration.getNameDef();
        Type nameDefType = nameDef.getType();
        //Make sure nameDef is assigned
        Expr initializer = declaration.getInitializer();
        if (initializer == null){
            nameDef.visit(this, arg);
        }
        else {
            Type initType = (Type) initializer.visit(this, arg);
            nameDef.visit(this, arg);
            check(assignmentCompatible(nameDefType, initType), declaration, "Type mismatch in Declaration");
        }
        return nameDefType;
        // haven't considered the last constraint (NameDef.Type == IMAGE)
    }

    //Dimension
    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {

        if (dimension == null) {
            return false;
        }
        Type widthType = (Type) dimension.getWidth().visit(this, arg);
        Type heightType = (Type) dimension.getHeight().visit(this, arg);


        //return true if both are ints otherwise error out.
        if(widthType == Type.INT && heightType == Type.INT){

            return true;
        }
        else{
            throw new TypeCheckException("Type mismatch in Dimension" + dimension.getFirstToken().getSourceLocation().column());
        }

    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {

        Type redType = (Type) expandedPixelExpr.getRedExpr().visit(this, arg);
        Type greenType = (Type) expandedPixelExpr.getGrnExpr().visit(this, arg);
        Type blueType = (Type) expandedPixelExpr.getBluExpr().visit(this, arg);
        Type resultType = null;

        //return true if all are ints otherwise error out.
        if(redType == Type.INT && greenType == Type.INT && blueType == Type.INT){

            resultType = Type.INT;
        }
        else{
            throw new TypeCheckException("Type mismatch in ExpandedPixelExpr" + expandedPixelExpr.getFirstToken().getSourceLocation().column());
        }

        return resultType;

    }

    @Override
    public Object visitIdent (Ident ident, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        String name = identExpr.getName();
        NameDef def = symbolTable.lookup(name);
        if (def != null) {
             identExpr.setType(def.getType());
             return def.getType();
        }
        throw new TypeCheckException("Type mismatch in IdentExpr" + identExpr.getFirstToken().getSourceLocation().column());
    }

    @Override
    public Object visitLValue (LValue lValue, Object arg) throws PLCException {

        String name = lValue.getIdent().getName();
        PixelSelector pixel = lValue.getPixelSelector();
        ColorChannel color = lValue.getColor();
        Type identType = symbolTable.lookup(name).getType();
        Type resultType = null;

        if(identType == null){
            throw new TypeCheckException("Type mismatch in LValue" + lValue.getFirstToken().getSourceLocation().column());
        }
        else{
            switch(identType){
                case IMAGE -> {
                    if(pixel == null && color == null) resultType = Type.IMAGE;
                    else if(pixel != null && color == null) resultType = Type.PIXEL;
                    else if(pixel == null && color != null) resultType = Type.IMAGE;
                    else resultType = Type.INT;

                }
                case PIXEL -> {
                    if(color == null && pixel == null) resultType = Type.PIXEL;
                    else if(color != null && pixel == null) resultType = Type.INT;
                    else throw new TypeCheckException("Type mismatch in LValue" + lValue.getFirstToken().getSourceLocation().column());

                }
                case STRING -> {
                    if(pixel == null && color == null) resultType = Type.STRING;
                    else throw new TypeCheckException("Type mismatch in LValue" + lValue.getFirstToken().getSourceLocation().column());
                }
                case INT -> {
                    if(pixel == null && color == null) resultType = Type.INT;
                    else throw new TypeCheckException("Type mismatch in LValue" + lValue.getFirstToken().getSourceLocation().column());
                }
                default -> throw new TypeCheckException("Type mismatch in LValue" + lValue.getFirstToken().getSourceLocation().column());
            }
        }
        return resultType;
    }

    //Name def
    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {

        boolean boolDimension = (boolean) visitDimension(nameDef.getDimension(), arg);
        Type typeResult = nameDef.getType();
        Ident ident = nameDef.getIdent();

        //if Dimension is not an empty string -> Type should be == to Type.IMAGE
        if(boolDimension){

           if(typeResult != Type.IMAGE){
               throw new TypeCheckException("Type mismatch in NameDef" + nameDef.getFirstToken().getSourceLocation().column());
           }

        }


        if(typeResult == Type.VOID){
            throw new TypeCheckException("Type mismatch in NameDef" + nameDef.getFirstToken().getSourceLocation().column());
        }

        //insert Symbol Table -> need to insert symbolTable
        if(symbolTable.lookup(ident.getName()) == null){
            symbolTable.insert(ident.getName(), nameDef);
        }
        else {
            throw new TypeCheckException("Type mismatch in NameDef" + nameDef.getFirstToken().getSourceLocation().column());
        }

        //?
        return nameDef.getType();
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {

        numLitExpr.setType(Type.INT);
        return Type.INT;
    }

    //PixelFunctionExpression: this needs to be worked on
    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {


        PixelSelector selector = pixelFuncExpr.getSelector();
        boolean selectorCheck = (boolean) visitPixelSelector(selector, arg);

        //if its true then its properly typed
        if(selectorCheck){

            pixelFuncExpr.setType(Type.INT);
            return Type.INT;

        }
        //else throw an exception
        else{

            throw new TypeCheckException("Type mismatch in PixelFuncExpr" + pixelFuncExpr.getFirstToken().getSourceLocation().column());
        }


    }

    //PixelSelector
    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {

        //if pixelSelector is null then return false
        if(pixelSelector == null){
            return false;
        }

        //properly type check the x and y
        Type xType = (Type) pixelSelector.getX().visit(this, arg);
        Type yType = (Type) pixelSelector.getY().visit(this, arg);

        //return true if both are ints otherwise error out.
        if(xType == Type.INT && yType == Type.INT){

            return true;
        }
        else if (xType == null || yType == null){
            return false;
        }
        else{
            throw new TypeCheckException("Type mismatch in PixelSelector" + pixelSelector.getFirstToken().getSourceLocation().column());
        }

    }

    @Override
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {

        predeclaredVarExpr.setType(Type.INT);
        return Type.INT;
    }

    //Save returnType for each visit?
    @Override
    public Object visitProgram (Program program, Object arg) throws PLCException {
        //enter scope
        symbolTable.enterScope();

        //get program return type
        Type returnType = program.getType();
        programType = returnType;

        //All NameDefs are properly typed
        List<NameDef> parameters = program.getParamList();
        for(NameDef nameDef : parameters){
            nameDef.visit(this, arg);
        }

        //Block is properly typed
        Block block = program.getBlock();
        visitBlock(block, arg);

        //leave scope
        symbolTable.leaveScope();
        return program;//remove later
    }

    @Override
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {

        randomExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitReturnStatement (ReturnStatement returnStatement, Object arg) throws PLCException {
        //expression is properly typed
        Expr expr = returnStatement.getE();
        Type exprType = (Type) expr.visit(this, arg);

        //Expr.type is assignmentCompatible with program.Type
        if (exprType == programType){
            return exprType;
        }
        else{
            throw new TypeCheckException("Type mismatch in ReturnStatement" + returnStatement.getFirstToken().getSourceLocation().column());
        }

    }
    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {

        stringLitExpr.setType(Type.STRING);
        return Type.STRING;
    }

    //UnaryExpression: look for check method in pulling Pedros code
    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException{

        IToken.Kind op = unaryExpr.getOp();
        Type rightType = (Type) unaryExpr.getE().visit(this, arg);
        Type resultType = null;

        switch(op){

            case BANG -> {
                if(rightType == Type.INT){
                    resultType = Type.INT;
                }
                else if(rightType == Type.PIXEL){
                    resultType = Type.PIXEL;
                }
                else{
                    throw new TypeCheckException("Type mismatch in UnaryExpr" + unaryExpr.getFirstToken().getSourceLocation().column());
                }
            }

            case MINUS, RES_cos, RES_sin, RES_atan -> {
                if(rightType == Type.INT){
                    resultType = Type.INT;
                }
                else{
                    throw new TypeCheckException("Type mismatch in UnaryExpr" + unaryExpr.getFirstToken().getSourceLocation().column());
                }

            }

            default -> throw new TypeCheckException("Type mismatch in UnaryExpr" + unaryExpr.getFirstToken().getSourceLocation().column());

        }

        unaryExpr.setType(resultType);
        return resultType;

    }

    @Override
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
        PixelSelector selector = unaryExprPostfix.getPixel();
        boolean checkSelector = (boolean) visitPixelSelector(selector, arg);

        Type primaryExpr = (Type) unaryExprPostfix.getPrimary().visit(this, arg);
        ColorChannel channel = unaryExprPostfix.getColor();
        Type resultType = null;

        if (checkSelector){

           if (channel == null && primaryExpr== Type.IMAGE){
               unaryExprPostfix.setType(Type.PIXEL);
               resultType = Type.PIXEL;
           }
           else if (channel != null && primaryExpr == Type.IMAGE){
               unaryExprPostfix.setType(Type.INT);
               resultType = Type.INT;
           }
           else throw new TypeCheckException("Type mismatch in UnaryExprPostfix" + unaryExprPostfix.getFirstToken().getSourceLocation().column());
        }
        else{ //pixel selector not present
            if (channel != null && primaryExpr == Type.IMAGE){
                unaryExprPostfix.setType(Type.IMAGE);
                resultType = Type.IMAGE;
            }
            else if (channel != null && primaryExpr == Type.PIXEL){
                unaryExprPostfix.setType(Type.INT);
                resultType = Type.INT;
            }
            else throw new TypeCheckException("Type mismatch in UnaryExprPostfix" + unaryExprPostfix.getFirstToken().getSourceLocation().column());

        }

        //Return type of UnaryExprPostfix
        return resultType;
    }

    @Override
    public Object visitWhileStatement (WhileStatement whileStatement, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCException {

        Type type = (Type) writeStatement.getE().visit(this, arg);

        return type;

    }

    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {

        zExpr.setType(Type.INT);
        return Type.INT;
    }

}
