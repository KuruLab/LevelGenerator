/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evoPuzzle;

import evoGraph.Decoder;
import evoGraph.GraphIndividual;
import extendedMetaZelda.Condition;
import extendedMetaZelda.DungeonConfig;
import extendedMetaZelda.DungeonUtil;
import extendedMetaZelda.Symbol;
import graphstream.GraphStreamUtil;
import graphstream.Tree;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.graphstream.algorithm.AStar;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

/**
 *
 * @author andre
 */
public class PuzzleDecoder {

    private Random random;
    private Graph graph;
    //private int seed;
    private Hashtable<String, Integer> childPlaceMap;
    private ArrayList<Node> allMappedNodes;

    private boolean bossRoomLocked;

    public PuzzleDecoder(GraphIndividual original) {
        Decoder connectDecoder = new Decoder(original);
        GraphIndividual connectedIndividual = connectDecoder.barabasiAlbertGraph();
        Decoder graphDecoder = new Decoder(connectedIndividual);
        this.graph = graphDecoder.decode();
        
        /*String seedString = "";
        for (int i = 0; i < graph.getNodeCount(); i++) {
            double[] point = nodePosition(graph.getNode(i));
            seedString += Arrays.toString(point) + "\n";
        }
        this.seed = seedString.hashCode();*/
        //System.out.println("Seed: "+seed);
        this.random = new Random(System.nanoTime());
        this.bossRoomLocked = true;
    }

    public PuzzleDecoder(GraphIndividual original, int seed) {
        Decoder connectDecoder = new Decoder(original);
        GraphIndividual connectedIndividual = connectDecoder.barabasiAlbertGraph();
        Decoder graphDecoder = new Decoder(connectedIndividual);
        this.graph = graphDecoder.decode();
        
        //this.seed = seed;
        this.random = new Random(seed);
        this.bossRoomLocked = true;
    }

    /**
     * Maps 'keyLevel' to the set of rooms within that keyLevel.
     * <p>
     * A 'keyLevel' is the count of the number of unique keys are needed for all
     * the locks we've placed. For example, all the rooms in keyLevel 0 are
     * accessible without collecting any keys, while to get to rooms in keyLevel
     * 3, the player must have collected at least 3 keys.
     */
    protected class KeyLevelRoomMapping {

        protected List<List<Node>> map = new ArrayList<List<Node>>();

        java.util.List<Node> getRooms(int keyLevel) {
            while (keyLevel >= map.size()) {
                map.add(null);
            }
            if (map.get(keyLevel) == null) {
                map.set(keyLevel, new ArrayList<Node>());
            }
            return map.get(keyLevel);
        }

        void addRoom(int keyLevel, Node room) {
            getRooms(keyLevel).add(room);
            //System.out.println("Mapping: added keyLevel "+keyLevel+" to room "+room.getId());
        }

        int keyCount() {
            return map.size();
        }
    }

    /**
     * Comparator objects for sorting {@link Room}s in a couple of different
     * ways. These are used to determine in which rooms of a given keyLevel it
     * is best to place the next key.
     *
     * @see #placeKeys
     */
    protected static final Comparator<Node> EDGE_COUNT_COMPARATOR = new Comparator<Node>() {
        @Override
        public int compare(Node arg0, Node arg1) {
            return arg0.getDegree() - arg1.getDegree();
        }
    },
    INTENSITY_COMPARATOR = new Comparator<Node>() {
        @Override
        public int compare(Node arg0, Node arg1) {
            double intensity0 = arg0.getAttribute("ui.color");
            double intensity1 = arg1.getAttribute("ui.color");
            return intensity0 > intensity1 ? -1
                    : intensity0 < intensity1 ? 1
                            : 0;
        }
    };
    
    /**
     * Thrown by several IDungeonGenerator methods that can fail.
     * Should be caught and handled in {@link #generate}.
     */
    protected static class RetryException extends Exception {
        private static final long serialVersionUID = 1L;
    }
    
    protected static class OutOfRoomsException extends Exception {
        private static final long serialVersionUID = 1L;
    }
    
