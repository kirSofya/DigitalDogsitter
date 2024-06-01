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

public class AddEditArticleActivity extends AppCompatActivity {
    TextView TextViewTitle;
    EditText EditTextName, EditTextDescription;
    Button ButtonAddEdit;
    int Oper, IdArticle;

    AsyncHttpResponseHandler GetArticleUserControllerPost = new AsyncHttpResponseHandler() {
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
                EditTextName.setText(obj.getString("title"));
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

    AsyncHttpResponseHandler AddEditArticleControllerPost = new AsyncHttpResponseHandler() {
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
                String title, description;
                title=EditTextName.getText().toString();
                description=EditTextDescription.getText().toString();
                ProgressDialog pd;
                RequestParams params = new RequestParams();
                params.put("oper", Oper);
                if(Oper==2)params.put("id_article", IdArticle);
                params.put("id_user", Service.IdUser);
                params.put("title", title);
                params.put("description", description);
                pd = new ProgressDialog(AddEditArticleActivity.this);
                pd.setTitle("Сообщение");
                pd.setMessage("Подождите, идет обработка данных.");
                // включаем анимацию ожидания
                pd.setIndeterminate(true);
                //не даем исчезнуть диалогу с сообщением
                pd.setCancelable(false);
                pd.show();
                //отправка данных на сервер
                AsyncHttpClient client = new AsyncHttpClient();
                client.post(Service.UrlServer + "/AddEditArticleController", params, AddEditArticleControllerPost);
                pd.dismiss();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_article);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//задаем портретную ориентацию
        // убираем верхнюю строку где время и уровень заряда батареи
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        TextViewTitle=findViewById(R.id.textViewTitleAddEditArticle);
        EditTextName=findViewById(R.id.editTextNameAddEditArticle);
        EditTextDescription=findViewById(R.id.editTextDescriptionAddEditArticle);
        ButtonAddEdit=findViewById(R.id.buttonAddEditArticle);
        //получаем значение операции
        Bundle arguments = getIntent().getExtras();
        Oper = Integer.parseInt(arguments.get("Oper").toString());
        switch (Oper){
            case 1:
                TextViewTitle.setText("Добавить статью");
                ButtonAddEdit.setText("Добавить");
                break;
            case 2:
                TextViewTitle.setText("Изменить данные по статье");
                ButtonAddEdit.setText("Изменить");
                IdArticle=Integer.parseInt(arguments.get("IdArticle").toString());
                ProgressDialog pd;
                RequestParams params = new RequestParams();
                params.put("id_article", IdArticle);
                pd = new ProgressDialog(AddEditArticleActivity.this);
                pd.setTitle("Сообщение");
                pd.setMessage("Подождите, идет обработка данных.");
                // включаем анимацию ожидания
                pd.setIndeterminate(true);
                //не даем исчезнуть диалогу с сообщением
                pd.setCancelable(false);
                pd.show();
                //отправка данных на сервер
                AsyncHttpClient client = new AsyncHttpClient();
                client.post(Service.UrlServer + "/GetArticleUserController", params, GetArticleUserControllerPost);
                pd.dismiss();
                break;
        }
        //нажата кнопка 'Добавить/Изменить'
        ButtonAddEdit.setOnClickListener(ButtonAddEditOnClickListener);
    }
    //проверка на корректность ввода данных
    private Boolean CheckCorrectData(){
        String title, description;
        title=EditTextName.getText().toString();
        if(title.length()==0){
            Toast.makeText(getApplicationContext(),"Введите заголовок.", Toast.LENGTH_SHORT).show();
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
