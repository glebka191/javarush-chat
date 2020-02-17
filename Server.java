//Основной класс сервера//

package com.javarush.task.task30.task3008;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static class Handler extends Thread{
        private Socket socket;
        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{

            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));     //сформировать и отправить команду запроса имени пользователя
                Message message = connection.receive();                     //ответ клиента
                String userName = message.getData();


                if (message.getType() != MessageType.USER_NAME || userName.isEmpty() || connectionMap.containsKey(userName))    //проверить, что получена команда с именем пользователя
                    continue;

                connectionMap.put(userName, connection);                //Добавить нового пользователя и соединение с ним
                connection.send(new Message(MessageType.NAME_ACCEPTED));    //Отправить клиенту команду информирующую, что его имя принято

                return userName;    //Если какая-то проверка не прошла, заново запросить имя клиента
            }
        }

        private void notifyUsers(Connection connection, String userName) throws IOException{    //У каждого элемента получить имя клиента 
            for (Map.Entry<String,Connection> pair : connectionMap.entrySet()){      //Cформировать команду с типом USER_ADDED и полученным именем
                if (!pair.getKey().equals(userName)){   //проверка чтобы не отправить самому себе
                    Message message = new Message(MessageType.USER_ADDED,pair.getKey());
                    connection.send(message);                                              //Отправить сформированную команду через connection
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message messageReceived = connection.receive();         //Принимать сообщение клиента
                if (messageReceived.getType() == MessageType.TEXT) {    //если сообщения имеют тип TEXT,т о формируем новое текстовое сообщение
                    Message messageSent = new Message(MessageType.TEXT, userName + ": " + messageReceived.getData());
                    sendBroadcastMessage(messageSent);                  //Отправлять сформированное сообщение всем клиентам
                } else {
                    ConsoleHelper.writeMessage("Ошибка отправления/получения");
                }
            }
        }

        public void run(){
            ConsoleHelper.writeMessage(socket.getRemoteSocketAddress().toString()); //установлено новое соединение с удаленным адресом

            String userName = null;

            try(Connection connection = new Connection(socket)) {

                userName = serverHandshake(connection);

                Message messageAdd = new Message(MessageType.USER_ADDED, userName); //Рассылать всем участникам чата 

                sendBroadcastMessage(messageAdd);                                   //информацию об имени присоединившегося участника

                notifyUsers(connection, userName);

                serverMainLoop(connection, userName);
            } catch (IOException | ClassNotFoundException ioe){
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом.");
            }
            if (userName != null) {
                if (!userName.isEmpty()) {
                    connectionMap.remove(userName);
                    Message messageRem = new Message(MessageType.USER_REMOVED, userName);   //формируем сообщение о удалении username
                    sendBroadcastMessage(messageRem);                                       //отправляем всем
                }
            }
            ConsoleHelper.writeMessage("Соединение закрыто!");

        }
    }

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>(); //ключом будет имя клиента, а значением соединение с ним

    public static void sendBroadcastMessage(Message message){   //должен отправлять сообщение message всем соединениям из connectionMap

        for (Map.Entry<String,Connection> pair : connectionMap.entrySet()){
            try {
                pair.getValue().send(message);
            } catch (IOException e) {                           //ловим исключение сообщаем о ошибке
                e.printStackTrace();
                ConsoleHelper.writeMessage("Сообщение не отправленно");
            }
        }

    }
    public static void main(String[] args){

        int port;

        port = ConsoleHelper.readInt();         // запрашиваем порт с помощью метода класса ConsoleHelper
        ServerSocket serverSocket = null;
        Socket socket = null;


        try {
            serverSocket = new ServerSocket(port);      //Создаем серверный сокет используя порт из предыдущего пункта
        } catch (IOException e) {
            e.printStackTrace();
        }
        ConsoleHelper.writeMessage("Сервер запущен!");


            try {
                while (true) {
                    socket = serverSocket.accept();     //В бесконечном цикле принимать входящие сокетные соединения созданного серверного сокета
                    Handler handler = new Handler(socket);  //создавать и запускать новый поток
                    handler.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {                                       //если словили ошибку закрываем
                    serverSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();                    //вывести ошибку
                    ConsoleHelper.writeMessage("Ошибка!");    
                }
            }



    }
}
