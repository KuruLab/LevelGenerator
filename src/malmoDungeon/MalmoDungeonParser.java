/*
 * Copyright 2018 andre.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package malmoDungeon;

import evoGraph.EvoJSONFileReader;
import extendedMetaZelda.DungeonUtil;
import extendedMetaZelda.Symbol;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphPosLengthUtils;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.nodePointPosition;

/**
 *
 * @author andre
 */
public class MalmoDungeonParser {
    
    public static final int static_y = 4;
    public static final int static_height = 10;
    public static final int doorSize = 3;
    public static final String wall_type = "stone";
    public static final String key_type = "chest";
    
    public List<Point3> doors;
    public HashMap<Point3, Point3> connection;
    public HashMap<Integer, List<Point3>> northConnections;
    public HashMap<Integer, List<Point3>> southConnections;
    public HashMap<Integer, List<Point3>> eastConnections;
    public HashMap<Integer, List<Point3>> westConnections;
    
    public void generateXMLDungeon(String jsonPath){
        
        Graph graph = loadJsonFile(jsonPath);
        String xml = parseDungeon(graph);
        System.out.println(xml);
        
        File file = new File("dungeon_test.xml");
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(xml);
            fw.flush();
        } catch (IOException ex) {
            Logger.getLogger(MalmoDungeonParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String parseDungeon(Graph graph){
        String xmlString =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n"+
            "<Mission xmlns=\"http://ProjectMalmo.microsoft.com\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"+
            "\n"+
            "\t<About>\n"+
            "\t\t<Summary>Dungeon!</Summary>\n"+
            "\t</About>\n"+
            "\n"+  
            "\t<ServerSection>\n"+
            "\t\t<ServerInitialConditions>\n"+
            "\t\t\t<Time>\n"+
            "\t\t\t\t<StartTime>12000</StartTime>\n"+
            "\t\t\t\t<AllowPassageOfTime>false</AllowPassageOfTime>\n"+
            "\t\t\t</Time>\n"+
            "\t\t\t<Weather>clear</Weather>\n"+
            "\t\t</ServerInitialConditions>\n"+
            "\t\t<ServerHandlers>\n"+
            //"\t\t\t<FlatWorldGenerator generatorString=\"3;7,44*49,73,35:1,159:4,95:13,35:13,159:11,95:10,159:14,159:6,35:6,95:6;12;\"/>\n"+
            "\t\t\t<FlatWorldGenerator/>\n"+
            "\t\t\t<DrawingDecorator>\n"+
            drawDungeon(graph)+
            "\t\t\t</DrawingDecorator>\n"+
            "\t\t\t<ServerQuitFromTimeUp timeLimitMs=\"1000\"/>\n"+
            "\t\t\t<ServerQuitWhenAnyAgentFinishes/>\n"+
            "\t\t</ServerHandlers>\n"+
            "\t</ServerSection>\n"+
            "\n"+  
            "\t<AgentSection mode=\"Survival\">\n"+
            "\t\t<Name>Malminion</Name>\n"+
            "\t\t<AgentStart>\n"+
            "\t\t\t<Placement "+agentStart(graph)+" yaw=\"0\"/>\n"+
            "\t\t\t<Inventory>\n"+
            "\t\t\t\t<InventoryItem slot=\"0\" type=\"torch\"/>\n"+
            "\t\t\t</Inventory>\n"+
            "\t\t</AgentStart>\n"+
            "\t\t<AgentHandlers>\n"+
            "\t\t\t<ObservationFromFullStats/>\n"+
            "\t\t\t<ContinuousMovementCommands turnSpeedDegs=\"180\">\n"+
            "\t\t\t\t<ModifierList type=\"deny-list\">\n"+
            "\t\t\t\t\t<command>attack</command>\n"+
            "\t\t\t\t</ModifierList>\n"+
            "\t\t\t</ContinuousMovementCommands>\n"+
            "\t\t</AgentHandlers>\n"+
            "\t</AgentSection>\n"+
            "</Mission>";
        return xmlString;
    }
    public String drawCuboid(int x1, int y1, int z1, int x2, int y2, int z2, String type){
        return "<DrawCuboid x1=\"" + x1 + "\" y1=\"" + y1 + "\" z1=\"" + z1 + "\" x2=\"" + x2 + "\" y2=\"" + y2 + "\" z2=\"" + z2 + "\" type=\"" + type + "\"/>";
    }
    
    public String drawLine(int x1, int y1, int z1, int x2, int y2, int z2, String type){
        return "<DrawLine x1=\"" + x1 + "\" y1=\"" + y1 + "\" z1=\"" + z1 + "\" x2=\"" + x2 + "\" y2=\"" + y2 + "\" z2=\"" + z2 + "\" type=\"" + type + "\"/>";
    }
    
    public String drawBlock(int x, int y, int z, String type){
        return "<DrawBlock x=\"" + x + "\" y=\"" + y + "\" z=\"" + z + "\" type=\"" + type + "\"/>\n";
    }
    
    public String drawSphere(int x, int y, int z, int radius, String type){
        return "<DrawSphere x=\"" + x + "\" y=\"" + y + "\" z=\"" + z +" radius=\""+radius+"\" type=\"" + type + "\"/>";
    }
    
    public String drawItem(int x, int y, int z, String type){
        return "<DrawItem x=\"" + x + "\" y=\"" + y + "\" z=\"" + z + "\" type=\"" + type + "\"/>";
    }
    
    public Graph loadJsonFile(String filename){
        EvoJSONFileReader reader = new EvoJSONFileReader(filename);
        Graph graph = reader.parseJson();
        return graph;
    }

    private String drawDungeon(Graph graph) {
        String result = "";
        for (Node node : graph.getEachNode()) {
            result += drawNode(node);
        }
        
        initializeDoors(graph);

        for (Point3 door : connection.keySet()) {
            result += drawEdge(door);
        }
        return result;
    }

    // very close to Evaluation's edgeDistance
    public void initializeDoors(Graph graph) {
        doors = new ArrayList<>();
        connection = new HashMap<>();
        northConnections = new HashMap<>();
        southConnections = new HashMap<>();
        eastConnections = new HashMap<>();
        westConnections = new HashMap<>();
        
        // We first, create the door list
        for (Edge edge : graph.getEachEdge()) {
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
                updateConnection(northConnections, nodeA, doorA);
                
                // line equation of the B's south wall
                double A3 = bottomLeftB.y - bottomRightB.y;
                double B3 = bottomRightB.x - bottomLeftB.x;
                double C3 = A3 * bottomRightB.x + B3 * bottomRightB.y;
                doorB = calculateIntersectionPoint(A1, B1, C1, A3, B3, C3);
                updateConnection(southConnections, nodeB, doorB);
            } //A's south to B's north
            else if ((pB.y < pA.y) && (deltaY >= deltaX)) {
                // line equation of the A's south wall
                double A2 = bottomRightA.y - bottomLeftA.y;
                double B2 = bottomLeftA.x - bottomRightA.x;
                double C2 = A2 * bottomLeftA.x + B2 * bottomLeftA.y;
                doorA = calculateIntersectionPoint(A1, B1, C1, A2, B2, C2);
                updateConnection(southConnections, nodeA, doorA);
                
                // line equation of the B's north wall
                double A3 = topRightB.y - topLeftB.y;
                double B3 = topLeftB.x - topRightB.x;
                double C3 = A3 * topLeftB.x + B3 * topLeftB.y;
                doorB = calculateIntersectionPoint(A1, B1, C1, A3, B3, C3);
                updateConnection(northConnections, nodeB, doorB);
            } //A's east to B's west
            else if ((pB.x > pA.x) && (deltaX >= deltaY)) {
                // line equation of the right wall
                double A2 = bottomRightA.y - topRightA.y;
                double B2 = topRightA.x - bottomRightA.x;
                double C2 = A2 * topRightA.x + B2 * topRightA.y;
                doorA = calculateIntersectionPoint(A1, B1, C1, A2, B2, C2);
                updateConnection(eastConnections, nodeA, doorA);
                
                // line equation of the left wall
                double A3 = topLeftB.y - bottomLeftB.y;
                double B3 = bottomLeftB.x - topLeftB.x;
                double C3 = A3 * bottomLeftB.x + B3 * bottomLeftB.y;
                doorB = calculateIntersectionPoint(A1, B1, C1, A3, B3, C3);
                updateConnection(westConnections, nodeB, doorB);
            } //A's west to B's east
            else if ((pB.x < pA.x) && (deltaX >= deltaY)) {
                // line equation of the left wall
                double A2 = topLeftA.y - bottomLeftA.y;
                double B2 = bottomLeftA.x - topLeftA.x;
                double C2 = A2 * bottomLeftA.x + B2 * bottomLeftA.y;
                doorA = calculateIntersectionPoint(A1, B1, C1, A2, B2, C2);
                updateConnection(westConnections, nodeA, doorA);
                
                // line equation of the right wall
                double A3 = bottomRightB.y - topRightB.y;
                double B3 = topRightB.x - bottomRightB.x;
                double C3 = A3 * topRightB.x + B3 * topRightB.y;
                doorB = calculateIntersectionPoint(A1, B1, C1, A3, B3, C3);
                updateConnection(eastConnections, nodeB, doorB);
            }
            doors.add(doorA);
            doors.add(doorB);
            connection.put(doorA, doorB);
        }
        // Then, we verify and adjust the position of the doors (under development)
        /*for(Node room : graph.getEachNode()){
            int id = room.getIndex();
            Point3 center = nodePointPosition(room);
            int width = (int) (double) room.getAttribute("width");
            int height = (int) (double) room.getAttribute("height");
            //North
            if(northConnections.containsKey(id)){
                List<Point3> northDoors = northConnections.get(id);
                
                int min = (int) (center.x - (width / 2) + (doorSize / 2) + 1);
                int max = (int) (center.x + (width / 2) - (doorSize / 2) - 1);
                // fix the position of the first door if necessary
                if (northDoors.get(0).x < min)
                    northDoors.get(0).x = min;
                else if (northDoors.get(0).x > max)
                    northDoors.get(0).x = max;
                // then, check for others
                for (int i = 1; i < northDoors.size(); i++){
                    Point3 door = northDoors.get(i);
                     // first, also check bounds
                    if (door.x < min)
                        door.x = min;
                    else if (door.x > max)
                        door.x = max;
                    // then check for door collision
                    Point3 previous = northDoors.get(i - 1);
                    if(door.x < (previous.x + doorSize)){
                        door.x = previous.x + doorSize;
                        // if the adjustment exceeds the bound, then we merge the doors
                        //code removed: this condition very unlikely to happen and would require several changes
                    }
                }
            }
            //South
            //East
            //West
        }*/
    }
    
    public void updateConnection(HashMap<Integer, List<Point3>> map, Node node, Point3 door){
        int id = node.getIndex();
        if(map.containsKey(id)){
            map.get(id).add(door);
        }
        else{
            List<Point3> list = new ArrayList<>();
            list.add(door);
            map.put(id, list);
        }
    }
    
    // copy and past from Evaluation's
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
    
    private String drawNode(Node node){
        String result = "";
        
        Point3 center = GraphPosLengthUtils.nodePointPosition(node);
        int width = (int) (double) node.getAttribute("width");
        int height = (int) (double) node.getAttribute("height");
        
        int x1 = (int) (center.x - (width/2.0f));
        int y1 = (int) center.z + static_y;
        int z1 = (int) (center.y - (height/2.0f));
        
        int x2 = (int) (center.x + (width/2.0f));
        int y2 = (int) y1 + static_height;
        int z2 = (int) (center.y + (height/2.0f));
        
        result += "\t\t\t\t"+drawCuboid(x1, y1, z1, x2, y2, z2, wall_type)+"\n";         // solid box
        result += "\t\t\t\t"+drawCuboid(x1+1, y1+1, z1+1, x2-1, y2-1, z2-1, "air")+"\n"; // air box
        
        Symbol symbol = node.getAttribute("symbol");
        if(symbol != null){
            if(symbol.isKey()){
                result += "\t\t\t\t"+drawBlock((int) center.x, (int) center.z + static_y + 1, (int) center.y, key_type)+"\n";
                
                result += "\t\t\t\t"+drawBlock((int) center.x-1, (int) center.z + static_y + 1, (int) center.y-1, "torch")+"\n";
                result += "\t\t\t\t"+drawBlock((int) center.x-1, (int) center.z + static_y + 1, (int) center.y+1, "torch")+"\n";
                result += "\t\t\t\t"+drawBlock((int) center.x+1, (int) center.z + static_y + 1, (int) center.y-1, "torch")+"\n";
                result += "\t\t\t\t"+drawBlock((int) center.x+1, (int) center.z + static_y + 1, (int) center.y+1, "torch")+"\n";
            }
        }
        
        return result;
    }
    
    private String drawEdge(Point3 from){
        String result = "";
        Point3 to = connection.get(from);
        
        int x1 = (int) (from.x);
        int y1 = (int) from.z + static_y;
        int z1 = (int) (from.y);
        
        int x2 = (int) (to.x);
        int y2 = (int) to.z + static_y;
        int z2 = (int) (to.y);
        
        result += "\t\t\t\t"+drawLine(x1, y1, z1, x2, y2, z2, wall_type)+"\n";
        result += "\t\t\t\t"+drawCuboid(x1-1, y1+1, z1-1, x1+1, y1+2, z1+1, "air")+"\n"; // door from
        result += "\t\t\t\t"+drawCuboid(x2-1, y2+1, z2-1, x2+1, y2+2, z2+1, "air")+"\n"; // door to
        //result += "\t\t\t\t"+drawBlock(x1, y1+3, z1, "torch")+"\n";
        //result += "\t\t\t\t"+drawBlock(x2, y2+3, z2, "torch")+"\n";
        
        return result;
    }

    private String agentStart(Graph graph) {
        Node startNode = (new DungeonUtil().findStart(graph));
        Point3 start = GraphPosLengthUtils.nodePointPosition(startNode);
        int y = (int) (start.z + 5 + static_y);
        return "x=\""+start.x+"\" y=\""+y+"\" z=\""+start.y+"\""; // remember [x,y,z] coordinate system
    }
    
}
