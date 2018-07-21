/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoLevel;

import config.GeneralConfig;
import java.util.ArrayList;
import org.graphstream.algorithm.APSP;
import org.graphstream.algorithm.APSP.APSPInfo;
import org.graphstream.algorithm.AStar;
import org.graphstream.algorithm.AStar.DistanceCosts;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphPosLengthUtils;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.nodePointPosition;

/**
 *
 * @author andre
 */
public class LevelEvaluation {

    public LevelEvaluation() {
        /*
        double uwe = org.graphstream.algorithm.Toolkit.unweightedEccentricity(graph.getNode(0), false);
        System.out.println("UWE: "+uwe+" ("+graph.getNode(0).getId()+")");
         */
    }

    public double fitness(LevelIndividual individual, boolean debug) {
        LevelDecoder barabasiDecoder = new LevelDecoder(individual);
        LevelIndividual barabasi = barabasiDecoder.barabasiAlbertGraph();
        LevelDecoder graphDecoder = new LevelDecoder(barabasi);
        Graph graph = graphDecoder.decode();
        //DungeonGenerator dgg = new DungeonGenerator(graph);
        //dgg.generate();
        
        double areaIntersection = nodeAreaOverlapPenalty(graph);
        double edgeIntersection = edgeIntersectionPenalty(graph);
        double minEdgeDistance = minEdgeDistancePenalty(graph);
        double sizePenalty = nodeCountPenalty(graph);
        
        double avgSP = LevelConfig.useAverageShortestPath? efficientAStarAVGShortestPath(graph) : 0;
        double undesiredAngleSum = LevelConfig.useDesiredAngles? undesiredAngleSum(graph) : 0;
        //double nonLinearity = LevelConfig.useIdealNonLinearity? distanceFromIdealNonLinearity(graph) : 0; (moved to puzzle project)
        
        double fitness = areaIntersection + edgeIntersection + minEdgeDistance + sizePenalty + avgSP + undesiredAngleSum;
        //+ nonLinearity; (moved to puzzle project)
        if (debug) {
            System.out.println("Size: " + individual.getNodes().size() + ", Fitness: " + fitness + 
                    "(AIP: " + areaIntersection + 
                    " EIP: " + edgeIntersection + 
                    " SP: "  + sizePenalty + 
                    " MEP: " + minEdgeDistance +
                    
                    (LevelConfig.useAverageShortestPath? " ASP: " + avgSP : "" )+ 
                    (LevelConfig.useDesiredAngles?       " UAS: " + undesiredAngleSum : "") +
                    //(LevelConfig.useIdealNonLinearity?   " DIN: " + nonLinearity : "") + (moved to puzzle project)
                    ")");
        }

        return fitness;
    }
    // same as above, but as an array with each fitness component
    public double[] detailedFitness(LevelIndividual individual, boolean debug){
        //Decoder dungeonDecoder = new LevelDecoder(individual);
        //Graph graph = dungeonDecoder.individualToDungeon();
        
        LevelDecoder barabasiDec = new LevelDecoder(individual);
        LevelIndividual barabasiInd = barabasiDec.barabasiAlbertGraph();
        LevelDecoder graphDec = new LevelDecoder(barabasiInd);
        Graph graph = graphDec.decode();
        //DungeonGenerator dgg = new DungeonGenerator(graph);
        //dgg.generate(debug);
        
        return detailedFitness(graph, debug);
    }
    
    // detailded fitness without decode
    public double[] detailedFitness(Graph graph, boolean debug){
 
        double[] detailedFitness = new double[8];
        double areaIntersection = nodeAreaOverlapPenalty(graph);
        double edgeIntersection = edgeIntersectionPenalty(graph);
        double minEdgeDist = minEdgeDistancePenalty(graph);
        double sizePenalty = nodeCountPenalty(graph);
        double avgSP = LevelConfig.useAverageShortestPath? efficientAStarAVGShortestPath(graph) : 0;
        double undesiredAngleSum = LevelConfig.useDesiredAngles? undesiredAngleSum(graph) : 0;
        //double nonLinearity = LevelConfig.useIdealNonLinearity? distanceFromIdealNonLinearity(graph) : 0; (moved to puzzle project)
        
        double fitness =
            // Penalty
            areaIntersection + edgeIntersection + minEdgeDist + sizePenalty +
            // Preference
            avgSP + undesiredAngleSum ;
            // + nonLinearity (moved to puzzle project)
        if (debug) {
            System.out.println("Size: " + graph.getNodeCount() + ", Fitness: " + fitness + 
                    "(AIP: " + areaIntersection + 
                    " EIP: " + edgeIntersection + 
                    " SP: "  + sizePenalty + 
                    " MEP: " + minEdgeDist +
                    
                    (LevelConfig.useAverageShortestPath? " ASP: " + avgSP : "" )+ 
                    (LevelConfig.useDesiredAngles?       " UAS: " + undesiredAngleSum : "") +
                    //(LevelConfig.useIdealNonLinearity?   " DIN: " + nonLinearity : "") + (moved to puzzle project)
                    ")");
        }
        detailedFitness[0] = graph.getNodeCount();
        detailedFitness[1] = fitness;
        detailedFitness[2] = areaIntersection;
        detailedFitness[3] = edgeIntersection;
        detailedFitness[4] = sizePenalty;
        detailedFitness[5] = minEdgeDist;
        detailedFitness[6] = avgSP;
        detailedFitness[7] = undesiredAngleSum;
        //detailedFitness[8] = nonLinearity;
        return detailedFitness;
    }
    
