/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoLevel;

import config.GeneralConfig;
import java.util.Arrays;

/**
 *
 * @author andre
 */
public class RefinementOperator {

    public RefinementOperator() {

    }
    
    public LevelIndividual geoRefinement(LevelIndividual original, boolean debug) {
        original.evaluate();
        if(debug) System.out.println("Geographic Refinement ("+String.format("%.4f", original.getFitness())+")");
        LevelOperators gop = new LevelOperators();
        int attempts = 2;
        for (int i = 0; i < original.getNodes().size(); i++) {
            LevelIndividual refined = original.clone();
            LevelGene roomA = original.getNode(i);
            //if(debug) System.out.println("\nTrying to refine gene: "+i+" at "+roomA.toHashString()+" with "+roomA.getConnectedNodes().size()+" neighbours");
            //if (roomA.getConnectedNodes().size() >= 1) {
                int[] ideal = new int[3];
                for (int j = 0; j < roomA.getConnectedNodes().size(); j++) {
                    LevelGene roomB = roomA.getConnectedNodes().get(j);
                    
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
                        
                        refined.evaluate();
                        
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

    public LevelIndividual exaustiveRefinement(LevelIndividual original, boolean debug) {
        original.evaluate();
        if(debug) System.out.println("Exaustive Refinement ("+String.format("%.4f", original.getFitness())+")");
        LevelOperators gop = new LevelOperators();
        
        for (int i = 0; i < original.getNodes().size(); i++) {
            LevelIndividual refined = original.clone();
            LevelGene roomA = original.getNode(i);
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
                refined.evaluate();

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

    protected int[] calculateIdealPoint(LevelGene roomA, LevelGene roomB) {
        double minDistance = 0;
        if (false) { // not implemented yet
            minDistance = LevelConfig.minEdgeDistance;
        }
        int[] ideal = new int[3];
        int[] pA = roomA.getXYZ();
        int[] pB = roomB.getXYZ();
        // difference of coordinates to find location
        int deltaX = (int) Math.abs(pA[0] - pB[0]);
        int deltaY = (int) Math.abs(pA[1] - pB[1]);
        // north, so roomA must go north
        if ((pB[1] > pA[1]) && (deltaY >= deltaX)) {
            ideal[0] = pB[0];
            ideal[1] = (int) Math.max((int) (pB[1] - (roomB.getHeight() / 2) - (roomA.getHeight() / 2) - minDistance), GeneralConfig.borderSize);
            ideal[2] = 0;
        } // south, so roomA must go south
        else if ((pB[1] < pA[1]) && (deltaY >= deltaX)) {
            ideal[0] = pB[0];
            ideal[1] = (int) Math.max((int) (pB[1] + (roomB.getHeight() / 2) + (roomA.getHeight() / 2) + minDistance), GeneralConfig.borderSize);
            ideal[2] = 0;
        } // east
        else if ((pB[0] > pA[0]) && (deltaX >= deltaY)) {
            ideal[0] = (int) Math.max((int) (pB[0] - (roomB.getWidth() / 2) - (roomA.getWidth() / 2) - minDistance), GeneralConfig.borderSize);
            ideal[1] = pB[1];
            ideal[2] = 0;
        } // west
        else if ((pB[0] < pA[0]) && (deltaX >= deltaY)) {
            ideal[0] = (int) Math.max((int) (pB[0] + (roomB.getWidth() / 2) + (roomA.getWidth() / 2) + minDistance), GeneralConfig.borderSize);
            ideal[1] = pB[1];
            ideal[2] = 0;
        } else {
            System.err.println("Error calculating ideal point:\n"
                    + "Room A: " + Arrays.toString(pA) + "\n"
                    + "Room B: " + Arrays.toString(pB));
        }
        return ideal;
    }
}
