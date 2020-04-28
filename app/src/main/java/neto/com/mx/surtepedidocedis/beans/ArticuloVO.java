package neto.com.mx.surtepedidocedis.beans;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Created by yruizm on 30/10/16.
 */

public class ArticuloVO implements Serializable {

    private long articuloId;
    private HashSet<String> codigos;
    private int totalCajasAsignadas;
    private int totalCajasPickeadas;
    private String nombreArticulo;
    private int normaEmpaque;
    private String unidadMedida;
    private int unidadMedidaId;
    private boolean esCapturado = false;
    private boolean esBuscadoDiferencias = false;
    private int posicion = 0;
    private boolean esArticuloContado = false;

    public ArticuloVO() {
        codigos = new HashSet<String>();
    }

    public long getArticuloId() {
        return articuloId;
    }

    public void setArticuloId(long articuloId) {
        this.articuloId = articuloId;
    }

    public HashSet<String> getCodigos() {
        return codigos;
    }

    public void setCodigos(HashSet<String> codigos) {
        this.codigos = codigos;
    }

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

    public String getNombreArticulo() {
        return nombreArticulo;
    }

    public void setNombreArticulo(String nombreArticulo) {
        this.nombreArticulo = nombreArticulo;
    }

    public int getNormaEmpaque() {
        return normaEmpaque;
    }

    public void setNormaEmpaque(int normaEmpaque) {
        this.normaEmpaque = normaEmpaque;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public int getUnidadMedidaId() {
        return unidadMedidaId;
    }

    public void setUnidadMedidaId(int unidadMedidaId) {
        this.unidadMedidaId = unidadMedidaId;
    }

    public boolean isEsCapturado() {
        return esCapturado;
    }

    public void setEsCapturado(boolean esCapturado) {
        this.esCapturado = esCapturado;
    }

    public boolean isEsBuscadoDiferencias() {
        return esBuscadoDiferencias;
    }

    public void setEsBuscadoDiferencias(boolean esBuscadoDiferencias) {
        this.esBuscadoDiferencias = esBuscadoDiferencias;
    }

    public int getPosicion() {
        return posicion;
    }

    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    public boolean isEsArticuloContado() {
        return esArticuloContado;
    }

    public void setEsArticuloContado(boolean esArticuloContado) {
        this.esArticuloContado = esArticuloContado;
    }

    @Override
    public String toString() {
        return "ArticuloVO{" +
                "articuloId=" + articuloId +
                ", codigos=" + codigos +
                ", totalCajasAsignadas=" + totalCajasAsignadas +
                ", totalCajasSurtidas=" + totalCajasPickeadas +
                ", nombreArticulo='" + nombreArticulo + '\'' +
                ", normaEmpaque=" + normaEmpaque +
                ", unidadMedida='" + unidadMedida + '\'' +
                ", unidadMedidaId=" + unidadMedidaId +
                ", esCapturado=" + esCapturado +
                ", esBuscadoDiferencias=" + esBuscadoDiferencias +
                ", posicion=" + posicion +
                ", esArticuloContado=" + esArticuloContado +
                '}';
    }
}
