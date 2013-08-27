import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.StringWriter;
import java.util.Scanner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.DOMException;

//a proof-of-concept servlet to play the role of reversi server
//based on XML tunneled over http
public class Servlet extends HttpServlet {
    //this method is called once when the servlet is first loaded
    //board object here
    ReversiBoard board;


    public void init(){
        //initialize board
        board = new ReversiBoard();

    }

    public void doPost(HttpServletRequest req,
                       HttpServletResponse res)
            throws IOException, ServletException {
        //first goal is to be able to receive and process
        //a well formatted XML move piggybacked on a POST

        //this gives me a raw stream to payload of Post request
        Scanner in = new Scanner(req.getInputStream());
        in.useDelimiter("");
        StringBuffer input = new StringBuffer();
        //peel off XML message a line at a time
        while (in.hasNext())
            input.append(in.next());
        //convert to String for convenience
        String inputAsString = input.toString();

        //parse the XML and marshal the java Move object

        //now create the response
        PrintWriter out = res.getWriter();

        //at this pont we want to return an XML document
        //that represents the move "response" to one or both
        //clients
      /*
        <moveResponse>
          <status>confimed</status>
          <mover> player ID here </mover>
               <loc> loc value here </loc>
             </moveResponse>
      */
        //A good first test is to just veryify that you can
        //successfully send back any XML string. From there
        //building up simple response is trivial and can be
        //done at the level of String building or, if you prefer,
        //DOM objects converted to Strings

        //test xml just to show that we can do it.
        //no significance to this move definition. just mechanics.


        try {
            if(board != null && board.getBoardString() != null){
                //String boardString = board.getBoardString();
                //out.print(boardString);
                out.println(Servlet.marshalUpdate(board));
                //out.println("WTF");
            }  else out.println("board is broken");

        } catch(Exception e){
            out.println("try failed");
        }
        out.flush();
        out.close();
    }

    public static final String marshalUpdate(ReversiBoard board) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            return "builder didn't work";
        }
        Document document = builder.newDocument();
        Element root = document.createElement("response");
        document.appendChild(root);
        root.setAttribute("type", "update");


        Element boardElement = document.createElement("board");
        String thisBoard = board.getBoardString();
        thisBoard = "testing";
        Text text = document.createTextNode(thisBoard);
        boardElement.appendChild(text);
        StringWriter sw = new StringWriter();
        Transformer transformer = null;
        /*
        try{
            TransformerFactory tFactory = TransformerFactory.newInstance();
            transformer = tFactory.newTransformer();
        } catch (Exception e) {
            return "transformer configuration didn't work";
        }
        */
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(sw);
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            return "transformer transform didnt' work";
        }
        return "finished";

    }

    public void doGet(HttpServletRequest req,
                      HttpServletResponse res)
            throws IOException, ServletException {
        //first goal is to be able to receive and process
        //a well formatted XML move piggybacked on a POST

        //this gives me a raw stream to payload of Post request
        Scanner in = new Scanner(req.getInputStream());
        in.useDelimiter("");
        StringBuffer input = new StringBuffer();
        //peel off XML message a line at a time
        while (in.hasNext())
            input.append(in.next());
        //convert to String for convenience
        String inputAsString = input.toString();

        //parse the XML and marshal the java Move object
        Move move = processInput(inputAsString);

        //now create the response
        PrintWriter out = res.getWriter();

        //at this pont we want to return an XML document
        //that represents the move "response" to one or both
        //clients
      /*
        <moveResponse>
          <status>confimed</status>
          <mover> player ID here </mover>
               <loc> loc value here </loc>
             </moveResponse>
      */
        //A good first test is to just veryify that you can
        //successfully send back any XML string. From there
        //building up simple response is trivial and can be
        //done at the level of String building or, if you prefer,
        //DOM objects converted to Strings

        //test xml just to show that we can do it.
        //no significance to this move definition. just mechanics.
        out.println("<move> <location>" + "hello" +
                "</location> </move>");
        out.flush();
        out.close();
    }

    private Move processInput(String xmlString) {
        //goal is to convert XML representation, embedded
        //in xmlString, to instance of move object. Ideally
        //this could be done auto-magically, but these technologies
        //are heavyweight and not particularly robust. A nice
        //compromise is to use a generic tree parsing methodology.
        //such exists for XML -- it is called DOM. DOM is mapped
        //to many languages and is robust and simple (if a bit
        //of a hack). JDOM is superior but not as uniformly adopted.

        //first goal is to yank these values out of XML with minimal effort
        String moveLocation;
        String color;
        String requestType;

        //parse XML into DOM tree
        //getting parsers is longwinded but straightforward
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            //once you have parse just call parse. to parse a string vs.
            //a File requires using an InputSource. In any case result is
            //a DOM tree -- ie an instance of org.w3c.dom.Document
            Document document
                    = builder.parse(new InputSource(new StringReader(xmlString)));
            //must invoke XML validator here from java. It is a major advantage
            //of using XML

            //assuming validated we continue to parse the data ...

            //always start by getting root element
            Element root = document.getDocumentElement();
            //get the value of the id attribute
            requestType = root.getAttribute("type");
            if(requestType.equalsIgnoreCase("move")){
            Element locElement =
                    (Element) document.getElementsByTagName("location").item(0);
            Element playerElement =
                    (Element) document.getElementsByTagName("player").item(0);
            moveLocation = locElement.getFirstChild().getNodeValue();
            color = playerElement.getFirstChild().getNodeValue();
            Move move = new Move(color, moveLocation);
                return move;
            } else return new Move("not finding move", "blah");
        }
        catch (Exception e) {
            System.out.print(e);
            return new Move("try isn't working", "stuff");
        }
    }
}

