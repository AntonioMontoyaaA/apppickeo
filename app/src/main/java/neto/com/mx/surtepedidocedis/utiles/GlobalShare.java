package neto.com.mx.surtepedidocedis.utiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import neto.com.mx.surtepedidocedis.beans.ArticuloBean;
import neto.com.mx.surtepedidocedis.beans.UbicacionBean;
import neto.com.mx.surtepedidocedis.beans.ZonaBean;

/**
 * Created by dramirezr on 28/01/2018.
 */

public class GlobalShare {
    private static boolean acceso_verificado = false;
    private static boolean version_verificado = false;
    public static final String ID_TODOS_ZONA = "0";
    public final static String logAplicaion = "LogConteoTIENDA";
    private static volatile GlobalShare instancia = null;

    private static List<UbicacionBean> ubicaciones;
    private static List<ZonaBean> zonas;
    private static Map<String, List<ArticuloBean>> articulosXZona;
    private static List<ArticuloBean> ultimosArticulosContados;

    private GlobalShare(){}

    public void restearVariables(){
        ubicaciones = new ArrayList<UbicacionBean>() ;
        zonas = new ArrayList<ZonaBean>();
        articulosXZona = new HashMap<String, List<ArticuloBean>>();
        ultimosArticulosContados = new ArrayList<ArticuloBean>();
    }

    public synchronized static GlobalShare getInstace(){
        if (instancia == null) {
            synchronized (GlobalShare.class) {
                instancia = new GlobalShare();
                ubicaciones = new ArrayList<UbicacionBean>();
                zonas = new ArrayList<ZonaBean>();
                articulosXZona = new HashMap<String, List<ArticuloBean>>();
                ultimosArticulosContados = new ArrayList<ArticuloBean>();
            }
        }
        return instancia;
    }

    public void setAccesoVerificado(boolean verificado){
        acceso_verificado = verificado;
    }

    public boolean getAccesoVerificado(){
        return acceso_verificado;
    }

    public void setVersionVerificado(boolean verificado){
        version_verificado = verificado;
    }

    public boolean getVersionVerificado(){
        return version_verificado;
    }

    public List<UbicacionBean> getUbicaciones() {
        return ubicaciones;
    }

    public void setUbicaciones(List<UbicacionBean> ubicaciones) {
        GlobalShare.ubicaciones = ubicaciones;
    }

    public List<ZonaBean> getZonas() {
        return zonas;
    }

    public void setZonas(List<ZonaBean> zonas) {
        GlobalShare.zonas = zonas;
    }

    public Map<String, List<ArticuloBean>> getArticulos() {
        if( articulosXZona == null )
            articulosXZona = new HashMap<String, List<ArticuloBean>>();
        return articulosXZona;
    }

    public void setArticulos(Map<String, List<ArticuloBean>> articulosxzona) {
        GlobalShare.articulosXZona = articulosxzona;
    }

    public List<ArticuloBean> getUltimosArticulosContados(){
        return ultimosArticulosContados;
    }
    public void addUltimoArticuloContado(ArticuloBean articulo){
        if( ultimosArticulosContados == null )
            ultimosArticulosContados = new ArrayList<ArticuloBean>();
        ultimosArticulosContados.add(articulo);
    }
    public void removeUltimoArticuloContado(ArticuloBean articulo){
        if( ultimosArticulosContados == null )
            ultimosArticulosContados = new ArrayList<ArticuloBean>();
        ultimosArticulosContados.remove(articulo);
    }
    public void resetUltimosArticulosContados(){
        if( ultimosArticulosContados != null )
            ultimosArticulosContados.clear();
    }


}
