package com.example.uaiter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    //aaaaaaaaaaaaaa
    int frente = 0, esquerda = 0, direita = 0, parado = 0, garraAberta = 0, modoAutomatico = 0;
    boolean valvula = false;
    public void mudarEstadoValvula(View v) {
        if(valvula)
            valvula = false;
        else
            valvula = true;
        atualizar(v);
    }

    public void atualizar(View view) {
        // Verifica dados recebidos
        SincronizaDados sincroniza = new SincronizaDados();
        sincroniza.execute(new String[]{""});
        // Envia comandos
        String comandosDirecao = "?direcao";
        if (valvula)
            comandosDirecao += "=a";
        else
            comandosDirecao += "=f";
        EnviaComandos controleDirecao = new EnviaComandos();
        controleDirecao.execute(new String[]{comandosDirecao});
    }

    String ip = "";
    public void conectarESP(View v){
        EditText IP = (EditText) findViewById(R.id.etIP);
        ip = IP.getText().toString();
        atualizar(v);
    }
    private class EnviaComandos extends AsyncTask<String, Void, String> {
        // dispara a requisição
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) ( new URL("http://" + ip + "/controle" + params[0])).openConnection();
                con.setRequestMethod("GET");
                con.setDoInput(true);
                con.setDoOutput(true);
                con.connect();
                StringBuffer buffer = new StringBuffer();
                InputStream is = con.getInputStream();
                is.close();
                con.disconnect();
                return "Conectado";
            } catch (IOException e) {
                e.printStackTrace();
                return "Desconectado";
            }
        }
        // trata os dados retornados
        @Override
        protected void onPostExecute(String retorno) {
            super.onPostExecute(retorno);
            EditText etEstadoComunicacao = (EditText) findViewById(R.id.etComunicacao);
            etEstadoComunicacao.setText(retorno);
        }
    }
    private class SincronizaDados extends AsyncTask<String, Void, String> {
        // dispara a requisição
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) ( new URL("http://" + ip + "/sincronizacao")).openConnection();
                con.setRequestMethod("GET");
                con.setDoInput(true);
                con.setDoOutput(true);
                con.connect();
                StringBuffer buffer = new StringBuffer();
                InputStream is = con.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ( (line = br.readLine()) != null )
                    buffer.append(line + "\r\n");
                is.close();
                con.disconnect();
                return buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return "Erro na comunicação";
            }
        }
        // trata os dados retornados
        @Override
        protected void onPostExecute(String retorno) {
            EditText etEstadoComunicacao = (EditText) findViewById(R.id.etComunicacao);
            if (retorno.equals("Desconectado"))
            {
                etEstadoComunicacao.setText(retorno);
                return;
            }
            etEstadoComunicacao.setText("Conectado");
            String[] sensoresSeparados = retorno.split("\n");
            for (int i = 0; i < sensoresSeparados.length; i++)
            {
                String[] sensorargumento = sensoresSeparados[i].split("=");
                switch (sensorargumento[0])
                {
                    case "sensorLinha":
                        EditText etEstadoLinha = (EditText) findViewById(R.id.etEstadoLinha);
                        etEstadoLinha.setText("Caminho: " + sensorargumento[1]);
                        break;
                    case "sensorDistancia":
                        EditText etEstadoDistancia = (EditText) findViewById(R.id.etDistancia);
                        etEstadoDistancia.setText("Distancia: " + sensorargumento[1]);
                        break;
                }
            }
            super.onPostExecute(retorno);
        }
    }
    //aaaaaaaaaaaaaa
}