/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package experiments;

import evoLevel.LevelConfig;
import evoLevel.LevelDecoder;
import evoLevel.LevelEvaluation;
import io.EvoJSONFileReader;
import evoLevel.LevelGA;
import evoLevel.LevelIndividual;
import evoLevel.LevelStatistics;
import graphstream.GraphStreamUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.graphstream.graph.Graph;

/**
 *
 * @author andre
 */
public class Experiment {
    
    public int current;
    public int executions;
    public Graph[] graph;
    public DescriptiveStatistics time;
    public DescriptiveStatistics fitness;
    public DescriptiveStatistics size; // Size, number of nodes
    public DescriptiveStatistics asp; // Average Shortest Path
    public DescriptiveStatistics uas; // Undesired Angle Sum
    public DescriptiveStatistics area; // Area, width x height
    
    //public DescriptiveStatistics din; // Distance from Ideal Nonlinearity
    
    public static void main(String args[]){  
       Experiment exp = new Experiment();
       exp.run();
    }
    
    public Experiment() {
        current = 0;
        executions = 30;
        graph = new Graph[executions];
        time = new DescriptiveStatistics(executions);
        fitness = new DescriptiveStatistics(executions);
        asp = new DescriptiveStatistics(executions);
        uas = new DescriptiveStatistics(executions);
        //din = new DescriptiveStatistics(executions);
        area = new DescriptiveStatistics(executions);
    }
    
    public void run(){
        setup();
        verifyFolder();
        execute();
        readResults();
    }
    
    public void setup(){
        LevelConfig.borderSize = 15;
        LevelConfig.edgeSize = 6;

        LevelConfig.minNodeCount = 25;
        LevelConfig.maxNodeCount = 100;

        LevelConfig.minEdgeDistance = 5;
    
        LevelConfig.areaIntersectionPenalty =  100;
        LevelConfig.edgeIntersectionPenalty = 1000;
        LevelConfig.nodeCountPenalty        = 1000;
        LevelConfig.edgeDistancePentalty    = 1000;

        LevelConfig.minNodeSize = 15.0;
        LevelConfig.maxNodeSize = 30.0;
        LevelConfig.scaleFactor = 50;
        LevelConfig.expansionProb = 0.05; // used only on graphX crossover
        
        LevelConfig.barabasiFactor = 5; // delta
        LevelConfig.maxLinksPerStep = 2; // m

        LevelConfig.crossoverProb  =  0.90;
        LevelConfig.mutationProb   =  0.10;
        LevelConfig.refinementProb =  0.01;

        LevelConfig.nodeXLeap = 1.1;
        LevelConfig.nodeYLeap = 1.1;
        LevelConfig.nodeZLeap = 0;

        LevelConfig.popSize = 100;
        LevelConfig.maxGen  = 100;
        
        LevelConfig.desiredAngles = new int[3];
        LevelConfig.desiredAngles[0] = 0;
        LevelConfig.desiredAngles[1] = 90;
        LevelConfig.desiredAngles[2] = 180;
        
        LevelConfig.idealNonLinearity = 3;
    
        LevelConfig.useDesiredAngles = true;
        LevelConfig.useAverageShortestPath = true;
        LevelConfig.useMaximizeNodeCount = true;
        LevelConfig.useIdealNonLinearity = true;
        
        LevelConfig.useRefinement = true;
        
        LevelConfig.folder = "D:\\Mega\\posdoc\\MapGenerator\\experiments\\stantard\\";
        LevelConfig.numberOfProcess = 16;
    }
    
    public void verifyFolder(){
        File folder = new File(LevelConfig.folder);
        if(!folder.exists())
            folder.mkdir();
        ArrayList<File> results = new ArrayList<>();
        for(int i = 0; i < folder.listFiles().length; i++){
            if(folder.listFiles()[i].isDirectory())
                results.add(folder.listFiles()[i]);
        }
        if(results.size() > 0){
            int remaining = Math.max(0, executions - results.size());
            JOptionPane.showMessageDialog(null, "There are already "+results.size()+" results in the speficied folder out of "+executions+" required.", "Atention!", JOptionPane.WARNING_MESSAGE);
            String opt = JOptionPane.showInputDialog("Choose an option:\n"
                    + "[0] - Proceed anyway (new "+executions+" executions).\n"
                    + "[1] - Proceed just "+remaining+" more times.\n"
                    + "[2] - Only read existing results.\n"
                    + "[3] - Exit.");
            if(opt.compareTo("0")==0){
                current = 0;
            }
            else if(opt.compareTo("1")==0){
                current = results.size();
            }
            else if(opt.compareTo("2")==0){
                current = executions;
            }
            else{
                System.exit(0);
            }
        }
    }
    
