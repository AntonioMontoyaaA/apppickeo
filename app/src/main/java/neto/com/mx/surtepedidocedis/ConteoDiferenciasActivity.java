package neto.com.mx.surtepedidocedis;

import android.annotation.SuppressLint;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import neto.com.mx.surtepedidocedis.beans.ArticuloVO;
import neto.com.mx.surtepedidocedis.beans.CodigoBarraVO;
import neto.com.mx.surtepedidocedis.beans.CodigosGuardadosVO;
import neto.com.mx.surtepedidocedis.dialogos.DiferenciaAclaradaDialog;
import neto.com.mx.surtepedidocedis.dialogos.ViewDialog;
import neto.com.mx.surtepedidocedis.dialogos.ViewDialogoConfirma;
import neto.com.mx.surtepedidocedis.dialogos.ViewDialogoErrorActivity;
import neto.com.mx.surtepedidocedis.utiles.Constantes;
import neto.com.mx.surtepedidocedis.utiles.TiposAlert;

public class ConteoDiferenciasActivity extends AppCompatActivity {

    EditText editTextCodigos = null;
    private String codigoBarras = "";
    private long articuloIdBusqueda = 0;
    private HashMap<Long, ArticuloVO> mapaCatalogo = new HashMap<Long, ArticuloVO>();
    private String folio = "";
    private int ACCION_GUARDA = 0;
    private boolean esGuardadoPorCodigos = false;

