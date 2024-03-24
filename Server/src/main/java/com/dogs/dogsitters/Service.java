package com.dogs.dogsitters;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Base64;

//класс по сервисным общим операциям
public class Service {
    //подключение к MySQL серверу
    public static Connection GetConnection() throws ClassNotFoundException, SQLException {
        Class.forName(ApplicationProperties.getProperty(ApplicationProperties.DRIVER_SQL));
        return DriverManager.getConnection(ApplicationProperties.getProperty(ApplicationProperties.CONNECTION_STRING_SQL),
                ApplicationProperties.getProperty(ApplicationProperties.USER_SQL),
                ApplicationProperties.getProperty(ApplicationProperties.PASSWORD_SQL));
    }
    //Base64string в Blob
    public static SerialBlob Base64stringToBlob(String data) throws SerialException, SQLException, UnsupportedEncodingException {
        data=data.replace("\n", "");
        byte[] decodedBytes = Base64.getDecoder().decode(new String(data).getBytes("UTF-8"));
        return new SerialBlob(decodedBytes);
    }
    //Blob в Base64string
    public static String BlobToBase64string(Blob image) throws SQLException {
        byte[] bytes_img = image.getBytes(1, (int)image.length());
        byte[] bytes_img64 = Base64.getEncoder().encode(bytes_img);
        String result = new String(bytes_img64);
        result=result.replace("\n", "");
        return result;
    }
    //получение даты в текстовом формате (конвертация даты SQL в текст)
    public static String GetDateText(Date date){
        String result="";
        int year, month, day;
        LocalDate localDate = date.toLocalDate();
        day=localDate.getDayOfMonth();
        month=localDate.getMonthValue();
        year=localDate.getYear();
        if(day<10)result="0"+Integer.toString(day)+".";
        else result+=Integer.toString(day)+".";
        if(month<10)result+="0"+Integer.toString(month)+".";
        else result+=Integer.toString(month)+".";
        result+=Integer.toString(year);
        return result;
    }
    //получение возраста
    public static int GetAge(Date date){
        //вычисляем возраст
        LocalDate date_birth = date.toLocalDate();
        LocalDate date_now = LocalDate.now();
        int years=date_now.getYear()-date_birth.getYear();
        if(date_now.getDayOfMonth()<date_birth.getDayOfMonth() && date_now.getMonthValue()<=date_birth.getMonthValue())years--;
        return years;
    }
}
