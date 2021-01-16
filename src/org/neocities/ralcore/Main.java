package org.neocities.ralcore;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.HashMap;

public class Main extends PApplet {

    // Defining the scroll speed, changing how fast notes cross the screen.
    int scrollSpeed = 4;
    // Height offset for the rows.
    int rowOffsetH = 100;
    // Note square height. Also defines the gap between rows, to make it 0.
    int rowHeight = 50;
    // Millisecond leniency for a note to get missed...
    int leniencyMiss = 500;
    // ...and for a 40...
    int leniency40 = 75;
    // ...and for an 80.
    int leniency80 = 25;

    // Creating the ArrayList that will hold the notes for each row.
    ArrayList<Note> notes = new ArrayList<>();

    public class Note {
        int time;
        int key;
        public void noteTick(){
            // Assuming for now that we know exactly what time the note should be hit on to not calculate it, and that the song starts when the program starts.
            // TODO: Not do those things.
            if (millis() - leniencyMiss > time) {
                // If the note is deemed old, destroy it.
                notes.remove(0);
            } else {
                // Drawing note
                rect((time - millis()) * scrollSpeed / 10, rowOffsetH, 10, 50);
            }
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
        notes.add(new Note(2500, (int) 'Z'));
        notes.add(new Note(2600, (int) 'X'));
        notes.add(new Note(2700, (int) 'M'));
        notes.add(new Note(2800, (int) 'N'));
        notes.add(new Note(2900, (int) 'Z'));
        notes.add(new Note(3000, (int) 'X'));
        notes.add(new Note(3100, (int) 'M'));
        notes.add(new Note(3100, (int) 'N'));

    }

    public void draw() {
        background(80);
        for (int i = 0; i < notes.size(); i++) {
            notes.get(i).noteTick();
        }
    }

    public void checkNote() {
        if (millis() > notes.get(0).time - leniencyMiss && millis() < notes.get(0).time + leniencyMiss) {
            // If the hit time is not outside the miss leniency, then we know the note is a hit.
            if (keys.get(notes.get(0).key) == null || keys.get(notes.get(0).key) != 'T'|| millis() < notes.get(0).time - leniency40 || millis() > notes.get(0).time + leniency40) {
                // If the wrong key is hit, or the note is hit outside the 40 leniency, it's a miss.
                notes.remove(0);
                print("Miss!");
            } else if (millis() < notes.get(0).time - leniency80 || millis() > notes.get(0).time + leniency80) {
                // If the note is outside the 80 leniency, it's a 40.
                notes.remove(0);
                print("40!");
            } else {
                //Otherwise, it's an 80.
                notes.remove(0);
                print("80!");
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
        // Only set to true if the key is not blocked (preventing repeating keys without releasing them)
        if (!keys.containsKey(keyCode) || keys.get(keyCode) != 'B') {
            keys.put(keyCode, 'T');
            checkNote();
        }
    }

    public void keyReleased() {
        keys.put(keyCode, 'F');
    }

}