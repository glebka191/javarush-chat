package com.javarush.task.task30.task3008.client;


import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected;

    public class SocketThread extends Thread{

        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage("участник с именем "+userName+" присоединился к чату");
        }

        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage("участник с именем "+userName+"  покинул чат");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected){

            synchronized (Client.this){
                Client.this.clientConnected = clientConnected;
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException{
            while (true){

                Message recievedMes = connection.receive();

                if (recievedMes.getType() == MessageType.NAME_REQUEST){
                    String sName = getUserName();
                    Message messageNewName = new Message(MessageType.USER_NAME,sName);
                    connection.send(messageNewName);
                }else if (recievedMes.getType() == MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    return;
                }else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {


            while (true) {

                Message recievedMes = connection.receive();

                if (recievedMes.getType() == MessageType.TEXT) {
                    processIncomingMessage(recievedMes.getData());
                } else if (recievedMes.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(recievedMes.getData());
                } else if (recievedMes.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(recievedMes.getData());
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        public void run(){
            String serverAddress = getServerAddress();
            int serverPort = getServerPort();
            
            try(Socket socket = new Socket(serverAddress,serverPort)) {
                Client.this.connection = new Connection(socket);

                clientHandshake();

                clientMainLoop();
            } catch (IOException | ClassNotFoundException cnfExp){
                notifyConnectionStatusChanged(false);
            }

        }

    }

    protected String getServerAddress(){
        return ConsoleHelper.readString();
    }

    protected int getServerPort(){
        return ConsoleHelper.readInt();
    }

    protected String getUserName(){
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole(){
        return true;
    }

    protected SocketThread getSocketThread(){
        return new SocketThread();
    }

    protected void sendTextMessage(String text){
        try{
            Message messageSent = new Message(MessageType.TEXT,text);
            connection.send(messageSent);
        } catch (IOException e) {
            e.printStackTrace();
            ConsoleHelper.writeMessage("Ошибка отправки сообщения.");
            clientConnected = false;
        }


    }

    public void run(){
        SocketThread socketThread = getSocketThread();

        socketThread.setDaemon(true);

        socketThread.start();

        synchronized (this){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Возникла ошибка на клиенте во время ожидания!");
            }

            if (clientConnected) System.out.println("Соединение установлено.\nДля выхода наберите команду 'exit'.");
            else System.out.println("Произошла ошибка во время работы клиента.");

            while (clientConnected){
                String strMes = ConsoleHelper.readString();
                if (strMes.equalsIgnoreCase("exit")) break;
                if (shouldSendTextFromConsole())sendTextMessage(strMes);
            }
        }


    }

    public static void main(String[] args){
        Client client = new Client();
        client.run();
    }


}
