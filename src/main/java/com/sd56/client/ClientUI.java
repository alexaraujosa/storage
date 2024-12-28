package com.sd56.client;

import java.nio.charset.StandardCharsets;
import java.util.*;

import com.sd56.common.exceptions.ConnectionException;
import com.sd56.common.exceptions.SDException;
import com.sd56.common.menu.BetterMenu;
import com.sd56.common.util.logger.Logger;

public class ClientUI implements IClientImpl {
    private final Scanner sc;
    private final Client client;
    private final BetterMenu menu;

//    private final LockedResource<Queue<ClientRequestQueueEntry>> requestQueue; // Placed here for convenience
    private final Logger logger; // Placed here for convenience

    public ClientUI(Client client) {
        this.sc = new Scanner(System.in);
        this.client = client;
        this.logger = this.client.getLogger();
//        this.requestQueue = client.getRequestQueue();
        this.menu = new BetterMenu(new String[] {
                "Close Connection",
                "Authentication",
                "Send Get",
                "Send Put",
                "Send MultiGet",
                "Send MultiPut",
                "Send GetWhen"
        });
    }

    /**
     * Método que executa o menu principal.
     * Coloca a interface em execução.
     */
    public void run() {
//        try {
//            this.client.tryConnect();
//            System.out.println("Connection established.");
//        } catch (Exception e) {
//            System.out.println("Error connecting to the server: " + SDException.getStackTrace(e));
//            return;
//        }

        this.menu.setPreCondition(1, () -> !this.client.isAuthenticated());
        this.menu.setPreCondition(2, () -> this.client.isAuthenticated());
        this.menu.setPreCondition(3, () -> this.client.isAuthenticated());
        this.menu.setPreCondition(4, () -> this.client.isAuthenticated());
        this.menu.setPreCondition(5, () -> this.client.isAuthenticated());
        this.menu.setPreCondition(6, () -> this.client.isAuthenticated());

        this.menu.setHandler(0, this::close);
        this.menu.setHandler(1, this::authentication);
        this.menu.setHandler(2, this::get);
        this.menu.setHandler(3, this::put);
        this.menu.setHandler(4, this::multiGet);
        this.menu.setHandler(5, this::multiPut);
        this.menu.setHandler(6, this::getWhen);
        this.menu.run();
    }

    private void close() {
        this.menu.clearScreen();
        System.out.println("Closing the connection.");
        try {
            client.close();
        } catch (ConnectionException e) {
            logger.error("Error closing the connection: " + SDException.getStackTrace(e));
        }
    }

    private void authentication() {
        this.menu.clearScreen();
        System.out.println("Insert username:");
        String username = this.sc.nextLine();

        while (username.isEmpty()) {
            System.out.println("The username can't be empty. Try again:");
            username = this.sc.nextLine();
        }
        System.out.println("Insert password:");
        String password = this.sc.nextLine();

        while (password.isEmpty()) {
            System.out.println("The password can't be empty. Try again:");
            password = this.sc.nextLine();
        }

        try {
            ClientRequestQueueEntry<?> entry = client.authenticate(username, password);
            entry.lock();
            entry.await();
            logger.debug("[CUI] Authentication response: " + entry);
            if (!entry.succeeded()) {
                if (entry.getError() != null) {
                    throw entry.getError();
                } else {
                    System.out.println(entry.getResponse());
                }
            } else {
                System.out.println("Authentication successful.");
            }
            entry.unlock();
        } catch (Exception e) {
            logger.error("Authentication failed: " + SDException.getStackTrace(e));
        }
    }

    private void get() {
        this.menu.clearScreen();
        System.out.println("Insert key:");
        String key = this.sc.nextLine();

        while (key.isEmpty()) {
            System.out.println("The key can't be empty. Try again:");
            key = this.sc.nextLine();
        }

        try {
//            byte[] value = client.get(key);
//            System.out.println("Value: " + new String(value, StandardCharsets.UTF_8));
//            client.get(key);

            ClientRequestQueueEntry<?> entry = client.get(key);
            String finalKey = key;
            this.client.getEventEmmiter().once(String.valueOf(entry.getId()), (e) -> {
                logger.debug("[CUI] GET " + finalKey + " event: " + e);
                entry.lock();
                logger.debug("[CUI] GET " + finalKey + " response: " + entry);
                if (!entry.succeeded()) {
                    if (entry.getError() != null) {
                        System.out.println("Error getting value: " + SDException.getStackTrace(entry.getError()));
                    } else {
                        System.out.println(entry.getResponse());
                    }
                } else {
                    System.out.println("Key: " + finalKey + " | Value: " + entry.getResponse());
                }
                entry.unlock();
            });
        } catch (Exception e) {
            System.out.println("Error getting value: " + SDException.getStackTrace(e));
        }
    }

