package com.dog.dogsitter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.IOException;

public class DataUserActivity extends AppCompatActivity {
    private static final int READ_REQUEST_CODE = 42;
    EditText EditTextName, EditTextDateBirth, EditTextPhone, EditTextEmail, EditTextPassword, EditTextAbout;
    ImageView ImageViewPhoto;
    Button ButtonDownloadPhoto, ButtonDeletePhoto, ButtonSave;
    Switch SwitchDogsitter;
    Bitmap ImagePhoto=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_user);
        EditTextName=findViewById(R.id.editTextNameDataUser);
        EditTextDateBirth=findViewById(R.id.editTextDateBirthDataUser);
        EditTextPhone=findViewById(R.id.editTextPhoneDataUser);
        EditTextEmail=findViewById(R.id.editTextEmailDataUser);
        EditTextPassword=findViewById(R.id.editTextPasswordDataUser);
        EditTextAbout=findViewById(R.id.editTextAboutDataUser);
        ImageViewPhoto=findViewById(R.id.imageViewPhotoDataUser);
        ButtonDownloadPhoto=findViewById(R.id.buttonDownloadImageDataUser);
        ButtonDeletePhoto=findViewById(R.id.buttonDeletePhotoDataUser);
        ButtonSave=findViewById(R.id.buttonSaveDataUser);
        SwitchDogsitter=findViewById(R.id.switchDogsitterDataUser);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//задаем портретную ориентацию
        // убираем верхнюю строку где время и уровень заряда батареи
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //нажата кнопка 'Загрузить фото'
        ButtonDownloadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //открываем файловый менеджер
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, READ_REQUEST_CODE);
            }
        });
        //нажата кнопка 'Удалить фото'
        ButtonDeletePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ImagePhoto!=null) {
                    ImagePhoto = null;
                    ImageViewPhoto.setImageResource(android.R.color.transparent);
                }
            }
        });
        //нажата кнопка 'Сохранить'
        ButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CheckCorrectData()){
                    String name, date_birth, email, password, phone, about, photo64="";
                    Boolean dogsitter;
                    name=EditTextName.getText().toString();
                    date_birth=EditTextDateBirth.getText().toString();
                    email=EditTextEmail.getText().toString();
                    password=EditTextPassword.getText().toString();
                    phone=EditTextPhone.getText().toString();
                    about=EditTextAbout.getText().toString();
                    dogsitter=SwitchDogsitter.isChecked();
                    if(ImagePhoto!=null)photo64 = Service.EncodeToBase64(ImagePhoto, Bitmap.CompressFormat.JPEG, 100);
                    ProgressDialog pd;
                    RequestParams params = new RequestParams();
                    params.put("id_user", Service.IdUser);
                    params.put("name", name);
                    params.put("date_birth", date_birth);
                    params.put("email", email);
                    params.put("password", password);
                    params.put("phone", phone);
                    params.put("about", about);
                    params.put("dogsitter", dogsitter);
                    params.put("photo", photo64);
                    pd = new ProgressDialog(DataUserActivity.this);
                    pd.setTitle("Сообщение");
                    pd.setMessage("Подождите, идет обработка данных.");
                    // включаем анимацию ожидания
                    pd.setIndeterminate(true);
                    //не даем исчезнуть диалогу с сообщением
                    pd.setCancelable(false);
                    pd.show();
                    //отправка данных на сервер
                    AsyncHttpClient client = new AsyncHttpClient();
                    client.post(Service.UrlServer + "/SaveDataUserController", params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                            pd.dismiss();
                            String response=new String(responseBody);
                            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
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
        });
        GetDataUser();
    }
    //проверка на корректность ввода данных
    private Boolean CheckCorrectData(){
        if(Service.CheckDate(EditTextDateBirth.getText().toString())==false){
            Toast.makeText(getApplicationContext(),"Введите корректно дату рождения.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(EditTextEmail.getText().toString().length()==0){
            Toast.makeText(getApplicationContext(),"Введите корректно электронную почту.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(EditTextPassword.getText().toString().length()<5){
            Toast.makeText(getApplicationContext(),"Введите корректно пароль, не менее 5 символов.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    //загрузка личных данных пользователя
    private void GetDataUser(){
        ProgressDialog pd;
        RequestParams params = new RequestParams();
        params.put("id_user", Service.IdUser);
        pd = new ProgressDialog(DataUserActivity.this);
        pd.setTitle("Сообщение");
        pd.setMessage("Подождите, идет обработка данных.");
        // включаем анимацию ожидания
        pd.setIndeterminate(true);
        //не даем исчезнуть диалогу с сообщением
        pd.setCancelable(false);
        pd.show();
        //отправка данных на сервер
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(Service.UrlServer+"/GetDataUserController", params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                pd.dismiss();
                //получение ответа от сервера
                String response=new String(responseBody);
                response=response.trim();
                try {
                    JSONObject obj=new JSONObject(response);
                    EditTextName.setText(obj.getString("name"));
                    EditTextDateBirth.setText(obj.getString("date_birth"));
                    EditTextPhone.setText(obj.getString("phone"));
                    EditTextEmail.setText(obj.getString("email"));
                    EditTextPassword.setText(obj.getString("password"));
                    EditTextAbout.setText(obj.getString("about"));
                    SwitchDogsitter.setChecked(obj.getBoolean("dogsitter"));
                    String image_str=obj.getString("photo");
                    Bitmap image;
                    if(image_str.length()>0){
                        ImagePhoto=Service.DecodeBase64(image_str);
                        image=Service.ScaleImage(30, ImagePhoto);
                        ImageViewPhoto.setImageBitmap(image);
                    }else{
                        ImagePhoto=null;
                        ImageViewPhoto.setImageResource(android.R.color.transparent);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                pd.dismiss();
                Toast.makeText(getApplicationContext(),"Произошла ошибка.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //при активации активности для получения результата
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            Bitmap image=null;
            if (resultData != null) {
                uri = resultData.getData();
                try {
                    image=GetBitmapFromUri(uri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if(image!=null)
                    if(Service.SizeOfBitmap(image)>30000000){
                        Toast.makeText(getApplicationContext(), "Слишком большой файл.", Toast.LENGTH_LONG).show();
                        return;
                    }
                try {
                    ImagePhoto=GetBitmapFromUri(uri);
                    image=Service.ScaleImage(30, ImagePhoto);
                    ImageViewPhoto.setImageBitmap(image);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    //преобразование Uri в Bitmap
    private Bitmap GetBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
}
