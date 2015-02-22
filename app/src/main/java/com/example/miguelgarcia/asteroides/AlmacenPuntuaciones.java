package com.example.miguelgarcia.asteroides;

import java.util.Vector;

/**
 * Created by MiguelGarcia on 21/2/15.
 */
public interface AlmacenPuntuaciones {
    public void guardarPuntuacion(int puntos,String nombre,long fecha);
    public Vector<String> listaPuntuaciones(int cantidad);
}
