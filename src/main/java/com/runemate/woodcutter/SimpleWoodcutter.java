package com.runemate.woodcutter;

import com.runemate.game.api.hybrid.entities.GameObject;
import com.runemate.game.api.hybrid.entities.Player;
import com.runemate.game.api.hybrid.entities.details.Locatable;
import com.runemate.game.api.hybrid.local.Camera;
import com.runemate.game.api.hybrid.local.hud.interfaces.Bank;
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory;
import com.runemate.game.api.hybrid.location.navigation.Traversal;
import com.runemate.game.api.hybrid.region.GameObjects;
import com.runemate.game.api.hybrid.region.Players;
import com.runemate.game.api.hybrid.util.calculations.Distance;
import com.runemate.game.api.script.framework.LoopingBot;
import com.runemate.game.api.script.framework.listeners.SettingsListener;
import com.runemate.game.api.script.framework.listeners.events.SettingChangedEvent;
import com.runemate.ui.setting.annotation.open.SettingsProvider;
import com.runemate.game.api.hybrid.util.Execution;
import com.runemate.game.api.hybrid.location.navigation.ScenePath;
import com.runemate.game.api.hybrid.location.Area;
import com.runemate.game.api.hybrid.local.hud.interfaces.SpriteItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.Override;
import java.lang.String;

/*
 * LoopingBot is the framework I recommend for most people. The other options are TaskBot and TreeBot which each have their own
 * use-cases, but realistically LoopingBot is the simplest in almost all cases.
 *
 * We implement the 'SettingsListener' interface here, because we want to be able to react when the user presses the "Start" button.
 *
 * Note: Please pay attention to the Java Naming Conventions! This is an important habit to get into, and it will make your code
 * easier to read and maintain.
 * https://www.geeksforgeeks.org/java-naming-conventions/
 */
public class SimpleWoodcutter extends LoopingBot implements SettingsListener {

    /*
     * This creates a logger instance for this class, which is useful for reporting status and for debugging issues.
     * With good logging, you should be able to see exactly what the bot is up to at any given time.
     */
    private static final Logger logger = LogManager.getLogger(SimpleWoodcutter.class);

    /*
     * RuneMate provides a default user interface, which includes "smart settings". The @SettingsProvider annotation tells RuneMate
     * that you want it to create the user interface using the settings descriptor, in this case using 'WoodcuttingSettings'.
     *
     * Once RuneMate has created these settings, it will set the value of this field so that we can access them, as you will see below.
     */
    @SettingsProvider(updatable = true)
    private WoodcuttingSettings settings;


    /*
     * Here I have created an enum that defines the valid states that the bot can be in. In this case, that is either 'CHOP' or 'DROP'.
     * I store the current state in a field, and update it as necessary.
     */
    private WoodcuttingState state = WoodcuttingState.CHOP;


    /*
     * This is just a simple flag that we use to wait until the user has pressed the start button in the user interface. If they haven't
     * pressed it yet, we don't want the bot to do anything.
     */
    private boolean settingsConfirmed;

    /*
     * #onStart() is a method inherited from AbstractBot that we can override to perform actions when the bot is started.
     * In this implementation we are registering this class (which implements SettingsListener) with the EventDispatcher. This means
     * that the EventDispatcher will know to send any 'settings' events to this class.
     */
    @Override
    public void onStart(final String... arguments) {
        getEventDispatcher().addListener(this);
        // Immediately set the initial state based on user settings
        if (settings.shouldDropLogs()) {
            state = WoodcuttingState.DROP;
            logger.info("User has selected to drop logs. Bot will drop logs.");
        } else {
            state = WoodcuttingState.BANK;
            logger.info("User has selected to bank logs. Bot will bank logs.");
        }
    }

