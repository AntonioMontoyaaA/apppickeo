package neto.com.mx.surtepedidocedis;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;

import org.ksoap2.serialization.SoapObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import neto.com.mx.surtepedidocedis.beans.ArticuloVO;
import neto.com.mx.surtepedidocedis.beans.CodigosGuardadosVO;
import neto.com.mx.surtepedidocedis.dialogos.ViewDialog;
import neto.com.mx.surtepedidocedis.dialogos.ViewDialogoErrorActivity;
import neto.com.mx.surtepedidocedis.providers.ProviderGeneraCatalogo;
import neto.com.mx.surtepedidocedis.providers.ProviderGuardarArticulos;
import neto.com.mx.surtepedidocedis.utiles.TiposAlert;

import static neto.com.mx.surtepedidocedis.utiles.Constantes.METHOD_NAME_GUARDARARTSCONTADOSVERIFICADOR;
import static neto.com.mx.surtepedidocedis.utiles.Constantes.METHOD_NAME_OBTIENECATALOGOSARTICULOS;
import static neto.com.mx.surtepedidocedis.utiles.Constantes.NAMESPACE;

public class CargaCodigosBarraActivity extends AppCompatActivity {

    EditText editTextCodigos = null;

    private int ACCION_GUARDA = 0;

    private String folio = "";
    private String nombreEmpleado = "";
    private String numeroEmpleado = "";
    private String nombreTienda = "";
    private String nombreZona = "";
    private int idZona = 0;
    private boolean descargaCatalogoFlag = false;
    private int indicePivote = 0;
    private int totalArticulosPedidoZona = 0;

    private String codigoBarras = "";
    private boolean existeCodigo = false;
    String descripcionCodigoBarras = "";
    int cantidadCapturadaDialogo = 0;
    int cantidadAsignadaDialogo = 0;
    String descripcionNormaEmpaqueDialogo = "";
    String descripcionNormaEmpaqueWSDialogo = "";
    private long articuloIdBusqueda = 0;
    public static int totalCajasAsignadas = 0;
    private int totalCajasPickeadas = 0;
    private boolean esGuardadoPorCodigos = false;

