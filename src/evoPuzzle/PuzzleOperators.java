/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoPuzzle;

//import evoGraph.Config;
import extendedMetaZelda.Condition;
import extendedMetaZelda.Symbol;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author andre
 */
public class PuzzleOperators {
    
    private Random rng;

    public PuzzleOperators() {
        rng = new Random();
    }
      
    public PuzzleIndividual[] crossover(PuzzleIndividual father, PuzzleIndividual mother){
        PuzzleIndividual[] son = new PuzzleIndividual[2];
        
        if (rng.nextDouble() < PuzzleConfig.crossoverProb) {
            son = independentOnePointCrossover(father, mother);
            return son;
        } else {
            son[0] = father.clone();
            son[1] = mother.clone();
            return son;
        }
    }
    private PuzzleIndividual[] independentOnePointCrossover(PuzzleIndividual father, PuzzleIndividual mother) {
        PuzzleIndividual[] son = new PuzzleIndividual[2];
        for (int i = 0; i < son.length; i++) {
            son[i] = new PuzzleIndividual();
        }
        // generate points and ensure they are inside bounds
        int pointF = rng.nextInt(father.getNodes().size());
        pointF = Math.min(mother.getNodes().size(), pointF);
        int pointM = rng.nextInt(mother.getNodes().size());
        pointM = Math.min(father.getNodes().size(), pointM);

        // build the first son
        for (int i = 0; i < pointF; i++) {
            PuzzleGene gene = father.getNodes().get(i).clone();
            son[0].getNodes().add(gene);
        }
        for (int i = pointF; i < mother.getNodes().size(); i++) {
            PuzzleGene gene = mother.getNodes().get(i).clone();
            son[0].getNodes().add(gene);
        }
        // build the second son
        for (int i = 0; i < pointM; i++) {
            PuzzleGene gene = mother.getNodes().get(i).clone();
            son[1].getNodes().add(gene);
        }
        for (int i = pointM; i < father.getNodes().size(); i++) {
            PuzzleGene gene = father.getNodes().get(i).clone();
            son[1].getNodes().add(gene);
        }

        return son;
    }
    
    public PuzzleIndividual mutation(int size, PuzzleIndividual mutant) {
        if (rng.nextDouble() < PuzzleConfig.mutationProb) {
            //Individual mutant = graph.clone();
            int mutations = 2;
            switch (rng.nextInt(mutations)) {
                case 0: // swap node
                    //System.out.println("Swap Node Mutation");
                    return swapNode(mutant);
                case 1: // tweak node (x, y) coords
                    //System.out.println("Tweak Node Mutation");
                    return tweakNode(size, mutant);
                default:
                    return mutant;
            }
        } else {
            return mutant;
        }
    }
    // simple node swap
    private PuzzleIndividual swapNode(PuzzleIndividual original) {
        PuzzleIndividual mutant = original.clone();
        int point1 = rng.nextInt(mutant.getNodes().size());
        int point2 = rng.nextInt(mutant.getNodes().size());
        // prevent swaping with himself
        while (point1 == point2) {
            point1 = rng.nextInt(mutant.getNodes().size());
            point2 = rng.nextInt(mutant.getNodes().size());
        }
        PuzzleGene node1 = mutant.getNodes().get(point1).clone();
        PuzzleGene node2 = mutant.getNodes().get(point2).clone();
        mutant.getNodes().set(point2, node1);
        mutant.getNodes().set(point1, node2);
        return mutant;
    }
    
    private PuzzleIndividual tweakNode(int size, PuzzleIndividual original) {
        PuzzleIndividual mutant = original.clone();
        int index = rng.nextInt(mutant.getNodes().size());
        PuzzleGene node = mutant.getNodes().get(index);
        
        ArrayList<Integer> existingNodes = new ArrayList<>();
        for(int i = 0; i< original.getNodes().size(); i++)
            existingNodes.add(original.getNodes().get(i).getNodeID());
        
        int newNodeIndex = rng.nextInt(size);
        while(existingNodes.contains(newNodeIndex)){
            newNodeIndex = rng.nextInt(size);
        }
        node.setNodeID(newNodeIndex);
        mutant.getNodes().set(index, node);
        return mutant;
    }

    public PuzzleIndividual fixIndividual(int size, PuzzleIndividual puzzleIndividual) {
        ArrayList<Integer> puzzleNodes = new ArrayList<>();
        ArrayList<Integer> badIndexes = new ArrayList<>();
        for(int i = 0; i < puzzleIndividual.getNodes().size(); i++){
            if(puzzleNodes.contains(puzzleIndividual.getNodes().get(i).getNodeID())){
                badIndexes.add(i);
            }
            puzzleNodes.add(puzzleIndividual.getNodes().get(i).getNodeID());
        }
       
        for(int i = 0; i < badIndexes.size(); i++){
            int newNode = rng.nextInt(size);
            while(puzzleNodes.contains(newNode))
                newNode = rng.nextInt(size);
            puzzleNodes.set(badIndexes.get(i), newNode);
        }
        
        PuzzleIndividual puzzle = new PuzzleIndividual();
        int start = puzzleNodes.get(0);
        int boss = puzzleNodes.get(1);
         
        PuzzleGene startGene = new PuzzleGene(start);
        startGene.getSymbols().add(new Symbol(Symbol.START));
        puzzle.add(startGene);
        
        PuzzleGene bossGene = new PuzzleGene(boss);
        bossGene.getSymbols().add(new Symbol(Symbol.BOSS));
        puzzle.add(bossGene);
        
        for(int i = 2, level = 0; i < puzzleIndividual.getNodes().size(); i++, level++){
            int nextKey = puzzleNodes.get(i);            
            PuzzleGene keyGene = new PuzzleGene(nextKey);
            
            Condition condition = new Condition(new Symbol(level));
            Symbol symbol = new Symbol(level+1);
            
            keyGene.getConditions().add(condition);
            keyGene.getSymbols().add(symbol);
            
            puzzle.add(keyGene);
        }
        // switches not supported yet
        return puzzle;
    }
    
    
}
