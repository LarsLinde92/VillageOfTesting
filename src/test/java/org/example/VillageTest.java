package org.example;

import org.example.objects.Building;
import org.example.objects.Project;
import org.example.objects.Worker;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class VillageTest {

    private Village village;
    private Worker worker;

    @BeforeEach
    public void beforeEach() {
        village = new Village();
    }

    @Test
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
    @CsvSource({
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
        assertFalse(village.isFull(),"The village should not be full");
    }
    @Test
    public void testToAddAProject(){
        village.setWood(5);
        village.setMetal(1);

        village.addProject("Woodmill");

        assertFalse(village.getProjects().isEmpty(),"Project list should not be empty after we add a project.");
        assertTrue(village.getWood() < 5,"Wood should decrease after adding a project.");
        assertTrue(village.getMetal() < 1, "Metal should decrease after adding a project.");

        Project addedProject = village.getProjects().get(0);
        assertEquals("Woodmill", addedProject.getName(),"The added project should be a woodmill.");
    }
     @Test
      public void testToAddANonWorkingProject(){
          // Given
          int initialWood = village.getWood();
          int initialMetal = village.getMetal();
          int initialProjectsSize = village.getProjects().size();

          // When
          boolean result = village.addProject("InvalidProjectName");

          // Then
          assertFalse(result, "Adding a non-existing project should fail");
          assertEquals(initialProjectsSize, village.getProjects().size(), "Projects list should not change after attempting to add a non-existing project");
          assertEquals(initialWood, village.getWood(), "Wood should not decrease after attempting to add a non-existing project");
          assertEquals(initialMetal, village.getMetal(), "Metal should not decrease after attempting to add a non-existing project");
      }
    @Test
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
        assertEquals(initialMaxWorkers + 2, village.getMaxWorkers(), "Max workers should increase by 2 after building a House");
        assertTrue(isAdded, "House project should be successfully added");

    }
    @Test
    public void testWoodmillIncreasesWoodProduction() {
        // Given
        village.setWood(5); // Ensure there's enough wood for the Woodmill project
        village.setMetal(1); // Ensure there's enough metal for the Woodmill project
        village.setFood(10);
        int initialWoodProduction = village.getWoodPerDay();
        village.addWorker("Arta", "builder"); // Assuming adding a builder is necessary
        village.addWorker("Anders","lumberjack");

        // When
        boolean isAdded = village.addProject("Woodmill"); // Attempt to add a Woodmill project
        for (int day = 0; day < 5; day++) { // It takes 5 days for a Woodmill to complete
            village.Day(); // Simulate a day passing in the game
        }

        // Then
        int newWoodProduction = village.getWoodPerDay();
        assertTrue(newWoodProduction > initialWoodProduction, "Wood production per day should increase after building a Woodmill");
        assertTrue(isAdded, "Woodmill project should be successfully added");
        assertEquals(initialWoodProduction + 1, newWoodProduction, "Wood production per day should increase by the expected amount after building a woodmill");
    }
    @Test
    public void testQuarryIncreasesMetalProduction() {
        // Given
        village.setWood(3); // Ensure there's enough wood for the Quarry project
        village.setMetal(5); // Ensure there's enough metal for the Quarry project
        village.setFood(14);
        int initialMetalProduction = village.getMetalPerDay();
        village.addWorker("Arta", "builder");
        village.addWorker("Mickey","miner");

        // When
        boolean isAdded = village.addProject("Quarry"); // Attempt to add a Quarry project
        for (int day = 0; day < 7; day++) { // It takes 7 days for a Quarry to complete
            village.Day(); // Simulate a day passing in the game
        }

        // Then
        int newMetalProduction = village.getMetalPerDay();
        assertTrue(newMetalProduction > initialMetalProduction, "Metal production per day should increase after building a Quarry");
        assertTrue(isAdded, "Quarry project should be successfully added");
        assertEquals(initialMetalProduction + 1, newMetalProduction, "Metal production per day should increase by the expected amount after building a quarry");
    }
    @Test
    public void testFarmIncreasesFoodProduction() {
        // Given
        village.setWood(5); // Ensure there's enough wood for the Farm project
        village.setMetal(2); // Ensure there's enough metal for the Farm project
        int initialFoodProduction = village.getFoodPerDay();
        village.addWorker("Arta","builder");
        village.addWorker("Lars", "farmer"); // Adding a farmer to the village, assuming role relevance

        // When
        boolean isAdded = village.addProject("Farm"); // Attempt to add a Farm project
        for (int day = 0; day < 5; day++) { // It takes 5 days for a Farm to complete
            village.Day(); // Simulate a day passing in the game
        }

        // Then
        int newFoodProduction = village.getFoodPerDay();
        assertTrue(newFoodProduction > initialFoodProduction, "Food production per day should increase after building a Farm");
        assertTrue(isAdded, "Farm project should be successfully added");
        assertEquals(initialFoodProduction + 5, newFoodProduction, "Food production per day should increase by the expected amount after building a Farm");
    }
    @Test
    @DisplayName("Test that the castle project ends the game when completed")
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
        assertTrue(village.isGameOver(), "Game should end after building a Castle");
        assertTrue(isAdded, "Castle project should be successfully added");
        assertTrue(day <= 50, "Game should end within 50 days after starting the Castle project");
        assertFalse(village.addProject("AnotherProject"), "No new projects should be added after the game is over");
    }

    @Test
    void testPrintInfo() {
        // Your test setup code

        // Invoke the PrintInfo method to get the actual output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        village.PrintInfo();
        String actualOutput = outContent.toString().trim(); // Trim any leading/trailing whitespaces

        // Define the updated expected output string
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

        // Compare the expected and actual outputs
        assertEquals(expected, actualOutput);
        assertTrue(actualOutput.contains("You have no workers."), "No workers message not found in output");
    }


}