    public static HashMap<Long, ArticuloVO> mapaCatalogo = new HashMap<Long, ArticuloVO>();
    private HashMap<String, Integer> mapaCodigosNoRem = new HashMap<String, Integer>();

    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.5F);
    private String version = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carga_codigos_barra);
        getSupportActionBar().hide();

        String arreglo = this.getIntent().getStringExtra("folio").trim();
        folio = new String(arreglo);
        System.out.println("//////////////////////////////////////////////////////////////////////FOLIO:" + folio+ "/////////////");
        nombreEmpleado = new String(this.getIntent().getStringExtra("nombreEmpleado").trim());
        numeroEmpleado = new String(this.getIntent().getStringExtra("numeroEmpleado").trim());
        nombreTienda = new String(this.getIntent().getStringExtra("nombreTienda").trim());
        nombreZona = new String(this.getIntent().getStringExtra("nombreZona").trim());
        idZona = this.getIntent().getIntExtra("idZona", 0);
        descargaCatalogoFlag = this.getIntent().getBooleanExtra("descargaCatalogo",false);

        if(descargaCatalogoFlag) {
            mapaCatalogo.clear();
            mapaCodigosNoRem.clear();
            descargaCatalogoArticulos();
        }


        editTextCodigos = (EditText) findViewById(R.id.codigoBarraText);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            editTextCodigos.setCursorVisible(false);
        }else{
            editTextCodigos.setInputType(InputType.TYPE_NULL);
        }
        editTextCodigos.requestFocus();
        editTextCodigos.setOnEditorActionListener(codigosListener);

        TextView zona = (TextView) findViewById(R.id.zonaTienda);
        String tiendaText = "";
        if(nombreTienda.length() > 20) {
            tiendaText = nombreTienda.substring(0,20) + "... ";
        } else {
            tiendaText = nombreTienda;
        }
        zona.setText("Tienda: " + tiendaText + "   \t\tZona: " + nombreZona);

        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch(PackageManager.NameNotFoundException ne) {
            Log.e("CARGA_FOLIO_TAG", "Error al obtener la versión: " + ne.getMessage());
        }
    }

    public void avanzaCarruselPosicion(View view) {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);

        if(mapaCatalogo.size() > 0) {
            indicePivote++;
            indicePivote = indicePivote % totalArticulosPedidoZona;
        }
        dibujaArticuloPorIndice();
    }

    public void retrocedeCarruselPosicion(View view) {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);

        indicePivote--;
        if(indicePivote >= 0) {
            indicePivote =  indicePivote % totalArticulosPedidoZona;
        } else {
            indicePivote = mapaCatalogo.size() - 1;
        }
        dibujaArticuloPorIndice();
    }

    public void dibujaArticuloPorIndice() {
        ACCION_GUARDA = 0;
        //Busca código de barras en el catálogo
        int i = 0;
        for (Map.Entry<Long, ArticuloVO> entry : mapaCatalogo.entrySet()) {
            System.out.println("////////////////////////////////valor de I"+i);
            i++;
            if(entry.getValue().getPosicion() == indicePivote) {
                articuloIdBusqueda = entry.getKey();
                descripcionCodigoBarras = entry.getValue().getNombreArticulo();
                cantidadCapturadaDialogo = entry.getValue().getTotalCajasPickeadas();
                cantidadAsignadaDialogo = entry.getValue().getTotalCajasAsignadas();
                descripcionNormaEmpaqueWSDialogo = " con " + entry.getValue().getNormaEmpaque() + " piezas";
                existeCodigo = true;
                entry.getValue().setEsCapturado(true);

                System.out.println("entry.getKey()///////" +entry.getKey());
                System.out.println("entry.getValue().getNombreArticulo()/////"+entry.getValue().getNombreArticulo());
                System.out.println("entry.getValue().getTotalCajasPickeadas()/////" + entry.getValue().getTotalCajasPickeadas());
                System.out.println("entry.getValue().getTotalCajasAsignadas()////" + entry.getValue().getTotalCajasAsignadas());
                System.out.println("entry.getValue().getNormaEmpaque()/////////" + entry.getValue().getNormaEmpaque());

                break;
            }
        }
        ejecutaWSHilo();
        estableceNombreArticulo();
    }

    TextView.OnEditorActionListener codigosListener = new TextView.OnEditorActionListener(){
        public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {
            try {
                if (actionId == EditorInfo.IME_NULL
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    existeCodigo = false;
                    //example_confirm();//match this behavior to your 'Send' (or Confirm) button

                    //EditText editText = (EditText) findViewById(R.id.codigoBarraText);
                    //editText.setInputType(InputType.TYPE_NULL);
                    codigoBarras = editTextCodigos.getText().toString().trim();

                    descripcionCodigoBarras = codigoBarras;

                    if(!codigoBarras.equals("")) {
                        //Busca código de barras en el catálogo
                        for (Map.Entry<Long, ArticuloVO> entry : mapaCatalogo.entrySet()) {
                            if (entry.getValue().getCodigos().contains(codigoBarras)) {
                                articuloIdBusqueda = entry.getKey();
                                descripcionCodigoBarras = entry.getValue().getNombreArticulo();
                                entry.getValue().setTotalCajasPickeadas(entry.getValue().getTotalCajasPickeadas() + 1);
                                cantidadCapturadaDialogo = entry.getValue().getTotalCajasPickeadas();
                                cantidadAsignadaDialogo = entry.getValue().getTotalCajasAsignadas();
                                descripcionNormaEmpaqueWSDialogo = " con " + entry.getValue().getNormaEmpaque() + " piezas";
                                existeCodigo = true;
                                entry.getValue().setEsCapturado(true);
                                entry.getValue().setEsArticuloContado(true);

                                indicePivote = entry.getValue().getPosicion();
                            }
                        }

                        if (!existeCodigo) {
                            ViewDialog alert = new ViewDialog(CargaCodigosBarraActivity.this);
                            alert.showDialog(CargaCodigosBarraActivity.this, "Artículo no encontrado - " + codigoBarras, null, TiposAlert.ERROR);
                            editTextCodigos.setText("");
                        } else if (cantidadCapturadaDialogo > cantidadAsignadaDialogo) {
                            ViewDialog alert = new ViewDialog(CargaCodigosBarraActivity.this);
                            alert.showDialog(CargaCodigosBarraActivity.this, "No puedes pickear más cajas de las asignadas", null, TiposAlert.ERROR);

                            for (Map.Entry<Long, ArticuloVO> entry : mapaCatalogo.entrySet()) {
                                if (entry.getValue().getCodigos().contains(codigoBarras)) {
                                    entry.getValue().setTotalCajasPickeadas(entry.getValue().getTotalCajasAsignadas());
                                    entry.getValue().setEsCapturado(false);
                                    ejecutaWSHilo();

                                    indicePivote = entry.getValue().getPosicion();
                                }
                            }
                            estableceNombreArticulo();
                        } else {
                            ingresaCodigoBarras();
                        }
                    }
                }
            } catch(Exception e) {
                ViewDialog alert = new ViewDialog(CargaCodigosBarraActivity.this);
                alert.showDialog(CargaCodigosBarraActivity.this, "Error al leer el código de barras: " + e.getMessage(), null, TiposAlert.ERROR);
                //EditText editText = (EditText) findViewById(R.id.codigoBarraText);
                editTextCodigos.setText("");
            }

            return true;
        }
    };
    public void guardarMenuFront(View view) {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);
        guardaAvance();
    }

    public void finalizarMenuFront(View view) {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);
        cuentaCajasPickeadas();
        if(totalCajasPickeadas < totalCajasAsignadas) {
            ViewDialogoErrorActivity errorDialogo = new ViewDialogoErrorActivity(CargaCodigosBarraActivity.this);
            errorDialogo.showDialog(CargaCodigosBarraActivity.this, String.valueOf(totalCajasAsignadas-totalCajasPickeadas), true);
            errorDialogo.setViewDialogoErrorActivityListener(new ViewDialogoErrorActivity.ViewDialogoErrorActivityListener() {
                @Override
                public void onFinaliza() {
                    finalizaConteo();
                }
                @Override
                public void onConteoDiferencias() {
                    conteoDiferencias();
                }
            });
        } else {
            finalizaConteo();
        }
    }

    public void consultarAvanceMenuFront(View view) {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);
        consultaAvance();
    }

    public void salirMenuFront(View view) {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);
        salirMenu();
    }

    public void salirMenu() {
        Intent intent = new Intent(getApplicationContext(), CargaFolioPedidoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("LOGOUT", true);
        intent.putExtra("nombreEmpleado", nombreEmpleado);
        intent.putExtra("numeroEmpleado", numeroEmpleado);
        startActivity(intent);
    }

    public void finalizaConteo() {
        ACCION_GUARDA = 1;
        esGuardadoPorCodigos = false;
        ejecutaWS();
    }

    public void guardaAvance() {
        ACCION_GUARDA = 0;
        esGuardadoPorCodigos = false;
        ejecutaWS();
    }

    public void conteoDiferencias() {
        Intent intent = new Intent(getApplicationContext(), ConteoDiferenciasActivity.class);
        intent.putExtra("mapaCat", mapaCatalogo);
        intent.putExtra("folio", folio);
        intent.putExtra("nombreEmpleado", nombreEmpleado);
        intent.putExtra("numeroEmpleado", numeroEmpleado);
        intent.putExtra("nombreTienda", nombreTienda);
        intent.putExtra("nombreZona", nombreZona);
        intent.putExtra("idZona", idZona);
        startActivity(intent);
    }

    public void consultaAvance() {
        Intent intent = new Intent(getApplicationContext(), ConsultaAvanceActivity.class);
        intent.putExtra("mapaCat", mapaCatalogo);
        intent.putExtra("mapaCodNoRem", mapaCodigosNoRem);
        intent.putExtra("folio", folio);
        intent.putExtra("nombreEmpleado", nombreEmpleado);
        intent.putExtra("numeroEmpleado", numeroEmpleado);
        intent.putExtra("nombreTienda", nombreTienda);
        intent.putExtra("nombreZona", nombreZona);
        intent.putExtra("idZona", idZona);
        startActivity(intent);
    }

    public void descargaCatalogoArticulos() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // Operaciones http
            try {
                final ProgressDialog mDialog = new ProgressDialog(this);
                mDialog.setMessage("Descargando catálogos de artículos...");
                mDialog.setCancelable(false);
                mDialog.setInverseBackgroundForced(false);
                mDialog.show();


                SoapObject request =new SoapObject(NAMESPACE,METHOD_NAME_OBTIENECATALOGOSARTICULOS);

                String serie=Build.SERIAL;

                request.addProperty("folio", folio);
                request.addProperty("numeroSerie", serie);
                request.addProperty("zonaId", String.valueOf(idZona));
                request.addProperty("usuario", numeroEmpleado);


                System.out.println("/////////////////Antes de entrar al Provider MapaCatalogo: " + mapaCatalogo + "////////////Request:///" + request);

                ProviderGeneraCatalogo.getInstance(this).getGeneraCatalogo(request, new ProviderGeneraCatalogo.interfaceGeneraCatalogo() {
                    @Override
                    public void resolver(ArticuloVO respuestaGeneraCatalogo) {
                        System.out.println("*** 1 *** NO ENTRA AL ERROR :o " + respuestaGeneraCatalogo);

                        System.out.println("///////////////////////////mapaCatalogo:"+mapaCatalogo);

                        if (respuestaGeneraCatalogo != null) {

                            if (mapaCatalogo.size() > 0) {
                                System.out.println("////////////////////////Despues de pasar por el Provider MapaCataloo: " + mapaCatalogo.size() + "////////////////");
                                indicePivote = 0;
                                totalArticulosPedidoZona = mapaCatalogo.size();
                                dibujaArticuloPorIndice();
                            }
                        }  else {
                            mDialog.dismiss();
                            System.out.println("*** 2 ***");
                            //cuando el tiempo del servicio exedio el timeout
                            ViewDialog alert = new ViewDialog(CargaCodigosBarraActivity.this);
                            alert.showDialog(CargaCodigosBarraActivity.this, "Error al cargar el catálogo de artículos", null, TiposAlert.ERROR);
                        }

                    }



                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        return getHeaders();
                    }

                });
                mDialog.dismiss();
                System.out.println("//////////STRREQUEST:///////// " + request + "/////////////////////");

            } catch(Exception me) {
                ViewDialog alert = new ViewDialog(CargaCodigosBarraActivity.this);
                alert.showDialog(CargaCodigosBarraActivity.this, "URL no disponible: " + me.getMessage(), null, TiposAlert.ERROR);
            }
        } else {
            // Mostrar errores
            ViewDialog alert = new ViewDialog(CargaCodigosBarraActivity.this);
            alert.showDialog(CargaCodigosBarraActivity.this, "No hay conexión HTTP", null, TiposAlert.ERROR);
        }





                /*String url = Constantes.URL_STRING + "obtieneCatalogoArticulos";

                StringRequest strRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                mDialog.dismiss();
                                System.out.println("*** 1 *** NO ENTRA AL ERROR :o " + response);
                                generaCatalogoV2(response);

                                if(mapaCatalogo.size() > 0) {
                                    indicePivote = 0;
                                    totalArticulosPedidoZona = mapaCatalogo.size();
                                    dibujaArticuloPorIndice();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                mDialog.dismiss();
                                System.out.println("*** 2 ***");

                                Intent intent = new Intent(CargaCodigosBarraActivity.this, CargaFolioPedidoActivity.class);
                                intent.putExtra("numeroEmpleado", numeroEmpleado);
                                intent.putExtra("nombreEmpleado", nombreEmpleado);
                                intent.putExtra("nombreTienda", nombreTienda);
                                intent.putExtra("nombreZona", nombreZona);
                                ViewDialog alert = new ViewDialog(CargaCodigosBarraActivity.this);
                                alert.showDialog(CargaCodigosBarraActivity.this, "Error al cargar el catálogo de artículos: " + error.toString(), intent, TiposAlert.ERROR);
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();

                        params.put("folio", folio);
                        params.put("numeroSerie", Build.SERIAL);
                        params.put("zonaId", String.valueOf(idZona));
                        params.put("usuario", numeroEmpleado);

                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        return super.getHeaders();
                    }
                };*/

    }

    public void ejecutaWS() {
        // Do something in response to button
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // Operaciones http

            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Guardando códigos de barra...");
            mDialog.setCancelable(false);
            mDialog.setInverseBackgroundForced(false);
            mDialog.show();

            try {

                SoapObject request = new SoapObject(NAMESPACE,METHOD_NAME_GUARDARARTSCONTADOSVERIFICADOR);
                request.addProperty("folio", folio);
                request.addProperty("articulosArray", obtieneCadenaArticulos());
                request.addProperty("cantidadesArray", obtieneCadenaCajas());
                request.addProperty("tipoGuardado", String.valueOf(ACCION_GUARDA));
                request.addProperty("zonaId", String.valueOf(idZona));
                request.addProperty("usuario", numeroEmpleado);
                request.addProperty("numeroSerie", Build.SERIAL);
                request.addProperty("version", version);

                ProviderGuardarArticulos.getInstance(this).getGuardarArticulos(request, new ProviderGuardarArticulos.interfaceGuardarArticulos() {
                    @Override
                    public void resolver(CodigosGuardadosVO respuestaGuardaArticulos) {

                        System.out.println("*** 1 *** response: " + respuestaGuardaArticulos);


                        if (respuestaGuardaArticulos != null) {
                            if (respuestaGuardaArticulos.getCodigo() == 0) {
                                if (ACCION_GUARDA == 1) {
                                    Intent intent = new Intent(CargaCodigosBarraActivity.this, DiferenciasRecibidasActivity.class);
                                    intent.putExtra("CodigosGuardados", respuestaGuardaArticulos);
                                    intent.putExtra("folio", folio);
                                    intent.putExtra("numeroEmpleado", numeroEmpleado);
                                    intent.putExtra("nombreEmpleado", nombreEmpleado);
                                    intent.putExtra("nombreTienda", nombreTienda);
                                    intent.putExtra("nombreZona", nombreZona);
                                    intent.putExtra("idZona", idZona);
                                    startActivity(intent);
                                } else if (ACCION_GUARDA == 0) {
                                    ViewDialog alert = new ViewDialog(CargaCodigosBarraActivity.this);
                                    alert.showDialog(CargaCodigosBarraActivity.this, "Se han guardado correctamente los cambios. Recuerda que debes completar tu pedido", null, TiposAlert.CORRECTO);
                                }
                            } else {
                                ViewDialog alert = new ViewDialog(CargaCodigosBarraActivity.this);
                                alert.showDialog(CargaCodigosBarraActivity.this, "Error en el servicio que guarda los códigos: " + respuestaGuardaArticulos.getMensaje(), null, TiposAlert.ERROR);
                            }
                        } else {
                            mDialog.dismiss();
                            System.out.println("*** 2 ***");
                            //cuando el tiempo del servicio exedio el timeout
                            ViewDialog alert = new ViewDialog(CargaCodigosBarraActivity.this);
                            alert.showDialog(CargaCodigosBarraActivity.this, "Error en el servicio que guarda los códigos: favor de validar la comunicación del dispositivo", null, TiposAlert.ERROR);
                        }
                        mDialog.dismiss();
                    }
                });
            } catch(Exception me) {
                if(!esGuardadoPorCodigos) {
                    mDialog.dismiss();
                    ViewDialog alert = new ViewDialog(CargaCodigosBarraActivity.this);
                    alert.showDialog(CargaCodigosBarraActivity.this, "URL no disponible: favor de validar la conexión a Internet" + me.getMessage(), null, TiposAlert.ERROR);
                }
            }
        } else {
            // Mostrar errores
            if(!esGuardadoPorCodigos) {
                ViewDialog alert = new ViewDialog(CargaCodigosBarraActivity.this);
                alert.showDialog(CargaCodigosBarraActivity.this, "No hay conexión HTTP", null, TiposAlert.ERROR);
            }
        }


                /*String url = Constantes.URL_STRING + "guardarArticulosContados";
                StringRequest strRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                mDialog.dismiss();
                                System.out.println("*** 1 *** response: " + response);

                                CodigosGuardadosVO codigosFaltantes = new CodigosGuardadosVO();
                                generaFaltantes(response, codigosFaltantes);

                                if(codigosFaltantes.getCodigo() == 0) {
                                    if(ACCION_GUARDA == 1) {
                                        Intent intent = new Intent(CargaCodigosBarraActivity.this, DiferenciasRecibidasActivity.class);
                                        intent.putExtra("CodigosGuardados",codigosFaltantes);
                                        intent.putExtra("folio",folio);
                                        intent.putExtra("numeroEmpleado",numeroEmpleado);
                                        intent.putExtra("nombreEmpleado",nombreEmpleado);
                                        intent.putExtra("nombreTienda", nombreTienda);
                                        intent.putExtra("nombreZona", nombreZona);
                                        intent.putExtra("idZona",idZona);
                                        startActivity(intent);
                                    } else if(ACCION_GUARDA == 0) {
                                        ViewDialog alert = new ViewDialog(CargaCodigosBarraActivity.this);
                                        alert.showDialog(CargaCodigosBarraActivity.this, "Se han guardado correctamente los cambios. Recuerda que debes completar tu pedido", null, TiposAlert.CORRECTO);
                                    }
                                } else {
                                    ViewDialog alert = new ViewDialog(CargaCodigosBarraActivity.this);
                                    alert.showDialog(CargaCodigosBarraActivity.this, "Error en el servicio que guarda los códigos: " + codigosFaltantes.getMensaje(), null, TiposAlert.ERROR);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if(!esGuardadoPorCodigos) {
                                    mDialog.dismiss();
                                    ViewDialog alert = new ViewDialog(CargaCodigosBarraActivity.this);
                                    alert.showDialog(CargaCodigosBarraActivity.this, "Error en el servicio que guarda los códigos: favor de validar la comunicación del dispositivo", null, TiposAlert.ERROR);
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
                    ViewDialog alert = new ViewDialog(CargaCodigosBarraActivity.this);
                    alert.showDialog(CargaCodigosBarraActivity.this, "URL no disponible: favor de validar la conexión a Internet" + me.getMessage(), null, TiposAlert.ERROR);
                }
            }
        } else {
            // Mostrar errores
            if(!esGuardadoPorCodigos) {
                ViewDialog alert = new ViewDialog(CargaCodigosBarraActivity.this);
                alert.showDialog(CargaCodigosBarraActivity.this, "No hay conexión HTTP", null, TiposAlert.ERROR);
            }
        }*/
    }

    public void ingresaCodigoBarras() {
        ACCION_GUARDA = 0;
        ejecutaWSHilo();
        if(!existeCodigo) {
            ViewDialog alert = new ViewDialog(CargaCodigosBarraActivity.this);
            alert.showDialog(CargaCodigosBarraActivity.this, "Artículo no encontrado - " + codigoBarras, null, TiposAlert.ERROR);

            //EditText editText = (EditText) findViewById(R.id.codigoBarraText);
            editTextCodigos.setText("");

        } else {
            estableceNombreArticulo();
        }
    }

    public void estableceNombreArticulo() {
        //EditText editText = (EditText) findViewById(R.id.codigoBarraText);
        TextView textView = (TextView) findViewById(R.id.numeroSerieLabel);
        TextView textView30 = (TextView) findViewById(R.id.textView311);
        TextView textView32 = (TextView) findViewById(R.id.textView32);
        Button boton0 = (Button) findViewById(R.id.disminuyeCajasBoton);
        Button boton1 = (Button) findViewById(R.id.cargaCajasBoton);

        textView30.setText(String.valueOf(mapaCatalogo.get(articuloIdBusqueda).getTotalCajasPickeadas()));
        textView32.setText(String.valueOf(mapaCatalogo.get(articuloIdBusqueda).getTotalCajasAsignadas()));
        textView.setText(descripcionCodigoBarras);
        boton1.setText(String.valueOf(cantidadAsignadaDialogo));
        editTextCodigos.setText("");

        if(mapaCatalogo.get(articuloIdBusqueda).isEsArticuloContado()) {
            boton0.setEnabled(true);
            boton1.setEnabled(true);
            boton0.setAlpha(1f);
            boton1.setAlpha(1f);
        } else {
            boton0.setEnabled(false);
            boton1.setEnabled(false);
            boton0.setAlpha(0.4f);
            boton1.setAlpha(0.4f);
        }
    }

    public void estableceTotalCajas(View view) {
        view.startAnimation(buttonClick);
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);

        ACCION_GUARDA = 0;
        ejecutaWSHilo();

        if(articuloIdBusqueda != 0) {

            final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.mensaje_ok);
            mp.start();

            CountDownTimer timer = new CountDownTimer(1800, 1800) {

                @Override
                public void onTick(long millisUntilFinished) {
                    // Nothing to do
                }

                @Override
                public void onFinish() {
                    if (mp.isPlaying()) {
                        mp.stop();
                        mp.release();
                    }
                }
            };
            timer.start();


            TextView textView3 = (TextView) findViewById(R.id.textView311);
            int cantidadCapturada = mapaCatalogo.get(articuloIdBusqueda).getTotalCajasAsignadas();
            String descripcionNormaEmpaque = "";
            mapaCatalogo.get(articuloIdBusqueda).setTotalCajasPickeadas(cantidadCapturada);

            if(cantidadCapturada == 1) {
                descripcionNormaEmpaque = "1 de " + mapaCatalogo.get(articuloIdBusqueda).getTotalCajasAsignadas() + " cajas";
            } else if(cantidadCapturada == 0) {
                descripcionNormaEmpaque = "por primera vez";
            } else {
                descripcionNormaEmpaque = mapaCatalogo.get(articuloIdBusqueda).getTotalCajasPickeadas() + " de " +
                        mapaCatalogo.get(articuloIdBusqueda).getTotalCajasAsignadas() + " cajas";
            }
            textView3.setText(String.valueOf(cantidadCapturada));
        }
    }

    public void restaCaja(View view) {
        view.startAnimation(buttonClick);
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);

        ACCION_GUARDA = 0;
        ejecutaWSHilo();

        if(articuloIdBusqueda != 0) {
            TextView textView3 = (TextView) findViewById(R.id.textView311);
            int cantidadCapturada = mapaCatalogo.get(articuloIdBusqueda).getTotalCajasPickeadas() - 1;
            if(cantidadCapturada < 0) {
                cantidadCapturada = 0;
            }
            mapaCatalogo.get(articuloIdBusqueda).setTotalCajasPickeadas(cantidadCapturada);
            String descripcionNormaEmpaque = "";

            if(cantidadCapturada == 1) {
                descripcionNormaEmpaque = "1 de " + mapaCatalogo.get(articuloIdBusqueda).getTotalCajasAsignadas() + " cajas";
            } else if(cantidadCapturada == 0) {
                descripcionNormaEmpaque = "0 cajas";
            } else {
                descripcionNormaEmpaque = mapaCatalogo.get(articuloIdBusqueda).getTotalCajasPickeadas() + " de " +
                        mapaCatalogo.get(articuloIdBusqueda).getTotalCajasAsignadas() + " cajas";
            }
            textView3.setText(String.valueOf(cantidadCapturada));
        }
    }

    public String obtieneCadenaArticulos() {
        StringBuffer cadena = new StringBuffer();
        System.out.println("//////////////////NEW CADENA "+cadena);
        for (Map.Entry<Long, ArticuloVO> entry : mapaCatalogo.entrySet()) {
            System.out.println("//////////////////Entra a for "+cadena+"////////////");
            System.out.println("AccionGuarda"+ACCION_GUARDA+"//////////");
            if(ACCION_GUARDA == 0 && entry.getValue().isEsCapturado()) {
                System.out.println("//////////////////Entra a if "+"///////");
                cadena.append(entry.getKey());
                System.out.println("//////////////////CADENA IF "+cadena);
                cadena.append("|");
            } else if(ACCION_GUARDA == 1) {
                cadena.append(entry.getKey());
                System.out.println("//////////////////CADENA ELSE"+cadena);
                cadena.append("|");
            }
        }
        cadena.replace(cadena.lastIndexOf("|"), cadena.lastIndexOf("|") + 1, "" );

        System.out.println("//////////////////CADENA FINAL"+cadena);

        return cadena.toString();
    }

    public String obtieneCadenaCajas() {
        StringBuffer cadena = new StringBuffer();
        for (Map.Entry<Long, ArticuloVO> entry : mapaCatalogo.entrySet()) {
            if(ACCION_GUARDA == 0 && entry.getValue().isEsCapturado()) {
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

    public void generaCatalogoV2(String response) {
        totalCajasAsignadas = 0;
        totalCajasPickeadas = 0;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput( new StringReader(response) );

            int eventType = xpp.getEventType();
            ArticuloVO articuloVO = null;

            int contadorPosicion = 0;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if(xpp.getName().equals("articulos")) {
                        articuloVO = new ArticuloVO();
                    } else if (xpp.getName().equals("articuloId")) {
                        eventType = xpp.next(); // advance to inner text
                        articuloVO.setArticuloId(Long.parseLong(xpp.getText()));
                    }  else if (xpp.getName().equals("codigosBarraArr")) {
                        eventType = xpp.next(); // advance to inner text
                        articuloVO.getCodigos().add(xpp.getText());
                    } else if (xpp.getName().equals("nombre")) {
                        eventType = xpp.next(); // advance to inner text
                        articuloVO.setNombreArticulo(xpp.getText());
                    } else if (xpp.getName().equals("normaEmpaque")) {
                        eventType = xpp.next(); // advance to inner text
                        articuloVO.setNormaEmpaque(Integer.parseInt(xpp.getText()));
                    } else if (xpp.getName().equals("unidadMedida")) {
                        eventType = xpp.next(); // advance to inner text
                        articuloVO.setUnidadMedida(xpp.getText());
                    } else if (xpp.getName().equals("unidadMedidaId")) {
                        eventType = xpp.next(); // advance to inner text
                        articuloVO.setUnidadMedidaId(Integer.parseInt(xpp.getText()));
                    } else if(xpp.getName().equals("cantidadPickeada")) {
                        eventType = xpp.next(); // advance to inner text
                        articuloVO.setTotalCajasPickeadas(Integer.parseInt(xpp.getText()));
                    } else if(xpp.getName().equals("cantidadAsignada")) {
                        eventType = xpp.next(); // advance to inner text
                        articuloVO.setTotalCajasAsignadas(Integer.parseInt(xpp.getText()));
                        totalCajasAsignadas += articuloVO.getTotalCajasAsignadas();
                    }
                } else if(eventType == XmlPullParser.END_TAG) {
                    if(xpp.getName().equals("articulos")) {
                        articuloVO.setPosicion(contadorPosicion);
                        contadorPosicion++;
                        mapaCatalogo.put(articuloVO.getArticuloId(), articuloVO);
                    }
                }
                eventType = xpp.next();
            }
        } catch(Exception e) {
            //Toast.makeText(this, "Error al formar las diferencias del xml: " + e.getMessage(), Toast.LENGTH_LONG).show();
            ViewDialog alert = new ViewDialog(CargaCodigosBarraActivity.this);
            alert.showDialog(CargaCodigosBarraActivity.this, "Error al formar el catálogo del xml: " + e.getMessage(), null, TiposAlert.ERROR);
        }
    }

    /*public void generaFaltantes(String response, CodigosGuardadosVO codigos) {
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
                    if (xpp.getName().equals("totalCajasAsignadas")) {
                        eventType = xpp.next(); // advance to inner text
                        codigos.setTotalCajasAsignadas(Integer.parseInt(xpp.getText()));
                    } else if (xpp.getName().equals("totalCajasPickeadas")) {
                        eventType = xpp.next(); // advance to inner text
                        codigos.setTotalCajasPickeadas(Integer.parseInt(xpp.getText()));
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
            ViewDialog alert = new ViewDialog(CargaCodigosBarraActivity.this);
            alert.showDialog(CargaCodigosBarraActivity.this, "Error al formar las diferencias del xml: " + e.getMessage(), null, TiposAlert.ERROR);
        }
    }*/

    public void cuentaCajasPickeadas() {
        totalCajasPickeadas = 0;
        for (ArticuloVO entry : mapaCatalogo.values()) {
            totalCajasPickeadas += entry.getTotalCajasPickeadas();
        }
    }

    public void ejecutaWSHilo() {
        // Do something in response to button
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // Operaciones http
            System.out.println("Antes del try Catch");
            try {

                SoapObject request = new SoapObject(NAMESPACE,METHOD_NAME_GUARDARARTSCONTADOSVERIFICADOR);
                //System.out.println("Se crea SoapObject");
                request.addProperty("folio", folio);
                //System.out.println("se agrega folio");
                request.addProperty("articulosArray", obtieneCadenaArticulos());
                //System.out.println("se agrega articulosArray");
                request.addProperty("cantidadesArray", obtieneCadenaCajas());
                //System.out.println("se agrega cantidadesArray");
                request.addProperty("tipoGuardado", String.valueOf(ACCION_GUARDA));
                //System.out.println("se agrega tipoGuardado");
                request.addProperty("zonaId", String.valueOf(idZona));
                //System.out.println("se agrega zonaId");
                request.addProperty("usuario", numeroEmpleado);
                //System.out.println("se agrega usuario");
                request.addProperty("numeroSerie", Build.SERIAL);
                //System.out.println("se agrega numeroSerie");
                request.addProperty("version", version);
                //System.out.println("se agrega version");
                //System.out.println("/////////////////////////////////////REQUEST: "+ request + "////////////////////////////////////////");

                ProviderGuardarArticulos.getInstance(this).getGuardarArticulos(request, new ProviderGuardarArticulos.interfaceGuardarArticulos() {
                    @Override
                    public void resolver(CodigosGuardadosVO respuestaGuardaArticulos) {

                        System.out.println("*** 1 *** response: " + respuestaGuardaArticulos);

                        if (respuestaGuardaArticulos != null) {
                            System.out.println("*** Códigos guardados con éxito *** response: " + respuestaGuardaArticulos);
                            System.out.println("*** Pedido = " + folio);
                        }else {
                            System.out.println("*** 2 ***");
                            //cuando el tiempo del servicio exedio el timeout
                            ViewDialog alert = new ViewDialog(CargaCodigosBarraActivity.this);
                            alert.showDialog(CargaCodigosBarraActivity.this, "Error en el servicio que guarda los códigos: favor de validar la comunicación del dispositivo", null, TiposAlert.ERROR);
                        }
                    }
                });
                /*String url = Constantes.URL_STRING + "guardarArticulosContados";
                int contadorOcurrencias = 0;

                StringRequest strRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                System.out.println("*** Códigos guardados con éxito *** response: " + response);
                                System.out.println("*** Pedido = " + folio);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                System.out.println("*** Error al guardar los códigos *** response: " + error.getMessage());
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
                System.out.println("*** No existe comunicación ***");
            }
        } else {
            System.out.println("*** No existe comunicación ***");
        }
    }

    @Override
    public void onBackPressed() {
    }
}
