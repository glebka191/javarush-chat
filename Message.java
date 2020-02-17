//класс, отвечающий за пересылаемые сообщения.//

package com.javarush.task.task30.task3008;

import java.io.Serializable;

public class Message implements Serializable {
    private final MessageType type;         //тип сообщения//
    private final String data;              //данные сообщения//

    public Message(MessageType type) {      //Конструктор принимающий тип сообщения, инициализирующий data из-за final//
        this.type = type;
        this.data = null;
    }

    public Message(MessageType type, String data) {     // 2-й конструктор//
        this.type = type;
        this.data = data;
    }

    public MessageType getType() {                  //get//
        return type;
    }

    public String getData() {                       //get//
        return data;
    }
}
