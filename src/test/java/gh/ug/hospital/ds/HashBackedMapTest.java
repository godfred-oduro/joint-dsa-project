package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owner: 22160537
 *
 * Tests for HashBackedMap, the lookup the whole program leans on.
 */
public class HashBackedMapTest {

    @Test
    public void ordinaryUse_looksThingsUpByKey() {
        HashBackedMap<String, String> places = new HashBackedMap<>(101);
        places.put("L015", "A&E Triage Point");
        places.put("L013", "Pharmacy Dispensing Window");

        assertEquals("A&E Triage Point", places.get("L015"));
        assertTrue(places.containsKey("L013"));
        assertEquals(2, places.size());
    }

    @Test
    public void ordinaryUse_listsItsKeysAndValues() {
        HashBackedMap<String, Integer> beds = new HashBackedMap<>(31);
        beds.put("male", Integer.valueOf(26));
        beds.put("female", Integer.valueOf(26));
        beds.put("maternity", Integer.valueOf(24));

        assertEquals(3, beds.keys().size());
        assertEquals(3, beds.values().size());
    }

    @Test
    public void edge_replacingAValueDoesNotAddASecondEntry() {
        HashBackedMap<String, String> map = new HashBackedMap<>(11);
        map.put("Q001", "NEW");
        map.put("Q001", "COMPLETED");

        assertEquals(1, map.size());
        assertEquals("COMPLETED", map.get("Q001"));
    }

    @Test
    public void badInput_nullKeysAndMissingKeysAreRefused() {
        final HashBackedMap<String, String> map = new HashBackedMap<>(11);

        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                map.put(null, "value");
            }
        });
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                map.getOrFail("not stored");
            }
        });
    }
}
