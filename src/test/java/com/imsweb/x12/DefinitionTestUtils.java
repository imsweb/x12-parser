package com.imsweb.x12;

import java.lang.reflect.Field;
import java.util.List;

import com.imsweb.x12.mapping.CompositeDefinition;
import com.imsweb.x12.mapping.ElementDefinition;
import com.imsweb.x12.mapping.LoopDefinition;
import com.imsweb.x12.mapping.SegmentDefinition;

/**
 * Builds mapping definitions for tests; the definition classes are only populated by XStream and have no setters.
 */
final class DefinitionTestUtils {

    private DefinitionTestUtils() {
    }

    static ElementDefinition element(String xid, String name) {
        ElementDefinition def = new ElementDefinition();
        set(def, "_xid", xid);
        set(def, "_name", name);
        return def;
    }

    static CompositeDefinition composite(String xid, String name) {
        CompositeDefinition def = new CompositeDefinition();
        set(def, "_xid", xid);
        set(def, "_name", name);
        return def;
    }

    static SegmentDefinition segment(String xid, String name, String pos, List<ElementDefinition> elements) {
        return segment(xid, name, pos, elements, null);
    }

    static SegmentDefinition segment(String xid, String name, String pos, List<ElementDefinition> elements, List<CompositeDefinition> composites) {
        SegmentDefinition def = new SegmentDefinition();
        set(def, "_xid", xid);
        set(def, "_name", name);
        set(def, "_pos", pos);
        set(def, "_elements", elements);
        set(def, "_composites", composites);
        return def;
    }

    static LoopDefinition loop(String xid, String name, String pos, List<SegmentDefinition> segments, List<LoopDefinition> loops) {
        LoopDefinition def = new LoopDefinition();
        set(def, "_xid", xid);
        set(def, "_name", name);
        set(def, "_pos", pos);
        set(def, "_segment", segments);
        set(def, "_loop", loops);
        return def;
    }

    private static void set(Object target, String field, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        }
        catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
