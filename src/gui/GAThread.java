/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import evoGraph.SingleRunStatistics;
import evoGraph.Config;
import evoGraph.Decoder;
import evoGraph.Evaluation;
import evoGraph.GeneticAlgorithm;
import evoGraph.GraphIndividual;
import extendedMetaZelda.DungeonGenerator;
import extendedMetaZelda.DungeonUtil;
import graphstream.GraphStreamUtil;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

/**
 *
 * @author andre
 */
public class GAThread implements Runnable {

    private JProgressBar progressbar;
    private JButton runButton;
    private JPanel panel;
    private JComboBox nodeBox, edgeBox;
    private JTextField fitness, size, nip, eip, sp, medp, asp, uas, din, gen;
    
    private JTextField[][] statistics;
    
    private Graph graph;
    private GeneticAlgorithm ga;

    private Hashtable<String, Integer> id2IndexMap; 
    private Hashtable<Integer, String> index2IdMap;
    
    protected boolean stop;
    
    public GAThread(Graph g,
            JButton run, JProgressBar prog, JPanel panel,
            JComboBox nodeb, JComboBox edgeb,
            JTextField size, JTextField fit, 
            JTextField nip, JTextField eip, JTextField sp, JTextField medp,
            JTextField asp, JTextField uas, JTextField gen, JTextField din,
            JTextField[][] popStatistics) {
        this.graph = g;
        this.progressbar = prog;
        this.runButton = run;
        this.panel = panel;
        this.nodeBox = nodeb;
        this.edgeBox = edgeb;
        this.size = size;
        this.fitness = fit;
        this.nip = nip;
        this.eip = eip;
        this.sp = sp;
        this.medp = medp;
        this.asp = asp;
        this.uas = uas;
        this.gen = gen;
        this.din = din;
        
        this.statistics = popStatistics;
        
        this.stop = false;
        this.ga = new GeneticAlgorithm();
    }

    public void viewBar() {
        progressbar.setStringPainted(true);
        progressbar.setValue(0);
        
        int timerDelay = 10;
        new javax.swing.Timer(timerDelay, new ActionListener() {
            int previousGen = 0;
            public void actionPerformed(ActionEvent e) {
                int progress = Math.round(((float) ga.getCurrentGeneration() / (float) Config.maxGen) * 100f);
                progressbar.setValue(progress);
                
                if(previousGen < ga.getCurrentGeneration() && previousGen > 0){
                    SingleRunStatistics stat = ga.getStats();
                    statistics[0][0].setText(String.format("%s", (int) stat.getSizeMap().get(previousGen).getMin()));
                    statistics[0][1].setText(String.format("%.2f", stat.getSizeMap().get(previousGen).getMean()));
                    statistics[0][2].setText(String.format("%s", (int) stat.getSizeMap().get(previousGen).getPercentile(50)));
                    
                    statistics[1][0].setText(String.format("%.2f", stat.getFitnessMap().get(previousGen).getMin()));
                    statistics[1][1].setText(String.format("%.2f", stat.getFitnessMap().get(previousGen).getMean()));
                    statistics[1][2].setText(String.format("%.2f", stat.getFitnessMap().get(previousGen).getPercentile(50)));
                    
                    statistics[2][0].setText(String.format("%.2f", stat.getNipMap().get(previousGen).getMin()));
                    statistics[2][1].setText(String.format("%.2f", stat.getNipMap().get(previousGen).getMean()));
                    statistics[2][2].setText(String.format("%.2f", stat.getNipMap().get(previousGen).getPercentile(50)));
                    
                    statistics[3][0].setText(String.format("%.2f", stat.getEipMap().get(previousGen).getMin()));
                    statistics[3][1].setText(String.format("%.2f", stat.getEipMap().get(previousGen).getMean()));
                    statistics[3][2].setText(String.format("%.2f", stat.getEipMap().get(previousGen).getPercentile(50)));
                    
                    statistics[4][0].setText(String.format("%.2f", stat.getSpMap().get(previousGen).getMin()));
                    statistics[4][1].setText(String.format("%.2f", stat.getSpMap().get(previousGen).getMean()));
                    statistics[4][2].setText(String.format("%.2f", stat.getSpMap().get(previousGen).getPercentile(50)));
                    
                    statistics[5][0].setText(String.format("%.2f", stat.getMedpMap().get(previousGen).getMin()));
                    statistics[5][1].setText(String.format("%.2f", stat.getMedpMap().get(previousGen).getMean()));
                    statistics[5][2].setText(String.format("%.2f", stat.getMedpMap().get(previousGen).getPercentile(50)));
                    
                    statistics[6][0].setText(String.format("%.2f", stat.getAspMap().get(previousGen).getMin()));
                    statistics[6][1].setText(String.format("%.2f", stat.getAspMap().get(previousGen).getMean()));
                    statistics[6][2].setText(String.format("%.2f", stat.getAspMap().get(previousGen).getPercentile(50)));
                    
                    statistics[7][0].setText(String.format("%.2f", stat.getUasMap().get(previousGen).getMin()));
                    statistics[7][1].setText(String.format("%.2f", stat.getUasMap().get(previousGen).getMean()));
                    statistics[7][2].setText(String.format("%.2f", stat.getUasMap().get(previousGen).getPercentile(50)));
                    
                    /*statistics[8][0].setText(String.format("%.2f", stat.getDinMap().get(previousGen).getMin()));
                    statistics[8][1].setText(String.format("%.2f", stat.getDinMap().get(previousGen).getMean()));
                    statistics[8][2].setText(String.format("%.2f", stat.getDinMap().get(previousGen).getPercentile(50)));*/
                }
                if (progress >= 100 || stop) {
                    runButton.setEnabled(true);
                    ((javax.swing.Timer) e.getSource()).stop(); // stop the timer
                }
                previousGen = ga.getCurrentGeneration();
            }
        }).start();

        progressbar.setValue(progressbar.getMinimum());
    }
    
