package neto.com.mx.surtepedidocedis.utiles;


import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * Created by yruizm on 21/07/17.
 */

public class SoundManager {

    private Context pContext;
    private SoundPool sndPool;
    private float rate = 1.0f;
    private float leftVolume = 1.0f;
    private float rightVolume = 1.0f;

    public SoundManager(Context appContext) {
        sndPool = new SoundPool(8, AudioManager.STREAM_MUSIC, 0);
        pContext = appContext;
    }

    public int load(int idSonido) {
        return sndPool.load(pContext, idSonido, 1);
    }

    //Ejecuta el sonido, toma como parametro el id del sonido a ejecutar.
    public void play(int idSonido)
    {
        sndPool.play(idSonido, leftVolume, rightVolume, 1, 0, rate);
    }

    // Libera memoria de todos los objetos del sndPool que ya no son requeridos.
    public void unloadAll()
    {
        sndPool.release();
    }
}