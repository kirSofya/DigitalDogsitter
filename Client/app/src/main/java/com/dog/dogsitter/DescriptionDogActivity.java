package com.dog.dogsitter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dog.dogsitter.structs.DataDog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class DescriptionDogActivity extends AppCompatActivity {
    TextView TextViewName, TextViewDateBirth, TextViewAge, TextViewBreed, TextViewDescription;
    ImageView ImageViewDog;

    AsyncHttpResponseHandler GetDataDogControllerPost = new AsyncHttpResponseHandler() {
        @Override
        public void onStart() {

        }

        @Override
        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
//            pd.dismiss();
            String response=new String(responseBody);
            response=response.trim();
            try {
                JSONObject obj=new JSONObject(response);
                TextViewName.setText("Кличка: "+obj.getString("name"));
                TextViewDateBirth.setText("Дата рождения: "+obj.getString("date_birth_text"));
                TextViewAge.setText("Возраст: "+obj.getInt("age"));
                TextViewBreed.setText("Порода: "+obj.getString("breed"));
                TextViewDescription.setText(obj.getString("about"));
                String image_str=obj.getString("image");
                Bitmap image;
                if(image_str.length()>0){
                    image=Service.DecodeBase64(image_str);
                    ImageViewDog.setImageBitmap(image);
                }else{
                    ImageViewDog.setImageResource(android.R.color.transparent);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
//            pd.dismiss();
            Toast toast = Toast.makeText(getApplicationContext(), "Произошла ошибка.", Toast.LENGTH_SHORT);
            toast.show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description_dog);
        TextViewName=findViewById(R.id.textViewNameDescriptionDog);
        TextViewDateBirth=findViewById(R.id.textViewDateBirthDescriptionDog);
        TextViewAge=findViewById(R.id.textViewAgeDescriptionDog);
        TextViewBreed=findViewById(R.id.textViewBreedDescriptionDog);
        TextViewDescription=findViewById(R.id.textViewDescriptionDog);
        ImageViewDog=findViewById(R.id.imageViewImageDescriptionDog);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//задаем портретную ориентацию
        // убираем верхнюю строку где время и уровень заряда батареи
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //получаем код собаки
        Bundle arguments = getIntent().getExtras();
        int id_dog = Integer.parseInt(arguments.get("IdDog").toString());
        GetDataDog(id_dog);
    }
    //получение и вывод в активность данных по собаке
    private void GetDataDog(int id_dog){
        ProgressDialog pd;
        RequestParams params = new RequestParams();
        params.put("id_dog", id_dog);
        pd = new ProgressDialog(DescriptionDogActivity.this);
        pd.setTitle("Сообщение");
        pd.setMessage("Подождите, идет обработка данных.");
        // включаем анимацию ожидания
        pd.setIndeterminate(true);
        //не даем исчезнуть диалогу с сообщением
        pd.setCancelable(false);
        pd.show();
        //отправка данных на сервер
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(Service.UrlServer + "/GetDataDogController", params, GetDataDogControllerPost);
        pd.dismiss();
    }
}
