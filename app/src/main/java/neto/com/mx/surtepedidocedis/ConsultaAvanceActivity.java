package neto.com.mx.surtepedidocedis;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.ksoap2.serialization.SoapObject;

import java.util.HashMap;
import java.util.Map;

import neto.com.mx.surtepedidocedis.beans.ArticuloVO;
import neto.com.mx.surtepedidocedis.beans.CodigosGuardadosVO;
import neto.com.mx.surtepedidocedis.dialogos.ViewDialog;
import neto.com.mx.surtepedidocedis.providers.ProviderGuardarArticulos;
import neto.com.mx.surtepedidocedis.utiles.TiposAlert;

import static neto.com.mx.surtepedidocedis.utiles.Constantes.METHOD_NAME_GUARDARARTSCONTADOSVERIFICADOR;
import static neto.com.mx.surtepedidocedis.utiles.Constantes.NAMESPACE;

public class ConsultaAvanceActivity extends AppCompatActivity {

    private int ACCION_GUARDA = 0;
    private HashMap<Long, ArticuloVO> mapaCatalogo = null;
    private HashMap<String, Integer> mapaCodigosNoRem = null;
    private String folio = "";
    private String nombreEmpleado = "";
    private String numeroEmpleado = "";
    private String nombreZona = "";
    private String nombreTienda = "";
    private int idZona = 0;
    String version = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta_avance);
        getSupportActionBar().hide();

        mapaCatalogo = (HashMap<Long, ArticuloVO>)this.getIntent().getSerializableExtra("mapaCat");
        mapaCodigosNoRem = (HashMap<String, Integer>)this.getIntent().getSerializableExtra("mapaCodNoRem");
        folio = new String(this.getIntent().getStringExtra("folio"));
        nombreEmpleado = new String(this.getIntent().getStringExtra("nombreEmpleado").trim());
        numeroEmpleado = new String(this.getIntent().getStringExtra("numeroEmpleado").trim());
        nombreZona = new String(this.getIntent().getStringExtra("nombreZona").trim());
        nombreTienda = new String(this.getIntent().getStringExtra("nombreTienda").trim());
        idZona = this.getIntent().getIntExtra("idZona", 0);

        guardaAvance();

        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch(PackageManager.NameNotFoundException ne) {
            Log.e("CARGA_FOLIO_TAG", "Error al obtener la versión: " + ne.getMessage());
        }
    }

    public void regresarMenuFront(View view) {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);
        regresar();
    }

    public void guardaAvance() {
        ACCION_GUARDA = 0;
        ejecutaWS();
    }

    public void ejecutaWS() {
        // Do something in response to button
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // Operaciones http
            try {
                //String url = Constantes.URL_STRING + "guardarArticulosContados";

                final ProgressDialog mDialog = new ProgressDialog(this);
                mDialog.setMessage("Consultando avance...");
                mDialog.setCancelable(false);
                mDialog.setInverseBackgroundForced(false);
                mDialog.show();

                SoapObject request = new SoapObject(NAMESPACE,METHOD_NAME_GUARDARARTSCONTADOSVERIFICADOR);

                request.addProperty("folio", folio);
                request.addProperty("articulosArray", obtieneCadenaArticulos());
                request.addProperty("cantidadesArray", obtieneCadenaCajas());
                request.addProperty("tipoGuardado", String.valueOf(ACCION_GUARDA));
                request.addProperty("zonaId", String.valueOf(idZona));
                request.addProperty("usuario", numeroEmpleado);
                request.addProperty("numeroSerie", Build.SERIAL);
                request.addProperty("version", version);

                System.out.println("///////////////////////////REQUEST"+request);

                ProviderGuardarArticulos.getInstance(this).getGuardarArticulos(request, new ProviderGuardarArticulos.interfaceGuardarArticulos() {
                    @Override
                    public void resolver(CodigosGuardadosVO respuestaGuardaArticulos) {
                        mDialog.dismiss();
                        System.out.println("*** 1 *** response: " + respuestaGuardaArticulos);


                        if (respuestaGuardaArticulos != null) {
                            if (respuestaGuardaArticulos.getCodigo() == 0) {

                                TextView artAsignadosText = (TextView) findViewById(R.id.artAsignados);
                                artAsignadosText.setText(String.valueOf(respuestaGuardaArticulos.getTotalArticulosEnPedido()));
                                System.out.println("/////////////////////////////////////ENpEDIDO"+respuestaGuardaArticulos.getTotalArticulosEnPedido());

                                TextView artContadosText = (TextView) findViewById(R.id.artContados);
                                artContadosText.setText(String.valueOf(respuestaGuardaArticulos.getTotalArticulosCapturados()));
                                System.out.println("////////////////////////////////CAPTURADOS"+respuestaGuardaArticulos.getTotalArticulosCapturados());

                                TextView cajasSurtidosText = (TextView) findViewById(R.id.cajasAsignadas);
                                cajasSurtidosText.setText(String.valueOf(respuestaGuardaArticulos.getTotalCajasAsignadas()));
                                System.out.println("////////////////////////////////Asignadas"+respuestaGuardaArticulos.getTotalCajasAsignadas());

                                TextView cajasContadosText = (TextView) findViewById(R.id.cajasContados);
                                cajasContadosText.setText(String.valueOf(respuestaGuardaArticulos.getTotalCajasPickeadas()));
                                System.out.println("////////////////////////////////Pickeadas"+respuestaGuardaArticulos.getTotalCajasPickeadas());

                                TextView porcentajeArticulosText = (TextView) findViewById(R.id.porcentajeArticulos);
                                if (respuestaGuardaArticulos.getTotalArticulosEnPedido() != 0) {
                                    int porcentajeArticulos = (int) ((respuestaGuardaArticulos.getTotalArticulosCapturados() * 100) / respuestaGuardaArticulos.getTotalArticulosEnPedido());
                                    porcentajeArticulosText.setText(String.valueOf(porcentajeArticulos + "%"));

                                    ProgressBar progressBarArticulos = (ProgressBar) findViewById(R.id.progressBarArticulos);
                                    ObjectAnimator animation = ObjectAnimator.ofInt(progressBarArticulos, "progress", 0, porcentajeArticulos); // see this max value coming back here, we animale towards that value
                                    animation.setDuration(2000); //in milliseconds
                                    animation.setInterpolator(new DecelerateInterpolator());
                                    animation.start();
                                } else {
                                    porcentajeArticulosText.setText("0%");
                                }

                                TextView porcentajeCajasText = (TextView) findViewById(R.id.porcentajeCajas);
                                if (respuestaGuardaArticulos.getTotalCajasAsignadas() != 0) {
                                    int porcentajeCajas = (int) ((respuestaGuardaArticulos.getTotalCajasPickeadas() * 100) / respuestaGuardaArticulos.getTotalCajasAsignadas());
                                    porcentajeCajasText.setText(String.valueOf(porcentajeCajas + "%"));

                                    ProgressBar progressBarCajas = (ProgressBar) findViewById(R.id.progressBarCajas);
                                    ObjectAnimator animationCajas = ObjectAnimator.ofInt(progressBarCajas, "progress", 0, porcentajeCajas); // see this max value coming back here, we animale towards that value
                                    animationCajas.setDuration(2000); //in milliseconds
                                    animationCajas.setInterpolator(new DecelerateInterpolator());
                                    animationCajas.start();
                                } else {
                                    porcentajeCajasText.setText("0%");
                                }
                            } else {
                                ViewDialog alert = new ViewDialog(ConsultaAvanceActivity.this);
                                alert.showDialog(ConsultaAvanceActivity.this, "Error en el servicio que guarda los códigos: " + respuestaGuardaArticulos.getMensaje(), null, TiposAlert.ERROR);
                            }
                        } else {
                            mDialog.dismiss();
                            System.out.println("*** 2 ***");
                            //cuando el tiempo del servicio exedio el timeout
                            ViewDialog alert = new ViewDialog(ConsultaAvanceActivity.this);
                            alert.showDialog(ConsultaAvanceActivity.this, "Error en el servicio que guarda los códigos", null, TiposAlert.ERROR);
                        }

                    }
                });








                /*StringRequest strRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                mDialog.dismiss();
                                System.out.println("*** 1 *** response: " + response);

                                CodigosGuardadosVO codigosFaltantes = new CodigosGuardadosVO();
                                generaFaltantes(response, codigosFaltantes);

                                if(codigosFaltantes.getCodigo() == 0) {
                                    TextView artAsignadosText = (TextView) findViewById(R.id.artAsignados);
                                    artAsignadosText.setText(String.valueOf(codigosFaltantes.getTotalArticulosEnPedido()));

                                    TextView artContadosText = (TextView) findViewById(R.id.artContados);
                                    artContadosText.setText(String.valueOf(codigosFaltantes.getTotalArticulosCapturados()));

                                    TextView cajasSurtidosText = (TextView) findViewById(R.id.cajasAsignadas);
                                    cajasSurtidosText.setText(String.valueOf(codigosFaltantes.getTotalCajasAsignadas()));

                                    TextView cajasContadosText = (TextView) findViewById(R.id.cajasContados);
                                    cajasContadosText.setText(String.valueOf(codigosFaltantes.getTotalCajasPickeadas()));

                                    TextView porcentajeArticulosText = (TextView) findViewById(R.id.porcentajeArticulos);
                                    if(codigosFaltantes.getTotalArticulosEnPedido() != 0) {
                                        int porcentajeArticulos= (int)((codigosFaltantes.getTotalArticulosCapturados() * 100) / codigosFaltantes.getTotalArticulosEnPedido());
                                        porcentajeArticulosText.setText(String.valueOf(porcentajeArticulos + "%"));

                                        ProgressBar progressBarArticulos = (ProgressBar) findViewById(R.id.progressBarArticulos);
                                        ObjectAnimator animation = ObjectAnimator.ofInt (progressBarArticulos, "progress", 0, porcentajeArticulos); // see this max value coming back here, we animale towards that value
                                        animation.setDuration (2000); //in milliseconds
                                        animation.setInterpolator (new DecelerateInterpolator());
                                        animation.start ();
                                    } else {
                                        porcentajeArticulosText.setText("0%");
                                    }

                                    TextView porcentajeCajasText = (TextView) findViewById(R.id.porcentajeCajas);
                                    if(codigosFaltantes.getTotalCajasAsignadas() != 0) {
                                        int porcentajeCajas= (int)((codigosFaltantes.getTotalCajasPickeadas() * 100) / codigosFaltantes.getTotalCajasAsignadas());
                                        porcentajeCajasText.setText(String.valueOf(porcentajeCajas + "%"));

                                        ProgressBar progressBarCajas = (ProgressBar) findViewById(R.id.progressBarCajas);
                                        ObjectAnimator animationCajas = ObjectAnimator.ofInt (progressBarCajas, "progress", 0, porcentajeCajas); // see this max value coming back here, we animale towards that value
                                        animationCajas.setDuration (2000); //in milliseconds
                                        animationCajas.setInterpolator (new DecelerateInterpolator());
                                        animationCajas.start ();
                                    } else {
                                        porcentajeCajasText.setText("0%");
                                    }
                                } else {
                                    ViewDialog alert = new ViewDialog(ConsultaAvanceActivity.this);
                                    alert.showDialog(ConsultaAvanceActivity.this, "Error en el servicio que guarda los códigos: " + codigosFaltantes.getMensaje(), null, TiposAlert.ERROR);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                mDialog.dismiss();
                                //Toast.makeText(getApplicationContext(), "Error en el WS que guarda los códigos: " + error.toString(), Toast.LENGTH_SHORT).show();
                                ViewDialog alert = new ViewDialog(ConsultaAvanceActivity.this);
                                alert.showDialog(ConsultaAvanceActivity.this, "Error en el servicio que guarda los códigos: favor de validar la comunicación del dispositivo", null, TiposAlert.ERROR);
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();

                        params.put("folio", folio);
                        params.put("articulosArray", obtieneCadenaArticulos());
                        params.put("cantidadesArray", obtieneCadenaCajas());
                        params.put("tipoGuardado", String.valueOf(ACCION_GUARDA));
                        params.put("zonaId", String.valueOf(idZona));
                        params.put("usuario", numeroEmpleado);
                        params.put("numeroSerie", Build.SERIAL);
                        params.put("version", version);

                        return params;
                    }
                };
                AppController.getInstance().addToRequestQueue(strRequest, "tag");*/

            } catch(Exception me) {
                ViewDialog alert = new ViewDialog(ConsultaAvanceActivity.this);
                alert.showDialog(ConsultaAvanceActivity.this, "URL no disponible: favor de validar la conexión a Internet", null, TiposAlert.ERROR);
            }
        } else {
            // Mostrar errores
            ViewDialog alert = new ViewDialog(ConsultaAvanceActivity.this);
            alert.showDialog(ConsultaAvanceActivity.this, "No hay conexión HTTP", null, TiposAlert.ERROR);
        }
    }

    /*public void generaFaltantes(String response, CodigosGuardadosVO codigos) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput( new StringReader(response) );

            int eventType = xpp.getEventType();
            List<CodigoBarraVO> listaCodigos = new ArrayList<CodigoBarraVO>();

            CodigoBarraVO codigoBarraVO = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("totalCajasPickeadas")) {
                        eventType = xpp.next(); // advance to inner text
                        codigos.setTotalCajasPickeadas(Integer.parseInt(xpp.getText()));
                    } else if (xpp.getName().equals("totalCajasAsignadas")) {
                        eventType = xpp.next(); // advance to inner text
                        codigos.setTotalCajasAsignadas(Integer.parseInt(xpp.getText()));
                    } else if (xpp.getName().equals("articulosContados")) {
                        eventType = xpp.next(); // advance to inner text
                        codigos.setTotalArticulosCapturados(Integer.parseInt(xpp.getText()));
                    } else if (xpp.getName().equals("articulosAsignados")) {
                        eventType = xpp.next(); // advance to inner text
                        codigos.setTotalArticulosEnPedido(Integer.parseInt(xpp.getText()));
                    } else if (xpp.getName().equals("errorCode")) {
                        eventType = xpp.next(); // advance to inner text
                        codigos.setCodigo(Integer.parseInt(xpp.getText()));
                    } else if (xpp.getName().equals("errorDesc")) {
                        eventType = xpp.next(); // advance to inner text
                        codigos.setMensaje(xpp.getText());
                    } else if(xpp.getName().equals("articulosConDiferencia")) {
                        codigoBarraVO = new CodigoBarraVO();
                    } else if(xpp.getName().equals("articuloId")) {
                        eventType = xpp.next(); // advance to inner text
                        codigoBarraVO.setArticuloId(Long.parseLong(xpp.getText()));
                    } else if(xpp.getName().equals("cantidadPickeada")) {
                        eventType = xpp.next(); // advance to inner text
                        codigoBarraVO.setCajasCapturadas(Integer.parseInt(xpp.getText()));
                    } else if(xpp.getName().equals("cantidadAsignada")) {
                        eventType = xpp.next(); // advance to inner text
                        codigoBarraVO.setCajasPedido(Integer.parseInt(xpp.getText()));
                    } else if(xpp.getName().equals("codigobarras")) {
                        eventType = xpp.next(); // advance to inner text
                        codigoBarraVO.setCodigoBarras(xpp.getText());
                    } else if(xpp.getName().equals("nombre")) {
                        eventType = xpp.next(); // advance to inner text
                        codigoBarraVO.setNombreArticulo(xpp.getText());
                    }
                } else if(eventType == XmlPullParser.END_TAG) {
                    if(xpp.getName().equals("articulosConDiferencia")) {
                        listaCodigos.add(codigoBarraVO);
                    }
                }
                eventType = xpp.next();
            }
            codigos.setArticulosDiferencias(listaCodigos.toArray(new CodigoBarraVO[listaCodigos.size()]));

            int contador = 0;
            for (Map.Entry<Long, ArticuloVO> entry : mapaCatalogo.entrySet()) {
                if(entry.getValue().isEsCapturado()) {
                    contador++;
                }
            }
        } catch(Exception e) {
            ViewDialog alert = new ViewDialog(ConsultaAvanceActivity.this);
            alert.showDialog(ConsultaAvanceActivity.this, "Error al formar las diferencias del xml: " + e.getMessage(), null, TiposAlert.ERROR);
        }
    }*/

    public String obtieneCadenaArticulos() {
        StringBuffer cadena = new StringBuffer();
        for (Map.Entry<Long, ArticuloVO> entry : mapaCatalogo.entrySet()) {
            cadena.append(entry.getKey());
            cadena.append("|");
        }
        cadena.replace(cadena.lastIndexOf("|"), cadena.lastIndexOf("|") + 1, "" );
        System.out.println(":::: Articulos = " + cadena);
        return cadena.toString();
    }

    public String obtieneCadenaCajas() {
        StringBuffer cadena = new StringBuffer();
        for (Map.Entry<Long, ArticuloVO> entry : mapaCatalogo.entrySet()) {
            cadena.append(entry.getValue().getTotalCajasPickeadas());
            cadena.append("|");
        }
        cadena.replace(cadena.lastIndexOf("|"), cadena.lastIndexOf("|") + 1, "" );
        System.out.println(":::: Cajas = " + cadena);
        return cadena.toString();
    }

    public void regresar() {
        onBackPressed();
    }

}
