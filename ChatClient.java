package megachat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatClient {

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("MegaChat");
    JTextArea messageArea = new JTextArea(8, 40);
    JTextField textField = new JTextField(40);

    public ChatClient() {

        messageArea.setEditable(false);
        textField.setEditable(false);
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        frame.getContentPane().add(textField, "South");
        frame.pack();

        textField.addActionListener(new ActionListener() {
  
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
    }


    private String getServerAddress() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    private String getName() {
        return JOptionPane.showInputDialog(
            frame,
            "Choose an user name:",
            "User name selection",
            JOptionPane.PLAIN_MESSAGE);
    }

    private void run() throws IOException {

        String serverAddress = getServerAddress();
        Socket socket = new Socket(serverAddress, 9001);
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        while (true) {
            String line = in.readLine();
            if (line.startsWith("SUBMITNAME")) {
                out.println(getName());
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
            } else if (line.startsWith("BROADCAST")) {
                messageArea.append("BROADCAST MESSAGE: \n");
                messageArea.append(line.substring(9) + "\n");
            } else if (line.startsWith("LISTSTART")) {
              messageArea.append("USERLIST: \n");  
            } else if (line.startsWith("USERLIST")) {
                messageArea.append(line.substring(9) + "\n");  
            } else if (line.startsWith("MSG")) {
                messageArea.append(line.substring(4) + "\n");
            } else if (line.startsWith("SENT")) {
                messageArea.append(line.substring(5) + "\n");
            } else if (line.startsWith("NICKCHANGED")) {
                messageArea.append(line.substring(12));
            } else if (line.startsWith("ERRNICKSPACE")) {
                messageArea.append("New nickname cannot contain spaces. \n");
            } else if (line.startsWith("ERRNICKINUSE")) {
                messageArea.append("New nickname already in use. \n");
            } else if (line.startsWith("ERRMSGTARGET")) {
                messageArea.append("No such user available. \n");
            } else if (line.startsWith("QUIT")) {
                return;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}