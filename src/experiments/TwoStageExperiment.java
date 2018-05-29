/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package experiments;

import evoGraph.Config;
import evoGraph.Decoder;
import evoGraph.EvoJSONFileReader;
import evoGraph.GraphIndividual;
import evoPuzzle.PuzzleConfig;
import evoPuzzle.PuzzleDecoder;
import evoPuzzle.PuzzleIndividual;
import graphstream.GraphStreamUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.graphstream.graph.Graph;
import twoStageEvo.TwoStageEvaluation;
import twoStageEvo.TwoStageGA;

/**
 *
 * @author andre
 */
public class TwoStageExperiment extends Experiment {
    
    public DescriptiveStatistics pfitness;
    public DescriptiveStatistics td;
    public DescriptiveStatistics vr;
    
    public TwoStageExperiment() {
        super();
    }
    
    public static void main(String args[]){
        TwoStageExperiment tsexp = new TwoStageExperiment();
        tsexp.run();
    }
    
    @Override
    public void run(){
        graphConfigSetup();
        puzzleConfigSetup();
        verifyFolder();
        execute();
        readResults();
    }
    
    public void puzzleConfigSetup(){
        PuzzleConfig.popSize = 100;
        PuzzleConfig.maxGen  =  50;
        PuzzleConfig.crossoverProb  =  0.9;
        PuzzleConfig.mutationProb   =  0.1;
    }
    
    public void graphConfigSetup(){
        super.setup();
        Config.maxGen  = 200;
        Config.folder = "D:\\Mega\\posdoc\\MapGenerator\\experiments\\twoStageStantard\\";
    }
    
    @Override
    public void execute(){
        for(int i = current; i < executions; i++){
            System.out.println("\n++Execution number "+(i+1)+"++\n");
            
            long initialTime = System.currentTimeMillis();
            TwoStageGA tsGA = new TwoStageGA();
            tsGA.run();
            long finalTime = System.currentTimeMillis();
            time.addValue(finalTime - initialTime);
            
            GraphIndividual individual = tsGA.getBestIndividual();
            PuzzleIndividual puzzle = tsGA.getBestPuzzleIndividual();
            
            TwoStageEvaluation tsEva = new TwoStageEvaluation();
            double[] graphFitness = tsEva.detailedGraphFitness(individual, true);
            double[] puzzleFitness = tsEva.detailedPuzzleFitness(individual, puzzle, true);
            
            PuzzleDecoder decoder = new PuzzleDecoder(individual);
            Graph g = decoder.decode(puzzle, false);

            GraphStreamUtil gUtil = new GraphStreamUtil();
            g.addAttribute("ui.stylesheet", "url('media/stylesheet.css')");
            gUtil.normalizeNodesSizes(g, Config.minNodeSize, Config.maxNodeSize);
            gUtil.setupStyle(g);
            
            graph[i] = g;
            tsGA.exportGraph(graph[i]);
            exportSizePerGen(graph[i], tsGA.getStats());
            //g.display(false);
        }
        if(time.getN()==0)
            time.addValue(0.0);
    }
    
