package com.sd56.client;

import com.sd56.common.connectors.Demultiplexer;
import com.sd56.common.datagram.*;
import com.sd56.common.exceptions.ConnectionException;
import com.sd56.common.exceptions.SDException;
import com.sd56.common.util.event.Event;
import com.sd56.common.util.logger.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ClientHandler implements Runnable {
    private final int tag;
    private final Client client;
    private final Logger logger; // Here for convenience


    public ClientHandler(int tag, Client mainClient) {
        this.tag = tag;
        this.client = mainClient;
        this.logger = this.client.getLogger();
    }

    private ClientRequestQueueEntry<Object> getMessage() {
        ProtectedMessages messagesToSend = this.client.getMessagesToSend();
        messagesToSend.getLock().lock();

        try {
            while (messagesToSend.getMessagesToSend().isEmpty()) {
                logger.debug("Waiting for messages to be put in queue...");
                messagesToSend.getEmptyCondition().await();
            }

            logger.debug("Removed a message from the protected Messages in thread " + this.tag);
            return messagesToSend.getMessage();
        } catch (InterruptedException e) {
            logger.debug("Thread " + this.tag + " interrupted.");
            return null;
        } finally {
            messagesToSend.getLock().unlock();
        }
    }

    @Override
    public void run() {
        Demultiplexer demultiplexer = this.client.getDemultiplexer();

        while(true){
            ClientRequestQueueEntry<Object> message = this.getMessage();
            if (Thread.currentThread().isInterrupted() || message == null) return;

            logger.debug("[WK" + tag + "] MESSAGE: " + message);

            try {
                byte[] recivedBytes;
                Datagram header;
                Datagram dg = Datagram.deserialize(message.exec(r -> r));
                switch (dg.getType()) {
                    case DATAGRAM_TYPE_REQUEST_AUTHENTICATION: {
                        demultiplexer.send(this.tag, message.exec(r -> r));

                        logger.info("Awaiting for authentication...");

                        recivedBytes = demultiplexer.receive(this.tag);
                        header = Datagram.deserialize(recivedBytes);

                        logger.debug("[GENERAL] HEADER: " + header);
                        try {
                            switch (header.getType()) {
                                case DATAGRAM_TYPE_RESPONSE_AUTHENTICATION:
                                    ResponseAuthDatagram resAuth = ResponseAuthDatagram.deserialize(recivedBytes);
                                    logger.debug("[AUTH] DATAGRAM: " + resAuth);

                                    if (!resAuth.getValidation()) {
                                        message.setResponse("Invalid credentials.");
                                        message.setSuccess(false);
                                        break;
                                    }

                                    this.client.setAuthenticated(true);
                                    message.setResponse(true);
                                    break;
                                default:
                                    message.autoExec(lr -> {
                                        lr.setError(
                                                new ConnectionException("[AUTH] Received wrong datagram type: " + header.getType())
                                        );
                                        return null;
                                    });
                                    break;
                            }
                        } catch (Exception e) {
                            message.autoExec(lr -> {
                                lr.setError(
                                        new ConnectionException("[AUTH] Error on receive: " + SDException.getStackTrace(e))
                                );
                                return null;
                            });
                        } finally {
                            message.lock();
                            // Signal synchronous calls
                            message.signalAll();
                            // Signal asynchronous calls
                            this.client.getEventEmitter().emit(new Event(String.valueOf(message.getId()), null));
                            message.unlock();
                        }
                        break;
                    }
                    case DATAGRAM_TYPE_REQUEST_PUT: {
                        demultiplexer.send(this.tag, message.exec(r -> r));

                        recivedBytes = demultiplexer.receive(this.tag);
                        header = Datagram.deserialize(recivedBytes);

                        logger.debug("[WK" + this.tag + "] PUT HEADER: " + header);

                        try {
                            switch (header.getType()) {
                                case DATAGRAM_TYPE_RESPONSE_PUT:
                                    ResponsePutDatagram resPut = ResponsePutDatagram.deserialize(recivedBytes);

                                    if (!resPut.getValidation()) {
                                        message.setResponse("Unknown error.");
                                        message.setSuccess(false);
                                        break;
                                    }

                                    message.setResponse(true);
                                    break;
                                default:
                                    message.autoExec(lr -> {
                                        lr.setError(
                                                new ConnectionException("[PUT] Received wrong datagram type: " + header.getType())
                                        );
                                        return null;
                                    });
                                    break;
                            }
                        } catch (Exception e) {
                            message.autoExec(lr -> {
                                lr.setError(
                                        new ConnectionException("[PUT] Error on receive: " + SDException.getStackTrace(e))
                                );
                                return null;
                            });
                        } finally {
                            message.lock();
                            // Signal synchronous calls
                            message.signalAll();
                            // Signal asynchronous calls
                            this.client.getEventEmitter().emit(new Event(String.valueOf(message.getId()), null));
                            message.unlock();
                        }
                        break;
                    }
                    case DATAGRAM_TYPE_REQUEST_GET: {
                        demultiplexer.send(this.tag, message.exec(r -> r));

                        recivedBytes = demultiplexer.receive(this.tag);
                        header = Datagram.deserialize(recivedBytes);

                        try {
                            switch (header.getType()) {
                                case DATAGRAM_TYPE_RESPONSE_GET:
                                    ResponseGetDatagram resGet = ResponseGetDatagram.deserialize(recivedBytes);

                                    if (resGet.getValue() == null) {
                                        message.setResponse("Invalid key");
                                        message.setSuccess(false);
                                        return;
                                    } else {
                                        message.setResponse(new String(resGet.getValue(), StandardCharsets.UTF_8));
                                    }
                                    break;
                                default:
                                    message.autoExec(lr -> {
                                        lr.setError(
                                                new ConnectionException("[GET] Received wrong datagram type: " + header.getType())
                                        );
                                        return null;
                                    });
                                    break;
                            }
                        } catch (Exception e) {
                            message.autoExec(lr -> {
                                lr.setError(
                                        new ConnectionException("[GET] Error on receive: " + SDException.getStackTrace(e))
                                );
                                return null;
                            });
                        } finally {
                            message.lock();
                            // Signal synchronous calls
                            message.signalAll();
                            // Signal asynchronous calls
                            this.client.getEventEmitter().emit(new Event(String.valueOf(message.getId()), null));
                            message.unlock();
                        }
                        break;
                    }
                    case DATAGRAM_TYPE_REQUEST_MULTIGET: {
                        demultiplexer.send(this.tag, message.exec(r -> r));

                        recivedBytes = demultiplexer.receive(this.tag);
                        header = Datagram.deserialize(recivedBytes);

                        try {
                            switch (header.getType()) {
                                case DATAGRAM_TYPE_RESPONSE_MULTIGET:
                                    ResponseMultiGetDatagram resMultiGet = ResponseMultiGetDatagram.deserialize(recivedBytes);

                                    Map<String, byte[]> values = resMultiGet.getValues();
                                    message.setResponse(values);
                                    break;
                                default:
                                    message.autoExec(lr -> {
                                        lr.setError(
                                                new ConnectionException("[MULTIGET] Received wrong datagram type: " + header.getType())
                                        );
                                        return null;
                                    });
                                    break;
                            }
                        } catch (Exception e) {
                            message.autoExec(lr -> {
                                lr.setError(
                                        new ConnectionException("[MULTIGET] Error on receive: " + SDException.getStackTrace(e))
                                );
                                return null;
                            });
                        } finally {
                            message.lock();
                            // Signal synchronous calls
                            message.signalAll();
                            // Signal asynchronous calls
                            this.client.getEventEmitter().emit(new Event(String.valueOf(message.getId()), null));
                            message.unlock();
                        }
                        break;
                    }
                    case DATAGRAM_TYPE_REQUEST_MULTIPUT: {
                        demultiplexer.send(this.tag, message.exec(r -> r));

                        recivedBytes = demultiplexer.receive(this.tag);
                        header = Datagram.deserialize(recivedBytes);

                        try {
                            switch (header.getType()) {
                                case DATAGRAM_TYPE_RESPONSE_MULTIPUT:
                                    ResponseMultiPutDatagram resMultiPut = ResponseMultiPutDatagram.deserialize(recivedBytes);

                                    Map<String, Boolean> validations = resMultiPut.getValidations();
                                    message.setResponse(validations);
                                    break;
                                default:
                                    message.autoExec(lr -> {
                                        lr.setError(
                                                new ConnectionException("[MULTIPUT] Received wrong datagram type: " + header.getType())
                                        );
                                        return null;
                                    });
                                    break;
                            }
                        } catch (Exception e) {
                            message.autoExec(lr -> {
                                lr.setError(
                                        new ConnectionException("[MULTIPUT] Error on receive: " + SDException.getStackTrace(e))
                                );
                                return null;
                            });
                        } finally {
                            message.lock();
                            // Signal synchronous calls
                            message.signalAll();
                            // Signal asynchronous calls
                            this.client.getEventEmitter().emit(new Event(String.valueOf(message.getId()), null));
                            message.unlock();
                        }
                        break;
                    }
                    case DATAGRAM_TYPE_REQUEST_GETWHEN: {
                        demultiplexer.send(this.tag, message.exec(r -> r));

                        recivedBytes = demultiplexer.receive(this.tag);
                        header = Datagram.deserialize(recivedBytes);

                        try {
                            switch (header.getType()) {
                                case DATAGRAM_TYPE_RESPONSE_GETWHEN:
                                    ResponseGetWhenDatagram resGetWhen = ResponseGetWhenDatagram.deserialize(recivedBytes);

                                    message.setResponse(resGetWhen.getValue());
                                    break;
                                default:
                                    message.autoExec(lr -> {
                                        lr.setError(
                                                new ConnectionException("[MULTIGET] Received wrong datagram type: " + header.getType())
                                        );
                                        return null;
                                    });
                                    break;
                            }
                        } catch (Exception e) {
                            message.autoExec(lr -> {
                                lr.setError(
                                        new ConnectionException("[MULTIGET] Error on receive: " + SDException.getStackTrace(e))
                                );
                                return null;
                            });
                        } finally {
                            message.lock();
                            // Signal synchronous calls
                            message.signalAll();
                            // Signal asynchronous calls
                            this.client.getEventEmitter().emit(new Event(String.valueOf(message.getId()), null));
                            message.unlock();
                        }
                        break;
                    }
                    case DATAGRAM_TYPE_REQUEST_CLOSE: {
                        demultiplexer.send(this.tag, message.exec(r -> r));
                        demultiplexer.close();
                        this.client.closeWorkers();
                        System.exit(0);
                        break;
                    }
                    default:
                        System.out.println("Error converting Datagram in client Thread");
                        break;
                }
            } catch (Exception e) {
                logger.error("[WK" + this.tag + "] Uncaught exception: " + SDException.getStackTrace(e));
                throw new RuntimeException(e);
            }
        }
    }
}