    public PuzzleIndividual encode(Graph g){
        PuzzleIndividual puzzle = new PuzzleIndividual();
        ArrayList<PuzzleGene> chromosome = new ArrayList<>();
        PuzzleGene[] chromoArray = new PuzzleGene[100];
        for(int i = 0; i < chromoArray.length; i++)
            chromoArray[i] = null;
        for(Node node : g.getEachNode()){
            Symbol symbol = node.getAttribute("symbol");
            Condition condition = node.getAttribute("condition");
            if(symbol.isStart() || symbol.isBoss() || symbol.isKey()){
                //System.out.println("Node "+node.getIndex()+" is "+symbol);
                PuzzleGene gene = new PuzzleGene(node.getIndex());
                ArrayList<Symbol> symbols = new ArrayList<>();
                ArrayList<Condition> conditions = new ArrayList<>();
                symbols.add(symbol);
                conditions.add(condition);
                gene.setSymbols(symbols);
                gene.setConditions(conditions);
                if(symbol.isStart()){
                    chromoArray[0] = gene;
                }
                else if(symbol.isBoss()){
                    chromoArray[1] = gene;
                }
                else if(symbol.isKey()){
                    chromoArray[symbol.getValue()+1] = gene;
                }
            }
        }
        for(int i = 0; i < chromoArray.length; i++){
            if(chromoArray[i] == null)
                break;
            else
                chromosome.add(chromoArray[i]);
        }
        puzzle.setNodes(chromosome);
        return puzzle;
    }

    public Graph decode(PuzzleIndividual puzzleInd, boolean debug) {
        if (debug) {
            System.out.println("\nGenerating");
        }
        // initialize variables
        //int attempt = 0;
        DungeonConfig.defaultMaxKeys = (int) ((graph.getNodeCount() / 5.0) - 1);
        (new DungeonUtil()).initializeGraphSymbols(graph);
        (new DungeonUtil()).initializeGraphConditions(graph);
        (new DungeonUtil()).initializeGraphIntensity(graph);

        // Initialize Start, Boss, Spanning Tree and other elements:
        initStartAndBoss(puzzleInd);
        Tree<Node> tree = null;
        tree = (new GraphStreamUtil()).minimumSpanningTree(graph);
        if (debug) {
            System.out.println("Base Optimal Spanning Tree Built.\n" + tree.toString());
        }
        //suspicious behaviour: why random trees fail?
        /*tree = (new GraphStreamUtil()).randomSpanningTree(graph, random);
        if(debug)System.out.println("Base Random Spanning Tree Built.\n"+tree.toString());*/
        
        KeyLevelRoomMapping levels;
        allMappedNodes = new ArrayList<>();
        childPlaceMap = new Hashtable<>();

        int roomsPerLock;
        if (DungeonConfig.defaultMaxKeys > 0) {
            roomsPerLock = graph.getNodeCount() / DungeonConfig.defaultMaxKeys;
        } else {
            roomsPerLock = graph.getNodeCount();
        }
        if (debug) {
            System.out.println("Rooms per lock defined as: " + roomsPerLock +
                               " Max Keys: " + DungeonConfig.defaultMaxKeys);
        }
        
        try {
            // Maps keyLevel -> Rooms that were mapped per key level 
            levels = new KeyLevelRoomMapping();
            // Create the entrance to the dungeon:
            if(debug)System.out.print("Setting entrance, keys and boss rooms...");
            initRooms(levels, puzzleInd);
            if(debug)System.out.println(" Done.");

            // Fill the dungeon with rooms:
            if(debug)System.out.println("Setting other rooms...");
            placeRooms(tree, levels, roomsPerLock, puzzleInd);
            if(debug)System.out.println("Done.");

            // Place required symbols on edges (i still don't know why not use conditions):
            if(debug)System.out.print("Fixing other egdes...");
            graphify();
            if(debug)System.out.println(" Done.");

            if(debug)System.out.print("Placing boss doors...");
            fixBossKey(levels, puzzleInd);
            if(debug)System.out.println(" Done.");

            if(debug)System.out.print("Computing dungeon rooms intensity...");
            myComputeIntensity(puzzleInd, tree, levels);
            if(debug)System.out.println(" Done.");

            if(debug)System.out.print("Checking Dungeon...");
            checkAcceptable(); // actually, does nothing yet
            if(debug)System.out.println(" Accepted!");
        } catch (RetryException e) {

        }
        if(debug) System.out.println("Dungeon generation is over.");
        
        return graph;
    }

