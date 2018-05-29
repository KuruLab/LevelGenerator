package graphstream;


import evoGraph.GraphIndividual;
import evoGraph.NodeGene;
import java.util.ArrayList;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import static org.graphstream.algorithm.Toolkit.*;
import org.graphstream.ui.geom.Point3;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author andre
 */
public class MyToolkit {
    
    public static double nodeAreaOverlap(Graph graph) {
        double interSection = 0;
        for (int i = 0; i < graph.getNodeCount(); i++) {
            Node nodeA = graph.getNode(i); 
            Point3 a = nodePointPosition(nodeA);
            double nodeAx1 = a.x - ((double) nodeA.getAttribute("width")) / 2;
            double nodeAx2 = a.x + ((double) nodeA.getAttribute("width")) / 2;
            double nodeAy1 = a.y + ((double) nodeA.getAttribute("height")) / 2;
            double nodeAy2 = a.y - ((double) nodeA.getAttribute("height")) / 2;
            //System.out.printf("%s: A.X1:%.2f; A.X2:%.2f; A.Y1:%.2f; A.Y2:%.2f; \n", nodeA.getId(),nodeAx1,nodeAx2,nodeAy1,nodeAy2);
            
            for (int j = i + 1; j < graph.getNodeCount(); j++) {
                Node nodeB = graph.getNode(j);
                Point3 b = nodePointPosition(nodeB);
                double nodeBx1 = b.x - ((double) nodeB.getAttribute("width")) / 2;
                double nodeBx2 = b.x + ((double) nodeB.getAttribute("width")) / 2;
                double nodeBy1 = b.y + ((double) nodeB.getAttribute("height")) / 2;
                double nodeBy2 = b.y - ((double) nodeB.getAttribute("height")) / 2;
                //System.out.printf("%s: B.X1:%.2f; B.X2:%.2f; B.Y1:%.2f; B.Y2:%.2f; \n", nodeB.getId(),nodeBx1,nodeBx2,nodeBy1,nodeBy2);

                if (nodeAx1 < nodeBx2 && nodeAx2 > nodeBx1
                 && nodeAy1 > nodeBy2 && nodeAy2 < nodeBy1) {
                    //System.out.println(nodeA.getId() + " and " + nodeB.getId());
                    double left = Math.max(nodeAx1, nodeBx1);
                    double right = Math.min(nodeAx2, nodeBx2);
                    double bottom = Math.max(nodeAy2, nodeBy2);
                    double top =  Math.min(nodeAy1, nodeBy1);
                    
                    interSection += (right - left) * (top - bottom);
                }
            }
        } 
        return interSection;
    }

    public static void normalizeNodesSizes(Graph graph, double minimumsize, double maximumsize) {
        int smaller = -1;
        int greater = -1;
        for (Node n : graph.getEachNode()) {
            if (n.getDegree() > greater || smaller == -1) {
                greater = n.getDegree();
            }
            if (n.getDegree() < smaller || greater == -1) {
                smaller = n.getDegree();
            }
        }
        for (Node n : graph.getEachNode()) {
            double scale = (double) (n.getDegree() - smaller) / (double) (greater - smaller);
            double roundedSize = (double) Math.round((scale * maximumsize) + minimumsize);
            if (null != n.getAttribute("ui.style")) {
                n.setAttribute("ui.style", n.getAttribute("ui.style") + " size: " + roundedSize + ";");
            } else {
                n.addAttribute("ui.style", " size: " + roundedSize + ";");
            }
            n.setAttribute("width", roundedSize);
            n.setAttribute("height", roundedSize);
        }
    }
    
    public static void normalizeNodesSizes(GraphIndividual graph, double minimumsize, double maximumsize) {
        int smaller = -1;
        int greater = -1;
        for (NodeGene n : graph.getNodes()) {
            if (n.getConnectedNodes().size()> greater || smaller == -1) {
                greater = n.getConnectedNodes().size();
            }
            if (n.getConnectedNodes().size() < smaller || greater == -1) {
                smaller = n.getConnectedNodes().size();
            }
        }
        for (NodeGene n : graph.getNodes()) {
            double scale = (double) (n.getConnectedNodes().size() - smaller) / (double) (greater - smaller);
            double roundedSize = (double) Math.round((scale * maximumsize) + minimumsize);
            
            if(roundedSize % 2 != 0)
                roundedSize++;
            
            n.setWidth(roundedSize);
            n.setHeight(roundedSize);
        }
    }
    
