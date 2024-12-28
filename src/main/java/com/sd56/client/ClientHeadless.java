package com.sd56.client;

import com.sd56.common.exceptions.ConnectionException;
import com.sd56.common.exceptions.DataFileException;
import com.sd56.common.exceptions.SDException;
import com.sd56.common.util.LockedResource;
import com.sd56.common.util.Nullable;
import com.sd56.common.util.logger.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.sd56.client.ClientDataFile.*;

public class ClientHeadless implements IClientImpl {
    private final ClientDataFile data;
    private final LockedResource<Object, ?> lr; // I'm abusing the LockedResource's condition because I'm a lazy fuck.
    private Client client;

    public ClientHeadless(Client client, String path) throws FileNotFoundException, DataFileException {
        this.client = client;
        this.data = ClientDataFile.readDataFile(path);
        this.lr = new LockedResource<>(null);
    }

    public LockedResource<Object, ?> getLockedResource() {
        return this.lr;
    }
    
    public void run() {
        Logger logger = this.client.getLogger();

        logger.debug("CDF: " + this.data.toString());

//        try {
//            logger.debug("Connecting to the server...");
//            this.client.tryConnect();
//            logger.success("Connection established.");
//        } catch (Exception e) {
//            logger.error("Error connecting to the server: " + SDException.getStackTrace(e));
//            return;
//        }

        logger.info("Authenticating with credentials " + this.data.username() + ":" + this.data.password());
        try {
//            if (!this.client.authenticate(this.data.username(), this.data.password())) {
//                logger.error("Authentication failed: Invalid credentials.");
//                return;
//            }

            logger.success("Authentication successful.");
        } catch (Exception e) {
            logger.error("Authentication failed: " + SDException.getStackTrace(e));
            return;
        }

        switch (this.data.method()) {
            case "GET": {
                this.get();
                break;
            }
            case "PUT": {
                this.put();
                break;
            }
            case "MULTIGET": {
                this.multiGet();
                break;
            }
            case "MULTIPUT": {
                this.multiPut();
                break;
            }
            case "GETWHEN": {
                this.getWhen();
                break;
            }
            default:
                logger.error("Invalid method: " + this.data.method());
        }

//        fuckingDie();
    }

    private void get() {
        Logger logger = this.client.getLogger();
        String key = this.data.payload().get(DATA_FILE_INLINABLE_SECTION_VALUE);
        byte[] control = this.data.control().get(DATA_FILE_INLINABLE_SECTION_VALUE).getBytes(StandardCharsets.UTF_8);

        logger.info("GET " + key + " STARTUP");
        try {
            ClientRequestQueueEntry<Object> entry = this.client.get(key);

            this.client.getEventEmmiter().once(String.valueOf(entry.getId()), (e) -> {
                entry.lock();
//                byte[] value = entry.exec(r -> r);
//                if (value == null) {
//                    logger.error("GET " + key + " FAILED");
//                    return;
//                }
//
//                logger.info("GET " + key + " STARTUP SUCCESS");
//                logger.debug("GET PAYLOAD: " + key + " -> " + new String(value, StandardCharsets.UTF_8));
//                logger.debug("GET CONTROL: " + key + " -> " + new String(control, StandardCharsets.UTF_8));
//
//                boolean matchControl = Arrays.equals(value, control);
//                if (matchControl) {
//                    logger.success("GET " + key + " MATCH");
//                } else {
//                    logger.error("GET " + key + " MISMATCH");
//                }

                if (!entry.succeeded()) {
                    if (entry.getError() != null) {
                        logger.error("GET " + key + " FAILED: " + SDException.getStackTrace(entry.getError()));
                    } else {
                        logger.error("GET " + key + " FAILED: " + entry.getResponse());
                    }

                    System.out.println("@SUCCESS: false");
                } else {
                    logger.info("GET " + key + " STARTUP SUCCESS");
                    logger.debug("GET PAYLOAD: " + key + " -> " + entry.getResponse());
                    logger.debug("GET CONTROL: " + key + " -> " + new String(control, StandardCharsets.UTF_8));

                    boolean matchControl = Objects.equals(entry.getResponse(), new String(control, StandardCharsets.UTF_8));
                    if (matchControl) {
                        logger.success("GET " + key + " MATCH");
                    } else {
                        logger.error("GET " + key + " MISMATCH");
                    }

                    System.out.println("@SUCCESS: " + matchControl);
                }
                entry.unlock();

                fuckingDie();
            });
        } catch (Exception e) {
            logger.error("GET " + key + " FAILED: " + SDException.getStackTrace(e));
        }
    }

