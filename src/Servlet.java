import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Scanner;

//a proof-of-concept servlet to play the role of reversi server
//based on XML tunneled over http
public class Servlet extends HttpServlet {
    //this method is called once when the servlet is first loaded
    //board object here
    ReversiBoard board;
    DocumentBuilderFactory factory;
    DocumentBuilder builder;


    public void init() {
        //initialize board
        board = new ReversiBoard();
        factory = DocumentBuilderFactory.newInstance();
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
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
            if (board != null && board.getBoardString() != null) {
                //String boardString = board.getBoardString();
                //out.print(boardString);
                out.println(processInput(inputAsString));
                //out.println("WTF");
            } else out.println("board is broken");

        } catch (Exception e) {
            out.println("try failed updated");
        }
        out.flush();
        out.close();
    }

    public String marshallConnect(){
        Document document = builder.newDocument();
        Element root = document.createElement("response");
        document.appendChild(root);
        root.setAttribute("type", "connection_received");
        Element playerID = document.createElement("player_id");
        Text text = document.createTextNode(board.getNewPlayer());
        playerID.appendChild(text);
        root.appendChild(playerID);
        document = this.buildPlayerTurn(document, root, board);
        return this.transformDocumentToString(document);
    }



    public String marshalUpdate(ReversiBoard board, boolean update) {
        Document document = builder.newDocument();
        Element root = document.createElement("response");
        document.appendChild(root);
        root.setAttribute("type", "update");

        //build specific portions of response
        document = buildPlayerTurn(document, root, board);
        document = buildUpdateStatus(document, root, board, update);
        document = buildScore(document, root, board);
        document = buildValidMovesList(document, root, board);
        document = buildBoard(document, root, board);
        return this.transformDocumentToString(document);

    }

    public String transformDocumentToString(Document document) {
        StringWriter sw = new StringWriter();
        TransformerFactory tFactory =
                TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = tFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(sw);
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return sw.toString();
    }

    public Document buildBoard(Document document, Element root, ReversiBoard board) {
        Element boardElement = document.createElement("board");
        for (int i = 0; i < board.getBoard().length; i++) {
            Element position = document.createElement("position_" + i);
            Text text = document.createTextNode(String.valueOf(board.getBoard()[i]));
            position.appendChild(text);
            boardElement.appendChild(position);
        }
        root.appendChild(boardElement);
        return document;
    }

    public Document buildValidMovesList(Document document, Element root, ReversiBoard board) {
        Element validMovesElement = document.createElement("valid_moves");
        for (int i = 0; i < board.getValidMoves(board.getCurrentPlayerNum()).size(); i++) {
            Element position = document.createElement("move_" + i);
            Text text = document.createTextNode(String.valueOf(board.getValidMoves(board.getCurrentPlayerNum()).get(i)));
            position.appendChild(text);
            validMovesElement.appendChild(position);
        }
        root.appendChild(validMovesElement);
        return document;
    }

    public Document buildPlayerTurn(Document document, Element root, ReversiBoard board) {
        Element currentPlayerElement = document.createElement("player_turn");
        Text text = document.createTextNode(board.getCurrentPlayer());
        currentPlayerElement.appendChild(text);
        root.appendChild(currentPlayerElement);
        return document;
    }


    public Document buildScore(Document document, Element root, ReversiBoard board){
        Element playerScores = document.createElement("scores");
        Element player1Score  =document.createElement("player1_score");
        Element player2Score = document.createElement("player2_score");
        Text player1Text = document.createTextNode(""+board.getScore(1));
        Text player2Text = document.createTextNode(""+board.getScore(2));
        player1Score.appendChild(player1Text);
        player2Score.appendChild(player2Text);
        playerScores.appendChild(player1Score);
        playerScores.appendChild(player2Score);
        root.appendChild(playerScores);
        return document;

    }

    public Document buildUpdateStatus(Document document, Element root, ReversiBoard board, boolean update){
        Element currentPlayerElement = document.createElement("update_status");
        String updateString;
        if(update) updateString = "completed";
        else updateString = "failed";
        Text text = document.createTextNode(updateString);
        currentPlayerElement.appendChild(text);
        root.appendChild(currentPlayerElement);
        return document;
    }


    private String processInput(String xmlString) {
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
            if (requestType.equalsIgnoreCase("move")) {
                String moveLocation;
                String player;
                Element locElement =
                        (Element) document.getElementsByTagName("location").item(0);
                Element playerElement =
                        (Element) document.getElementsByTagName("player").item(0);
                moveLocation = locElement.getFirstChild().getNodeValue();
                player = playerElement.getFirstChild().getNodeValue();
                //return moveLocation+" "+player;
                int moveInt = Integer.parseInt(moveLocation);
                boolean result = board.makeMove(moveInt, player);
                return marshalUpdate(board, result);
            }
            else if(requestType.equalsIgnoreCase("connect")) {
                return marshallConnect();
            }
            else return null;
        } catch (Exception e) {
            System.out.print(e);
            return null;
        }
    }



//    public void doGet(HttpServletRequest req,
//                      HttpServletResponse res)
//            throws IOException, ServletException {
//        //first goal is to be able to receive and process
//        //a well formatted XML move piggybacked on a POST
//
//        //this gives me a raw stream to payload of Post request
//        Scanner in = new Scanner(req.getInputStream());
//        in.useDelimiter("");
//        StringBuffer input = new StringBuffer();
//        //peel off XML message a line at a time
//        while (in.hasNext())
//            input.append(in.next());
//        //convert to String for convenience
//        String inputAsString = input.toString();
//
//        //parse the XML and marshal the java Move object
//        String result = processInput(inputAsString);
//
//        //now create the response
//        PrintWriter out = res.getWriter();
//
//        //at this pont we want to return an XML document
//        //that represents the move "response" to one or both
//        //clients
//      /*
//        <moveResponse>
//          <status>confimed</status>
//          <mover> player ID here </mover>
//               <loc> loc value here </loc>
//             </moveResponse>
//      */
//        //A good first test is to just veryify that you can
//        //successfully send back any XML string. From there
//        //building up simple response is trivial and can be
//        //done at the level of String building or, if you prefer,
//        //DOM objects converted to Strings
//
//        //test xml just to show that we can do it.
//        //no significance to this move definition. just mechanics.
//        out.println("<move> <location>" + "hello" +
//                "</location> </move>");
//        out.flush();
//        out.close();
//    }
}

