package com.dogs.dogsitters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

//класс по операциям пользователя
public class OperationsUser {
    //проверка электронной почты и пароля при входе, возвращает id пользователя
    public int CheckUserEmailPassword(String email, String password) throws ClassNotFoundException {
        int id_user=0;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = Service.GetConnection();
            stmt = conn.prepareStatement("SELECT id,email,password FROM users");
            rs = stmt.executeQuery();
            while(rs.next())
                if(email.equals(rs.getString(2)) && password.equals(rs.getString(3))) {
                    id_user=rs.getInt(1);
                    break;
                }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return id_user;
    }
    //проверка на существование пользователя по логину, если true пользователь существует
    public boolean CheckUser(String email) throws ClassNotFoundException {
        boolean result=false;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = Service.GetConnection();
            stmt = conn.prepareStatement("SELECT email FROM users");
            rs = stmt.executeQuery();
            while(rs.next())
                if(email.equals(rs.getString(1))) {//если уже есть такой пользователь
                    result=true;
                    break;
                }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }
    //регистрация нового пользователя
    public int RegNewUser(String email, String password, String date_birth, boolean dogsitter) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int id_user=0;
        try {
            conn = Service.GetConnection();
            //вставка основных данных
            stmt = conn.prepareStatement("INSERT INTO users (name,date_birth,email,phone,dogsitter,password)VALUES(?,?,?,?,?,?)");
            stmt.setString(1, "");
            stmt.setDate(2, java.sql.Date.valueOf(date_birth));
            stmt.setString(3, email);
            stmt.setString(4, "");
            stmt.setBoolean(5, dogsitter);
            stmt.setString(6, password);
            stmt.execute();stmt.close();
            //получение макс. id пользователя
            stmt = conn.prepareStatement("SELECT MAX(id) AS max_id FROM users");
            rs = stmt.executeQuery();
            if(rs.next())id_user=rs.getInt("max_id");
            rs.close();stmt.close();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return id_user;
    }
    //получение личных данных пользователя в формате Json
    public JSONObject GetDataUser(int id_user){
        JSONObject result=new JSONObject();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = Service.GetConnection();
            stmt = conn.prepareStatement("SELECT name,date_birth,email,image,phone,dogsitter,about,password FROM users WHERE id=?");
            stmt.setInt(1, id_user);
            rs = stmt.executeQuery();
            if(rs.next()){
                result.put("name", rs.getString(1));
                result.put("date_birth", rs.getDate(2));
                result.put("date_birth_text", Service.GetDateText(rs.getDate(2)));
                result.put("age", Service.GetAge(rs.getDate(2)));
                result.put("email", rs.getString(3));
                Blob blob=rs.getBlob(4);
                if(blob!=null)result.put("photo", Service.BlobToBase64string(blob));
                else result.put("photo", "");
                result.put("phone", rs.getString(5));
                result.put("dogsitter", rs.getBoolean(6));
                if(rs.getString(7)!=null)result.put("about", rs.getString(7));
                else result.put("about", "");
                result.put("password", rs.getString(8));
                result.put("rating", GetRatingUser(conn, id_user));
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    //сохранение личных данных пользователя
    public String SaveDataUser(int id_user, String name, String date_birth, String email, String password, String phone, String about, boolean dogsitter, String photo){
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String result="";
        Boolean flag_update=true;
        try {
            conn = Service.GetConnection();
            //проверяем нет ли такого уже пользователя с email
            stmt = conn.prepareStatement("SELECT id FROM users WHERE email=?");
            stmt.setString(1, email);
            rs = stmt.executeQuery();
            if(rs.next())
                if(id_user!=rs.getInt(1)){
                    flag_update=false;
                    result="Пользователь с данной электронной почтой уже зарегестрирован в системе.";
                }
            rs.close();stmt.close();
            if(flag_update) {
                stmt = conn.prepareStatement("UPDATE users SET name=?,date_birth=?,email=?,password=?,phone=?,about=?,dogsitter=?,image=? WHERE id=?");
                stmt.setString(1, name);
                stmt.setDate(2, java.sql.Date.valueOf(date_birth));
                stmt.setString(3, email);
                stmt.setString(4, password);
                stmt.setString(5, phone);
                stmt.setString(6, about);
                stmt.setBoolean(7, dogsitter);
                if(photo.length()>0)stmt.setBlob(8, Service.Base64stringToBlob(photo));
                else stmt.setNull(8, java.sql.Types.BLOB);
                stmt.setInt(9, id_user);
                stmt.execute();
                stmt.close();
                result="Сохранение прошло успешно.";
            }
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    //получение данных по собаке в формате Json
    public JSONObject GetDataDog(int id_dog){
        JSONObject result=new JSONObject();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = Service.GetConnection();
            stmt = conn.prepareStatement("SELECT id,name,date_birth,breed,about,image FROM dogs WHERE id=?");
            stmt.setInt(1, id_dog);
            rs = stmt.executeQuery();
            if(rs.next()){
                result.put("id", rs.getInt(1));
                result.put("name", rs.getString(2));
                result.put("date_birth", rs.getDate(3));
                result.put("date_birth_text", Service.GetDateText(rs.getDate(3)));
                result.put("breed", rs.getString(4));
                result.put("about", rs.getString(5));
                result.put("age", Service.GetAge(rs.getDate(3)));
                Blob blob=rs.getBlob(6);
                if(blob!=null)result.put("image", Service.BlobToBase64string(blob));
                else result.put("image", "");
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    //добавление или изменение данных по собаке
    public void AddEditDataDog(int id_dog, int oper, int id_user, String name, String date_birth, String breed, String about, String image_str){
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = Service.GetConnection();
            switch (oper){
                case 1://добавление
                    stmt = conn.prepareStatement("INSERT INTO dogs (id_user,name,date_birth,breed,about,image)VALUES(?,?,?,?,?,?)");
                    stmt.setInt(1, id_user);
                    stmt.setString(2, name);
                    stmt.setDate(3, java.sql.Date.valueOf(date_birth));
                    stmt.setString(4, breed);
                    stmt.setString(5, about);
                    if(image_str.length()>0)stmt.setBlob(6, Service.Base64stringToBlob(image_str));
                    else stmt.setNull(6, java.sql.Types.BLOB);
                    stmt.execute();stmt.close();
                    break;
                case 2://изменение
                    stmt = conn.prepareStatement("UPDATE dogs SET name=?,date_birth=?,breed=?,about=?,image=? WHERE id=?");
                    stmt.setString(1, name);
                    stmt.setDate(2, java.sql.Date.valueOf(date_birth));
                    stmt.setString(3, breed);
                    stmt.setString(4, about);
                    if(image_str.length()>0)stmt.setBlob(5, Service.Base64stringToBlob(image_str));
                    else stmt.setNull(5, java.sql.Types.BLOB);
                    stmt.setInt(6, id_dog);
                    stmt.execute();stmt.close();
                    break;
            }
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    //получение списка данных по собаках пользователя
    public JSONObject GetListDogsUser(int id_user){
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        JSONArray array=new JSONArray();
        JSONObject result=new JSONObject();
        List<Integer> list_id_dogs=new ArrayList<>();
        try {
            conn = Service.GetConnection();
            stmt = conn.prepareStatement("SELECT id FROM dogs WHERE id_user=?");
            stmt.setInt(1, id_user);
            rs = stmt.executeQuery();
            while(rs.next())list_id_dogs.add(rs.getInt(1));
            rs.close();
            stmt.close();
            for(int i=0;i<list_id_dogs.size();i++){
                JSONObject obj=GetDataDog(list_id_dogs.get(i));
                array.put(obj);
            }
            result.put("array", array);
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    //удаление данных по собаке
    public void DeleteDog(int id_dog){
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = Service.GetConnection();
            //удаляем медицинские данные
            stmt = conn.prepareStatement("DELETE FROM medical_dogs WHERE id_dog=?");
            stmt.setInt(1, id_dog);
            stmt.execute();stmt.close();
            //удаляем данные по собаке
            stmt = conn.prepareStatement("DELETE FROM dogs WHERE id=?");
            stmt.setInt(1, id_dog);
            stmt.execute();stmt.close();
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    //добавление или изменение данных по медицинским данным собаки
    public void AddEditMedicalDog(int oper, int id_medical, int id_user, int id_dog, String date, String description){
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = Service.GetConnection();
            switch (oper){
                case 1://добавление
                    stmt = conn.prepareStatement("INSERT INTO medical_dogs (id_dog,id_user,date,description)VALUES(?,?,?,?)");
                    stmt.setInt(1, id_dog);
                    stmt.setInt(2, id_user);
                    stmt.setDate(3, java.sql.Date.valueOf(date));
                    stmt.setString(4, description);
                    stmt.execute();stmt.close();
                    break;
                case 2://изменение
                    stmt = conn.prepareStatement("UPDATE medical_dogs SET date=?,description=? WHERE id=?");
                    stmt.setDate(1, java.sql.Date.valueOf(date));
                    stmt.setString(2, description);
                    stmt.setInt(3, id_medical);
                    stmt.execute();stmt.close();
                    break;
            }
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    //получение медицинских данных собаки
    public JSONObject GetListMedicalDog(int id_dog){
        JSONObject result=new JSONObject();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        JSONArray array=new JSONArray();
        try {
            conn = Service.GetConnection();
            stmt = conn.prepareStatement("SELECT id,date,description FROM medical_dogs WHERE id_dog=? ORDER BY date DESC");
            stmt.setInt(1, id_dog);
            rs = stmt.executeQuery();
            while(rs.next()){
                JSONObject obj=new JSONObject();
                obj.put("id", rs.getInt(1));
                obj.put("date", Service.GetDateText(rs.getDate(2)));
                obj.put("description", rs.getString(3));
                array.put(obj);
            }
            rs.close();
            stmt.close();
            result.put("array", array);
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    //получение определенных медицинских данных о собаке
    public JSONObject GetMedicalDog(int id_medical){
        JSONObject result=new JSONObject();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = Service.GetConnection();
            stmt = conn.prepareStatement("SELECT date,description FROM medical_dogs WHERE id=?");
            stmt.setInt(1, id_medical);
            rs = stmt.executeQuery();
            if(rs.next()){
                result.put("date", rs.getDate(1));
                result.put("description", rs.getString(2));
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    //удаление определенных данных по собаке
    public void DeleteMedicalDog(int id_medical){
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = Service.GetConnection();
            stmt = conn.prepareStatement("DELETE FROM medical_dogs WHERE id=?");
            stmt.setInt(1, id_medical);
            stmt.execute();stmt.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    //получение данных по статье
    public JSONObject GetDataArticle(int id_article){
        JSONObject result=new JSONObject();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = Service.GetConnection();
            stmt = conn.prepareStatement("SELECT id_user,title,date,description FROM articles WHERE id=?");
            stmt.setInt(1, id_article);
            rs = stmt.executeQuery();
            if(rs.next()){
                result.put("id", id_article);
                result.put("id_user", rs.getInt(1));
                result.put("title", rs.getString(2));
                result.put("date", rs.getDate(3));
                result.put("date_text", Service.GetDateText(rs.getDate(3)));
                result.put("description", rs.getString(4));
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    //получение списка статей пользователя
    public JSONObject GetListArticlesUser(int id_user){
        JSONObject result=new JSONObject();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        JSONArray array=new JSONArray();
        List<Integer> list_id_articles=new ArrayList<>();
        try {
            conn = Service.GetConnection();
            stmt = conn.prepareStatement("SELECT id FROM articles WHERE id_user=? ORDER BY date DESC");
            stmt.setInt(1, id_user);
            rs = stmt.executeQuery();
            while(rs.next())list_id_articles.add(rs.getInt(1));
            rs.close();
            stmt.close();
            for(int i=0;i<list_id_articles.size();i++){
                JSONObject obj=GetDataArticle(list_id_articles.get(i));
                String description=obj.getString("description");
                if(description.length()>150){
                    description=description.substring(0, 150) + "...";
                    obj.put("description", description);
                }
                array.put(obj);
            }
            result.put("array", array);
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    //удаление статьй пользователя
    public void DeleteArticle(int id_article){
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = Service.GetConnection();
            stmt = conn.prepareStatement("DELETE FROM articles WHERE id=?");
            stmt.setInt(1, id_article);
            stmt.execute();stmt.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    //добавление или изменение данных по статье
    public void AddEditArtice(int oper, int id_artice, int id_user, String title, String description){
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = Service.GetConnection();
            switch (oper){
                case 1://добавление
                    java.sql.Date date = new java.sql.Date(Calendar.getInstance().getTime().getTime());
                    stmt = conn.prepareStatement("INSERT INTO articles (id_user,date,title,description)VALUES(?,?,?,?)");
                    stmt.setInt(1, id_user);
                    stmt.setDate(2, date);
                    stmt.setString(3, title);
                    stmt.setString(4, description);
                    stmt.execute();stmt.close();
                    break;
                case 2://изменение
                    stmt = conn.prepareStatement("UPDATE articles SET title=?,description=? WHERE id=?");
                    stmt.setString(1, title);
                    stmt.setString(2, description);
                    stmt.setInt(3, id_artice);
                    stmt.execute();stmt.close();
                    break;
            }
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    //поиск статей, если в search пустая строка, то все статьи
    public JSONObject SearchArticles(String search){
        JSONObject result=new JSONObject();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        JSONArray array=new JSONArray();
        try {
            conn = Service.GetConnection();
            if(search.length()==0)stmt = conn.prepareStatement("SELECT id,id_user,title,date,description FROM articles ORDER BY date DESC");
            else stmt = conn.prepareStatement("SELECT id,id_user,title,date,description FROM articles WHERE title LIKE '%" + search + "%' OR description LIKE '%" + search + "%' ORDER BY date DESC");
            rs = stmt.executeQuery();
            while(rs.next()){
                JSONObject obj=new JSONObject();
                obj.put("id", rs.getInt(1));
                obj.put("id_user", rs.getInt(2));
                obj.put("user", GetDataBriefUser(rs.getInt(2)));
                obj.put("title", rs.getString(3));
                obj.put("date", Service.GetDateText(rs.getDate(4)));
                String description=rs.getString(5);
                if(description.length()>150)description=description.substring(0, 150) + "...";
                obj.put("description", description);
                array.put(obj);
            }
            rs.close();stmt.close();
            result.put("array", array);
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    //получение краткой информации о пользователе
    private String GetDataBriefUser(int id_user){
        String result="";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = Service.GetConnection();
            stmt = conn.prepareStatement("SELECT name,date_birth FROM users WHERE id=?");
            stmt.setInt(1, id_user);
            rs = stmt.executeQuery();
            if(rs.next())result=rs.getString(1) + ", возраст " + Service.GetAge(rs.getDate(2)) + " лет";
            rs.close();stmt.close();
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    //получение списка догситтеров с сортировкой по возрасту
    public JSONObject GetListDogsitters(){
        JSONObject result=new JSONObject();
        JSONArray array=new JSONArray();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = Service.GetConnection();
            stmt = conn.prepareStatement("SELECT id,name,date_birth,email,phone FROM users WHERE dogsitter=1 ORDER BY date_birth");
            rs = stmt.executeQuery();
            while(rs.next()){
                JSONObject obj=new JSONObject();
                obj.put("id", rs.getInt(1));
                obj.put("name", rs.getString(2));
                obj.put("date_birth", Service.GetDateText(rs.getDate(3)));
                obj.put("age", Service.GetAge(rs.getDate(3)));
                obj.put("email", rs.getString(4));
                obj.put("phone", rs.getString(5));
                obj.put("rating", GetRatingUser(conn, rs.getInt(1)));
                array.put(obj);
            }
            rs.close();stmt.close();
            result.put("array", array);
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    //получение рейтинга догситтера
    private double GetRatingUser(Connection conn, int id_user){
        double result=0;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count=0;
        double sum=0;
        try {
            conn = Service.GetConnection();
            stmt = conn.prepareStatement("SELECT rating FROM rating_users WHERE id_user=?");
            stmt.setInt(1, id_user);
            rs = stmt.executeQuery();
            while (rs.next()){
                sum+=rs.getInt(1);
                count++;
            }
            if(count>0)result = sum / count;
            rs.close();stmt.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    //получение списка координат избранных мест
    public JSONObject GetListFavoritePlaces(int id_user){
        JSONObject result=new JSONObject();
        JSONArray array=new JSONArray();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = Service.GetConnection();
            stmt = conn.prepareStatement("SELECT lat,lng FROM favorite_places WHERE id_user=?");
            stmt.setInt(1, id_user);
            rs = stmt.executeQuery();
            while(rs.next()){
                JSONObject obj=new JSONObject();
                obj.put("lat", rs.getDouble(1));
                obj.put("lng", rs.getDouble(2));
                array.put(obj);
            }
            rs.close();stmt.close();
            result.put("array", array);
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    //добавление или изменение избранного места в базе данных
    public void DoFavoritePlace(boolean favorite, int id_user, double lat, double lng){
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = Service.GetConnection();
            if(favorite) stmt = conn.prepareStatement("INSERT INTO favorite_places (id_user,lat,lng)VALUES(?,?,?)");
            else stmt = conn.prepareStatement("DELETE FROM favorite_places WHERE id_user=? AND lat=? AND lng=?");
            stmt.setInt(1, id_user);
            stmt.setDouble(2, lat);
            stmt.setDouble(3, lng);
            stmt.execute();stmt.close();
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    //установка рейтинга догситтеру
    public JSONObject DoRatingDogsitter(int id_dogsitter, int rating){
        JSONObject result=new JSONObject();
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = Service.GetConnection();
            stmt = conn.prepareStatement("INSERT INTO rating_users (id_user,rating)VALUES(?,?)");
            stmt.setInt(1, id_dogsitter);
            stmt.setInt(2, rating);
            stmt.execute();stmt.close();
            result.put("rating", GetRatingUser(conn, id_dogsitter));
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