    /*public int distanceFromIdealNonLinearity(Graph graph){
        Node start = null;
        Node boss = null;
        Hashtable<String, Integer> pathCount = new Hashtable<>();
        ArrayList<Node> targets = new ArrayList<>();
        for(int i = 0; i < graph.getNodeCount(); i++){
            Node node = graph.getNode(i);
            Symbol symbol = node.getAttribute("symbol");
            if(symbol != null || !symbol.isNothinig()){
                if(symbol.isKey())
                    targets.add(node);
                else if(symbol.isStart())
                    start = node;
                else if(symbol.isBoss())
                    boss = node;
            }
        }
        Node current = start;
        Collections.sort(targets, KEYVALUE_COMPARATOR);
        targets.add(boss);
        
        // prevent the existance of broken dungeons and grants solvability on final solutions
        if(targets.size() == 1)
            return 1000000;
        
        //for(Node node : targets){
        //    Symbol symbol = node.getAttribute("symbol");
        //    System.out.println(node.getIndex()+": "+symbol.toString());
        //}
        int keyLevel = 0;
        for(Node node : targets){
            Symbol symbol = node.getAttribute("symbol");
            if(symbol.isKey())
                keyLevel = symbol.getValue()-1;
            if(symbol.isBoss())
                keyLevel++;
            
            LevelDistanceCost ddc = new LevelDistanceCost(keyLevel);
            AStar astar = new AStar(graph);
            astar.setCosts(ddc);
            astar.compute(current.getId(), node.getId());
            Path path = astar.getShortestPath();
            
            double cost = 0;
            //System.out.println("Next key: "+symbol.toString()+": ");
            Node pathStep = current;
            for (Node step : path.getNodePath()) {
                if(step.equals(path.getRoot())){
                    continue;
                }
                else{
                    if(pathCount.containsKey(step.getId())){
                        int value = pathCount.get(step.getId());
                        pathCount.replace(step.getId(), ++value);
                    }
                    else{
                        pathCount.put(step.getId(), 1);
                    }
                }
                Edge edge = pathStep.getEdgeBetween(step);
                Symbol edgeKey = edge.getAttribute("symbol");
                double penalty = keyLevel < edgeKey.getValue() ? ((edgeKey.getValue()-keyLevel)*1000000) : 0;
                cost += edgeLength(edge) + penalty;
                // if(keyLevel < edgeKey.getValue())
                //     System.out.println(keyLevel+" < "+edgeKey.getValue());
                //System.out.println(step.getIndex()+" cost: "+(edgeLength(pathStep.getEdgeBetween(step)) + penalty));
                pathStep = step;
            }
            //System.out.println();
            // prevent the existance of broken dungeons and grants solvability on final solutions
            if(cost > 1000000)
                return (int) cost;
            current = node;
        }
        int nonlinearity = Integer.MIN_VALUE;
        //String maxKey = "";
        for(String key : pathCount.keySet()){
            if(nonlinearity < pathCount.get(key)){
                nonlinearity = pathCount.get(key);
                //maxKey = key;
            }
        }
        //System.out.println("Nonlinearity: "+maxKey+": "+nonlinearity);
        return Math.abs(nonlinearity - LevelConfig.idealNonLinearity);
    }*/
    
    /*protected static final Comparator<Node> KEYVALUE_COMPARATOR = new Comparator<Node>(){
        @Override
        public int compare(Node arg0, Node arg1) {
            Symbol key0 = arg0.getAttribute("symbol");
            Symbol key1 = arg1.getAttribute("symbol");
            return key0.getValue() > key1.getValue() ? 1
                 : key0.getValue() < key1.getValue() ? -1
                 : 0;
        }
    };*/
    
    protected double nodeAreaOverlapPenalty(Graph graph){
        return nodeAreaOverlap(graph) * LevelConfig.areaIntersectionPenalty;
    }
    
    // sums the minimal angle difference of each edge
    public double undesiredAngleSum(Graph graph){
        double undesiredAngle = 0;
        if(LevelConfig.desiredAngles.length > 0){
            for(Edge edge : graph.getEachEdge()){
                undesiredAngle += minimalAngleDifference(edge);
            }
        }
        return undesiredAngle;
    }
    // find the min difference from the actual angle and the closest desired angle
    public double minimalAngleDifference(Edge edge){
        double angle = Math.abs(angleInDegrees(edge));
        double min = 180;
        for(int i = 0; i < LevelConfig.desiredAngles.length; i++)
            min = Math.min(min, Math.abs(LevelConfig.desiredAngles[i] - angle));
        return min;
    }
    // calculates theta: https://math.stackexchange.com/questions/1201337/finding-the-angle-between-two-points
    public double angleInRad(Edge edge){
        Node nodeA = edge.getNode0();
        Node nodeB = edge.getNode1();
        Point3 pA = nodePointPosition(nodeA);
        Point3 pB = nodePointPosition(nodeB);
        double angleRad = Math.atan2(pB.y - pA.y, pB.x - pA.x);
        return angleRad;
    }
    
