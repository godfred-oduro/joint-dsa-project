package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owner: 22399601
 *
 * Tests for DynamicArray. Three kinds, as the brief asks: ordinary use, the
 * edges of what is allowed, and things that must fail.
 */
public class DynamicArrayTest {

    @Test
    public void ordinaryUse_addsAndReadsBack() {
        DynamicArray<String> places = new DynamicArray<>();

        places.add("L001");
        places.add("L015");
        places.add("L038");

        assertEquals(3, places.size());
        assertEquals("L015", places.get(1));
        assertTrue(places.contains("L038"));
        assertEquals(1, places.indexOf("L015"));
    }

    @Test
    public void edge_growsItselfWhenFull() {
        // Start with room for two, then add ten. It has to grow three times:
        // 2 becomes 4, 4 becomes 8, 8 becomes 16.
        DynamicArray<Integer> numbers = new DynamicArray<>(2);

        for (int i = 0; i < 10; i++) {
            numbers.add(Integer.valueOf(i));
        }

        assertEquals(10, numbers.size());
        assertEquals(16, numbers.capacity());
        assertEquals(3, numbers.getGrowCount());
        assertEquals(Integer.valueOf(9), numbers.get(9));
    }

    @Test
    public void edge_removingFromTheMiddleShufflesTheRestBack() {
        DynamicArray<String> wards = new DynamicArray<>();
        wards.add("male");
        wards.add("female");
        wards.add("maternity");

        String removed = wards.remove(1);

        assertEquals("female", removed);
        assertEquals(2, wards.size());
        assertEquals("maternity", wards.get(1));
    }

    @Test
    public void badInput_readingPastTheEndFails() {
        final DynamicArray<String> places = new DynamicArray<>();
        places.add("L001");

        // Position 5 does not exist. Java would let a plain array crash with
        // its own error; ours says which position and how many there are.
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                places.get(5);
            }
        });
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                places.get(-1);
            }
        });
    }
}
