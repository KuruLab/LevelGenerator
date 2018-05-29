/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package twoStageEvo;

import evoGraph.GeneticOperators;
import evoGraph.GraphIndividual;
import evoGraph.NodeGene;
import evoGraph.RefinementOperator;

/**
 *
 * @author andre
 */
public class TwoStageRefinementOperator extends RefinementOperator {
    
    public void evaluate(GraphIndividual individual){
        TwoStageEvaluation eva = new TwoStageEvaluation();
        double[] graphFitness = eva.detailedGraphFitness(individual, false);
        individual.setFitness(graphFitness[1]);
    }
    
    @Override
    public GraphIndividual geoRefinement(GraphIndividual original, boolean debug) {
        evaluate(original);
        if(debug) 
            System.out.println("Geographic Refinement ("+String.format("%.4f", original.getFitness())+")");
        GeneticOperators gop = new GeneticOperators();
        int attempts = 2;
        for (int i = 0; i < original.getNodes().size(); i++) {
            GraphIndividual refined = original.clone();
            NodeGene roomA = original.getNode(i);
            //if(debug) System.out.println("\nTrying to refine gene: "+i+" at "+roomA.toHashString()+" with "+roomA.getConnectedNodes().size()+" neighbours");
            //if (roomA.getConnectedNodes().size() >= 1) {
                int[] ideal = new int[3];
                for (int j = 0; j < roomA.getConnectedNodes().size(); j++) {
                    NodeGene roomB = roomA.getConnectedNodes().get(j);
                    
                    for (int a = 0; a < attempts; a++) {
                        if (a == 0) {
                            ideal = calculateIdealPoint(roomA, roomB);
                        } else {
                            ideal[0] = (int) ((roomA.getXYZ()[0] + roomB.getXYZ()[0]) / 2.0);
                            ideal[1] = (int) ((roomA.getXYZ()[1] + roomB.getXYZ()[1]) / 2.0);
                            ideal[2] = 0;
                        }
                        //if(debug) System.out.println("-> Trying ideal position with neighbour: "+j+" at "+roomB.toHashString()+" -> "+Arrays.toString(ideal));
                        refined.getNode(i).setXYZ(ideal);
                        if (gop.isInvalid(refined)) {
                            refined = gop.fixInvalidIndividual(refined);
                        }
                        
                        evaluate(refined);
                        
                        if (refined.getFitness() < original.getFitness()) {
                            if (a == 0) {
                                if(debug) System.err.println("-> Ideal Improvement at "+i+" and "+j+": "+String.format("%.4f", refined.getFitness())+" < "+String.format("%.4f", original.getFitness()));
                                original = refined.clone();
                                a = attempts;
                            } else {
                                if(debug) System.err.println("-> Gradual Improvement at "+i+" and "+j+": "+String.format("%.4f", refined.getFitness())+" < "+String.format("%.4f", original.getFitness()));
                                original = refined.clone();
                                roomA.setXYZ(ideal);
                                a = 0;
                            }
                        }
                        else{
                            //if(debug) System.out.println("-> Refinement fail at node "+i+": "+String.format("%.4f", refined.getFitness())+" > "+String.format("%.4f", original.getFitness()));
                        }
                    }
                }
            //}
        }
        return original;
    }

    @Override
    public GraphIndividual exaustiveRefinement(GraphIndividual original, boolean debug) {
        evaluate(original);
        if(debug)
            System.out.println("Exaustive Refinement ("+String.format("%.4f", original.getFitness())+")");
        GeneticOperators gop = new GeneticOperators();
        
        for (int i = 0; i < original.getNodes().size(); i++) {
            GraphIndividual refined = original.clone();
            NodeGene roomA = original.getNode(i);
            int[] ideal = new int[3];
            //if(debug) System.out.println("\nTrying to refine node: "+i+" at "+roomA.toHashString());
            
            for(int a = 1; a < 5; a++){
                if (a == 1){ // try north
                    ideal[0] = (int) (roomA.getXYZ()[0]);
                    ideal[1] = (int) (roomA.getXYZ()[1] + 1);
                    ideal[2] = 0;
                    //if(debug) System.out.println("-> Trying north position -> "+Arrays.toString(ideal));
                } else if (a == 2){ // try south
                    ideal[0] = (int) (roomA.getXYZ()[0]);
                    ideal[1] = (int) (roomA.getXYZ()[1] - 1);
                    ideal[2] = 0;
                    //if(debug) System.out.println("-> Trying south position -> "+Arrays.toString(ideal));
                } else if (a == 3){ // try east
                    ideal[0] = (int) (roomA.getXYZ()[0] + 1);
                    ideal[1] = (int) (roomA.getXYZ()[1]);
                    ideal[2] = 0;
                    //if(debug) System.out.println("-> Trying east position -> "+Arrays.toString(ideal));
                } else if (a == 4){ // try west
                    ideal[0] = (int) (roomA.getXYZ()[0] - 1);
                    ideal[1] = (int) (roomA.getXYZ()[1]);
                    ideal[2] = 0;
                    //if(debug) System.out.println("-> Trying west position -> "+Arrays.toString(ideal));
                }
                refined.getNode(i).setXYZ(ideal);
                if (gop.isInvalid(refined)) {
                    refined = gop.fixInvalidIndividual(refined);
                }
                evaluate(refined);

                if (refined.getFitness() < original.getFitness()) {
                    if(debug) System.err.println("-> Improvement at node "+i+": "+String.format("%.4f", refined.getFitness())+" < "+String.format("%.4f", original.getFitness()));
                    roomA.setXYZ(ideal);
                    original = refined.clone();
                }
                else{
                    //if(debug) System.out.println("-> Refinement fail at node "+i+": "+String.format("%.4f", refined.getFitness())+" > "+String.format("%.4f", original.getFitness()));
                }
            }
        }
        return original;
    }
    
}
