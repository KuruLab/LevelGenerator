/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import evoLevel.LevelDecoder;
import evoLevel.LevelEvaluation;
import image.LevelImageBuilder;
import evoLevel.LevelIndividual;
import evoLevel.RandomLevelGenerator;
import evoLevel.RefinementOperator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.ui.geom.Point3;

/**
 *
 * @author andre
 */
public class EvaluationTest {
    
    public static void main(String[] args){
        System.out.println("Creating Graph");
        RandomLevelGenerator rgg = new RandomLevelGenerator();
        Graph father = rgg.barabasiAlbert("father");
        //father.display(false);
        
        System.out.println("Encoding Graph as Individual");
        LevelDecoder fatherEncoder = new LevelDecoder(father);
        LevelIndividual fatherIndividual = fatherEncoder.encode();
        
        System.out.println("Connecting Nodes");
        LevelDecoder mutantDecoder = new LevelDecoder(fatherIndividual);
        LevelIndividual connectedIndividual = mutantDecoder.barabasiAlbertGraph();
  
        System.out.println("Evaluation");
        LevelEvaluation evaluation = new LevelEvaluation();
        double fitness = evaluation.fitness(connectedIndividual, true);
        connectedIndividual.setFitness(fitness);
        //System.out.println(fatherIndividual.toString());
        /*for(Edge edge : son.getEachEdge()){
            System.out.println(edge.getId()+": angle: "+evaluation.angle(edge)+" minDist: "+evaluation.minimalAngleDifference(edge));
        }*/
        System.out.println("Decoding Random Individual as Graph");
        LevelDecoder randomGraphDecoder = new LevelDecoder(connectedIndividual);
        DefaultGraph sonRandom = randomGraphDecoder.decode();
        sonRandom.display(false);
        
        System.out.println("Refinement");
        RefinementOperator ro = new RefinementOperator();
        LevelIndividual refinedIndividual = ro.geoRefinement(connectedIndividual, true);
        fitness = evaluation.fitness(refinedIndividual, true);
        
        System.out.println("Decoding Refined Individual as Graph");
        LevelDecoder finalGraphDecoder = new LevelDecoder(refinedIndividual);
        DefaultGraph sonRefined = finalGraphDecoder.decode();
        sonRefined.display(false);
        
        System.out.println("Edge Rect Test");
        Edge edge = sonRefined.getEdge(0);
        System.out.println(edge);
        evaluation.rectEdgeIntersection(edge, new Point3(0,0,0), new Point3(0,0,0));
        
        LevelImageBuilder gib = new LevelImageBuilder("image");
        gib.buildImage(sonRefined);
    }
}
