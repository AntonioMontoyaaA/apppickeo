package neto.com.mx.surtepedidocedis.mensajes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;

/**
 *
 * @author dramirezr
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({"indice","tipoDato","valor"})
public class ParametroTipo implements Serializable {
    @JsonProperty("tipoDato")
    private String tipoDato;
    @JsonProperty("valor")
    private String valor;
    private final static long serialVersionUID = -8070836895723993550L;

    public ParametroTipo() {}
    public ParametroTipo(String tipoDato, String valor) {
        super();
        this.tipoDato = tipoDato;
        this.valor = valor;
    }

    @JsonProperty("tipoDato")
    public String getTipoDato() {
        return tipoDato;
    }

    @JsonProperty("tipoDato")
    public void setTipoDato(String tipoDato) {
        this.tipoDato = tipoDato;
    }

    @JsonProperty("valor")
    public String getValor() {
        return valor;
    }

    @JsonProperty("valor")
    public void setValor(String valor) {
        this.valor = valor;
    }
}
