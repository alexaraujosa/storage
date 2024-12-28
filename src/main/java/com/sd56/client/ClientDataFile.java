package com.sd56.client;

import com.sd56.common.exceptions.DataFileException;
import com.sd56.common.util.map.MetaHashMap;
import com.sd56.common.util.map.MetaMap;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public record ClientDataFile(String method, String username, String password, Map<String, String> payload, Map<String, String> control) {
    public static final String DATA_FILE_EXTENSION = ".sddat";

    private static final List<String> DATA_FILE_METHODS = new LinkedList<>(List.of(
            "GET", "PUT", "MULTIGET", "MULTIPUT", "GETWHEN"
    ));
    private static final List<String> DATA_FILE_INLINABLE_SECTIONS = new LinkedList<>(List.of(
            "PAYLOAD", "CONTROL"
    ));
    public static final String DATA_FILE_INLINABLE_SECTION_VALUE = "@VALUE";
    public static final String DATA_FILE_GETWHEN_KEY = "key";
    public static final String DATA_FILE_GETWHEN_KEYCOND = "keyCond";
    public static final String DATA_FILE_GETWHEN_VALUECOND = "valueCond";
    public static final String DATA_FILE_META_IS_ARRAY_KEY = "isArray";
    public static final String DATA_FILE_META_ARRAY_LEN_KEY = "arrayLen";

    public static boolean isDataFile(String path) {
        return path.endsWith(DATA_FILE_EXTENSION);
    }

    public static ClientDataFile processDataFile(Map<String, MetaMap<String, String>> data) throws DataFileException {
        // ------- Process GLOBAL section -------
        if (!data.containsKey("GLOBAL")) throw new DataFileException("Missing GLOBAL section.");

        Map<String, String> globalData = data.get("GLOBAL");
        if (!globalData.containsKey("USERNAME")) throw new DataFileException("Missing USERNAME.");
        if (!globalData.containsKey("PASSWORD")) throw new DataFileException("Missing PASSWORD.");

        // Method
        if (!globalData.containsKey("METHOD")) throw new DataFileException("Missing METHOD.");
        String method = globalData.get("METHOD");
        if (!DATA_FILE_METHODS.contains(method)) throw new DataFileException("Invalid METHOD.");

        // ------- Process PAYLOAD section -------
        if (!data.containsKey("PAYLOAD")) throw new DataFileException("Missing PAYLOAD section.");
        MetaMap<String, String> payloadData = data.get("PAYLOAD");

        if (payloadData.isEmpty()) throw new DataFileException("Payload must not be empty.");

        MetaMap<String, String> controlData = data.get("CONTROL");

        switch (method) {
            case "GET": {
                if (payloadData.size() > 1) throw new DataFileException("Method does not support multiple payloads.");
                if (payloadData.containsKeyMeta(DATA_FILE_META_IS_ARRAY_KEY)) {
                    if ((int)payloadData.getOrDefaultMeta(DATA_FILE_META_ARRAY_LEN_KEY, 2) > 1)
                        throw new DataFileException("Method does not support array payloads with more than one element.");

                    MetaMap<String, String> _payloadData = new MetaHashMap();
                    _payloadData.put(DATA_FILE_INLINABLE_SECTION_VALUE, payloadData.get("0"));
                    payloadData = _payloadData;
                } else {
                    if (!payloadData.containsKey(DATA_FILE_INLINABLE_SECTION_VALUE)) {
                        throw new DataFileException(
                                "Payload must be inlined or defined with "
                                        + DATA_FILE_INLINABLE_SECTION_VALUE + " meta."
                        );
                    }
                }
                if (controlData == null) throw new DataFileException("Missing CONTROL section.");
                if (controlData.size() > 1) throw new DataFileException("Method does not support multiple control values.");
                if (controlData.containsKeyMeta(DATA_FILE_META_IS_ARRAY_KEY)) {
                    if ((int)controlData.getOrDefaultMeta(DATA_FILE_META_ARRAY_LEN_KEY, 2) > 1)
                        throw new DataFileException("Method does not support array controls with more than one element.");

                    MetaMap<String, String> _controlData = new MetaHashMap();
                    _controlData.put(DATA_FILE_INLINABLE_SECTION_VALUE, controlData.get("0"));
                    controlData = _controlData;
                }

                return new ClientDataFile(
                        method,
                        globalData.get("USERNAME"),
                        globalData.get("PASSWORD"),
                        payloadData,
                        controlData
                );
            }
            case "PUT": {
                if (payloadData.size() > 1) throw new DataFileException("Method does not support multiple payloads.");

                return new ClientDataFile(
                        method,
                        globalData.get("USERNAME"),
                        globalData.get("PASSWORD"),
                        payloadData,
                        null
                );
            }
            case "MULTIGET": {
                if (!payloadData.containsKeyMeta(DATA_FILE_META_IS_ARRAY_KEY)) {
                    throw new DataFileException("Method requires array payload.");
                }

                if (controlData == null) throw new DataFileException("Missing CONTROL section.");
                for (Map.Entry<String, String> entry : payloadData.entrySet()) {
                    if (!controlData.containsKey(entry.getValue())) {
                        throw new DataFileException("Control value missing for payload key: " + entry.getValue());
                    }
                }

                return new ClientDataFile(
                        method,
                        globalData.get("USERNAME"),
                        globalData.get("PASSWORD"),
                        payloadData,
                        controlData
                );
            }
            case "MULTIPUT": {
                if (payloadData.containsKeyMeta(DATA_FILE_META_IS_ARRAY_KEY)) {
                    throw new DataFileException("Method does not support array payloads.");
                }

                if (payloadData.size() == 1 && payloadData.containsKey(DATA_FILE_INLINABLE_SECTION_VALUE)) {
                    throw new DataFileException("Method does not support inline payloads.");
                }

                return new ClientDataFile(
                        method,
                        globalData.get("USERNAME"),
                        globalData.get("PASSWORD"),
                        payloadData,
                        null
                );
            }
            case "GETWHEN": {
                if (payloadData.size() > 3) throw new DataFileException("Method requires compound data.");
                if (payloadData.containsKeyMeta(DATA_FILE_META_IS_ARRAY_KEY)) {
                    throw new DataFileException("Method does not support array payloads.");
                }
                if (!payloadData.containsKey(DATA_FILE_GETWHEN_KEY)) {
                    throw new DataFileException("Missing key in payload.");
                }
                if (!payloadData.containsKey(DATA_FILE_GETWHEN_KEYCOND)) {
                    throw new DataFileException("Missing keyCond in payload.");
                }
                if (!payloadData.containsKey(DATA_FILE_GETWHEN_VALUECOND)) {
                    throw new DataFileException("Missing valueCond in payload.");
                }
                if (controlData == null) throw new DataFileException("Missing CONTROL section.");
                if (controlData.size() > 1) throw new DataFileException("Method does not support multiple control values.");

                return new ClientDataFile(
                        method,
                        globalData.get("USERNAME"),
                        globalData.get("PASSWORD"),
                        payloadData,
                        controlData
                );
            }
            default: {
                throw new DataFileException("Invalid method.");
            }
        }
    }

    public static ClientDataFile readDataFile(String path) throws FileNotFoundException, DataFileException {
        if (!isDataFile(path)) throw new DataFileException("Not a data file.");

        File file = new File(path);
        if (!file.exists()) throw new FileNotFoundException("Data file does not exist.");

        FileInputStream fis = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        Map<String, MetaMap<String, String>> data = new HashMap<>();
        data.put("GLOBAL", new MetaHashMap<>());

        String line;
        String section = "GLOBAL";
        boolean sectionIsArrayCandidate = true;
        try {
            while ((line = br.readLine()) != null) {
                String clearLine = line.trim();

                if (clearLine.isEmpty()) continue; // Ignore empty lines
                else if (clearLine.startsWith("#")) continue; // Ignore comments
                else if (clearLine.startsWith("[") && clearLine.endsWith("]")) { // Section
                    section = clearLine.substring(1, clearLine.length() - 1);
                    if (!data.containsKey(section)) {
                        data.put(section, new MetaHashMap<>());
                        sectionIsArrayCandidate = true;
                    } else throw new DataFileException("Duplicate key for section: " + section);
                } else {
                    String[] parts = clearLine.split("=", 2);
//                    if (parts.length != 2) {
//                        throw new DataFileException("Invalid data file format.");
//                    }

                    // Array element
                    if (parts.length == 1) {
                        if (!sectionIsArrayCandidate) {
                            throw new DataFileException("Singleton entries not allowed on populated section.");
                        }

                        MetaMap<String, String> sectionData = data.get(section);
                        sectionData.put(String.valueOf(sectionData.size()), parts[0]);
                        sectionData.putIfAbsentMeta(DATA_FILE_META_IS_ARRAY_KEY, true);
                        sectionData.putMeta(
                                DATA_FILE_META_ARRAY_LEN_KEY,
                                (int)sectionData.getOrDefaultMeta(DATA_FILE_META_ARRAY_LEN_KEY, 0) + 1
                        );
                        continue;
                    }

                    if (sectionIsArrayCandidate && data.get(section).size() > 1) {
                        throw new DataFileException("Key-value pairs not allowed on array section.");
                    }

                    String key = parts[0].trim();
                    String value = parts[1].trim();

                    if (data.containsKey(section) && data.get(section).containsKey(key)) {
                        throw new DataFileException("Duplicate key in section " + section + ": " + key);
                    }

                    if (DATA_FILE_INLINABLE_SECTIONS.contains(key)) {
                        data.put(key, new MetaHashMap<>());
                        data.get(key).put(DATA_FILE_INLINABLE_SECTION_VALUE, value);
                    } else {
                        data.get(section).put(key, value);
                    }

                    sectionIsArrayCandidate = false;
                }
            }

            System.out.println("DATA: " + data.toString());

            br.close();
        } catch (IOException e) {
            throw new DataFileException("Error reading data file: " + e.getMessage());
        }

//        return new ClientDataFile();
        return processDataFile(data);
    }
}
