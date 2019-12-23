package p2;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


public class Sint82P2 extends HttpServlet {
   
   
   private HashMap<String, Document> ficherosIML;
	private HashMap<String, String> anios;
	private LinkedHashMap<String, LinkedList<String>> warnings;
	private LinkedHashMap<String, LinkedList<String>> errores;
	private LinkedHashMap<String, LinkedList<String>> erroresFatales;

   private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
   private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
   private static final String INI_FORM = "<form action=\"P2TV\" method=\"get\">";
   private static final String SEND_BUTTON = "<br><input type=\"submit\"  value=\"Enviar\" id=\"send_button\"";
   private static final String BACK_BUTTON = "<br><input type=\"submit\" value=\"Atrás\" id=\"back_button\"";
   private static final String BEG_BUTTON = "<br><input type=\"submit\"  value=\"Inicio\" id=\"beginning_button\" onClick=\"document.forms[0].pfase.value='01'\"/>";
   private static final String END_FORM = "</form>";
   private static final String TVML_INI = "http://gssi.det.uvigo.es/users/agil/public_html/SINT/19-20/tvml-2004-12-01.xml";
   private static String URL_XSD = "p2/tvml.xsd";

   public void init (ServletConfig config) {
		// Empezamos leyendo el fichero inicial, y a partir de él sacamos el resto
		URL_XSD = config.getServletContext().getRealPath(URL_XSD);
		TVML_browser(TVML_INI);
		// A partir de aquí, ya podemos procesar peticiones GET de los clientes

	}
     
   
   public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
      res.setContentType("text/html");
      PrintWriter out = res.getWriter();
      String p = req.getParameter("p");
      String pfase = req.getParameter("pfase");

      out.println("<html>");
      out.println("<head>");
      out.println("<title>Servicio de información musical</title>"); // título de la página
      out.println("<meta charset=\"UTF-8\"/>"); // codificación de la página (UTF-8)
      out.println("<LINK rel=\"stylesheet\" href=\"p2/p2.css\"     type=\"text/css\" />");
      out.println("</head>");

      if (pfase == null)
         pfase = "01";

      if (p == null || !p.equals("p4sss1nt82")) {
         out.println("<body>");
         out.println("<h1> Contraseña incorrecta! </h1>");
         out.println("</body>");
         out.println("</html>");
         return;
      }