    public double angleInDegrees(Edge edge){ 
        double angleDeg = angleInRad(edge) * 180 / Math.PI;
        return angleDeg;
    }
    
    protected double minEdgeDistancePenalty(Graph graph){
        double violations = 0;
        double penalty = 0;
        
        for(Edge edge : graph.getEachEdge()){
            // if the rooms are in a bad position, there must be an edge to ensure connection
            if(!checkAcceptableNodePosition(edge)){
                double distance = edgeDistance(edge);
                violations += Math.min(0, distance - LevelConfig.minEdgeDistance);
            }
        }
        penalty = Math.pow(violations, 2) * LevelConfig.edgeDistancePentalty;
        return penalty;
    }
    
    // check if the position is acceptable, and return true if it is
    // acceptable = it is possible to connect both without break the map
    // if not, we must create a good hall
    protected boolean checkAcceptableNodePosition(Edge edge) {
        Node nodeA = edge.getNode0();
        Node nodeB = edge.getNode1();
        Point3 pA = nodePointPosition(nodeA);
        Point3 pB = nodePointPosition(nodeB);
        // difference of coordinates to find location
        int deltaX = (int) Math.abs(pA.x - pB.x);
        int deltaY = (int) Math.abs(pA.y - pB.y);
        // north || south
        if ((pB.y > pA.y) && (deltaY >= deltaX) || ((pB.y < pA.y) && (deltaY >= deltaX))) {
            double diff = 0;
            if(pB.x >= pA.x){
                double pointA = pA.x + (double)nodeA.getAttribute("width")/2.0;
                double pointB = pB.x - (double)nodeB.getAttribute("width")/2.0;
                diff = pointA - pointB;
            }
            else{
                double pointA = pA.x - (double)nodeA.getAttribute("width")/2.0;
                double pointB = pB.x + (double)nodeB.getAttribute("width")/2.0;
                diff = pointB - pointA;
            }
            if(diff >= GeneralConfig.edgeSize + 2.0)
                return true;
        }
        // east || west 
        else if(((pB.x > pA.x) && (deltaX >= deltaY)) || ((pB.x < pA.x) && (deltaX >= deltaY))){
            double diff = 0;
            if(pB.y >= pA.y){
                double pointA = pA.y + (double)nodeA.getAttribute("height")/2.0;
                double pointB = pB.y - (double)nodeB.getAttribute("height")/2.0;
                diff = pointA - pointB;
            }
            else{
                double pointA = pA.y - (double)nodeA.getAttribute("height")/2.0;
                double pointB = pB.y + (double)nodeB.getAttribute("height")/2.0;
                diff = pointB - pointA;
            }
            if(diff >= GeneralConfig.edgeSize + 2.0)
                return true;
        }
        return false;
    }

