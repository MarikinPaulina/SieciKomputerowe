import java.io.BufferedReader;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

public class Nodes {
//	static int PORT = 9999;
	static int BUFFER_SIZE = 256;
	
	public static void main(String[] args) throws Exception{
		JSONParser parser = new JSONParser();
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
		int Port = Integer.parseInt(nodes.get(Integer.parseInt(args[0])).get(1));

//        DatagramSocket datagramSocket = new DatagramSocket(PORT);
		DatagramSocket datagramSocket = new DatagramSocket(Port);

        byte[] byteResponse;

        while (true){

            DatagramPacket receivedPacket = new DatagramPacket( new byte[BUFFER_SIZE], BUFFER_SIZE);

            datagramSocket.receive(receivedPacket);

            int length = receivedPacket.getLength(); 
            
            String messageReceived = new String(receivedPacket.getData(), 0, length, "utf8");
            inetAddress.add( receivedPacket.getAddress() );
            ports.add( receivedPacket.getPort() );


            Object obj = parser.parse(messageReceived);
            JSONArray array = (JSONArray)obj;
            JSONObject obj2 = (JSONObject)array.get(1);
            
            if( (boolean) array.get(0) ) {
//	            ArrayList<InetAddress> addressesList;
//	            addressesList = (ArrayList<InetAddress>)obj2.get("addresses");
//	            InetAddress address = (InetAddress)obj2.get("address");
//	            int port = (int)obj2.get("port");

	            InetAddress address = (InetAddress) obj2.get("address");
	            int port = (int) obj2.get("port");
	            if(address != null) {
//	            	InetAddress nextAddress = addressesList.remove(0);
//	            	obj2.put("address", nextAddress);
//	                obj2.put("addresses", addressesList);
//	                array.set(1, obj2);
//	                Object json = (Object)array;
					Object json = (Object)obj2.get("message");
	                byteResponse = json.toString().getBytes("utf8");
	                DatagramPacket response = new DatagramPacket(byteResponse, byteResponse.length, address, port);
	                datagramSocket.send(response);
	            }else {
					System.out.print("Wiadomość od: " + receivedPacket.getAddress() +
							" port: " + receivedPacket.getPort() +
							" o treści: " + obj2.get("message"));
//	            	System.out.print(obj2.get("message"));
	            	obj2.put("message", "odebrano");
	            	array.set(0, false);
	            	array.set(1, obj2);
	            	Object json = (Object)array;
	            	address = inetAddress.remove(inetAddress.size()-1);
	 	            port = ports.remove(ports.size()-1);
	                byteResponse = json.toString().getBytes("utf8");
	                DatagramPacket response = new DatagramPacket(byteResponse, byteResponse.length, address, port);
	                datagramSocket.send(response);
	            }
            }else {
            	Object json = (Object)array;
            	InetAddress address = inetAddress.remove(inetAddress.size()-1);
 	            int port = ports.remove(ports.size()-1);
                byteResponse = json.toString().getBytes("utf8");
                DatagramPacket response = new DatagramPacket(byteResponse, byteResponse.length, address, port);
                datagramSocket.send(response);
            }
            
            Thread.sleep(1000); 
        }
    }
}

