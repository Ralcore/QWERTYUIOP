package org.neocities.ralcore;

import processing.core.PApplet;
import java.util.HashMap;

public class Main extends PApplet {

    // Defining the scroll speed, changing how fast notes cross the screen.
    int scrollSpeed = 1;
    // Height offset for the rows.
    int rowOffsetH = 100;
    // Note square height. Also defines the gap between rows, to make it 0.
    int rowHeight = 50;
    // Millisecond leniency for a note to get missed...
    int leniencyMiss = 500;
    // ...and for a 40...
    int leniency40 = 250;
    // ...and for an 80.
    int leniency80 = 150;

    public class Note {
        int time;
        int key;
        public void noteTick(){
            // Assuming for now that we know exactly what time the note should be hit on to not calculate it, and that the song starts when the program starts.
            // TODO: Not do those things.
            // Drawing note
            rect((time-millis())*scrollSpeed/10, rowOffsetH, 10, 50);
        }
        public Note(Integer defTime, Integer defKey) {
            time = defTime;
            key = defKey;
        }
    }

    public static void main(String[] args) {
        String[] processingArgs = new String[]{"MySketch"};
        Main mySketch = new Main();
        PApplet.runSketch(processingArgs, mySketch);
    }

    public void settings() {
        size(400, 400);
        smooth();
        myNote = new Note(2000, (int) 'A');
        myNote2 = new Note(2250, (int) 'A');
    }

    Note myNote, myNote2;

    public void draw() {
        background(80);
        if (myNote != null) {
            myNote.noteTick();
        }
        if (myNote2 != null) {
            myNote2.noteTick();
        }
    }

    public void checkNote() {
        if (!(millis() < myNote.time - leniencyMiss)) {
            // If the hit time is not outside the miss leniency, then we know the note is a hit.
            if (keys.get(myNote.key) == null || keys.get(myNote.key) != 'T' || millis() < myNote.time - leniency40) {
                // If the wrong key is hit, or the note is hit outside the 40 leniency, it's a miss.
                myNote = null;
                print("Miss!");
            } else {
                print("Hit!");
            }
        }
    }

    // This project uses a modified version of jeremydouglass' solution for checking when multiple keys are pressed or held at once.
    // It can be found here: https://discourse.processing.org/t/solution-for-repeating-keypressed-while-holding-key/3989/3

    // Within this HashMap, the key is the character, and the value is it's state.
    HashMap<Integer, Character> keys = new HashMap<Integer, Character>();

    // Sets the value in the HashMap to either:
    // T - True - Pressed down, can hit notes
    // B - Blocked - Pressed down but has already hit note, used to block repeating keys
    // F - False - Not pressed

    public void keyPressed() {
        if (keys.containsKey(keyCode)) {
            // Only set to true if the key is not blocked (preventing repeating keys without releasing them)
            if (keys.get(keyCode) != 'B') keys.put(keyCode, 'T');
        } else {
            keys.put(keyCode, 'T');
        }
        checkNote();
    }

    public void keyReleased() {
        keys.put(keyCode, 'F');
    }

}