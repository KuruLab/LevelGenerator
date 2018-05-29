/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package twoStageEvo;

import evoGraph.Config;
import evoGraph.Decoder;
import evoGraph.GeneticAlgorithm;
import evoGraph.GraphIndividual;
import evoGraph.RefinementOperator;
import evoPuzzle.PuzzleGA;
import evoPuzzle.PuzzleIndividual;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.graphstream.graph.Graph;
import parallelEvaluation.Notifier;
import parallelEvaluation.Process;
import parallelEvaluation.Resource;

/**
 *
 * @author andre
 */
public class TwoStageGA extends GeneticAlgorithm{
    
    private PuzzleIndividual bestPuzzleIndividual;
    
    /*
    * Detailed Graph Fitness:
    *   [0] = The number of nodes N
    *   [1] = The total fitness value, i.e, the sum of each subsequent fitness value
    *   [2] = Area of Node Intersection
    *   [3] = Number of Edge Intersections
    *   [4] = Size Penalty, delta^2 * 1000
    *   [5] = Minimum Edge Distance Penalty
    *   [6] = Average Shortest Path Lenght
    *   [7] = Undesired Angle Sum
    *   DIN (Distance from Ideal Nonlinearity) value was transfered to the Puzzle evaluation
    */
    private double[] bestGraphFitness;
    
    /*
    * Detailed Puzzle Fitness
    *   [0] = The total fitness value, i.e, the sum of each subsequent fitness value
    *   [1] = DIN (Distance from Ideal Nonlinearity)
    *   [2] = 1/TD, where TD = total travelled distance (Travelled Distance)
    *   [3] = 1/VR, where VR = number of unique visited rooms (Visited Rooms)
    *   [4] = Unsolvable Puzzle Penalty
    */
    private double[] bestPuzzleFitness; // detailed version, index 0 is the total fitness value

    public TwoStageGA() {
        super();
        bestGraphIndividual = new GraphIndividual();
        bestPuzzleIndividual = new PuzzleIndividual();
        bestGraphFitness = new double[8];
        for(int i = 0; i < bestGraphFitness.length; i++)
            bestGraphFitness[i] = Double.MAX_VALUE;
        bestPuzzleFitness = new double[5];
        for(int i = 0; i < bestPuzzleFitness.length; i++)
            bestPuzzleFitness[i] = Double.MAX_VALUE;
    }
    
