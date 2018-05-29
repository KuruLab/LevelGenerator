/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package extendedMetaZelda;

import graphstream.GraphStreamUtil;
import graphstream.Tree;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.JOptionPane;
import oldmapgenerator.Room;
import org.graphstream.algorithm.AStar;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.edgeLength;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.nodePosition;

/**
 *
 * @author andre
 */
public class DungeonGenerator {
    
    private Random random;
    private Graph graph;
    private int seed;
    private Hashtable<String, Integer> childPlaceMap; 
    
    private boolean bossRoomLocked;

    public DungeonGenerator(Graph graph) {
        //System.out.println("Initializing Dungeon Generator");
        this.graph = graph;
        String seedString = "";
        for(int i = 0; i < graph.getNodeCount(); i++){
            double[] point = nodePosition(graph.getNode(i));
            seedString += Arrays.toString(point)+"\n";
        }
        this.seed = seedString.hashCode();
        //System.out.println("Seed: "+seed);
        this.random = new Random(seed);
        this.bossRoomLocked = true;
    }
    
    public DungeonGenerator(Graph graph, int seed) {
        //System.out.println("Initializing Dungeon Generator");
        this.graph = graph;
        this.seed = seed;
        this.random = new Random(seed);
        this.bossRoomLocked = true;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public boolean isBossRoomLocked() {
        return bossRoomLocked;
    }
    
    public void setBossRoomLocked(boolean bossRoomLocked) {
        this.bossRoomLocked = bossRoomLocked;
    }   

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }
    
    public void generate(){
        generate(false);
    }
    