    protected double edgeDistance(Edge edge) {
        Node nodeA = edge.getNode0();
        Node nodeB = edge.getNode1();
        Point3 pA = nodePointPosition(nodeA);
        Point3 pB = nodePointPosition(nodeB);

        // line equation of the edge
        double A1 = pB.y - pA.y;
        double B1 = pA.x - pB.x;
        double C1 = A1 * pA.x + B1 * pA.y;

        // difference of coordinates to find location
        int deltaX = (int) Math.abs(pA.x - pB.x);
        int deltaY = (int) Math.abs(pA.y - pB.y);

        // get the 4 corner points from A
        Point3 bottomLeftA = new Point3();
        bottomLeftA.x = (int) (pA.x - ((double) nodeA.getAttribute("width") / 2.0f));
        bottomLeftA.y = (int) (pA.y - ((double) nodeA.getAttribute("height") / 2.0f));
        bottomLeftA.z = 0;
        Point3 bottomRightA = new Point3();
        bottomRightA.x = (int) (pA.x + ((double) nodeA.getAttribute("width") / 2.0f));
        bottomRightA.y = (int) (pA.y - ((double) nodeA.getAttribute("height") / 2.0f));
        bottomRightA.z = 0;
        Point3 topLeftA = new Point3();
        topLeftA.x = (int) (pA.x - ((double) nodeA.getAttribute("width") / 2.0f));
        topLeftA.y = (int) (pA.y + ((double) nodeA.getAttribute("height") / 2.0f));
        topLeftA.z = 0;
        Point3 topRightA = new Point3();
        topRightA.x = (int) (pA.x + ((double) nodeA.getAttribute("width") / 2.0f));
        topRightA.y = (int) (pA.y + ((double) nodeA.getAttribute("height") / 2.0f));
        topRightA.z = 0;

        // get the 4 corner points from B
        Point3 bottomLeftB = new Point3();
        bottomLeftB.x = (int) (pB.x - ((double) nodeB.getAttribute("width") / 2.0f));
        bottomLeftB.y = (int) (pB.y - ((double) nodeB.getAttribute("height") / 2.0f));
        bottomLeftB.z = 0;
        Point3 bottomRightB = new Point3();
        bottomRightB.x = (int) (pB.x + ((double) nodeB.getAttribute("width") / 2.0f));
        bottomRightB.y = (int) (pB.y - ((double) nodeB.getAttribute("height") / 2.0f));
        bottomRightB.z = 0;
        Point3 topLeftB = new Point3();
        topLeftB.x = (int) (pB.x - ((double) nodeB.getAttribute("width") / 2.0f));
        topLeftB.y = (int) (pB.y + ((double) nodeB.getAttribute("height") / 2.0f));
        topLeftB.z = 0;
        Point3 topRightB = new Point3();
        topRightB.x = (int) (pB.x + ((double) nodeB.getAttribute("width") / 2.0f));
        topRightB.y = (int) (pB.y + ((double) nodeB.getAttribute("height") / 2.0f));
        topRightB.z = 0;

        Point3 doorA = new Point3();
        Point3 doorB = new Point3();

        //A's north to B's south
        if ((pB.y > pA.y) && (deltaY >= deltaX)) {
            // line equation of the A's north wall
            double A2 = topRightA.y - topLeftA.y;
            double B2 = topLeftA.x - topRightA.x;
            double C2 = A2 * topLeftA.x + B2 * topLeftA.y;
            doorA = calculateIntersectionPoint(A1, B1, C1, A2, B2, C2);

            // line equation of the B's south wall
            double A3 = bottomLeftB.y - bottomRightB.y;
            double B3 = bottomRightB.x - bottomLeftB.x;
            double C3 = A3 * bottomRightB.x + B3 * bottomRightB.y;
            doorB = calculateIntersectionPoint(A1, B1, C1, A3, B3, C3);
        } //A's south to B's north
        else if ((pB.y < pA.y) && (deltaY >= deltaX)) {
            // line equation of the A's south wall
            double A2 = bottomRightA.y - bottomLeftA.y;
            double B2 = bottomLeftA.x - bottomRightA.x;
            double C2 = A2 * bottomLeftA.x + B2 * bottomLeftA.y;
            doorA = calculateIntersectionPoint(A1, B1, C1, A2, B2, C2);

            // line equation of the B's north wall
            double A3 = topRightB.y - topLeftB.y;
            double B3 = topLeftB.x - topRightB.x;
            double C3 = A3 * topLeftB.x + B3 * topLeftB.y;
            doorB = calculateIntersectionPoint(A1, B1, C1, A3, B3, C3);
        } //A's east to B's west
        else if ((pB.x > pA.x) && (deltaX >= deltaY)) {
            // line equation of the right wall
            double A2 = bottomRightA.y - topRightA.y;
            double B2 = topRightA.x - bottomRightA.x;
            double C2 = A2 * topRightA.x + B2 * topRightA.y;
            doorA = calculateIntersectionPoint(A1, B1, C1, A2, B2, C2);

            // line equation of the left wall
            double A3 = topLeftB.y - bottomLeftB.y;
            double B3 = bottomLeftB.x - topLeftB.x;
            double C3 = A3 * bottomLeftB.x + B3 * bottomLeftB.y;
            doorB = calculateIntersectionPoint(A1, B1, C1, A3, B3, C3);
        } //A's west to B's east
        else if ((pB.x < pA.x) && (deltaX >= deltaY)) {
            // line equation of the left wall
            double A2 = topLeftA.y - bottomLeftA.y;
            double B2 = bottomLeftA.x - topLeftA.x;
            double C2 = A2 * bottomLeftA.x + B2 * bottomLeftA.y;
            doorA = calculateIntersectionPoint(A1, B1, C1, A2, B2, C2);

            // line equation of the right wall
            double A3 = bottomRightB.y - topRightB.y;
            double B3 = topRightB.x - bottomRightB.x;
            double C3 = A3 * topRightB.x + B3 * topRightB.y;
            doorB = calculateIntersectionPoint(A1, B1, C1, A3, B3, C3);
        }
        double edgeABDist = dist(doorA.x, doorA.y, doorA.z, doorB.x, doorB.y, doorB.z);
        return edgeABDist;
    }