    private void put() {
        this.menu.clearScreen();
        System.out.println("Insert key:");
        String key = this.sc.nextLine();

        while (key.isEmpty()) {
            System.out.println("The key can't be empty. Try again:");
            key = this.sc.nextLine();
        }
        System.out.println("Insert value:");
        String value = this.sc.nextLine(); // conversao para byte[] deve ser feita no cliente   // TODO: Convert to byte[] here

        while (value.isEmpty()) {
            System.out.println("The value can't be empty. Try again:");
            value = this.sc.nextLine();
        }

        try {
//            boolean success = client.put(key, value);
//            if (!success) System.out.println("Invalid key.");
//            client.put(key, value);

            ClientRequestQueueEntry<?> entry = client.put(key, value);
            String finalKey = key;
            this.client.getEventEmmiter().once(String.valueOf(entry.getId()), (e) -> {
                logger.debug("[CUI] PUT " + finalKey + " event: " + e);
                entry.lock();
                logger.debug("[CUI] PUT " + finalKey + " response: " + entry);
                if (!entry.succeeded()) {
                    if (entry.getError() != null) {
                        System.out.println("Error putting value: " + SDException.getStackTrace(entry.getError()));
                    } else {
                        System.out.println(entry.getResponse());
                    }
                } else {
                    System.out.println("Key: " + finalKey + " successfully added to the database.");
                }
                entry.unlock();
            });
//            entry.lock();
//            entry.await();
//            logger.debug("[CUI] PUT " + key + ":" + value + ": " + entry);
//            if (!entry.succeeded()) {
//                if (entry.getError() != null) {
//                    throw entry.getError();
//                } else {
//                    System.out.println(entry.getResponse());
//                }
//            } else {
//                System.out.println("Authentication successful.");
//            }
//            entry.unlock();
        } catch (Exception e) {
            System.out.println("Error putting value: "+ SDException.getStackTrace(e));
        }
    }

    private void multiGet() {
        this.menu.clearScreen();
        Set<String> keys = new HashSet<>();
        System.out.println("How many keys do you wanna get?");
        int size = this.sc.nextInt();

        while (size <= 0) {
            System.out.println("The number of keys can't be negative or nil. Try again:");
            size = this.sc.nextInt();
        }
        this.sc.nextLine(); // Clear scanner

        for (int i = 0; i < size; i++) {
            System.out.println("Insert key: ");
            String key = this.sc.nextLine();

            while (key.isEmpty()) {
                System.out.println("The key can't be empty. Try again:");
                key = this.sc.nextLine();
            }
            keys.add(key);
        }

        try {
//            Map<String, Nullable<byte[]>> values = client.multiGet(keys);
//            for (Map.Entry<String, Nullable<byte[]>> entry : values.entrySet()) {
//                if (entry.getValue().isNull()) {
//                    System.out.println("Key " + entry.getKey() + " doesn't exist.");
//                } else {
//                    byte[] _value = entry.getValue().getOrDefault(new byte[0]);
//                    String value = _value.length > 0 ? new String(_value, StandardCharsets.UTF_8) : "null";
//
//                    System.out.println("Key: " + entry.getKey() + " | Value: " + value);
//                }
//            }
//            client.multiGet(keys);

            ClientRequestQueueEntry<?> entry = client.multiGet(keys);
            this.client.getEventEmmiter().once(String.valueOf(entry.getId()), (e) -> {
                logger.debug("[CUI] MULTIGET event: " + e);
                entry.lock();
                logger.debug("[CUI] MULTIGET response: " + entry);
                if (!entry.succeeded()) {
                    if (entry.getError() != null) {
                        System.out.println("Error getting values: " + SDException.getStackTrace(entry.getError()));
                    } else {
                        System.out.println(entry.getResponse());
                    }
                } else {
                    Map<String, byte[]> values = (Map<String, byte[]>)entry.getResponse();
                    for (Map.Entry<String, byte[]> _entry : values.entrySet()) {
//                        String value = new String(_entry.getValue(), StandardCharsets.UTF_8);
//                        System.out.println("Key: " + _entry.getKey() + " | Value: " + value);

                        if (_entry.getValue() == null) {
                            System.out.println("Key " + _entry.getKey() + " doesn't exist.");
                        } else {
                            System.out.println(
                                    "Key: " + _entry.getKey()
                                    + " || Value: " + new String(_entry.getValue(), StandardCharsets.UTF_8)
                            );
                        }
                    }
                }
                entry.unlock();
            });

//            entry.lock();
//            entry.await();
//            logger.debug("[CUI] Authentication response: " + entry);
//            if (!entry.succeeded()) {
//                if (entry.getError() != null) {
//                    throw entry.getError();
//                } else {
//                    System.out.println(entry.getResponse());
//                }
//            } else {
//                System.out.println("Authentication successful.");
//            }
//            entry.unlock();
        } catch (Exception e) {
            System.out.println("Error getting values: " + SDException.getStackTrace(e));
        }
    }

