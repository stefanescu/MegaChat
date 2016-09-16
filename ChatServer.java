package megachat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ChatServer {

    private static final int PORT = 9001;

    private static HashSet<String> names = new HashSet<String>();

    private static HashMap<String, PrintWriter> writers = new HashMap<String, PrintWriter>();

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {

                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null || name.matches(".*\\s+.*")) {
                        continue;
                    }
                    synchronized (names) {
                        if (!names.contains(name)) {
                            names.add(name);
                            break;
                        }
                    }
                }

                out.println("NAMEACCEPTED");
                writers.put(name, out);

                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    } else if (input.startsWith("list")) {
                        out.println("LISTSTART");
                        for (String username : names)
                            out.println("USERLIST " + username);
                    } else if (input.startsWith("msg ")) {
                        String message = input.substring(4);
                        String[] tokens = message.split(" ");
                        String recieverName = tokens[0];
                        
                        if (!writers.containsKey(recieverName))
                            out.println("ERRMSGTARGET");
                        else {
                            PrintWriter recieverWriter = writers.get(recieverName);
                            message = String.join(" ", tokens);
                            message = message.substring(recieverName.length());
                            recieverWriter.println("MSG [" + name + "]: " + message);
                            out.println("SENT [To " + recieverName + "]: " + message);  
                        }
                    } else if (input.startsWith("bcast ")) {
                        input = input.substring(6);
                        Set set = writers.entrySet();
                        Iterator iterator = set.iterator();
                        
                        while(iterator.hasNext()) {
                            Map.Entry mentry = (Map.Entry)iterator.next();
                            PrintWriter writer = (PrintWriter) mentry.getValue();
                            writer.println("BROADCAST [" + name + "]: " + input);
                        }
                    } else if (input.startsWith("nick ")) {
                        String newName = input.substring(5);
                        if (newName.matches(".*\\s+.*"))
                            out.println("ERRNICKSPACE");
                        else if (names.contains(newName))
                            out.println("ERRNICKINUSE");
                        else {
                            names.remove(name);
                            name = newName;
                            names.add(name);
                            out.println("NICKCHANGED Nickname changed to " + name);
                        }
                    } else if (input.startsWith("quit")) {
                        out.println("QUIT");
                        return;
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                if (name != null) {
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(name);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}