    public double edgeDistance(LevelGene nodeA, LevelGene nodeB) {
        int[] xyzA = nodeA.getXYZ();
        Point3 pA = new Point3(xyzA[0], xyzA[1], xyzA[2]);
        int[] xyzB = nodeA.getXYZ();
        Point3 pB = new Point3(xyzB[0], xyzB[1], xyzB[2]);

        // line equation of the edge
        double A1 = pB.y - pA.y;
        double B1 = pA.x - pB.x;
        double C1 = A1 * pA.x + B1 * pA.y;

        // difference of coordinates to find location
        int deltaX = (int) Math.abs(pA.x - pB.x);
        int deltaY = (int) Math.abs(pA.y - pB.y);

        // get the 4 corner points from A
        Point3 bottomLeftA = new Point3();
        bottomLeftA.x = (int) (pA.x - ((double) nodeA.getWidth() / 2.0f));
        bottomLeftA.y = (int) (pA.y - ((double) nodeA.getHeight() / 2.0f));
        bottomLeftA.z = 0;
        Point3 bottomRightA = new Point3();
        bottomRightA.x = (int) (pA.x + ((double) nodeA.getWidth() / 2.0f));
        bottomRightA.y = (int) (pA.y - ((double) nodeA.getHeight() / 2.0f));
        bottomRightA.z = 0;
        Point3 topLeftA = new Point3();
        topLeftA.x = (int) (pA.x - ((double) nodeA.getWidth() / 2.0f));
        topLeftA.y = (int) (pA.y + ((double) nodeA.getHeight() / 2.0f));
        topLeftA.z = 0;
        Point3 topRightA = new Point3();
        topRightA.x = (int) (pA.x + ((double) nodeA.getWidth() / 2.0f));
        topRightA.y = (int) (pA.y + ((double) nodeA.getHeight() / 2.0f));
        topRightA.z = 0;

        // get the 4 corner points from B
        Point3 bottomLeftB = new Point3();
        bottomLeftB.x = (int) (pB.x - ((double) nodeB.getWidth() / 2.0f));
        bottomLeftB.y = (int) (pB.y - ((double) nodeB.getHeight() / 2.0f));
        bottomLeftB.z = 0;
        Point3 bottomRightB = new Point3();
        bottomRightB.x = (int) (pB.x + ((double) nodeB.getWidth() / 2.0f));
        bottomRightB.y = (int) (pB.y - ((double) nodeB.getHeight() / 2.0f));
        bottomRightB.z = 0;
        Point3 topLeftB = new Point3();
        topLeftB.x = (int) (pB.x - ((double) nodeB.getWidth() / 2.0f));
        topLeftB.y = (int) (pB.y + ((double) nodeB.getHeight() / 2.0f));
        topLeftB.z = 0;
        Point3 topRightB = new Point3();
        topRightB.x = (int) (pB.x + ((double) nodeB.getWidth() / 2.0f));
        topRightB.y = (int) (pB.y + ((double) nodeB.getHeight() / 2.0f));
        topRightB.z = 0;

        Point3 doorA = new Point3();
        Point3 doorB = new Point3();

        //A's north to B's south
        if ((pB.y > pA.y) && (deltaY > deltaX)) {
            // line equation of the A's north wall
            double A2 = topRightA.y - topLeftA.y;
            double B2 = topLeftA.x - topRightA.x;
            double C2 = A2 * topLeftA.x + B2 * topLeftA.y;
            doorA = calculateIntersectionPoint(A1, B1, C1, A2, B2, C2);

            // line equation of the B's south wall
            double A3 = bottomLeftB.y - bottomRightB.y;
            double B3 = bottomRightB.x - bottomLeftB.x;
            double C3 = A3 * bottomRightB.x + B3 * bottomRightB.y;
            doorB = calculateIntersectionPoint(A1, B1, C1, A3, B3, C3);
        } //A's south to B's north
        else if ((pB.y < pA.y) && (deltaY > deltaX)) {
            // line equation of the A's south wall
            double A2 = bottomRightA.y - bottomLeftA.y;
            double B2 = bottomLeftA.x - bottomRightA.x;
            double C2 = A2 * bottomLeftA.x + B2 * bottomLeftA.y;
            doorA = calculateIntersectionPoint(A1, B1, C1, A2, B2, C2);

            // line equation of the B's north wall
            double A3 = topRightB.y - topLeftB.y;
            double B3 = topLeftB.x - topRightB.x;
            double C3 = A3 * topLeftB.x + B3 * topLeftB.y;
            doorB = calculateIntersectionPoint(A1, B1, C1, A3, B3, C3);
        } //A's east to B's west
        else if ((pB.x > pA.x) && (deltaX > deltaY)) {
            // line equation of the right wall
            double A2 = bottomRightA.y - topRightA.y;
            double B2 = topRightA.x - bottomRightA.x;
            double C2 = A2 * topRightA.x + B2 * topRightA.y;
            doorA = calculateIntersectionPoint(A1, B1, C1, A2, B2, C2);

            // line equation of the left wall
            double A3 = topLeftB.y - bottomLeftB.y;
            double B3 = bottomLeftB.x - topLeftB.x;
            double C3 = A3 * bottomLeftB.x + B3 * bottomLeftB.y;
            doorB = calculateIntersectionPoint(A1, B1, C1, A3, B3, C3);
        } //A's west to B's east
        else if ((pB.x < pA.x) && (deltaX > deltaY)) {
            // line equation of the left wall
            double A2 = topLeftA.y - bottomLeftA.y;
            double B2 = bottomLeftA.x - topLeftA.x;
            double C2 = A2 * bottomLeftA.x + B2 * bottomLeftA.y;
            doorA = calculateIntersectionPoint(A1, B1, C1, A2, B2, C2);

            // line equation of the right wall
            double A3 = bottomRightB.y - topRightB.y;
            double B3 = topRightB.x - bottomRightB.x;
            double C3 = A3 * topRightB.x + B3 * topRightB.y;
            doorB = calculateIntersectionPoint(A1, B1, C1, A3, B3, C3);
        }
        double edgeABDist = dist(doorA.x, doorA.y, doorA.z, doorB.x, doorB.y, doorB.z);
        return edgeABDist;
    }
    