    private void multiPut() {
        this.menu.clearScreen();
        Map<String, byte[]> values = new HashMap<>();
        System.out.println("How many values do you wanna put?");
        int size = this.sc.nextInt();

        while (size <= 0) {
            System.out.println("The number of values can't be negative or nil. Try again:");
            size = this.sc.nextInt();
        }
        this.sc.nextLine(); // Clear scanner

        for (int i = 0; i < size; i++) {
            System.out.println("Insert key: ");
            String key = this.sc.nextLine();

            while (key.isEmpty()) {
                System.out.println("The key can't be empty. Try again:");
                key = this.sc.nextLine();
            }
            System.out.println("Insert value: ");
            String value = this.sc.nextLine();

            while (value.isEmpty()) {
                System.out.println("The value can't be empty. Try again:");
                value = this.sc.nextLine();
            }
            values.put(key, value.getBytes(StandardCharsets.UTF_8));
        }

        try {
//            Map<String, Boolean> validations = client.multiPut(values);
//            for (Map.Entry<String, Boolean> entry : validations.entrySet()) {
//                if (entry.getValue()) {
//                    System.out.println("Key '" + entry.getKey() + "' successfully added to the database.");
//                } else {
//                    System.out.println("Key '" + entry.getKey() + "' not added to the database.");
//                }
//            }
//            client.multiPut(values);

            ClientRequestQueueEntry<?> entry = client.multiPut(values);

            this.client.getEventEmmiter().once(String.valueOf(entry.getId()), (e) -> {
                logger.debug("[CUI] MULTIPUT event: " + e);
                entry.lock();
                logger.debug("[CUI] MULTIPUT response: " + entry);
                if (!entry.succeeded()) {
                    if (entry.getError() != null) {
                        System.out.println("Error putting values: " + SDException.getStackTrace(entry.getError()));
                    } else {
                        System.out.println(entry.getResponse());
                    }
                } else {
                    Map<String, Boolean> validations = (Map<String, Boolean>)entry.getResponse();
                    for (Map.Entry<String, Boolean> validation : validations.entrySet()) {
                        if (validation.getValue()) {
                            System.out.println("Key '" + validation.getKey() + "' successfully added to the database.");
                        } else {
                            System.out.println("Key '" + validation.getKey() + "' not added to the database.");
                        }
                    }
                }
                entry.unlock();
            });
        } catch (Exception e) {
            System.out.println("Error putting values: " + e.getMessage());
        }
    }

    private void getWhen() {
        this.menu.clearScreen();
        System.out.println("Insert key:");
        String key = this.sc.nextLine();

        while (key.isEmpty()) {
            System.out.println("The key can't be empty. Try again:");
            key = this.sc.nextLine();
        }

        System.out.println("Insert keyCond:");
        String keyCond = this.sc.nextLine();

        while (keyCond.isEmpty()) {
            System.out.println("The keyCond can't be empty. Try again:");
            keyCond = this.sc.nextLine();
        }

        System.out.println("Insert valueCond:");
        String valueCond = this.sc.nextLine();

        while (valueCond.isEmpty()) {
            System.out.println("The valueCond can't be empty. Try again:");
            valueCond = this.sc.nextLine();
        }

        try {
//            client.getWhen(key, keyCond, valueCond.getBytes(StandardCharsets.UTF_8));
//            byte[] value = client.getWhen(key, keyCond, valueCond.getBytes(StandardCharsets.UTF_8));
//            if (value == null) {
//                System.err.println("Invalid key");
//            } else {
//                System.out.println("Value: " + new String(value, StandardCharsets.UTF_8));
//            }

            ClientRequestQueueEntry<?> entry = client.getWhen(key, keyCond, valueCond.getBytes(StandardCharsets.UTF_8));
            String finalKey = key;
            String finalKeyCond = keyCond;
            String finalValueCond = valueCond;
            this.client.getEventEmmiter().once(String.valueOf(entry.getId()), (e) -> {
                logger.debug("[CUI] GETWHEN event: " + e);
                entry.lock();
                logger.debug("[CUI] GETWHEN response: " + entry);
                if (!entry.succeeded()) {
                    if (entry.getError() != null) {
                        System.out.println("Error waiting for value: " + SDException.getStackTrace(entry.getError()));
                    } else {
                        System.out.println(entry.getResponse());
                    }
                } else {
                    if (entry.getResponse() == null) {
                        System.err.println("Invalid key");
                        return;
                    } else{
                        System.out.println(
                                "GET " + finalKey
                                        + " WHEN " + finalKeyCond + "=" + finalValueCond
                                        + " | Value: " + new String((byte[])entry.getResponse(), StandardCharsets.UTF_8)
                        );
                    }
                }
                entry.unlock();
            });
        } catch (Exception e) {
            System.out.println("Error getting value: " + e.getMessage());
        }
    }
}
