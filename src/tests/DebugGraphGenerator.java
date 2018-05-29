/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import evoGraph.Config;
import evoGraph.EvoJSONFileWriter;
import evoGraph.GraphImageBuilder;
import graphstream.MyToolkit;
import java.io.File;
import org.graphstream.algorithm.generator.BarabasiAlbertGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.geom.Point3;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.nodePointPosition;
import org.graphstream.ui.view.Viewer;

/**
 *
 * @author andre
 */
public class DebugGraphGenerator {
    
    public static String graphName = "1";
    
    public static void main(String args[]) {
        System.setProperty("org.graphstream.ui.renderer",
                "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        
        DefaultGraph graph = new DefaultGraph(graphName);
        graph.addAttribute("ui.stylesheet", "url('media/stylesheet.css')");
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");
        graph.addNode("E");
        graph.addNode("F");
        graph.addNode("G");
        graph.addNode("H");
        graph.addNode("I");
        graph.addNode("J");
        graph.addNode("K");
        
        graph.addNode("L");
        graph.addNode("M");
        graph.addNode("N");
        graph.addNode("O");
        graph.addNode("P");
        graph.addNode("Q");
        graph.addNode("R");
        graph.addNode("S");
        graph.addNode("T");
        graph.addNode("U");
        
        graph.addEdge("A_B", "A", "B");
        graph.addEdge("A_C", "A", "C");
        graph.addEdge("A_D", "A", "D");
        graph.addEdge("A_E", "A", "E");
        graph.addEdge("A_F", "A", "F");
        graph.addEdge("A_G", "A", "G");
        graph.addEdge("A_H", "A", "H");
        graph.addEdge("A_I", "A", "I");
        graph.addEdge("A_J", "A", "J");
        graph.addEdge("A_K", "A", "K");
        
        graph.addEdge("A_L", "A", "L");
        graph.addEdge("A_M", "A", "M");
        graph.addEdge("A_N", "A", "N");
        graph.addEdge("A_O", "A", "O");
        graph.addEdge("A_P", "A", "P");
        graph.addEdge("A_Q", "A", "Q");
        graph.addEdge("A_R", "A", "R");
        graph.addEdge("A_S", "A", "S");
        graph.addEdge("A_T", "A", "T");
        graph.addEdge("A_U", "A", "U");
 
        graph.getNode("A").setAttribute("xyz", 0.0, 10.0, 0.0);
        graph.getNode("B").setAttribute("xyz", 10.0, 0.0, 0.0);
        graph.getNode("C").setAttribute("xyz", 10.0, 1.0, 0.0);
        graph.getNode("D").setAttribute("xyz", 10.0, 2.0, 0.0);
        graph.getNode("E").setAttribute("xyz", 10.0, 3.0, 0.0);
        graph.getNode("F").setAttribute("xyz", 10.0, 4.0, 0.0);
        graph.getNode("G").setAttribute("xyz", 10.0, 5.0, 0.0);
        graph.getNode("H").setAttribute("xyz", 10.0, 6.0, 0.0);
        graph.getNode("I").setAttribute("xyz", 10.0, 7.0, 0.0);
        graph.getNode("J").setAttribute("xyz", 10.0, 8.0, 0.0);
        graph.getNode("K").setAttribute("xyz", 10.0, 9.0, 0.0);
        
        graph.getNode("L").setAttribute("xyz", 10.0, 10.0, 0.0);
        graph.getNode("M").setAttribute("xyz", 10.0, 11.0, 0.0);
        graph.getNode("N").setAttribute("xyz", 10.0, 12.0, 0.0);
        graph.getNode("O").setAttribute("xyz", 10.0, 13.0, 0.0);
        graph.getNode("P").setAttribute("xyz", 10.0, 14.0, 0.0);
        graph.getNode("Q").setAttribute("xyz", 10.0, 15.0, 0.0);
        graph.getNode("R").setAttribute("xyz", 10.0, 16.0, 0.0);
        graph.getNode("S").setAttribute("xyz", 10.0, 17.0, 0.0);
        graph.getNode("T").setAttribute("xyz", 10.0, 18.0, 0.0);
        graph.getNode("U").setAttribute("xyz", 10.0, 19.0, 0.0);
        
        MyToolkit.normalizeNodesSizes(graph, Config.minNodeSize, Config.maxNodeSize);
        setupStyle(graph);
        MyToolkit.scalePosition(graph, Config.scaleFactor, true);
        
        
        Viewer v = graph.display(false);
        v.disableAutoLayout();
        exportGraph(graph);
        /*for (Node n : graph) {
            System.out.println(n.getId());
        }
        for (Edge e : graph.getEachEdge()) {
            System.out.println(e.getId());
        }*/

        //You can also obtain a read-only set of nodes from the graph (this is not a copy,
        //but a view on the set of nodes, hence the operation is reasonably fast):
        /*Collection<Node> nodes = graph.getNodeSet();*/

        /*for (int i = 0; i < graph.getNodeCount(); i++) {
            Node node = graph.getNode(i);
        }*/

        //Access by index is generally faster than access by identifier.
        //It can be useful to interface GraphStream with APIs that use arrays.
        //The following code constructs the adjacency matrix of a graph:
        /*int n = graph.getNodeCount();
        byte adjacencyMatrix[][] = new byte[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                adjacencyMatrix[i][j] = (byte) (graph.getNode(i).hasEdgeBetween(j) ? 1 : 0);
            }
        }*/
    }
    
    public static void exportGraph(Graph g){
        File dir = new File(Config.folder, graphName);
        if(!dir.exists()){
            dir.mkdir();
        }
        int maxX = 0;
        int maxY = 0;
        for(Node node : g.getEachNode()){
            Point3 p = nodePointPosition(node);
            //System.out.println(node.getId()+": "+p.toString());
            maxX = (int) Math.max(maxX, p.x + ((double) node.getAttribute("width")/2.0f));
            maxY = (int) Math.max(maxY, p.y + ((double) node.getAttribute("height")/2.0f));
        }
        EvoJSONFileWriter fw = new EvoJSONFileWriter(g);
        System.out.println("Exporting Debug Map\n"+
                dir+"\\data_"+graphName+".json\n"+
                dir+"\\map_"+graphName+".json");
        fw.exportDataJSON(dir+"\\data_"+graphName+".json",
                "GraphStream", maxX, maxY, false);
        fw.exportMapJSON(dir+"\\map_"+graphName+".json",
                "Evolutionary"+BarabasiAlbertGenerator.class.getSimpleName(), false);
        
        // export PNG
        GraphImageBuilder gib = new GraphImageBuilder(dir+"\\img_"+graphName+".png");
        gib.buildImage(g);
    }
    
    public static void setupStyle(DefaultGraph graph) {
        for (Node node : graph) {
            node.addAttribute("ui.label", node.getId());
        }
    }
    
}
