package neto.com.mx.surtepedidocedis.utiles;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import neto.com.mx.surtepedidocedis.CargaCodigosBarraActivity;
import neto.com.mx.surtepedidocedis.beans.ArticuloVO;
import neto.com.mx.surtepedidocedis.beans.CodigoBarraVO;
import neto.com.mx.surtepedidocedis.beans.CodigosGuardadosVO;
import neto.com.mx.surtepedidocedis.beans.UsuarioVO;
import neto.com.mx.surtepedidocedis.beans.ValidaPedidoVO;
import neto.com.mx.surtepedidocedis.beans.ZonaVerificadoVO;
import neto.com.mx.surtepedidocedis.dialogos.ViewDialog;

/**
 * Created by yruizm on 21/10/16.
 */

public class Util {
    private final String TAG = "Util";
    public static String getProperty(String key, Context context) throws IOException {
        Properties properties = new Properties();;
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open("config.properties");
        properties.load(inputStream);
        return properties.getProperty(key);

    }

    /////////////////////////////////////////////ValidaUsuario//////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public UsuarioVO parseRespuestaValidaUsuario(SoapObject servicio, Context context) {
        final String str = servicio.toString();
        Log.d(TAG, "parseRespuestaValidaUsuario: " + str);
        UsuarioVO item = new UsuarioVO();

        int count = servicio.getPropertyCount();

