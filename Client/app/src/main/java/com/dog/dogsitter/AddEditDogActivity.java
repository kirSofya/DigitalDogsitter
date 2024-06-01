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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.IOException;

public class AddEditDogActivity extends AppCompatActivity {
    private static final int READ_REQUEST_CODE = 42;
    EditText EditTextName, EditTextDateBirth, EditTextBreed, EditTextAbout;
    ImageView ImageViewImage;
    Button ButtonAddImage, ButtonAddEdit;
    TextView TextViewTitle;
    Bitmap Image=null;
    int Oper;//1 - добавить 2 - изменить
    int IdDog=0;//код собаки при редактированни

    View.OnClickListener ButtonAddImageOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //открываем файловый менеджер
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, READ_REQUEST_CODE);
        }
    };

    View.OnClickListener ButtonAddEditOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(CheckCorrectData()){
                String name, date_birth, breed, about, image64="";
                name=EditTextName.getText().toString();
                date_birth=EditTextDateBirth.getText().toString();
                breed=EditTextBreed.getText().toString();
                about=EditTextAbout.getText().toString();
                image64 = Service.EncodeToBase64(Image, Bitmap.CompressFormat.JPEG, 100);
                ProgressDialog pd;
                RequestParams params = new RequestParams();
                if(Oper==2)params.put("id_dog", IdDog);
                params.put("oper", Oper);
                params.put("id_user", Service.IdUser);
                params.put("name", name);
                params.put("date_birth", date_birth);
                params.put("breed", breed);
                params.put("about", about);
                params.put("image_str", image64);
                pd = new ProgressDialog(AddEditDogActivity.this);
                pd.setTitle("Сообщение");
                pd.setMessage("Подождите, идет обработка данных.");
                // включаем анимацию ожидания
                pd.setIndeterminate(true);
                //не даем исчезнуть диалогу с сообщением
                pd.setCancelable(false);
                pd.show();
                //отправка данных на сервер
                AsyncHttpClient client = new AsyncHttpClient();
                client.post(Service.UrlServer + "/AddEditDataDogController", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                        pd.dismiss();
                        Service.FlagUpdate=true;
                        if(Oper==1)Toast.makeText(getApplicationContext(), "Добавление данных прошло успешно.", Toast.LENGTH_SHORT).show();
                        else Toast.makeText(getApplicationContext(), "Изменение данных прошло успешно.", Toast.LENGTH_SHORT).show();
                        finish();
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
    };

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
                EditTextName.setText(obj.getString("name"));
                EditTextDateBirth.setText(obj.getString("date_birth"));
                EditTextBreed.setText(obj.getString("breed"));
                EditTextAbout.setText(obj.getString("about"));
                String image_str=obj.getString("image");
                Bitmap image;
                if(image_str.length()>0){
                    Image=Service.DecodeBase64(image_str);
                    image=Service.ScaleImage(30, Image);
                    ImageViewImage.setImageBitmap(image);
                }else{
                    Image=null;
                    ImageViewImage.setImageResource(android.R.color.transparent);
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
        setContentView(R.layout.activity_add_edit_dog);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//задаем портретную ориентацию
        // убираем верхнюю строку где время и уровень заряда батареи
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        TextViewTitle=findViewById(R.id.textViewTitleAddEditDog);
        EditTextName=findViewById(R.id.editTextNameAddEditDog);
        EditTextDateBirth=findViewById(R.id.editTextDateBirthAddEditDog);
        EditTextBreed=findViewById(R.id.editTextBreedAddEditDog);
        EditTextAbout=findViewById(R.id.editTextAboutAddEditDog);
        ImageViewImage=findViewById(R.id.imageViewAddEditDog);
        ButtonAddImage=findViewById(R.id.buttonAddImageAddEditDog);
        ButtonAddEdit=findViewById(R.id.buttonAddEditDog);
        //получаем значение операции
        Bundle arguments = getIntent().getExtras();
        Oper = Integer.parseInt(arguments.get("Oper").toString());
        switch (Oper){
            case 1:
                TextViewTitle.setText("Добавить собаку");
                ButtonAddEdit.setText("Добавить");
                break;
            case 2:
                TextViewTitle.setText("Изменить данные по собаке");
                ButtonAddEdit.setText("Изменить");
                IdDog=Integer.parseInt(arguments.get("IdDog").toString());
                break;
        }
        //нажата кнопка 'Загрузить фото'
        ButtonAddImage.setOnClickListener(ButtonAddImageOnClickListener);
        //нажата кнопка 'Добавить/Изменить'
        ButtonAddEdit.setOnClickListener(ButtonAddEditOnClickListener);
        if(Oper==2)GetDataDog();
    }
    //получение данных о собаке при редактировании
    private void GetDataDog(){
        ProgressDialog pd;
        RequestParams params = new RequestParams();
        params.put("id_dog", IdDog);
        pd = new ProgressDialog(AddEditDogActivity.this);
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
    //проверка на корректность ввода данных
    private Boolean CheckCorrectData(){
        if(EditTextName.getText().length()==0){
            Toast.makeText(getApplicationContext(),"Введите кличку.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(Service.CheckDate(EditTextDateBirth.getText().toString())==false){
            Toast.makeText(getApplicationContext(),"Введите корректно дату рождения.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(EditTextBreed.getText().length()==0){
            Toast.makeText(getApplicationContext(),"Введите породу.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(EditTextAbout.getText().length()==0){
            Toast.makeText(getApplicationContext(),"Введите данные о собаке.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(Image==null){
            Toast.makeText(getApplicationContext(),"Загрузите изображение.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
                    Image=GetBitmapFromUri(uri);
                    image=Service.ScaleImage(30, Image);
                    ImageViewImage.setImageBitmap(image);
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
