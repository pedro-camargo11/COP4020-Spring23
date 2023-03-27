package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.ast.*;
import java.util.HashMap;
import java.util.Stack;

public class TypeCheck implements ASTVisitor {

    SymbolTable symbolTable;
    public TypeCheck(){
        symbolTable = new SymbolTable();
    }


    //Create a symbol table to store the scope and declaration.
    public static class SymbolTable {

        //create a hashmap to store the scope and declaration.
        Stack<HashMap<String,Declaration>> symbolTable = new Stack<HashMap<String,Declaration>>();

        //insert the declaration into the symbol table.
        public boolean insert(String name, Declaration declaration){

            //if the stack is empty then any insertion will be successful.
            if(symbolTable.isEmpty()){
                HashMap<String,Declaration> map = new HashMap<String,Declaration>();
                map.put(name,declaration);
                symbolTable.push(map);
                return true;
            }
            //if the stack is not empty then check if the name is already present in the symbol table.
            else{
                HashMap<String,Declaration> map = symbolTable.peek();
                if(map.containsKey(name)){
                    return false;
                }
                else{
                    map.put(name,declaration);
                    symbolTable.push(map);
                    return true;
                }
            }
        }


        public Declaration lookup(String  name) {
            return symbolTable.peek().get(name);
        }

    }

    //helper method used to perform checks in visitor methods:
    //i.e. check(dec != null, identExpr, "Undeclared identifier " + identExpr.getName());
    //i.e. check(dec.isAssigned(), identExpr, "Unassigned identifier " + identExpr.getName());
    public boolean check (boolean checkBool, IdentExpr identExpr, String message) {
        //not entirely sure if i need to do anything checks with the identExpr in this yet.
        if (!checkBool){
            System.out.println(message);
            return false;
        }
        return true;
    }


    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {

        numLitExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {

        stringLitExpr.setType(Type.STRING);
        return Type.STRING;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        String name = identExpr.getName();
        Declaration dec = symbolTable.lookup(name);
        check(dec != null, identExpr, "Undeclared identifier " + identExpr.getName());
        check(dec.isAssigned(), identExpr, "Unassigned identifier " + identExpr.getName());
        //identExpr.setDec(dec); save declaration it will be useful later
        Type type = dec.getType();
        identExpr.setType(type);
        return type;

    }


    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {

        zExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {

        randomExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {

        predeclaredVarExpr.setType(Type.INT);
        return Type.INT;
    }

    //Name def
    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {

        boolean boolDimension = (boolean) visitDimension(nameDef.getDimension(), arg);
        Type typeResult = nameDef.getType();
        Ident ident = nameDef.getIdent();

        //if Dimension is not an empty string -> Type should be == to Type.IMAGE
        if(boolDimension){

            typeResult = Type.IMAGE;

        }

        if(typeResult == Type.VOID){

            throw new TypeCheckException("Type mismatch in NameDef" + nameDef.getFirstToken().getSourceLocation().column());
        }

        //insert Symbol Table -> need to insert symbolTable
        if(symbolTable.lookup(ident.getName()) == null){


        }

        return null;
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
               else if(rightType == Type.STRING){
                   resultType = Type.STRING;
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

    //Dimension
    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {

        Type widthType = (Type) dimension.getWidth().visit(this, arg);
        Type heightType = (Type) dimension.getHeight().visit(this, arg);


        //return true if both are ints otherwise error out.
        if(widthType == Type.INT && heightType == Type.INT){

            return true;
        }
        else if (widthType == null || heightType == null){
            return false;
        }
        else{
            throw new TypeCheckException("Type mismatch in Dimension" + dimension.getFirstToken().getSourceLocation().column());
        }

    }

    //PixelSelector
    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {

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
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCException {

        Type type = (Type) writeStatement.getE().visit(this, arg);

        return type;

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
                    throw new TypeCheckException("Type mismatch in BinaryExpression" + binaryExpression.getFirstToken().getSourceLocation().column());
                }
            }
            case OR, AND , LT, GT, LE, GE-> {
                if (leftType == Type.INT && rightType == Type.INT) {
                    resultType = Type.INT;
                } else {
                    throw new TypeCheckException("Type mismatch in BinaryExpression" + binaryExpression.getFirstToken().getSourceLocation().column());
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
                    throw new TypeCheckException("Type mismatch in BinaryExpression" + binaryExpression.getFirstToken().getSourceLocation().column());
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
                    throw new TypeCheckException("Type mismatch in BinaryExpression" + binaryExpression.getFirstToken().getSourceLocation().column());
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
                    throw new TypeCheckException("Type mismatch in BinaryExpression" + binaryExpression.getFirstToken().getSourceLocation().column());
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
                    throw new TypeCheckException("Type mismatch in BinaryExpression" + binaryExpression.getFirstToken().getSourceLocation().column());
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
                    throw new TypeCheckException("Type mismatch in BinaryExpression" + binaryExpression.getFirstToken().getSourceLocation().column());
                }
            }
            default -> throw new TypeCheckException("Type mismatch in BinaryExpression" + binaryExpression.getFirstToken().getSourceLocation().column());

        }

        binaryExpression.setType(resultType);
        return resultType;

    }

}
