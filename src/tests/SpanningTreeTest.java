/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import org.graphstream.algorithm.Prim;
import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;

/**
 *
 * @author andre
 */
public class SpanningTreeTest {

    public static void main(String[] args) {
        DorogovtsevMendesGenerator gen = new DorogovtsevMendesGenerator();
        Graph graph = new DefaultGraph("Prim Test");

        String css = "edge .notintree {size:1px;fill-color:gray;} "
                + "edge .intree {size:3px;fill-color:black;}";

        graph.addAttribute("ui.stylesheet", css);
        graph.display();

        gen.addEdgeAttribute("weight");
        gen.setEdgeAttributesRange(1, 100);
        gen.addSink(graph);
        gen.begin();
        for (int i = 0; i < 25; i++) {
            gen.nextEvents();
        }
        gen.end();

        Prim prim = new Prim("ui.class", "intree", "notintree");

        prim.init(graph);
        prim.compute();
    }

}
