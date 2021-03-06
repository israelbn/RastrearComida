package br.com.sebrae.rastrearcomida.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import br.com.sebrae.rastrearcomida.R;
import br.com.sebrae.rastrearcomida.modelo.Endereco;
import br.com.sebrae.rastrearcomida.task.RecebeEnderecoTask;

/**
 * Created by Israel on 04/05/2015.
 */
public class BuscarEndereco extends Activity{
    private String cep;
    private Endereco endereco;
    private EditText txtCep;
    private EditText txtNumero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buscar_endereco);

        txtCep = (EditText) findViewById(R.id.cep);
        txtNumero = (EditText) findViewById(R.id.numero);

        Button btnBuscar = (Button) findViewById(R.id.btnBuscarEndereco);
        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cep = txtCep.getText().toString();
                String url = "http://viacep.com.br/ws/"+cep+"/json/";
                new JsonDownload().execute(url);
            }
        });
    }

    //get e set
    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    class JsonDownload extends AsyncTask<String, Void, List<Endereco>> {
        ProgressDialog dialog;

        //Exibe pop-up indicando que está sendo feito o download do JSON
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(BuscarEndereco.this, "Aguarde",
                    "Buscando endereço...");
        }

        @Override
        protected List<Endereco> doInBackground(String... params) {
            String urlString = params[0];
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(urlString);
            try {
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream instream = entity.getContent();
                    String json = getStringFromInputStream(instream);
                    instream.close();
                    List<Endereco> enderecos = getEndereco(json);
                    return enderecos;
                }
            } catch (Exception e) {
                Log.e("Erro", "Falha ao acessar Web service", e);
            }
            return null;
        }

        //Depois de executada a chamada do serviço
        @Override
        protected void onPostExecute(List<Endereco> result) {
            super.onPostExecute(result);
            dialog.dismiss();
            setEndereco(result.get(0));
            endereco.setNumero(txtNumero.getText().toString());

            // Chamando outra activity, passando o objeto endereco
            Intent intent = new Intent(BuscarEndereco.this, ListaEmpresa.class);
            intent.putExtra("endereco", endereco);
            startActivity(intent);
        }

        private List<Endereco> getEndereco(String jsonString){
            List<Endereco> enderecos = new ArrayList<Endereco>();
            try {
                JSONObject pessoa = new JSONObject(jsonString);
                Endereco obj = new Endereco();
                obj.setLogradouro(pessoa.getString("logradouro"));
                obj.setBairro(pessoa.getString("bairro"));
                obj.setCidade(pessoa.getString("localidade"));
                obj.setEstado(pessoa.getString("uf"));
                enderecos.add(obj);
            } catch (Exception e) {
                Log.e("Erro", "Erro no parsing do JSON", e);
            }
            return enderecos;
        }

        //Converte objeto InputStream para String
        private String getStringFromInputStream(InputStream is) {
            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return sb.toString();
        }
    }
}