    public Point3 calculateIntersectionPoint(double A1, double B1, double C1, double A2, double B2, double C2) {
        Point3 door = new Point3();
        double det = A1 * B2 - A2 * B1;
        if (det == 0) {
            //Lines are parallel
        } else {
            int x = (int) ((B2 * C1 - B1 * C2) / det);
            int y = (int) ((A1 * C2 - A2 * C1) / det);
            door = new Point3(x, y);
        }
        return door;
    }

    protected double nodeAreaOverlap(Graph graph) {
        double interSection = 0;
        for (int i = 0; i < graph.getNodeCount(); i++) {
            Node nodeA = graph.getNode(i);
            Point3 a = nodePointPosition(nodeA);
            double nodeAx1 = a.x - ((double) nodeA.getAttribute("width")) / 2f;
            double nodeAx2 = a.x + ((double) nodeA.getAttribute("width")) / 2f;
            double nodeAy1 = a.y + ((double) nodeA.getAttribute("height")) / 2f;
            double nodeAy2 = a.y - ((double) nodeA.getAttribute("height")) / 2f;
            //System.out.printf("%s: A.X1:%.2f; A.X2:%.2f; A.Y1:%.2f; A.Y2:%.2f; \n", nodeA.getId(),nodeAx1,nodeAx2,nodeAy1,nodeAy2);

            for (int j = i + 1; j < graph.getNodeCount(); j++) {
                Node nodeB = graph.getNode(j);
                Point3 b = nodePointPosition(nodeB);
                double nodeBx1 = b.x - ((double) nodeB.getAttribute("width")) / 2f;
                double nodeBx2 = b.x + ((double) nodeB.getAttribute("width")) / 2f;
                double nodeBy1 = b.y + ((double) nodeB.getAttribute("height")) / 2f;
                double nodeBy2 = b.y - ((double) nodeB.getAttribute("height")) / 2f;
                //System.out.printf("%s: B.X1:%.2f; B.X2:%.2f; B.Y1:%.2f; B.Y2:%.2f; \n", nodeB.getId(),nodeBx1,nodeBx2,nodeBy1,nodeBy2);

                if (nodeAx1 < nodeBx2 && nodeAx2 > nodeBx1
                        && nodeAy1 > nodeBy2 && nodeAy2 < nodeBy1) {
                    //System.out.println(nodeA.getId() + " and " + nodeB.getId());
                    double left = Math.max(nodeAx1, nodeBx1);
                    double right = Math.min(nodeAx2, nodeBx2);
                    double bottom = Math.max(nodeAy2, nodeBy2);
                    double top = Math.min(nodeAy1, nodeBy1);

                    interSection += (right - left) * (top - bottom);
                }
            }
        }
        return interSection;
    }

    protected double nodeCountPenalty(Graph graph) {
        double penalty = 0;
        if (graph.getNodeCount() < LevelConfig.minNodeCount) {
            int delta = LevelConfig.minNodeCount - graph.getNodeCount();
            penalty = delta * delta * LevelConfig.nodeCountPenalty;
        } else {
            if (graph.getNodeCount() > LevelConfig.maxNodeCount) {
                int delta = graph.getNodeCount() - LevelConfig.maxNodeCount;
                penalty = delta * delta * LevelConfig.nodeCountPenalty;
            }
        }
        return penalty;
    }

    protected double edgeIntersectionPenalty(Graph graph) {
        int eI = edgeIntersection(graph) + edgeNodeIntersection(graph);
        return eI * eI * LevelConfig.edgeIntersectionPenalty;
    }

    // http://graphstream-project.org/doc/Algorithms/Shortest-path/All-Pair-Shortest-Path/
    // this methos returns the average dinstance of all pair shortest path
    // DONT USE THIS! Use efficient version with A* bellow, which is like 4x faster
    public double averageShortestPath(Graph graph) {
        APSP apsp = new APSP();
        apsp.init(graph); // registering apsp as a sink for the graph
        apsp.setDirected(false); // undirected graph
        apsp.setWeightAttributeName("weight"); // ensure that the attribute name used is "weight", in this case the distance
        apsp.compute(); // the method that actually computes shortest paths

        double[] shortPath = new double[graph.getNodeCount()]; // aux vector
        double avgSP = 0; // average shortest path based on distances
        for (int i = 0; i < shortPath.length; i++) {
            shortPath[i] = 0;
            //System.out.println("Node "+graph.getNode(i).getId());
            for (int j = 0; j < shortPath.length; j++) {
                if (i != j) {
                    APSPInfo info = graph.getNode(i).getAttribute(APSPInfo.ATTRIBUTE_NAME); // retrieve already computed and stored info
                    Path path = info.getShortestPathTo(graph.getNode(j).getId());
                    for (Edge edge : path.getEachEdge()) {
                        shortPath[i] += GraphPosLengthUtils.edgeLength(edge);
                    }
                }
            }
            //System.out.println("Node "+graph.getNode(i).getId()+": "+shortPath[i]);
            avgSP += shortPath[i];
        }
        int N = graph.getNodeCount();
        avgSP /= N*(N-1);
        //System.out.println("AvgSP: "+avgSP);
        return avgSP;
    }
    
