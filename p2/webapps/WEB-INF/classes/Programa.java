package p2;

import java.util.Comparator;
import java.util.ArrayList;

import java.lang.String;


public class Programa {
	
	private String pelicula; // titulo de la pelicula
	private String edad; //edad minima de la pelicula
	private String hora; // hora del pelicula
	private String comentario; //comentarios sobre la película
	

	public Programa(){
		
	}

	public Programa(String pelicula, String edad,  String hora) {
		this.pelicula = pelicula;
		this.edad = edad;
		this.hora = hora;
	}
	
	public String getpelicula() {
		return this.pelicula;
	}



	public String getedad() {
		return this.edad;
	}


	public String gethora() {
		return this.hora;
	}

	public String getcomentario() {
		return this.comentario;
	}


	public void setpelicula(String pelicula) {
		this.pelicula= pelicula;
	}



	public void setedad(String edad) {
		this.edad= edad;
	}



	public void sethora(String hora) {
		this.hora=hora;
	}

	public void setcomentario(String comentario) {
		this.comentario=comentario;
	}



	public static class sort_length implements Comparator<Programa>{

		@Override
		public int compare(Programa o1, Programa o2) {

			if (o1.getpelicula().trim().length() > o2.getpelicula().trim().length()) {
				return 1;
			} else if (o1.getpelicula().trim().length() < o2.getpelicula().trim().length()) {
				return -1;
			} else {
				return -1;
			}
			
		}
		
	}
	
	
	
}