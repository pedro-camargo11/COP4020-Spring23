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


        public Declaration lookup(Stringgit  name) {
            return symbolTable.peek().get(name);
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
        symbolTable.check(dec != null, identExpr, "Undeclared identifier " + identExpr.getName());
        symbolTable.check(dec.isAssigned(), identExpr, "Unassigned identifier " + identExpr.getName());
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

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {

        return null;
    }

    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {

        pixelFuncExpr.setType(Type.INT);
        return Type.INT;
    }

    //look for check method in pulling Pedros code
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


}
