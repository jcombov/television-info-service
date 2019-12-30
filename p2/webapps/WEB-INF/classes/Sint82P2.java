package p2;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.net.URL;
import java.util.*;
import java.util.Map.*;
import p2.Canal;
import p2.Programa;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.String;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class Sint82P2 extends HttpServlet {

   private TreeMap<String, Document> ficherosTVML;
   private TreeMap<String, ArrayList<String>> warnings;
   private TreeMap<String, ArrayList<String>> errores;
   private TreeMap<String, ArrayList<String>> erroresFatales;
   private ArrayList<String> filestoread;

   private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
   private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
   private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
   private static final String INI_FORM = "<form action=\"P2TV\" method=\"get\">";
   private static final String SEND_BUTTON = "<br><br><br><input type=\"submit\"  value=\"Enviar\" id=\"send_button\"";
   private static final String BACK_BUTTON = "  <input type=\"submit\" value=\"Atrás\" id=\"back_button\"";
   private static final String HOME_BUTTON = "  <input type=\"submit\"  value=\"Inicio\" id=\"home_button\" onClick=\"document.forms[0].pfase.value='01'\"/>";
   private static final String END_FORM = "</form>";
   private static String TVML_INI = "p2/tvml/tvml-2004-12-01.xml";
   private static String URL_XSD = "p2/tvml.xsd";

   public void init(ServletConfig config) {
      // Empezamos leyendo el fichero inicial, y a partir de él sacamos el resto
      URL_XSD = config.getServletContext().getRealPath(URL_XSD);
      TVML_INI = config.getServletContext().getRealPath(TVML_INI);
      TVML_browser(TVML_INI);
      // A partir de aquí, ya podemos procesar peticiones GET de los clientes

   }

   public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
      res.setContentType("text/html");
      PrintWriter out = res.getWriter();
      String p = req.getParameter("p");
      String pfase = req.getParameter("pfase");
      boolean auto = req.getParameter("auto") == null ? false : req.getParameter("auto").equals("si");

      if (auto) {
         res.setContentType("text/xml");
         out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
      } else {
         res.setContentType("text/html");
         out.println("<html>");
         out.println("<head>");
         out.println("<title>Servicio de información musical</title>"); // título de la página
         out.println("<meta charset=\"UTF-8\"/>"); // codificación de la página (UTF-8)
         out.println("<LINK rel=\"stylesheet\" href=\"p2/p2.css\"     type=\"text/css\" />");
         out.println("</head>");
      }

      if (pfase == null)
         pfase = "01";

      if (p == null) {

         out.println(auto ? "<wrongRequest>no passwd</wrongRequest>"
               : "<body><h1> Contraseña incorrecta! </h1></body></html>");

      }

      else if (!p.equals("p4sss1nt82")) {

         out.println(auto ? "<wrongRequest>bad passwd</wrongRequest>"
               : "<body><h1> Contraseña incorrecta! </h1></body></html>");

      }

      else {

         switch (pfase) {

         case "01":

            this.doGetFase01(req, res);

            break;

         case "02":
            this.doGetFase02(req, res);
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

            this.doGetFase01(req, res);

            break;

         }
      }

   }

   private void doGetFase01(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

      PrintWriter out = res.getWriter();
      boolean auto = req.getParameter("auto") == null ? false : req.getParameter("auto").equals("si");

      if (auto) {
         // XML
         out.println("<service> <status>OK</status> </service>");
      } else {
         out.println("<body><h1>Servicio de información sobre canales TV</h1><br><br>");

         out.println("<h2>Bienvenido al servicio de consulta de información sobre canales de TV</h2> ");
         out.println("<br><a href=\"P2TV?pfase=02&p=" + req.getParameter("p") + "\">Listar ficheros IML erróneos</a>"); // Opción
                                                                                                                        // para
                                                                                                                        // mostrar
                                                                                                                        // los
                                                                                                                        // ficheros
                                                                                                                        // erróneos
                                                                                                                        // (fase
                                                                                                                        // 02)
         out.println("<br><h3>Selecciona una consulta:</h3> "// Opción para mostrar la lista de años (fase 11)
               + INI_FORM
               + "<input type=\"radio\" name=\"pfase\" value=\"11\" checked/> <b>Consulta 1:</b> Películas de un día en un canal <br><br>" // ?pfase=11
               + "<input type=\"hidden\" name=\"p\" value=\"" + req.getParameter("p") + "\"/>" + SEND_BUTTON);

         out.println("</body>");
         out.println("</html>");
      }
   }

   private void doGetFase02(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

      boolean auto = req.getParameter("auto") == null ? false : req.getParameter("auto").equals("si");
      PrintWriter out = res.getWriter();
      out.println(auto ? "<errores>" : "<h2>Ficheros erróneos</h2><br>");

      // Imprime los warnings
      out.println(
            auto ? "<warnings>" : "<h3>Se han encontrado " + warnings.size() + " ficheros con warnings:</h3><br><ul>");
      for (String urlFichero : warnings.keySet()) { // Muestra los warnings de cada fichero
         out.println(auto ? "<warning>" : "");
         out.println(auto ? "<file>" + urlFichero + "</file><cause>" : "<li><b>" + urlFichero + ":</b><br><ul>");
         Iterable<String> detalles = warnings.get(urlFichero);
         for (String detalle : detalles) { // Va imprimiendo una a una todas las warnings
            out.println(auto ? detalle : "<li>" + detalle);
         }
         out.println(auto ? "</cause></warning>" : "</ul><br>");

      }

      out.println(auto ? "</warnings>" : "</ul><br>");

      // Imprime los errores
      out.println(
            auto ? "<errors>" : "<h3>Se han encontrado " + errores.size() + " ficheros con errores:</h3><br><ul>");
      for (String urlFichero : errores.keySet()) { // Muestra los errores de cada fichero
         out.println(auto ? "<error>" : "");
         out.println(auto ? "<file>" + urlFichero + "</file><cause>" : "<li><b>" + urlFichero + ":</b><br><ul>");
         Iterable<String> detalles = errores.get(urlFichero);
         for (String detalle : detalles) { // imprimiendo uno a uno todos los errores
            out.println(auto ? detalle : "<li>" + detalle + "<br>");
         }
         out.println(auto ? "</cause></error>" : "</ul><br>");
      }

      out.println(auto ? "</errors>" : "</ul><br>");

      // Imprime los fatal errors
      out.println(auto ? "<fatalerrors>"
            : "<h3>Se han encontrado " + erroresFatales.size() + " ficheros con errores fatales:</h3><br><ul>");
      for (String urlFichero : erroresFatales.keySet()) { // Muestra los errores fatales de cada fichero
         out.println(auto ? "<fatalerror>" : "");
         out.println(auto ? "<file>" + urlFichero + "</file><cause>" : "<li><b>" + urlFichero + ":</b><br><ul>");
         Iterable<String> detalles = erroresFatales.get(urlFichero);
         for (String detalle : detalles) { // Va imprimiendo uno a uno todos los errores fatales
            out.println(auto ? detalle : "<li>" + detalle + "<br>");
         }
         out.println(auto ? "</cause></fatalerror>" : "</ul><br>");
      }

      out.println(auto ? "</fatalerrors>" : "</ul><br>");
      out.println(auto?"":INI_FORM);
      out.println(auto ?"": "<input type=\"hidden\" name=\"p\" value=\"" + req.getParameter("p") + "\"/>");
      out.println(auto ?"":"<input type=\"hidden\" name=\"pfase\" value=\"02\"/>");
      
      out.println(auto? "" : BACK_BUTTON + " onClick=\"document.forms[0].pfase.value='01'\"/>");
      out.println(auto ? "</errores>" : "</html>");

   }

   private void doGetFase11(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
      int i;
      PrintWriter out = res.getWriter();
      boolean auto = req.getParameter("auto") == null ? false : req.getParameter("auto").equals("si");
      ArrayList<String> fechas = getC1Fechas();

      if (!auto) { // Respuesta HTML
         out.println("<body><h1>Servicio de información sobre canales TV</h1><br><br>");

         out.println("<h2>Consulta 1</h2> ");

         out.println("<br><h3>Selecciona una fecha:</h3> "); // Opción para mostrar la lista de años (fase 11)
         out.println(INI_FORM);
         for (i = 0; i < fechas.size(); i++) {
            out.println(i == 0
                  ? "<input type=\"radio\" name=\"panio\" class=\"form_input\" value=\"" + fechas.get(i)
                        + "\" checked/>  " + fechas.get(i) + "<br>"
                  : "<input type=\"radio\" name=\"panio\" class=\"form_input\" value=\"" + fechas.get(i) + "\" />  "
                        + fechas.get(i) + "<br>");
         }
         out.println("<input type=\"hidden\" name=\"p\" value=\"" + req.getParameter("p") + "\"/>");
         out.println("<input type=\"hidden\" name=\"pfase\" value=\"11\"/>");
         out.println(SEND_BUTTON + " onClick=\"document.forms[0].pfase.value='12'\"/>");
         out.println(BACK_BUTTON + " onClick=\"document.forms[0].pfase.value='01'\"/>");
         out.println(END_FORM + "</body>");
         out.println("</html>");

      } else { // Respuesta XML
         out.println("<dias>");
         for (i = 0; i < fechas.size(); i++) {

            out.println("<dia>" + fechas.get(i) + "</dia>");
         }

         out.println("</dias>");

      }

   }

   private void doGetFase12(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

      PrintWriter out = res.getWriter();
      String fecha = req.getParameter("panio");
      boolean auto = req.getParameter("auto") == null ? false : req.getParameter("auto").equals("si");
      int i;
      ArrayList<Canal> canal = getC1Canales(fecha);
      Collections.sort(canal, new Canal.sort_name());

      if (!auto) { // Respuesta html
         out.println("<body><h1>Servicio de información sobre canales TV</h1> <br><br>");
         out.println("<h2>Consulta1: Fecha= " + fecha + "</h2> ");
         out.println("<br><h3>Selecciona un canal:</h3> ");// Opción para mostrar la lista de años (fase 11)
         out.println(INI_FORM);
         for (i = 0; i < canal.size(); i++) {
            out.println(i == 0
                  ? "<input type=\"radio\" name=\"pcanal\" class=\"form_input\" value=\"" + canal.get(i).getcanal()
                        + "\" checked>" + "<b>  Canal:  </b>" + canal.get(i).getcanal() + "<b>  Idioma:  </b>"
                        + canal.get(i).getidioma() + "<b>  Grupo:  </b> " + canal.get(i).getgrupo() + "<br>"
                  : "<input type=\"radio\" name=\"pcanal\" class=\"form_input\" value=\"" + canal.get(i).getcanal()
                        + "\" > " + "<b>  Canal:  </b>" + canal.get(i).getcanal() + "<b>  Idioma:  </b>"
                        + canal.get(i).getidioma() + "<b>  Grupo:  </b>" + canal.get(i).getgrupo() + "<br>");
         }
         out.println("<input type=\"hidden\" name=\"p\" value=\"" + req.getParameter("p") + "\"/>");
         out.println("<input type=\"hidden\" name=\"panio\" value=\"" + req.getParameter("panio") + "\"/>");
         out.println("<input type=\"hidden\" name=\"pfase\" value=\"11\"/>");
         out.println(SEND_BUTTON + " onClick=\"document.forms[0].pfase.value='13'\"/>");
         out.println(BACK_BUTTON + " onClick=\"document.forms[0].pfase.value='11'\"/>");
         out.println(HOME_BUTTON);
         out.println(END_FORM + "</body>");
         out.println("</html>");

      } else { // Respuesta xml

         if (fecha == null) { // No hay parametro panio
            out.println("<wrongRequest>no param:panio</wrongRequest>");
            
         } else {
            out.println("<canales>");
            for (i = 0; i < canal.size(); i++) {

               out.println("<canal idioma=\"" + canal.get(i).getidioma() + "\" grupo=\"" + canal.get(i).getgrupo()
                     + "\">" + canal.get(i).getcanal() + "</canal>");
            }
            out.println("</canales>");
         }
      }

   }

   private void doGetFase13(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

      PrintWriter out = res.getWriter();
      String fecha = req.getParameter("panio");
      String canal = req.getParameter("pcanal");
      boolean auto = req.getParameter("auto") == null ? false : req.getParameter("auto").equals("si");
      int i;
      ArrayList<Programa> programa = getC1Peliculas(fecha, canal);
      Collections.sort(programa, new Programa.sort_length());

      if (!auto) { // Respuesta html
         out.println("<body><h1>Servicio de información sobre canales TV</h1><br><br>");
         out.println("<h2>Consulta1: Fecha= " + fecha + "    Canal = " + canal + "</h2> ");
         out.println("<br><h3>Mostrar resultado:</h3> ");// Opción para mostrar la lista de años (fase 11)
         out.println(INI_FORM);
         for (i = 0; i < programa.size(); i++) {
            out.println((i + 1) + "<b> . Titulo:  </b>" + programa.get(i).getpelicula() + "<b>  Hora:  </b>"
                  + programa.get(i).gethora() + "<b>  Edad Minima:  </b>" + programa.get(i).getedad()
                  + "<b>  Resumen:  </b>" + programa.get(i).getcomentario() + "<br>");
         }
         out.println("<input type=\"hidden\" name=\"p\" value=\"" + req.getParameter("p") + "\"/>");
         out.println("<input type=\"hidden\" name=\"pfase\" value=\"11\"/>");

         out.println("<input type=\"hidden\" name=\"panio\" value=\"" + req.getParameter("panio") + "\"/><br><br>");
         out.println(BACK_BUTTON + " onClick=\"document.forms[0].pfase.value='12'\"/>");
         out.println(HOME_BUTTON);
         out.println(END_FORM + "</body>");
         out.println("</html>");

      } else { // Respuesta xml

         if (fecha == null) { // No hay parametro panio
            out.println("<wrongRequest>no param:panio</wrongRequest>");

         } else if (canal == null) { // No hay parametro pcanal
            out.println("<wrongRequest>no param:pcanal</wrongRequest>");
            
         } else {
            out.println("<peliculas>");
            for (i = 0; i < programa.size(); i++) {

               out.println("<pelicula edad=\"" + programa.get(i).getedad() + "\" hora=\"" + programa.get(i).gethora()
                     + "\" resumen=\"" + programa.get(i).getcomentario() + "\">" + programa.get(i).getpelicula()
                     + "</pelicula>");
            }
            out.println("</peliculas>");
         }
      }

   }

   private void TVML_browser(String fichero) {
      System.out.println("EMPEZAMOS");
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      ficherosTVML = new TreeMap<String, Document>(); // inicializa el árbol de ficheros IML...
      warnings = new TreeMap<String, ArrayList<String>>(); // inicializa el registro de warnings...
      errores = new TreeMap<String, ArrayList<String>>(); // ... el de errores...
      erroresFatales = new TreeMap<String, ArrayList<String>>(); // ... el de errores fatales
      filestoread = new ArrayList<String>();
      dbf.setValidating(true);
      dbf.setNamespaceAware(true);
      dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
      dbf.setAttribute(JAXP_SCHEMA_SOURCE, URL_XSD);

      try {
         leerDoc(dbf, fichero);
      } catch (ParserConfigurationException e1) {
         e1.printStackTrace();
      }

      // System.out.println("numero errores---->" + errores.size());
      // errores.forEach((f, e) -> {
      // System.out.println(f + " => " + e);
      // });
      // System.out.println("numero errores fatales---->" + erroresFatales.size());
      // erroresFatales.forEach((f, e) -> {
      // System.out.println(f + " => " + e);
      // });
      // System.out.println("numero warnings---->" + warnings.size());
      // warnings.forEach((f, e) -> {
      // System.out.println(f + " => " + e);
      // });

   }

   private void leerDoc(DocumentBuilderFactory factory, String fichero) throws ParserConfigurationException {

      DocumentBuilder dbuilder = factory.newDocumentBuilder();
      XMLErrorHandler errorHandler = new XMLErrorHandler(fichero); // se encarga de validar la sintaxis XML del
                                                                   // documento (no el schema!)
      dbuilder.setErrorHandler(errorHandler);
      Document doc = null;
      try {

         doc = dbuilder.parse(new InputSource(new FileInputStream(fichero))); // lee el fichero IML base,...
         // doc = dbuilder.parse(new InputSource(new URL(fichero).openStream())); // lee
         // el fichero IML base,...
      } catch (SAXException e1) {
         // Documento con malformación XML
         ArrayList<String> malformacion = new ArrayList<String>();
         malformacion.add(e1.getMessage());
         erroresFatales.put(fichero, malformacion);

      } catch (IOException e2) {
         // El fichero no existe, pasamos al siguiente fichero...
      }

      if (doc != null) {

         System.out.println("-------------------Documento leido------------------");

         if (errorHandler.isXMLCorrecto()) {
            ficherosTVML.put(fichero, doc);
            getTVMLUrl(fichero, doc, factory);
         }
      }

   }

   class XMLErrorHandler extends DefaultHandler {

      private String nombreFichero;
      private boolean correcto = true;

      XMLErrorHandler(String ficheroIML) {
         this.nombreFichero = ficheroIML;
      }

      public void warning(SAXParseException spe) {
         if (warnings.get(nombreFichero) == null) {
            warnings.put(nombreFichero, new ArrayList<String>());
         }
         warnings.get(nombreFichero).add(spe.getMessage());
         correcto = false;
      }

      public void error(SAXParseException spe) {
         if (errores.get(nombreFichero) == null) {
            errores.put(nombreFichero, new ArrayList<String>());
         }
         errores.get(nombreFichero).add(spe.getMessage());
         correcto = false;
      }

      public void fatalError(SAXParseException spe) {
         if (erroresFatales.get(nombreFichero) == null) {
            erroresFatales.put(nombreFichero, new ArrayList<String>());
         }
         erroresFatales.get(nombreFichero).add(spe.getMessage());
         correcto = false;
      }

      public boolean isXMLCorrecto() {
         return correcto;
      }

   }

   private void getTVMLUrl(String fichero, Document doc, DocumentBuilderFactory factory)
         throws ParserConfigurationException {
      NodeList nl, nl2;
      Node n, n2;
      String[] url_parts = fichero.split("/");
      int x, y;
      Element el = doc.getDocumentElement();
      String url = "";

      for (x = 0; x < url_parts.length - 1; x++) {
         url = url.concat(url_parts[x]) + "/";
      }

      nl = el.getElementsByTagName("TVML");
      for (x = 0; x < nl.getLength(); x++) {
         n = (Node) nl.item(x);
         String next_tvml = n.getTextContent().trim();
         String url_completa = next_tvml.startsWith("http://") ? next_tvml : url + next_tvml;

         if (!filestoread.contains(url_completa)) {
            // System.out.println(url_completa);
            filestoread.add(url_completa);
            leerDoc(factory, url_completa);
         }

      }
      return;
   }

   private ArrayList<String> getC1Fechas() {

      ArrayList<String> fechas = new ArrayList<String>();

      ficherosTVML.forEach((name, document) -> {
         Element el = document.getDocumentElement();
         NodeList nl = el.getElementsByTagName("Fecha");
         Node n = (Node) nl.item(0);
         System.out.println("FECHA-->" + n.getTextContent().trim());
         fechas.add(n.getTextContent());
      });

      return fechas;

   }

   private ArrayList<Canal> getC1Canales(String fecha) {

      ArrayList<Canal> channel_list = new ArrayList<Canal>();

      ficherosTVML.forEach((name, document) -> {
         Element el = document.getDocumentElement();
         NodeList nl = el.getElementsByTagName("Fecha");
         Node n = (Node) nl.item(0);

         if (n.getTextContent().equals(fecha)) {

            NodeList channel_nl = el.getElementsByTagName("Canal");
            int x;
            for (x = 0; x < channel_nl.getLength(); x++) {
               int i;
               Node channel_nit = (Node) channel_nl.item(x);
               NodeList channel_nodes = channel_nit.getChildNodes();
               Canal c_aux = new Canal();
               for (i = 0; i < channel_nodes.getLength(); i++) {

                  String parameter = channel_nodes.item(i).getNodeName();
                  if (parameter.equals("NombreCanal"))
                     c_aux.setcanal(channel_nodes.item(i).getTextContent());
                  if (parameter.equals("Grupo"))
                     c_aux.setgrupo(channel_nodes.item(i).getTextContent());

               }

               NamedNodeMap channel_attributes = channel_nit.getAttributes();

               for (i = 0; i < channel_attributes.getLength(); i++) {
                  String attribute = channel_attributes.item(i).getNodeName();
                  if (attribute.equals("lang"))
                     c_aux.setidioma(channel_attributes.item(i).getTextContent());
               }

               channel_list.add(c_aux);

            }

         }
      });

      return channel_list;

   }

   private ArrayList<Programa> getC1Peliculas(String fecha, String canal) {

      ArrayList<Programa> program_list = new ArrayList<Programa>();
      XPath xpath = XPathFactory.newInstance().newXPath(); // Usaremos XPath para obtener las canciones de un disco

      ficherosTVML.forEach((name, document) -> {
         Element el = document.getDocumentElement();
         NodeList nl = el.getElementsByTagName("Fecha");
         Node n = (Node) nl.item(0);

         if (n.getTextContent().equals(fecha)) { // Buscamos dentro de los archivos los que coincidan con la fecha
            int i;
            NodeList program_nodes = null;
            try {
               program_nodes = (NodeList) xpath.evaluate(
                     "//Canal[NombreCanal =\"" + canal + "\"]/Programa[Categoria=\"Cine\"]",
                     document.getDocumentElement(), XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
               // Expresión XPath incorrecta (no debería darse nunca este error)
               e.printStackTrace();
               return;
            }

            for (i = 0; i < program_nodes.getLength(); i++) {
               Programa p_aux = new Programa();
               Node program = (Node) program_nodes.item(i);
               NodeList p_elems = program.getChildNodes();
               int j;
               for (j = 0; j < p_elems.getLength(); j++) {
                  if (p_elems.item(j).getNodeName().equals("HoraInicio"))
                     p_aux.sethora(p_elems.item(j).getTextContent());
                  if (p_elems.item(j).getNodeName().equals("NombrePrograma"))
                     p_aux.setpelicula(p_elems.item(j).getTextContent());
                  if (p_elems.item(j).getNodeType() == Node.TEXT_NODE) {
                     if (!p_elems.item(j).getTextContent().isBlank()) // Si no es una cadena en blanco lo guardamos
                        p_aux.setcomentario(p_elems.item(j).getTextContent());
                  }
               }

               NamedNodeMap p_attr = program.getAttributes();
               for (j = 0; j < p_attr.getLength(); j++) {
                  if (p_attr.item(j).getNodeName().equals("edadminima"))
                     p_aux.setedad(p_attr.item(j).getTextContent());
               }

               program_list.add(p_aux);
               System.out.println(p_aux.getpelicula() + "         " + p_aux.gethora());

            }

         }
      });

      return program_list;

   }

}
