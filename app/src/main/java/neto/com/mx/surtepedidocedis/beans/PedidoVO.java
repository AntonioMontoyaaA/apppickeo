package neto.com.mx.surtepedidocedis.beans;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yruizm on 28/09/17.
 */

public class PedidoVO implements Serializable {

    private long folioPedido;
    private boolean pedidoValido;
    private List<ZonaPickeoVO> listaZonas;
    private int codigo;
    private String mensaje;
    private String nombreTienda;


    public long getFolioPedido() {
        return folioPedido;
    }

    public void setFolioPedido(long folioPedido) {
        this.folioPedido = folioPedido;
    }

    public boolean isPedidoValido() {
        return pedidoValido;
    }

    public void setPedidoValido(boolean pedidoValido) {
        this.pedidoValido = pedidoValido;
    }

    public List<ZonaPickeoVO> getListaZonas() {
        return listaZonas;
    }

    public void setListaZonas(List<ZonaPickeoVO> listaZonas) {
        this.listaZonas = listaZonas;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getNombreTienda() {
        return nombreTienda;
    }

    public void setNombreTienda(String nombreTienda) {
        this.nombreTienda = nombreTienda;
    }
}
