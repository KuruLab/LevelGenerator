/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoGraph;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 *
 * @author andre
 */
// a.k.a. Chromosome
public class GraphIndividual {
    
    private ArrayList<NodeGene> nodes;
    private double fitness;

    public GraphIndividual() {
        this.nodes = new ArrayList<>();
        this.fitness = Double.MAX_VALUE;
    }

    public GraphIndividual(ArrayList<NodeGene> nodes) {
        this.nodes = nodes;
        this.fitness = Double.MAX_VALUE;
    }

    public GraphIndividual(ArrayList<NodeGene> nodes, double fitness) {
        this.nodes = nodes;
        this.fitness = fitness;
    }

    public ArrayList<NodeGene> getNodes() {
        return nodes;
    }
    
    public NodeGene getNode(int i){
        return nodes.get(i);
    }
    
    public void setNodes(ArrayList<NodeGene> nodes) {
        this.nodes = nodes;
    }
    
    public void addNode(NodeGene node){
        this.nodes.add(node);
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
    
    public void evaluate(){
        Evaluation eva = new Evaluation();
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
    public GraphIndividual clone(){
        GraphIndividual clone = new GraphIndividual();
        clone.setFitness(fitness);
        for(int i = 0; i < nodes.size(); i++){
            //Gene node = nodes.get(i).clone();
            clone.addNode(nodes.get(i).clone());
        }
            
        return clone;
    }
}
