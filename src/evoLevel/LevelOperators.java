/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author andre
 */
public class LevelOperators {

    private Random rng;

    public LevelOperators() {
        rng = new Random(System.nanoTime());
    }

    public LevelIndividual[] crossover(LevelIndividual father, LevelIndividual mother) {
        LevelIndividual[] son = new LevelIndividual[2];
        if (rng.nextDouble() < LevelConfig.crossoverProb) {
            son = independentOnePointCrossover(father, mother);
            return son;
        } else {
            son[0] = father.clone();
            son[1] = mother.clone();
            return son;
        }
    }

    public LevelIndividual mutation(LevelIndividual mutant) {
        if (rng.nextDouble() < LevelConfig.mutationProb) {
            //Individual mutant = graph.clone();
            int mutations = 4;
            switch (rng.nextInt(mutations)) {
                case 0: // add node
                    //System.out.println("Add Node Mutation");
                    return addNode(mutant);
                case 1: // remove node
                    //System.out.println("Remove Node Mutation");
                    return removeNode(mutant);
                case 2: // swap node
                    //System.out.println("Swap Node Mutation");
                    return swapNode(mutant);
                case 3: // tweak node (x, y) coords
                    //System.out.println("Tweak Node Mutation");
                    return tweakNode(mutant);
                default:
                    return mutant;
            }
        } else {
            return mutant;
        }
    }

    // creates a random node within the map bounds
    private LevelIndividual addNode(LevelIndividual original) {
        double width = 0;
        double height = 0;
        LevelIndividual mutant = original.clone();
        // first, find the bounds
        for (LevelGene node : mutant.getNodes()) {
            int[] xyz = node.getXYZ();
            if (xyz[0] > width) {
                width = xyz[0] + node.getWidth() / 2.0;
            }
            if (xyz[1] > height) {
                height = xyz[1] + node.getHeight() / 2.0;
            }
        }
        // allow the mutation to expand the bounds;
        width = width * LevelConfig.nodeXLeap;
        height = height * LevelConfig.nodeYLeap;
        // then, create the random node
        int[] xyz = new int[3];
        xyz[0] = (int) Math.max(rng.nextInt((int) width), LevelConfig.borderSize);
        xyz[1] = (int) Math.max(rng.nextInt((int) height), LevelConfig.borderSize);
        xyz[2] = 0;
        // node sides are currently not evolved, but automatically normalized based on degree
        //int deltaSize = (int) (LevelConfig.maxNodeSize - LevelConfig.minNodeSize);
        //int nodeWidth = (int) (rng.nextInt(deltaSize) + LevelConfig.minNodeSize);
        //int nodeHeight = (int) (rng.nextInt(deltaSize) + LevelConfig.minNodeSize);
        //int nodeHeight = nodeWidth;
        LevelGene node = new LevelGene();
        node.setXYZ(xyz);
        node.setWidth(LevelConfig.minNodeSize);
        node.setHeight(LevelConfig.minNodeSize);
        node.setConnectedNodes(new ArrayList<>());
        mutant.addNode(node);
        return mutant;
    }

    // simply get a random one and remove
    private LevelIndividual removeNode(LevelIndividual original) {
        LevelIndividual mutant = original.clone();
        mutant.getNodes().remove(rng.nextInt(mutant.getNodes().size()));
        return mutant;
    }

    // simple node swap
    private LevelIndividual swapNode(LevelIndividual original) {
        LevelIndividual mutant = original.clone();
        int point1 = rng.nextInt(mutant.getNodes().size());
        int point2 = rng.nextInt(mutant.getNodes().size());
        // prevent swaping with himself
        while (point1 == point2) {
            point1 = rng.nextInt(mutant.getNodes().size());
            point2 = rng.nextInt(mutant.getNodes().size());
        }
        LevelGene node1 = mutant.getNodes().get(point1).clone();
        LevelGene node2 = mutant.getNodes().get(point2).clone();
        mutant.getNodes().set(point2, node1);
        mutant.getNodes().set(point1, node2);
        return mutant;
    }

