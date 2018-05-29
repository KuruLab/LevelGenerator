package graphstream;


import evoGraph.Config;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import static org.graphstream.algorithm.Toolkit.*;
import org.graphstream.graph.Edge;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author andre
 */
public class JSONFileWriter {

    private Graph graph;

    public JSONFileWriter(Graph graph) {
        this.graph = graph;
    }
    
    public void exportDataJSON(String filename, String generator, int width, int height){
        /*{"border":5,"random_seed":false,"method":"random seed cellular automata","seed":"666","size":"128x64","wall_threshold":25,"room_threshold":75,"type":"cave","fill_percent":50,"smooth":5}*/
         try {
            PrintWriter pw = new PrintWriter(filename);
            pw.printf("{%n"); 
            pw.printf("\"random_seed\": false,%n");
            pw.printf("\"seed\": %s,%n", graph.hashCode());
            pw.printf("\"type\": \"cave\",%n");
            pw.printf("\"method\": \"%s\",%n", generator);
            pw.printf("\"size\": \"%sx%s\",%n", width, height);
            pw.printf("\"border\": \"%s\",%n", 5);
            pw.printf("\"fill_percent\": %s,%n", 50);
            pw.printf("\"wall_threshold\": %s,%n", 25);
            pw.printf("\"room_threshold\": %s,%n", 75);
            pw.printf("\"smooth\": %s%n", 5);
            pw.printf("}%n");
            pw.flush();
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JSONFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void exportMapJSON(String filename, String generator){
        try {
            PrintWriter pw = new PrintWriter(filename);
            pw.printf("{%n");
            pw.printf("\"graph\": \"%s\",%n", graph.getId());
            pw.printf("\"generator\": \"%s\",%n", generator);
            pw.printf("\"ui.antialias\": %s,%n", true);
            pw.printf("\"ui.quality\": %s,%n", true);
            //pw.printf("\"ui.stylesheet\": \"%s\",%n", "url('media/stylesheet.css')");
            pw.printf("\"nodes\": [%n");
            for(int i = 0; i < graph.getNodeCount(); i++){
                Node node = graph.getNode(i);
                pw.printf("\t{%n");
                pw.printf("\t\t\"id\": \"%s\",%n", node.getId());
                double[] xyz = nodePosition(node);
                pw.printf("\t\t\"position\": %s,%n", Arrays.toString(xyz));
                pw.printf("\t\t\"width\": %.1f,%n",  (double)node.getAttribute("width"));
                pw.printf("\t\t\"height\": %.1f,%n", (double)node.getAttribute("height"));
                pw.printf("\t\t\"degree\": %s%n", node.getDegree());
                pw.printf("\t}");
                if(i != graph.getNodeCount()-1) pw.printf(",");
                pw.printf("%n");
            }
            pw.printf("],%n");
            pw.printf("\"edges\": [%n");
            for(int i = 0; i < graph.getEdgeCount(); i++){
                Edge edge = graph.getEdge(i);
                pw.printf("\t{%n");
                pw.printf("\t\t\"id\": \"%s\",%n", edge.getId());
                pw.printf("\t\t\"a\": \"%s\",%n", edge.getNode0());
                pw.printf("\t\t\"b\": \"%s\"%n", edge.getNode1());
                pw.printf("\t}");
                if(i != graph.getEdgeCount()-1) pw.printf(",");
                pw.printf("%n");
            }
            pw.printf("]%n");
            pw.printf("}%n");
            pw.flush();
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JSONFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
