package com.mycompany.crud;

import java.util.List;

public class Persona {
    private int id;
    private String nombre;
    private String direccion;
    private List<String> telefonos;

    public Persona(int id, String nombre, String direccion, List<String> telefonos) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefonos = telefonos;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public List<String> getTelefonos() { return telefonos; }
}
