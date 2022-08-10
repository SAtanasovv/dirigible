package org.eclipse.dirigible.graalium.core.dirigible.globals;

import org.eclipse.dirigible.graalium.core.graal.globals.JSGlobalObject;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class DirigibleContextGlobalObject.
 */
public class DirigibleContextGlobalObject implements JSGlobalObject {

    /** The dirigible context value. */
    private final Map<Object, Object> dirigibleContextValue;

    /**
     * Instantiates a new dirigible context global object.
     *
     * @param dirigibleContextValue the dirigible context value
     */
    public DirigibleContextGlobalObject(Map<Object, Object> dirigibleContextValue) {
        this.dirigibleContextValue = dirigibleContextValue != null ? dirigibleContextValue : new HashMap<>();
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return "__context";
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    @Override
    public Object getValue() {
        return dirigibleContextValue;
    }
}
