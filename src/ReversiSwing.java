import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ReversiSwing implements ActionListener{

        //Declaration of all calculator's components.
        JPanel windowContent, controlContent, pl;
        JLabel displayLabel;
        JButton buttons[];
        Icon blackIcon, blueIcon;
        XMLClient client;

        //Constructor creates the components in memory and adds the to the frame using combination of Borderlayout.
        public ReversiSwing() {
           client = new XMLClient();
           this.displayGUI();
        }

        public void displayGUI(){
            blackIcon = new ImageIcon(getClass().getResource("black_circle.png"));
            blueIcon = new ImageIcon(getClass().getResource("blue_circle.png"));


            windowContent= new JPanel();
            buttons = new JButton[64];


            // Set the layout manager for this panel
            BorderLayout bl = new BorderLayout();
            windowContent.setLayout(bl);

            //Create the display field and place it in the North area of the window
            displayLabel = new JLabel("Welcome To Reversi", SwingConstants.CENTER);
            displayLabel.setFont(new Font("Serif", Font.PLAIN, 30));
            windowContent.add("North", displayLabel);

            //Create button field and place it in the North area of the window
            for(int i = 0; i < buttons.length; i++) {
                JButton button = new JButton();
                buttons[i] = this.setButtonProperties(button, i);
            }


            //Create the panel with the GridLayout that will contain 12 buttons - 10 numeric ones, and button with the points
            //and the equal sign.
            pl = new JPanel ();
            pl.setPreferredSize(new Dimension(800, 800));
            GridLayout gl =new GridLayout(8,8);
            pl.setLayout(gl);
            //Add window controls to the panel pl.

            for(int i = 0; i < buttons.length; i++) {
                pl.add(buttons[i]);
            }

            controlContent = new JPanel();
            controlContent.setLayout(new BorderLayout());
            JLabel playerLabel = new JLabel("You Are Player X");
            playerLabel.setFont(new Font("Serif", Font.PLAIN, 22));
            JLabel turnLabel = new JLabel("Your Turn", SwingConstants.CENTER);
            turnLabel.setFont(new Font("Serif", Font.PLAIN, 22));
            JLabel scoreLabel = new JLabel("Score is X : 0");
            scoreLabel.setFont(new Font("Serif", Font.PLAIN, 22));
            controlContent.add("West", playerLabel);
            controlContent.add("Center", turnLabel);

            controlContent.add("East", scoreLabel);
            controlContent.setPreferredSize(new Dimension(800, 45));

            //Add the panel pl to the center area of the window
            windowContent.add("Center",pl);
            windowContent.add("South", controlContent);
            //Create the frame and set its content pane
            JFrame frame = new JFrame("Reversi");
            frame.setContentPane(windowContent);
            //set the size of the window to be big enough to accomodate all controls.
            frame.pack();
            //Finnaly, display the window
            frame.setVisible(true);
        }

        public static void main(String[] args) {
           ReversiSwing swing = new ReversiSwing();
        }


    public JButton setButtonProperties(JButton button, int buttonNum){
        button.addActionListener(this);
        button.setName(String.valueOf(buttonNum));
        button.setPreferredSize(new Dimension(80, 80));
        button.setMaximumSize(new Dimension(80, 80));
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String xmlString = "";
        //To change body of implemented methods use File | Settings | File Templates.
        for(int i = 0; i < buttons.length; i++){
            if(e.getSource() == buttons[i]){
                System.out.println("button is " + buttons[i].getName());
                try {
                    xmlString = client.marshalMoveToXML("black", buttons[i].getName());
                } catch (Exception e1) {
                    System.out.println("xmlString problem");
                }
                try {
                    String reply = client.postToServlet(xmlString);
                    System.out.println(">>"+reply);
                } catch (Exception e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }
}
