package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class VillageInputTest {

    private DatabaseConnection databaseConnectionMock;
    private Village village;
    private final InputStream originalSystemIn = System.in;

    @BeforeEach
    public void beforeEach() {
        databaseConnectionMock = mock(DatabaseConnection.class);
        village = new Village();

        // Define the list of town names
        ArrayList<String> townNames = new ArrayList<>(Arrays.asList("Straw town", "Wheatfield town", "Greendale"));
        when(databaseConnectionMock.GetTownNames()).thenReturn(townNames);
    }

    @Test
    @DisplayName("Test the save function")
    public void testSave() {
        // Include both the village name and the confirmation in the input
        String input = "Straw town\ny\n"; // Add \n to separate inputs as if they were entered in the console
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        when(databaseConnectionMock.SaveVillage(any(Village.class), any(String.class))).thenReturn(true);

        VillageInput villageInput = new VillageInput(village, databaseConnectionMock);
        villageInput.Save();

        verify(databaseConnectionMock).SaveVillage(any(Village.class), any(String.class));

        // Restore System.in after the test
        System.setIn(originalSystemIn);
    }

    @Test
    @DisplayName("Test the load function")
    public void testLoad() {
        // Simulate user input by redirecting System.in to a ByteArrayInputStream containing the village name.
        String villageName = "Wheatfield town";
        System.setIn(new ByteArrayInputStream(villageName.getBytes()));

        // Mock the behavior of the LoadVillage method to return a new Village instance when called with the specified village name.
        when(databaseConnectionMock.LoadVillage(villageName)).thenReturn(new Village());

        // Instantiate VillageInput with the mocked DatabaseConnection and a new Village object.
        VillageInput villageInput = new VillageInput(village, databaseConnectionMock);
        // Call the Load method, which is expected to use the mocked DatabaseConnection to load a village.
        villageInput.Load();

        // Verify that the LoadVillage method was called on the mocked DatabaseConnection with the specified village name.
        verify(databaseConnectionMock).LoadVillage(villageName);

        // Restore the original System.in stream after the test is complete to avoid affecting other tests.
        System.setIn(originalSystemIn);
    }
}