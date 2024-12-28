package com.sd56.common.menu.CLI;

import java.util.List;

public record CLIEntry(String name, boolean multiValue, Object value) {
    public static CLIEntry flag(String name, boolean value) {
        return new CLIEntry(name, false, value);
    }

    public static CLIEntry single(String name, String value) {
        return new CLIEntry(name, false, value);
    }

    public static CLIEntry multi(String name, List<String> value) {
        return new CLIEntry(name, true, value);
    }

    public boolean isFlag() {
        return !multiValue && value instanceof Boolean;
    }

    public boolean isSingle() {
        return !multiValue;
    }

    public boolean isMulti() {
        return multiValue;
    }

    public boolean asFlag() {
        if (multiValue) throw new IllegalStateException("Entry is a multivalue.");
        return (boolean)value;
    }

    public Object asSingle() {
        if (multiValue) throw new IllegalStateException("Entry is a multivalue.");
        return (Object)value;
    }

    public List<Object> asMulti() {
        if (!multiValue) throw new IllegalStateException("Entry is not a multivalue.");
        return (List<Object>)value;
    }
}