    public static void scalePosition(Graph graph, double scalar, boolean normalize){
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minXwidth = 0;
        double minYheight = 0;
        for(int i = 0; i < graph.getNodeCount(); i++){
            Point3 p = nodePointPosition(graph.getNode(i));
            p.scale(scalar);
            //System.out.println("Point "+graph.getNode(i).getId()+": "+p.toString());
            graph.getNode(i).setAttribute("xyz", Math.round(p.x), Math.round(p.y), 0.0);
            if(normalize){
                if(p.x < minX){
                    minX = p.x;
                    minXwidth = graph.getNode(i).getAttribute("width");
                }
                if(p.y < minY){
                    minY = p.y;
                    minYheight = graph.getNode(i).getAttribute("height");
                }
            }
        }
        //System.out.println("minX, minY: "+minX+", "+minY);
        // positive values normalization
        if(normalize){
            minX -= minXwidth/2;
            minY -= minYheight/2;
            for (int i = 0; i < graph.getNodeCount(); i++) {
                Point3 p = nodePointPosition(graph.getNode(i));
                graph.getNode(i).setAttribute("xyz", Math.round(p.x + Math.abs(minX))+ AutoPositionGraph.border, Math.round(p.y + Math.abs(minY)) + AutoPositionGraph.border, 0.0);
                p = nodePointPosition(graph.getNode(i));
                //graph.getNode(i).
            }
        }
    }
    // https://www.topcoder.com/community/data-science/data-science-tutorials/geometry-concepts-line-intersection-and-its-applications/
    public static int edgeOverlap2(Graph graph) {
        ArrayList<String> intersections = new ArrayList<>();
        for (int i = 0; i < graph.getEdgeCount(); i++) {
            // first edge
            Edge edgeA = graph.getEdge(i);
            Node node1 = edgeA.getNode0();
            Node node2 = edgeA.getNode1();
            Point3 p1 = nodePointPosition(node1);
            Point3 p2 = nodePointPosition(node2);
            double A1 = p2.y - p1.y;
            double B1 = p1.x - p2.x;
            double C1 = A1 * p1.x + B1 * p1.y;
            for (int j = i + 1; j < graph.getEdgeCount(); j++) {
                if (i != j) {
                    // second edge
                    Edge edgeB = graph.getEdge(j);
                    Node node3 = edgeB.getNode0();
                    Node node4 = edgeB.getNode1();
                    if (node1.equals(node3) || node1.equals(node4)
                     || node2.equals(node3) || node2.equals(node4)) {
                        //if both edges share a node, they overlap on the node
                        continue;
                    }
                    Point3 p3 = nodePointPosition(node3);
                    Point3 p4 = nodePointPosition(node4);
                    double A2 = p4.y - p3.y;
                    double B2 = p3.x - p4.x;
                    double C2 = A2 * p3.x + B2 * p3.y;

                    double det = A1 * B2 - A2 * B1;
                    if (det == 0) {
                        //Lines are parallel
                    } else {
                        double x = (B2 * C1 - B1 * C2) / det;
                        double y = (A1 * C2 - A2 * C1) / det;
                        if (Math.min(p1.x, p2.x) < x && x < Math.max(p1.x, p2.x)
                         && Math.min(p1.y, p2.y) < y && y < Math.max(p1.y, p2.y)
                         && Math.min(p3.x, p4.x) < x && x < Math.max(p3.x, p4.x)
                         && Math.min(p3.y, p4.y) < y && y < Math.max(p3.y, p4.y)) {
                            if(!intersections.contains(edgeB.getId()+"x"+edgeA.getId()) &&
                               !intersections.contains(edgeA.getId()+"x"+edgeB.getId())){
                                intersections.add(edgeA.getId()+"x"+edgeB.getId());
                                System.err.println("Edge intersection: "+edgeA.getId()+" and "+edgeB.getId()+" at ("+x+","+y+")");
                            }
                        }
                    }
                }
            }
        }
        return 0;
    }
    
    // not working properly
    public static int edgeOverlap(Graph graph){
        ArrayList<String> intersections = new ArrayList<>();
        for (int i = 0; i < graph.getEdgeCount(); i++) {
            // first edge
            Edge edgeA = graph.getEdge(i);
            
            Node node1 = edgeA.getNode0();
            Node node2 = edgeA.getNode1();
            Point3 n1 = nodePointPosition(node1);
            Point3 n2 = nodePointPosition(node2);
            
            for (int j = i + 1; j < graph.getEdgeCount(); j++) {
                // second edge
                Edge edgeB = graph.getEdge(j);
                
                Node node3 = edgeB.getNode0();
                Node node4 = edgeB.getNode1();
                if(node1.equals(node3) || node1.equals(node4) ||
                   node2.equals(node3) || node2.equals(node4)){
                    //if both edges share a node, they overlap on the node
                    continue;
                }
                Point3 n3 = nodePointPosition(node3);
                Point3 n4 = nodePointPosition(node4);
                // in case of the exact point of intersection is needed:
                double intersectX = (((n1.x*n2.y-n1.y*n2.x)*(n3.x-n4.x)-(n1.x-n2.x)*(n3.x*n4.y-n3.y*n4.x))/
                                    ((n1.x-n2.x)*(n3.y-n4.y)-(n1.y-n2.y)*(n3.x-n4.x)));
                double intersectY = (((n1.x*n2.y-n1.y*n2.x)*(n3.y-n4.y)-(n1.y-n2.y)*(n3.x*n4.y-n3.y*n4.x))/
                                    ((n1.x-n2.x)*(n3.y-n4.y)-(n1.y-n2.y)*(n3.x-n4.x)));
                if ((n1.x - n2.x) * (n3.y - n4.y) - (n1.y - n2.y) * (n3.x - n4.x) != 0) {
                    if(!intersections.contains(edgeB.getId()+"."+edgeA.getId()) &&
                       !intersections.contains(edgeA.getId()+"."+edgeB.getId()))
                        intersections.add(edgeA.getId()+"."+edgeB.getId());
                    System.err.println("Edge intersection: "+edgeA.getId()+" and "+edgeB.getId()+" at ("+
                            intersectX+","+intersectY+")");
                }
            }
        }
        //System.out.println("Edge overlaps: "+overlaps);
        return intersections.size();
        //return 0;
    }
}