    private void initStartAndBoss(PuzzleIndividual puzzleInd) {
        int startID = puzzleInd.getStart().getNodeID();
        int bossID = puzzleInd.getBoss().getNodeID();
        Node start = graph.getNode(startID);
        Node boss = graph.getNode(bossID);
        start.setAttribute("symbol", new Symbol(Symbol.START));
        start.setAttribute("ui.class", "start");
        boss.setAttribute("symbol", new Symbol(Symbol.BOSS));
        boss.setAttribute("ui.class", "boss");
    }
    
    /**
     * Sets up the dungeon's entrance room.
     *
     * @param levels    the keyLevel -> room-set mapping to update
     * @see KeyLevelRoomMapping
     */
    protected void initRooms(KeyLevelRoomMapping levels, PuzzleIndividual individual){
        //System.out.println();
        Node start = graph.getNode(individual.getStart().getNodeID());
        start.setAttribute("condition", new Condition(new Symbol(Symbol.NOTHING)));
        levels.addRoom(0, start);
        allMappedNodes.add(start);
        int keyValue = 0;
        for(int i = 2; i < individual.getNodes().size(); i++, keyValue++){
            Node key = graph.getNode(individual.getNodes().get(i).getNodeID());
            
            Condition condition = new Condition(new Symbol(keyValue));
            Symbol symbol = new Symbol(keyValue+1);
            
            key.setAttribute("condition", condition);
            key.setAttribute("symbol", symbol);
            
            levels.addRoom(keyValue, key);
            //System.out.println("new key at "+key.getIndex()+" S:"+symbol.toString()+" C:"+condition.toString());
            
            allMappedNodes.add(key);
        }
        Node boss = graph.getNode(individual.getBoss().getNodeID());
        boss.setAttribute("condition", new Condition(new Symbol(keyValue)));
        levels.addRoom(keyValue, boss);
        allMappedNodes.add(boss);
    }
    
