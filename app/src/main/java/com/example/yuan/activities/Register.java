package com.example.yuan.activities;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.yuan.webmarket.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity;
import ch.boye.httpclientandroidlib.client.methods.CloseableHttpResponse;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.impl.client.CloseableHttpClient;
import ch.boye.httpclientandroidlib.impl.client.HttpClients;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;

public class Register extends AppCompatActivity {

    private String url = "http://128.235.40.185:8080/MyWebAppTest/Register";

    private Button mBtnSub = null;
    private Button mBtnReturn = null;
    private EditText mEtEmail = null;
    private EditText mEtPwd_1 = null;
    private EditText mEtPwd_2 = null;
    private EditText mEtSt = null;
    private EditText mEtCity = null;
    private EditText mEtState = null;
    private EditText mEtZIP = null;
    private EditText mEtLName = null;
    private EditText mEtFName = null;
    private EditText mEtPhone = null;

    String email = null;
    String lName = null;
    String fName = null;
    String phone = null;
    String address = null;
    String pwd_1 = null;
    String pwd_2 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
        setListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Initial button members
     */
    private void initView(){
        mBtnSub = (Button) findViewById(R.id.btnSub);
        mBtnReturn = (Button) findViewById(R.id.btnReturn);
        mEtEmail = (EditText)findViewById(R.id.etEmail_1);
        mEtPhone = (EditText)findViewById(R.id.etPhone_1);
        mEtFName = (EditText)findViewById(R.id.etName_1);
        mEtLName = (EditText)findViewById(R.id.etName_2);
        mEtPwd_1 = (EditText)findViewById(R.id.etPwd_1);
        mEtPwd_2 = (EditText)findViewById(R.id.etPwd_2);
        mEtSt = (EditText)findViewById(R.id.etSt_1);
        mEtCity = (EditText)findViewById(R.id.etCity_1);
        mEtState = (EditText) findViewById(R.id.etState_1);
        mEtZIP = (EditText) findViewById(R.id.etZIP_1);
    }


    private void setListener(){
        //
        mBtnSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = mEtEmail.getText().toString();
                pwd_1 = mEtPwd_1.getText().toString();
                pwd_2 = mEtPwd_2.getText().toString();
                lName = mEtLName.getText().toString();
                fName = mEtFName.getText().toString();
                phone = mEtPhone.getText().toString();
                address = mEtSt.getText().toString() + ", " + mEtCity.getText().toString() + ", "
                        + mEtState.getText().toString() + ", " + mEtZIP.getText().toString();
                if (email.equals("")) {
                    Toast.makeText(Register.this, "Please input your email address.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (lName.equals("")){
                    Toast.makeText(Register.this, "Please input your last name.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (fName.equals("")){
                    Toast.makeText(Register.this, "Please input your first name.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (phone.equals("")){
                    Toast.makeText(Register.this, "Please input your phone number.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (address.equals("")){
                    Toast.makeText(Register.this, "Please input your address.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (pwd_1.equals("")){
                    Toast.makeText(Register.this, "Please input your password.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (pwd_2.equals("")){
                    Toast.makeText(Register.this, "Please confirm your password.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (!pwd_1.equals(pwd_2)){
                    Toast.makeText(Register.this, "The password and confirm passwords are not consistent.", Toast.LENGTH_SHORT).show();
                    return;
                }
                new Thread(RegisterThread).start();
            }
        });
        //
        mBtnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Register.this, Login.class);
                Register.this.startActivity(intent);
            }
        });
    }


    /**
     *
     */
    Handler httpHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");
            Log.d("Http", "请求结果:" + val);
            //可以开始处理UI
            if (val.equals("1")){
                Toast.makeText(Register.this, "Registration successful, login now.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.setClass(Register.this, Login.class);
                Register.this.startActivity(intent);
            } else if (val.equals("0")) {
                Toast.makeText(Register.this, "Username not available, try again!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(Register.this, "Fail, error code: " + val, Toast.LENGTH_LONG).show();
            }
        }
    };

    /**
     *
     */
    Runnable RegisterThread = new Runnable(){
        @Override
        public void run() {
            // TODO: http post.
            String result = "-1";
            //noinspection deprecation
            //HttpClient httpClient = new DefaultHttpClient();
            CloseableHttpClient httpClient = HttpClients.createDefault();
            //String url = "https://web.njit.edu/~yl768/webapps7/Register";

            //第二步：生成使用POST方法的请求对象
            HttpPost httpPost = new HttpPost(url);
            //NameValuePair对象代表了一个需要发往服务器的键值对
            NameValuePair pair1 = new BasicNameValuePair("email", email);
            NameValuePair pair2 = new BasicNameValuePair("fName", fName);
            NameValuePair pair3 = new BasicNameValuePair("lName", lName);
            NameValuePair pair4 = new BasicNameValuePair("phone", phone);
            NameValuePair pair5 = new BasicNameValuePair("address", address);
            NameValuePair pair6 = new BasicNameValuePair("password", pwd_1);
            //将准备好的键值对对象放置在一个List当中
            ArrayList<NameValuePair> pairs = new ArrayList<>();
            pairs.add(pair1);
            pairs.add(pair2);
            pairs.add(pair3);
            pairs.add(pair4);
            pairs.add(pair5);
            pairs.add(pair6);
            try {
                //创建代表请求体的对象（注意，是请求体）
                HttpEntity requestEntity = new UrlEncodedFormEntity(pairs);
                //将请求体放置在请求对象当中
                httpPost.setEntity(requestEntity);
                //执行请求对象
                try {
                    //第三步：执行请求对象，获取服务器发还的相应对象
                    CloseableHttpResponse response = httpClient.execute(httpPost);
                    //第四步：检查相应的状态是否正常：检查状态码的值是200表示正常
                    if (response.getStatusLine().getStatusCode() == 200) {
                        //第五步：从相应对象当中取出数据，放到entity当中
                        HttpEntity entity = response.getEntity();
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(entity.getContent()));
                        result = reader.readLine();
                        Log.d("HTTP", "POST:" + result);
                    } else {
                        result = "" + response.getStatusLine().getStatusCode();
                        Log.d("HTTP", "ERROR:" + result);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value",result);
            msg.setData(data);
            httpHandler.sendMessage(msg);
        }
    };
}
