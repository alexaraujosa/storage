package com.sd56.client;

import com.sd56.common.connectors.Demultiplexer;
import com.sd56.common.datagram.*;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Queue;

public class ClientHandler implements Runnable {
    private final int tag;
    private Client client;


    public ClientHandler(int tag, Client mainClient) {
        this.tag = tag;
        this.client = mainClient;
    }

    private byte[] getMessage(){
        ProtectedMessages messagesToSend = this.client.getMessagesToSend();
        messagesToSend.getLock().lock();
        try{
            while (messagesToSend.getMessagesToSend().isEmpty()){
                System.out.println("Waiting for messages to be put in queue...");
                messagesToSend.getEmptyCondition().await();
            }
            System.out.println("Removed a message from the protected Messages in thread " + this.tag);
            return messagesToSend.getMessage();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            messagesToSend.getLock().unlock();
        }
    }

    @Override
    public void run() {
        Demultiplexer demultiplexer = this.client.getDemultiplexer();

        while(true){
            byte[] message = this.getMessage();

            try {
                byte[] recivedBytes;
                Datagram header;
                Datagram dg = Datagram.deserialize(message);
                switch (dg.getType()){
                    case DATAGRAM_TYPE_REQUEST_AUTHENTICATION:
                        demultiplexer.send(this.tag, message);

                        System.out.println("Awaiting for authentication...");

                        recivedBytes = demultiplexer.receive(this.tag);
                        header = Datagram.deserialize(recivedBytes);

                        System.out.println(header);

                        switch (header.getType()) {
                            case DATAGRAM_TYPE_RESPONSE_AUTHENTICATION:
                                ResponseAuthDatagram resAuth = ResponseAuthDatagram.deserialize(recivedBytes);
                                System.out.println(resAuth);
                                if (!resAuth.getValidation()) {
                                    System.err.println("Invalid credencials.");
                                    return;
                                }
                                this.client.setAuthenticated(true);
                                break;
                            default:
                                System.out.println("Recived wrong datagram type: " + header.getType());
                                break;
                        }
                        break;
                    case DATAGRAM_TYPE_REQUEST_PUT:
                        demultiplexer.send(this.tag, message);

                        recivedBytes = demultiplexer.receive(this.tag);
                        header = Datagram.deserialize(recivedBytes);

                        switch (header.getType()) {
                            case DATAGRAM_TYPE_RESPONSE_PUT:
                                ResponsePutDatagram resPut = ResponsePutDatagram.deserialize(recivedBytes);
                                if (!resPut.getValidation()) {
                                    System.err.println("The operation put could not succeed.");
                                    return;
                                }
                                break;
                            default:
                                break;
                        }
                        break;
                    case DATAGRAM_TYPE_REQUEST_GET:
                        demultiplexer.send(this.tag, message);

                        recivedBytes = demultiplexer.receive(this.tag);
                        header = Datagram.deserialize(recivedBytes);

                        switch (header.getType()) {
                            case DATAGRAM_TYPE_RESPONSE_GET:
                                ResponseGetDatagram resGet = ResponseGetDatagram.deserialize(recivedBytes);
                                if (resGet.getValue() == null) {
                                    System.err.println("Invalid key");
                                    return;
                                } else{
                                    System.out.println("Value: " + new String(resGet.getValue(), StandardCharsets.UTF_8));
                                }
                                break;
                            default:
                                break;
                        }
                        break;
                    case DATAGRAM_TYPE_REQUEST_MULTIGET:
                        demultiplexer.send(this.tag, message);

                        recivedBytes = demultiplexer.receive(this.tag);
                        header = Datagram.deserialize(recivedBytes);

                        switch (header.getType()) {
                            case DATAGRAM_TYPE_RESPONSE_MULTIGET:
                                ResponseMultiGetDatagram resMultiGet = ResponseMultiGetDatagram.deserialize(recivedBytes);
                                Map<String, byte[]> values = resMultiGet.getValues();
                                for (Map.Entry<String, byte[]> entry : values.entrySet()) {
                                    if (entry.getValue() == null) {
                                        System.out.println("Key doesn't exist.");
                                    } else {
                                        System.out.println("Key: " + entry.getKey() + " || Value: " + new String(entry.getValue(), StandardCharsets.UTF_8));
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                        break;
                    case DATAGRAM_TYPE_REQUEST_MULTIPUT:
                        demultiplexer.send(this.tag, message);

                        recivedBytes = demultiplexer.receive(this.tag);
                        header = Datagram.deserialize(recivedBytes);
                        switch (header.getType()) {
                            case DATAGRAM_TYPE_RESPONSE_MULTIPUT:
                                ResponseMultiPutDatagram resMultiPut = ResponseMultiPutDatagram.deserialize(recivedBytes);
                                Map<String, Boolean> validations = resMultiPut.getValidations();

                                for (Map.Entry<String, Boolean> entry : validations.entrySet()) {
                                    if (entry.getValue()) {
                                        System.out.println("Key '" + entry.getKey() + "' successfully added to the database.");
                                    } else {
                                        System.out.println("Key '" + entry.getKey() + "' not added to the database.");
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                        break;
                    case DATAGRAM_TYPE_REQUEST_GETWHEN:
                        demultiplexer.send(this.tag, message);

                        recivedBytes = demultiplexer.receive(this.tag);
                        header = Datagram.deserialize(recivedBytes);
                        //System.out.println("\nDATAGRAM_TYPE: " + datagram.getType());
                        switch (header.getType()){
                            case DATAGRAM_TYPE_RESPONSE_GETWHEN:
                                ResponseGetWhenDatagram resGetWhen = ResponseGetWhenDatagram.deserialize(recivedBytes);
                                //System.out.println("\nRESPONSE_GETWHEN VALUE: " + Arrays.toString(resGetWhen.getValue()));
                                if (resGetWhen.getValue() == null) {
                                    System.err.println("Invalid key");
                                    return;
                                } else{
                                    System.out.println("Value: " + new String(resGetWhen.getValue(), StandardCharsets.UTF_8));
                                }
                                break;
                            default:
                                break;
                        }
                        break;
                    case DATAGRAM_TYPE_REQUEST_CLOSE:
                        demultiplexer.send(this.tag,message);
                        demultiplexer.close();
                        break;
                    default:
                        System.out.println("Error converting Datagram in client Thread");
                        break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }
}
