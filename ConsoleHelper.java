//вспомогательный класс, для чтения или записи в консоль.//


package com.javarush.task.task30.task3008;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    
    //выводим message В консоль//
    public static void writeMessage(String message){
        System.out.println(message);

    }
    
    //читаем строку с консоли, если исключение вывести текст ошибки, повторить ввод//
    public static String readString() {
        Boolean OK = false;
        String strret = null;
        while (!OK) {
            try {
                strret = reader.readLine();
                OK = true;
            } catch (IOException e) {
                System.out.println("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
            }
        }
        return strret;
    }

    //парсим число используя метод readString чтения со строки//
    public static int readInt(){
        try {
            return Integer.parseInt(readString());
        } catch (NumberFormatException e) {
            System.out.println("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
            return Integer.parseInt(readString());
        }
    }

}