    private LevelIndividual tweakNode(LevelIndividual original) {
        LevelIndividual mutant = original.clone();
        int index = rng.nextInt(mutant.getNodes().size());
        LevelGene node = mutant.getNodes().get(index).clone();
        
        // first find the graph dimensions
        int width = 0;
        int height = 0;
        for(LevelGene gene : original.getNodes()){
            int[] p = gene.getXYZ();
            //System.out.println(node.getId()+": "+p.toString());
            width = (int) Math.max(width, p[0] + gene.getWidth()/2.0);
            height = (int) Math.max(height, p[1] + gene.getHeight()/2.0);
        }
        
        int[] xyz = new int[3];
        // find the possible leap size
        double xLeap = (LevelConfig.nodeXLeap - 1.0) * width;
        double yLeap = (LevelConfig.nodeYLeap - 1.0) * height;
        
        // find the bounds of the tweak for x
        double minX = Math.max(node.getXYZ()[0] - xLeap, LevelConfig.borderSize);
        double maxX =          node.getXYZ()[0] + xLeap;
        xyz[0] = (int) ((rng.nextDouble() * (maxX - minX)) + minX);
        
        // find the bounds of the tweak for y
        double minY = Math.max(node.getXYZ()[1] - yLeap, LevelConfig.borderSize);
        double maxY =          node.getXYZ()[1] + yLeap;
        xyz[1] = (int) ((rng.nextDouble() * (maxY - minY)) + minY);
        
        xyz[2] = 0;
        node.setXYZ(xyz);
        mutant.getNodes().set(index, node);
        return mutant;
    }

    public boolean isInvalid(LevelIndividual individual) {
        ArrayList<String> takenID = new ArrayList<>();
        for (LevelGene node : individual.getNodes()) {
            String id = node.getXYZ()[0] + "." + node.getXYZ()[1] + "." + node.getXYZ()[2];
            if (takenID.contains(id)) {
                return true;
            } else {
                takenID.add(id);
            }
        }
        return false;
    }

    public LevelIndividual fixInvalidIndividual(LevelIndividual badSon) {
        ArrayList<String> takenID = new ArrayList<>();
        LevelIndividual goodSon = new LevelIndividual();
        double width = 0;
        double height = 0;
        for (LevelGene node : badSon.getNodes()) {
            int[] xyz = node.getXYZ();
            if (xyz[0] > width) {
                width = xyz[0] + node.getWidth() / 2.0;
            }
            if (xyz[1] > height) {
                height = xyz[1] + node.getHeight() / 2.0;
            }
        }
        for (LevelGene node : badSon.getNodes()) {
            String id = node.getXYZ()[0] + "." + node.getXYZ()[1] + "." + node.getXYZ()[2];
            while (takenID.contains(id)) {
                int[] xyz = new int[3];
                int minX = (int) Math.max(LevelConfig.borderSize, node.getXYZ()[0] - node.getWidth());
                int maxX = (int) Math.min(node.getXYZ()[0] + node.getWidth(), width - LevelConfig.borderSize);
                xyz[0] = rng.nextInt(maxX - minX) + minX;

                int minY = (int) Math.max(LevelConfig.borderSize, node.getXYZ()[1] - node.getHeight());
                int maxY = (int) Math.min(node.getXYZ()[1] + node.getHeight(), height - LevelConfig.borderSize);
                xyz[1] = rng.nextInt(maxY - minY) + minY;
                xyz[2] = 0;

                node.setXYZ(xyz);
                node.setConnectedNodes(new ArrayList<>());
                id = node.getXYZ()[0] + "." + node.getXYZ()[1] + "." + node.getXYZ()[2];
                //int deltaSize = (int) (LevelConfig.maxNodeSize - LevelConfig.minNodeSize);
                //int nodeWidth = (int) (rng.nextInt(deltaSize) + LevelConfig.minNodeSize);
                //int nodeHeight = (int) (rng.nextInt(deltaSize) + LevelConfig.minNodeSize);
                //node.setWidth(nodeWidth);
                //node.setHeight(nodeWidth);
            }
            takenID.add(id);
            goodSon.addNode(node);
        }
        return goodSon;
    }