    // A* experiment of all-pair-shortest path
    /*public double aStarAVGShortestPath(Graph graph) {
        double avgSP = 0; // average shortest path based on distances
        double N = graph.getNodeCount();
        AStar astar = new AStar(graph);
        astar.setCosts(new DistanceCosts());
        for(int i = 0; i < N; i++){
            for(int j = 0; j < N; j++){
                if(i == j) continue;
                astar.compute(graph.getNode(i).getId(), graph.getNode(j).getId());
                Path path = astar.getShortestPath();
                for (Edge edge : path.getEachEdge()) {
                     avgSP += GraphPosLengthUtils.edgeLength(edge);
                }
            }
        }
        avgSP /= N*(N-1);
        return avgSP;
    }*/
    
    // A* experiment of all-pair-shortest path
    public double efficientAStarAVGShortestPath(Graph graph) {
        double avgSP = 0; // average shortest path based on distances
        double N = graph.getNodeCount();
        AStar astar = new AStar(graph);
        astar.setCosts(new DistanceCosts());
        int times = 0;
        for(int i = 0; i < N; i++){
            for(int j = i+1; j < N; j++){
                astar.compute(graph.getNode(i).getId(), graph.getNode(j).getId());
                Path path = astar.getShortestPath();
                for (Edge edge : path.getEachEdge()) {
                     avgSP += GraphPosLengthUtils.edgeLength(edge);
                }
                times++;
            }
        }
        avgSP = avgSP / times;
        return avgSP;
    }

