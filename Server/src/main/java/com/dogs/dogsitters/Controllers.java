package com.dogs.dogsitters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.SQLException;

//класс контроллеров
@Controller
public class Controllers {
    OperationsUser ClUser=new OperationsUser();
    //регистрация нового пользователя
    @RequestMapping(value = { "/RegUserController" }, method = RequestMethod.POST)
    public ResponseEntity<String> RegUserController(@RequestParam(defaultValue="") String email, @RequestParam(defaultValue="") String password, @RequestParam(defaultValue="") String date_birth, @RequestParam(defaultValue="false") boolean dogsitter) throws ClassNotFoundException, SQLException, JSONException {
        JSONObject response = new JSONObject();
        boolean check=ClUser.CheckUser(email);
        response.put("Result", !check);
        //если пользователя еще нет, то регистрация
        if(check==false) {
            int id_user=ClUser.RegNewUser(email, password, date_birth, dogsitter);
            response.put("IdUser", id_user);
        }
        else response.put("Message", "Пользователь с такой электронной почтой уже существует.");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(response.toString(), headers, HttpStatus.OK);
    }
    //авторизация пользователя
    @RequestMapping(value = { "/SignInUserController" }, method = RequestMethod.POST)
    public ResponseEntity<String> SignInUserController(@RequestParam(defaultValue="") String email, @RequestParam(defaultValue="") String password) throws ClassNotFoundException, SQLException, JSONException {
        JSONObject response = new JSONObject();
        int id_user=ClUser.CheckUserEmailPassword(email, password);
        if(id_user>0){
            response.put("Result", true);
            response.put("IdUser", id_user);
        }else{
            response.put("Result", false);
            response.put("Message", "Не правильно введен логин или пароль.");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(response.toString(), headers, HttpStatus.OK);
    }
    //получение личных данных пользователя
    @RequestMapping(value = { "/GetDataUserController" }, method = RequestMethod.POST)
    public ResponseEntity<String> GetDataUserController(@RequestParam(defaultValue="0") int id_user) {
        JSONObject response = ClUser.GetDataUser(id_user);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(response.toString(), headers, HttpStatus.OK);
    }
    //сохранение личных данных пользователя
    @RequestMapping(value = { "/SaveDataUserController" }, method = RequestMethod.POST)
    public ResponseEntity<String> SaveDataUserController(@RequestParam(defaultValue="0") int id_user, @RequestParam(defaultValue="") String name, @RequestParam(defaultValue="") String date_birth, @RequestParam(defaultValue="") String email,
                                                         @RequestParam(defaultValue="") String password, @RequestParam(defaultValue="") String about, @RequestParam(defaultValue="") String phone, @RequestParam(defaultValue="") String photo,
                                                         @RequestParam(defaultValue="false") Boolean dogsitter) {
        String message=ClUser.SaveDataUser(id_user, name, date_birth, email, password, phone, about, dogsitter, photo);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(message, headers, HttpStatus.OK);
    }
    //получение данных по собаке
    @RequestMapping(value = { "/GetDataDogController" }, method = RequestMethod.POST)
    public ResponseEntity<String> GetDataDogController(@RequestParam(defaultValue="0") int id_dog) {
        JSONObject response = ClUser.GetDataDog(id_dog);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(response.toString(), headers, HttpStatus.OK);
    }
    //добавление или изменение данных по собаке
    @RequestMapping(value = { "/AddEditDataDogController" }, method = RequestMethod.POST)
    public ResponseEntity<String> AddEditDataDogController(@RequestParam(defaultValue="0") int id_dog, @RequestParam(defaultValue="0") int oper, @RequestParam(defaultValue="0") int id_user, @RequestParam(defaultValue="") String name,
                                                           @RequestParam(defaultValue="") String date_birth, @RequestParam(defaultValue="") String breed, @RequestParam(defaultValue="") String about,
                                                           @RequestParam(defaultValue="") String image_str) {
        ClUser.AddEditDataDog(id_dog, oper, id_user, name, date_birth, breed, about, image_str);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>("", headers, HttpStatus.OK);
    }
    //получение данных по собакам пользователя
    @RequestMapping(value = { "/GetListDogsUserController" }, method = RequestMethod.POST)
    public ResponseEntity<String> GetListDogsUserController(@RequestParam(defaultValue="0") int id_user) {
        JSONObject response = ClUser.GetListDogsUser(id_user);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(response.toString(), headers, HttpStatus.OK);
    }
    //удаление данных по собаке
    @RequestMapping(value = { "/DeleteDogController" }, method = RequestMethod.POST)
    public ResponseEntity<String> DeleteDogController(@RequestParam(defaultValue="0") int id_dog) {
        ClUser.DeleteDog(id_dog);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>("", headers, HttpStatus.OK);
    }
    //добавление или изменение медицинских данных по собаке
    @RequestMapping(value = { "/AddEditMedicalDogController" }, method = RequestMethod.POST)
    public ResponseEntity<String> AddEditMedicalDogController(@RequestParam(defaultValue="0") int oper, @RequestParam(defaultValue="0") int id_medical,@RequestParam(defaultValue="0") int id_user,
                                                              @RequestParam(defaultValue="0") int id_dog, @RequestParam(defaultValue="") String date, @RequestParam(defaultValue="") String description) {
        ClUser.AddEditMedicalDog(oper, id_medical, id_user, id_dog, date, description);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>("", headers, HttpStatus.OK);
    }
    //получение списка медицинских данных о собаке
    @RequestMapping(value = { "/GetListMedicalDogController" }, method = RequestMethod.POST)
    public ResponseEntity<String> GetListMedicalDogController(@RequestParam(defaultValue="0") int id_dog) {
        JSONObject result=ClUser.GetListMedicalDog(id_dog);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(result.toString(), headers, HttpStatus.OK);
    }
    //получение определенных медицинских данных о собаке
    @RequestMapping(value = { "/GetMedicalDogController" }, method = RequestMethod.POST)
    public ResponseEntity<String> GetMedicalDogController(@RequestParam(defaultValue="0") int id_medical) {
        JSONObject result=ClUser.GetMedicalDog(id_medical);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(result.toString(), headers, HttpStatus.OK);
    }
    //удаление определенных медицинских данных о собаке
    @RequestMapping(value = { "/DeleteMedicalDogController" }, method = RequestMethod.POST)
    public ResponseEntity<String> DeleteMedicalDogController(@RequestParam(defaultValue="0") int id_medical) {
        ClUser.DeleteMedicalDog(id_medical);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>("", headers, HttpStatus.OK);
    }
    //получение списка статей пользователя
    @RequestMapping(value = { "/GetArticlesUserController" }, method = RequestMethod.POST)
    public ResponseEntity<String> GetArticlesUserController(@RequestParam(defaultValue="0") int id_user) {
        JSONObject result=ClUser.GetListArticlesUser(id_user);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(result.toString(), headers, HttpStatus.OK);
    }
    //удаление статьй
    @RequestMapping(value = { "/DeleteArticleController" }, method = RequestMethod.POST)
    public ResponseEntity<String> DeleteArticleController(@RequestParam(defaultValue="0") int id_article) {
        ClUser.DeleteArticle(id_article);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>("", headers, HttpStatus.OK);
    }
    //добавление или изменение статьй
    @RequestMapping(value = { "/AddEditArticleController" }, method = RequestMethod.POST)
    public ResponseEntity<String> AddEditArticleController(@RequestParam(defaultValue="0") int oper, @RequestParam(defaultValue="0") int id_article,@RequestParam(defaultValue="0") int id_user,
                                                              @RequestParam(defaultValue="") String title, @RequestParam(defaultValue="") String description) {
        ClUser.AddEditArtice(oper, id_article, id_user, title, description);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>("", headers, HttpStatus.OK);
    }
    //получение определенной статьй пользователя
    @RequestMapping(value = { "/GetArticleUserController" }, method = RequestMethod.POST)
    public ResponseEntity<String> GetArticleUserController(@RequestParam(defaultValue="0") int id_article) {
        JSONObject result=ClUser.GetDataArticle(id_article);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(result.toString(), headers, HttpStatus.OK);
    }
    //поиск статей
    @RequestMapping(value = { "/SearchArticlesController" }, method = RequestMethod.POST)
    public ResponseEntity<String> SearchArticlesController(@RequestParam(defaultValue="") String search) {
        JSONObject result=ClUser.SearchArticles(search);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(result.toString(), headers, HttpStatus.OK);
    }
    //получение списка догситтеров
    @RequestMapping(value = { "/GetDogsittersController" }, method = RequestMethod.POST)
    public ResponseEntity<String> GetDogsittersController() {
        JSONObject result=ClUser.GetListDogsitters();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(result.toString(), headers, HttpStatus.OK);
    }
    //получение списка координат избранных мест
    @RequestMapping(value = { "/GetFavoritePlacesController" }, method = RequestMethod.POST)
    public ResponseEntity<String> GetFavoritePlacesController(@RequestParam(defaultValue="0") int id_user) {
        JSONObject result=ClUser.GetListFavoritePlaces(id_user);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(result.toString(), headers, HttpStatus.OK);
    }
    //добавление или удаление избранного места
    @RequestMapping(value = { "/DoFavoritePlaceController" }, method = RequestMethod.GET)
    public ResponseEntity<String> DoFavoritePlaceController(@RequestParam(defaultValue="true") boolean favorite, @RequestParam(defaultValue="0") int id_user,
                                                            @RequestParam(defaultValue="0.0") double lat, @RequestParam(defaultValue="0.0") double lng) {
        ClUser.DoFavoritePlace(favorite, id_user, lat, lng);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>("", headers, HttpStatus.OK);
    }
    //установка оценки догситтеру
    @RequestMapping(value = { "/DoRatingDogsitterController" }, method = RequestMethod.POST)
    public ResponseEntity<String> DoRatingDogsitterController(@RequestParam(defaultValue="0") int id_dogsitter, @RequestParam(defaultValue="0") int rating) {
        JSONObject result=ClUser.DoRatingDogsitter(id_dogsitter, rating);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<String>(result.toString(), headers, HttpStatus.OK);
    }
}
