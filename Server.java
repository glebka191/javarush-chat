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
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();
                String userName = message.getData();


                if (message.getType() != MessageType.USER_NAME || userName.isEmpty() || connectionMap.containsKey(userName))
                    continue;

                connectionMap.put(userName, connection);
                connection.send(new Message(MessageType.NAME_ACCEPTED));

                return userName;
            }
        }

        private void notifyUsers(Connection connection, String userName) throws IOException{
            for (Map.Entry<String,Connection> pair : connectionMap.entrySet()){
                if (!pair.getKey().equals(userName)){
                    Message message = new Message(MessageType.USER_ADDED,pair.getKey());
                    connection.send(message);
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message messageReceived = connection.receive();
                if (messageReceived.getType() == MessageType.TEXT) {
                    Message messageSent = new Message(MessageType.TEXT, userName + ": " + messageReceived.getData());
                    sendBroadcastMessage(messageSent);
                } else {
                    ConsoleHelper.writeMessage("Ошибка отправления/получения");
                }
            }
        }

        public void run(){
            ConsoleHelper.writeMessage(socket.getRemoteSocketAddress().toString());

            String userName = null;

            try(Connection connection = new Connection(socket)) {

                userName = serverHandshake(connection);

                Message messageAdd = new Message(MessageType.USER_ADDED, userName);

                sendBroadcastMessage(messageAdd);

                notifyUsers(connection, userName);

                serverMainLoop(connection, userName);
            } catch (IOException | ClassNotFoundException ioe){
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом.");
            }
            if (userName != null) {
                if (!userName.isEmpty()) {
                    connectionMap.remove(userName);
                    Message messageRem = new Message(MessageType.USER_REMOVED, userName);
                    sendBroadcastMessage(messageRem);
                }
            }
            ConsoleHelper.writeMessage("Соединение закрыто!");

        }
    }

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(Message message){

        for (Map.Entry<String,Connection> pair : connectionMap.entrySet()){
            try {
                pair.getValue().send(message);
            } catch (IOException e) {
                e.printStackTrace();
                ConsoleHelper.writeMessage("Сообщение не отправленно");
            }
        }

    }
    public static void main(String[] args){

        int port;

        port = ConsoleHelper.readInt();
        ServerSocket serverSocket = null;
        Socket socket = null;


        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ConsoleHelper.writeMessage("Сервер запущен!");


            try {
                while (true) {
                    socket = serverSocket.accept();
                    Handler handler = new Handler(socket);
                    handler.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    serverSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    ConsoleHelper.writeMessage("Ошибка!");
                }
            }



    }
}
