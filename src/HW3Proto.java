import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Scanner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

//a proof-of-concept servlet to play the role of reversi server
//based on XML tunneled over http
public class HW3Proto extends HttpServlet {
   //this method is called once when the servlet is first loaded
   //board object here
   public void init(){
      //initialize board

   }

   public void doPost(HttpServletRequest req,
                      HttpServletResponse res)
                      throws IOException, ServletException {
      //first goal is to be able to receive and process
      //a well formatted XML moveTwo piggybacked on a POST

      //this gives me a raw stream to payload of Post request
      Scanner in = new Scanner(req.getInputStream());
      StringBuffer input = new StringBuffer();
      //peel off XML message a line at a time
      while (in.hasNext())
          input.append(in.next());
      //convert to String for convenience
      String inputAsString = input.toString();

      //parse the XML and marshal the java Move_Two object
      Move_Two moveTwo = processInput(inputAsString);
      
      //now create the response
      PrintWriter out = res.getWriter();

      //at this pont we want to return an XML document
      //that represents the moveTwo "response" to one or both
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
      //no significance to this moveTwo definition. just mechanics.
      out.println("<moveTwo> <location> " + moveTwo.getLocation() +
             "</location> </moveTwo>");
      out.flush();
      out.close();
   }

   public void doGet(HttpServletRequest req,
                      HttpServletResponse res)
                      throws IOException, ServletException {
      //first goal is to be able to receive and process
      //a well formatted XML moveTwo piggybacked on a POST

      //this gives me a raw stream to payload of Post request
      Scanner in = new Scanner(req.getInputStream());
      StringBuffer input = new StringBuffer();
      //peel off XML message a line at a time
      while (in.hasNext())
          input.append(in.next());
      //convert to String for convenience
      String inputAsString = input.toString();

      //parse the XML and marshal the java Move_Two object
      Move_Two moveTwo = processInput(inputAsString);
      
      //now create the response
      PrintWriter out = res.getWriter();

      //at this pont we want to return an XML document
      //that represents the moveTwo "response" to one or both
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
      //no significance to this moveTwo definition. just mechanics.
      out.println("<moveTwo> <location> " + moveTwo.getLocation() +
             "</location> </moveTwo>");
      out.flush();
      out.close();
   }

   private static Move_Two processInput(String xmlString) {
      //goal is to convert XML representation, embedded
      //in xmlString, to instance of move object. Ideally
      //this could be done auto-magically, but these technologies
      //are heavyweight and not particularly robust. A nice 
      //compromise is to use a generic tree parsing methodology.
      //such exists for XML -- it is called DOM. DOM is mapped
      //to many languages and is robust and simple (if a bit
      //of a hack). JDOM is superior but not as uniformly adopted.

      //first goal is to yank these values out of XML with minimal effort
      int moveLocation;
      String color;
      int playerID;

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
         playerID = Integer.parseInt(root.getAttribute("id"));
         Element locElement = 
             (Element) document.getElementsByTagName("location").item(0);
         Element colorElement = 
             (Element) document.getElementsByTagName("color").item(0);
         moveLocation = 
             Integer.parseInt(locElement.getFirstChild().getNodeValue());
         color = colorElement.getFirstChild().getNodeValue();
         Move_Two moveTwo = new Move_Two(playerID,color,moveLocation);
         return moveTwo;
      }
      catch (Exception e) {
         System.out.print(e);
         return null;
      }
   }
}

class Move_Two {
   private int id;
   private String color;
   private int location;

   Move_Two(int id, String color, int loc) {
      this.id = id;
      this.color = color;
      this.location = loc;
   }

   public int getID() {
      return this.id;
   }

   public int getLocation() {
      return this.location;
   }

   public String getColor() {
      return this.color;
   }
}