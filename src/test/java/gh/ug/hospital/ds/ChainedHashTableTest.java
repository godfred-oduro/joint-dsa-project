package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owner: 22370298
 *
 * Tests for ChainedHashTable, which handles clashes by hanging a small list off each slot.
 */
public class ChainedHashTableTest {

    @Test
    public void ordinaryUse_storesAndReadsBack() {
        ChainedHashTable<String, String> cards = new ChainedHashTable<>(101);
        cards.put("CARD-001", "L015");
        cards.put("CARD-002", "L038");

        assertEquals("L015", cards.get("CARD-001"));
        assertTrue(cards.containsKey("CARD-002"));
        assertNull(cards.get("CARD-999"));
        assertEquals(2, cards.size());
    }

    @Test
    public void ordinaryUse_puttingTheSameKeyTwiceReplacesTheValue() {
        ChainedHashTable<String, String> table = new ChainedHashTable<>(101);
        table.put("Q001", "NEW");
        table.put("Q001", "ASSIGNED");

        assertEquals("ASSIGNED", table.get("Q001"));
        assertEquals(1, table.size());
    }

    @Test
    public void edge_forcedCollisionsStillFindEverything() {
        // One slot only, so every key collides. It gets slow, but nothing is
        // lost. That is the difference between a collision and a bug.
        ChainedHashTable<String, Integer> crowded = new ChainedHashTable<>(1);
        for (int i = 0; i < 20; i++) {
            crowded.put("key" + i, Integer.valueOf(i));
        }

        assertEquals(20, crowded.size());
        assertEquals(19, crowded.getCollisions());
        assertEquals(20, crowded.longestChain());
        assertEquals(Integer.valueOf(13), crowded.get("key13"));
    }

    @Test
    public void badInput_nullKeysAndMissingKeysAreRefused() {
        final ChainedHashTable<String, String> table = new ChainedHashTable<>(11);

        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                table.put(null, "value");
            }
        });
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                table.remove("never stored");
            }
        });
    }
}