    private LevelIndividual[] independentOnePointCrossover(LevelIndividual father, LevelIndividual mother) {
        LevelIndividual[] son = new LevelIndividual[2];
        for (int i = 0; i < son.length; i++) {
            son[i] = new LevelIndividual();
        }
        // generate points and ensure they are inside bounds
        int pointF = rng.nextInt(father.getNodes().size());
        pointF = Math.min(mother.getNodes().size(), pointF);
        int pointM = rng.nextInt(mother.getNodes().size());
        pointM = Math.min(father.getNodes().size(), pointM);

        // build the first son
        for (int i = 0; i < pointF; i++) {
            son[0].getNodes().add(father.getNode(i).clone());
        }
        for (int i = pointF; i < mother.getNodes().size(); i++) {
            son[0].getNodes().add(mother.getNode(i).clone());
        }
        // build the second son
        for (int i = 0; i < pointM; i++) {
            son[1].getNodes().add(mother.getNode(i).clone());
        }
        for (int i = pointM; i < father.getNodes().size(); i++) {
            son[1].getNodes().add(father.getNode(i).clone());
        }

        return son;
    }

    // based on x,y coordinates, currently just cutting on x
    private LevelIndividual[] geographicCrossover(LevelIndividual father, LevelIndividual mother) {
        LevelIndividual[] son = new LevelIndividual[2];
        for (int i = 0; i < son.length; i++) {
            son[i] = new LevelIndividual();
        }

        double pointX = rng.nextDouble();
        //double pointY = rng.nextDouble();
        System.out.println("Point X: " + pointX);
        double fatherWidth = 0;
        double fatherHeight = 0;
        double motherWidth = 0;
        double motherHeight = 0;

        for (LevelGene node : father.getNodes()) {
            int[] xyz = node.getXYZ();
            if (xyz[0] > fatherWidth) {
                fatherWidth = xyz[0] + node.getWidth() / 2.0;
            }
            if (xyz[1] > fatherHeight) {
                fatherHeight = xyz[1] + node.getHeight() / 2.0;
            }
        }
        System.out.println("Father Sides: " + fatherWidth + " x " + fatherHeight);

        for (LevelGene node : mother.getNodes()) {
            int[] xyz = node.getXYZ();
            if (xyz[0] > motherWidth) {
                motherWidth = xyz[0] + node.getWidth() / 2.0;
            }
            if (xyz[1] > motherHeight) {
                motherHeight = xyz[1] + node.getHeight() / 2.0;
            }
        }
        System.out.println("Mother Sides: " + motherWidth + " x " + motherHeight);

        double fatherPoint = fatherWidth * pointX;
        double motherPoint = motherWidth * pointX;
        System.out.println("Father Point X at: " + fatherPoint);
        System.out.println("Mother Point X at: " + motherPoint);

        for (LevelGene node : father.getNodes()) {
            int[] xyz = node.getXYZ();
            if (xyz[0] < fatherPoint) {
                son[0].addNode(node);
            } else {
                int x = (int) ((xyz[0] / fatherWidth) * motherWidth);
                int y = (int) ((xyz[1] / fatherHeight) * motherHeight);
                int z = 0;
                int[] newXYZ = {x, y, z};
                node.setXYZ(newXYZ);
                son[1].addNode(node);
            }
        }
        for (LevelGene node : mother.getNodes()) {
            int[] xyz = node.getXYZ();
            if (xyz[0] < motherPoint) {
                son[1].addNode(node);
            } else {
                int x = (int) ((xyz[0] / motherWidth) * fatherWidth);
                int y = (int) ((xyz[1] / motherHeight) * fatherHeight);
                int z = 0;
                int[] newXYZ = {x, y, z};
                node.setXYZ(newXYZ);
                son[0].addNode(node);
            }
        }

        return son;
    }

