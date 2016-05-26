package com.example.zengguangyi.mtuapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.lang.String;

public class MainActivity extends AppCompatActivity {
    private Button btnWeather;
    private Button btnImg;
    private TextView text;
    private WebView webView;
    private ImageView img;
    public static final int SHOW_RESPONSE = 0;

    String response;//天气预报的回调数据
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_RESPONSE:
                    response = (String) msg.obj;
                    // 在这里进行UI操作，将结果显示到界面上
//                    text.setText(response);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = (TextView)findViewById(R.id.text);
        text.setText("123");

        /*WebView嵌入网页*/
        webView = (WebView) findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;//不用借助浏览器
            }
        });
//        webView.loadUrl("http://www.zengguangyi.com/wx/androidHtml/tet.html");
//        webView.loadUrl("http://www.baidu.com");

        /*天气按钮点击事件*/
        btnWeather = (Button)findViewById(R.id.weather);
        /*httpClient先请求好数据，保证reponse不为空*/
        sendRequestWithHttpURLConnection();
        btnWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text.setText("456");

                /*对话框*/
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("天气");
                dialog.setMessage(response);
                dialog.setCancelable(false);
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                if (response != null) {
                    dialog.show();
                } else {
                    sendRequestWithHttpURLConnection();
                    dialog.setMessage("网络延迟，待会试试0.o");
                    dialog.show();
                }
            }
        });

        btnImg = (Button)findViewById(R.id.new_image);
        btnImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.setVisibility(View.VISIBLE);
                webView.loadUrl("http://www.zengguangyi.com/wx/androidHtml/test.html");
            }
        });

        
    }

    public void sendRequestWithHttpURLConnection(){
        // 开启线程来发起网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL("http://op.juhe.cn/onebox/weather/query?cityname=%e7%8f%a0%e6%b5%b7&key=8593f3225a8f2d8892f4f1ac50345a07");
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    // 下面对获取到的输入流进行读取
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    String responsed = response.toString();//转换成String给parseJSONWithJSONObject解析
                    responsed = parseJSONWithJSONObject(responsed);
                    Message message = new Message();
                    message.what = SHOW_RESPONSE;
                    // 将服务器返回的结果存放到Message中
                    message.obj = responsed;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    private String parseJSONWithJSONObject(String jsonData) {
        String rlt = "";
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            String result_str = jsonObject.getString("result");
            JSONObject result = new JSONObject(result_str);

            String data_str = result.getString("data");
            JSONObject data = new JSONObject(data_str);

            String realtime_str = data.getString("realtime");
            JSONObject realtime = new JSONObject(realtime_str);

            String weather_str = realtime.getString("weather");
            JSONObject weather = new JSONObject(weather_str);

            String city_name_str = realtime.getString("city_name"); //获取到城市名
            String date_str = realtime.getString("date");            //获取到日期
            String time_str = realtime.getString("time");            //获取到时间
            String humidity_str = weather.getString("humidity");            //获取到湿度
            String temperature_str = weather.getString("temperature");            //获取到温度
            String info_str = weather.getString("info");            //获取到天气状态（雾、雨。。）

            rlt = "城市名称：" + city_name_str + "\n"
                    + "更新时间：" + date_str + "  " + time_str + "\n"
                    + "湿度：" + humidity_str + "\n"
                    + "温度：" + temperature_str + "`C\n"
                    + "状态：" + info_str;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rlt;
    }

}
