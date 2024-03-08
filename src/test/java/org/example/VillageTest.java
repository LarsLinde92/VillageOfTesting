package org.example;

import org.example.objects.Project;
import org.example.objects.Worker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class VillageTest {

    private Village village;

    @BeforeEach
    public void beforeEach() {
        village = new Village();
    }

    @Test
    @DisplayName("Resource levels after adding workers of various occupations and simulating a day.")
    void testAddWorkersAndSimulateADayInTheVillage() {

        // Given:
        // A new village and initial resource levels
        int expectedFood = 12; // Expected food after adding workers and simulating a day
        int expectedWood = 1; // Expected wood after adding workers and simulating a day
        int expectedMetal = 1; // Expected metal after adding workers and simulating a day

        // When:
        // Adding three workers of different jobs and simulating a day
        village.addWorker("Lars", "farmer");
        village.addWorker("Anders", "lumberjack");
        village.addWorker("Mickey", "miner");
        village.Day();

        // Then:
        // Asserting that the resource levels are as expected
        assertAll(
                () -> assertEquals(expectedFood, village.getFood(), "Food should match expected level after simulating a day."),
                () -> assertEquals(expectedWood, village.getWood(), "Wood should match expected level after simulating a day."),
                () -> assertEquals(expectedMetal, village.getMetal(), "Metal should match expected level after simulating a day.")
        );

        // Asserting that the number of workers is correct
        assertEquals(3, village.getWorkers().size(), "There should be 3 workers in the village.");

        // Asserting the occupations of the added workers
        assertEquals("farmer", village.getWorkers().get(0).getOccupation(), "Lars should be a farmer.");
        assertEquals("lumberjack", village.getWorkers().get(1).getOccupation(), "Anders should be a lumberjack.");
        assertEquals("miner", village.getWorkers().get(2).getOccupation(), "Mickey should be a miner.");
    }

    @Test
    @DisplayName("Prevent adding workers beyond maximum capacity")
    public void testCannotAddMoreWorkersThanMax() {
        // Given:
        // Adds maximum number of workers.
        for (int i = 0; i < village.getMaxWorkers(); i++) {
            village.addWorker("RandomWorker" + i, "builder");
        }

        // When:
        // Attempting to add one more worker beyond the maximum capacity.
        boolean wasAdded = village.addWorker("BadWorker", "builder");

        // Then:
        // Checks so the village is full and the bad worker was not added.
        assertTrue(village.isFull(), "Village should be full when maximum workers are added.");
        assertFalse(wasAdded, "Should not be able to add more workers than max.");
    }

    @Test
    @DisplayName("Resource levels unchanged in a day without workers")
    public void testNextDayWithoutWorkers() {
        // Given:
        // The village has no workers from the start.
        assertEquals(0, village.getWorkers().size(), "Initial condition failed: there should be no workers.");

        // Store initial resource levels
        int initialFood = village.getFood();
        int initialWood = village.getWood();
        int initialMetal = village.getMetal();

        // When:
        // Simulating one day in the village without workers
        village.Day();

        // Then:
        // Verify that no resources have been consumed or collected since there are no workers in the village.
        assertEquals(initialFood, village.getFood(), "Food should remain unchanged when there are no workers.");
        assertEquals(initialWood, village.getWood(), "Wood should remain unchanged when there are no workers.");
        assertEquals(initialMetal, village.getMetal(), "Metal should remain unchanged when there are no workers.");
    }

    @Test
    @DisplayName("Worker count remains unchanged after a day without food")
    public void testNextDayWithWorkersButNoFood() {
        // Given:
        // Initial setup
        village.addWorker("Mickey", "miner");
        village.setFood(0); // Ensuring no food is available at the start
        int initialWorkerCount = village.getWorkers().size();

        // When:
        // Simulate a day
        village.Day();

        // Then:
        // Check the outcomes
        assertEquals(0, village.getFood(), "No food should be added or consumed.");
        assertEquals(initialWorkerCount, village.getWorkers().size(), "Worker count should remain the same.");
    }

    @ParameterizedTest
    @DisplayName("Worker survival and game outcome after days without food")
    @CsvSource(value = {
            "1, true, false", // Day 1: worker alive (true), game over (false)
            "2, true, false", // Day 2: worker alive (true), game over (false)
            "3, true, false", // Day 3: worker alive (true), game over (false)
            "4, true, false", // Day 4: worker alive (true), game over (false)
            "5, true, false", // Day 5: worker alive (true), game over (false)
            "6, false, true"  // Day 6: worker alive (false), game over (true)
    })
    public void testWorkerStarvationAndGameOutcome(int daysWithoutFood, boolean expectedWorkerAlive, boolean expectedGameOver) {
        // Given:
        // Initial setup
        village.addWorker("Mickey", "miner");
        village.addWorker("Anders", "lumberjack");
        village.setFood(0); // No food from the start

        // When:
        // Simulate days without food
        for (int i = 0; i < daysWithoutFood; i++) {
            village.Day(); // Simulate day progression
            System.out.println("Day: " + village.getDaysGone());
        }

        // Then:
        // Check worker's living status and game over status
        Worker worker = village.getWorkers().get(0);
        assertEquals(expectedWorkerAlive, worker.isAlive(), "Worker alive status does not match expected for day " + daysWithoutFood);
        assertEquals(expectedGameOver, village.isGameOver(), "Game over status does not match expected for day " + daysWithoutFood);
        assertFalse(village.isFull(), "The village should not be full");
    }

    @Test
    @DisplayName("Successfully adding a project deducts resources and updates project list.")
    public void testToAddAProject() {
        // Given:
        // Initial resources are set to allow adding a specific project.
        village.setWood(5);
        village.setMetal(1);

        // When:
        // A project is added, which should use some resources.
        village.addProject("Woodmill");

        // Then:
        // Asserts that the project list is updated, resources are deducted, and the correct project is added.
        assertFalse(village.getProjects().isEmpty(), "Project list should not be empty after adding a project.");
        assertTrue(village.getWood() < 5, "Wood should decrease after adding a project.");
        assertTrue(village.getMetal() < 1, "Metal should decrease after adding a project.");

        Project addedProject = village.getProjects().get(0);
        assertEquals("Woodmill", addedProject.getName(), "The added project should be a woodmill.");
    }

    @Test
    @DisplayName("Attempt to add non-existent project fails without altering resources.")
    public void testToAddANonWorkingProject() {
        // Given:
        // Captures initial state for resources and projects list before attempting to add a non-existent project.
        int initialWood = village.getWood();
        int initialMetal = village.getMetal();
        int initialProjectsSize = village.getProjects().size();

        // When:
        // Attempts to add a project with a name that doesn't exist in the possible projects.
        boolean result = village.addProject("InvalidProjectName");

        // Then:
        // Verifies that the attempt to add a non-existent project fails and does not alter the village's resources or projects list.
        assertFalse(result, "Adding a non-existing project should fail.");
        assertEquals(initialProjectsSize, village.getProjects().size(), "Projects list should not change after attempting to add a non-existing project.");
        assertEquals(initialWood, village.getWood(), "Wood should not decrease after attempting to add a non-existing project.");
        assertEquals(initialMetal, village.getMetal(), "Metal should not decrease after attempting to add a non-existing project.");
    }

    @Test
    @DisplayName("Increase maximum workers after building a house")
    public void testHouseIncreasesMaximumWorkers() {
        // Given
        village.setWood(5); // Ensure there's enough wood for a house
        village.setFood(3); // Assuming you also need to maintain food for workers
        int initialMaxWorkers = village.getMaxWorkers();
        village.addWorker("Arta", "builder"); // Adding a builder to the village


        // When
        boolean isAdded = village.addProject("House"); // Attempt to add a house project
        for (int day = 0; day < 3; day++) { // It takes 3 days for a house to complete
            village.Day(); // Simulate a day passing in the game
        }

        // Then
        assertEquals(initialMaxWorkers + 2, village.getMaxWorkers(), "Max workers should increase by 2 after building a house.");
        assertTrue(isAdded, "House project should be successfully added");

    }

    @Test
    @DisplayName("Increase in wood production after building a wood-mill")
    public void testWoodmillIncreasesWoodProduction() {
        // Given
        village.setWood(5); // Ensure there's enough wood for the Woodmill project
        village.setMetal(1); // Ensure there's enough metal for the Woodmill project
        village.setFood(10);
        int initialWoodProduction = village.getWoodPerDay();
        village.addWorker("Arta", "builder"); // Assuming adding a builder is necessary
        village.addWorker("Anders", "lumberjack");

        // When
        boolean isAdded = village.addProject("Woodmill"); // Attempt to add a Woodmill project
        for (int day = 0; day < 5; day++) { // It takes 5 days for a Woodmill to complete
            village.Day(); // Simulate a day passing in the game
        }

        // Then
        int newWoodProduction = village.getWoodPerDay();
        assertTrue(newWoodProduction > initialWoodProduction, "Wood production per day should increase after building a woodmill.");
        assertTrue(isAdded, "Woodmill project should be successfully added.");
        assertEquals(initialWoodProduction + 1, newWoodProduction, "Wood production per day should increase by the expected amount after building a woodmill.");
    }

    @Test
    @DisplayName("Increase in metal production after building a quarry.")
    public void testQuarryIncreasesMetalProduction() {
        // Given
        village.setWood(3); // Ensure there's enough wood for the Quarry project
        village.setMetal(5); // Ensure there's enough metal for the Quarry project
        village.setFood(14);
        int initialMetalProduction = village.getMetalPerDay();
        village.addWorker("Arta", "builder");
        village.addWorker("Mickey", "miner");

        // When
        boolean isAdded = village.addProject("Quarry"); // Attempt to add a Quarry project
        for (int day = 0; day < 7; day++) { // It takes 7 days for a Quarry to complete
            village.Day(); // Simulate a day passing in the game
        }

        // Then
        int newMetalProduction = village.getMetalPerDay();
        assertTrue(newMetalProduction > initialMetalProduction, "Metal production per day should increase after building a quarry.");
        assertTrue(isAdded, "Quarry project should be successfully added.");
        assertEquals(initialMetalProduction + 1, newMetalProduction, "Metal production per day should increase by the expected amount after building a quarry.");
    }

    @Test
    @DisplayName("Increase in food production after building a farm.")
    public void testFarmIncreasesFoodProduction() {
        // Given
        village.setWood(5); // Ensure there's enough wood for the Farm project
        village.setMetal(2); // Ensure there's enough metal for the Farm project
        int initialFoodProduction = village.getFoodPerDay();
        village.addWorker("Arta", "builder"); // Adding a builder to the village
        village.addWorker("Lars", "farmer"); // Adding a farmer to the village

        // When
        boolean isAdded = village.addProject("Farm"); // Attempt to add a Farm project
        for (int day = 0; day < 5; day++) { // It takes 5 days for a Farm to complete
            village.Day(); // Simulate a day passing in the game
        }

        // Then
        int newFoodProduction = village.getFoodPerDay();
        assertTrue(newFoodProduction > initialFoodProduction, "Food production per day should increase after building a Farm.");
        assertTrue(isAdded, "Farm project should be successfully added.");
        assertEquals(initialFoodProduction + 5, newFoodProduction, "Food production per day should increase by the expected amount after building a Farm.");
    }

    @Test
    @DisplayName("Test that the castle project ends the game when completed.")
    public void testCastleEndsGame() {
        // Given
        village.setWood(50); // Ensure there's enough wood for the Castle project
        village.setMetal(50); // Ensure there's enough metal for the Castle project
        village.setFood(50); // Assuming you also need to maintain food for workers
        village.addWorker("Arta", "builder"); // Adding a builder to the village

        // When
        boolean isAdded = village.addProject("Castle"); // Attempt to add a Castle project
        int day;
        for (day = 0; day < 50; day++) { // Assuming it takes a significant amount of time to complete
            village.Day(); // Simulate a day passing in the game, until the Castle is completed or the game ends
            if (village.isGameOver()) { // Check if the game has ended as a result of completing the Castle
                break;
            }
        }

        // Then
        assertTrue(village.isGameOver(), "Game should end after building a Castle.");
        assertTrue(isAdded, "Castle project should be successfully added.");
        assertTrue(day <= 50, "Game should end within 50 days after starting the Castle project.");
        assertFalse(village.addProject("AnotherProject"), "No new projects should be added after the game is over.");
    }

    @Test
    @DisplayName("Simulate game progression and win by building a castle.")
    public void testSimulateGameAndWin() {
        // Given:
        // Initial setup with maximum workers, resources, and verifies initial conditions.
        addMaxWorkers(village, 6); // Assumes village and addMaxWorkers are properly initialized and implemented.
        village.setFood(10);
        assertEquals(6, village.getWorkers().size(), "Initial worker count should be 6.");

        // Simulating initial phase of resource gathering and infrastructure development.
        simulateDays(village, 5); // Simulate 5 days for resource accumulation.
        addProject(village, "House"); // Increase worker limit by building an extra house.
        simulateDays(village, 3); // Allow time for house construction.

        // When:
        // Expansion phase with additional workers and building projects to gather enough resources for the Castle.
        addMaxWorkers(village, 2); // Adds two more workers after the house is built.
        assertEquals(8, village.getWorkers().size(), "After building a house, we should have 8 workers.");

        // Adds essential buildings/projects for increased resource production.
        addProject(village, "Woodmill");
        addProject(village, "Quarry");
        addProject(village, "Farm");
        simulateDays(village, 20); // Simulates days for project completion and resource gathering.

        // Initiates the construction of the Castle, the winning condition.
        addProject(village, "Castle");
        simulateDays(village, 50); // Simulates days until the Castle is built.

        // Then:
        // Verifies that building the Castle wins the game.
        assertTrue(village.isGameOver(), "Game should be over after building the castle.");
    }
    // Helper methods for the test
    private void addMaxWorkers(Village village, int numberOfWorkers) {
        String[] occupations = {"farmer", "lumberjack", "miner", "builder", "miner", "lumberjack"};
        for (int i = 0; i < numberOfWorkers; i++) {
            village.addWorker("Worker" + i, occupations[i % occupations.length]);
        }
    }
    private void addProject(Village village, String projectName) {
        village.addProject(projectName);
    }

    private void simulateDays(Village village, int days) {
        for (int i = 0; i < days; i++) {
            village.Day();
        }
    }

    @Test
    @DisplayName("Verify village information printout for accuracy and completeness.")
    void testPrintInfo() {
        // Given:
        // Set up your village state here if necessary. The setup might include adding buildings or setting initial resources,
        // but for this snippet, it appears the village starts with default values as per the Village constructor.

        // When:
        // Capture the output of the PrintInfo method.
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent)); // Redirect System.out to capture the printout.
        village.PrintInfo(); // Invoke the method to generate the output.
        String actualOutput = outContent.toString().trim(); // Retrieve and trim the captured output for comparison.

        // Define the expected output based on the initial state of the village as set up above.
        String expected = "You have no workers." + System.lineSeparator() +
                "Your current buildings are: " + System.lineSeparator() +
                "House House House " + System.lineSeparator() +
                "You can have 6 workers." + System.lineSeparator() +
                "Your current projects are: " + System.lineSeparator() +
                System.lineSeparator() +
                "Current Food:  10" + System.lineSeparator() +
                "Current Wood:  0" + System.lineSeparator() +
                "Current Metal: 0" + System.lineSeparator() +
                "Generating 5 food per day per worker." + System.lineSeparator() +
                "Generating 1 wood per day per worker." + System.lineSeparator() +
                "Generating 1 metal per day per worker.";

        // Then:
        // Compare the expected and actual outputs to verify the accuracy and completeness of the PrintInfo output.
        assertEquals(expected, actualOutput, "The actual output should match the expected output.");
        // Additional specific check for a part of the expected output.
        assertTrue(actualOutput.contains("You have no workers."), "No workers message not found in output.");
    }

    @Test
    @DisplayName("Ensure setMaxWorkers updates capacity and village is not full post-update.")
    public void testSetMaxWorkers() {
        // Set a new value for maxWorkers
        int MaxWorkers = 10; // Arbitrary value for testing
        village.setMaxWorkers(MaxWorkers);

        // Verify that the maxWorkers has been updated correctly
        assertEquals(MaxWorkers, village.getMaxWorkers(), "The maxWorkers should be updated to the new value.");
        assertFalse(village.isFull(),"The village should not be full, since we changed the max workers capacity.");
    }
    @Test
    @DisplayName("Verify setGameOver(true) correctly sets game over condition.")
    void testSetGameOverTrue() {
        village.setGameOver(true);
        assertTrue(village.isGameOver(), "The game should be marked as over.");
    }

    @Test
    @DisplayName("Verify setGameOver(false) correctly resets game over condition.")
    void testSetGameOverFalse() {
        village.setGameOver(false);
        assertFalse(village.isGameOver(), "The game should not be marked as over.");
    }

    @Test
    @DisplayName("Verify setDaysGone updates days and does not affect unrelated attributes.")
   public void testSetDaysGoneAndUnrelatedAttributes() {
        int initialFood = village.getFood(); // Assuming getFood() is a method that exists
        int testDaysGone = 10;
        village.setDaysGone(testDaysGone);

        // Verify daysGone was set correctly
        assertEquals(testDaysGone, village.getDaysGone(), "The daysGone should be updated to the test value.");

        // Additionally, verify that setting daysGone does not inadvertently affect unrelated attributes
        assertEquals(initialFood, village.getFood(), "The food supply should remain unchanged after setting daysGone.");
    }

    @Test
    @DisplayName("Verify setFoodPerDay correctly updates daily food production rate.")
   public void testSetFoodPerDay() {
        // Example value to test
        int newFoodPerDay = 15;
        // Set foodPerDay to the new value
        village.setFoodPerDay(newFoodPerDay);

        // Verify that the foodPerDay was correctly updated
        assertEquals(newFoodPerDay, village.getFoodPerDay(), "foodPerDay should be updated to the new value.");
    }

    @Test
    @DisplayName("Verify setWoodPerDay correctly updates daily wood production rate.")
    public void testSetWoodPerDay() {
        // Example value to test
        int newWoodPerDay = 15;
        // Set woodPerDay to the new value
        village.setWoodPerDay(newWoodPerDay);

        //Verify that the woodPerDay was correctly updated
        assertEquals(newWoodPerDay, village.getWoodPerDay(), "woodPerDay should be updated to the new value.");
    }
    @Test
    @DisplayName("Verify setMetalPerDay correctly updates daily metal production rate.")
    public void testSetMetalPerDay() {
        // Example value to test
        int newMetalPerDay = 15;
        // Set woodPerDay to the new value
        village.setMetalPerDay(newMetalPerDay);

        //Verify that the woodPerDay was correctly updated
        assertEquals(newMetalPerDay, village.getMetalPerDay(),"metalPerDay should be updated to the new value.");
    }
}






