package com.mycompany.crud;

import java.util.ArrayList;
import java.util.Scanner;

public class CRUD {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        boolean salir = false;

        while (!salir) {
            System.out.println("\n==== MENÚ AGENDA ====");
            System.out.println("Elige una opción: ");
            System.out.println("1. Listar personas");
            System.out.println("2. Insertar persona");
            System.out.println("3. Eliminar persona");
            System.out.println("4. Modificar persona");
            System.out.println("5. Salir");
            String opcion = sc.nextLine();

            switch (opcion) {
                case "1":
                    AgendaDB.mostrarPersonas();
                    break;

                case "2":
                    System.out.print("Nombre: ");
                    String nombre = sc.nextLine();
                    System.out.print("Dirección: ");
                    String direccion = sc.nextLine();
                    ArrayList<String> telefonos = new ArrayList<>();
                    while (true) {
                    System.out.print("Agregar teléfono (enter para terminar): ");
                    String tel = sc.nextLine();
                    if (tel.isEmpty()) break;
                    telefonos.add(tel);
                    }

                    AgendaDB.insertarPersona(nombre, direccion,telefonos);
                    break;

                case "3":
                    System.out.print("ID de la persona a eliminar: ");
                    int idEliminar = Integer.parseInt(sc.nextLine());
                    AgendaDB.eliminarPersona(idEliminar);
                    break;

                case "4":
                    System.out.print("ID de la persona a modificar: ");
                    int idModificar = Integer.parseInt(sc.nextLine());

                    System.out.println("¿Qué deseas modificar?");
                    System.out.println("1. Datos personales (nombre y dirección)");
                    System.out.println("2. Teléfonos");
                    String optMod = sc.nextLine();

                    if (optMod.equals("1")) {
                        System.out.print("Nuevo nombre: ");
                        String nuevoNombre = sc.nextLine();
                        System.out.print("Nueva dirección: ");
                        String nuevaDireccion = sc.nextLine();
                        AgendaDB.modificarDatosPersona(idModificar, nuevoNombre, nuevaDireccion);
                    } else if (optMod.equals("2")) {
                        ArrayList<String> nuevosTelefonos = new ArrayList<>();
                        while (true) {
                            System.out.print("Nuevo teléfono (enter para terminar): ");
                            String tel = sc.nextLine();
                            if (tel.isEmpty()) break;
                            nuevosTelefonos.add(tel);
                        }
                        AgendaDB.modificarTelefonos(idModificar, nuevosTelefonos);
                    } else {
                        System.out.println("Opción inválida.");
                        }
                    break;

                case "5":
                    salir = true;
                    System.out.println("Saliendo del programa...");
                    break;

                default:
                    System.out.println("Opción inválida");
            }
        }
        sc.close();
    }
}