    public void execute(){
        for(int i = current; i < executions; i++){
            System.out.println("\n++Execution number "+(i+1)+"++\n");
            
            long initialTime = System.currentTimeMillis();
            LevelGA ga = new LevelGA();
            ga.run();
            long finalTime = System.currentTimeMillis();
            time.addValue(finalTime - initialTime);
            
            LevelEvaluation eva = new LevelEvaluation();
            double[] detailedFitness = eva.detailedFitness(ga.getBestIndividual(), true);
            LevelDecoder barabasiDecoder = new LevelDecoder(ga.getBestIndividual());
            LevelIndividual barabasiIndividual = barabasiDecoder.barabasiAlbertGraph();
            LevelDecoder graphDecoder = new LevelDecoder(barabasiIndividual);
            graph[i] = graphDecoder.decode();
            //DungeonGenerator dgg = new DungeonGenerator(graph[i]);
            //dgg.generate();
            ga.exportGraph(graph[i]);
            exportSizePerGen(graph[i], ga.getStats());
        }
        if(time.getN()==0)
            time.addValue(0.0);
    }
    
    public void exportSizePerGen(Graph g, LevelStatistics stats){
        String genLine = "gen: ";
        String sizeLine = "size: ";
        for(int i = 0; i < stats.getBestGenerationArchive().size(); i++)
            genLine += stats.getBestGenerationArchive().get(i)+" ";
        for(int i = 0; i < stats.getBestGraphArchive().size(); i++)
            sizeLine += stats.getBestGraphArchive().get(i).getNodes().size()+" ";
        
        String hashString = ""+g.hashCode();
        File dir = new File(LevelConfig.folder, hashString);
        System.out.println("Exporting Stats\n"+
                dir+"\\sizePerGen_"+g.hashCode()+".txt");
        try {
            PrintWriter pw = new PrintWriter(dir+"\\sizePerGen_"+g.hashCode()+".txt");
            pw.printf(genLine);
            pw.println();
            pw.printf(sizeLine);
            pw.flush();
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void readResults(){
        File folder = new File(LevelConfig.folder);
        ArrayList<File> results = new ArrayList<>();
        for(int i = 0; i < folder.listFiles().length; i++){
            if(folder.listFiles()[i].isDirectory())
                results.add(folder.listFiles()[i]);
        }
        
        executions = results.size();
        
        graph = new Graph[executions];
        fitness = new DescriptiveStatistics(executions);
        size = new DescriptiveStatistics(executions);
        asp = new DescriptiveStatistics(executions);
        uas = new DescriptiveStatistics(executions);
        //din = new DescriptiveStatistics(executions);
        area = new DescriptiveStatistics(executions);
        
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        int minIndex = 0, minHash = 0;
        int maxIndex = 0, maxHash = 0;
        double mingArea = Double.MAX_VALUE;
        double maxgArea = Double.MIN_VALUE;
        int[] minBounds = new int[3];
        int[] maxBounds = new int[3];
        
        Hashtable<Integer, ArrayList<Integer>> sizePerGeneration = new Hashtable();
        for(int i = 0; i < LevelConfig.maxGen; i++)
            sizePerGeneration.put(i, new ArrayList<>());

        for(int i = 0; i < results.size(); i++){
            File dir = results.get(i);
            String name = dir.getPath()+File.separator+"map_"+dir.getName()+".json";
            System.out.println(name);
            EvoJSONFileReader reader = new EvoJSONFileReader(name);
            graph[i] = reader.parseJson();
            double[] detailedFitness = evaluate(graph[i]); 
            
            size.addValue(detailedFitness[0]);
            fitness.addValue(detailedFitness[1]);
            asp.addValue(detailedFitness[6]);
            uas.addValue(detailedFitness[7]);
            //din.addValue(detailedFitness[8]);
            
            double graphArea = (new GraphStreamUtil()).getGraphAreaWithBorder(graph[i], LevelConfig.borderSize);
            area.addValue(graphArea);
            
            if(detailedFitness[1] < min){
                min = detailedFitness[1];
                minIndex = i;
            }
            if(detailedFitness[1] > max){
                max = detailedFitness[1];
                maxIndex = i;
            }
            
            if(graphArea < mingArea){
                mingArea = graphArea;
                minBounds = (new GraphStreamUtil()).getGraphBoundsWithBorder(graph[i], LevelConfig.borderSize);
            }
            if(graphArea > maxgArea){
                maxgArea = graphArea;
                maxBounds = (new GraphStreamUtil()).getGraphBoundsWithBorder(graph[i], LevelConfig.borderSize);
            }
            // Size per Generation
            String spgName = dir.getPath()+File.separator+"sizePerGen_"+dir.getName()+".txt";
            System.out.println(name);
            try {
                FileReader fr = new FileReader(spgName);
                Scanner scan = new Scanner(fr);
                String genLine = scan.nextLine();
                String sizeLine = scan.nextLine();
                String[] genLineArray = genLine.substring(genLine.indexOf(": ")+2).split(" ");
                String[] sizeLineArray = sizeLine.substring(sizeLine.indexOf(": ")+2).split(" ");
                System.out.println(Arrays.toString(genLineArray));
                System.out.println(Arrays.toString(sizeLineArray));
                for(int j = 0; j < genLineArray.length; j++){
                    ArrayList<Integer> list = sizePerGeneration.get(Integer.parseInt(genLineArray[j]));
                    list.add(Integer.parseInt(sizeLineArray[j]));
                    sizePerGeneration.replace(Integer.parseInt(genLineArray[j]), list);
                }
                    
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        double minFitness = fitness.getMin();
        double meanFitness = fitness.getMean();
        double stdFitness = fitness.getStandardDeviation();
        double maxFitness = fitness.getMax();
        
        double minS = size.getMin();
        double meanS = size.getMean();
        double stdS = size.getStandardDeviation();
        double maxS = size.getMax();
        
        double minASP = asp.getMin();
        double meanASP = asp.getMean();
        double stdASP = asp.getStandardDeviation();
        double maxASP = asp.getMax();
        
        double minUAS = uas.getMin();
        double meanUAS = uas.getMean();
        double stdUAS = uas.getStandardDeviation();
        double maxUAS = uas.getMax();
        
        //double minDIN = din.getMin();
        //double meanDIN = din.getMean();
        //double stdDIN = din.getStandardDeviation();
        //double maxDIN = din.getMax();
        
        double minTime = time.getMin();
        double meanTime = time.getMean();
        double stdTime = time.getStandardDeviation();
        double maxTime = time.getMax();
        
        double minArea = area.getMin();
        double meanArea = area.getMean();
        double stdArea = area.getStandardDeviation();
        double maxArea = area.getMax();
        
        String tableLine =   "N & $"+String.format("%.3f",minS)+"$ & $"+String.format("%.3f",meanS)+"_{ \\pm "+String.format("%.3f",stdS)+"}$ & $"+String.format("%.3f",maxS)+"$ \\\\\n"
                           + "ASP & $"+String.format("%.3f",minASP)+"$ & $"+String.format("%.3f",meanASP)+"_{ \\pm "+String.format("%.3f",stdASP)+"}$ & $"+String.format("%.3f",maxASP)+"$ \\\\\n"
                           + "UAS & $"+String.format("%.3f",minUAS)+"$ & $"+String.format("%.3f",meanUAS)+"_{ \\pm "+String.format("%.3f",stdUAS)+"}$ & $"+String.format("%.3f",maxUAS)+"$ \\\\\n"
                           //+ "DIN & $"+String.format("%.3f",minDIN)+"$ & $"+String.format("%.3f",meanDIN)+"_{ \\pm "+String.format("%.3f",stdDIN)+"}$ & $"+String.format("%.3f",maxDIN)+"$ \\\\\n"
                           + "Fitness & $"+String.format("%.3f",minFitness)+"$ & $"+String.format("%.3f",meanFitness)+"_{ \\pm "+String.format("%.3f",stdFitness)+"}$ & $"+String.format("%.3f",maxFitness)+"$ \\\\\n"
                           + "Time (s) & $"+String.format("%.3f",minTime/1000)+"$ & $"+String.format("%.3f",meanTime/1000)+"_{ \\pm "+String.format("%.3f",stdTime/1000)+"}$ & $"+String.format("%.3f",maxTime/1000)+"$\\\\\n"
                           + "Area & $"+String.format("%.3f",minArea)+"$ & $"+String.format("%.3f",meanArea)+"_{ \\pm "+String.format("%.3f",stdArea)+"}$ & $"+String.format("%.3f",maxArea)+"$\\\\\n"
                ;
        //System.out.println(tableLine); 
        //System.out.println("Min Index: "+minIndex);
        //System.out.println("Max Index: "+maxIndex);
        
        tableLine += "\n\n"
                + "Min Index: "+minIndex+" Min Hash: "+minHash+"\n"
                + "Max Index: "+maxIndex+" Max Hash: "+maxHash+"\n"
                + "Min Area: "+mingArea+" Dimensions: "+Arrays.toString(minBounds)+"\n"
                + "Max Area: "+maxgArea+" Dimensions: "+Arrays.toString(maxBounds)+"\n";
                    
        try {
            PrintWriter pw = new PrintWriter(LevelConfig.folder+"table.txt");
            pw.printf(tableLine);
            pw.flush();
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Results done at: "+LevelConfig.folder+"/table.txt");
        
        String sizePerGen = "";
        for(int i = 0; i < LevelConfig.maxGen; i++){
            ArrayList<Integer> list = sizePerGeneration.get(i);
            sizePerGen += i+" ";
            for(int j = 0; j < list.size(); j++){
                sizePerGen += list.get(j)+" ";
            }
            sizePerGen += "\n";
        }
        try {
            PrintWriter pw = new PrintWriter(LevelConfig.folder+"sizePerGenMatrix.txt");
            pw.printf(sizePerGen);
            pw.flush();
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public double[] evaluate(Graph graph){
        LevelDecoder encoder = new LevelDecoder(graph);
        LevelIndividual encodedIndividual = encoder.encode();
        
        LevelEvaluation eva = new LevelEvaluation();
        double[] detailedFitness = eva.detailedFitness(encodedIndividual, false);
        return detailedFitness;
    }
}
