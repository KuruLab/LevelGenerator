/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoLevel;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 *
 * @author andre
 */
// a.k.a. Chromosome
public class LevelIndividual {
    
    private ArrayList<LevelGene> nodes;
    private double fitness;

    public LevelIndividual() {
        this.nodes = new ArrayList<>();
        this.fitness = Double.MAX_VALUE;
    }

    public LevelIndividual(ArrayList<LevelGene> nodes) {
        this.nodes = nodes;
        this.fitness = Double.MAX_VALUE;
    }

    public LevelIndividual(ArrayList<LevelGene> nodes, double fitness) {
        this.nodes = nodes;
        this.fitness = fitness;
    }

    public ArrayList<LevelGene> getNodes() {
        return nodes;
    }
    
    public LevelGene getNode(int i){
        return nodes.get(i);
    }
    
    public void setNodes(ArrayList<LevelGene> nodes) {
        this.nodes = nodes;
    }
    
    public void addNode(LevelGene node){
        this.nodes.add(node);
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
    
    public void evaluate(){
        LevelEvaluation eva = new LevelEvaluation();
        this.fitness = eva.fitness(this, false);
    }
    
    @Override
    public String toString(){
        String str = "number of nodes: "+this.getNodes().size()+"\n"+
                     "fitness: "+this.getFitness()+"\n"+
                     "nodes:\n";
        for(int i = 0; i < this.getNodes().size(); i++){
            str += this.getNode(i).toString();
        }
        return str;
    }
    
    public String toHashString(){
        String str = "nodes:\n";
        for(int i = 0; i < this.getNodes().size(); i++){
            str += i+": "+this.getNode(i).toHashString()+"\n";
        }
        return str;
    }
    
    @Override
    public LevelIndividual clone(){
        LevelIndividual clone = new LevelIndividual();
        clone.setFitness(fitness);
        for(int i = 0; i < nodes.size(); i++){
            //Gene node = nodes.get(i).clone();
            clone.addNode(nodes.get(i).clone());
        }
            
        return clone;
    }
}
