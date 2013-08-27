import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

// your first servlet. servlets work by implement a
// special class (HttpServlet) and overriding the key
// "lifecycle methods"

//this method is invoked when the web server receives
//an http GET command that specifies HelloServlet as its
//target (I'll show how to do that on client end)
public class HelloServlet extends HttpServlet{
    public void doGet(HttpServletRequest req,
		      HttpServletResponse res)
	throws IOException, ServletException{
	//note that since I'm only printing hello world
	//back to client I'll ignore req in this example
	//main role of res is to get outputstream to write
	//back to client. For this we use getWriter();
	PrintWriter out = res.getWriter();
	out.println("<html> <body> Hello World </body> </html>");
	out.flush();
	out.close();
	
    }
    
    
    //this method is invoked when the web server receives
    //an http POST command that specifies HelloServlet as its
    //target (I'll show how to do that on client end)
    public void doPost(HttpServletRequest req,
		       HttpServletResponse res)
	throws IOException, ServletException{
	doGet(req,res); //this is appropriate if post and get
	                //should respond identically
    }
}