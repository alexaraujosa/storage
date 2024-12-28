package com.sd56.common.menu.CLI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CLI {
    private final Map<String, CLIEntry> entries;
    private final List<String> args;

    public CLI(String[] args) {
        ParsedArgs pArgs = CLI.parseArgs(args);
        this.entries = pArgs.parsedArgs;
        this.args = pArgs.remainingArgs;
    }

    public boolean has(String name) {
        return this.entries.containsKey(name);
    }

    public boolean getFlag(String name) {
        if (!this.entries.containsKey(name)) return false;
        return this.entries.get(name).asFlag();
    }

    public Object getSingle(String name) {
        return this.entries.get(name).asSingle();
    }

    public List<Object> getMulti(String name) {
        return List.of(this.entries.get(name).asMulti());
    }

    public List<String> getRemainingArgs() {
        return List.copyOf(this.args);
    }

    public String printParsedArgs() {
        return CLI.printParsedArgs(this.entries);
    }

    public static ParsedArgs parseArgs(String[] args) {
        Map<String, CLIEntry> parsedArgs = new HashMap<>();
        List<String> remainingArgs = null;

        int i = 0;
        for (i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                if (arg.contains("=")) {
                    String[] split = arg.split("=");
                    String name = split[0].substring(2);
                    String value = split[1];

                    if (parsedArgs.containsKey(name)) {
                        CLIEntry entry = parsedArgs.get(name);
                        if (entry.multiValue()) {
                            entry.asMulti().add(value);
                        } else {
                            parsedArgs.put(name, CLIEntry.multi(name, List.of((String)entry.value(), value)));
                        }
                    } else {
                        parsedArgs.put(name, CLIEntry.single(name, value));
                    }
                } else {
                    String name = arg.substring(2);

                    if (parsedArgs.containsKey(name)) {
                        CLIEntry entry = parsedArgs.get(name);
                        if (!entry.isFlag()) {
                            throw new IllegalArgumentException("Duplicate flag element.");
                        }
                    } else {
                        parsedArgs.put(name, CLIEntry.flag(name, true));
                    }
                }
            } else {
                break;
            }
        }

        remainingArgs = List.of(args).subList(i, args.length);

        return new ParsedArgs(parsedArgs, remainingArgs);
    }

    public static String printParsedArgs(Map<String, CLIEntry> parsedArgs) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, CLIEntry> entry : parsedArgs.entrySet()) {
            sb.append("  - ").append(entry.getKey()).append(": ");
            if (entry.getValue().multiValue()) {
                sb.append("\n");
                for (Object value : entry.getValue().asMulti()) {
                    sb.append("      - ").append(value).append("\n");
                }
            } else {
                sb.append(entry.getValue().asSingle());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static class ParsedArgs {
        public Map<String, CLIEntry> parsedArgs;
        public List<String> remainingArgs;

        public ParsedArgs(Map<String, CLIEntry> parsedArgs, List<String> remainingArgs) {
            this.parsedArgs = parsedArgs;
            this.remainingArgs = remainingArgs;
        }
    }
}