    private void put() {
        Logger logger = this.client.getLogger();
        Map.Entry<String, String> payload = this.data.payload().entrySet().stream().findFirst().get();
        String key = payload.getKey();
        String value = payload.getValue();

        logger.info("PUT " + key + ":" + value + " STARTUP");
        try {
//            boolean validation = this.client.put(key, value);
//            if (!validation) {
//                logger.error("PUT " + key + " FAILED");
//                return;
//            }
//
//            logger.success("PUT " + key + " SUCCESS");

            ClientRequestQueueEntry<Object> entry = this.client.put(key, value);

            this.client.getEventEmmiter().once(String.valueOf(entry.getId()), (e) -> {
                entry.lock();
                if (!entry.succeeded()) {
                    if (entry.getError() != null) {
                        logger.error("PUT " + key + " FAILED: " + SDException.getStackTrace(entry.getError()));
                    } else {
                        logger.error("PUT " + key + " FAILED: " + entry.getResponse());
                    }

                    System.out.println("@SUCCESS: false");
                } else {
                    logger.info("PUT " + key + " SUCCESS");

                    System.out.println("@SUCCESS: true");
                }
                entry.unlock();

                fuckingDie();
            });
        } catch (Exception e) {
            logger.error("PUT " + key + " FAILED: " + SDException.getStackTrace(e));
        }
    }

    private void multiGet() {
        Logger logger = this.client.getLogger();
        Map<String, String> payload = this.data.payload();
        Map<String, String> control = this.data.control();

        logger.debug("PAYLOAD: " + payload);
        logger.debug("CONTROL: " + control);

        logger.info("MULTIGET " + payload.values() + " STARTUP");
        try {
//            Map<String, Nullable<byte[]>> values = this.client.multiGet(new HashSet<>(payload.values()));
//            for (Map.Entry<String, Nullable<byte[]>> entry : values.entrySet()) {
//                String key = entry.getKey();
//                Nullable<byte[]> value = entry.getValue();
//                String controlValue = control.get(key);
//
//                if (value.isNull()) {
//                    logger.error("MULTIGET " + key + " FAILED: Key does not exist.");
//                    continue;
//                }
//
//                logger.debug("MULTIGET PAYLOAD: " + key + " -> " + new String(value.get(), StandardCharsets.UTF_8));
//                logger.debug("MULTIGET CONTROL: " + key + " -> " + controlValue);
//
//                boolean matchControl = Arrays.equals(value.get(), controlValue.getBytes(StandardCharsets.UTF_8));
//                if (matchControl) {
//                    logger.success("MULTIGET " + key + " MATCH");
//                } else {
//                    logger.error("MULTIGET " + key + " MISMATCH");
//                }
//            }
            ClientRequestQueueEntry<Object> entry = this.client.multiGet(new HashSet<>(payload.values()));

            this.client.getEventEmmiter().once(String.valueOf(entry.getId()), (e) -> {
                entry.lock();
                if (!entry.succeeded()) {
                    if (entry.getError() != null) {
                        logger.error("MULTIGET " + payload.values() + " FAILED: " + SDException.getStackTrace(entry.getError()));
                    } else {
                        System.out.println("MULTIGET " + payload.values() + " FAILED: " + entry.getResponse());
                    }

                    System.out.println("@SUCCESS: false");
                } else {
                    boolean allMatch = true;

                    Map<String, byte[]> values = (Map<String, byte[]>)entry.getResponse();
                    for (Map.Entry<String, byte[]> _entry : values.entrySet()) {
                        if (_entry.getValue() == null) {
                            logger.error("Key " + _entry.getKey() + " doesn't exist.");
                        } else {
                            String controlValue = control.get(_entry.getKey());
                            String entryValue = new String(_entry.getValue(), StandardCharsets.UTF_8);

                            logger.debug("MULTIGET PAYLOAD: " + _entry.getKey() + " -> " + entryValue);
                            logger.debug("MULTIGET CONTROL: " + _entry.getKey() + " -> " + controlValue);

                            boolean matchControl = Objects.equals(entryValue, controlValue);
                            if (matchControl) {
                                logger.success("MULTIGET " + _entry.getKey() + " MATCH");
                            } else {
                                logger.error("MULTIGET " + _entry.getKey() + " MISMATCH");
                                allMatch = false;
                            }
                        }
                    }

                    System.out.println("@SUCCESS: " + allMatch);
                }
                entry.unlock();

                fuckingDie();
            });
        } catch (Exception e) {
            logger.error("MULTIGET FAILED: " + SDException.getStackTrace(e));
        }
    }

