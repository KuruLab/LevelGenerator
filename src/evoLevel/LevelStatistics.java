/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoLevel;

import evoLevel.LevelConfig;
import evoLevel.LevelIndividual;
import java.util.ArrayList;
import java.util.LinkedList;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author andre
 */
public class LevelStatistics {
    
    private LinkedList<LevelIndividual> bestGraphArchive;
    private LinkedList<Integer> bestGenerationArchive;
    
    private ArrayList<DescriptiveStatistics> sizeMap;
    private ArrayList<DescriptiveStatistics> fitnessMap;
    private ArrayList<DescriptiveStatistics> nipMap;
    private ArrayList<DescriptiveStatistics> eipMap;
    private ArrayList<DescriptiveStatistics> spMap;
    private ArrayList<DescriptiveStatistics> medpMap;
    private ArrayList<DescriptiveStatistics> aspMap;
    private ArrayList<DescriptiveStatistics> uasMap;

    public LevelStatistics() {
        bestGraphArchive = new LinkedList<>();
        bestGenerationArchive = new LinkedList<>();
        
        sizeMap = new ArrayList<>(LevelConfig.maxGen);
        fitnessMap = new ArrayList<>(LevelConfig.maxGen);
        nipMap = new ArrayList<>(LevelConfig.maxGen);
        eipMap = new ArrayList<>(LevelConfig.maxGen);
        spMap = new ArrayList<>(LevelConfig.maxGen);
        medpMap = new ArrayList<>(LevelConfig.maxGen);
        aspMap = new ArrayList<>(LevelConfig.maxGen);
        uasMap = new ArrayList<>(LevelConfig.maxGen);
    }
    
    public synchronized LinkedList<LevelIndividual> getBestGraphArchive() {
        return bestGraphArchive;
    }

    public synchronized void setBestGraphArchive(LinkedList<LevelIndividual> bestGraphArchive) {
        this.bestGraphArchive = bestGraphArchive;
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
}