    @Override
    public void run(){
        improvement = false;
        currentGeneration = 0;

        System.out.println("Initializing Population");
        GraphIndividual[] gPopulation = initializeGraphPopulation(); // graph individual population
        
        Notifier notifier = new Notifier(this);
        Thread thread = new Thread(notifier);
        thread.start();
        
        while(currentGeneration < Config.maxGen && !finished){
            System.out.println("Gen "+currentGeneration+" F: "+bestGraphIndividual.getFitness()+" S: "+bestGraphIndividual.getNodes().size());
            //System.out.println("Evaluation");
            twoStageEvaluation(gPopulation);
            //printPopulation(population);
            
            //System.out.println("Selection");
            GraphIndividual[] selected  = graphSelection(gPopulation);
            //printPopulation(selected);
            
            //System.out.println("Reproduction");
            GraphIndividual[] offspring = graphReproduction(selected);
            //printPopulation(offspring);
            
            //System.out.println("");
            gPopulation = offspring;
            currentGeneration++;
        }
        if(!finished){
            if(Config.useRefinement){
                System.out.println("Final Iterative Refinement");
                finalIterativeRefinement();
            }

            sequentialGeneratePuzzle(bestGraphIndividual);
        }
        finished = true;
        notifier.setFinished(true);
        try {
            thread.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected GraphIndividual[] initializeGraphPopulation(){
        return initializePopulation();
    }
    
    protected GraphIndividual[] graphSelection(GraphIndividual[] pop){
        return selection(pop);
    }
    
    protected GraphIndividual[] graphReproduction(GraphIndividual[] pop){
        return reproduction(pop);
    }
    
    protected void twoStageEvaluation(GraphIndividual[] pop){
        if(Config.numberOfProcess == 1)
            sequentialEvaluation(pop);
        else
            parallelEvaluation(pop);
    }
    
    @Override
    protected void sequentialEvaluation(GraphIndividual[] population){
        DescriptiveStatistics size = new DescriptiveStatistics();
        DescriptiveStatistics fitness = new DescriptiveStatistics();
        DescriptiveStatistics nip = new DescriptiveStatistics();
        DescriptiveStatistics eip = new DescriptiveStatistics();
        DescriptiveStatistics sp = new DescriptiveStatistics();
        DescriptiveStatistics medp = new DescriptiveStatistics();
        DescriptiveStatistics asp = new DescriptiveStatistics();
        DescriptiveStatistics uas = new DescriptiveStatistics();
        
        //DescriptiveStatistics din = new DescriptiveStatistics();
        
        Random random = new Random(System.nanoTime());
        for(int i = 0; i < Config.popSize; i++){
            
            TwoStageEvaluation eva = new TwoStageEvaluation();
            double[] graphFitness = eva.detailedGraphFitness(population[i], false);
            population[i].setFitness(graphFitness[1]);
            if (Config.useRefinement) {
                if (random.nextDouble() < Config.refinementProb) {
                    RefinementOperator ro = new TwoStageRefinementOperator();
                    //System.out.print("Refinement: from "+population[i].toString()+" to ");
                    population[i] = ro.geoRefinement(population[i], false);
                    population[i] = ro.exaustiveRefinement(population[i], false);
                    //System.out.println(population[i].toString());
                }
            }
            
            if(checkImprovementConditions(graphFitness)){
                //System.out.println("Improvement: "+population[i].getFitness());
                if (Config.useRefinement) {
                    RefinementOperator ro = new TwoStageRefinementOperator();
                    population[i] = ro.geoRefinement(population[i], false);
                    population[i] = ro.exaustiveRefinement(population[i], false);
                }
                bestGraphIndividual = population[i].clone();
                bestGraphIndividual.setFitness(graphFitness[1]);
                bestGraphFitness = graphFitness;
                
                //sequentialGeneratePuzzle(bestGraphIndividual);
                
                stats.getBestGraphArchive().addLast(bestGraphIndividual);
                stats.getBestGenerationArchive().addLast(currentGeneration);

                notifyImprovement();
            }
            size.addValue(graphFitness[0]);
            fitness.addValue(graphFitness[1]);
            nip.addValue(graphFitness[2]);
            eip.addValue(graphFitness[3]);
            sp.addValue(graphFitness[4]);
            medp.addValue(graphFitness[5]);
            asp.addValue(graphFitness[6]);
            uas.addValue(graphFitness[7]);
            //din.addValue(graphFitness[8]);
        }
        stats.getSizeMap().add(size);
        stats.getFitnessMap().add(fitness);
        stats.getNipMap().add(nip);
        stats.getEipMap().add(eip);
        stats.getSpMap().add(sp);
        stats.getMedpMap().add(medp);
        stats.getAspMap().add(asp);
        stats.getUasMap().add(uas);
        //stats.getDinMap().add(din);
    }
    
    @Override
    protected void parallelEvaluation(GraphIndividual[] population){
        //System.out.println("Starting Parallel Evaluation");
        DescriptiveStatistics size = new DescriptiveStatistics();
        DescriptiveStatistics fitness = new DescriptiveStatistics();
        DescriptiveStatistics nip = new DescriptiveStatistics();
        DescriptiveStatistics eip = new DescriptiveStatistics();
        DescriptiveStatistics sp = new DescriptiveStatistics();
        DescriptiveStatistics medp = new DescriptiveStatistics();
        DescriptiveStatistics asp = new DescriptiveStatistics();
        DescriptiveStatistics uas = new DescriptiveStatistics();
        
        //DescriptiveStatistics din = new DescriptiveStatistics();
        
        processes = new parallelEvaluation.Process[Config.numberOfProcess];
        Resource<TwoStageEvaluationTask> resources = new Resource<>("Evaluations");
        for (int i = 0; i < processes.length; i++) {
            processes[i] = new parallelEvaluation.Process("Process " + i, resources);
            processes[i].start();
        }
        TwoStageEvaluationTask[] evaluations = new TwoStageEvaluationTask[population.length];
        Random random = new Random(System.nanoTime());
        
        for (int i = 0; i < population.length; i++) {
            boolean refine = (random.nextDouble() < Config.refinementProb) && Config.useRefinement;
            evaluations[i] = new TwoStageEvaluationTask(refine, population[i]);
            resources.putRegister(evaluations[i]);
        }
        resources.setFinished();
        
        for (Process processe : processes) {
            processe.getTasksQueue().setFinished();
        }
        for (Process processe : processes) {
            try {
                processe.join();
            }catch (InterruptedException ex) {
            }
        }
        
        for (int i = 0; i < population.length; i++) {
            double[] graphFitness = evaluations[i].getFitness();
            population[i] = evaluations[i].getIndividual();
            population[i].setFitness(graphFitness[1]);
            
            if(checkImprovementConditions(graphFitness)){
                //System.out.println("Improvement: "+population[i].getFitness());
                if (Config.useRefinement) {
                    RefinementOperator ro = new TwoStageRefinementOperator();
                    population[i] = ro.geoRefinement(population[i], false);
                    population[i] = ro.exaustiveRefinement(population[i], false);
                }
                bestGraphIndividual = population[i].clone();
                bestGraphIndividual.setFitness(graphFitness[1]);
                bestGraphFitness = graphFitness;
                
                //sequentialGeneratePuzzle(bestGraphIndividual);
                
                stats.getBestGraphArchive().addLast(bestGraphIndividual);
                stats.getBestGenerationArchive().addLast(currentGeneration);

                notifyImprovement();
            }
            size.addValue(graphFitness[0]);
            fitness.addValue(graphFitness[1]);
            nip.addValue(graphFitness[2]);
            eip.addValue(graphFitness[3]);
            sp.addValue(graphFitness[4]);
            medp.addValue(graphFitness[5]);
            asp.addValue(graphFitness[6]);
            uas.addValue(graphFitness[7]);
            //din.addValue(graphFitness[8]);
        }
        stats.getSizeMap().add(size);
        stats.getFitnessMap().add(fitness);
        stats.getNipMap().add(nip);
        stats.getEipMap().add(eip);
        stats.getSpMap().add(sp);
        stats.getMedpMap().add(medp);
        stats.getAspMap().add(asp);
        stats.getUasMap().add(uas);
        //stats.getDinMap().add(din);
        //System.out.println("Parallel Evaluation Finished");
    }
    
    public void sequentialGeneratePuzzle(GraphIndividual individual){
        System.out.println("Starting Puzzle Generator");
        try {
            Decoder barabasiDec = new Decoder(individual.clone());
            GraphIndividual barabasiInd = barabasiDec.barabasiAlbertGraph();
            Decoder graphDec = new Decoder(barabasiInd);
            Graph graph = graphDec.decode();
            
            PuzzleGA pga = new PuzzleGA(graph);
            Thread t = new Thread(pga);
            t.start();
            t.join();
            
            updatePuzzle(pga.getBestIndividual(), pga.getBestFitness());
            
            stats.getBestPuzzleArchive().addLast(bestPuzzleIndividual);
            stats.getDinMap().addValue(pga.getBestFitness()[1]);
            stats.getTdMap().addValue(pga.getBestFitness()[2]);
            stats.getVrMap().addValue(pga.getBestFitness()[3]);
            stats.getUpMap().addValue(pga.getBestFitness()[4]);
            
        } catch (InterruptedException ex) {
            Logger.getLogger(TwoStageGA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void parallelGeneratePuzzle(GraphIndividual individual){
        Thread thread = new Thread(() -> {
            Decoder barabasiDec = new Decoder(individual.clone());
            GraphIndividual barabasiInd = barabasiDec.barabasiAlbertGraph();
            Decoder graphDec = new Decoder(barabasiInd);
            Graph graph = graphDec.decode();
            
            PuzzleGA pga = new PuzzleGA(graph);
            Thread t = new Thread(pga);
            t.start();
            try {
                t.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(TwoStageGA.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            updatePuzzle(pga.getBestIndividual(), pga.getBestFitness());
            
            stats.getBestPuzzleArchive().addLast(bestPuzzleIndividual);
            stats.getDinMap().addValue(pga.getBestFitness()[1]);
            stats.getTdMap().addValue(pga.getBestFitness()[2]);
            stats.getVrMap().addValue(pga.getBestFitness()[3]);
            stats.getUpMap().addValue(pga.getBestFitness()[4]);
        });
        thread.start();
    }
    
    public synchronized void updatePuzzle(PuzzleIndividual puzzle, double[] fitness){
        if(fitness[0] < bestPuzzleFitness[0]){
            bestPuzzleFitness = fitness;
            bestPuzzleIndividual = puzzle;
        }
    }
    
    @Override
    protected void finalIterativeRefinement(){
        RefinementOperator ref = new TwoStageRefinementOperator();
        Decoder barabasiDecoder = new Decoder(bestGraphIndividual);
        GraphIndividual connectedIndividual = barabasiDecoder.barabasiAlbertGraph();

        TwoStageEvaluation evaluator = new TwoStageEvaluation();
        double[] candidate = evaluator.detailedFitness(connectedIndividual, false);
        double[] best;
        int count = 0;
        do{
            best = candidate;
            System.out.println("Iteration "+count+" F: "+best[1]);
            connectedIndividual = ref.geoRefinement(connectedIndividual, false);
            connectedIndividual = ref.exaustiveRefinement(connectedIndividual, false);
            candidate = evaluator.detailedFitness(connectedIndividual, false);
            count++;
        } while(candidate[1] < best[1]);
        bestGraphIndividual = connectedIndividual.clone();
    }

    public PuzzleIndividual getBestPuzzleIndividual() {
        return bestPuzzleIndividual;
    }

    public void setBestPuzzleIndividual(PuzzleIndividual bestPuzzleIndividual) {
        this.bestPuzzleIndividual = bestPuzzleIndividual;
    }

    public double[] getBestGraphFitness() {
        return bestGraphFitness;
    }

    public void setBestGraphFitness(double[] bestGraphFitness) {
        this.bestGraphFitness = bestGraphFitness;
    }

    public double[] getBestPuzzleFitness() {
        return bestPuzzleFitness;
    }

    public void setBestPuzzleFitness(double[] bestPuzzleFitness) {
        this.bestPuzzleFitness = bestPuzzleFitness;
    }
}
