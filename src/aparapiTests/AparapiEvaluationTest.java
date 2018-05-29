/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aparapiTests;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;

import tests.*;
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
public class AparapiEvaluationTest {
    
    public static void main(String[] args){
        System.out.println("Creating Graph");
        RandomGraphGenerator rgg = new RandomGraphGenerator();
        Graph original = rgg.barabasiAlbert("father");
        
        System.out.println("Encoding Graph as Individual");
        Decoder originalEncoder = new Decoder(original);
        GraphIndividual originalIndividual = originalEncoder.encode();
        
        System.out.println("Connecting Nodes");
        Decoder connectDecoder = new Decoder(originalIndividual);
        GraphIndividual connectedIndividual = connectDecoder.barabasiAlbertGraph();
  
        System.out.println("GPU Evaluation");
        GPUEvaluation evaluation = new GPUEvaluation();
        evaluation.gpuEvaluation(connectedIndividual, false);
        //double[] fitness = evaluation.getFitness();
        //connectedIndividual.setFitness(fitness[1]);
        
        //System.out.println("Normal Evaluation");
        //Evaluation nevaluation = new Evaluation();
        //double nfitness = nevaluation.fitness(connectedIndividual, true);
        //connectedIndividual.setFitness(nfitness);
        //System.out.println(fatherIndividual.toString());
        /*for(Edge edge : son.getEachEdge()){
            System.out.println(edge.getId()+": angle: "+evaluation.angle(edge)+" minDist: "+evaluation.minimalAngleDifference(edge));
        }*/
        /*System.out.println("Decoding Random Individual as Graph");
        Decoder randomGraphDecoder = new Decoder(connectedIndividual);
        Graph graph = randomGraphDecoder.decode();
        graph.display(false);*/
        
        /*System.out.println("Refinement");
        RefinementOperator ro = new RefinementOperator();
        Individual refinedIndividual = ro.geoRefinement(connectedIndividual, true);
        evaluation = new GPUEvaluation();
        evaluation.gpuEvaluation(refinedIndividual, true);
        fitness = evaluation.getFitness();*/
        
        /*System.out.println("Decoding Refined Individual as Graph");
        Decoder finalGraphDecoder = new Decoder(refinedIndividual);
        DefaultGraph sonRefined = finalGraphDecoder.decode();
        sonRefined.display(false);*/
        
        /*System.out.println("Edge Rect Test");
        Edge edge = sonRefined.getEdge(0);
        System.out.println(edge);
        evaluation.rectEdgeIntersection(edge, new Point3(0,0,0), new Point3(0,0,0));
        
        GraphImageBuilder gib = new GraphImageBuilder("image");
        gib.buildImage(sonRefined);*/
    }
}
