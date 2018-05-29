/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoPuzzle;

import extendedMetaZelda.Condition;
import org.graphstream.algorithm.AStar;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.edgeLength;

/**
 *
 * @author andre
 */
public class PuzzleDistanceCost extends AStar.DistanceCosts{
    private int keyLevel;

    public PuzzleDistanceCost(int keyLevel) {
        super();
        this.keyLevel = keyLevel;
    }
    
    @Override
    public double cost(Node node, Edge edge, Node node1) {
        Condition nextKey = node1.getAttribute("condition");
        double penalty = keyLevel < nextKey.getKeyLevel() ? ((nextKey.getKeyLevel()-keyLevel)*1000000) : 0;
        return edgeLength(edge) + penalty;
    }
}
