package tests;

import java.awt.*;
import javax.swing.*;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.ui.swingViewer.*;
import org.graphstream.ui.view.*;

/** @see https://stackoverflow.com/a/45055683/230513 */
public class GraphSwingTest {

    public static void main(String args[]) {
        EventQueue.invokeLater(new GraphSwingTest()::display);
    }

    private void display() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel(new GridLayout()){
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(640, 480);
            }
        };
        panel.setBorder(BorderFactory.createLineBorder(Color.blue, 5));
        Graph graph = new SingleGraph("Tutorial", false, true);
        graph.addEdge("AB", "A", "B");
        Node a = graph.getNode("A");
        a.setAttribute("xy", 1, 1);
        Node b = graph.getNode("B");
        b.setAttribute("xy", -1, -1);
        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        ViewPanel viewPanel = viewer.addDefaultView(false);
        panel.add(viewPanel);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}