    public void generate(boolean debug) {
        if(debug)System.out.println("\nGenerating");
        
        int attempt = 0;

        while (true) {
            try {
                DungeonConfig.defaultMaxKeys = Math.min(4, (int) (graph.getNodeCount()/ 5.0) + random.nextInt(3));
                (new DungeonUtil()).initializeGraphSymbols(graph);
                (new DungeonUtil()).initializeGraphConditions(graph);
                (new DungeonUtil()).initializeGraphIntensity(graph);

                // Initialize Start, Boss, Spanning Tree and other elements:
                initStartAndBoss(graph);
                Tree<Node> tree = null;
                //if(random.nextDouble() > -1){ // if (true)
                    tree = (new GraphStreamUtil()).minimumSpanningTree(graph);
                    if(debug)System.out.println("Base Optimal Spanning Tree Built.\n"+tree.toString());
                //}
                //suspicious behaviour
                /*else{
                    tree = (new GraphStreamUtil()).randomSpanningTree(graph, random);
                    if(debug)System.out.println("Base Random Spanning Tree Built.\n"+tree.toString());
                }*/
                
                KeyLevelRoomMapping levels;
                childPlaceMap = new Hashtable<>();
                
                int roomsPerLock;
                if (DungeonConfig.defaultMaxKeys > 0) {
                    roomsPerLock = graph.getNodeCount() / DungeonConfig.defaultMaxKeys;
                } else {
                    roomsPerLock = graph.getNodeCount();
                }
                if(debug)System.out.println("Rooms per lock defined as: "+roomsPerLock+" Max Keys: "+DungeonConfig.defaultMaxKeys);
                while (true) {
                    //dungeon = new Dungeon();
                    // Maps keyLevel -> Rooms that were created when lockCount had that
                    // value
                    
                    levels = new KeyLevelRoomMapping();
                    // Create the entrance to the dungeon:
                    if(debug)System.out.print("Setting entrance room...");
                    initEntranceRoom(levels);
                    if(debug)System.out.println(" Done.");
                    try {
                        // Fill the dungeon with rooms:
                        if(debug)System.out.println("Setting other rooms...");
                        placeRooms(tree, levels, roomsPerLock);
                        if(debug)System.out.println("Done.");
                        break;
                    } catch (OutOfRoomsException e) {
                        // We can run out of rooms where certain links have
                        // predetermined locks. Example: if a river bisects the
                        // map, the keyLevel for rooms in the river > 0 because
                        // crossing water requires a key. If there are not
                        // enough rooms before the river to build up to the
                        // key for the river, we've run out of rooms.
                        if(debug)System.out.println("Ran out of rooms. roomsPerLock was "+roomsPerLock);
                        roomsPerLock = roomsPerLock * DungeonConfig.defaultMaxKeys /
                                (DungeonConfig.defaultMaxKeys + 1);
                        if(debug)System.out.println("roomsPerLock is now "+roomsPerLock);

                        if (roomsPerLock == 0) {
                            if(debug) System.out.println(
                                    "Failed to place rooms. Have you forgotten to disable boss-locking?");
                            // If the boss room is locked, the final key is used
                            // only for the boss room. So if the final key is
                            // also used to cross the river, rooms cannot be
                            // placed.
                            break;
                        }
                    }
                }
                if(debug)System.out.print("Placing boss door...");
                fixBossKey(levels);
                if(debug)System.out.println(" Done.");
                
                // Place switches and the locks that require it:
                //System.out.println("Placing switches...");
                //placeSwitches(tree);
                //System.out.println("Switches placement done.");
                
                if(debug)System.out.print("Computing dungeon rooms intensity...");
                myComputeIntensity(tree, levels);
                if(debug)System.out.println(" Done.");
                
                // Place the keys within the dungeon:
                if(debug)System.out.print("Placing keys...");
                placeKeys(levels);
                if(debug)System.out.println(" Done.");
                
                if (levels.keyCount() != DungeonConfig.defaultMaxKeys){
                    if(debug)System.out.println(levels.keyCount()+" != "+DungeonConfig.defaultMaxKeys);
                    throw new RetryException();
                }
                
                // Make the dungeon less tree-like:
                if(debug)System.out.print("Fixing other egdes...");
                graphify();
                if(debug)System.out.println(" Done.");
                
                if(debug)System.out.print("Checking Dungeon...");
                checkAcceptable();
                if(debug)System.out.println(" Accepted!");
                break;
            } catch (RetryException e) {
                if(debug)System.out.println("Dungeon generator failed.");
                if (++attempt > DungeonConfig.defaultMaxTries) {
                    seed = random.nextInt();
                    random = new Random(seed);
                    if(debug)System.out.println("Trying new seed: "+seed);
                    if (++attempt > DungeonConfig.defaultMaxTries*1.5){
                        //System.err.println("Dungeon generator failed.");
                        return;
                    }
                }
                if(debug)System.out.println("Going to attempt: "+attempt);
            }
        }
        if(debug)System.out.println("Dungeon generation is over.");
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
    
    public void initStartAndBoss(Graph graph){
        //System.out.println("Setting Start and Boss...");
        AStar astar = new AStar(graph);
 	astar.setCosts(new AStar.DistanceCosts());
        Node from = null;
        Node to = null;
        double bestCost = -1;
        for(Node nodeFrom : graph.getEachNode()){
            for(Node nodeTo : graph.getEachNode()){
                astar.compute(nodeFrom.getId(), nodeTo.getId());
                double cost = 0;
                try {
                    for (Edge edge : astar.getShortestPath().getEdgePath()) {
                        cost += (double) edgeLength(edge);
                    }
                    if (cost > bestCost) {
                        from = nodeFrom;
                        to = nodeTo;
                        bestCost = cost;
                    }
                } catch (java.lang.NullPointerException ex) {
                    JOptionPane.showMessageDialog(null, "It was not possible to find a valid path to a node.\nPlease, fix the graph edges before 'Apply'.", "Error!", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }
        from.setAttribute("symbol", new Symbol(Symbol.START));
        from.setAttribute("ui.class", "start");
        to.setAttribute("symbol", new Symbol(Symbol.BOSS));
        to.setAttribute("ui.class", "boss");
        //System.out.println("Node "+from.getId()+" is start.");
        //System.out.println("Node "+to.getId()+" is boss.");
    }
    
    public void fixBossKey(KeyLevelRoomMapping levels){
        Node boss = (new DungeonUtil()).findBoss(graph);
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
    
    /**
     * Computes the 'intensity' of each {@link Room}. Rooms generally get more
     * intense the deeper they are into the dungeon.
     *
     * @param levels    the keyLevel -> room-set mapping to update
     * @throws RetryException if it fails
     * @see KeyLevelRoomMapping
     * @see Room
     */
    protected void computeIntensity(Tree<Node> tree, KeyLevelRoomMapping levels)
            throws RetryException {
        DungeonUtil util = new DungeonUtil();
        double nextLevelBaseIntensity = 0.0;
        for (int level = 0; level < levels.keyCount(); ++level) {

            double intensity = nextLevelBaseIntensity * (1.0 - INTENSITY_EASE_OFF);

            for (Node room: levels.getRooms(level)) {
                Tree<Node> subtree = tree.getTree(room);
                
                if (subtree.getParent() == null){
                    nextLevelBaseIntensity = Math.max(nextLevelBaseIntensity, applyIntensity(subtree, room, intensity));
                }
                else{
                    Condition parentCondition = subtree.getParent().getHead().getAttribute("condition");
                    Condition roomCondition = room.getAttribute("condition");
                    if(parentCondition.implies(roomCondition)){
                        nextLevelBaseIntensity = Math.max(nextLevelBaseIntensity, applyIntensity(subtree, room, intensity));
                    }
                }    
            }
        }
        util.normalizeIntensity(graph);
        
        Node start = util.findStart(graph);
        start.setAttribute("ui.color", 0.0);
        Node boss = util.findBoss(graph);
        boss.setAttribute("ui.color", 1.0);
        
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
    protected void myComputeIntensity(Tree<Node> tree, KeyLevelRoomMapping levels)
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
        
        Node start = util.findStart(graph);
        start.setAttribute("ui.color", 0.0);
        Node boss = util.findBoss(graph);
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
     * Maps 'keyLevel' to the set of rooms within that keyLevel.
     * <p>
     * A 'keyLevel' is the count of the number of unique keys are needed for all
     * the locks we've placed. For example, all the rooms in keyLevel 0 are
     * accessible without collecting any keys, while to get to rooms in
     * keyLevel 3, the player must have collected at least 3 keys.
     */
    protected class KeyLevelRoomMapping {
        protected List<List<Node>> map = new ArrayList<List<Node>>();

        java.util.List<Node> getRooms(int keyLevel) {
            while (keyLevel >= map.size()) map.add(null);
            if (map.get(keyLevel) == null)
                map.set(keyLevel, new ArrayList<Node>());
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
    protected static final Comparator<Node>
    EDGE_COUNT_COMPARATOR = new Comparator<Node>() {
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
     * Sets up the dungeon's entrance room.
     *
     * @param levels    the keyLevel -> room-set mapping to update
     * @see KeyLevelRoomMapping
     */
    protected void initEntranceRoom(KeyLevelRoomMapping levels)
            throws RetryException {
        Node start = (new DungeonUtil()).findStart(graph);
        start.setAttribute("condition", new Condition(new Symbol(Symbol.NOTHING)));
        levels.addRoom(0, start);
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
    protected boolean shouldAddNewLock(int keyLevel, int numRooms, int targetRoomsPerLock) {
        int usableKeys = DungeonConfig.defaultMaxKeys;
        if (isBossRoomLocked())
            usableKeys -= 1;
        return numRooms >= targetRoomsPerLock && keyLevel < usableKeys;
    }
    
    /**
     * Fill the dungeon's space with rooms and doors (some locked).
     * Keys are not inserted at this point.
     *
     * @param levels    the keyLevel -> room-set mapping to update
     * @throws RetryException if it fails
     * @see KeyLevelRoomMapping
     */
    protected void placeRooms(Tree<Node> tree, KeyLevelRoomMapping levels, int roomsPerLock)
            throws RetryException, OutOfRoomsException {

        // keyLevel: the number of keys required to get to the new room
        int keyLevel = 0;
        Symbol latestKey = null;
        // condition that must hold true for the player to reach the new room
        // (the set of keys they must have).
        Condition cond = new Condition();
        
        ArrayList<Node> mapped = new ArrayList<>(levels.getRooms(keyLevel));
        while(mapped.size() < graph.getNodeCount()){
            
            boolean doLock = false;
            
            // Decide whether we need to place a new lock
            // (Don't place the last lock, since that's reserved for the boss)
            if (shouldAddNewLock(keyLevel, levels.getRooms(keyLevel).size(), roomsPerLock)) {
                latestKey = new Symbol(keyLevel++);
                cond = cond.and(new Symbol(keyLevel));
                doLock = true;
                //System.out.println("Adding new keyLevel: "+keyLevel+". Cond: "+cond.toString());
            }
            
            // Find an existing room with a free edge:
            Node parentRoom = null;
            int rand = random.nextInt(10);
            //System.out.println("\nRandom: "+rand);
            if (!doLock &&  rand > 0)
                parentRoom = chooseRoomWithFreeEdge(tree, levels.getRooms(keyLevel), keyLevel);
            if (parentRoom == null) {
                parentRoom = chooseRoomWithFreeEdge(tree, mapped, keyLevel);
                doLock = true;
            }

            if (parentRoom == null)
                throw new OutOfRoomsException();
            
            // Decide which direction to put the new room in relative to the
            // parent
            List<Node> children = (List<Node>) tree.getSuccessors(parentRoom);
            //System.out.println("Node "+parentRoom.getId()+" has "+children.size()+" children");
            Node nextRoom = null;
            for(Node next : children){
                if(!mapped.contains(next)){
                    mapped.add(next);
                    nextRoom = next;
                    nextRoom.setAttribute("condition", cond);
                    break;
                }
            }
            
            if (nextRoom == null)
                throw new OutOfRoomsException();
            
            if(doLock){
                Edge edge = parentRoom.getEdgeBetween(nextRoom);
                edge.setAttribute("symbol", new Symbol(keyLevel));
                //System.out.println("Edge "+edge.toString()+" locked! Key: "+new Symbol(keyLevel).toString());
            }
            levels.addRoom(keyLevel, nextRoom);
        }
    }
    
    /**
     * Places keys within the dungeon in such a way that the dungeon is
     * guaranteed to be solvable.
     *
     * @param levels    the keyLevel -> room-set mapping to use
     * @throws RetryException if it fails
     * @see KeyLevelRoomMapping
     */
    protected void placeKeys(KeyLevelRoomMapping levels) throws RetryException {
        // Now place the keys. For every key-level but the last one, place a
        // key for the next level in it, preferring rooms with fewest links
        // (dead end rooms).
        for (int key = 0; key < levels.keyCount()-1; ++key) {
            List<Node> rooms = levels.getRooms(key);

            Collections.shuffle(rooms, random);
            // Collections.sort is stable: it doesn't reorder "equal" elements,
            // which means the shuffling we just did is still useful.
            Collections.sort(rooms, INTENSITY_COMPARATOR);
            // Alternatively, use the EDGE_COUNT_COMPARATOR to put keys at
            // 'dead end' rooms.

            Symbol keySym = new Symbol(key+1);

            boolean placedKey = false;
            for (Node room: rooms) {
                Symbol symbol = room.getAttribute("symbol");
                if (symbol == null || symbol.isNothinig()) {
                    room.setAttribute("symbol", keySym);
                    placedKey = true;
                    //System.out.println(room.getId()+" ("+room.getIndex()+"): "+keySym);
                    break;
                }
            }
            if (!placedKey)
                // there were no rooms into which the key would fit
                throw new RetryException();
        }
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
    
    /**
     * Makes some {@link Edge}s within the dungeon require the dungeon's switch
     * to be in a particular state, and places the switch in a room in the
     * dungeon.
     *
     * @throws RetryException if it fails
     */
    protected void placeSwitches(Tree<Node> tree) throws RetryException {
        // Possible TODO: have multiple switches on separate circuits
        // At the moment, we only have one switch per dungeon.
        if (DungeonConfig.defaultMaxSwitches <= 0) return;

        List<Node> solution = getSolutionPath(tree);

        for (int attempt = 0; attempt < 10; ++attempt) {

            List<Node> rooms = new ArrayList<Node>(graph.getNodeSet());
            Collections.shuffle(rooms, random);
            Collections.shuffle(solution, random);

            // Pick a base room from the solution path so that the player
            // will have to encounter a switch-lock to solve the dungeon.
            Node baseRoom = null;
            for (Node room: solution) {
                Tree<Node> subtree = tree.getTree(room);
                if (subtree.getSuccessors(room).size() > 1 && subtree.getParent() != null) {
                    baseRoom = room;
                    break;
                }
            }
            if (baseRoom == null) throw new RetryException();
            Condition baseRoomCond = baseRoom.getAttribute("condition");

            removeDescendantsFromList(rooms, tree, baseRoom);

            Symbol switchSym = new Symbol(Symbol.SWITCH);

            Node switchRoom = null;
            for (Node room: rooms) {
                Symbol symbol = room.getAttribute("symbol");
                Condition condition = room.getAttribute("condition");
                if ((symbol == null || symbol.isNothinig()) &&
                        baseRoomCond.implies(condition)
                        //constraints.roomCanFitItem(room.id, switchSym))
                        ){
                    switchRoom = room;
                    break;
                }
            }
            if (switchRoom == null) continue;

            if (switchLockChildRooms(tree, baseRoom, Condition.SwitchState.EITHER)) {
                switchRoom.setAttribute("symbol", switchSym);
                return;
            }
        }
        throw new RetryException();
    }
    
    /**
     * Returns a path from the goal to the dungeon entrance, along the 'parent'
     * relations.
     *
     * @return  a list of linked {@link Room}s starting with the goal room and
     *          ending with the start room.
     */
    protected List<Node> getSolutionPath(Tree<Node> tree) {
        List<Node> solution = new ArrayList<Node>();
        Node boss = (new DungeonUtil()).findBoss(graph);
        Tree<Node> subtree = tree.getTree(boss);
        while (subtree.getParent() != null) {
            solution.add(subtree.getHead());
            subtree = subtree.getParent();
        }
        return solution;
    }
    
    /**
     * Removes the given {@link Room} and all its descendants from the given
     * list.
     *
     * @param rooms the list of Rooms to remove nodes from
     * @param room  the Room whose descendants to remove from the list
     */
    protected void removeDescendantsFromList(List<Node> rooms, Tree<Node> tree, Node room) {
        //System.out.println("Removing room "+room.getId()+" from list");
        rooms.remove(room);
        Tree<Node> subtree = tree.getTree(room);
        for (Node child: subtree.getSuccessors(room)) {
            removeDescendantsFromList(rooms, subtree, child);
        }
    }
    
    /**
     * Adds extra conditions to the given {@link Room}'s preconditions and all
     * of its descendants.
     *
     * @param room  the Room to add extra preconditions to
     * @param cond  the extra preconditions to add
     */
    protected void addPrecond(Tree<Node> tree, Node room, Condition cond) {
        //System.out.println("Adding precondition "+cond.toString()+" to room "+room.getId());
        Condition nodeContition = room.getAttribute("condition");
        nodeContition = nodeContition.and(cond);
        room.setAttribute("condition", nodeContition);
        
        Tree<Node> subtree = tree.getTree(room);
        for (Node child: subtree.getSuccessors(room)) {
            addPrecond(subtree, child, cond);
        }
    }
    
    /**
     * Randomly locks descendant rooms of the given {@link Room} with
     * {@link Edge}s that require the switch to be in the given state.
     * <p>
     * If the given state is EITHER, the required states will be random.
     *
     * @param room          the room whose child to lock
     * @param givenState    the state to require the switch to be in for the
     *                      child rooms to be accessible
     * @return              true if any locks were added, false if none were
     *                      added (which can happen due to the way the random
     *                      decisions are made)
     * @see Condition.SwitchState
     */
    protected boolean switchLockChildRooms(Tree<Node> tree, Node room,
            Condition.SwitchState givenState) {
        boolean anyLocks = false;
        Condition.SwitchState state = givenState != Condition.SwitchState.EITHER
                ? givenState
                : (random.nextInt(2) == 0
                    ? Condition.SwitchState.ON
                    : Condition.SwitchState.OFF);

        for (Edge edge: room.getEachEdge()) {
            
            Node nextRoom = edge.getOpposite(room);
            if (tree.getSuccessors(room).contains(nextRoom)) {
                Symbol edgeSymbol = edge.getAttribute("symbol");
                if (random.nextInt(4) != 0) {
                    if(edgeSymbol == null)
                        edge.addAttribute("symbol", state.toSymbol());
                    else if(edgeSymbol.isNothinig())
                        edge.setAttribute("symbol", state.toSymbol());
                    addPrecond(tree, nextRoom, new Condition(state.toSymbol()));
                    anyLocks = true;
                } else {
                    anyLocks |= switchLockChildRooms(tree, nextRoom, state);
                }

                if (givenState == Condition.SwitchState.EITHER) {
                    state = state.invert();
                }
            }
        }
        return anyLocks;
    }
    
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
    
}
