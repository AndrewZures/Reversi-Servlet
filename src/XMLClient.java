import java.net.*;
import java.io.*;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import java.io.StringWriter;

public class XMLClient {

        private static int id = 200;
        private static String color = "white";
/*
        public static void main(String[] args) throws Exception{
            Scanner keyInput = new Scanner(System.in);
            boolean gameOver = false;
            while (!gameOver){
                System.out.print(">>");
                int move    = Integer.parseInt(keyInput.next());
                String xmlMove = marshalMoveToXML(color, move);
                //send xml to servlet
                System.out.println("sending xml: "+xmlMove);
                System.out.println("sending input XML to server");
                String reply = postToServlet(xmlMove);
                System.out.println(reply);
                //Get Response
            }
        }
        */

        /*
          take a move int and create corresponding XML representation to
          send to server.ex
          <move id="100">
             <location>22</location>
             <color>white</color>
          </move>
        */

    /*
            <request type="move">   //or new_player //update
                <location>22</location>
                <player>player1</location>
            </request>


            <request type="update">

            <request type="new_player">
     */
        public String marshalMoveToXML(String playerString, String moveString) throws Exception{
            //obviously this first part should be done once per game, not for each move
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element root = document.createElement("request");
            document.appendChild(root);

            root.setAttribute("type", "move");

            Element location   = document.createElement("location");
            Element player = document.createElement("player");
            root.appendChild(location);
            root.appendChild(player);

            Text text = document.createTextNode(moveString);
            location.appendChild(text);

            text = document.createTextNode(playerString);
            player.appendChild(text);
            //now that I have a DOM representation of the requested move,
            //convert the DOM into a XML String to be sent to server
            StringWriter sw = new StringWriter();
            TransformerFactory tFactory =
                    TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(sw);
            transformer.transform(source, result);
            return sw.toString();
        }

        public String postToServlet(String xmlMove) throws Exception{
            URL url = new URL("http://tomcat-cspp.cs.uchicago.edu:8180/my_test_servlet/Servlet");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml");
            //conn.setRequestProperty("Content-Length", "" +  8);
            conn.setRequestProperty("Content-Language", "en-US");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            PrintWriter pw = new PrintWriter(conn.getOutputStream());
            pw.write(xmlMove);
            pw.close();

            Scanner in = new Scanner(conn.getInputStream());
            in.useDelimiter("");
            StringBuffer retVal = new StringBuffer();
            while (in.hasNext())
                retVal.append(in.next());

            conn.disconnect();

            return (retVal.toString());

        }

}
