package neto.com.mx.surtepedidocedis.beans;

import java.io.Serializable;

/**
 * Created by yruizm on 20/10/16.
 */

public class CodigoBarraVO implements Serializable {

    private long articuloId;
    private String codigoBarras;
    private String nombreArticulo;
    private int cajasPedido;
    private int cajasCapturadas;

    public long getArticuloId() {
        return articuloId;
    }

    public void setArticuloId(long articuloId) {
        this.articuloId = articuloId;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getNombreArticulo() {
        return nombreArticulo;
    }

    public void setNombreArticulo(String nombreArticulo) {
        this.nombreArticulo = nombreArticulo;
    }

    public int getCajasPedido() {
        return cajasPedido;
    }

    public void setCajasPedido(int cajasPedido) {
        this.cajasPedido = cajasPedido;
    }

    public int getCajasCapturadas() {
        return cajasCapturadas;
    }

    public void setCajasCapturadas(int cajasCapturadas) {
        this.cajasCapturadas = cajasCapturadas;
    }

    @Override
    public String toString() {
        return "CodigoBarraVO{" +
                "articuloId=" + articuloId +
                ", codigoBarras='" + codigoBarras + '\'' +
                ", nombreArticulo='" + nombreArticulo + '\'' +
                ", cajasPedido=" + cajasPedido +
                ", cajasCapturadas=" + cajasCapturadas +
                '}';
    }
}
