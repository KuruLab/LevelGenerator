/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import evoGraph.Decoder;
import evoGraph.Evaluation;
import evoGraph.GraphImageBuilder;
import evoGraph.GraphIndividual;
import evoGraph.RandomGraphGenerator;
import evoGraph.RefinementOperator;
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
        RandomGraphGenerator rgg = new RandomGraphGenerator();
        Graph father = rgg.barabasiAlbert("father");
        //father.display(false);
        
        System.out.println("Encoding Graph as Individual");
        Decoder fatherEncoder = new Decoder(father);
        GraphIndividual fatherIndividual = fatherEncoder.encode();
        
        System.out.println("Connecting Nodes");
        Decoder mutantDecoder = new Decoder(fatherIndividual);
        GraphIndividual connectedIndividual = mutantDecoder.barabasiAlbertGraph();
  
        System.out.println("Evaluation");
        Evaluation evaluation = new Evaluation();
        double fitness = evaluation.fitness(connectedIndividual, true);
        connectedIndividual.setFitness(fitness);
        //System.out.println(fatherIndividual.toString());
        /*for(Edge edge : son.getEachEdge()){
            System.out.println(edge.getId()+": angle: "+evaluation.angle(edge)+" minDist: "+evaluation.minimalAngleDifference(edge));
        }*/
        System.out.println("Decoding Random Individual as Graph");
        Decoder randomGraphDecoder = new Decoder(connectedIndividual);
        DefaultGraph sonRandom = randomGraphDecoder.decode();
        sonRandom.display(false);
        
        System.out.println("Refinement");
        RefinementOperator ro = new RefinementOperator();
        GraphIndividual refinedIndividual = ro.geoRefinement(connectedIndividual, true);
        fitness = evaluation.fitness(refinedIndividual, true);
        
        System.out.println("Decoding Refined Individual as Graph");
        Decoder finalGraphDecoder = new Decoder(refinedIndividual);
        DefaultGraph sonRefined = finalGraphDecoder.decode();
        sonRefined.display(false);
        
        System.out.println("Edge Rect Test");
        Edge edge = sonRefined.getEdge(0);
        System.out.println(edge);
        evaluation.rectEdgeIntersection(edge, new Point3(0,0,0), new Point3(0,0,0));
        
        GraphImageBuilder gib = new GraphImageBuilder("image");
        gib.buildImage(sonRefined);
    }
}