    /**
     * Fill the dungeon's space with rooms and doors (some locked).
     * Keys are not inserted at this point.
     *
     * @param levels    the keyLevel -> room-set mapping to update
     * @throws RetryException if it fails
     * @see KeyLevelRoomMapping
     */
    protected void placeRooms(Tree<Node> tree, KeyLevelRoomMapping levels, int roomsPerLock, PuzzleIndividual puzzleIndi) {
        // keyLevel: the number of keys required to get to the new room
        int keyLevel = 0;
        
        ArrayList<Node> targets = new ArrayList<>();
        Node start = graph.getNode(puzzleIndi.getStart().getNodeID());
        Node boss = graph.getNode(puzzleIndi.getBoss().getNodeID());
        targets.add(0, start);
        for(int i = 2; i < puzzleIndi.getNodes().size(); i++){
            targets.add(graph.getNode(puzzleIndi.getNodes().get(i).getNodeID()));
        }
        targets.add(boss);
        
        Node current = targets.remove(0);
        while(!targets.isEmpty()){
            Condition condition = new Condition(new Symbol(keyLevel));
            // first, we try to ensure a valid path to the target
            Node next = targets.get(0);
            PuzzleDistanceCost pdc = new PuzzleDistanceCost(keyLevel);
            AStar astar = new AStar(graph);
            astar.setCosts(pdc);
            astar.compute(current.getId(), next.getId());
            Path path = astar.getShortestPath();
            //System.out.println("Path from "+current.getIndex()+" to "+next.getIndex());
            for (Node step : path.getNodePath()) {
                if(!allMappedNodes.contains(step)){
                    step.setAttribute("symbol", new Symbol(Symbol.NOTHING));
                    step.setAttribute("condition", condition);
                    levels.addRoom(keyLevel, step);
                    allMappedNodes.add(step);
                }
                //System.out.println(step.getIndex()+" S:"+step.getAttribute("symbol").toString()+" C:"+step.getAttribute("condition").toString());
            }
            // second, we try to fill the neighbor nodes
            while(shouldMapNewRooms(keyLevel, levels.getRooms(keyLevel).size(), roomsPerLock)){
                //System.out.println("We should try to add more rooms to the "+keyLevel+" keylevel ("+levels.getRooms(keyLevel).size()+"/"+roomsPerLock+")");
                // Find an existing room with a free edge:
                Node parentRoom = null;
                parentRoom = chooseRoomWithFreeEdge(tree, levels.getRooms(keyLevel), keyLevel);
                if (parentRoom == null) {
                    parentRoom = chooseRoomWithFreeEdge(tree, allMappedNodes, keyLevel);
                }
                if (parentRoom == null) {
                    break;
                }
                List<Node> children = (List<Node>) tree.getSuccessors(parentRoom);
                //System.out.println("Node "+parentRoom.getId()+" has "+children.size()+" children");
                Node nextRoom = null;
                for(Node nextChildren : children){
                    if(!levels.getRooms(keyLevel).contains(nextChildren) && !allMappedNodes.contains(nextChildren)){
                        levels.getRooms(keyLevel).add(nextChildren);
                        nextRoom = nextChildren;
                        nextRoom.setAttribute("condition", condition);
                        allMappedNodes.add(nextRoom);
                        //System.out.println("New mapping: "+nextRoom.getIndex()+" S:"+nextRoom.getAttribute("symbol").toString()+" C:"+nextRoom.getAttribute("condition").toString());
                        break;
                    }
                }
            }
            current = targets.remove(0);
            keyLevel++;
        }
    }
    
    /**
     * Decides whether to add a new lock (and keyLevel) at this point.
     *
     * @param keyLevel the number of distinct locks that have been placed into
     *      the map so far
     * @param numRooms the number of rooms at the current keyLevel
     * @param targetRoomsPerLock the number of rooms the generator has chosen
     *      as the target number of rooms to place at each keyLevel (which
     *      subclasses can ignore, if desired).
     */
    protected boolean shouldMapNewRooms(int keyLevel, int numRooms, int targetRoomsPerLock) {
        int usableKeys = DungeonConfig.defaultMaxKeys;
        if (isBossRoomLocked())
            usableKeys -= 1;
        return !(numRooms >= targetRoomsPerLock && keyLevel < usableKeys);
    }
    /**
     * Randomly chooses a {@link Room} within the given collection that has at
     * least one adjacent empty space.
     *
     * @param roomCollection    the collection of rooms to choose from
     * @return  the room that was chosen, or null if there are no rooms with
     *          adjacent empty spaces
     */
    protected Node chooseRoomWithFreeEdge(Tree<Node> tree, Collection<Node> roomCollection, int keyLevel) {
        //System.out.println("Choosing a new free room from a set of "+roomCollection.size()+" others");
        List<Node> rooms = new ArrayList<Node>(roomCollection);
        /*for(Node room : rooms){
            if(childPlaceMap.getOrDefault(room.getId(), 0) == room.getDegree()){
                rooms.remove(room);
                System.out.println("Removed "+room.getId()+" from the free room list.");
            }
        }*/
        
        Collections.shuffle(rooms, random);
        for (int i = 0; i < rooms.size(); ++i) {
            Node room = rooms.get(i);
            Tree<Node> subtree = tree.getTree(room);
            if(subtree.getSuccessors(room).size() > 0){
                if(childPlaceMap.containsKey(room.getId())){
                    if(childPlaceMap.get(room.getId()) < subtree.getSuccessors(room).size()){
                        int used = childPlaceMap.get(room.getId()) + 1;
                        childPlaceMap.replace(room.getId(), used);
                        //System.out.println("-> Selected "+room.getId()+" as the new room. ("+used+")");
                        return room;
                    }
                }
                else{
                    childPlaceMap.put(room.getId(), 1);
                    //System.out.println("-> Selected "+room.getId()+" as the new room. (first time)");
                    return room;
                }
            }
        }
        //System.out.println("-> There is no free room!");
        return null;
    }
    
