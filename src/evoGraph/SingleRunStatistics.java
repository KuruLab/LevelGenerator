/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoGraph;

import evoGraph.Config;
import evoGraph.GraphIndividual;
import evoPuzzle.PuzzleIndividual;
import java.util.ArrayList;
import java.util.LinkedList;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author andre
 */
public class SingleRunStatistics {
    
    private LinkedList<GraphIndividual> bestGraphArchive;
    private LinkedList<PuzzleIndividual> bestPuzzleArchive;
    private LinkedList<Integer> bestGenerationArchive;
    
    private ArrayList<DescriptiveStatistics> sizeMap;
    private ArrayList<DescriptiveStatistics> fitnessMap;
    private ArrayList<DescriptiveStatistics> nipMap;
    private ArrayList<DescriptiveStatistics> eipMap;
    private ArrayList<DescriptiveStatistics> spMap;
    private ArrayList<DescriptiveStatistics> medpMap;
    private ArrayList<DescriptiveStatistics> aspMap;
    private ArrayList<DescriptiveStatistics> uasMap;
    
    private DescriptiveStatistics dinMap;
    private DescriptiveStatistics tdMap;
    private DescriptiveStatistics vrMap;
    private DescriptiveStatistics upMap;

    public SingleRunStatistics() {
        bestGraphArchive = new LinkedList<>();
        bestPuzzleArchive = new LinkedList<>();
        bestGenerationArchive = new LinkedList<>();
        
        sizeMap = new ArrayList<>(Config.maxGen);
        fitnessMap = new ArrayList<>(Config.maxGen);
        nipMap = new ArrayList<>(Config.maxGen);
        eipMap = new ArrayList<>(Config.maxGen);
        spMap = new ArrayList<>(Config.maxGen);
        medpMap = new ArrayList<>(Config.maxGen);
        aspMap = new ArrayList<>(Config.maxGen);
        uasMap = new ArrayList<>(Config.maxGen);
        
        dinMap = new DescriptiveStatistics();
        tdMap = new DescriptiveStatistics();
        vrMap = new DescriptiveStatistics();
        upMap = new DescriptiveStatistics();
    }
    
    public synchronized LinkedList<GraphIndividual> getBestGraphArchive() {
        return bestGraphArchive;
    }

    public synchronized void setBestGraphArchive(LinkedList<GraphIndividual> bestGraphArchive) {
        this.bestGraphArchive = bestGraphArchive;
    }

    public synchronized LinkedList<PuzzleIndividual> getBestPuzzleArchive() {
        return bestPuzzleArchive;
    }

    public synchronized void setBestPuzzleArchive(LinkedList<PuzzleIndividual> bestPuzzleArchive) {
        this.bestPuzzleArchive = bestPuzzleArchive;
    }

    public synchronized LinkedList<Integer> getBestGenerationArchive() {
        return bestGenerationArchive;
    }

    public synchronized void setBestGenerationArchive(LinkedList<Integer> bestGenerationArchive) {
        this.bestGenerationArchive = bestGenerationArchive;
    }

    public synchronized ArrayList<DescriptiveStatistics> getSizeMap() {
        return sizeMap;
    }

    public synchronized void setSizeMap(ArrayList<DescriptiveStatistics> sizeMap) {
        this.sizeMap = sizeMap;
    }

    public synchronized ArrayList<DescriptiveStatistics> getFitnessMap() {
        return fitnessMap;
    }

    public synchronized void setFitnessMap(ArrayList<DescriptiveStatistics> fitnessMap) {
        this.fitnessMap = fitnessMap;
    }

    public synchronized ArrayList<DescriptiveStatistics> getNipMap() {
        return nipMap;
    }

    public synchronized void setNipMap(ArrayList<DescriptiveStatistics> nipMap) {
        this.nipMap = nipMap;
    }

    public synchronized ArrayList<DescriptiveStatistics> getEipMap() {
        return eipMap;
    }

    public synchronized void setEipMap(ArrayList<DescriptiveStatistics> eipMap) {
        this.eipMap = eipMap;
    }

    public synchronized ArrayList<DescriptiveStatistics> getSpMap() {
        return spMap;
    }

    public synchronized void setSpMap(ArrayList<DescriptiveStatistics> spMap) {
        this.spMap = spMap;
    }

    public synchronized ArrayList<DescriptiveStatistics> getMedpMap() {
        return medpMap;
    }

    public synchronized void setMedpMap(ArrayList<DescriptiveStatistics> medpMap) {
        this.medpMap = medpMap;
    }

    public synchronized ArrayList<DescriptiveStatistics> getAspMap() {
        return aspMap;
    }

    public synchronized void setAspMap(ArrayList<DescriptiveStatistics> aspMap) {
        this.aspMap = aspMap;
    }

    public synchronized ArrayList<DescriptiveStatistics> getUasMap() {
        return uasMap;
    }

    public synchronized void setUasMap(ArrayList<DescriptiveStatistics> uasMap) {
        this.uasMap = uasMap;
    }

    public synchronized DescriptiveStatistics getDinMap() {
        return dinMap;
    }

    public synchronized void setDinMap(DescriptiveStatistics dinMap) {
        this.dinMap = dinMap;
    }

    public synchronized DescriptiveStatistics getTdMap() {
        return tdMap;
    }

    public synchronized void setTdMap(DescriptiveStatistics tdMap) {
        this.tdMap = tdMap;
    }

    public synchronized DescriptiveStatistics getVrMap() {
        return vrMap;
    }

    public synchronized void setVrMap(DescriptiveStatistics vrMap) {
        this.vrMap = vrMap;
    }

    public synchronized DescriptiveStatistics getUpMap() {
        return upMap;
    }

    public synchronized void setUpMap(DescriptiveStatistics upMap) {
        this.upMap = upMap;
    }
    
}