    private LevelIndividual[] graphX(LevelIndividual father, LevelIndividual mother) {
        LevelIndividual[] son = new LevelIndividual[2];
        son[0] = new LevelIndividual();
        son[1] = new LevelIndividual();

        LevelIndividual smallGraph;
        LevelIndividual bigGraph;
        System.out.println("Father Size:" + father.getNodes().size());
        System.out.println("Mother Size:" + mother.getNodes().size());
        if (father.getNodes().size() > mother.getNodes().size()) {
            smallGraph = mother;
            bigGraph = father;
        } else {
            smallGraph = father;
            bigGraph = mother;
        }

        int size = bigGraph.getNodes().size();
        boolean expanded = false;
        if (rng.nextDouble() < LevelConfig.expansionProb) {
            size++;
            expanded = true;
        }

        int[][] smallAdj = getAdjMatrix(smallGraph, size, expanded);
        int[][] bigAdj = getAdjMatrix(bigGraph, size, expanded);
        printAdjMatrix("Small Matrix\nSize " + size + "" + (expanded ? " (expanded)" : "") + ":", smallAdj);
        printAdjMatrix("Big Matrix\nSize " + size + "" + (expanded ? " (expanded)" : "") + ":", bigAdj);

        int point1 = rng.nextInt(size * size);
        int point2 = rng.nextInt(size * size);
        point1 = Math.min(point1, point2);
        point2 = Math.max(point1, point2);

        int trimRow = (int) (point2 / size);
        trimRow = Math.max(trimRow, smallGraph.getNodes().size());
        System.out.println("Point2: " + point2 + " Trim Row: " + trimRow);

        int[][] smallSon = new int[size][size];
        int[][] bigSon = new int[size][size];

        int index = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++, index++) {
                if (index < point1 || index > point2) {
                    smallSon[i][j] = smallAdj[i][j];
                    bigSon[i][j] = bigAdj[i][j];
                } else {
                    smallSon[i][j] = bigAdj[i][j];
                    bigSon[i][j] = smallAdj[i][j];
                }
            }
        }
        if (smallGraph.getNodes().size() < bigGraph.getNodes().size()) {
            smallSon = getTrimMatrix(trimRow, smallSon);
        }
        printAdjMatrix("Small Son Matrix\nSize " + smallSon.length + "" + (expanded ? " (expanded)" : "") + ":", smallSon);
        printAdjMatrix("Big Son Matrix\nSize " + bigSon.length + "" + (expanded ? " (expanded)" : "") + ":", bigSon);

        double smallWidth = 0;
        double smallHeight = 0;
        double bigWidth = 0;
        double bigHeight = 0;

        for (LevelGene node : smallGraph.getNodes()) {
            int[] xyz = node.getXYZ();
            if (xyz[0] > smallWidth) {
                smallWidth = xyz[0] + node.getWidth() / 2.0;
            }
            if (xyz[1] > smallHeight) {
                smallHeight = xyz[1] + node.getHeight() / 2.0;
            }
        }
        for (LevelGene node : bigGraph.getNodes()) {
            int[] xyz = node.getXYZ();
            if (xyz[0] > bigWidth) {
                bigWidth = xyz[0] + node.getWidth() / 2.0;
            }
            if (xyz[1] > bigHeight) {
                bigHeight = xyz[1] + node.getHeight() / 2.0;
            }
        }

        ArrayList<LevelGene> smallArray = getNodeArray(smallGraph, size);
        ArrayList<LevelGene> bigArray = getNodeArray(bigGraph, size);

        point1 = rng.nextInt(size);
        point2 = rng.nextInt(size);
        point1 = Math.min(point1, point2);
        point2 = Math.max(point1, point2);

        ArrayList<LevelGene> smallSonArray = new ArrayList<>();
        ArrayList<LevelGene> bigSonArray = new ArrayList<>();

        for (int i = 0; i < point1; i++) {
            smallSonArray.add(smallArray.get(i));
            bigSonArray.add(bigArray.get(i));
        }
        for (int i = point1; i < point2; i++) {
            smallSonArray.add(bigArray.get(i));
            bigSonArray.add(smallArray.get(i));
        }
        for (int i = point2; i < size; i++) {
            smallSonArray.add(smallArray.get(i));
            bigSonArray.add(bigArray.get(i));
        }
        while (smallSonArray.size() > trimRow) {
            smallSonArray.remove(smallSonArray.size() - 1);
        }

        smallSonArray = fixConnections(smallSonArray, smallSon);
        bigSonArray = fixConnections(bigSonArray, bigSon);

        son[0].setNodes(smallSonArray);
        son[1].setNodes(bigSonArray);

        return son;
    }

    private ArrayList<LevelGene> getNodeArray(LevelIndividual graph, int size) {
        ArrayList<LevelGene> nodes = new ArrayList();
        for (int i = 0; i < graph.getNodes().size(); i++) {
            nodes.add(graph.getNode(i));
        }

        if (nodes.size() < size) {
            double smallWidth = 0;
            double smallHeight = 0;
            for (LevelGene node : graph.getNodes()) {
                int[] xyz = node.getXYZ();
                if (xyz[0] > smallWidth) {
                    smallWidth = xyz[0] + node.getWidth() / 2.0;
                }
                if (xyz[1] > smallHeight) {
                    smallHeight = xyz[1] + node.getHeight() / 2.0;
                }
            }

            while (graph.getNodes().size() < size) {
                LevelGene node = new LevelGene();
                int[] xyz = new int[3];
                xyz[0] = rng.nextInt((int) smallWidth);
                xyz[1] = rng.nextInt((int) smallHeight);
                xyz[2] = 0;
                int deltaSize = (int) (LevelConfig.maxNodeSize - LevelConfig.minNodeSize);
                int nodeWidth = (int) (rng.nextInt(deltaSize) + LevelConfig.minNodeSize);
                //int nodeHeight = (int) (rng.nextInt(deltaSize) + LevelConfig.minNodeSize);
                int nodeHeight = nodeWidth;
                node.setXYZ(xyz);
                node.setWidth(nodeHeight);
                node.setHeight(nodeHeight);
                node.setConnectedNodes(new ArrayList<>());
                nodes.add(node);
            }
        }
        return nodes;
    }

    private ArrayList<LevelGene> fixConnections(ArrayList<LevelGene> nodes, int[][] matrix) {
        ArrayList<LevelGene> fixedNodes = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).setConnectedNodes(new ArrayList<>());
            for (int j = 0; j < nodes.size(); j++) {
                if (matrix[i][j] == 1) {
                    nodes.get(i).getConnectedNodes().add(nodes.get(j));
                }
            }
            fixedNodes.add(nodes.get(i));
        }
        return fixedNodes;
    }

    private int[][] getAdjMatrix(LevelIndividual individual, int size, boolean expanded) {
        int[][] matrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            if (i < individual.getNodes().size()) {
                LevelGene node0 = individual.getNode(i);
                for (int j = 0; j < size; j++) {
                    if (i == j) {
                        matrix[i][j] = 0;
                    } else {
                        if (j < individual.getNodes().size()) {
                            LevelGene node1 = individual.getNode(j);
                            if (node0.getConnectedNodes().contains(node1)) {
                                matrix[i][j] = 1;
                            } else {
                                matrix[i][j] = 0;
                            }
                        } else {
                            if (expanded) {
                                matrix[i][j] = rng.nextInt(2);
                            } else {
                                matrix[i][j] = 0;
                            }
                        }
                    }
                }
            } else {
                for (int j = 0; j < size; j++) {
                    matrix[i][j] = 0;
                }
            }
        }
        return matrix;
    }

    private int[][] getTrimMatrix(int size, int[][] matrix) {
        int[][] trim = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                trim[i][j] = matrix[i][j];
            }
        }
        return trim;
    }

    private void printAdjMatrix(String header, int[][] matrix) {
        System.out.println(header);
        for (int i = 0; i < matrix.length; i++) {
            System.out.println(Arrays.toString(matrix[i]));
        }
    }
}
