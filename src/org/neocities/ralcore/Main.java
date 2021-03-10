package org.neocities.ralcore;

// OpenCSV
import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvException;

// Google's SimpleJSON
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

// Processing (plus some dependencies)
import processing.core.PApplet;
import processing.sound.*;

// General Java
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.stream.*;
import java.util.*;
import static java.lang.System.nanoTime;

public class Main extends PApplet {

    // Storing current game instance
    gameInstance currentGameInstance = null;
    // The current gamestate, used as the master control for the entire game.
    String gameState = "menu";

    class gameInstance {
        // Defining the scroll speed, changing how fast notes cross the screen.
        static final int scrollSpeed = 4;
        // Height offset for the rows.
        static final int rowOffsetH = 100;
        // Note square height. Also defines the gap between rows, to make it 0.
        static final int rowHeight = 50;
        // Millisecond leniency for a note to get missed...
        static final int leniencyMiss = 250;
        // ...and for a 40...
        static final int leniency40 = 175;
        // ...and for an 80.
        static final int leniency80 = 100;
        // The BPM of the chart, which will be defined in the chart's metadata.
        float bpm = 120;
        // The offset of the chart, which is also part of the metadata.
        float offset = 0;
        // The relative current of the chart, calculated to be 0 when a chart begins.
        float relativeTime = 10000000;
        // The relative start time of the chart, used in calculations for the above time
        float relativeStartTime = 10000000;
        // Current score
        int score = 0;
        // Current health
        int health = 100;
        // Creating the array that will hold the ArrayList for each row
        ArrayList<Main.Note>[] rows = new ArrayList[3];
        // The SoundFile entity that contains the audio for the current chart
        SoundFile chartAudio = null;

        public void startPlay() {
            // Setting up the array of ArrayLists that will store row data
            for (int i = 0; i < 3; i++) {
                rows[i] = new ArrayList<Note>();
            }
            // Importing chart info using simple-json, yonked from here: https://www.tutorialspoint.com/how-to-read-the-contents-of-a-json-file-using-java
            try {
                // Creating JSONParser object and setting up file
                FileReader chartJSON = new FileReader("Charts/1 HALLEY LABS R&D - catching up (first wip)/metadata.json");
                JSONParser jsonParser = new JSONParser();
                // Parsing JSON contents
                JSONObject jsonContents = (JSONObject) jsonParser.parse(chartJSON);
                // TODO: make this read title and artist and that kinda funky thing; for now we just read the BPM
                bpm = ((Double) jsonContents.get("bpm")).floatValue();
                offset = ((Double) jsonContents.get("offset")).floatValue();
                chartJSON.close();
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
            // Import chart from file using OpenCSV
            // Setting up the list to hold the chart temporarily:
            List<String[]> importedChart = new ArrayList<String[]>();
            try {
                // Creating file and csv reader objects
                FileReader chartFile = new FileReader("Charts/1 HALLEY LABS R&D - catching up (first wip)/normal.csv");
                CSVReaderHeaderAware csv = new CSVReaderHeaderAware(chartFile);
                // Reading in file
                importedChart = csv.readAll();
                // TODO: potentially rework so that this reads line-by-line for efficiency
                // TODO: rework so that bpm changes per chart
                // Iterating through imported CSV, using data to create note objects in rows.
                for (int i = 0; i < importedChart.size(); i++) {
                    rows[Integer.parseInt(importedChart.get(i)[2])].add(new Note(Float.parseFloat(importedChart.get(i)[0]) * (60000 / bpm), (int) importedChart.get(i)[1].charAt(0), Integer.parseInt(importedChart.get(i)[2])));
                }
                csv.close();
                chartFile.close();
            } catch (IOException | CsvException e) {
                e.printStackTrace();
            }
            // Begin playing audio from the chart file
            playAudio("Charts/1 HALLEY LABS R&D - catching up (first wip)/audio.mp3");
            relativeStartTime = (nanoTime() / 1000000) + offset;
            try (Stream<Path> paths = Files.walk(Paths.get("Charts"))) {
                paths.forEach(System.out::println);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Time to begin! Signalling to the rest of the game control to begin play.
            gameState = "playing";
        }

        public void gameTick() {
            relativeTime += (nanoTime() / 1000000) - relativeStartTime - relativeTime;
            background(80);
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < rows[i].size(); j++) {
                    rows[i].get(j).noteTick();
                }
            }
        }