    @Override
    public void readResults(){
        File folder = new File(Config.folder);
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
        area = new DescriptiveStatistics(executions);
        
        pfitness = new DescriptiveStatistics(executions);
        din = new DescriptiveStatistics(executions);
        td = new DescriptiveStatistics(executions);
        vr = new DescriptiveStatistics(executions);
        
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        
        int minIndex = 0, minHash = 0;
        int maxIndex = 0, maxHash = 0;
        double mingArea = Double.MAX_VALUE;
        double maxgArea = Double.MIN_VALUE;
        int[] minBounds = new int[3];
        int[] maxBounds = new int[3];
        
        Hashtable<Integer, ArrayList<Integer>> sizePerGeneration = new Hashtable();
        for(int i = 0; i < Config.maxGen; i++)
            sizePerGeneration.put(i, new ArrayList<>());

        for(int i = 0; i < results.size(); i++){
            File dir = results.get(i);
            String name = dir.getPath()+File.separator+"map_"+dir.getName()+".json";
            System.out.println(name);
            EvoJSONFileReader reader = new EvoJSONFileReader(name);
            graph[i] = reader.parseJson();
            
            Decoder decoder = new Decoder(graph[i]);
            GraphIndividual individual = decoder.encode();
            
            PuzzleDecoder pDecoder = new PuzzleDecoder(individual);
            PuzzleIndividual puzzle = pDecoder.encode(graph[i]);
            //System.out.println(puzzle);
            TwoStageEvaluation tsEva = new TwoStageEvaluation();
            double[] graphFitness = tsEva.detailedGraphFitness(individual, true);
            double[] puzzleFitness = tsEva.detailedPuzzleFitness(individual, puzzle, true);
            
            size.addValue(graphFitness[0]);
            fitness.addValue(graphFitness[1]);
            asp.addValue(graphFitness[6]);
            uas.addValue(graphFitness[7]);
            
            pfitness.addValue(puzzleFitness[0]);
            din.addValue(puzzleFitness[1]);
            td.addValue(1.0/puzzleFitness[2]);
            vr.addValue(1.0/puzzleFitness[3]);
            
            double graphArea = (new GraphStreamUtil()).getGraphAreaWithBorder(graph[i]);
            area.addValue(graphArea);
            
            if(graphFitness[1] < min){
                min = graphFitness[1];
                minIndex = i;
            }
            if(graphFitness[1] > max){
                max = graphFitness[1];
                maxIndex = i;
            }
            
            if(graphArea < mingArea){
                mingArea = graphArea;
                minBounds = (new GraphStreamUtil()).getGraphBoundsWithBorder(graph[i]);
            }
            if(graphArea > maxgArea){
                maxgArea = graphArea;
                maxBounds = (new GraphStreamUtil()).getGraphBoundsWithBorder(graph[i]);
            }
            // Size per Generation
            String spgName = dir.getPath()+File.separator+"sizePerGen_"+dir.getName()+".txt";
            //System.out.println(name);
            try {
                FileReader fr = new FileReader(spgName);
                Scanner scan = new Scanner(fr);
                String genLine = scan.nextLine();
                String sizeLine = scan.nextLine();
                String[] genLineArray = genLine.substring(genLine.indexOf(": ")+2).split(" ");
                String[] sizeLineArray = sizeLine.substring(sizeLine.indexOf(": ")+2).split(" ");
                //System.out.println(Arrays.toString(genLineArray));
                //System.out.println(Arrays.toString(sizeLineArray));
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

        double minTime = time.getMin();
        double meanTime = time.getMean();
        double stdTime = time.getStandardDeviation();
        double maxTime = time.getMax();
        
        double minArea = area.getMin();
        double meanArea = area.getMean();
        double stdArea = area.getStandardDeviation();
        double maxArea = area.getMax();
        
        double minPFitness = pfitness.getMin();
        double meanPFitness = pfitness.getMean();
        double stdPFitness = pfitness.getStandardDeviation();
        double maxPFitness = pfitness.getMax();
        
        double minDIN = din.getMin();
        double meanDIN = din.getMean();
        double stdDIN = din.getStandardDeviation();
        double maxDIN = din.getMax();
        
        double minTD = td.getMin();
        double meanTD = td.getMean();
        double stdTD = td.getStandardDeviation();
        double maxTD = td.getMax();
        
        double minVR = vr.getMin();
        double meanVR = vr.getMean();
        double stdVR = vr.getStandardDeviation();
        double maxVR = vr.getMax();
        
        String tableLine =   "N & $"+String.format("%.3f",minS)+"$ & $"+String.format("%.3f",meanS)+"_{ \\pm "+String.format("%.3f",stdS)+"}$ & $"+String.format("%.3f",maxS)+"$ \\\\\n"
                           + "Area & $"+String.format("%.3f",minArea)+"$ & $"+String.format("%.3f",meanArea)+"_{ \\pm "+String.format("%.3f",stdArea)+"}$ & $"+String.format("%.3f",maxArea)+"$\\\\\n"
                           + "ASP & $"+String.format("%.3f",minASP)+"$ & $"+String.format("%.3f",meanASP)+"_{ \\pm "+String.format("%.3f",stdASP)+"}$ & $"+String.format("%.3f",maxASP)+"$ \\\\\n"
                           + "UAS & $"+String.format("%.3f",minUAS)+"$ & $"+String.format("%.3f",meanUAS)+"_{ \\pm "+String.format("%.3f",stdUAS)+"}$ & $"+String.format("%.3f",maxUAS)+"$ \\\\\n"
                           + "G.Fitness & $"+String.format("%.3f",minFitness)+"$ & $"+String.format("%.3f",meanFitness)+"_{ \\pm "+String.format("%.3f",stdFitness)+"}$ & $"+String.format("%.3f",maxFitness)+"$ \\\\\n"
                           + "\\\\\n"
                           + "DIN & $"+String.format("%.1f",minDIN)+"$ & $"+String.format("%.1f",meanDIN)+"_{ \\pm "+String.format("%.1f",stdDIN)+"}$ & $"+String.format("%.1f",maxDIN)+"$ \\\\\n"
                           + "TD & $"+String.format("%.2f",minTD)+"$ & $"+String.format("%.2f",meanTD)+"_{ \\pm "+String.format("%.2f",stdTD)+"}$ & $"+String.format("%.2f",maxTD)+"$ \\\\\n"
                           + "VR & $"+String.format("%.2f",minVR)+"$ & $"+String.format("%.2f",meanVR)+"_{ \\pm "+String.format("%.2f",stdVR)+"}$ & $"+String.format("%.2f",maxVR)+"$ \\\\\n"
                           + "P.Fitness & $"+String.format("%.3f",minPFitness)+"$ & $"+String.format("%.3f",meanPFitness)+"_{ \\pm "+String.format("%.3f",stdPFitness)+"}$ & $"+String.format("%.3f",maxPFitness)+"$ \\\\\n"
                           + "\\\\\n"
                           + "Time (s) & $"+String.format("%.3f",minTime/1000)+"$ & $"+String.format("%.3f",meanTime/1000)+"_{ \\pm "+String.format("%.3f",stdTime/1000)+"}$ & $"+String.format("%.3f",maxTime/1000)+"$\\\\\n"
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
            PrintWriter pw = new PrintWriter(Config.folder+"table.txt");
            pw.printf(tableLine);
            pw.flush();
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Results done at: "+Config.folder+"table.txt");
        
        String sizePerGen = "";
        for(int i = 0; i < Config.maxGen; i++){
            ArrayList<Integer> list = sizePerGeneration.get(i);
            sizePerGen += i+" ";
            for(int j = 0; j < list.size(); j++){
                sizePerGen += list.get(j)+" ";
            }
            sizePerGen += "\n";
        }
        try {
            PrintWriter pw = new PrintWriter(Config.folder+"sizePerGenMatrix.txt");
            pw.printf(sizePerGen);
            pw.flush();
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Experiment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
