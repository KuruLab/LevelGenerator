package evoGraph;


import graphstream.*;
import evoGraph.Config;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Hashtable;
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
public class EvoJSONFileWriter {

    private Graph graph;

    public EvoJSONFileWriter(Graph graph) {
        this.graph = graph;
    }
    
    public void exportDataJSON(String filename, String generator, int width, int height, boolean useHash){
        /*{"border":5,"random_seed":false,"method":"random seed cellular automata","seed":"666","size":"128x64","wall_threshold":25,"room_threshold":75,"type":"cave","fill_percent":50,"smooth":5}*/
         try {
            PrintWriter pw = new PrintWriter(filename);
            pw.printf("{%n"); 
            pw.printf("\"random_seed\": false,%n");
            if(useHash)
                pw.printf("\"seed\": %s,%n", graph.hashCode());
            else
                pw.printf("\"seed\": %s,%n", graph.getId());
            pw.printf("\"type\": \"evo-dungeon\",%n");
            pw.printf("\"method\": \"%s\",%n", generator);
            pw.printf("\"size\": \"%sx%s\",%n", width, height);
            pw.printf("\"border\": \"%s\",%n", Config.borderSize);
            pw.printf("\"fill_percent\": %s,%n", 0);
            pw.printf("\"wall_threshold\": %s,%n", 0);
            pw.printf("\"room_threshold\": %s,%n", 0);
            pw.printf("\"smooth\": %s%n", 0);
            pw.printf("}%n");
            pw.flush();
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EvoJSONFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void exportMapJSON(String filename, String generator, boolean useHash){
        Hashtable coordHash = parseCoordsToID(graph);
        try {
            PrintWriter pw = new PrintWriter(filename);
            pw.printf("{%n");
            if(useHash)
                pw.printf("\"graph\": \"%s\",%n", graph.hashCode());
            else
                pw.printf("\"graph\": \"%s\",%n", graph.getId());
            pw.printf("\"generator\": \"%s\",%n", generator);
            pw.printf("\"ui.antialias\": %s,%n", true);
            pw.printf("\"ui.quality\": %s,%n", true);
            //pw.printf("\"ui.stylesheet\": \"%s\",%n", "url('media/stylesheet.css')");
            pw.printf("\"nodes\": [%n");
            for(int i = 0; i < graph.getNodeCount(); i++){
                Node node = graph.getNode(i);
                pw.printf("\t{%n");
                pw.printf("\t\t\"id\": \"%s\",%n", coordHash.get(node.getId()));
                double[] xyz = nodePosition(node);
                pw.printf("\t\t\"position\": %s,%n", Arrays.toString(xyz));
                pw.printf("\t\t\"width\": %.1f,%n",  (double)node.getAttribute("width"));
                pw.printf("\t\t\"height\": %.1f,%n", (double)node.getAttribute("height"));
                pw.printf("\t\t\"degree\": %s,%n", node.getDegree());
                pw.printf("\t\t\"intensity\": %.6f,%n", (double)node.getAttribute("ui.color"));
                pw.printf("\t\t\"symbol\": \"%s\",%n", node.getAttribute("symbol").toString());
                pw.printf("\t\t\"condition\": \"%s\"%n", node.getAttribute("condition").toString());
                pw.printf("\t}");
                if(i != graph.getNodeCount()-1) pw.printf(",");
                pw.printf("%n");
            }
            pw.printf("],%n");
            pw.printf("\"edges\": [%n");
            for(int i = 0; i < graph.getEdgeCount(); i++){
                Edge edge = graph.getEdge(i);
                String coord1 = edge.getId().substring(0, edge.getId().indexOf("_"));
                String coord2 = edge.getId().substring(edge.getId().indexOf("_")+1);
                String id = coordHash.get(coord1)+"_"+coordHash.get(coord2);
                pw.printf("\t{%n");
                pw.printf("\t\t\"id\": \"%s\",%n", id);
                pw.printf("\t\t\"a\": \"%s\",%n", coordHash.get(coord1));
                pw.printf("\t\t\"b\": \"%s\",%n", coordHash.get(coord2));
                pw.printf("\t\t\"symbol\": \"%s\"%n", edge.getAttribute("symbol").toString());
                pw.printf("\t}");
                if(i != graph.getEdgeCount()-1) pw.printf(",");
                pw.printf("%n");
            }
            pw.printf("]%n");
            pw.printf("}%n");
            pw.flush();
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EvoJSONFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Hashtable<String, Integer> parseCoordsToID(Graph graph){
        Hashtable<String, Integer> hash = new Hashtable<>();
        for(int i = 0; i < graph.getNodeCount(); i++){
            Integer id = new Integer(i);
            String coord = graph.getNode(i).getId();
            if(hash.containsKey(coord)){
                System.out.println("Error: hash already contains coord "+coord);
            }
            else{
                hash.put(coord, id);
            }
        }
        return hash;
    }
}
