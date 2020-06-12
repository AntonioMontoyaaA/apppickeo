package neto.com.mx.surtepedidocedis.beans;

import java.io.Serializable;

/**
 * Created by yruizm on 23/10/16.
 */

public class CodigosGuardadosVO implements Serializable {

    private int totalCajasPickeadas;
    private int totalCajasAsignadas;
    private int totalArticulosCapturados;
    private int totalArticulosEnPedido;
    private CodigoBarraVO[] articulosDiferencias;
    private int codigo;
    private String mensaje;


    public int getTotalCajasAsignadas() {
        return totalCajasAsignadas;
    }

    public void setTotalCajasAsignadas(int totalCajasAsignadas) {
        this.totalCajasAsignadas = totalCajasAsignadas;
    }

    public int getTotalCajasPickeadas() {
        return totalCajasPickeadas;
    }

    public void setTotalCajasPickeadas(int totalCajasPickeadas) {
        this.totalCajasPickeadas = totalCajasPickeadas;
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

    public CodigoBarraVO[] getArticulosDiferencias() {
        return articulosDiferencias;
    }

    public void setArticulosDiferencias(CodigoBarraVO[] articulosDiferencias) {
        this.articulosDiferencias = articulosDiferencias;
    }

    public int getTotalArticulosCapturados() {
        return totalArticulosCapturados;
    }

    public void setTotalArticulosCapturados(int totalArticulosCapturados) {
        this.totalArticulosCapturados = totalArticulosCapturados;
    }

    public int getTotalArticulosEnPedido() {
        return totalArticulosEnPedido;
    }

    public void setTotalArticulosEnPedido(int totalArticulosEnPedido) {
        this.totalArticulosEnPedido = totalArticulosEnPedido;
    }


}
