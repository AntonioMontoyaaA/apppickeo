package neto.com.mx.surtepedidocedis.utiles;

/**
 * Created by yruizm on 28/09/17.
 */

public class Constantes {
    public static final String URL_STRING = "http://10.81.12.45:7777/WSSIAN/services/WSPickeoMovil/";//DESA
    //public static final String URL_STRING = "http://10.81.12.46:7777/WSSIANPAR/services/WSPickeoMovil/";//QA
    //public static final String URL_STRING = "http://10.81.12.203:8003/WSSIONIndicadores/services/WSPickeoMovil/";//PROD

    public static final int CONTADOR_GUARDA_AVANCE = 10;

    public static final String LLAVE_PRIVADA = "RecibePedidosV1";

    public static final String ID_APP_PICKEO   =   "1";


    public static String NAMESPACE = "http://servicio.pickeo.movil.abasto.neto";
    public static String METHOD_NAME_VALIDAUSUARIO = "validaUsuario";
    public static String METHOD_NAME_GUARDARARTSCONTADOSVERIFICADOR = "guardarArticulosContados";
    public static String METHOD_NAME_VALIDAPEDIDOVERIFICADOR = "validaPedido";
    public static String METHOD_NAME_OBTIENECATALOGOSARTICULOS = "obtieneCatalogoArticulos";
}
