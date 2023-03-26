package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.ast.*;
import java.util.HashMap;
import java.util.Stack;

public class TypeCheck implements ASTVisitor {




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


        public Declaration lookup(String name) {
            return symbolTable.peek().get(name);
        }


    }
}
