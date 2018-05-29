/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoPuzzle;

import extendedMetaZelda.Condition;
import extendedMetaZelda.Symbol;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author andre
 */
public class PuzzleGene {
    
    private int nodeID;
    private ArrayList<Symbol> symbols;
    private ArrayList<Condition> conditions;

    public PuzzleGene(int nodeID) {
        this.nodeID = nodeID;
        this.symbols = new ArrayList<>();
        this.conditions = new ArrayList<>();
        
        /*Symbol nothing = new Symbol(Symbol.NOTHING);
        Condition open = new Condition(nothing);
        
        this.symbols.add(nothing);
        this.conditions.add(open);*/
    }

    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public ArrayList<Symbol> getSymbols() {
        return symbols;
    }

    public void setSymbols(ArrayList<Symbol> symbols) {
        this.symbols = symbols;
    }

    public ArrayList<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(ArrayList<Condition> conditions) {
        this.conditions = conditions;
    }
    
    @Override
    public String toString(){
        String result = nodeID+" S:"+Arrays.toString(symbols.toArray())+" C:"+Arrays.toString(conditions.toArray());
        return result;
    }
    
    @Override
    public PuzzleGene clone(){
        PuzzleGene gene = new PuzzleGene(nodeID);
        for(int i = 0; i < symbols.size(); i++)
            gene.getSymbols().add(getSymbols().get(i));
        for(int i = 0; i < conditions.size(); i++)
            gene.getConditions().add(getConditions().get(i));
        return gene;
    }
}
