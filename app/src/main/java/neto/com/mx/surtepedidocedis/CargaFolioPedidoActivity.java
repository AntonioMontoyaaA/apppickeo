package neto.com.mx.surtepedidocedis;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import neto.com.mx.surtepedidocedis.beans.PedidoVO;
import neto.com.mx.surtepedidocedis.beans.ZonaPickeoVO;
import neto.com.mx.surtepedidocedis.dialogos.BienvenidaDialog;
import neto.com.mx.surtepedidocedis.dialogos.ViewDialog;
import neto.com.mx.surtepedidocedis.utiles.Constantes;
import neto.com.mx.surtepedidocedis.utiles.TiposAlert;

public class CargaFolioPedidoActivity extends AppCompatActivity {

    Context context;
    String version = "";
    String nombreEmpleado;
    String numeroEmpleado;
    EditText editTextFolio = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carga_folio_pedido);
        context = this.getApplicationContext();
        editTextFolio = (EditText) findViewById(R.id.folioPedidoText);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
            editTextFolio.setCursorVisible(false);
        }else{
            editTextFolio.setInputType(InputType.TYPE_NULL);
        }
        editTextFolio.requestFocus();
        editTextFolio.setOnEditorActionListener(escaneaListener);
        getSupportActionBar().hide();

        numeroEmpleado = new String(this.getIntent().getStringExtra("numeroEmpleado").trim());
        nombreEmpleado = new String(this.getIntent().getStringExtra("nombreEmpleado").trim());

        BienvenidaDialog alert = new BienvenidaDialog(CargaFolioPedidoActivity.this);
        alert.showDialog(CargaFolioPedidoActivity.this, nombreEmpleado);



        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch(PackageManager.NameNotFoundException ne) {
            Log.e("CARGA_FOLIO_TAG", "Error al obtener la versión: " + ne.getMessage());
        }
    }

    public void regresarMenuFront(View view) {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);
        regresaMenu();
    }

    public void salirMenuFront(View view) {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);
        salirMenu();
    }

    public void regresaMenu() {
        Intent intent = new Intent(getApplicationContext(), CargaFolioPedidoActivity.class);
        startActivity(intent);
    }

    public void salirMenu() {
        Intent intent = new Intent(getApplicationContext(), SplashScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("LOGOUT", true);
        startActivity(intent);
    }

    TextView.OnEditorActionListener escaneaListener = new TextView.OnEditorActionListener(){

        public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {

            if (actionId == EditorInfo.IME_NULL
                    && event.getAction() == KeyEvent.ACTION_DOWN) {
                ejecutaWS();
            }
            return true;
        }
    };

    public void ejecutaWS() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            // Operaciones http
            try {
                EditText editText = (EditText) findViewById(R.id.folioPedidoText);

                if(!editText.getText().toString().equals("")) {
                    final ProgressDialog mDialog = new ProgressDialog(this);
                    mDialog.setMessage("Buscando folio del pedido...");
                    mDialog.setCancelable(false);
                    mDialog.setInverseBackgroundForced(false);
                    mDialog.show();

                    String url = Constantes.URL_STRING + "validaPedido";

                    StringRequest strRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    mDialog.dismiss();
                                    EditText editText = (EditText) findViewById(R.id.folioPedidoText);

                                    PedidoVO pedidoVO = new PedidoVO();
                                    generaRespuesta(response, pedidoVO);

                                    if(pedidoVO.isPedidoValido()) {
                                        Intent intent = new Intent(context, CargaZonasDisponiblesActivity.class);
                                        intent.putExtra("folio", editText.getText().toString());
                                        intent.putExtra("listaZonas", (Serializable) pedidoVO.getListaZonas());
                                        intent.putExtra("nombreEmpleado", nombreEmpleado);
                                        intent.putExtra("numeroEmpleado", numeroEmpleado);
                                        intent.putExtra("nombreTienda", pedidoVO.getNombreTienda());
                                        startActivity(intent);
                                    } else {
                                        ViewDialog alert = new ViewDialog(CargaFolioPedidoActivity.this);
                                        alert.showDialog(CargaFolioPedidoActivity.this, "Pedido no válido: " +
                                                        pedidoVO.getMensaje() + "\n\n* Para mayor información puedes preguntar a tu líder o a mesa de ayuda", null,
                                                TiposAlert.CORRECTO);
                                    }
                                    editText.setText("");
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    mDialog.dismiss();
                                    EditText editText = (EditText) findViewById(R.id.folioPedidoText);
                                    editText.setText("");
                                    System.out.println("*** 2 ***");
                                    ViewDialog alert = new ViewDialog(CargaFolioPedidoActivity.this);
                                    alert.showDialog(CargaFolioPedidoActivity.this, "Error al consumir el servicio que valida el folio del pedido ", null, TiposAlert.ERROR);
                                }
                            }) {
                        @Override
                        protected Map<String, String> getParams() {
                            EditText editText = (EditText) findViewById(R.id.folioPedidoText);

                            Map<String, String> params = new HashMap<String, String>();
                            params.put("folio", editText.getText().toString().trim());
                            params.put("numeroSerie", Build.SERIAL);
                            params.put("version", version);
                            params.put("usuario", numeroEmpleado);

                            return params;
                        }
                    };

                    strRequest.setRetryPolicy(new DefaultRetryPolicy(
                            50000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                    AppController.getInstance().addToRequestQueue(strRequest, "tag");
                } else {
                    ViewDialog alert = new ViewDialog(CargaFolioPedidoActivity.this);
                    alert.showDialog(CargaFolioPedidoActivity.this, "Escanea un folio de pedido", null, TiposAlert.ALERT);
                }
            } catch(Exception me) {
                ViewDialog alert = new ViewDialog(CargaFolioPedidoActivity.this);
                alert.showDialog(CargaFolioPedidoActivity.this, "URL no disponible: favor de validar la conexión a Internet", null, TiposAlert.ERROR);
            }
        } else {
            // Mostrar errores
            //EditText editText = (EditText) findViewById(R.id.folioPedidoText);
            //editText.setInputType(InputType.TYPE_NULL);
            editTextFolio.requestFocus();
            editTextFolio.setText("");
            ViewDialog alert = new ViewDialog(CargaFolioPedidoActivity.this);
            alert.showDialog(CargaFolioPedidoActivity.this, "No hay conexión HTTP", null, TiposAlert.ERROR);
        }
    }

    public void generaRespuesta(String response, PedidoVO pedidoVO) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput( new StringReader(response) );

            int eventType = xpp.getEventType();
            List<ZonaPickeoVO> listaZonas = new ArrayList<ZonaPickeoVO>();

            ZonaPickeoVO zonaPickeoVO = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("errorCode")) {
                        eventType = xpp.next(); // advance to inner text
                        pedidoVO.setCodigo(Integer.parseInt(xpp.getText()));
                    }  else if (xpp.getName().equals("errorDesc")) {
                        eventType = xpp.next(); // advance to inner text
                        pedidoVO.setMensaje(xpp.getText());
                    } else if (xpp.getName().equals("nombreTienda")) {
                        eventType = xpp.next(); // advance to inner text
                        pedidoVO.setNombreTienda(xpp.getText());
                    } else if (xpp.getName().equals("esPedidoValido")) {
                        eventType = xpp.next(); // advance to inner text
                        if(xpp.getText().equals("true")) {
                            pedidoVO.setPedidoValido(true);
                        } else {
                            pedidoVO.setPedidoValido(false);
                        }
                    } else if(xpp.getName().equals("listaZonas")) {
                        zonaPickeoVO = new ZonaPickeoVO();
                    } else if(xpp.getName().equals("zonaId")) {
                        eventType = xpp.next(); // advance to inner text
                        zonaPickeoVO.setIdZona(Integer.parseInt(xpp.getText()));
                    } else if(xpp.getName().equals("descripcionZona")) {
                        eventType = xpp.next(); // advance to inner text
                        zonaPickeoVO.setDescripcionZona(xpp.getText());
                    } else if(xpp.getName().equals("esZonaValida")) {
                        eventType = xpp.next(); // advance to inner text
                        zonaPickeoVO.setZonaValida(Integer.parseInt(xpp.getText()));
                    } else if(xpp.getName().equals("usuarioConteo")) {
                        eventType = xpp.next(); // advance to inner text
                        zonaPickeoVO.setNombreUsuario(xpp.getText());
                    } else if(xpp.getName().equals("nombreCorto")) {
                        eventType = xpp.next(); // advance to inner text
                        zonaPickeoVO.setNombreCorto(xpp.getText());
                    }
                } else if(eventType == XmlPullParser.END_TAG) {
                    if(xpp.getName().equals("listaZonas")) {
                        listaZonas.add(zonaPickeoVO);
                    }
                }
                eventType = xpp.next();
            }
            pedidoVO.setListaZonas(listaZonas);
        } catch(Exception e) {
            ViewDialog alert = new ViewDialog(CargaFolioPedidoActivity.this);
            alert.showDialog(CargaFolioPedidoActivity.this, "Error al formar las zonas desde el xml: " + e.getMessage(), null, TiposAlert.ERROR);
        }
    }

    @Override
    public void onBackPressed() {
    }
}
