import java.io.BufferedReader;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

public class Nodes {
	static int PORT = 9999;
	static int BUFFER_SIZE = 256;
	
	public static void main(String[] args) throws Exception{
//		JSONParser parser = new JSONParser();
		ArrayList<InetAddress> inetAddress = new ArrayList<InetAddress>();
		ArrayList<Integer> ports = new ArrayList<Integer>();

//		Żeby numer portu był zgodny z ogólną listą
		List<List<String>> nodes = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader("nodes.csv"))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				nodes.add(Arrays.asList(values));
			}
		}

		int Port;
		if(args.length >0){
			Port = Integer.parseInt(nodes.get(Integer.parseInt(args[0])).get(1));
		}
		else{
			Scanner sc = new Scanner(System.in);
			for(int i=0;i<nodes.size();i++)
			{
				System.out.println(i + " Ip adres: " + (nodes.get(i)).get(0) + " port: " + (nodes.get(i)).get(1));
			}
			System.out.println("Wybierz adres węzła:");
			int n = Integer.parseInt(sc.nextLine());

			Port = Integer.parseInt(nodes.get(n).get(1));
			System.out.println(nodes.get(n));
		}

        DatagramSocket datagramSocket = new DatagramSocket(Port);

        byte[] byteResponse;

        while (true){

            DatagramPacket receivedPacket = new DatagramPacket( new byte[BUFFER_SIZE], BUFFER_SIZE);
            datagramSocket.receive(receivedPacket);
            int length = receivedPacket.getLength(); 
            
            String messageReceived = new String(receivedPacket.getData(), 0, length, "utf8");



//            Object obj = parser.parse(messageReceived);
// 			JSONArray array = (JSONArray)obj;
//            JSONObject obj2 = (JSONObject)array.get(1);
			JSONObject obj2 = new JSONObject(messageReceived);


            
            if( (boolean) obj2.get("forward") ) {
				inetAddress.add( receivedPacket.getAddress() );
				ports.add( receivedPacket.getPort() );
//	            ArrayList<InetAddress> addressesList;
//	            addressesList = (ArrayList<InetAddress>)obj2.get("addresses");
//	            InetAddress address = (InetAddress)obj2.get("address");
//	            int port = (int)obj2.get("port");

				InetAddress address;
	            int port;
	            if(!"".equals((String) obj2.get("inetAddress"))) {
					address = InetAddress.getByName((String) obj2.get("inetAddress"));
					port = (int) obj2.get("port");
//	            	InetAddress nextAddress = addressesList.remove(0);
//	            	obj2.put("address", nextAddress);
//	                obj2.put("addresses", addressesList);
//	                array.set(1, obj2);
//	                Object json = (Object)array;
					Object json = (Object)obj2.get("message");
	                byteResponse = json.toString().getBytes("utf8");
	                DatagramPacket response = new DatagramPacket(byteResponse, byteResponse.length, address, port);
	                datagramSocket.send(response);
	                System.out.println("Przekierowałem wiadomość");
	            }else {
					System.out.println("Wiadomość od: " + receivedPacket.getAddress().toString() +
							" port: " + receivedPacket.getPort() +
							" o treści: " + obj2.get("message"));
//	            	System.out.print(obj2.get("message"));
	            	obj2.put("message", "odebrano");
	            	obj2.put("forward", false);
//	            	JObject json = (Object)array;
	            	address = inetAddress.remove(inetAddress.size()-1);
	 	            port = ports.remove(ports.size()-1);
	                byteResponse = obj2.toString().getBytes("utf8");
	                DatagramPacket response = new DatagramPacket(byteResponse, byteResponse.length, address, port);
	                datagramSocket.send(response);
	                System.out.println("Wysyłam odpowiedź");
	            }
            }else {
//            	Object json = (Object)array;
				System.out.println("Otrzymano odpowiedź");
            	InetAddress address = inetAddress.remove(inetAddress.size()-1);
 	            int port = ports.remove(ports.size()-1);
//                byteResponse = json.toString().getBytes("utf8");
                DatagramPacket response = new DatagramPacket(receivedPacket.getData(), length, address, port);
                datagramSocket.send(response);
				System.out.println("Przekierowano odpowiedź");
            }
            
            Thread.sleep(1000); 
        }
    }
}

