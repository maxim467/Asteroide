package com.example.miguelgarcia.asteroides;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.Vector;

/**
 * Created by MiguelGarcia on 21/2/15.
 */
public class VistaJuego extends View {


    // //// ASTEROIDES //////
    private Grafico nave;// Gráfico de la nave

    private int giroNave; // Incremento de dirección

    private float aceleracionNave; // aumento de velocidad


    // //// THREAD Y TIEMPO //////
    // Thread encargado de procesar el juego
    private ThreadJuego thread = new ThreadJuego();
    // Cada cuanto queremos procesar cambios (ms)
    private static int PERIODO_PROCESO = 50;
    // Cuando se realizó el último proceso
    private long ultimoProceso = 0;

    // //// MISIL //////
    private Grafico misil;
    private static int PASO_VELOCIDAD_MISIL = 12;
    private boolean misilActivo = false;
    private int tiempoMisil;




    // Incremento estándar de giro y aceleración

    private static final int PASO_GIRO_NAVE = 5;

    private static final float PASO_ACELERACION_NAVE = 0.5f;



    private Vector<Grafico> Asteroides; // Vector con los Asteroides

    private int numAsteroides= 5; // Número inicial de asteroides

    private int numFragmentos= 3; // Fragmentos en que se divide



    public VistaJuego(Context context, AttributeSet attrs) {





        super(context, attrs);

        Drawable drawableNave, drawableAsteroide, drawableMisil;

        drawableAsteroide = context.getResources().getDrawable(
                R.drawable.asteroide1);

        drawableNave = context.getResources().getDrawable(
                R.drawable.nave);

        drawableMisil = context.getResources().getDrawable(
                R.drawable.misil1);


        nave = new Grafico(this, drawableNave);
        misil = new Grafico(this, drawableMisil);

        Asteroides = new Vector<Grafico>();







        for (int i = 0; i < numAsteroides; i++) {

            Grafico asteroide = new Grafico(this, drawableAsteroide);

            asteroide.setIncY(Math.random() * 4 - 2);

            asteroide.setIncX(Math.random() * 4 - 2);

            asteroide.setAngulo((int) (Math.random() * 360));

            asteroide.setRotacion((int) (Math.random() * 8 - 4));

            Asteroides.add(asteroide);

        }

    }



    @Override protected void onSizeChanged(int ancho, int alto,
                                           int ancho_anter, int alto_anter) {

        super.onSizeChanged(ancho, alto, ancho_anter, alto_anter);

        // Una vez que conocemos nuestro ancho y alto.

        nave.setPosX((ancho - nave.getAncho()) / 2);
        nave.setPosY((alto - nave.getAlto()) / 2);


        for (Grafico asteroide: Asteroides) {

            do{
                asteroide.setPosX(Math.random()*(ancho-asteroide.getAncho()));
                asteroide.setPosY(Math.random()*(alto-asteroide.getAlto()));
            } while(asteroide.distancia(nave) < (ancho+alto)/5);


        }
        ultimoProceso = System.currentTimeMillis();
        thread.start();


    }



    @Override protected synchronized void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        for (Grafico asteroide: Asteroides) {

            asteroide.dibujaGrafico(canvas);

        }
        if (misilActivo) {
            misil.dibujaGrafico(canvas);
        }
        nave.dibujaGrafico(canvas);

    }
    protected synchronized void actualizaFisica() {
        long ahora = System.currentTimeMillis();
        // No hagas nada si el período de proceso no se ha cumplido.
        if (ultimoProceso + PERIODO_PROCESO > ahora) {
            return;
        }
        // Para una ejecución en tiempo real calculamos retardo
        double retardo = (ahora - ultimoProceso) / PERIODO_PROCESO;
        ultimoProceso = ahora; // Para la próxima vez
        // Actualizamos velocidad y dirección de la nave a partir de
        // giroNave y aceleracionNave (según la entrada del jugador)
        nave.setAngulo((int) (nave.getAngulo() + giroNave * retardo));
        double nIncX = nave.getIncX() + aceleracionNave *
                Math.cos(Math.toRadians(nave.getAngulo())) * retardo;
        double nIncY = nave.getIncY() + aceleracionNave *
                Math.sin(Math.toRadians(nave.getAngulo())) * retardo;
        // Actualizamos si el módulo de la velocidad no excede el máximo
        if (Math.hypot(nIncX,nIncY) <= Grafico.getMaxVelocidad()){
            nave.setIncX(nIncX);
            nave.setIncY(nIncY);
        }
        // Actualizamos posiciones X e Y
        nave.incrementaPos(retardo);
        for (Grafico asteroide : Asteroides) {
            asteroide.incrementaPos(retardo);
        }

        // Actualizamos posición de misil
        if (misilActivo) {
            misil.incrementaPos(retardo);
            tiempoMisil-=retardo;
            if (tiempoMisil < 0) {
                misilActivo = false;
            } else {
                for (int i = 0; i < Asteroides.size(); i++)
                    if (misil.verificaColision(Asteroides.elementAt(i))) {
                        destruyeAsteroide(i);
                        break;
                    }
            }
        }

    }

    class ThreadJuego extends Thread {
        @Override
        public void run() {
            while (true) {
                actualizaFisica();
            }
        }
    }


    @Override
    public boolean onKeyUp(int codigoTecla, KeyEvent evento) {
        super.onKeyUp(codigoTecla, evento);
        // Suponemos que vamos a procesar la pulsación
        boolean procesada = true;
        switch (codigoTecla) {
            case KeyEvent.KEYCODE_DPAD_UP:
                aceleracionNave = 0;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                giroNave = 0;
                break;
            default:
                // Si estamos aquí, no hay pulsación que nos interese
                procesada = false;
                break;
        }
        return procesada;
    }



    private float mX=0, mY=0;
    private boolean disparo=false;

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                disparo=true;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - mX);
                float dy = Math.abs(y - mY);
                if (dy<6 && dx>6){
                    giroNave = Math.round((x - mX) / 2);
                    disparo = false;
                } else if (dx<6 && dy>6){
                    aceleracionNave = Math.round((mY - y) / 25);
                    disparo = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                giroNave = 0;
                aceleracionNave = 0;
                if (disparo){
                    ActivaMisil();
                }
                break;
        }
        mX=x; mY=y;
        return true;
    }



    private void destruyeAsteroide(int i) {
        Asteroides.remove(i);
        misilActivo = false;
    }

    private void ActivaMisil() {

        misil.setPosX(nave.getPosX() + nave.getAncho() / 2 - misil.getAncho()
                / 2);
        misil.setPosY(nave.getPosY() + nave.getAlto() / 2 - misil.getAlto() / 2);
        misil.setAngulo(nave.getAngulo());
        misil.setIncX(Math.cos(Math.toRadians(misil.getAngulo()))
                * PASO_VELOCIDAD_MISIL);
        misil.setIncY(Math.sin(Math.toRadians(misil.getAngulo()))
                * PASO_VELOCIDAD_MISIL);
        tiempoMisil = (int) Math.min(
                this.getWidth() / Math.abs(misil.getIncX()), this.getHeight()
                        / Math.abs(misil.getIncY())) - 2;
        misilActivo = true;
    }




}