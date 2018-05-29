/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoGraph;

import extendedMetaZelda.Condition;
import extendedMetaZelda.Symbol;
import org.graphstream.algorithm.AStar;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.edgeLength;

/**
 *
 * @author andre
 */
public class DungeonDistanceCost extends AStar.DistanceCosts{
    private int keyLevel;

    public DungeonDistanceCost(int keyLevel) {
        super();
        this.keyLevel = keyLevel;
    }
    
    @Override
    public double cost(Node node, Edge edge, Node node1) {
        Symbol edgeKey = edge.getAttribute("symbol");
        double penalty = keyLevel < edgeKey.getValue() ? ((edgeKey.getValue()-keyLevel)*1000000) : 0;
        return edgeLength(edge) + penalty;
    }
}