    /*
     * This is where your bot logic goes! You can control how fast the bot loops by using 'setLoopDelay()' and providing your desired
     * loop delay (in milliseconds).
     *
     * It's good practice in bots to limit in-game interactions to 1 per loop. You might be tempted to try and have the bot perform multiple
     * actions in a single loop, like dropping every log in your inventory, but we would *heavily* advise against this. Games are dynamic
     * things, and anything can change in the middle of your loop. The safest thing is always to just perform a single action, and then
     * your bot loop again.
     */
    @Override
    public void onLoop() {
        //We don't want to do anything until the user presses the "start" button, so we just check if they've pressed it and return if not.
        if (!settingsConfirmed) {
            return;
        }

        //I've broken the logic into a couple of methods here, 'chopTrees()' and 'dropLogs()'. I decide which one to use by looking at
        //the current state of the bot.
        if (settings.shouldDropLogs()) {
            switch (state) {
                case DROP -> chopTrees();
                case CHOP -> dropLogs();
            }
        } else {
            switch (state) {
                case CHOP -> chopTrees();
                case DROP -> dropLogs();
            }
        }
    }

    private void chopTrees() {
        //First thing we want to do when we're meant to be chopping is checking that we can actually chop!
        //If our inventory is full, we want to update the state to 'DROP' so that the bot will start dropping our logs.
        if (Inventory.isFull()) {
            state = WoodcuttingState.DROP;
            logger.info("Inventory is full, starting to drop logs");
            return;
        }

        //This gets a reference to the current player. We're going to use this to check if we're already animating and avoid spam clicking,
        //as well as walking towards the next tree.
        Player player = Players.getLocal();
        if (player == null) {
            logger.warn("Unable to find local player");
            return;
        }

        //When our player is idle our animation ID will be -1. If our animation isn't -1, we can safely assume that we're already chopping
        //and don't need to do anything else!
        if (player.getAnimationId() != -1) {
            logger.info("Already chopping...");
            return;
        }

        //RuneMate's QueryBuilders are a powerful way to locate virtually anything in the game.
        //Here we are using our 'WoodcuttingSettings' to work out which type of tree we want to chop, and then looking
        //for an object in-game that has that name.
        String treeName = settings.getTreeType().getTreeName();
        GameObject tree = GameObjects.newQuery().names(treeName).results().nearest();
        if (tree == null) {
            logger.warn("Unable to find tree with name: {}", settings.getTreeType().getTreeName());
            return;
        }


        //Just because we managed to find a nearby tree doesn't mean that we can immediately interact with it!
        //This block of code will do a few things:
        //  1. Check if the tree is invisible, and if it isn't...
        //  2. Check how far away from the tree we are. If we're reasonably far away then we might need to build a path to it
        //  3. Build a path to the tree, and walk it using 'step()'
        //  4. Finally, we can try turning our camera towards the tree.
        if (!tree.isVisible()) {
            if (Distance.between(player, tree) > 8) {
                logger.info("We're far away from {}, walking towards it", tree);

                /*
                 * Building a path can be tricky! The tiles directly underneath GameObjects often can't be walked on. The path-builder
                 * doesn't know this, so will fail to build a path to it. To help get around this, we can try walking to a tile that's next
                 * to the tree instead. We can work out what tiles surround the tree by using 'getArea()' to work out the area of the tree
                 * in-game, and then using 'getSurroundingCoordinates()' to get the tiles immediately around it.
                 */
                Area.Rectangular area = tree.getArea();
                if (area == null) {
                    logger.warn("Unable to find an appropriate tile next to the tree to walk to!");
                    return;
                }

                ScenePath path = ScenePath.buildBetween(player, area.getSurroundingCoordinates());
                if (path == null) {
                    logger.warn("Unable to find a path to {}", tree);
                    return;
                }

                /*
                 * The 'step()' method just takes the next step along the path, it doesn't walk the whole thing.
                 */
                path.step();
                return;
            }

            /*
             * This turns the camera in a background task, which is why it's called 'concurrentlyTurnTo'. If we wanted to wait for the
             * camera to finish moving before doing anything else, we could just use 'turnTo' instead.
             */
            Camera.concurrentlyTurnTo(tree);
        }

        /*
         * There's quite a lot to break down in this line, so let's take it step-by-step.
         *
         * Most entities in the game are 'Interactable', which means we can use the 'interact' method on them. This method returns a boolean
         * which will be 'true' when the interaction succeeded, and 'false' when the interaction fails.
         *
         * Likewise, the "delay" methods in the 'Execution' class also return a boolean. Using 'delayUntil' will wait until either:
         *  1. The condition in the first parameter is met, in this case if the player is animating.
         *  2. The timeout in the last parameter is met, in this case 1200ms (or 2 game ticks).
         *
         * The second parameter it's a "reset" condition, which resets the timeout while true. In this example, it means that the 1200ms
         * timeout will not start counting down until the player has stopped moving.
         *
         * The delay is necessary in order to stop the bot from spam-clicking the tree.
         * If both of these methods succeed, we know that we have successfully started chopping the tree.
         */
        if (tree.interact("Chop down") && Execution.delayUntil(() -> player.getAnimationId() != -1, () -> player.isMoving(), 1200)) {
            logger.info("Chopping tree");
        }
    }

