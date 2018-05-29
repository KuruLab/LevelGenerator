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

/**
 *
 * @author andre
 */
import com.microsoft.msr.malmo.MissionSpec;
import com.microsoft.msr.malmo.AgentHost;
import com.microsoft.msr.malmo.ClientInfo;
import com.microsoft.msr.malmo.ClientPool;
import com.microsoft.msr.malmo.MissionException;
import com.microsoft.msr.malmo.MissionRecordSpec;
import com.microsoft.msr.malmo.MissionSpec;
import com.microsoft.msr.malmo.StringVector;
import com.microsoft.msr.malmo.TimestampedReward;
import com.microsoft.msr.malmo.TimestampedString;
import com.microsoft.msr.malmo.WorldState;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DungeonMalmo {
    
    public static final String jsonPath = "D:\\Mega\\posdoc\\MapGenerator\\experiments\\only_asp_and_din\\1618875503\\map_1618875503.json";
    
    public static MissionSpec loadMissionXML(String filename) {
        MissionSpec mission = null;
        try {
            String xml = new String(Files.readAllBytes(Paths.get(filename)));
            mission = new MissionSpec(xml, true);
        } catch (Exception e) {
            //e.printStackTrace();
            throw new RuntimeException(e);
        }

        return mission;
    }

    static {
        System.loadLibrary("MalmoJava"); // attempts to load MalmoJava.dll (on Windows) or libMalmoJava.so (on Linux)
    }

    public static void main(String argv[]) {
        AgentHost agent_host = new AgentHost();
        try {
            StringVector args = new StringVector();
            args.add("JavaExamples_run_mission");
            for (String arg : argv) {
                args.add(arg);
            }
            agent_host.parse(args);
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            System.err.println(agent_host.getUsage());
            System.exit(1);
        }
        if (agent_host.receivedArgument("help")) {
            System.out.println(agent_host.getUsage());
            System.exit(0);
        }
        
        MalmoDungeonParser parser = new MalmoDungeonParser();
        parser.generateXMLDungeon(jsonPath); // read the json and generate the dungeon_test.xml file
        
        MissionSpec my_mission = loadMissionXML("dungeon_test.xml");

        MissionRecordSpec my_mission_record = new MissionRecordSpec("./saved_data.tgz");
        my_mission_record.recordCommands();
        my_mission_record.recordMP4(20, 400000);
        my_mission_record.recordRewards();
        my_mission_record.recordObservations();

        try {
            agent_host.startMission(my_mission, my_mission_record);
        } catch (MissionException e) {
            System.err.println("Error starting mission: " + e.getMessage());
            System.err.println("Error code: " + e.getMissionErrorCode());
            // We can use the code to do specific error handling, eg:
            if (e.getMissionErrorCode() == MissionException.MissionErrorCode.MISSION_INSUFFICIENT_CLIENTS_AVAILABLE) {
                // Caused by lack of available Minecraft clients.
                System.err.println("Is there a Minecraft client running?");
            }
            System.exit(1);
        }

        WorldState world_state;

        System.out.print("Waiting for the mission to start");
        do {
            System.out.print(".");
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                System.err.println("User interrupted while waiting for mission to start.");
                return;
            }
            world_state = agent_host.getWorldState();
            for (int i = 0; i < world_state.getErrors().size(); i++) {
                System.err.println("Error: " + world_state.getErrors().get(i).getText());
            }
        } while (!world_state.getIsMissionRunning());
        System.out.println("");

        // main loop:
        do {
            world_state = agent_host.getWorldState();
            System.out.print("video,observations,rewards received: ");
            System.out.print(world_state.getNumberOfVideoFramesSinceLastState() + ",");
            System.out.print(world_state.getNumberOfObservationsSinceLastState() + ",");
            System.out.println(world_state.getNumberOfRewardsSinceLastState());
            for (int i = 0; i < world_state.getRewards().size(); i++) {
                TimestampedReward reward = world_state.getRewards().get(i);
                System.out.println("Summed reward: " + reward.getValue());
            }
            for (int i = 0; i < world_state.getErrors().size(); i++) {
                TimestampedString error = world_state.getErrors().get(i);
                System.err.println("Error: " + error.getText());
            }
        } while (world_state.getIsMissionRunning());

        System.out.println("Mission has stopped.");
    }
}
