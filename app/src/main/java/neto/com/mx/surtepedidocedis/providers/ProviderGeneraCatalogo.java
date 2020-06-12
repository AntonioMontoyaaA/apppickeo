package neto.com.mx.surtepedidocedis.providers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.AuthFailureError;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.Map;

import neto.com.mx.surtepedidocedis.beans.ArticuloVO;
import neto.com.mx.surtepedidocedis.utiles.Constantes;
import neto.com.mx.surtepedidocedis.utiles.Util;

import static neto.com.mx.surtepedidocedis.utiles.Constantes.METHOD_NAME_OBTIENECATALOGOSARTICULOS;
import static neto.com.mx.surtepedidocedis.utiles.Constantes.NAMESPACE;

public class ProviderGeneraCatalogo {
    final String TAG = "ProviderGeneraCatalogo";
    private static ProviderGeneraCatalogo instance;
    private Context context;
    Util u = new Util();
    public static ProviderGeneraCatalogo getInstance(Context context) {
        if (instance == null) {
            instance = new ProviderGeneraCatalogo();
        }
        instance.context = context;
        return instance;
    }

    public void getGeneraCatalogo(final SoapObject request, final interfaceGeneraCatalogo promise ) {


        (new AsyncTask<Void, Void, ArticuloVO>() {
            ArticuloVO respuestaGeneraCatalogo = null;

            @Override
            protected ArticuloVO doInBackground(Void... voids) {
                try {
                    SoapSerializationEnvelope sse = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    sse.dotNet = true;
                    sse.setOutputSoapObject(request);

                    HttpTransportSE transportSE = new HttpTransportSE(Constantes.URL_STRING,60000);
                    transportSE.call(NAMESPACE + METHOD_NAME_OBTIENECATALOGOSARTICULOS, sse);


                    SoapObject response = (SoapObject) sse.getResponse();
                    System.out.println("//////////////////////////////////Response:"+response);
                    Log.d(TAG, response.toString());
                    respuestaGeneraCatalogo = u.parseRespuestaGeneraCatalogo(response, context);
                    return respuestaGeneraCatalogo;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "error IOExepction");
                }
                return respuestaGeneraCatalogo;
            }


            @Override
            protected void onPostExecute(ArticuloVO respuestaGeneraCatalogo) {
                promise.resolver(respuestaGeneraCatalogo);
            }
        }).execute();
    }

    public interface interfaceGeneraCatalogo {
        void resolver(ArticuloVO respuestaGeneraCatalogo);

        Map<String, String> getHeaders() throws AuthFailureError;
    }


}
