package com.example.samsunghackathon2021;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class MainActivity extends AppCompatActivity {
    MqttHelper mqttHelper;

    TextView dataReceived;
    TextView temperature;
    RadioButton btn_manual, btn_ontime, btn_auto;
    ProgressBar ground, air;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        temperature = (TextView) findViewById(R.id.temperature);
        dataReceived = (TextView) findViewById(R.id.dataReceived);
        btn_manual = (RadioButton) findViewById(R.id.radio_manual);
        btn_ontime = (RadioButton) findViewById(R.id.radio_ontime);
        btn_auto = (RadioButton) findViewById(R.id.radio_auto);
        btn_manual.setOnClickListener(radioButtonClickListener);
        btn_ontime.setOnClickListener(radioButtonClickListener);
        btn_auto.setOnClickListener(radioButtonClickListener);

        ground = (ProgressBar) findViewById(R.id.progressGroundHum);
        air = (ProgressBar) findViewById(R.id.progressAirHum);

        Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                startMqtt("user_f70f4807/test/temp");

            }
        };
        handler.sendEmptyMessage(0);


    }

    private void startMqtt(String topic) {
        mqttHelper = new MqttHelper(getApplicationContext(),topic);
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Debug", mqttMessage.toString());
                dataReceived.setText(mqttMessage.toString());
                String[] answer = mqttMessage.toString().split(" ");
                double d_temp = Double.valueOf(answer[0]);
                double d_hum = Double.valueOf(answer[1]);
                int temp = (int) Math.ceil(d_temp);
                int hum = (int) Math.ceil(d_hum);
                temperature.setText("+" + temp);

                //air.setMaxWidth(100);
                air.setProgress(hum);

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    View.OnClickListener radioButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RadioButton rb = (RadioButton)v;
            clearRadioChecked();
            switch (rb.getId()) {
                case R.id.radio_manual:
                    btn_manual.setChecked(!btn_manual.isChecked());
                    sendMessage(1);
                    break;
                case R.id.radio_ontime:
                    btn_ontime.setChecked(!btn_ontime.isChecked());
                    sendMessage(2);
                    break;
                case R.id.radio_auto:
                    btn_auto.setChecked(!btn_auto.isChecked());
                    sendMessage(3);
                    break;

                default:
                    break;
            }
        }
    };
    public void clearRadioChecked() {
        btn_manual.setChecked(false);
        btn_auto.setChecked(false);
        btn_ontime.setChecked(false);
    }
    public void sendMessage(int opcode){

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mqttHelper.publishMessage(""+opcode);
            }
        },1);
    }
}