/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package twoStageEvo;

import evoGraph.Config;
import evoGraph.Decoder;
import evoGraph.Evaluation;
import evoGraph.GraphIndividual;
import evoPuzzle.PuzzleEvaluation;
import evoPuzzle.PuzzleIndividual;
import org.graphstream.graph.Graph;

/**
 *
 * @author andre
 */
public class TwoStageEvaluation extends Evaluation{
    
    public double[] detailedPuzzleFitness(GraphIndividual original, PuzzleIndividual puzzle, boolean debug){
        PuzzleEvaluation pEva = new PuzzleEvaluation();
        return pEva.fitness(original, puzzle, debug);
    }
    
    public double[] detailedGraphFitness(GraphIndividual individual, boolean debug){ 
        return detailedFitness(individual, debug);
    }
    
    @Override
    public double[] detailedFitness(GraphIndividual individual, boolean debug){ 
        Decoder barabasiDec = new Decoder(individual);
        GraphIndividual barabasiInd = barabasiDec.barabasiAlbertGraph();
        Decoder graphDec = new Decoder(barabasiInd);
        Graph graph = graphDec.decode();
        
        return detailedFitness(graph, debug);
    }
    
    public double[] detailedGraphFitness(Graph graph, boolean debug){ 
        return detailedFitness(graph, debug);
    }
    
    // detailded fitness without decode
    @Override
    public double[] detailedFitness(Graph graph, boolean debug){ 
        double[] detailedFitness = new double[8];
        double areaIntersection = nodeAreaOverlapPenalty(graph);
        double edgeIntersection = edgeIntersectionPenalty(graph);
        double minEdgeDist = minEdgeDistancePenalty(graph);
        double sizePenalty = nodeCountPenalty(graph);
        double avgSP = Config.useAverageShortestPath? efficientAStarAVGShortestPath(graph) : 0;
        double undesiredAngleSum = Config.useDesiredAngles? undesiredAngleSum(graph) : 0;
        
        double fitness =
            // Penalty
            areaIntersection + edgeIntersection + minEdgeDist + sizePenalty +
            // Preference
            avgSP + undesiredAngleSum;
        
        if (debug) {
            System.out.println("Size: " + graph.getNodeCount() + ", Fitness: " + fitness + 
                    "(AIP: " + areaIntersection + 
                    " EIP: " + edgeIntersection + 
                    " SP: "  + sizePenalty + 
                    " MEP: " + minEdgeDist +
                    
                    (Config.useAverageShortestPath? " ASP: " + avgSP : "" )+ 
                    (Config.useDesiredAngles?       " UAS: " + undesiredAngleSum : "") +
                    ")");
        }
        detailedFitness[0] = graph.getNodeCount();
        detailedFitness[1] = fitness;
        detailedFitness[2] = areaIntersection;
        detailedFitness[3] = edgeIntersection;
        detailedFitness[4] = sizePenalty;
        detailedFitness[5] = minEdgeDist;
        detailedFitness[6] = avgSP;
        detailedFitness[7] = undesiredAngleSum;
        return detailedFitness;
    }
}
