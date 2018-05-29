/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoPuzzle;

import evoGraph.Decoder;
import evoGraph.GraphIndividual;
import java.util.Random;
import org.graphstream.graph.Graph;

/**
 *
 * @author andre
 */
public class PuzzleGA implements Runnable{
    
    private Graph graph;
    private GraphIndividual original;
    
    private double[] bestFitness;
    private PuzzleIndividual bestIndividual;
    private int currentGeneration;
    private boolean improvement, finished;

    public PuzzleGA(){
        improvement = false;
        finished = false;
        currentGeneration = 0;
        bestIndividual = new PuzzleIndividual();
        bestFitness = new double[5];
        for(int i = 0; i < bestFitness.length; i++)
            bestFitness[i] = Double.MAX_VALUE;
    }
    
    public PuzzleGA(Graph g) {
        this();
        this.graph = g;
        
        //System.out.println("Encoding Graph as Individual");
        Decoder originalEncoder = new Decoder(graph);
        GraphIndividual originalIndividual = originalEncoder.encode();
        
        //System.out.println("Connecting Nodes");
        Decoder connectDecoder = new Decoder(originalIndividual);
        original = connectDecoder.barabasiAlbertGraph();
        
        //System.out.println("Decoding Individual back as Graph");
        Decoder graphDecoder = new Decoder(original);
        graph = graphDecoder.decode();
        //graph.display(false);
    }
    
    public PuzzleGA(GraphIndividual individual) {
        this();
        
        //System.out.println("Connecting Nodes");
        Decoder connectDecoder = new Decoder(individual);
        original = connectDecoder.barabasiAlbertGraph();
        
        //System.out.println("Decoding Individual back as Graph");
        Decoder graphDecoder = new Decoder(original);
        graph = graphDecoder.decode();
    }
  
    @Override
    public void run() {
        PuzzleIndividual[] population = initializePopulation();
         while(currentGeneration < PuzzleConfig.maxGen && !finished){
            //System.out.println("Gen "+currentGeneration+" Fitness: "+String.format("%.6f", bestFitness[0])+")");
            //System.out.println("Evaluation");
            evaluation(population);
            //printPopulation(population);
            
            //System.out.println("Selection");
            PuzzleIndividual[] selected  = selection(population);
            //printPopulation(selected);
            
            //System.out.println("Reproduction");
            PuzzleIndividual[] offspring = reproduction(selected);
            //printPopulation(offspring);
            
            //System.out.println("");
            population = offspring;
            currentGeneration++;
        }
        PuzzleEvaluation peva = new PuzzleEvaluation();
        double[] fitness = peva.fitness(original, bestIndividual, false);
        System.out.println(bestIndividual+"Fitness: "+String.format("%.6f", fitness[0])+" / "
                + "DIN: "+fitness[1]+" "
                + "TD: "+String.format("%.6f", fitness[2])+"("+String.format("%.2f", 1.0/fitness[2])+") "
                + "VR: "+String.format("%.6f", fitness[3])+"("+String.format("%.2f", 1.0/fitness[3])+") "
                + "P: "+fitness[4]);
        finished = true;
    }
    
    private PuzzleIndividual[] initializePopulation(){
        PuzzleIndividual[] population = new PuzzleIndividual[PuzzleConfig.popSize];
        for(int i = 0; i < PuzzleConfig.popSize; i++){
            RandomPuzzleGenerator rpg = new RandomPuzzleGenerator();
            population[i] = rpg.newPuzzle(original);
        }
        return population;
    }
    
    private void evaluation(PuzzleIndividual[] population){
        for(int i = 0; i < population.length; i++){
            PuzzleEvaluation peva = new PuzzleEvaluation();
            double[] fitness = peva.fitness(original, population[i], false);
            population[i].setFitness(fitness[0]);
            
            if(checkImprovementConditions(fitness)){
                bestFitness = fitness;
                bestIndividual = population[i].clone();
                notifyImprovement();
            }
        }
    }

    private boolean checkImprovementConditions(double[] fitness) {
        if(fitness[0] < bestFitness[0]){
            return true;
        }
        return false;
    }
    
    private PuzzleIndividual getWinner(PuzzleIndividual a, PuzzleIndividual b){
        if(a.getFitness() < b.getFitness()){
            return a;
        }
        else return b;
    }
    
    private PuzzleIndividual[] selection(PuzzleIndividual[] population){
        Random rng = new Random(System.nanoTime());
        PuzzleIndividual[] selected = new PuzzleIndividual[PuzzleConfig.popSize];
        for(int i = 0; i < PuzzleConfig.popSize; i++){
            int index1 = 0;
            int index2 = 0;
            while(index1 == index2){
                index1 = rng.nextInt(PuzzleConfig.popSize);
                index2 = rng.nextInt(PuzzleConfig.popSize);
            }
            selected[i] = getWinner(population[index1], population[index2]).clone();
        }
        return selected;
    }
    
    private PuzzleIndividual[] reproduction(PuzzleIndividual[] selected){
        PuzzleIndividual[] offspring = new PuzzleIndividual[PuzzleConfig.popSize];
        int size = graph.getNodeCount();
        for(int i = 0; i < PuzzleConfig.popSize; i+=2){
            PuzzleOperators pop = new PuzzleOperators();
            PuzzleIndividual[] son = pop.crossover(selected[i], selected[i+1]);
            
            offspring[i  ] = pop.mutation(size, son[0]);
            offspring[i+1] = pop.mutation(size, son[1]);
            
            offspring[i  ] = pop.fixIndividual(size, offspring[i  ]);
            offspring[i+1] = pop.fixIndividual(size, offspring[i+1]);
        }
        return offspring;
    }
    
    public boolean checkImprovement(){
        if(!improvement){
            return false;
        }
        else{
            improvement = false;
            return true;
        }
    }
    
    public void notifyImprovement(){
        //System.out.println("Improvement");
        this.improvement = true;
    }

    public double[] getBestFitness() {
        return bestFitness;
    }

    public void setBestFitness(double[] bestFitness) {
        this.bestFitness = bestFitness;
    }

    public PuzzleIndividual getBestIndividual() {
        return bestIndividual;
    }

    public void setBestIndividual(PuzzleIndividual bestIndividual) {
        this.bestIndividual = bestIndividual;
    }
}
