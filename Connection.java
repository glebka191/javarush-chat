//класс соединения между клиентом и сервером.
//Клиент и сервер будут общаться через сокетное соединение.
//Одна сторона будет записывать данные в сокет, а другая читать.
//Их общение представляет собой обмен сообщениями Message.
//Класс Connection будет выполнять роль обертки над классом java.net.Socket,
//которая должна будет уметь сериализовать и десериализовать объекты типа Message в сокет.
//Методы этого класса должны быть готовы к вызову из разных потоков

package com.javarush.task.task30.task3008;


import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;

public class Connection implements Closeable {
    public Connection(Socket socket) throws IOException {       //Конструктор, который принимает Socket в качестве параметра
        this.socket = socket;                                   //и инициализирует поля.
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;


    @Override
    public void close() throws IOException {        //переопределил метод и закрываем все ресурсы нашего класса
        out.close();
        in.close();
        socket.close();
    }

    public void send(Message message) throws IOException{   //записываем сообщение message в ObjectOutputStream
        synchronized(out) {                                 //и синхронизируем т.к. доступ будет из разных потоков
            out.writeObject(message);
        }
    }

    public Message receive() throws IOException, ClassNotFoundException{    //тоже самое только читаем
        synchronized(in) {
            return (Message) in.readObject();
        }
    }

    public SocketAddress getRemoteSocketAddress(){             //возвращает удаленный адрес сокетного соединения
        return socket.getRemoteSocketAddress();
    }
}
