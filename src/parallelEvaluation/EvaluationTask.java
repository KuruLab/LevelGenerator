/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parallelEvaluation;

import evoGraph.Config;
import evoGraph.Evaluation;
import evoGraph.GraphIndividual;
import evoGraph.RefinementOperator;
import java.util.Random;

/**
 *
 * @author andre
 */
public class EvaluationTask implements Runnable {

    protected boolean refine;
    protected double[] fitness;
    protected GraphIndividual individual;

    public EvaluationTask(boolean refine, GraphIndividual individual) {
        this.refine = refine;
        this.individual = individual;
    }

    @Override
    public void run() {
        Evaluation eva = new Evaluation();
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

    public GraphIndividual getIndividual() {
        return individual;
    }

    public void setIndividual(GraphIndividual individual) {
        this.individual = individual;
    }

}
