/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aparapiTests;

import evoGraph.Evaluation;
import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;
import evoGraph.Config;
import evoGraph.Decoder;
import evoGraph.GraphIndividual;
import extendedMetaZelda.DungeonGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.geom.Point3;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.nodePointPosition;

/**
 *
 * @author andre
 */
public class GPUEvaluation extends Evaluation {

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
    public GPUEvaluation() {
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
    public void gpuEvaluation(GraphIndividual individual, boolean debug) {
        Decoder barabasiDec = new Decoder(individual);
        GraphIndividual barabasiInd = barabasiDec.barabasiAlbertGraph();
        Decoder graphDec = new Decoder(barabasiInd);
        Graph graph = graphDec.decode();
        DungeonGenerator dgg = new DungeonGenerator(graph);
        dgg.generate(debug);

        gpuEvaluation(graph, debug);
    }

    // detailded fitness without decode
    public void gpuEvaluation(Graph graph, boolean debug) {

        fitness[2] = nodeAreaIntersection(graph) * Config.areaIntersectionPenalty;

        double sum
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
                    + (Config.useAverageShortestPath ? " ASP: " + fitness[6] : "")
                    + (Config.useDesiredAngles ? " UAS: " + fitness[7] : "")
                    + (Config.useIdealNonLinearity ? " DIN: " + fitness[8] : "")
                    + ")");
        }
        fitness[0] = graph.getNodeCount();
        fitness[1] = sum;
    }

    private double nodeAreaIntersection(Graph graph) {
        // Parse relevant values
        final int size = graph.getNodeCount();
        final float[] x, y, z, w, h;
        x = new float[size];
        y = new float[size];
        z = new float[size];
        w = new float[size]; // width
        h = new float[size]; // height
        for (int i = 0; i < size; i++) {
            Node node = graph.getNode(i);
            Point3 p = nodePointPosition(node);
            x[i] = (float) p.x;
            y[i] = (float) p.y;
            z[i] = (float) p.z;
            w[i] = (float) ((double) node.getAttribute("width"));
            h[i] = (float) ((double) node.getAttribute("height"));
        }

        // Output array
        final double[] intersection = new double[graph.getNodeCount()];
        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                int i = getGlobalId();
                intersection[i] = 0;

                float nodeAx1 = x[i] - (w[i] / 2f);
                float nodeAx2 = x[i] + (w[i] / 2f);
                float nodeAy1 = y[i] + (h[i] / 2f);
                float nodeAy2 = y[i] - (h[i] / 2f);

                for (int j = i + 1; j < size; j++) {
                    float nodeBx1 = x[j] - (w[j] / 2f);
                    float nodeBx2 = x[j] + (w[j] / 2f);
                    float nodeBy1 = y[j] + (h[j] / 2f);
                    float nodeBy2 = y[j] - (h[j] / 2f);

                    if (nodeAx1 < nodeBx2 && nodeAx2 > nodeBx1
                            && nodeAy1 > nodeBy2 && nodeAy2 < nodeBy1) {

                        float left = nodeAx1;
                        if (nodeBx1 > nodeAx1) {
                            left = nodeBx1;
                        }

                        float right = nodeAx2;
                        if (nodeBx2 > nodeAx2) {
                            right = nodeBx2;
                        }

                        float bottom = nodeAy2;
                        if (nodeBy2 > nodeAy2) {
                            bottom = nodeBy2;
                        }

                        float top = nodeAy1;
                        if (nodeBy1 > nodeAy1) {
                            top = nodeBy1;
                        }

                        intersection[i] += (right - left) * (top - bottom);
                    }
                }
            }
        };
        // Execute Kernel.
        kernel.execute(Range.create(size));
        // Report target execution mode: GPU or JTP (Java Thread Pool).
        System.out.println("Execution mode = " + kernel.getExecutionMode());
        // Dispose Kernel resources.
        kernel.dispose();

        double intersectionSum = 0;
        for (int i = 0; i < graph.getNodeCount(); i++) {
            intersectionSum += intersection[i];
            System.out.printf("%s %s\n", i, intersection[i]);
        }
        return intersectionSum;
    }
}
