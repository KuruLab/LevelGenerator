/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package twoStageEvo;

import evoGraph.GraphIndividual;
import evoGraph.RefinementOperator;
import parallelEvaluation.EvaluationTask;
import twoStageEvo.TwoStageEvaluation;

/**
 *
 * @author andre
 */
public class TwoStageEvaluationTask extends EvaluationTask{
    
    public TwoStageEvaluationTask(boolean refine, GraphIndividual individual) {
        super(refine, individual);
    }
    
    @Override
    public void run() {
        TwoStageEvaluation eva = new TwoStageEvaluation();
        fitness = eva.detailedGraphFitness(individual, false);
        individual.setFitness(fitness[1]);
        if (refine) {
            RefinementOperator ro = new TwoStageRefinementOperator();
            individual = ro.geoRefinement(individual, false);
            individual = ro.exaustiveRefinement(individual, false);
        }
    }
    
}
