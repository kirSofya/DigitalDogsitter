package com.dog.dogsitter;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONException;
import org.json.JSONObject;

public class AddEditMedicalDog extends AppCompatActivity {
    TextView TextViewTitle;
    EditText EditTextDate, EditTextDescription;
    Button ButtonAddEdit;
    int Oper=0, IdDog=0, IdMedical=0;

    AsyncHttpResponseHandler GetMedicalDogControllerPost = new AsyncHttpResponseHandler() {
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
                EditTextDate.setText(obj.getString("date"));
                EditTextDescription.setText(obj.getString("description"));
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

    AsyncHttpResponseHandler AddEditMedicalDogControllerPost = new AsyncHttpResponseHandler() {
        @Override
        public void onStart() {

        }

        @Override
        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
//            pd.dismiss();
            Service.FlagUpdate=true;
            if(Oper==1)Toast.makeText(getApplicationContext(), "Добавление данных прошло успешно.", Toast.LENGTH_SHORT).show();
            else Toast.makeText(getApplicationContext(), "Изменение данных прошло успешно.", Toast.LENGTH_SHORT).show();
            finish();
        }

        @Override
        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
//            pd.dismiss();
            Toast toast = Toast.makeText(getApplicationContext(), "Произошла ошибка.", Toast.LENGTH_SHORT);
            toast.show();
        }
    };

    View.OnClickListener ButtonAddEditOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(CheckCorrectData()){
                String date, description;
                date=EditTextDate.getText().toString();
                description=EditTextDescription.getText().toString();
                ProgressDialog pd;
                RequestParams params = new RequestParams();
                params.put("oper", Oper);
                params.put("id_dog", IdDog);
                params.put("id_user", Service.IdUser);
                if(Oper==2)params.put("id_medical", IdMedical);
                params.put("date", date);
                params.put("description", description);
                pd = new ProgressDialog(AddEditMedicalDog.this);
                pd.setTitle("Сообщение");
                pd.setMessage("Подождите, идет обработка данных.");
                // включаем анимацию ожидания
                pd.setIndeterminate(true);
                //не даем исчезнуть диалогу с сообщением
                pd.setCancelable(false);
                pd.show();
                //отправка данных на сервер
                AsyncHttpClient client = new AsyncHttpClient();
                client.post(Service.UrlServer + "/AddEditMedicalDogController", params, AddEditMedicalDogControllerPost);
                pd.dismiss();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_medical_dog);
        TextViewTitle=findViewById(R.id.textViewTitleAddEditMedicalDog);
        EditTextDate=findViewById(R.id.editTextDateAddEditMedicalDog);
        EditTextDescription=findViewById(R.id.editTextDescriptionAddEditMedicalDog);
        ButtonAddEdit=findViewById(R.id.buttonAddEditMedicalDog);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//задаем портретную ориентацию
        // убираем верхнюю строку где время и уровень заряда батареи
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //получаем значение операции
        Bundle arguments = getIntent().getExtras();
        Oper = Integer.parseInt(arguments.get("Oper").toString());
        switch (Oper){
            case 1:
                IdDog = Integer.parseInt(arguments.get("IdDog").toString());
                TextViewTitle.setText("Добавить мед. данные");
                ButtonAddEdit.setText("Добавить");
                break;
            case 2:
                IdMedical = Integer.parseInt(arguments.get("IdMedical").toString());
                TextViewTitle.setText("Изменить мед. данные");
                ButtonAddEdit.setText("Изменить");
                ProgressDialog pd;
                RequestParams params = new RequestParams();
                params.put("id_medical", IdMedical);
                pd = new ProgressDialog(AddEditMedicalDog.this);
                pd.setTitle("Сообщение");
                pd.setMessage("Подождите, идет обработка данных.");
                // включаем анимацию ожидания
                pd.setIndeterminate(true);
                //не даем исчезнуть диалогу с сообщением
                pd.setCancelable(false);
                pd.show();
                //отправка данных на сервер
                AsyncHttpClient client = new AsyncHttpClient();
                client.post(Service.UrlServer + "/GetMedicalDogController", params, GetMedicalDogControllerPost);
                pd.dismiss();
                break;
        }
        //нажата кнопка 'Добавить/Изменить'
        ButtonAddEdit.setOnClickListener(ButtonAddEditOnClickListener);
    }
    //событие при закрытии активности
    @Override
    protected void onDestroy(){
        super.onDestroy();
        Service.FlagUpdate=false;
    }
    //проверка на корректность ввода данных
    private Boolean CheckCorrectData(){
        String date, description;
        date=EditTextDate.getText().toString();
        if(!Service.CheckDate(date)){
            Toast.makeText(getApplicationContext(),"Введите корректно дату, например, 2020-12-01.", Toast.LENGTH_SHORT).show();
            return false;
        }
        description=EditTextDescription.getText().toString();
        if(description.length()==0){
            Toast.makeText(getApplicationContext(),"Введите описание.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
