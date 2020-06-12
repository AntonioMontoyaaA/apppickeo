package neto.com.mx.surtepedidocedis.providers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import neto.com.mx.surtepedidocedis.beans.ValidaPedidoVO;
import neto.com.mx.surtepedidocedis.utiles.Constantes;
import neto.com.mx.surtepedidocedis.utiles.Util;

import static neto.com.mx.surtepedidocedis.utiles.Constantes.METHOD_NAME_VALIDAPEDIDOVERIFICADOR;
import static neto.com.mx.surtepedidocedis.utiles.Constantes.NAMESPACE;

public class ProviderValidaPedido {
    final String TAG = "ProviderValidaPedido";
    private static ProviderValidaPedido instance;
    private Context context;
    Util u = new Util();
    public static ProviderValidaPedido getInstance(Context context) {
        if (instance == null) {
            instance = new ProviderValidaPedido();
        }
        instance.context = context;
        return instance;
    }

    public void getValidaPedido(final SoapObject request, final interfaceValidaPedido promise) {


        (new AsyncTask<Void, Void, ValidaPedidoVO>() {
            ValidaPedidoVO respuestaValidaPedido = null;

            @Override
            protected ValidaPedidoVO doInBackground(Void... voids) {
                try {
                    SoapSerializationEnvelope sse = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                    sse.dotNet = true;
                    sse.setOutputSoapObject(request);

                    HttpTransportSE transportSE = new HttpTransportSE(Constantes.URL_STRING,60000);
                    transportSE.call(NAMESPACE + METHOD_NAME_VALIDAPEDIDOVERIFICADOR, sse);


                    SoapObject response = (SoapObject) sse.getResponse();
                    System.out.println("//////////////////////////////////Response:"+response);
                    Log.d(TAG, response.toString());
                    respuestaValidaPedido = u.parseRespuestaValidaPedido(response,context);
                    return respuestaValidaPedido;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "error IOExepction");
                }
                return respuestaValidaPedido;
            }


            @Override
            protected void onPostExecute(ValidaPedidoVO respuestaValidaPedido) {
                promise.resolver(respuestaValidaPedido);
            }
        }).execute();
    }

    public interface interfaceValidaPedido {
        void resolver(ValidaPedidoVO respuestaValidaPedido);
    }


}