    ScrollView mScrollView = null;
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.5F);

    private String nombreEmpleado = "";
    private String numeroEmpleado = "";
    private String nombreTienda = "";
    private String nombreZona = "";

    //Variables para Dialogo de confirmación
    boolean existeCodigo = false;
    String descripcionCodigoBarras = "";
    int cantidadCapturadaDialogo = 0;
    int cantidadEmbarcadaDialogo = 0;
    String descripcionNormaEmpaqueDialogo = "";
    String descripcionNormaEmpaqueWSDialogo = "";
    private int idZona = 0;
    String version = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conteo_diferencias);

        getSupportActionBar().hide();

        mapaCatalogo = (HashMap<Long, ArticuloVO>)this.getIntent().getSerializableExtra("mapaCat");
        folio = new String(this.getIntent().getStringExtra("folio"));
        nombreEmpleado = new String(this.getIntent().getStringExtra("nombreEmpleado").trim());
        numeroEmpleado = new String(this.getIntent().getStringExtra("numeroEmpleado").trim());
        nombreTienda = new String(this.getIntent().getStringExtra("nombreTienda").trim());
        nombreZona = new String(this.getIntent().getStringExtra("nombreZona").trim());
        idZona = this.getIntent().getIntExtra("idZona", 0);
        editTextCodigos = (EditText) findViewById(R.id.codigoBarraText);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
            editTextCodigos.setCursorVisible(false);
        }else{
            editTextCodigos.setInputType(InputType.TYPE_NULL);
        }
        editTextCodigos.requestFocus();
        editTextCodigos.setOnEditorActionListener(codigosListener);

        imprimeDiferencias();

        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch(PackageManager.NameNotFoundException ne) {
            Log.e("CARGA_FOLIO_TAG", "Error al obtener la versión: " + ne.getMessage());
        }
    }

    private int totalCajasSurtidas = 0;
    private int totalCajasRecibidas = 0;

    TextView.OnEditorActionListener codigosListener = new TextView.OnEditorActionListener(){
        public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {
            try {
                if (actionId == EditorInfo.IME_NULL
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    existeCodigo = false;

                    //EditText editText = (EditText) findViewById(R.id.codigoBarraText);
                    codigoBarras =  editTextCodigos.getText().toString().trim();
                    descripcionCodigoBarras = codigoBarras;

                    if(!codigoBarras.equals("")) {

                        //Busca código de barras en el catálogo
                        for (Map.Entry<Long, ArticuloVO> entry : mapaCatalogo.entrySet()) {
                            if(entry.getValue().getCodigos().contains(codigoBarras)) {

                                articuloIdBusqueda = entry.getKey();
                                descripcionCodigoBarras = entry.getValue().getNombreArticulo();
                                entry.getValue().setTotalCajasPickeadas(entry.getValue().getTotalCajasPickeadas() + 1);
                                cantidadCapturadaDialogo = entry.getValue().getTotalCajasPickeadas();
                                cantidadEmbarcadaDialogo = entry.getValue().getTotalCajasAsignadas();
                                existeCodigo = true;
                                entry.getValue().setEsCapturado(true);
                                if(entry.getValue().getTotalCajasPickeadas() != entry.getValue().getTotalCajasAsignadas()) {
                                    entry.getValue().setEsBuscadoDiferencias(true);
                                }

                                if(cantidadCapturadaDialogo <= cantidadEmbarcadaDialogo) {
                                    guardaAvancePorCodigos();
                                    DiferenciaAclaradaDialog diferenciaAclaradaDialog = new DiferenciaAclaradaDialog(ConteoDiferenciasActivity.this);
                                    diferenciaAclaradaDialog.showDialog(ConteoDiferenciasActivity.this, entry.getValue().getNombreArticulo(),
                                            entry.getValue().getTotalCajasPickeadas(),entry.getValue().getTotalCajasAsignadas());
                                }
                            } else {
                                entry.getValue().setEsBuscadoDiferencias(false);
                                entry.getValue().setEsCapturado(false);
                            }
                        }

                        if(cantidadCapturadaDialogo > cantidadEmbarcadaDialogo) {
                            ViewDialog alert = new ViewDialog(ConteoDiferenciasActivity.this);
                            alert.showDialog(ConteoDiferenciasActivity.this, "No puedes pickear más cajas de las asignadas", null, TiposAlert.ERROR);

                            for (Map.Entry<Long, ArticuloVO> entry : mapaCatalogo.entrySet()) {
                                if (entry.getValue().getCodigos().contains(codigoBarras)) {
                                    entry.getValue().setTotalCajasPickeadas(entry.getValue().getTotalCajasAsignadas());
                                    entry.getValue().setEsCapturado(false);
                                }
                            }
                            ingresaCodigoBarras();



                            /*ViewDialogoConfirma confirma = new ViewDialogoConfirma(ConteoDiferenciasActivity.this);
                            confirma.showDialog(ConteoDiferenciasActivity.this, cantidadCapturadaDialogo, cantidadEmbarcadaDialogo);
                            confirma.setViewDialogoConfirmaListener(new ViewDialogoConfirma.ViewDialogoConfirmaListener() {
                                @Override
                                public void onIncrementaContador() {
                                    ingresaCodigoBarras();
                                }
                                @Override
                                public void onLimpiaCampo() {

                                    for (Map.Entry<Long, ArticuloVO> entry : mapaCatalogo.entrySet()) {
                                        if (entry.getValue().getCodigos().contains(codigoBarras)) {
                                            entry.getValue().setTotalCajasPickeadas(entry.getValue().getTotalCajasPickeadas() - 1);
                                            entry.getValue().setEsCapturado(false);
                                        }
                                    }
                                }
                            });*/
                        } else {
                            ingresaCodigoBarras();
                        }
                        editTextCodigos.setText("");
                    }



                }
            } catch(Exception e) {
                //Toast.makeText(CargaCodigosBarraActivity.this, "Error al leer el código de barras: " + e.getMessage(), Toast.LENGTH_LONG).show();
                ViewDialog alert = new ViewDialog(ConteoDiferenciasActivity.this);
                alert.showDialog(ConteoDiferenciasActivity.this, "Error al leer el código de barras: " + e.getMessage(), null, TiposAlert.ERROR);
            }
            return true;
        }
    };

    public void imprimeDiferencias() {

        List<CodigoBarraVO> listaCodigos = new ArrayList<CodigoBarraVO>();

        //Coloca el elemento escaneado al inicio
        for (Map.Entry<Long, ArticuloVO> entry : mapaCatalogo.entrySet()) {
            if(entry.getValue().isEsBuscadoDiferencias()) {

                if(entry.getValue().getTotalCajasPickeadas() != entry.getValue().getTotalCajasAsignadas()) {
                    CodigoBarraVO codigo = new CodigoBarraVO();
                    codigo.setArticuloId(entry.getKey());
                    codigo.setNombreArticulo(entry.getValue().getNombreArticulo());
                    codigo.setCajasPedido(entry.getValue().getTotalCajasAsignadas());
                    codigo.setCajasCapturadas(entry.getValue().getTotalCajasPickeadas());
                    listaCodigos.add(codigo);
                    break;

                }
            }
        }

        //Coloca los otros elementos
        for (Map.Entry<Long, ArticuloVO> entry : mapaCatalogo.entrySet()) {
            if(entry.getValue().getTotalCajasPickeadas() != entry.getValue().getTotalCajasAsignadas() && !entry.getValue().isEsBuscadoDiferencias()) {
                CodigoBarraVO codigo = new CodigoBarraVO();
                codigo.setArticuloId(entry.getKey());
                codigo.setNombreArticulo(entry.getValue().getNombreArticulo());
                codigo.setCajasPedido(entry.getValue().getTotalCajasAsignadas());
                codigo.setCajasCapturadas(entry.getValue().getTotalCajasPickeadas());
                listaCodigos.add(codigo);
            }
        }

        if(listaCodigos.size() > 0) {
            dibujaDiferencias(listaCodigos.toArray(new CodigoBarraVO[listaCodigos.size()]));
        } else {
            finalizaConteo();
        }
    }

    public void ingresaCodigoBarras() {
        ACCION_GUARDA = 0;
        guardaAvancePorCodigos();
        if(!existeCodigo) {
            ViewDialog alert = new ViewDialog(ConteoDiferenciasActivity.this);
            alert.showDialog(ConteoDiferenciasActivity.this, "Artículo no encontrado - " + codigoBarras, null, TiposAlert.ERROR);

            //EditText editText = (EditText) findViewById(R.id.codigoBarraText);
            editTextCodigos.setText("");

        } else {
            limpiarTabla();
            imprimeDiferencias();
        }
    }

    public void limpiarTabla() {
        int count = ((TableLayout) findViewById(R.id.tabla_diferencias_view)).getChildCount();
        for (int i = 0; i < count; i++) {
            View child = ((TableLayout) findViewById(R.id.tabla_diferencias_view)).getChildAt(i);
            if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
        }
    }


    @SuppressLint("ResourceType")
    public void dibujaDiferencias(CodigoBarraVO[] codigos) {
        int width_Ancho = this.getResources().getConfiguration().screenWidthDp;
        int height_Largo = this.getResources().getConfiguration().screenHeightDp;
        TableLayout ll = (TableLayout) findViewById(R.id.tabla_diferencias_view);
        TableRow row = new TableRow(this);
        row.setGravity(Gravity.CENTER_HORIZONTAL);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT);
        row.setLayoutParams(lp);

        TextView tArticuloHeader = new TextView(this);
        int anchoArticulo = 400;
        int anchoTextoArticulo = 130;
        //System.out.println("///////////////********************* Ancho: " + width_Ancho + "/****************//////////////////");
        //System.out.println("///////////////********************* largo: " + height_Largo + "/****************//////////////////");
        if (width_Ancho > 350 && height_Largo > 740){
            anchoArticulo = 700;
            anchoTextoArticulo = 180;
        }
        tArticuloHeader.setWidth(anchoArticulo);
        tArticuloHeader.setTextSize(25);

        TextView tCantidadHeader = new TextView(this);
        tCantidadHeader.setWidth(200);
        tCantidadHeader.setTextSize(25);

        //This generates the caption row
        tArticuloHeader.setText("Artículo");
        //tArticuloHeader.setPadding(20, 3, 3, 3);
        if (width_Ancho <= 320){
            tArticuloHeader.setPadding(85, 0, 0, 0);
        }else{
            tArticuloHeader.setPadding(0, 0, 0, 0);
        }
        tArticuloHeader.setTextColor(getResources().getColor(R.color.colorFuenteActivo));

        tCantidadHeader.setText("Cant.");
        //tCantidadHeader.setPadding(5, 3, 3, 3);
        if (width_Ancho <= 320){
            tCantidadHeader.setPadding(-100, 0, 0, 0);
        }else{
            tCantidadHeader.setPadding(0, 0, 0, 0);
        }
        tCantidadHeader.setTextColor(getResources().getColor(R.color.colorFuenteActivo));
        tCantidadHeader.setGravity(Gravity.CENTER);

        row.addView(tArticuloHeader);
        row.addView(tCantidadHeader);
        ll.addView(row,0);

        for (int i = 1; i <= codigos.length; i++) {
            row = new TableRow(this);
            row.setGravity(Gravity.CENTER_HORIZONTAL);
            lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT);
            lp.height = anchoTextoArticulo;
            row.setLayoutParams(lp);

            TextView tArticulo = new TextView(this);
            tArticulo.setWidth(400);
            tArticulo.setHeight(80);
            tArticulo.setText("*" + codigos[i-1].getNombreArticulo());
            if (width_Ancho <= 320){
                tArticulo.setPadding(85, 20, 0, 0);
            }else{
                tArticulo.setPadding(0, 20, 0, 0);
            }
            tArticulo.setTextColor(getResources().getColor(R.color.colorFuenteAzul));
            tArticulo.setLayoutParams(lp);

            TextView tCantidad = new TextView(this);
            tCantidad.setWidth(150);
            tCantidad.setHeight(80);
            tCantidad.setText(codigos[i-1].getCajasCapturadas() + "/" + codigos[i-1].getCajasPedido());
            if (width_Ancho <= 320){
                tCantidad.setPadding(-100, 3, 3, 3);
            }else{
                tCantidad.setPadding(0, 3, 3, 3);
            }
            tCantidad.setTextColor(getResources().getColor(R.color.colorFuenteAzul));
            tCantidad.setTextSize(25);
            tCantidad.setLayoutParams(lp);
            tCantidad.setGravity(Gravity.CENTER);

            row.addView(tArticulo);
            row.addView(tCantidad);
            ll.addView(row,i);
        }
    }

    public void guardaAvancePorCodigos() {
        ACCION_GUARDA = 0;
        esGuardadoPorCodigos = true;
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
                String url = Constantes.URL_STRING + "guardarArticulosContados";

                int contadorOcurrencias = 0;

                final ProgressDialog mDialog = new ProgressDialog(this);
                mDialog.setMessage("Guardando códigos de barra...");
                mDialog.setCancelable(false);
                mDialog.setInverseBackgroundForced(false);
                if(!esGuardadoPorCodigos) {
                    mDialog.show();
                }

                StringRequest strRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                mDialog.dismiss();
                                System.out.println("*** 1 *** response en ConteoDiferencias: " + response);

                                CodigosGuardadosVO codigosFaltantes = new CodigosGuardadosVO();
                                generaFaltantes(response, codigosFaltantes);

                                if(codigosFaltantes.getCodigo() == 0) {
                                    if(ACCION_GUARDA == 1) {
                                        Intent intent = new Intent(ConteoDiferenciasActivity.this, DiferenciasRecibidasActivity.class);
                                        intent.putExtra("CodigosGuardados",codigosFaltantes);
                                        intent.putExtra("folio",folio);
                                        intent.putExtra("numeroEmpleado", numeroEmpleado);
                                        intent.putExtra("nombreEmpleado", nombreEmpleado);
                                        intent.putExtra("nombreZona", nombreZona);
                                        intent.putExtra("nombreTienda", nombreTienda);
                                        intent.putExtra("idZona", idZona);
                                        startActivity(intent);
                                    }
                                } else {
                                    ViewDialog alert = new ViewDialog(ConteoDiferenciasActivity.this);
                                    alert.showDialog(ConteoDiferenciasActivity.this, "Error al guardar los códigos: " + codigosFaltantes.getMensaje(), null, TiposAlert.ERROR);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if(!esGuardadoPorCodigos) {
                                    mDialog.dismiss();
                                    ViewDialog alert = new ViewDialog(ConteoDiferenciasActivity.this);
                                    alert.showDialog(ConteoDiferenciasActivity.this, "Error en el WS que guarda los códigos: favor de validar la comunicación del dispositivo", null, TiposAlert.ERROR);
                                }
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
                AppController.getInstance().addToRequestQueue(strRequest, "tag");
            } catch(Exception me) {
                if(!esGuardadoPorCodigos) {
                    ViewDialog alert = new ViewDialog(ConteoDiferenciasActivity.this);
                    alert.showDialog(ConteoDiferenciasActivity.this, "URL no disponible: favor de validar la conexión a Internet" + me.getMessage(), null, TiposAlert.ERROR);
                }
            }
        } else {
            // Mostrar errores
            if(!esGuardadoPorCodigos) {
                ViewDialog alert = new ViewDialog(ConteoDiferenciasActivity.this);
                alert.showDialog(ConteoDiferenciasActivity.this, "No hay conexión HTTP", null, TiposAlert.ERROR);
            }
        }
    }

    public String obtieneCadenaArticulos() {
        StringBuffer cadena = new StringBuffer();
        for (Map.Entry<Long, ArticuloVO> entry : mapaCatalogo.entrySet()) {
            if(ACCION_GUARDA == 0 && esGuardadoPorCodigos && entry.getValue().isEsCapturado()) {
                cadena.append(entry.getKey());
                cadena.append("|");
            } else if(ACCION_GUARDA == 1) {
                cadena.append(entry.getKey());
                cadena.append("|");
            }
        }
        cadena.replace(cadena.lastIndexOf("|"), cadena.lastIndexOf("|") + 1, "" );

        return cadena.toString();
    }

    public String obtieneCadenaCajas() {
        StringBuffer cadena = new StringBuffer();
        for (Map.Entry<Long, ArticuloVO> entry : mapaCatalogo.entrySet()) {
            if(ACCION_GUARDA == 0 && esGuardadoPorCodigos && entry.getValue().isEsCapturado()) {
                cadena.append(entry.getValue().getTotalCajasPickeadas());
                cadena.append("|");
            } else if(ACCION_GUARDA == 1) {
                cadena.append(entry.getValue().getTotalCajasPickeadas());
                cadena.append("|");
            }
        }
        cadena.replace(cadena.lastIndexOf("|"), cadena.lastIndexOf("|") + 1, "" );

        return cadena.toString();
    }

    public void generaFaltantes(String response, CodigosGuardadosVO codigos) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput( new StringReader(response) );

            int eventType = xpp.getEventType();
            List<CodigoBarraVO> listaCodigosFaltantes = new ArrayList<CodigoBarraVO>();

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
                        listaCodigosFaltantes.add(codigoBarraVO);
                    }
                }
                eventType = xpp.next();
            }
            codigos.setArticulosDiferencias(listaCodigosFaltantes.toArray(new CodigoBarraVO[listaCodigosFaltantes.size()]));

            int contador = 0;
            for (Map.Entry<Long, ArticuloVO> entry : mapaCatalogo.entrySet()) {
                if(entry.getValue().isEsCapturado()) {
                    contador++;
                }
            }
        } catch(Exception e) {
            ViewDialog alert = new ViewDialog(ConteoDiferenciasActivity.this);
            alert.showDialog(ConteoDiferenciasActivity.this, "Error al formar las diferencias del xml: " + e.getMessage(), null, TiposAlert.ERROR);
        }
    }

    @Override
    public void onBackPressed() {
    }

    public void finalizarMenuFront(View view) {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);
        cuentaCajasRecibidas();
        if(totalCajasRecibidas < totalCajasSurtidas) {
            ViewDialogoErrorActivity errorDialogo = new ViewDialogoErrorActivity(ConteoDiferenciasActivity.this);
            errorDialogo.showDialog(ConteoDiferenciasActivity.this, String.valueOf(totalCajasSurtidas-totalCajasRecibidas), false);
            errorDialogo.setViewDialogoErrorActivityListener(new ViewDialogoErrorActivity.ViewDialogoErrorActivityListener() {
                @Override
                public void onFinaliza() {
                    finalizaConteo();
                }
                @Override
                public void onConteoDiferencias() {
                }
            });
        } else {
            finalizaConteo();
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

    public void scrollPaginado(View view) {
        view.startAnimation(buttonClick);
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);

        mScrollView = (ScrollView)findViewById(R.id.container_scroll_view);
        mScrollView.post(new Runnable() {
            public void run() {
                mScrollView.smoothScrollBy(0,300);
            }
        });
    }

    public void cuentaCajasRecibidas() {
        totalCajasRecibidas = 0;
        for (ArticuloVO entry : mapaCatalogo.values()) {
            totalCajasRecibidas += entry.getTotalCajasPickeadas();
        }
    }

    public void finalizaConteo() {
        ACCION_GUARDA = 1;
        esGuardadoPorCodigos = false;
        ejecutaWS();
    }

    public void regresaMenu() {
        /*Intent intent = new Intent(ConteoDiferenciasActivity.this, CargaCodigosBarraActivity.class);
        intent.putExtra("folio", folio);
        intent.putExtra("descargaCatalogo", true);
        intent.putExtra("nombreEmpleado", nombreEmpleado);
        intent.putExtra("numeroEmpleado", numeroEmpleado);
        intent.putExtra("nombreZona", nombreZona);
        intent.putExtra("nombreTienda", nombreTienda);
        intent.putExtra("idZona", idZona);
        startActivity(intent);*/
        finish();

    }

    public void salirMenu() {
        Intent intent = new Intent(getApplicationContext(), CargaFolioPedidoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("LOGOUT", true);
        intent.putExtra("nombreEmpleado", nombreEmpleado);
        intent.putExtra("numeroEmpleado", numeroEmpleado);
        startActivity(intent);
    }
}