      switch (pfase) {

      case "01":

         this.doGetFase01(req, res);

         break;

      case "02":

         break;

      case "11":

         this.doGetFase11(req, res);

         break;
      case "12":

         this.doGetFase12(req, res);
         break;

      case "13":

         this.doGetFase13(req, res);
         break;

      default:

         out.println("<body>");
         out.println("<h1> Pfase incorrecto </h1>");
         out.println("</body>");
         out.println("</html>");

         break;

      }

   }

   private void doGetFase01(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

      PrintWriter out = res.getWriter();
      out.println("<body><h1>Servicio de información sobre canales TV</h1>");

      out.println("<h2>Bienvenido al servicio de consulta de información sobre canales de TV</h2> ");
      // out.println("<br><a href=\"P2IM?pfase=02&p=" + PASSWORD + "\">Listar ficheros
      // IML erróneos</a>"); // Opción para mostrar los ficheros erróneos (fase 02)
      out.println("<br><h3>Selecciona una consulta:</h3> "// Opción para mostrar la lista de años (fase 11)
            + INI_FORM
            + "<input type=\"radio\" name=\"pfase\" value=\"11\" checked/> <b>Consulta 1:</b> Películas de un día en un canal <br><br>" // ?pfase=11
            + "<input type=\"hidden\" name=\"p\" value=\"" + req.getParameter("p") + "\"/>" + SEND_BUTTON);
      out.println("<h3>"+ficherosIML.values().toString()+"</h3>");
      out.println("</body>");
      out.println("</html>");

   }

  /* private void doGetFase02(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

     boolean auto=false; 
     out.println(auto?"<errores>":"<h2>Ficheros erróneos</h2><br>");

			// Imprime los warnings
			out.println(auto?"<warnings>":"<h3>Se han encontrado " + warnings.size() + " ficheros con warnings:</h3><br><ul>");
			for(String urlFichero: warnings.keySet()) {
				// Muestra los warnings de cada fichero
				out.println(auto?"<warning>":"");
				out.println(auto?"<file>" + urlFichero + "</file><cause>":"<li><b>" + urlFichero + ":</b><br><ul>");
				Iterable<String> detalles = warnings.get(urlFichero);
				for(String detalle: detalles) {
					// Va imprimiendo una a una todas las warnings
					out.println(auto?detalle:"<li>" + detalle);
				}
				out.println(auto?"</cause></warning>":"</ul><br>");

			}
			out.println(auto?"</warnings>":"</ul><br>");

			// Imprime los errores
			out.println(auto?"<errors>":"<h3>Se han encontrado " + errores.size() + " ficheros con errores:</h3><br><ul>");
			for(String urlFichero: errores.keySet()) {
				// Muestra los errores de cada fichero
				out.println(auto?"<error>":"");
				out.println(auto?"<file>" + urlFichero + "</file><cause>":"<li><b>" + urlFichero + ":</b><br><ul>");
				Iterable<String> detalles = errores.get(urlFichero);
				for(String detalle: detalles) {
					// Va imprimiendo uno a uno todos los errores
					out.println(auto?detalle:"<li>" + detalle + "<br>");
				}
				out.println(auto?"</cause></error>":"</ul><br>");

			}
			out.println(auto?"</errors>":"</ul><br>");

			// Imprime los fatal errors
			out.println(auto?"<fatalerrors>":"<h3>Se han encontrado " + erroresFatales.size() + " ficheros con errores fatales:</h3><br><ul>");
			for(String urlFichero: erroresFatales.keySet()) {
				// Muestra los errores fatales de cada fichero
				out.println(auto?"<fatalerror>":"");
				out.println(auto?"<file>" + urlFichero + "</file><cause>":"<li><b>" + urlFichero + ":</b><br><ul>");
				Iterable<String> detalles = erroresFatales.get(urlFichero);
				for(String detalle: detalles) {
					// Va imprimiendo uno a uno todos los errores fatales
					out.println(auto?detalle:"<li>" + detalle + "<br>");
				}
				out.println(auto?"</cause></fatalerror>":"</ul><br>");
			}

			out.println(auto?"</fatalerrors>":"</ul><br>");


   }*/






   private void doGetFase11(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

      PrintWriter out = res.getWriter();

      out.println("<body><h1>Servicio de información sobre canales TV</h1>");

      out.println("<h2>Consulta 1</h2> ");
      // out.println("<br><a href=\"P2IM?pfase=02&p=" + PASSWORD + "\">Listar ficheros
      // IML erróneos</a>"); // Opción para mostrar los ficheros erróneos (fase 02)

      out.println("<br><h3>Selecciona una fecha:</h3> "); // Opción para mostrar la lista de años (fase 11)
      out.println(INI_FORM
            + "<input type=\"radio\" checked/> <b>Aqui irian todas las fechas:</b> Películas de un día en un canal <br><br>");
      out.println("<input type=\"hidden\" name=\"p\" value=\"" + req.getParameter("p") + "\"/>");
      out.println("<input type=\"hidden\" name=\"pfase\" value=\"11\"/>");
      out.println(SEND_BUTTON + " onClick=\"document.forms[0].pfase.value='12'\"/>");
      out.println(BACK_BUTTON + " onClick=\"document.forms[0].pfase.value='01'\"/>");
      out.println(END_FORM + "</body>");
      out.println("</html>");

   }

   private void doGetFase12(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

      PrintWriter out = res.getWriter();
      out.println("<body><h1>Servicio de información sobre canales TV</h1>");

      out.println("<h2>Consulta1: fecha anteriormente seleccionada</h2> ");
      // out.println("<br><a href=\"P2IM?pfase=02&p=" + PASSWORD + "\">Listar ficheros
      // IML erróneos</a>"); // Opción para mostrar los ficheros erróneos (fase 02)
      out.println("<br><h3>Selecciona un canal:</h3> ");// Opción para mostrar la lista de años (fase 11)
      out.println(INI_FORM
            + "<input type=\"radio\" checked/> <b>Aqui irian todas las fechas:</b> Películas de un día en un canal <br><br>");
      out.println("<input type=\"hidden\" name=\"p\" value=\"" + req.getParameter("p") + "\"/>");
      out.println("<input type=\"hidden\" name=\"pfase\" value=\"11\"/>");
      out.println(SEND_BUTTON + " onClick=\"document.forms[0].pfase.value='13'\"/>");
      out.println(BACK_BUTTON + " onClick=\"document.forms[0].pfase.value='11'\"/>");
      out.println(BEG_BUTTON);
      out.println(END_FORM + "</body>");
      out.println("</html>");

   }

   private void doGetFase13(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

      PrintWriter out = res.getWriter();
      out.println("<body><h1>Servicio de información sobre canales TV</h1>");

      out.println("<h2>Consulta 1: fecha seleccionada y canal seleccionado</h2> ");
      // out.println("<br><a href=\"P2IM?pfase=02&p=" + PASSWORD + "\">Listar ficheros
      // IML erróneos</a>"); // Opción para mostrar los ficheros erróneos (fase 02)
      out.println("<br><h3>Mostrar resultado:</h3> ");// Opción para mostrar la lista de años (fase 11)
      out.println(INI_FORM + "<input type=\"hidden\" name=\"p\" value=\"" + req.getParameter("p") + "\"/>");
      out.println("<input type=\"hidden\" name=\"pfase\" value=\"11\"/>");
      out.println(BACK_BUTTON + " onClick=\"document.forms[0].pfase.value='12'\"/>");
      out.println(BEG_BUTTON);
      out.println(END_FORM + "</body>");
      out.println("</html>");

   }




   private void TVML_browser (String fichero) 
   {
      System.out.println("EMPEZAMOS");
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      ficherosIML = new HashMap<String, Document>(); // inicializa el árbol de ficheros IML...
		anios = new HashMap<String, String>(); // ... y un registro de como se llama el fichero de cada año
		warnings = new LinkedHashMap<String, LinkedList<String>>(); // inicializa el registro de warnings...
		errores = new LinkedHashMap<String, LinkedList<String>>(); // ... el de errores...
		erroresFatales = new LinkedHashMap<String, LinkedList<String>>(); // ... el de errores fatales
		dbf.setValidating(true);
		dbf.setNamespaceAware(true);
		dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
		dbf.setAttribute(JAXP_SCHEMA_SOURCE, URL_XSD);

		try {
			leerDoc(dbf, fichero);
		}catch(ParserConfigurationException e1) {
         e1.printStackTrace();
      }
      

      System.out.println("numero errores---->"+errores.size());
      errores.forEach((f, e) -> {
         System.out.println(f + " => " + e);
     });
      System.out.println("numero errores fatales---->"+erroresFatales.size());
      erroresFatales.forEach((f, e) -> {
         System.out.println(f + " => " + e);
     });
      System.out.println("numero warnings---->"+warnings.size());
      warnings.forEach((f, e) -> {
         System.out.println(f + " => " + e);
     });
      
   }

   


   private void leerDoc(DocumentBuilderFactory factory, String urlFicheroBase) throws ParserConfigurationException {
		final String ROOT = ""; // Ubicación de todo aquel archivo que no tenga nombre
		DocumentBuilder dbuilder = factory.newDocumentBuilder();
		XMLErrorHandler errorHandler = new XMLErrorHandler(urlFicheroBase); // se encarga de validar la sintaxis XML del documento (no el schema!)
		dbuilder.setErrorHandler(errorHandler);
		Document documentoBase = null;
		try {
			documentoBase = dbuilder.parse(new InputSource(new URL(urlFicheroBase).openStream())); // lee el fichero IML base,...
		}catch(SAXException e1) {
			// Documento con malformación XML
			LinkedList<String> malformacion = new LinkedList<String>();
			malformacion.add(e1.getMessage());
			erroresFatales.put(urlFicheroBase, malformacion);

		}catch(IOException e2) {
			// El fichero no existe, pasamos al siguiente fichero...
		}

		if(documentoBase!=null) {
      
      System.out.println("-------------------Documento leido------------------");
      }
   
   }






   class XMLErrorHandler extends DefaultHandler{

		private String nombreFichero;
		private boolean correcto = true;

		XMLErrorHandler(String ficheroIML){
			this.nombreFichero = ficheroIML;
		}

		public void warning(SAXParseException spe) {
			if(warnings.get(nombreFichero)==null) {
				warnings.put(nombreFichero, new LinkedList<String>());
			}
			warnings.get(nombreFichero).add(spe.getMessage());
			correcto = false;
		}

		public void error(SAXParseException spe) {
			if(errores.get(nombreFichero)==null) {
				errores.put(nombreFichero, new LinkedList<String>());
			}
			errores.get(nombreFichero).add(spe.getMessage());
			correcto = false;
		}

		public void fatalError(SAXParseException spe) {
			if(erroresFatales.get(nombreFichero)==null) {
				erroresFatales.put(nombreFichero, new LinkedList<String>());
			}
			erroresFatales.get(nombreFichero).add(spe.getMessage());
			correcto = false;
		}

		/**
		 * Informa de si el fichero está correcto o presenta algún warning o error.
		 * @return true si correcto, false si hay algún problema.
		 */
		public boolean isXMLCorrecto() {
			return correcto;
		}

	}






}