    public void fixBossKey(KeyLevelRoomMapping levels, PuzzleIndividual individual){
        Node boss = graph.getNode(individual.getBoss().getNodeID());
        Condition bossCondition = boss.getAttribute("condition");
        int oldKeyLevel = bossCondition.getKeyLevel(),
            newKeyLevel = Math.min(levels.keyCount(), DungeonConfig.defaultMaxKeys);
        //System.out.println("oldKeyLevel: "+oldKeyLevel+" newKeyLevel: "+newKeyLevel);
        if (oldKeyLevel != newKeyLevel) {
            List<Node> oklRooms = levels.getRooms(oldKeyLevel);
            
            oklRooms.remove(boss);

            levels.addRoom(newKeyLevel, boss);

            Symbol bossKey = new Symbol(newKeyLevel+1);
            Condition precond = bossCondition.and(bossKey);
            boss.setAttribute("condition", precond);
            //System.out.println("New boss condition: "+precond.toString());
            if (newKeyLevel != 0) {
                for(Edge edge : boss.getEachEdge())
                    edge.addAttribute("symbol", bossKey);
            }
        }
    }
    
    public void graphify(){
        //System.out.println("\nGraphify");
        for(Edge edge : graph.getEachEdge()){
            Node from = edge.getSourceNode();
            Node to = edge.getTargetNode();
            //System.out.println("\nEdge "+edge.toString());
            //Symbol fromSymbol = from.getAttribute("symbol");
            //Symbol toSymbol = to.getAttribute("symbol");
            Condition fromCondition = from.getAttribute("condition");
            Condition toCondition = to.getAttribute("condition");
            //System.out.println("From cond "+fromCondition.toString()+" -> to cond "+toCondition.toString());
            //if (fromSymbol.isGoal() || fromSymbol.isBoss() || toSymbol.is) continue;
            boolean forwardImplies = fromCondition.implies(toCondition),
                    backwardImplies = toCondition.implies(fromCondition);
            
            if (forwardImplies && backwardImplies) {
                // we do not have random edges, cause they are already built
                // so we are just doing nothing here, for now
            } else {
                //System.out.println("foward "+forwardImplies+" -> backward "+backwardImplies);
                Symbol difference = fromCondition.singleSymbolDifference(toCondition);
                if (difference == null /*|| (!difference.isSwitchState())*/)
                    continue;
                //System.out.println("Edge "+edge.toString()+" -> diff "+difference.toString());
                edge.setAttribute("symbol", difference);
            }
        }    
    }
    
    /**
     * Computes the 'intensity' of each {@link Room}. Rooms generally get more
     * intense the deeper they are into the dungeon.
     *
     * @param levels    the keyLevel -> room-set mapping to update
     * @throws RetryException if it fails
     * @see KeyLevelRoomMapping
     * @see Room
     */
    protected void myComputeIntensity(PuzzleIndividual individual, Tree<Node> tree, KeyLevelRoomMapping levels)
            throws RetryException {
        DungeonUtil util = new DungeonUtil();
        double nextLevelBaseIntensity = 0.0;
        
        boolean[] visited = new boolean[graph.getNodeCount()];
        for(int i = 0; i < visited.length; i++)
            visited[i] = false;
       
        for (int level = 0; level < levels.keyCount(); ++level) {

            double intensity = nextLevelBaseIntensity * (1.0 - INTENSITY_EASE_OFF);

            for (Node room: levels.getRooms(level)) {           
                if(tree.getTree(room).getParent() == null)
                    nextLevelBaseIntensity = Math.max(nextLevelBaseIntensity, myApplyIntensity(visited, room, intensity));
                else{
                    Condition parentCondition = tree.getTree(room).getParent().getHead().getAttribute("condition");
                    Condition roomCondition = room.getAttribute("condition");
                    if (!(parentCondition.implies(roomCondition))){
                        nextLevelBaseIntensity = Math.max(nextLevelBaseIntensity, myApplyIntensity(visited, room, intensity));
                    }
                }
            }
        }

        util.normalizeIntensity(graph);
        
        Node start = graph.getNode(individual.getStart().getNodeID());
        start.setAttribute("ui.color", 0.0);
        Node boss = graph.getNode(individual.getBoss().getNodeID());
        boss.setAttribute("ui.color", 1.0);
        
    }
    
