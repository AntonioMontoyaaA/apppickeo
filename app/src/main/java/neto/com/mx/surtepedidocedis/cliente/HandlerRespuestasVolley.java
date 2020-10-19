package neto.com.mx.surtepedidocedis.cliente;

import com.android.volley.VolleyError;

import neto.com.mx.surtepedidocedis.mensajes.RespuestaDinamica;

/**
 * Created by dramirezr on 18/01/2018.
 */

public interface HandlerRespuestasVolley {
    public void manejarExitoVolley(RespuestaDinamica respuesta);
    public void manejarErrorVolley(VolleyError error);
}
