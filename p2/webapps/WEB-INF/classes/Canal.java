package p2;

import java.util.Comparator;
import java.util.ArrayList;
import java.lang.String;

public class Canal {
	
	private String canal; // identificador tipo IDD_DISCO-XX de la canción
	private String grupo;
	private String idioma; // idioma del canal
	

	public Canal(){
		
	}

	public Canal(String canal, String grupo,  String idioma) {
		this.canal = canal;
		this.grupo = grupo;
		this.idioma = idioma;
	}
	
	public String getcanal() {
		return this.canal;
	}



	public String getgrupo() {
		return this.grupo;
	}


	public String getidioma() {
		return this.idioma;
	}


	public void setcanal(String canal) {
		this.canal= canal;
	}



	public void setgrupo(String grupo) {
		this.grupo= grupo;
	}



	public void setidioma(String idioma) {
		this.idioma=idioma;
	}



	public static class sort_name implements Comparator<Canal>{

		@Override
		public int compare(Canal o1, Canal o2) {
			return o1.getcanal().compareTo(o2.getcanal()); 
			
		}
		
	}
	
	
}