    private void dropLogs() {
        //Again, we reference our 'WoodcuttingSettings' to get the name of the logs we're chopping.
        String logName = settings.getTreeType().getLogName();

        //Again, we use a QueryBuilder to find the first of these logs in our inventory
        SpriteItem logs = Inventory.newQuery().names(logName).results().first();

        //If we don't have any logs, then the result of the query will be 'null', and we can change our bots state to 'CHOP'
        //This is very similar to using '!Inventory.contains(logName)'
        if (logs == null) {
            state = WoodcuttingState.CHOP;
            logger.info("No more logs left, starting to chop trees");
            return;
        }
        /*
         * Again, we check if our interaction with the log item is successful, and then wait until the item is no longer valid
         * We don't provide a reset condition here because it isn't necessary.
         *
         * You'll notice here that we use 'logs::isValid', which is just shorthand for '() -> logs.isValid()'.
         */
        if (logs.interact("Drop") && Execution.delayWhile(logs::isValid, 600)) {
            logger.info("Successfully dropped logs");
        }
    }

    private void bankLogs() {
        if (!Inventory.contains(settings.getTreeType().getLogName())) {
            state = WoodcuttingState.CHOP;
            logger.info("No logs left, returning to chopping.");
            return;
        }

        // Find nearest bank
        LocatableEntity bank = Banks.getLoaded().nearest();
        if (bank == null) {
            logger.warn("No bank found nearby!");
            return;
        }

        // Ensure bank is not null before proceeding
        if (bank == null) {
            logger.warn("No bank found nearby!");
            return;
        }

        // Ensure the bank is visible before interacting
        if (bank.getVisibility() < 50) {
            Camera.turnTo(bank);
            Execution.delay(500, 1000); // Add small delay for realistic camera movement
        }

        // Move to the bank if too far away
        if (Distance.to(bank) > 8) {
            ScenePath path = ScenePath.buildTo(bank);
            if (path != null && path.step()) {
                logger.info("Walking to the bank...");
                Execution.delayUntil(() -> Distance.to(bank) <= 3, 5000); // Wait until close
            } else {
                logger.warn("Failed to generate a path to the bank!");
            }
            return; // Exit function to allow movement before interacting
        }


        // Interact with the bank
        if (bank.interact("Bank")) {
            if (Execution.delayUntil(Bank::isOpen, 2000)) {
                Bank.depositInventory();
                Execution.delay(800, 1200); // Simulate human reaction time
                Bank.close();
                state = WoodcuttingState.CHOP;
                logger.info("Banked logs, resuming chopping.");
            }
        }
    }


    @Override
    public void onSettingChanged(SettingChangedEvent event) {

    }

    /*
     * This method is called when the user presses the 'Start' button in the user interface.
     */
    @Override
    public void onSettingsConfirmed() {
        settingsConfirmed = true;
    }
}
