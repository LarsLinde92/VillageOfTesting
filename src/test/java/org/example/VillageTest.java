package org.example;

import org.example.objects.Worker;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VillageTest {

    private Village village;

    @BeforeEach
    public void beforeEach() {
        village = new Village();
    }

    @Test
    void addWorkersAndSimulateADayInTheVillage() {

        // Given:
        // A new village and initial resource levels
        int initialFood = village.getFood();
        int initialWood = village.getWood();
        int initialMetal = village.getMetal();

        // When:
        // Adding three workers of different jobs and simulating a day
        village.AddWorker("Lars", "farmer");
        village.AddWorker("Anders", "lumberjack");
        village.AddWorker("Mickey", "miner");
        village.Day();

        // Then:
        // Asserting that the number of workers is correct and resources have increased
        assertTrue(village.getFood() > initialFood, "Food should have increased.");
        assertTrue(village.getWood() > initialWood, "Wood should have increased.");
        assertTrue(village.getMetal() > initialMetal, "Metal should have increased.");

        assertEquals(3, village.getWorkers().size(), "There should be 3 workers in the village.");
        assertEquals("farmer", village.getWorkers().get(0).getOccupation(), "Lars should be a farmer.");
        assertEquals("lumberjack", village.getWorkers().get(1).getOccupation(), "Anders should be a lumberjack.");
        assertEquals("miner", village.getWorkers().get(2).getOccupation(), "Mickey should be a miner.");

    }

    @Test
    public void testCannotAddMoreWorkersThanMax() {
        // Given: Adds maximum number of workers.
        for (int i = 0; i < village.getMaxWorkers(); i++) {
            village.AddWorker("Worker" + i, "builder");
        }

        // When: Attempting to add one more worker beyond the maximum capacity.
        boolean wasAdded = village.AddWorker("ExtraWorker", "builder");

        // Then: Checks so the village is full and the extra worker was not added.
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
        System.out.println("No resources consumed or collected when there are no workers in the village.");

        // Then:
        // Verify that no resources have been consumed or collected
        assertEquals(initialFood, village.getFood(), "Food should remain unchanged when there are no workers.");
        assertEquals(initialWood, village.getWood(), "Wood should remain unchanged when there are no workers.");
        assertEquals(initialMetal, village.getMetal(), "Metal should remain unchanged when there are no workers.");
    }






@Before
public void before (){
    village.Day();
}




    @Test
    public void testWorkersEatWhenFoodAvailable() {
        // Given: Initial conditions with workers and sufficient food
        int initialFood = 10; // Adjust as needed based on your initial conditions
        village.setFood(initialFood);
        assertEquals(initialFood, village.getFood(), "Initial condition failed: there should be sufficient food.");

        // When: Simulating one day in the village
        //village.Day();

        // Then: Verify that workers have consumed food
        assertTrue(village.getFood() < initialFood, "Food should have been consumed by workers.");
    }

    @Test
    public void testWorkersStarveWhenNoFoodAvailable() {
        // Given: Initial conditions with workers and no food
        village.setFood(0);
        assertEquals(0, village.getFood(), "Initial condition failed: there should be no food.");

        // When: Simulating one day in the village
        //village.Day();

        // Then: Verify that workers have not consumed any food
        assertEquals(0, village.getFood(), "Food should remain unchanged when there is no food.");
        // And verify that workers are starving
        for (Worker worker : village.getWorkers()) {
            assertTrue(worker.isHungry(), "Worker should be hungry when there is no food available.");
        }
    }
}