package neto.com.mx.surtepedidocedis;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class FinalizaActivity extends AppCompatActivity {

    private String numeroEmpleado = "";
    private String nombreEmpleado = "";
    private String nombreTienda = "";
    private String nombreZona = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finaliza);
        getSupportActionBar().hide();

        nombreEmpleado = new String(this.getIntent().getStringExtra("nombreEmpleado").trim());
        numeroEmpleado = new String(this.getIntent().getStringExtra("numeroEmpleado").trim());
        nombreTienda = new String(this.getIntent().getStringExtra("nombreTienda").trim());
        nombreZona = new String(this.getIntent().getStringExtra("nombreZona").trim());
    }

    public void regresaMain(View view) {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);

        Intent intent = new Intent(this, CargaFolioPedidoActivity.class);
        intent.putExtra("numeroEmpleado", numeroEmpleado);
        intent.putExtra("nombreEmpleado", nombreEmpleado);
        intent.putExtra("nombreTienda", nombreTienda);
        intent.putExtra("nombreZona", nombreZona);
        startActivity(intent);
    }
}