    public void viewPanel() {
        int timerDelay = 10;
        new javax.swing.Timer(timerDelay, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(ga.checkImprovement()){
                    GraphIndividual individual = ga.getBestIndividual();

                    Decoder decoder = new Decoder(individual);
                    GraphIndividual connectedIndividual = decoder.barabasiAlbertGraph();
                    decoder = new Decoder(connectedIndividual);
                    graph = decoder.decode();
                    DungeonGenerator dgg = new DungeonGenerator(graph);
                    dgg.generate();
                    
                    Evaluation eva = new Evaluation();
                    double[] detailedFitness = eva.detailedFitness(graph, true);
                    size.setText(String.format("%s", (int)detailedFitness[0]));
                    fitness.setText(String.format("%.4f", detailedFitness[1]));
                    nip.setText(String.format("%.4f", detailedFitness[2]));
                    eip.setText(String.format("%.4f", detailedFitness[3]));
                    sp.setText(String.format("%.4f", detailedFitness[4]));
                    medp.setText(String.format("%.4f", detailedFitness[5]));
                    asp.setText(String.format("%.4f", detailedFitness[6]));
                    uas.setText(String.format("%.4f", detailedFitness[7]));
                    din.setText(String.format("%s", detailedFitness[8]));
                    gen.setText(String.format("%s", ga.getCurrentGeneration())); 
                    
                    GraphStreamUtil gUtil = new GraphStreamUtil();
                    gUtil.normalizeNodesSizes(graph, Config.minNodeSize, Config.maxNodeSize);
                    gUtil.setupStyle(graph);
                    
                    Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
                    ViewPanel viewPanel = viewer.addDefaultView(false);

                    panel.removeAll();
                    panel.add(viewPanel);
                    panel.validate();
                    panel.updateUI();
                    panel.repaint();
                    
                    mapNodeID2Index(graph);
                    setupNodeCombo(graph);
                    setupEdgeCombo(graph);
                }
                if (ga.isFinished() || stop) {
                    ((javax.swing.Timer) e.getSource()).stop(); // stop the timer
                }
            }
        }).start();
    }
    
    public void setupNodeCombo(Graph graph){
        nodeBox.removeAllItems();
        if(graph != null){
            for(Node node : graph.getEachNode()){
                nodeBox.addItem(String.format("%s", getIndex(node.getId())));
            }
        }
        nodeBox.updateUI();
    }
    
    public void setupEdgeCombo(Graph graph){
        edgeBox.removeAllItems();
        if(graph != null){
            for(Edge edge : graph.getEachEdge()){
                String[] nodeCoord = edge.getId().split("_");
                String newID = getIndex(nodeCoord[0])+"_"+getIndex(nodeCoord[1]);
                edgeBox.addItem(newID);
            }
        }
        edgeBox.updateUI();
    }
    
    protected void mapNodeID2Index(Graph graph){
        id2IndexMap = new Hashtable<>();
        index2IdMap = new Hashtable<>();
        for(int i = 0; i < graph.getNodeCount(); i++){
            Integer index = new Integer(i);
            String coord = graph.getNode(i).getId();
            if(id2IndexMap.containsKey(coord)){
                System.out.println("Error: table already contains coord "+coord);
            }
            else{
                id2IndexMap.put(coord, index);
                index2IdMap.put(index, coord);
                //System.err.println(index+": "+coord);
            }
        }
    }
    
    protected Integer getIndex(String id){
        return id2IndexMap.get(id);
    }
    
    protected String getNodeID(Integer index){
        return index2IdMap.get(index);
    }
    
    @Override
    public void run() {
        viewPanel();
        viewBar();
        
        ga.run();
        
        Decoder barabasiDec = new Decoder(ga.getBestIndividual()); 
        GraphIndividual barabasiInd = barabasiDec.barabasiAlbertGraph();
        Decoder graphDec = new Decoder(barabasiInd);
        graph = graphDec.decode();
        DungeonGenerator dgg = new DungeonGenerator(graph);
        dgg.generate();
        ga.exportGraph(graph);
        
        JOptionPane.showMessageDialog(panel, "The Genetic Algorithm is finished!", 
                "Done",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public Hashtable<String, Integer> getId2IndexMap() {
        return id2IndexMap;
    }

    public void setId2IndexMap(Hashtable<String, Integer> id2IndexMap) {
        this.id2IndexMap = id2IndexMap;
    }

    public Hashtable<Integer, String> getIndex2IdMap() {
        return index2IdMap;
    }

    public void setIndex2IdMap(Hashtable<Integer, String> index2IdMap) {
        this.index2IdMap = index2IdMap;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
        this.ga.setFinished(stop);
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }
 
}
