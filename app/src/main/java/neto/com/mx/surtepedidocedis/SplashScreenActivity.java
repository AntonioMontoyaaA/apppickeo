package neto.com.mx.surtepedidocedis;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import neto.com.mx.surtepedidocedis.decarga_version.DescargaUltimaVersionDialogPrueba;
import neto.com.mx.surtepedidocedis.decarga_version.DescargaUltimaVersionDialog_https;
import neto.com.mx.surtepedidocedis.utiles.Constantes;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int UPDATEINSTALL_CODE = 0;
    private final int SPLASH_DISPLAY_LENGTH = 2000;
    private static SplashScreenActivity myContext;
    public static boolean bandera_bloqueaUsuario = false;

    public static SplashScreenActivity getMyContext() {
        return myContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        getSupportActionBar().hide();

        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        TypedValue tv = new TypedValue();
        this.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
        int actionBarHeight = getResources().getDimensionPixelSize(tv.resourceId);

        //Versi??n
        try {
            TextView versionText = (TextView) findViewById(R.id.versionAppText);
            versionText.setText("?? Todos los derechos reservados      v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName+"\nAndroid Id: "+ Settings.Secure.getString( getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
        } catch(PackageManager.NameNotFoundException ne) {
            Log.e("CARGA_FOLIO_TAG", "Error al obtener la versi??n: " + ne.getMessage());
        }


        //Verificacion de version
        Intent intentVersion ;
        if (Constantes.CADENA_CONEXION.contains( "http://10.81.12.203:8003" )){
            intentVersion= new Intent(this,
                    DescargaUltimaVersionDialogPrueba.class);//PROD
            System.out.println("cadenaConexion: PROD");

        }else {
            intentVersion = new Intent( this,
                    DescargaUltimaVersionDialog_https.class );//DESA_QA

            System.out.println("cadenaConexion: DESA_QA");
        }
        startActivityForResult(intentVersion, UPDATEINSTALL_CODE);

    }

    public void iniciaApp(View view) {

        SharedPreferences preferences = getSharedPreferences( "bloqueaUsuario",Context.MODE_PRIVATE );
        System.out.println("valores de sharedPreferences: "+ preferences.getAll());
        boolean bloqueaUsuario = preferences.getBoolean( "bloquear",true );

        if (bandera_bloqueaUsuario == true || bloqueaUsuario == true){
            MostrarSnack( view,"para poder iniciar sesion, se necesita que La aplicaci??n se encuentre asignada al dispositivo" );

        }else {
            Vibrator vibe = (Vibrator) getSystemService( Context.VIBRATOR_SERVICE );
            vibe.vibrate( 100 );
            // Do something in response to button
            Intent mainIntent = new Intent( SplashScreenActivity.this, LoginActivity.class );
            SplashScreenActivity.this.startActivity( mainIntent );
            SplashScreenActivity.this.finish();
        }
    }

    @Override
    public void onBackPressed() {
    }

    public void MostrarSnack(View view, String texto){
        Snackbar snackbar = Snackbar.make(view, texto, Snackbar.LENGTH_LONG);
        View view2 = snackbar .getView();
        TextView textView = (TextView) view2.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor( Color.WHITE);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)view2.getLayoutParams();
        params.gravity = Gravity.CENTER;
        params.leftMargin = 5;
        params.rightMargin = 5;
        view2.setLayoutParams(params);
        snackbar.show();
    }
}
