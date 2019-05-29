import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import org.json.JSONObject;


public class client {

    static int N = 2;
    static int BUFFER_SIZE = 256;

    public static void main(String[] args) throws Exception
    {
        //Wczytuje adresy z pliku
        List<List<String>> nodes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("nodes.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                nodes.add(Arrays.asList(values));
            }
        }
        Scanner sc = new Scanner(System.in);
        System.out.println("Aby wyjść wpisz 'end'");
        System.out.println("Podaj wiadomość:");
        while (true){
            String message = sc.nextLine();
            if(message.equals("end"))
            {
                break;
            }
            for(int i=0;i<nodes.size();i++)
            {
              System.out.println(i + " Ip adres: " + (nodes.get(i)).get(0) + " port: " + (nodes.get(i)).get(1));
            }
            System.out.println("Podaj adresata:");
            int n = Integer.parseInt(sc.nextLine());
            System.out.println(nodes.get(n));
            List<String > target = nodes.remove(n);
            Collections.shuffle(nodes);


            List<List<String>> track = nodes.subList(0,N);
            track.add(target);

            List<String> next = track.remove(0);

    //      Paczka z paczką z paczką...
            JSONObject obj = new JSONObject();
            obj.put("forward",true);
            obj.put("inetAddress", "");
            obj.put("port",0);
            obj.put("message", message);
            for(int i = track.size()-1; i>=0; i--)
            {
                JSONObject obj2 = new JSONObject();
                obj2.put("forward",true);
                obj2.put("inetAddress",track.get(i).get(0));
                obj2.put("port",Integer.parseInt((track.get(i)).get(1)));
                obj2.put("message",obj);
                obj = obj2;
            }
            byte[] send_data = obj.toString().getBytes();

            DatagramSocket socket = null;
            socket = new DatagramSocket();
            DatagramPacket sentPacket = new DatagramPacket(send_data, send_data.length);
            sentPacket.setAddress(InetAddress.getByName(next.get(0)));
            sentPacket.setPort(Integer.parseInt(next.get(1)));
            socket.send(sentPacket);
            System.out.println("Wiadomość wysłana do " +next.get(0) +next.get(1));

            DatagramPacket receivedPacket = new DatagramPacket( new byte[BUFFER_SIZE], BUFFER_SIZE);
            socket.receive(receivedPacket);
            int length = receivedPacket.getLength();

            String messageReceived = new String(receivedPacket.getData(), 0, length, "utf8");
            JSONObject obj2 = new JSONObject(messageReceived);
            System.out.println("Wiadomość od: " + receivedPacket.getAddress().toString() +
                    " port: " + receivedPacket.getPort() +
                    " o treści: " + obj2.get("message"));
    }}

}
