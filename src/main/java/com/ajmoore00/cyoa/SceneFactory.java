package com.ajmoore00.cyoa;
import java.util.Map;
import java.util.HashMap;

// Class for making all the scenes for the game
public class SceneFactory {
    // Method to create and return all scenes as a map
    public static Map<String, Scene> createScenes(
            String shipName, String moonName, String planetName, String systemName) {
        Map<String, Scene> scenes = new HashMap<>();

        Scene intro = new Scene(
            "=== Krylos ===\n" +
            "Year 2342. You were part of a science crew on the SS Pioneer, heading to Krylos, a moon orbiting the gas giant Virelia in the Helion Drift system.\n" +
            "Your team picked up weird stuff in the scans. The moon seemed to radiate some kind of energy, and command wanted answers.\n" +
            "What's your name, engineer?"
        );
        // No choices for intro; handled by frontend

        Scene cryoWake = new Scene(
            "You wake up in your busted cryo pod on the " + shipName +
            ". The ship is sideways, alarms are off, and your head is pounding. " +
            "You remember you were supposed to help set up a research base, but now it looks like the ship crash-landed. " +
            "Flickering lights cast long shadows, and you hear distant scraping noises echoing through the hull."
        );
        cryoWake.addChoice("Check the ship's systems", "Bridge");
        cryoWake.addChoice("Look for other crew", "Medbay");
        cryoWake.addChoice("Head outside", "CrashSite");
        cryoWake.addChoice("Search storage room", "Storage");

        Scene storage = new Scene(
            "You find the storage room. It's a mess of toppled crates and scattered tools. " +
            "The floor is covered in a thin layer of white dust tracked in from outside."
        );
        storage.addChoice("Search for tools", "CryoWake");
        storage.addChoice("Go back to cryo pod", "CryoWake");

        Scene bridge = new Scene(
            "You stumble to the bridge, stepping over loose wires and floating debris. " +
            "The controls are fried, but you spot a log entry about a massive energy spike right before the crash. " +
            "A cracked display shows a map with a blinking dot: possible escape shuttle location."
        );
        bridge.addChoice("Go to the escape shuttle", "ShuttleBay");
        bridge.addChoice("Search the workshop", "Workshop");
        bridge.addChoice("Go back to cryo pod", "CryoWake");

        Scene workshop = new Scene(
            "The workshop is cramped and smells like burnt circuits. Tools are scattered everywhere, " +
            "and you spot a plasma cutter on a bench."
        );
        workshop.addChoice("Grab plasma cutter", "Bridge");
        workshop.addChoice("Go back to bridge", "Bridge");

        Scene medbay = new Scene(
            "The medbay is trashed. No sign of the crew, just a lot of blood smeared across the floor " +
            "and a half-working med-stim dispenser humming in the corner. " +
            "A broken comms panel crackles with static, but you can't make out any voices."
        );
        medbay.addChoice("Take a med-stim", "Medbay");
        medbay.addChoice("Continue down the hall", "SpareParts");
        medbay.addChoice("Go back to cryo pod", "CryoWake");

        Scene spareParts = new Scene(
            "You find a small room labeled 'Spare Parts'. Shelves are knocked over, " +
            "but you spot a box labeled 'Shuttle Parts'."
        );
        spareParts.addChoice("Grab shuttle parts", "SpareParts");
        spareParts.addChoice("Go back to medbay", "Medbay");

        Scene crashSite = new Scene(
            "\nYou step outside. The landscape is almost blinding—flat, endless, and so stark white it barely seems real. " +
            "The air is perfectly still, not hot or cold, and the silence is absolute. You feel like you’re standing in a dream.\n" +
            "Far off, you see something on the horizon. It almost looks like a structure, but the more you stare, " +
            "the more it seems to shimmer and blur, like a mirage.\n" +
            "You spot some tracks leading away from the ship, and the SS Pioneer is a twisted wreck behind you."
        );
        crashSite.addChoice("Follow the tracks", "Tracks");
        crashSite.addChoice("Approach the mirage structure", "MirageWalk");
        crashSite.addChoice("Go back inside", "CryoWake");

        Scene tracks = new Scene(
            "You follow the tracks across the crunchy white dust. You find a crew member's badge half-buried " +
            "and some weird, sticky slime. Whatever dragged them away left deep gouges in the ground. " +
            "The tracks lead toward a cluster of jagged rocks."
        );
        tracks.addChoice("Keep following the tracks", "LargeCreatureEncounter");
        tracks.addChoice("Go back to the crash site", "CrashSite");

        Scene largeCreatureEncounter = new Scene(
            "You round the rocks and freeze. A massive pale beast, almost blending into the white dust, towers over you. " +
            "Its eyes are black and empty, and in one clawed hand it clutches a device—something you recognize from the ship. " +
            "The ground is littered with scraps of a crew suit. The creature turns, and you know you have to fight or run."
        );
        largeCreatureEncounter.addChoice("Fight it", "LargeCreatureEncounter");
        largeCreatureEncounter.addChoice("Run back to the ship", "CrashSite");

        Scene postCombatLarge = new Scene(
            "You survived the fight. The creature drops the device as it collapses. " +
            "It's clearly from your ship—a powerful explosive, maybe meant for emergencies. Why did the creature have it?"
        );
        postCombatLarge.addChoice("Take the device and check the mirage structure", "MirageWalk");
        postCombatLarge.addChoice("Go back to the ship", "CrashSite");

        Scene mirageWalk = new Scene(
            "You start walking toward the distant structure. The more you walk, the farther away it seems to get, " +
            "like it's playing tricks on your mind. The white ground stretches on forever."
        );
        mirageWalk.addChoice("Keep going", "MirageRest");
        mirageWalk.addChoice("Rest for a moment", "MirageRestScene");
        mirageWalk.addChoice("Turn back", "CrashSite");

        Scene mirageRestScene = new Scene(
            "You sit down, catching your breath. The silence is overwhelming. " +
            "A strange, mouse-like creature scurries across the ground, vanishing into the white. " +
            "You get up, feeling a little steadier."
        );
        mirageRestScene.addChoice("Keep going", "MirageRest");
        mirageRestScene.addChoice("Turn back", "CrashSite");

        Scene mirageRest = new Scene(
            "Finally, you reach the structure. Up close, it's strangely simple—just smooth, pale walls and a single door. " +
            "It almost looks like it shouldn't be here, but it's undeniably real and ominous."
        );
        mirageRest.addChoice("Knock on the door", "AlienChamber");
        mirageRest.addChoice("Turn back", "CrashSite");

        Scene shuttleBay = new Scene(
            "You find the escape shuttle, half-buried in dust. It's mostly intact, but the power cell is missing and the hull is scorched. " +
            "Inside, the controls flicker and the air smells like burnt plastic. You might be able to fix it, if you can find the right parts."
        );
        shuttleBay.addChoice("Try to fix the shuttle", "ShuttleBay");
        shuttleBay.addChoice("Go back to the crash site", "CrashSite");

        Scene shuttleBayFixed = new Scene(
            "The shuttle is patched together, wires and panels barely holding. It looks ugly, but it might just fly. " +
            "The controls flicker, waiting for your decision."
        );
        shuttleBayFixed.addChoice("Escape on the shuttle", "END_ESCAPE");
        shuttleBayFixed.addChoice("Go back to the crash site", "CrashSite");

        Scene alienChamber = new Scene(
            "You step inside. The air is heavy and silent. You look around, taking in the strange, empty space. " +
            "Then you turn—and a tall, translucent creature is right in front of you, shimmering like the structure itself. " +
            "You know you can't fight something like this."
        );
        alienChamber.addChoice("Try to communicate", "AlienTalk");
        alienChamber.addChoice("Use the device to destroy the structure and everything inside", "END_SACRIFICE");
        alienChamber.addChoice("Leave for the shuttle", "ShuttleBay");

        Scene alienTalk = new Scene(
            "You try to speak, but the alien doesn't understand your words. Instead, you feel knowledge being pressed into your mind. " +
            "You see flashes of the ship crashing, the alien's need to keep this place secret, to protect the universe from those who would abuse what is hidden here. " +
            "You realize they caused the crash, but not out of malice."
        );
        alienTalk.addChoice("Warn them about the other humans coming", "END_STAY");
        alienTalk.addChoice("Activate the device and destroy everything", "END_SACRIFICE");
        alienTalk.addChoice("Leave for the shuttle", "ShuttleBay");

        // Endings
        Scene endStay = new Scene(
            "You stay with the aliens, learning their secrets and exploring the mysteries of " +
            moonName + ". The universe feels bigger and stranger than you ever imagined."
        );
        Scene endEscape = new Scene(
            "You escape and warn the others. The moon stays a mystery, at least for now."
        );
        Scene endSacrifice = new Scene(
            "You activate the device and a blinding white light fills the structure. " +
            "You feel the ground shake as everything collapses. The secret of Krylos is buried, but so are you."
        );
        Scene endDeath = new Scene(
            "Your vision fades as the cold and silence of Krylos closes in. This is where your story ends."
        );

        scenes.put("Intro", intro);
        scenes.put("CryoWake", cryoWake);
        scenes.put("Storage", storage);
        scenes.put("Bridge", bridge);
        scenes.put("Workshop", workshop);
        scenes.put("Medbay", medbay);
        scenes.put("SpareParts", spareParts);
        scenes.put("CrashSite", crashSite);
        scenes.put("Tracks", tracks);
        scenes.put("LargeCreatureEncounter", largeCreatureEncounter);
        scenes.put("PostCombatLarge", postCombatLarge);
        scenes.put("MirageWalk", mirageWalk);
        scenes.put("MirageRestScene", mirageRestScene);
        scenes.put("MirageRest", mirageRest);
        scenes.put("ShuttleBay", shuttleBay);
        scenes.put("ShuttleBayFixed", shuttleBayFixed);
        scenes.put("AlienChamber", alienChamber);
        scenes.put("AlienTalk", alienTalk);
        scenes.put("END_STAY", endStay);
        scenes.put("END_ESCAPE", endEscape);
        scenes.put("END_SACRIFICE", endSacrifice);
        scenes.put("END_DEATH", endDeath);

        return scenes;
    }
}