    protected static final double
            INTENSITY_GROWTH_JITTER = 0.1,
            INTENSITY_EASE_OFF = 0.2;

    /**
     * Recursively applies the given intensity to the given {@link Room}, and
     * higher intensities to each of its descendants that are within the same
     * keyLevel.
     * <p>
     * Intensities set by this method may (will) be outside of the normal range
     * from 0.0 to 1.0. See {@link #normalizeIntensity} to correct this.
     *
     * @param room      the room to set the intensity of
     * @param intensity the value to set intensity to (some randomn variance is
     *                  added)
     * @see Room
     */
    protected double applyIntensity(Tree<Node> tree, Node room, double intensity) {
        intensity *= 1.0 - INTENSITY_GROWTH_JITTER/2.0 + INTENSITY_GROWTH_JITTER * random.nextDouble();

        room.setAttribute("ui.color", intensity);
        Condition roomCondition = room.getAttribute("condition");
        double maxIntensity = intensity;
        List<Node> subtree = (List<Node>) tree.getSuccessors(room);
        for(Node child : subtree){
            Condition childCondition = child.getAttribute("condition");
            if (roomCondition.implies(childCondition)) {
                maxIntensity = Math.max(maxIntensity, applyIntensity(tree.getTree(child), child, intensity + 1.0));
            }
        }

        return maxIntensity;
    }
    
    /**
     * Recursively applies the given intensity to the given {@link Room}, and
     * higher intensities to each of its descendants that are within the same
     * keyLevel.
     * <p>
     * Intensities set by this method may (will) be outside of the normal range
     * from 0.0 to 1.0. See {@link #normalizeIntensity} to correct this.
     *
     * @param room      the room to set the intensity of
     * @param intensity the value to set intensity to (some randomn variance is
     *                  added)
     * @see Room
     */
    protected double myApplyIntensity(boolean[] visited, Node room, double intensity) {
        intensity *= 1.0 - INTENSITY_GROWTH_JITTER/2.0 + INTENSITY_GROWTH_JITTER * random.nextDouble();
        
        visited[room.getIndex()] = true;
        
        room.setAttribute("ui.color", intensity);
        Condition roomCondition = room.getAttribute("condition");
        double maxIntensity = intensity;
        Iterator<Node> iterator = room.getNeighborNodeIterator();
        while(iterator.hasNext()){
            Node neighbour = iterator.next();
            if(!visited[neighbour.getIndex()]){
                Condition childCondition = neighbour.getAttribute("condition");
                if (roomCondition.implies(childCondition)) {
                    maxIntensity = Math.max(maxIntensity, myApplyIntensity(visited, neighbour, intensity + 1.0));
                }
            }
        }
        return maxIntensity;
    }
    
    /**
     * Checks with the
     * {@link net.bytten.metazelda.constraints.IDungeonConstraints} that the
     * dungeon is OK to use.
     *
     * @throws RetryException if the IDungeonConstraints decided generation must
     *                        be re-attempted
     * @see net.bytten.metazelda.constraints.IDungeonConstraints
     */
    protected void checkAcceptable() throws RetryException {
        // nothing to do
        return;
    }
    
    public boolean isBossRoomLocked() {
        return bossRoomLocked;
    }
    
    public void setBossRoomLocked(boolean bossRoomLocked) {
        this.bossRoomLocked = bossRoomLocked;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }
 
}