    private void multiPut() {
        Logger logger = this.client.getLogger();
        Map<String, String> payload = this.data.payload();

        logger.info("MULTIPUT " + payload + " STARTUP");
        try {
//            Map<String, Boolean> validation = this.client.multiPut(
//                    payload
//                            .entrySet()
//                            .stream()
//                            .collect(Collectors.toMap(
//                                    Map.Entry::getKey,
//                                    e -> e.getValue().getBytes(StandardCharsets.UTF_8)
//                            ))
//            );
//
//            for (Map.Entry<String, Boolean> entry : validation.entrySet()) {
//                String key = entry.getKey();
//                boolean value = entry.getValue();
//
//                if (!value) {
//                    logger.error("MULTIPUT " + key + " FAILED: Key does not exist.");
//                    continue;
//                }
//
//                logger.success("MULTIPUT " + key + " SUCCESS");
//            }
//
//            logger.success("MULTIPUT SUCCESS");

            ClientRequestQueueEntry<?> entry = client.multiPut(
                    payload
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    e -> e.getValue().getBytes(StandardCharsets.UTF_8)
                            ))
            );

            this.client.getEventEmmiter().once(String.valueOf(entry.getId()), (e) -> {
                entry.lock();
                if (!entry.succeeded()) {
                    if (entry.getError() != null) {
                        System.out.println("Error putting values: " + SDException.getStackTrace(entry.getError()));
                    } else {
                        System.out.println(entry.getResponse());
                    }

                    System.out.println("@SUCCESS: false");
                } else {
                    boolean allAdded = true;
                    Map<String, Boolean> validations = (Map<String, Boolean>)entry.getResponse();
                    for (Map.Entry<String, Boolean> validation : validations.entrySet()) {
                        if (validation.getValue()) {
                            System.out.println("Key '" + validation.getKey() + "' successfully added to the database.");
                        } else {
                            System.out.println("Key '" + validation.getKey() + "' not added to the database.");
                            allAdded = false;
                        }
                    }

                    System.out.println("@SUCCESS: " + allAdded);
                }
                entry.unlock();

                fuckingDie();
            });
        } catch (Exception e) {
            logger.error("MULTIPUT FAILED: " + SDException.getStackTrace(e));
        }
    }

    private void getWhen() {
        Logger logger = this.client.getLogger();
        Map<String, String> payload = this.data.payload();
        String key = payload.get(DATA_FILE_GETWHEN_KEY);
        String keyCond = payload.get(DATA_FILE_GETWHEN_KEYCOND);
        String _valueCond = payload.get(DATA_FILE_GETWHEN_VALUECOND);
        byte[] valueCond = _valueCond.getBytes(StandardCharsets.UTF_8);
        byte[] control = this.data.control().get(DATA_FILE_INLINABLE_SECTION_VALUE).getBytes(StandardCharsets.UTF_8);

        logger.info("GET " + key + " WHEN " + keyCond + "=" + _valueCond + " STARTUP");
        try {
            ClientRequestQueueEntry<?> entry = client.getWhen(key, keyCond, valueCond);

            this.client.getEventEmmiter().once(String.valueOf(entry.getId()), (e) -> {
                entry.lock();
                if (!entry.succeeded()) {
                    if (entry.getError() != null) {
                        logger.error("Error fetching values: " + SDException.getStackTrace(entry.getError()));
                    } else {
                        logger.error("" + entry.getResponse()); // Generics fuckery
                    }

                    System.out.println("@SUCCESS: false");
                } else {
                    if (entry.getResponse() == null) {
                        System.err.println("Invalid key");
                        return;
                    } else{
                        logger.debug(
                                "GET " + key + " WHEN " + keyCond + "=" + _valueCond
                                        + " | Value: " + new String((byte[])entry.getResponse(), StandardCharsets.UTF_8)
                        );
                        if (Arrays.equals((byte[])entry.getResponse(), control)) {
                            logger.success("GET " + key + " WHEN " + keyCond + "=" + _valueCond + " SUCCESS");
                            System.out.println("@SUCCESS: true");
                        } else {
                            logger.error("GET " + key + " WHEN " + keyCond + "=" + _valueCond + " FAILED");
                            System.out.println("@SUCCESS: false");
                        }
                    }
                }
                entry.unlock();

                fuckingDie();
            });
        } catch (Exception e) {
            logger.error("GET " + key + " WHEN " + keyCond + "=" + _valueCond + "FAILED: " + SDException.getStackTrace(e));
        }
    }

    public void fuckingDie() {
        try {
            this.client.close();
        } catch (Exception ignore) {}
    }
}