    // https://www.topcoder.com/community/data-science/data-science-tutorials/geometry-concepts-line-intersection-and-its-applications/
    protected int edgeIntersection(Graph graph) {
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
                        int x = (int) ((B2 * C1 - B1 * C2) / det);
                        int y = (int) ((A1 * C2 - A2 * C1) / det);
                        if (Math.min(p1.x, p2.x) <= x && x <= Math.max(p1.x, p2.x)
                                && Math.min(p1.y, p2.y) <= y && y <= Math.max(p1.y, p2.y)
                                && Math.min(p3.x, p4.x) <= x && x <= Math.max(p3.x, p4.x)
                                && Math.min(p3.y, p4.y) <= y && y <= Math.max(p3.y, p4.y)) {
                            if (!intersections.contains(edgeB.getId() + "x" + edgeA.getId())
                                    && !intersections.contains(edgeA.getId() + "x" + edgeB.getId())) {
                                intersections.add(edgeA.getId() + "x" + edgeB.getId());
                                //System.err.println("Edge intersection: "+edgeA.getId()+" and "+edgeB.getId()+" at ("+x+","+y+")");
                            }
                        }
                    }
                }
            }
        }
        return intersections.size();
    }

    // check if the edges intersect with each other (non connected) node(room) wall
    protected int edgeNodeIntersection(Graph graph) {
        //ArrayList<String> intersections = new ArrayList<>();
        int intersect = 0;
        for (int i = 0; i < graph.getEdgeCount(); i++) {
            // the edge
            Edge edge = graph.getEdge(i);
            Node node1 = edge.getNode0();
            Node node2 = edge.getNode1();
            for (int j = 0; j < graph.getNodeCount(); j++) {
                // the room
                Node room = graph.getNode(j);
                // if the edge is connected with the room, we won't count it
                if (!room.getId().equals(node1.getId()) && !room.getId().equals(node2.getId())) {
                    double width = room.getAttribute("width");
                    double height = room.getAttribute("height");
                    Point3 c = nodePointPosition(room);// the central point of the room
                    Point3 p1 = new Point3((c.x - (width / 2.0f)), (c.y - (height / 2.0f))); // left-bot
                    Point3 p2 = new Point3((c.x - (width / 2.0f)), (c.y + (height / 2.0f))); // left-top
                    Point3 p3 = new Point3((c.x + (width / 2.0f)), (c.y + (height / 2.0f))); // right-top
                    Point3 p4 = new Point3((c.x + (width / 2.0f)), (c.y - (height / 2.0f))); // right-bot
                    
                    intersect += rectEdgeIntersection(edge, p1, p2);// left wall
                    intersect += rectEdgeIntersection(edge, p2, p3);// top wall
                    intersect += rectEdgeIntersection(edge, p3, p4);// right wall
                    intersect += rectEdgeIntersection(edge, p4, p1);// bottom wall
                }
            }
        }
        //for(String intersec : intersections){
        //    System.out.println(intersec);
        //}
        //return intersections.size();
        return intersect;
    }
    
    // creates a rotated rectangle for the edge with hall measures
    // https://math.stackexchange.com/questions/384186/calculate-new-positon-of-rectangle-corners-based-on-angle
    public int rectEdgeIntersection(Edge edge, Point3 p3, Point3 p4){
        Node node1 = edge.getNode0();
        Node node2 = edge.getNode1();
        Point3 p1 = nodePointPosition(node1);
        Point3 p2 = nodePointPosition(node2);
        Point3 p0 = new Point3((p1.x + p2.x) / 2.0f, (p1.y + p2.y) / 2.0f, (p1.z + p2.z) / 2.0f);
        double eW = p1.distance(p2);
        double eH = (double) GeneralConfig.edgeSize;
        //System.out.println("w: "+eW+" h: "+eH);
        Point3 e1 = new Point3(p0.x - eW/2.0f, p0.y - eH/2.0f, 0); // bot left
        Point3 e2 = new Point3(p0.x - eW/2.0f, p0.y + eH/2.0f, 0); // top left
        Point3 e3 = new Point3(p0.x + eW/2.0f, p0.y + eH/2.0f, 0); // top right
        Point3 e4 = new Point3(p0.x + eW/2.0f, p0.y - eH/2.0f, 0); // bot right
        /* //debug print
        System.out.println("bot-left  = "+e1.toString());
        System.out.println("top-left  = "+e2.toString());
        System.out.println("top-right = "+e3.toString());
        System.out.println("bot-right = "+e4.toString());
        System.out.println();*/
        // clockwise rotation
        double angle = -1*angleInRad(edge);
        //double angle = 0.0;
        //System.out.println("angle: "+angle+"rad, "+(-1*angleInDegrees(edge))+"º");
        Point3 re1 = new Point3((int)(p0.x + (e1.x - p0.x) * Math.cos(angle) + (e1.y - p0.y) * Math.sin(angle)),
                                (int)(p0.y - (e1.x - p0.x) * Math.sin(angle) + (e1.y - p0.y) * Math.cos(angle)));
        Point3 re2 = new Point3((int)(p0.x + (e2.x - p0.x) * Math.cos(angle) + (e2.y - p0.y) * Math.sin(angle)),
                                (int)(p0.y - (e2.x - p0.x) * Math.sin(angle) + (e2.y - p0.y) * Math.cos(angle)));
        Point3 re3 = new Point3((int)(p0.x + (e3.x - p0.x) * Math.cos(angle) + (e3.y - p0.y) * Math.sin(angle)),
                                (int)(p0.y - (e3.x - p0.x) * Math.sin(angle) + (e3.y - p0.y) * Math.cos(angle)));
        Point3 re4 = new Point3((int)(p0.x + (e4.x - p0.x) * Math.cos(angle) + (e4.y - p0.y) * Math.sin(angle)),
                                (int)(p0.y - (e4.x - p0.x) * Math.sin(angle) + (e4.y - p0.y) * Math.cos(angle)));
        /*// debug print
        System.out.println("bot-left  = "+re1.toString());
        System.out.println("top-left  = "+re2.toString());
        System.out.println("top-right = "+re3.toString());
        System.out.println("bot-right = "+re4.toString());
        */
        int result = twoLinesIntersection(re2, re3, p3, p4) + twoLinesIntersection(re4, re1, p3, p4);
        return result;
    }
    
    // returns true if the edge intersects with the line segment
    protected int twoPointsEdgeIntersection(Edge edge, Point3 p3, Point3 p4) {
        Node node1 = edge.getNode0();
        Node node2 = edge.getNode1();
        Point3 p1 = nodePointPosition(node1);
        Point3 p2 = nodePointPosition(node2);
        return twoLinesIntersection(p1, p2, p3, p4);
    }
    
    // returns true if the edge intersects with the line segment
    protected int twoLinesIntersection(Point3 p1, Point3 p2, Point3 p3, Point3 p4) {
        double A1 = p2.y - p1.y;
        double B1 = p1.x - p2.x;
        double C1 = A1 * p1.x + B1 * p1.y;

        double A2 = p4.y - p3.y;
        double B2 = p3.x - p4.x;
        double C2 = A2 * p3.x + B2 * p3.y;

        double det = A1 * B2 - A2 * B1;
        if (det == 0) {
            //Lines are parallel
        } else {
            int x = (int) ((B2 * C1 - B1 * C2) / det);
            int y = (int) ((A1 * C2 - A2 * C1) / det);
            if (   Math.min(p1.x, p2.x) <= x && x <= Math.max(p1.x, p2.x)
                && Math.min(p1.y, p2.y) <= y && y <= Math.max(p1.y, p2.y)
                && Math.min(p3.x, p4.x) <= x && x <= Math.max(p3.x, p4.x)
                && Math.min(p3.y, p4.y) <= y && y <= Math.max(p3.y, p4.y)) {
                return 1;
            }
        }
        return 0;
    }

    protected int distToSegment(int px, int py, int pz, int lx1, int ly1, int lz1, int lx2, int ly2, int lz2) {
        int line_dist = dist(lx1, ly1, lz1, lx2, ly2, lz2);
        if (line_dist == 0) {
            return dist(px, py, pz, lx1, ly1, lz1);
        }
        int t = ((px - lx1) * (lx2 - lx1) + (py - ly1) * (ly2 - ly1) + (pz - lz1) * (lz2 - lz1)) / line_dist;
        t = Math.max(0, Math.min(1, t));
        return dist(px, py, pz, lx1 + t * (lx2 - lx1), ly1 + t * (ly2 - ly1), lz1 + t * (lz2 - lz1));
    }

    private int dist(int x1, int y1, int z1, int x2, int y2, int z2) {
        return (int) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2));
    }

    private double dist(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2));
    }
}
