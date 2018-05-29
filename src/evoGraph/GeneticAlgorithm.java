/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoGraph;

import java.io.File;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.graphstream.algorithm.generator.BarabasiAlbertGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.geom.Point3;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.nodePointPosition;
import parallelEvaluation.EvaluationTask;
import parallelEvaluation.Notifier;
import parallelEvaluation.Resource;
import parallelEvaluation.Process;

/**
 *
 * @author andre
 */
public class GeneticAlgorithm {
    
    protected GraphIndividual bestGraphIndividual;
    protected int currentGeneration;
    protected boolean improvement, finished;
    //double bestFitness;
    
    protected SingleRunStatistics stats;
    
    protected Process[] processes;
    
    public GeneticAlgorithm(){
        improvement = false;
        finished = false;
        currentGeneration = 0;
        //bestFitness = Double.MAX_VALUE;
        bestGraphIndividual = new GraphIndividual();
        
        stats = new SingleRunStatistics();
    }
    
    public void run(){
        improvement = false;
        currentGeneration = 0;
        //bestFitness = Double.MAX_VALUE;
        bestGraphIndividual = new GraphIndividual();
        
        //System.out.println("Initializing Population");
        GraphIndividual[] population = initializePopulation();
        
        Notifier notifier = new Notifier(this);
        Thread thread = new Thread(notifier);
        thread.start();
        
        while(currentGeneration < Config.maxGen && !finished){
            System.out.println("Gen "+currentGeneration+" F: "+bestGraphIndividual.getFitness()+" S: "+bestGraphIndividual.getNodes().size());
            //System.out.println("Evaluation");
            evaluation(population);
            //printPopulation(population);
            
            //System.out.println("Selection");
            GraphIndividual[] selected  = selection(population);
            //printPopulation(selected);
            
            //System.out.println("Reproduction");
            GraphIndividual[] offspring = reproduction(selected);
            //printPopulation(offspring);
            
            //System.out.println("");
            population = offspring;
            currentGeneration++;
        }
        finished = true;
        notifier.setFinished(true);

        if(Config.useRefinement){
            System.out.println("Final Iterative Refinement");
            finalIterativeRefinement();
        }
         
        try {
            thread.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void printPopulation(GraphIndividual[] population){
        for(int i = 0; i < Config.popSize; i++){
            System.out.println(i+" - "+population[i].toString());
        }
    }
    
    protected GraphIndividual[] initializePopulation(){
        GraphIndividual[] population = new GraphIndividual[Config.popSize];
        RandomGraphGenerator randomGen = new RandomGraphGenerator(); 
        for(int i = 0; i < Config.popSize; i++){
            Graph graph = randomGen.barabasiAlbert(String.format("%s", i));
            Decoder encoder = new Decoder(graph);
            population[i] = encoder.encode();
        }
        return population;
    }
    
    protected void evaluation(GraphIndividual[] population){
        if(Config.numberOfProcess == 1)
            sequentialEvaluation(population);
        else
            parallelEvaluation(population);
    }
    
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
        DescriptiveStatistics din = new DescriptiveStatistics();
        
        processes = new Process[Config.numberOfProcess];
        Resource<EvaluationTask> resources = new Resource<EvaluationTask>("Evaluations");
        for (int i = 0; i < processes.length; i++) {
            processes[i] = new Process("Process " + i, resources);
            processes[i].start();
        }
        EvaluationTask[] evaluations = new EvaluationTask[population.length];
        Random random = new Random();
        for (int i = 0; i < population.length; i++) {
            boolean refine = (random.nextDouble() < Config.refinementProb) && Config.useRefinement;
            evaluations[i] = new EvaluationTask(refine, population[i]);
            resources.putRegister(evaluations[i]);
        }
        resources.setFinished();
        
        for (int i = 0; i < processes.length; i++) {
            processes[i].getTasksQueue().setFinished();
        }
        for (int i = 0; i < processes.length; i++) {
            try {
                processes[i].join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        
        for (int i = 0; i < population.length; i++) {
            double[] detailedFitness = evaluations[i].getFitness();
            population[i] = evaluations[i].getIndividual();
            population[i].setFitness(detailedFitness[1]);
            
            if(checkImprovementConditions(detailedFitness)){
                if(Config.useRefinement){
                    RefinementOperator ro = new RefinementOperator();
                    population[i] = ro.geoRefinement(population[i], false);
                    population[i] = ro.exaustiveRefinement(population[i], false);
                }
                bestGraphIndividual = population[i].clone();
                
                stats.getBestGraphArchive().addLast(bestGraphIndividual);
                stats.getBestGenerationArchive().addLast(currentGeneration);

                notifyImprovement();
            }
            size.addValue(detailedFitness[0]);
            fitness.addValue(detailedFitness[1]);
            nip.addValue(detailedFitness[2]);
            eip.addValue(detailedFitness[3]);
            sp.addValue(detailedFitness[4]);
            medp.addValue(detailedFitness[5]);
            asp.addValue(detailedFitness[6]);
            uas.addValue(detailedFitness[7]);
            din.addValue(detailedFitness[8]);
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
    
    protected void sequentialEvaluation(GraphIndividual[] population){
        
        DescriptiveStatistics size = new DescriptiveStatistics();
        DescriptiveStatistics fitness = new DescriptiveStatistics();
        DescriptiveStatistics nip = new DescriptiveStatistics();
        DescriptiveStatistics eip = new DescriptiveStatistics();
        DescriptiveStatistics sp = new DescriptiveStatistics();
        DescriptiveStatistics medp = new DescriptiveStatistics();
        DescriptiveStatistics asp = new DescriptiveStatistics();
        DescriptiveStatistics uas = new DescriptiveStatistics();
        DescriptiveStatistics din = new DescriptiveStatistics();
        
        Random random = new Random(System.nanoTime());
        for(int i = 0; i < Config.popSize; i++){
            
            Evaluation eva = new Evaluation();
            double[] detailedFitness = eva.detailedFitness(population[i], false);
            population[i].setFitness(detailedFitness[1]);
            if (Config.useRefinement) {
                if (random.nextDouble() < Config.refinementProb) {
                    RefinementOperator ro = new RefinementOperator();
                    //System.out.print("Refinement: from "+population[i].toString()+" to ");
                    population[i] = ro.geoRefinement(population[i], false);
                    population[i] = ro.exaustiveRefinement(population[i], false);
                    //System.out.println(population[i].toString());
                }
            }
            
            if(checkImprovementConditions(detailedFitness)){
                //System.out.println("Improvement: "+population[i].getFitness());
                if (Config.useRefinement) {
                    RefinementOperator ro = new RefinementOperator();
                    population[i] = ro.geoRefinement(population[i], false);
                    population[i] = ro.exaustiveRefinement(population[i], false);
                }
                bestGraphIndividual = population[i].clone();
                
                stats.getBestGraphArchive().addLast(bestGraphIndividual);
                stats.getBestGenerationArchive().addLast(currentGeneration);
                
                // re-evaluate for console debug only
                /*Evaluation eva = new Evaluation();
                Decoder barabasiDecoder = new Decoder(bestIndividual);
                bestIndividual = barabasiDecoder.barabasiAlbertGraph();
                bestIndividual.setFitness(eva.fitness(bestIndividual, true));*/
                
                notifyImprovement();
            }
            size.addValue(detailedFitness[0]);
            fitness.addValue(detailedFitness[1]);
            nip.addValue(detailedFitness[2]);
            eip.addValue(detailedFitness[3]);
            sp.addValue(detailedFitness[4]);
            medp.addValue(detailedFitness[5]);
            asp.addValue(detailedFitness[6]);
            uas.addValue(detailedFitness[7]);
            din.addValue(detailedFitness[8]);
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
    
    protected void finalIterativeRefinement(){
        RefinementOperator ref = new RefinementOperator();
        Decoder barabasiDecoder = new Decoder(bestGraphIndividual);
        GraphIndividual connectedIndividual = barabasiDecoder.barabasiAlbertGraph();

        Evaluation evaluator = new Evaluation();
        double[] candidate = evaluator.detailedFitness(connectedIndividual, false);
        double[] best;
        do{
            best = candidate;
            connectedIndividual = ref.geoRefinement(connectedIndividual, false);
            connectedIndividual = ref.exaustiveRefinement(connectedIndividual, false);
            candidate = evaluator.detailedFitness(connectedIndividual, false);
        } while(candidate[1] < best[1]);
        bestGraphIndividual = connectedIndividual.clone();
    }
    
    protected boolean checkImprovementConditions(double[] detailedFitness){
        if(detailedFitness[1] < bestGraphIndividual.getFitness()){
            return true;
        }
        else{
            if(Config.useMaximizeNodeCount && detailedFitness[1] == bestGraphIndividual.getFitness()){
                if(detailedFitness[0] > bestGraphIndividual.getNodes().size())
                    return true;
            }
        } 
        return false;
    }
    
    protected GraphIndividual getWinner(GraphIndividual a, GraphIndividual b){
        if(a.getFitness() < b.getFitness()){
            return a;
        }
        else{
            if(Config.useMaximizeNodeCount && a.getFitness() == b.getFitness()){
                if(a.getNodes().size() > b.getNodes().size()){
                    return a;
                }
                else{
                    return b;
                }
            }
            else return b;
        }
    }
    
    protected GraphIndividual[] selection(GraphIndividual[] population){
        Random rng = new Random(System.nanoTime());
        GraphIndividual[] selected = new GraphIndividual[Config.popSize];
        for(int i = 0; i < Config.popSize; i++){
            int index1 = 0;
            int index2 = 0;
            while(index1 == index2){
                index1 = rng.nextInt(Config.popSize);
                index2 = rng.nextInt(Config.popSize);
            }
            selected[i] = getWinner(population[index1], population[index2]).clone();
        }
        return selected;
    }
    
    protected GraphIndividual[] reproduction(GraphIndividual[] selected){
        GraphIndividual[] offspring = new GraphIndividual[Config.popSize];
        for(int i = 0; i < Config.popSize; i+=2){
            GeneticOperators gop = new GeneticOperators();
            GraphIndividual[] son = gop.crossover(selected[i], selected[i+1]);
            
            offspring[i  ] = gop.mutation(son[0]);
            offspring[i+1] = gop.mutation(son[1]);
            
            offspring[i  ] = gop.fixInvalidIndividual(offspring[i  ]);
            offspring[i+1] = gop.fixInvalidIndividual(offspring[i+1]);
        }
        return offspring;
    }
    
    public void exportGraph(Graph g){
        String hashString = ""+g.hashCode();
        File dir = new File(Config.folder, hashString);
        if(!dir.exists()){
            dir.mkdir();
        }
        else{
            System.err.println("Dir "+dir+" already exists!");
        }
        int maxX = 0;
        int maxY = 0;
        for(Node node : g.getEachNode()){
            Point3 p = nodePointPosition(node);
            //System.out.println(node.getId()+": "+p.toString());
            maxX = (int) Math.max(maxX, p.x + ((double) node.getAttribute("width")/2));
            maxY = (int) Math.max(maxY, p.y + ((double) node.getAttribute("height")/2));
        }
        EvoJSONFileWriter fw = new EvoJSONFileWriter(g);
        System.out.println("Exporting Map\n"+
                dir+"\\data_"+g.hashCode()+".json\n"+
                dir+"\\map_"+g.hashCode()+".json");
        fw.exportDataJSON(dir+"\\data_"+g.hashCode()+".json",
                "GraphStream", maxX, maxY, true);
        fw.exportMapJSON(dir+"\\map_"+g.hashCode()+".json",
                "Evolutionary"+BarabasiAlbertGenerator.class.getSimpleName(), true);
        
        // export PNG
        GraphImageBuilder gib = new GraphImageBuilder(dir+"\\img_"+g.hashCode()+".png");
        gib.buildImage(g);
        
        ConfigManager cm = new ConfigManager();
        cm.saveRunTime(dir+"\\config_"+hashString+".json");  
    }
    
    public synchronized void wakeUp() {
        this.notifyAll();
    }
    
    public GraphIndividual getBestIndividual() {
        return bestGraphIndividual;
    }

    public void setBestIndividual(GraphIndividual bestIndividual) {
        this.bestGraphIndividual = bestIndividual;
    }

    public int getCurrentGeneration() {
        return currentGeneration;
    }

    public void setCurrentGeneration(int currentGeneration) {
        this.currentGeneration = currentGeneration;
    }

    public boolean isImprovement() {
        return improvement;
    }

    public void setImprovement(boolean improvement) {
        this.improvement = improvement;
    }
    
    public synchronized boolean checkImprovement(){
        if(!isImprovement()){
            return false;
        }
        else{
            setImprovement(false);
            return true;
        }
    }
    
    public synchronized void notifyImprovement(){
        //System.out.println("Improvement");
        this.improvement = true;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public SingleRunStatistics getStats() {
        return stats;
    }

    public void setStats(SingleRunStatistics stats) {
        this.stats = stats;
    }

    public Process[] getProcesses() {
        return processes;
    }

    public void setProcesses(Process[] processes) {
        this.processes = processes;
    }
    
}