            if (count > 0) {
                try {
                    SoapPrimitive codigo = (SoapPrimitive) servicio.getProperty("codigo");
                    SoapPrimitive mensaje = (SoapPrimitive) servicio.getProperty("mensaje");
                    SoapPrimitive nombreUsuario = (SoapPrimitive) servicio.getProperty("nombreUsuario");
                    SoapPrimitive esUsuarioValido = (SoapPrimitive) servicio.getProperty("esUsuarioValido");
                    try {
                        item.setCodigo(codigo.getValue() != null ? Integer.valueOf((String) codigo.getValue()) : 1);
                        System.out.println("codigo ");
                    } catch (Exception e) {
                        item.setCodigo(1);
                    }
                    try {
                        item.setMensaje(mensaje.getValue() != null ? (String) mensaje.getValue() : " ");
                        System.out.println("mensaje ");
                    } catch (Exception e) {
                        item.setMensaje(" ");
                    }
                    try {
                        item.setNombreEmpleado(nombreUsuario.getValue() != null ? (String) nombreUsuario.getValue() : " ");
                        System.out.println("nombreUsuario ");
                    } catch (Exception e) {
                        item.setNombreEmpleado(" ");
                    }
                    try {
                        item.setEmpleadoValido(esUsuarioValido.getValue() != null ? (String) esUsuarioValido.getValue().toString() : " ");
                        System.out.println("esUsuarioValido : " + esUsuarioValido.getValue().toString());
                    } catch (Exception e) {
                        item.setEmpleadoValido(" ");
                    }

                } catch (Exception e) {
                    ViewDialog alert = new ViewDialog(context);
                    alert.showDialog((Activity) context, e.getMessage(), null, TiposAlert.ALERT);
                }
            }
        return item;
    }


    /////////////////////////////////////////////ValidadaPedidos//////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public ValidaPedidoVO parseRespuestaValidaPedido(SoapObject servicio, Context context) {
        final String str = servicio.toString();
        Log.d(TAG, "parseRespuestaValidaPedido: " + str);
        ValidaPedidoVO item = new ValidaPedidoVO();
        ArrayList<ZonaVerificadoVO> listaZonaVerificado;
        int count = servicio.getPropertyCount();

        if (str.contains("Error") || servicio.equals(null)) {
            SoapPrimitive coidgo = (SoapPrimitive) servicio.getProperty("errorCode");
            SoapPrimitive mensaje = (SoapPrimitive) servicio.getProperty("errorDesc");
            item.setCodigo(Integer.valueOf((String) coidgo.getValue()));
            item.setMensaje((String) mensaje.getValue());

        } else {
            if (count > 0) {
                listaZonaVerificado = new ArrayList<>();
                try {
                    for (int i = 0; i < count; i++) {
                        ZonaVerificadoVO items = new ZonaVerificadoVO();
                        try {
                            if (!servicio.getPropertyAsString(i).contains("listaZonas")) {
                                SoapObject pojoSoap = null;
                                try {
                                    pojoSoap = (SoapObject) servicio.getProperty(i);

                                } catch (Exception e) {
                                    Log.d(TAG, "parseRespuestaValidaPedido: pojo es nulo");

                                }
                                if (pojoSoap != null) {
                                    SoapPrimitive zonaId = (SoapPrimitive) pojoSoap.getProperty("zonaId");
                                    SoapPrimitive descripcionZona = (SoapPrimitive) pojoSoap.getProperty("descripcionZona");
                                    SoapPrimitive nombreUsuario = (SoapPrimitive) pojoSoap.getProperty("usuarioConteo");
                                    SoapPrimitive zonaValida = (SoapPrimitive) pojoSoap.getProperty("esZonaValida");
                                    SoapPrimitive estatusZona = (SoapPrimitive) pojoSoap.getProperty("estatusConteoTransferenciaId");
                                    SoapPrimitive nombreCorto = (SoapPrimitive) pojoSoap.getProperty("nombreCorto");

                                    items.setIdZona(Integer.valueOf((String) zonaId.getValue()));
                                    items.setDescripcionZona((String) descripcionZona.getValue());
                                    items.setZonaValida(Integer.valueOf((String) zonaValida.getValue()));
                                    items.setNombreUsuario((String) nombreUsuario.getValue());
                                    items.setEstatusZona(Integer.valueOf((String) estatusZona.getValue()));
                                    items.setNombreCorto((String) nombreCorto.getValue());
                                    listaZonaVerificado.add(items);
                                    System.out.println("///////////////////////"+listaZonaVerificado+"/////////////////////////////");
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    item.setListaZonasVerificado(Arrays.asList(listaZonaVerificado.toArray(new ZonaVerificadoVO[listaZonaVerificado.size()])));
                    SoapPrimitive codigo = (SoapPrimitive) servicio.getProperty("errorCode");
                    SoapPrimitive mensaje = (SoapPrimitive) servicio.getProperty("errorDesc");
                    SoapPrimitive cedis = (SoapPrimitive) servicio.getProperty("cedis");
                    SoapPrimitive cedisId = (SoapPrimitive) servicio.getProperty("cedisId");
                    SoapPrimitive llave = (SoapPrimitive) servicio.getProperty("llave");
                    SoapPrimitive pedidoValido = (SoapPrimitive) servicio.getProperty("esPedidoValido");
                    SoapPrimitive requiereLlave = (SoapPrimitive) servicio.getProperty("requiereLlave");
                    SoapPrimitive tienda = (SoapPrimitive) servicio.getProperty("nombreTienda");
                    SoapPrimitive tiendaId = (SoapPrimitive) servicio.getProperty("tiendaId");
                    try {
                        item.setCodigo(codigo.getValue() != null ? Integer.valueOf((String) codigo.getValue()) : 1);
                        System.out.println("codigo ");
                    } catch (Exception e) {
                        item.setCodigo(1);
                    }
                    try {
                        item.setMensaje(mensaje.getValue() != null ? (String) mensaje.getValue() : " ");
                        System.out.println("mensaje ");
                    } catch (Exception e) {
                        item.setMensaje(" ");
                    }
                    try {
                        item.setNombreCedis(cedis.getValue() != null ? (String) cedis.getValue() : " ");
                        System.out.println("cedis ");
                    } catch (Exception e) {
                        item.setNombreCedis(" ");
                    }
                    try {
                        item.setCedisId(cedisId.getValue() != null ? Integer.valueOf((String) cedisId.getValue()) : 0);
                        System.out.println("cedisID ");
                    } catch (Exception e) {
                        item.setCedisId(0);
                    }
                    try {
                        item.setLlave(llave.getValue() != null ? (String) llave.getValue() : " ");
                        System.out.println("llave ");
                    } catch (Exception e) {
                        item.setLlave(" ");
                    }
                    try {
                        item.setPedidoValido(pedidoValido.getValue() != null ? (String) pedidoValido.getValue().toString() : " ");
                        System.out.println("pedidoValido : " + pedidoValido.getValue().toString());
                    } catch (Exception e) {
                        item.setPedidoValido(" ");
                    }
                    try {
                        item.setRequiereLlave(requiereLlave.getValue() != null ? (Boolean) requiereLlave.getValue() : false);
                        System.out.println("requiereLlave ");
                    } catch (Exception e) {
                        item.setRequiereLlave(false);
                    }
                    try {
                        item.setNombreTienda(tienda.getValue() != null ? (String) tienda.getValue() : " ");
                        System.out.println("tienda ");
                    } catch (Exception e) {
                        item.setNombreTienda(" ");
                    }
                    try {
                        item.setTiendaId(tiendaId.getValue() != null ? Integer.valueOf((String) tiendaId.getValue()) : 0);
                        System.out.println("tiendaId ");
                    } catch (Exception e) {
                        item.setTiendaId(0);
                    }

                } catch (Exception e) {
                    ViewDialog alert = new ViewDialog(context);
                    alert.showDialog((Activity) context, e.getMessage(), null, TiposAlert.ALERT);
                }
            }
        }
        return item;
    }



    /////////////////////////////////////////////GuardaArticulos//////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public CodigosGuardadosVO parseRespuestaGuardaArticulos(SoapObject servicio, Context context) {
        final String str = servicio.toString();
        System.out.println("SERVICIO///////////////////////////"+ str);
        Log.d(TAG, "parseRespuestaGuardaArticulos: " + str);
        CodigosGuardadosVO item = new CodigosGuardadosVO();
        ArrayList<CodigoBarraVO> listaCodigosFaltantes;
        int count = servicio.getPropertyCount();

        if (str.contains("Error") || servicio.equals(null)) {
            SoapPrimitive coidgo = (SoapPrimitive) servicio.getProperty("errorCode");
            SoapPrimitive mensaje = (SoapPrimitive) servicio.getProperty("errorDesc");
            item.setCodigo(Integer.valueOf((String) coidgo.getValue()));
            item.setMensaje((String) mensaje.getValue());

        } else {
            if (count > 0) {
                listaCodigosFaltantes = new ArrayList<>();
                try {
                    for (int i = 0; i < count; i++) {
                        CodigoBarraVO items = new CodigoBarraVO();
                        try {
                            if (!servicio.getPropertyAsString(i).contains("articulosConDiferencia")) {
                                SoapObject pojoSoap = null;
                                try {
                                    pojoSoap = (SoapObject) servicio.getProperty(i);

                                } catch (Exception e) {
                                    Log.d(TAG, "parseRespuestaGuardaArticulos: pojo es nulo");
                                }
                                if (pojoSoap != null) {
                                    SoapPrimitive articuloId = (SoapPrimitive) pojoSoap.getProperty("articuloId");
                                    SoapPrimitive codigobarras = (SoapPrimitive) pojoSoap.getProperty("codigobarras");
                                    SoapPrimitive nombre = (SoapPrimitive) pojoSoap.getProperty("nombre");
                                    SoapPrimitive cantidadAsignada = (SoapPrimitive) pojoSoap.getProperty("cantidadAsignada");
                                    SoapPrimitive cantidadPickeada = (SoapPrimitive) pojoSoap.getProperty("cantidadPickeada");

                                    items.setArticuloId(Long.valueOf((String) articuloId.getValue()));
                                    items.setCodigoBarras((String) codigobarras.getValue());
                                    items.setNombreArticulo((String) nombre.getValue());
                                    items.setCajasPedido(Integer.valueOf((String) cantidadAsignada.getValue()));
                                    items.setCajasCapturadas(Integer.valueOf((String) cantidadPickeada.getValue()));
                                    listaCodigosFaltantes.add(items);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    item.setArticulosDiferencias(listaCodigosFaltantes.toArray(new CodigoBarraVO[listaCodigosFaltantes.size()]));
                    SoapPrimitive codigo = (SoapPrimitive) servicio.getProperty("errorCode");
                    SoapPrimitive mensaje = (SoapPrimitive) servicio.getProperty("errorDesc");
                    SoapPrimitive articulosAsignados = (SoapPrimitive) servicio.getProperty("articulosAsignados");
                    SoapPrimitive articulosContados = (SoapPrimitive) servicio.getProperty("articulosContados");
                    //SoapPrimitive estatusPedido = (SoapPrimitive) servicio.getProperty("estatusPedido");
                    SoapPrimitive totalCajasAsignadas = (SoapPrimitive) servicio.getProperty("totalCajasAsignadas");
                    SoapPrimitive totalCajasPickeadas = (SoapPrimitive) servicio.getProperty("totalCajasPickeadas");
                    try {
                        item.setCodigo(codigo.getValue() != null ? Integer.valueOf((String) codigo.getValue()) : 1);
                    } catch (Exception e) {
                        item.setCodigo(1);
                    }
                    try {
                        item.setMensaje(mensaje.getValue() != null ? (String) mensaje.getValue() : " ");
                    } catch (Exception e) {
                        item.setMensaje(" ");
                    }
                    try {
                        item.setTotalArticulosEnPedido(articulosAsignados.getValue() != null ? Integer.valueOf((String) articulosAsignados.getValue()) : 0);
                    } catch (Exception e) {
                        item.setTotalArticulosEnPedido(0);
                    }
                    try {
                        item.setTotalArticulosCapturados(articulosContados.getValue() != null ? Integer.valueOf((String) articulosContados.getValue()) : 0);
                    } catch (Exception e) {
                        item.setTotalArticulosCapturados(0);
                    }
                    /*try {
                        item.setTotalArticulosCapturados(articulosVerificados.getValue() != null ? Integer.valueOf((String) articulosVerificados.getValue()) : 0);
                    } catch (Exception e) {
                        item.setTotalArticulosCapturados(0);
                    }
                    try {
                        item.setEstatusPedido(estatusPedido.getValue().equals(null) ? " " : (String) estatusPedido.getValue());
                    } catch (Exception e) {
                        item.setEstatusPedido(" ");
                    }*/
                    try {
                        item.setTotalCajasAsignadas(totalCajasAsignadas.getValue() != null ? Integer.valueOf((String) totalCajasAsignadas.getValue()) : 0);
                    } catch (Exception e) {
                        item.setTotalCajasAsignadas(0);
                    }
                    try {
                        item.setTotalCajasPickeadas(totalCajasPickeadas.getValue() != null ? Integer.valueOf((String) totalCajasPickeadas.getValue()) : 0);
                    } catch (Exception e) {
                        item.setTotalCajasPickeadas(0);
                    }
                    /*try {
                        item.setTotalCajasPickeadas(totalCajasPickeadas.getValue() != null ? Integer.valueOf((String) totalCajasPickeadas.getValue()) : 0);
                    } catch (Exception e) {
                        item.setTotalCajasPickeadas(0);
                    }*/
                } catch (Exception e) {
                    ViewDialog alert = new ViewDialog(context);
                    alert.showDialog((Activity) context, e.getMessage(), null, TiposAlert.ALERT);
                }

            }
        }
        return item;
    }




    /////////////////////////////////////////////GeneraCatalogoV2//////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public ArticuloVO parseRespuestaGeneraCatalogo(SoapObject servicio, Context context) {
        final String str = servicio.toString();
        Log.d(TAG, "parseRespuestaGeneraCatalogo: " + str);
        ArticuloVO item = new ArticuloVO();
        ArrayList<ArticuloVO> articulos;
        int count = servicio.getPropertyCount();

        int contadorPosicion = 0;

        if (str.contains("Error") || servicio.equals(null)) {
            SoapPrimitive coidgo = (SoapPrimitive) servicio.getProperty("errorCode");
            SoapPrimitive mensaje = (SoapPrimitive) servicio.getProperty("errorDesc");
            item.setCodigo(Integer.valueOf((String) coidgo.getValue()));
            item.setMensaje((String) mensaje.getValue());

        } else {
            if (count > 0) {
                articulos = new ArrayList<>();
                try {
                    for (int i = 0; i < count; i++) {
                        ArticuloVO items = new ArticuloVO();
                        try {
                            if (!servicio.getPropertyAsString(i).contains("articulos")) {
                                SoapObject pojoSoap = null;
                                try {
                                    pojoSoap = (SoapObject) servicio.getProperty(i);

                                } catch (Exception e) {
                                    Log.d(TAG, "parseRespuestaValidaPedido: pojo es nulo");

                                }
                                if (pojoSoap!= null){


                                    SoapPrimitive articuloId = (SoapPrimitive) pojoSoap.getProperty("articuloId");
                                    SoapPrimitive cantidadAsignada = (SoapPrimitive) pojoSoap.getProperty("cantidadAsignada");
                                    SoapPrimitive cantidadPickeada = (SoapPrimitive) pojoSoap.getProperty("cantidadPickeada");
                                    //SoapPrimitive codigosBarraArr = (SoapPrimitive) pojoSoap.getProperty("codigosBarraArr");
                                    SoapPrimitive nombre = (SoapPrimitive) pojoSoap.getProperty("nombre");
                                    SoapPrimitive normaEmpaque = (SoapPrimitive) pojoSoap.getProperty("normaEmpaque");
                                    SoapPrimitive unidadMedida = (SoapPrimitive) pojoSoap.getProperty("unidadMedida");
                                    SoapPrimitive unidadMedidaId = (SoapPrimitive) pojoSoap.getProperty("unidadMedidaId");
                                    for (int j = 0;j<pojoSoap.getPropertyCount();j++){
                                        //System.out.println("/////////pojoSoap.getProperty()"+pojoSoap.getProperty(j));
                                        //System.out.println("/////////pojoSoap.getPropertyInfo()"+pojoSoap.getPropertyInfo(j));
                                        String Property = String.valueOf(pojoSoap.getPropertyInfo(j));
                                        if (Property.contains("codigosBarraArr")){
                                            SoapPrimitive codigosBarraArr = (SoapPrimitive) pojoSoap.getProperty(j);
                                            items.getCodigos().add((String) codigosBarraArr.getValue());
                                        }
                                        //SoapPrimitive codigosBarraArr = (SoapPrimitive) pojoSoap.getProperty("codigosBarraArr");
                                        //items.getCodigos().add((String) codigosBarraArr.getValue());
                                        //System.out.println("/////////////.codigos"+items.getCodigos()+"////////.size"+items.getCodigos().size()+"/////");
                                    }
                                    items.setArticuloId(Long.parseLong((String)articuloId.getValue()));
                                    items.setTotalCajasAsignadas(Integer.valueOf((String) cantidadAsignada.getValue()));
                                    items.setTotalCajasPickeadas(Integer.valueOf((String) cantidadPickeada.getValue()));
                                    //items.getCodigos().add((String) codigosBarraArr.getValue());
                                    items.setNombreArticulo((String) nombre.getValue());
                                    items.setNormaEmpaque(Integer.valueOf((String)normaEmpaque.getValue()));
                                    items.setUnidadMedida((String) unidadMedida.getValue());
                                    items.setUnidadMedidaId(Integer.valueOf((String) unidadMedidaId.getValue()));
                                    articulos.add(items);
                                    CargaCodigosBarraActivity.totalCajasAsignadas += items.getTotalCajasAsignadas();
                                    //System.out.println("////////////////////////articulos"+articulos+"/////////");
                                    System.out.println("/////////////.codigosFINAL"+items.getCodigos()+"////////.size"+items.getCodigos().size()+"/////");

                                    System.out.println("///////////////ARTICULOid" + items.getArticuloId() + "//////////////////////////////");


                                    items.setPosicion(contadorPosicion);
                                    contadorPosicion++;
                                    System.out.println("///////////////" + CargaCodigosBarraActivity.mapaCatalogo + "//////////////////////////////");
                                    CargaCodigosBarraActivity.mapaCatalogo.put(items.getArticuloId(), items);
                                    System.out.println("///////////////" + items.getArticuloId() + "//////////////////////////////");



                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    item.setListaArticulo(Arrays.asList(articulos.toArray(new ArticuloVO[articulos.size()])));
                    SoapPrimitive codigo = (SoapPrimitive) servicio.getProperty("errorCode");
                    SoapPrimitive mensaje = (SoapPrimitive) servicio.getProperty("errorDesc");

                    try {
                        item.setCodigo(codigo.getValue() != null ? Integer.valueOf((String) codigo.getValue()) : 1);
                        System.out.println("codigo ");
                    } catch (Exception e) {
                        item.setCodigo(1);
                    }
                    try {
                        item.setMensaje(mensaje.getValue() != null ? (String) mensaje.getValue() : " ");
                        System.out.println("mensaje ");
                    } catch (Exception e) {
                        item.setMensaje(" ");
                    }



                } catch (Exception e) {
                    ViewDialog alert = new ViewDialog(context);
                    alert.showDialog((Activity) context, e.getMessage(), null, TiposAlert.ALERT);
                }
            }
        }
        return item;
    }





    /*public ArticuloVO parseRespuestaGeneraCatalogo(SoapObject servicio, Context context) {
        int totalCajasAsignadas = 0;
        final String str = servicio.toString();
        Log.d(TAG, "parseRespuestaGeneraCatalogo: " + str);
        ArticuloVO articuloVO = new ArticuloVO();
        ArrayList<ArticuloVO> articulos;
        int count = servicio.getPropertyCount();
        int contadorPosicion = 0;

        if (str.contains("error") || servicio.equals(null)) {
            SoapPrimitive coidgo = (SoapPrimitive) servicio.getProperty("errorCode");
            SoapPrimitive mensaje = (SoapPrimitive) servicio.getProperty("errorDesc");
            articuloVO.setCodigo(Integer.valueOf((String) coidgo.getValue()));
            articuloVO.setMensaje((String) mensaje.getValue());
            System.out.println("///////////////////////////ARTICULOVO:"+articuloVO);

        } else {
            if (count > 0) {
                articulos = new ArrayList<>();
                try {
                    for (int i = 0; i < count; i++) {
                        articuloVO = new ArticuloVO();
                        try {
                            if (!servicio.getPropertyAsString(i).contains("articulos")) {
                                SoapObject pojoSoap = null;
                                try {
                                    pojoSoap = (SoapObject) servicio.getProperty(i);

                                } catch (Exception e) {
                                    Log.d(TAG, "parseRespuestaValidaPedido: pojo es nulo");

                                }
                                if (pojoSoap != null) {
                                    SoapPrimitive articuloId = (SoapPrimitive) pojoSoap.getProperty("articuloId");
                                    SoapPrimitive cantidadAsignada = (SoapPrimitive) pojoSoap.getProperty("cantidadAsignada");
                                    SoapPrimitive cantidadPickeada = (SoapPrimitive) pojoSoap.getProperty("cantidadPickeada");
                                    SoapPrimitive codigosBarraArr = (SoapPrimitive) pojoSoap.getProperty("codigosBarraArr");
                                    SoapPrimitive nombre = (SoapPrimitive) pojoSoap.getProperty("nombre");
                                    SoapPrimitive normaEmpaque = (SoapPrimitive) pojoSoap.getProperty("normaEmpaque");
                                    SoapPrimitive unidadMedida = (SoapPrimitive) pojoSoap.getProperty("unidadMedida");
                                    SoapPrimitive unidadMedidaId = (SoapPrimitive) pojoSoap.getProperty("unidadMedidaId");

                                    articuloVO.setArticuloId(Long.parseLong((String) articuloId.getValue()));
                                    articuloVO.setTotalCajasAsignadas(Integer.valueOf((String) cantidadAsignada.getValue()));
                                    articuloVO.setTotalCajasPickeadas(Integer.valueOf((String) cantidadPickeada.getValue()));
                                    articuloVO.getCodigos().add((String) codigosBarraArr.getValue());
                                    articuloVO.setNombreArticulo((String) nombre.getValue());
                                    articuloVO.setNormaEmpaque(Integer.valueOf((String) normaEmpaque.getValue()));
                                    articuloVO.setUnidadMedida((String) unidadMedida.getValue());
                                    articuloVO.setUnidadMedidaId(Integer.valueOf((String) unidadMedidaId.getValue()));
                                    totalCajasAsignadas += articuloVO.getTotalCajasAsignadas();
                                    articulos.add(articuloVO);
                                    System.out.println("/////////////////////////ITEM:"+articuloVO);
                                    System.out.println("//////////////////////arregloARTICULOS/"+articulos+"/////////////////////////////");
                                }
                            }
                            articuloVO.setPosicion(contadorPosicion);
                            System.out.println("///////////////MAPA_CATALOGO1:" + CargaCodigosBarraActivity.mapaCatalogo + "//////////////////////////////");
                            contadorPosicion++;
                            CargaCodigosBarraActivity.mapaCatalogo.put(articuloVO.getArticuloId(), articuloVO);
                            System.out.println("///////////////MAPA_CATALOGO2   :" + CargaCodigosBarraActivity.mapaCatalogo + "//////////////////////////////");


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                } catch (Exception e) {
                    ViewDialog alert = new ViewDialog(context);
                    alert.showDialog((Activity) context, e.getMessage(), null, TiposAlert.ALERT);
                }
            }
        }
        return articuloVO;
    }*/



}
