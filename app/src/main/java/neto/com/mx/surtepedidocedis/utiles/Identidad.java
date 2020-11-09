package neto.com.mx.surtepedidocedis.utiles;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

import neto.com.mx.surtepedidocedis.SplashScreenActivity;

public class Identidad {
    private static String MODELO_ZEBRA = "MC33";
    private static String MANUFACTURER = "Zebra Technologies";
    private static int SDK_NUMERO_27 = 23;

    private static boolean esZEBRA_MC33() {
        return MANUFACTURER.equals( Build.MANUFACTURER) &&
                MODELO_ZEBRA.equals( Build.MODEL) &&
                Build.VERSION.SDK_INT == SDK_NUMERO_27;
    }
    private static boolean esRequiereLecturaSerie() {
        return Build.VERSION.SDK_INT >= SDK_NUMERO_27;
    }

    public static String leerIdentificadorDispositivo(Activity activity) throws Exception {
        TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService( Context.TELEPHONY_SERVICE);
        String imeii = null;
        String macAddress = null;

         if( esRequiereLecturaSerie() ) {
             StringBuilder strB = new StringBuilder();
             for (Character ss : Build.SERIAL.toUpperCase().toCharArray()){
                 int dato = (ss - 'A'+1);
                 if( dato < 0){
                     strB.append(ss);
                 }else{
                     strB.append(dato);
                 }
             }
           return strB.toString();
        }

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            imeii = telephonyManager.getDeviceId();
            //return TODO;
        }



        if (imeii != null && !"".equals(imeii)){
            return imeii;
        } else if( esZEBRA_MC33() ) {
            macAddress = Build.SERIAL;
        }else{
            try {

                List<NetworkInterface> networkInterfaces = Collections.list( NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface nif : networkInterfaces) {
                    //Pintar el nombre dispositivo de la MAC

                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes != null) {

                        StringBuilder strBuildMac = new StringBuilder();
                        for (byte b : macBytes) {
                            strBuildMac.append( String.format("%02X",b));
                        }

                        macAddress = Util.HexadecilaToDecimal(strBuildMac.toString());
                        //Log.i(GlobalShare.logAplicaion, Identidad.class.getName()+" : macAddress > "+macAddress);
                        break;
                    }
                }
            }catch(SocketException se){
                throw new Exception("No fue posible leer el IMEII de este dispositivo > " +
                        macAddress +" - "+ se.getMessage(), se);
            }

        }

        if( macAddress != null && !"".equals(macAddress))
            return macAddress;
        else
            throw new Exception("No se pudo obtener la identidad del dispositivo.");

    }
}



/*


public static String leerIdentificadorDispositivo(Activity activity) throws Exception {
        TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String imeii = telephonyManager.getDeviceId();
        String macAddress = null;

        if (imeii != null && !"".equals(imeii)){
            return imeii;
        }else{
            try {

                List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface nif : networkInterfaces) {
                    //Pintar el nombre dispositivo de la MAC

                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes != null) {

                        StringBuilder strBuildMac = new StringBuilder();
                        for (byte b : macBytes) {
                            strBuildMac.append(String.format("%02X",b));
                        }

                        macAddress = Util.HexadecilaToDecimal(strBuildMac.toString());
                        //Log.i(GlobalShare.logAplicaion, Identidad.class.getName()+" : macAddress > "+macAddress);
                        break;
                    }
                }
            }catch(SocketException se){
                throw new Exception("No fue posible leer el IMEII de este dispositivo > " +
                        macAddress +" - "+ se.getMessage(), se);
            }

        }

        if( macAddress != null && !"".equals(macAddress))
            return macAddress;
        else
            throw new Exception("No se pudo obtener la identidad del dispositivo.");

    }
 */