package com.example.uaiter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {
    static String MQTTHOST = "mqtt://192.168.0.6:1883";
    static String USERNAME = "uaiter";
    static String PASSWORD = "uaiter";
    MqttAndroidClient client;
    TextView txtStatus;
    TextView txtFluxo;
    TextView txtVolume;
    String topicoValvula = "valvula";
    String topicoFluxo = "vazao";
    String topicoVolume = "volume";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtStatus = findViewById(R.id.tvStatusMqtt);
        txtFluxo = findViewById(R.id.tvMedicaoFluxo);
        txtVolume = findViewById(R.id.tvMedicaoVolume);

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), MQTTHOST, clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    txtStatus.setText("conectado");
                    setSubscrition();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    txtStatus.setText("desconectado");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }


        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                txtStatus.setText("desconectado");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if(topic.toString().equals(topicoFluxo)){
                    String texto = new String(message.getPayload());
                    txtFluxo.setText(texto);
                }
                if(topic.toString().equals(topicoVolume)){
                    String texto = new String(message.getPayload());
                    txtVolume.setText(texto);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                txtStatus.setText("Dados Recebidos");
            }
        });

    }

    private void setSubscrition(){
        try{
            client.subscribe(topicoFluxo,0);
        }catch (MqttException e) {
            e.printStackTrace();
            txtStatus.setText("Erro Sub Fluxo");
        }
        try{
            client.subscribe(topicoVolume,0);
        }catch (MqttException e) {
            e.printStackTrace();
            txtStatus.setText("Erro Sub Volume");
        }
    }

    boolean estadoValvula = false;
    public void controleValvula(View v){
        String topic = topicoValvula;
        if (estadoValvula){
            String message = "fechar";
            try {
                client.publish(topic, message.getBytes(),0,false    );
                txtStatus.setText("Comando Fechar Enviado");
            } catch (MqttException e) {
                e.printStackTrace();
                txtStatus.setText("desconectado");
            }
        }else{
            String message = "abrir";
            try {
                client.publish(topic, message.getBytes(),0,false    );
                txtStatus.setText("Comando Abrir Enviado");
            } catch (MqttException e) {
                e.printStackTrace();
                txtStatus.setText("desconectado");
            }
        }
    }
}