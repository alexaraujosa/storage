package com.sd56.common.menu.CLI;

import java.util.List;

/**
 * This record represents an entry in the CLI. An entry can be a flag, a single value or a multivalue.
 * <p>
 * Flags are named arguments with no value attached (e.g. --flag).
 * <p>
 * Single values are named arguments with a single value attached (e.g. --name=value).
 * <p>
 * Multivalues are named arguments with multiple occurrences attached (e.g. --name=value1 --name=value2 --name=value3).
 */
public record CLIEntry(String name, boolean multiValue, Object value) {
    /**
     * Helper method to create a flag entry.
     * @param name The name of the flag.
     * @param value The value of the flag.
     */
    public static CLIEntry flag(String name, boolean value) {
        return new CLIEntry(name, false, value);
    }

    /**
     * Helper method to create a single value entry.
     * @param name The name of the single value.
     * @param value The value of the single value.
     */
    public static CLIEntry single(String name, String value) {
        return new CLIEntry(name, false, value);
    }

    /**
     * Helper method to create a multivalue entry.
     * @param name The name of the multivalue.
     * @param value The value of the multivalue.
     */
    public static CLIEntry multi(String name, List<String> value) {
        return new CLIEntry(name, true, value);
    }

    /**
     * Check if the entry is a flag.
     */
    public boolean isFlag() {
        return !multiValue && value instanceof Boolean;
    }

    /**
     * Check if the entry is a single value.
     */
    public boolean isSingle() {
        return !multiValue;
    }

    /**
     * Check if the entry is a multivalue.
     */
    public boolean isMulti() {
        return multiValue;
    }

    /**
     * Asserts that the entry is a flag and returns it's value, cast to the appropriate type.
     */
    public boolean asFlag() {
        if (multiValue) throw new IllegalStateException("Entry is a multivalue.");
        return (boolean)value;
    }

    /**
     * Asserts that the entry is a single value and returns it's value, cast to the appropriate type.
     */
    public Object asSingle() {
        if (multiValue) throw new IllegalStateException("Entry is a multivalue.");
        return (Object)value;
    }

    /**
     * Asserts that the entry is a multivalue and returns it's value, cast to the appropriate type.
     */
    public List<Object> asMulti() {
        if (!multiValue) throw new IllegalStateException("Entry is not a multivalue.");
        return (List<Object>)value;
    }
}
