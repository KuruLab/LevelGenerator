/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import evoLevel.LevelConfig;
import evoLevel.LevelDecoder;
import evoLevel.LevelEvaluation;
import evoLevel.LevelIndividual;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.geom.Point3;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.nodePointPosition;

/**
 *
 * @author andre
 */
public class EfficientEvaluation extends LevelEvaluation {

    private double[] fitness;

    //fitness[0] = graph.getNodeCount();
    //fitness[1] = sum;
    //fitness[2] = areaIntersection;
    //fitness[3] = edgeIntersection;
    //fitness[4] = sizePenalty;
    //fitness[5] = minEdgeDist;
    //fitness[6] = avgSP;
    //fitness[7] = undesiredAngleSum;
    //fitness[8] = nonLinearity;
    public EfficientEvaluation() {
        super();
        fitness = new double[9];
        for (int i = 0; i < fitness.length; i++) {
            fitness[i] = 0;
        }
    }

    public double[] getFitness() {
        return fitness;
    }

    public void setFitness(double[] fitness) {
        this.fitness = fitness;
    }

    // if you receive an individual, we first convert it to a dungeon (graph)
    public void efficientEvaluation(LevelIndividual individual, boolean debug) {
        LevelDecoder barabasiDec = new LevelDecoder(individual);
        LevelIndividual barabasiInd = barabasiDec.barabasiAlbertGraph();
        LevelDecoder graphDec = new LevelDecoder(barabasiInd);
        Graph graph = graphDec.decode();
        //DungeonGenerator dgg = new DungeonGenerator(graph);
        //dgg.generate(debug);

        efficientEvaluation(graph, debug);
    }

    // detailded fitness without decode
    public void efficientEvaluation(Graph graph, boolean debug) {

        // fitness[2] -> areaIntersection;
        double intersectionSum = 0;
        // First Node Loop
        for (int i = 0; i < graph.getNodeCount(); i++) {
            Node nodeA = graph.getNode(i);
            Point3 a = nodePointPosition(nodeA);
            double nodeAx1 = a.x - ((double) nodeA.getAttribute("width")) / 2f;
            double nodeAx2 = a.x + ((double) nodeA.getAttribute("width")) / 2f;
            double nodeAy1 = a.y + ((double) nodeA.getAttribute("height")) / 2f;
            double nodeAy2 = a.y - ((double) nodeA.getAttribute("height")) / 2f;
            //System.out.printf("%s: A.X1:%.2f; A.X2:%.2f; A.Y1:%.2f; A.Y2:%.2f; \n", nodeA.getId(),nodeAx1,nodeAx2,nodeAy1,nodeAy2);
            
            // Second Node Loop
            for (int j = i + 1; j < graph.getNodeCount(); j++) {
                Node nodeB = graph.getNode(j);
                Point3 b = nodePointPosition(nodeB);
                double nodeBx1 = b.x - ((double) nodeB.getAttribute("width")) / 2f;
                double nodeBx2 = b.x + ((double) nodeB.getAttribute("width")) / 2f;
                double nodeBy1 = b.y + ((double) nodeB.getAttribute("height")) / 2f;
                double nodeBy2 = b.y - ((double) nodeB.getAttribute("height")) / 2f;
                //System.out.printf("%s: B.X1:%.2f; B.X2:%.2f; B.Y1:%.2f; B.Y2:%.2f; \n", nodeB.getId(),nodeBx1,nodeBx2,nodeBy1,nodeBy2);

                if (nodeAx1 < nodeBx2 && nodeAx2 > nodeBx1
                        && nodeAy1 > nodeBy2 && nodeAy2 < nodeBy1) {
                    //System.out.println(nodeA.getId() + " and " + nodeB.getId());
                    double left = Math.max(nodeAx1, nodeBx1);
                    double right = Math.min(nodeAx2, nodeBx2);
                    double bottom = Math.max(nodeAy2, nodeBy2);
                    double top = Math.min(nodeAy1, nodeBy1);

                    intersectionSum += (right - left) * (top - bottom);
                }
            }
        }
        fitness[0] = graph.getNodeCount();
        fitness[2] = intersectionSum * LevelConfig.areaIntersectionPenalty;
        fitness[4] = nodeCountPenalty(graph);
        fitness[1]
                = // Penalty
                fitness[2] + fitness[3] + fitness[4] + fitness[5]
                + // Preference
                fitness[6] + fitness[7] + fitness[8];

        if (debug) {
            System.out.println("Size: " + graph.getNodeCount() + ", Fitness: " + fitness[1]
                    + " (AIP: " + fitness[2]
                    + " EIP: " + fitness[3]
                    + " SP: " + fitness[4]
                    + " MEP: " + fitness[5]
                    + (LevelConfig.useAverageShortestPath ? " ASP: " + fitness[6] : "")
                    + (LevelConfig.useDesiredAngles ? " UAS: " + fitness[7] : "")
                    //+ (LevelConfig.useIdealNonLinearity ? " DIN: " + fitness[8] : "")
                    + ")");
        }       
    }
}
