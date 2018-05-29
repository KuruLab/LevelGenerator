/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoPuzzle;

import java.util.ArrayList;

/**
 *
 * @author andre
 */
public class PuzzleIndividual {
    
    private ArrayList<PuzzleGene> nodes;
    private double fitness;

    public PuzzleIndividual() {
        this.nodes = new ArrayList<>();
        this.fitness = Double.MAX_VALUE;
    }

    public PuzzleIndividual(ArrayList<PuzzleGene> nodes) {
        this.nodes = nodes;
        this.fitness = Double.MAX_VALUE;
    }

    public PuzzleIndividual(ArrayList<PuzzleGene> nodes, double fitness) {
        this.nodes = nodes;
        this.fitness = fitness;
    }
    
    public ArrayList<PuzzleGene> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<PuzzleGene> nodes) {
        this.nodes = nodes;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
    
    public void setStart(PuzzleGene gene){
        nodes.set(0, gene);
    }
    
    public PuzzleGene getStart(){
        return nodes.get(0);
    }
    
    public void setBoss(PuzzleGene gene){
        nodes.set(1, gene);
    }
    
    public PuzzleGene getBoss(){
        return nodes.get(1);
    }
    
    public void add(PuzzleGene gene){
        //System.out.println("adding new gene: "+gene.toString());
        nodes.add(gene);
    }
    
    @Override
    public String toString(){
        String result = "";//"F: "+fitness+"\n";
        for(int i = 0; i < nodes.size(); i++)
            result += nodes.get(i).toString()+"\n";
        return result;
    }
    
    @Override
    public PuzzleIndividual clone(){
        PuzzleIndividual puzzle = new PuzzleIndividual();
        for(int i = 0; i < getNodes().size(); i++)
            puzzle.add(getNodes().get(i));
        return puzzle;
    }
    
}