        public void checkNote() {
            // Checking which row the user is hitting on
            // If the user is hitting neither up or down, or both up or down, we want to hit centre row.
            int currentRow;
            if (!((keys.getOrDefault(UP, false) == true) ^ (keys.getOrDefault(DOWN, false) == true))) {
                currentRow = 1;
            } else if (keys.getOrDefault(UP, false) == true) {
                currentRow = 0;
            } else {
                currentRow = 2;
            }
            if (relativeTime > rows[currentRow].get(0).time - leniencyMiss && relativeTime < rows[currentRow].get(0).time + leniencyMiss) {
                // If the hit time is not outside the miss leniency, then we know the note is a hit.
                if (keyCode != rows[currentRow].get(0).key || relativeTime < rows[currentRow].get(0).time - leniency40 || relativeTime > rows[currentRow].get(0).time + leniency40) {
                    // If the wrong key is hit, or the note is hit outside the 40 leniency, it's a miss.
                    print("Miss!");
                    health -= 5;
                } else if (relativeTime < rows[currentRow].get(0).time - leniency80 || relativeTime > rows[currentRow].get(0).time + leniency80) {
                    // If the note is outside the 80 leniency, it's a 40.
                    print("40!");
                    score += 40;
                    health += 1;
                } else {
                    //Otherwise, it's an 80.
                    print("80!");
                    score += 80;
                    health += 2;
                }
                rows[currentRow].remove(0);
                if (health > 100) {
                    health = 100;
                } else if (health <= 0) {
                    // TODO: end game code
                    gameState = "dead";
                }
            }
        }
    }

    public class Note {
        float time;
        int key;
        int row;
        public void noteTick(){
            // Assuming for now that we know exactly what time the note should be hit on to not calculate it, and that the song starts when the program starts.
            // TODO: Not do those things.
            textSize(32);
            textAlign(CENTER, TOP);
            if (currentGameInstance.relativeTime - currentGameInstance.leniencyMiss > time) {
                System.out.println(currentGameInstance.relativeTime);
                System.out.println(time);
                System.out.println(currentGameInstance.leniencyMiss);

                // If the note is deemed old, destroy it.
                currentGameInstance.rows[row].remove(0);
            } else {
                // Drawing note
                // A - x-coord: position based on when note should be hit, multiplied by an arbitrary scalar of the scrollspeed, minus half the note width.
                // B - y-coord: the height of the note plus the row number times how high each note is.
                fill(255, 255, 255);
                rect((time - currentGameInstance.relativeTime) * currentGameInstance.scrollSpeed / 10, currentGameInstance.rowOffsetH+(row-1)*currentGameInstance.rowHeight, 50, 50);
                fill(0, 0, 0);
                text((char)key, (time - currentGameInstance.relativeTime) * currentGameInstance.scrollSpeed / 10 + 25, currentGameInstance.rowOffsetH+(row-1)*currentGameInstance.rowHeight);
            }
        }
        public Note(Float time, Integer key, Integer row) {
            this.time = time;
            this.key = key;
            this.row = row;
        }
    }

    public void playAudio(String path) {
        currentGameInstance.chartAudio = new SoundFile(this, path);
        currentGameInstance.chartAudio.play(1, (float) 0.5);
    }

    public static void main(String[] args) {
        String[] processingArgs = new String[]{"MySketch"};
        Main mySketch = new Main();
        PApplet.runSketch(processingArgs, mySketch);
    }

    public void settings() {
        size(400, 400);
        smooth();
    }

    public void draw() {
        if (millis() > 100 && gameState != "playing") {
            currentGameInstance = new gameInstance();
            currentGameInstance.startPlay();
        }
        if (gameState == "playing") {
            currentGameInstance.gameTick();
        }
        if (!currentGameInstance.chartAudio.isPlaying()) {
            gameState = "finished";
        }
        if (gameState == "finished") {
            System.out.println("hello!");
        }
    }

    // This project uses a modified version of jeremydouglass' solution for checking when multiple keys are pressed or held at once.
    // It can be found here: https://discourse.processing.org/t/solution-for-repeating-keypressed-while-holding-key/3989/3

    // Within this HashMap, the key is the character, and the value is it's state.
    HashMap<Integer, Boolean> keys = new HashMap<Integer, Boolean>();

    // Sets the value in the HashMap to either:
    // false - Not Held
    // true - Held
    // These properties are used to a) prevent holding buttons from hitting notes and b) check UP/DOWN inputs to change rows

    public void keyPressed() {
        // Only set to true if the key is not blocked (preventing repeating keys without releasing them)
        if (!keys.getOrDefault(keyCode, false) && keyCode != UP && keyCode != DOWN) {
            currentGameInstance.checkNote();
        }
        keys.put(keyCode, true);
    }

    public void keyReleased() {
        keys.put(keyCode, false);
    }

}