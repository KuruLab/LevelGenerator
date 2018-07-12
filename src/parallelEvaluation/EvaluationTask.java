/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parallelEvaluation;

import evoLevel.LevelConfig;
import evoLevel.LevelEvaluation;
import evoLevel.LevelIndividual;
import evoLevel.RefinementOperator;
import java.util.Random;

/**
 *
 * @author andre
 */
public class EvaluationTask implements Runnable {

    protected boolean refine;
    protected double[] fitness;
    protected LevelIndividual individual;

    public EvaluationTask(boolean refine, LevelIndividual individual) {
        this.refine = refine;
        this.individual = individual;
    }

    @Override
    public void run() {
        LevelEvaluation eva = new LevelEvaluation();
        fitness = eva.detailedFitness(individual, false);
        individual.setFitness(fitness[1]);
        if (refine) {
            RefinementOperator ro = new RefinementOperator();
            individual = ro.geoRefinement(individual, false);
            individual = ro.exaustiveRefinement(individual, false);
        }
    }

    public double[] getFitness() {
        return fitness;
    }

    public void setFitness(double[] fitness) {
        this.fitness = fitness;
    }

    public LevelIndividual getIndividual() {
        return individual;
    }

    public void setIndividual(LevelIndividual individual) {
        this.individual = individual;
    }

}
