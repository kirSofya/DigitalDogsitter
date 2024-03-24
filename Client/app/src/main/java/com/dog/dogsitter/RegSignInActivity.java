package com.dog.dogsitter;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONException;
import org.json.JSONObject;

public class RegSignInActivity extends AppCompatActivity {
    int Oper;//1 - регистрация 2 - вход
    TextView TextTitle;
    Button ButtonRegSignIn;
    TableRow TableRowDateBirth1, TableRowDateBirth2, TableRowDogsitter;
    EditText EditTextDate, EditTextEmail, EditTextPassword;
    Switch SwitchDogsitter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_sign_in);
        TextTitle=(TextView) findViewById(R.id.textViewTitleRegSignIn);
        TableRowDateBirth1=(TableRow) findViewById(R.id.tableRowDateBirthRegSignIn1);
        TableRowDateBirth2=(TableRow) findViewById(R.id.tableRowDateBirthRegSignIn2);
        TableRowDogsitter=(TableRow) findViewById(R.id.tableRowDogsitterRegSignIn);
        ButtonRegSignIn=(Button) findViewById(R.id.buttonRegSignIn);
        EditTextDate=(EditText) findViewById(R.id.editTextDateBirthRegSign);
        EditTextEmail=(EditText) findViewById(R.id.editTextEmailRegSign);
        EditTextPassword=(EditText) findViewById(R.id.editTextPasswordRegSign);
        SwitchDogsitter=(Switch) findViewById(R.id.switchDogsitterRegSignIn);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//задаем портретную ориентацию
        // убираем верхнюю строку где время и уровень заряда батареи
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //получаем значение операции
        Bundle arguments = getIntent().getExtras();
        Oper = Integer.parseInt(arguments.get("Oper").toString());
        switch (Oper){
            case 1:
                TextTitle.setText("РЕГИСТРАЦИЯ");
                TableRowDateBirth1.setVisibility(View.VISIBLE);
                TableRowDateBirth2.setVisibility(View.VISIBLE);
                TableRowDogsitter.setVisibility(View.VISIBLE);
                ButtonRegSignIn.setText("Регистрация");
                break;
            case 2:
                TextTitle.setText("АВТОРИЗАЦИЯ");
                TableRowDateBirth1.setVisibility(View.GONE);
                TableRowDateBirth2.setVisibility(View.GONE);
                TableRowDogsitter.setVisibility(View.GONE);
                ButtonRegSignIn.setText("Авторизация");
                break;
        }
        //нажата кнопка 'Авторизация/Регистрация'
        ButtonRegSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String date_birth, email, password, name_controller="";
                date_birth=EditTextDate.getText().toString();
                email=EditTextEmail.getText().toString();
                password=EditTextPassword.getText().toString();
                boolean dogsitter=SwitchDogsitter.isChecked();
                //проверка на корректность ввода данных
                if(Oper==1 && !Service.CheckDate(date_birth)){
                    Toast.makeText(getApplicationContext(), "Введите корректно дату рождения.", Toast.LENGTH_LONG).show();
                    return;
                }
                if(email.length()==0){
                    Toast.makeText(getApplicationContext(), "Введите электронную почту.", Toast.LENGTH_LONG).show();
                    return;
                }
                if(password.length()<5){
                    Toast.makeText(getApplicationContext(), "Пароль должен состоять от 5 и более символов.", Toast.LENGTH_LONG).show();
                    return;
                }
                ProgressDialog pd;
                RequestParams params = new RequestParams();
                params.put("email", email);
                params.put("password", password);
                if(Oper==1){
                    params.put("date_birth", date_birth);
                    params.put("dogsitter", dogsitter);
                    name_controller="/RegUserController";
                }else name_controller="/SignInUserController";
                pd = new ProgressDialog(RegSignInActivity.this);
                pd.setTitle("Сообщение");
                pd.setMessage("Подождите, идет обработка данных.");
                // включаем анимацию ожидания
                pd.setIndeterminate(true);
                //не даем исчезнуть диалогу с сообщением
                pd.setCancelable(false);
                pd.show();
                //отправка данных на сервер
                AsyncHttpClient client = new AsyncHttpClient();
                client.post(Service.UrlServer+name_controller, params, new AsyncHttpResponseHandler() {
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
                            if(obj.getBoolean("Result")){
                                Service.IdUser=obj.getInt("IdUser");
                                Service.EmailUser=email;
                                if(Oper==1)Toast.makeText(getApplicationContext(), "Регистрация пользователя прошла успешно.", Toast.LENGTH_LONG).show();
                                else Toast.makeText(getApplicationContext(), "Авторизация пользователя прошла успешно.", Toast.LENGTH_LONG).show();
                                finish();
                            }else Toast.makeText(getApplicationContext(), obj.getString("Message"), Toast.LENGTH_LONG).show();
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
        });
    }
}
