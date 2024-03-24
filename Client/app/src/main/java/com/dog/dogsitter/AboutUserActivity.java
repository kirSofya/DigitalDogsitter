package com.dog.dogsitter;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

public class AboutUserActivity extends AppCompatActivity {
    int IdUser=0;
    TextView TextViewName, TextViewDateBirth, TextViewAge, TextViewEmail, TextViewPhone, TextViewDogsitter, TextViewAbout;
    ImageView ImageViewPhoto;
    Button ButtonDogs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_user);
        TextViewName=findViewById(R.id.textViewNameAboutUser);
        TextViewDateBirth=findViewById(R.id.textViewDateBirthAboutUser);
        TextViewAge=findViewById(R.id.textViewAgeAboutUser);
        TextViewEmail=findViewById(R.id.textViewEmailAboutUser);
        TextViewPhone=findViewById(R.id.textViewPhoneAboutUser);
        TextViewDogsitter=findViewById(R.id.textViewDogsitterAboutUser);
        TextViewAbout=findViewById(R.id.textViewDescriptionAboutUset);
        ImageViewPhoto=findViewById(R.id.imageViewPhotoAboutUser);
        ButtonDogs=findViewById(R.id.buttonDogsAboutUser);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//задаем портретную ориентацию
        // убираем верхнюю строку где время и уровень заряда батареи
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //получаем код пользователя
        Bundle arguments = getIntent().getExtras();
        IdUser= Integer.parseInt(arguments.get("IdUser").toString());
        //нажата кнопка 'Собаки'
        ButtonDogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Service.FlagUpdate=true;
                Intent intent = new Intent(AboutUserActivity.this, DogsUserActivity.class);
                intent.putExtra("IdUser", IdUser);
                startActivity(intent);
            }
        });
        System.out.println("IdUser="+IdUser);
        GetDataUser();
    }
    //получение данных о пользователе
    private void GetDataUser(){
        ProgressDialog pd;
        RequestParams params = new RequestParams();
        params.put("id_user", IdUser);
        pd = new ProgressDialog(AboutUserActivity.this);
        pd.setTitle("Сообщение");
        pd.setMessage("Подождите, идет обработка данных.");
        // включаем анимацию ожидания
        pd.setIndeterminate(true);
        //не даем исчезнуть диалогу с сообщением
        pd.setCancelable(false);
        pd.show();
        //отправка данных на сервер
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(Service.UrlServer + "/GetDataUserController", params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                pd.dismiss();
                String response=new String(responseBody);
                response=response.trim();
                try {
                    JSONObject obj=new JSONObject(response);
                    TextViewName.setText("ФИО: "+obj.getString("name"));
                    TextViewDateBirth.setText("Дата рождения: "+obj.getString("date_birth_text"));
                    TextViewAge.setText("Возрасть: "+obj.getString("age"));
                    TextViewEmail.setText("Email: "+obj.getString("email"));
                    TextViewPhone.setText("Телефон: "+obj.getString("phone"));
                    TextViewAbout.setText(obj.getString("about"));
                    if(obj.getBoolean("dogsitter"))TextViewDogsitter.setText("Догситтер: да");
                    else TextViewDogsitter.setText("Догситтер: нет");
                    String image_str=obj.getString("photo");
                    Bitmap image;
                    if(image_str.length()>0){
                        image=Service.DecodeBase64(image_str);
                        ImageViewPhoto.setImageBitmap(image);
                    }else ImageViewPhoto.setImageResource(android.R.color.transparent);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                pd.dismiss();
                Toast toast = Toast.makeText(getApplicationContext(), "Произошла ошибка.", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}
