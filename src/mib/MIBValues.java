package mib;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.TextArea;
import java.io.IOException;
import java.util.*;

import javax.swing.*;

import com.ireasoning.protocol.*;
import com.ireasoning.protocol.snmp.*;
import gui.*;

public class MIBValues {
	private static String[] OIDS = new String[] {
		".1.3.6.1.4.1.9.9.48.1.1.1.5.1", //pool used 1
		".1.3.6.1.4.1.9.9.48.1.1.1.5.2", //pool used 2
		".1.3.6.1.4.1.9.9.48.1.1.1.6.1", //pool free 1
		".1.3.6.1.4.1.9.9.48.1.1.1.6.2", //pool free 2
		
		//".1.3.6.1.4.1.9.9.48.1.1.1.2.1", //pool name 1
		//".1.3.6.1.4.1.9.9.48.1.1.1.2.2", //pool name 2
		
		".1.3.6.1.4.1.9.9.109.1.1.1.1.3.1", //5 seconds
		".1.3.6.1.4.1.9.9.109.1.1.1.1.4.1", //1 minute
		".1.3.6.1.4.1.9.9.109.1.1.1.1.5.1"  //5 minute
	};
	private static String comunity = "si2019";
	private static int port = 161;
	private static int version = SnmpConst.SNMPV2;
	private static List<Integer>time = new ArrayList<>(); //array for time
	
	private boolean isCreated = false;
	private int addTime;
	private JFrame f = new JFrame();
	
	private List<String> val = new ArrayList<>();
	private List<Integer> poolUsed1 = new ArrayList<>();
	private List<Integer> poolUsed2 = new ArrayList<>();
	private List<Integer> poolFree1 = new ArrayList<>();
	private List<Integer> poolFree2 = new ArrayList<>();
	
	private List<Integer> cpu5sec = new ArrayList<>();
	private List<Integer> cpu1min = new ArrayList<>();
	private List<Integer> cpu5min = new ArrayList<>();
	
	private String ip;
	private String router;
	private int interval;
	private SnmpSession session;
	private SnmpPoller poller;
	
	public MIBValues(String addr, String r, int i, int s1, int s2) { 
		ip = addr; router = r; addTime = interval = i; 
		
		getMIBValues();
		
		f.setTitle(router);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(600, 600);
		f.setLocation(s1, s2);
		f.setVisible(true);
	}
	
	private void handle(Object sender, Msg msg) {
		if (msg.getType() != Msg.ERROR_TYPE) {
			SnmpPdu pdu = (SnmpPdu)msg;
			
			SnmpVarBind[] binds = pdu.getVarBinds();
			for (int i = 0; i < binds.length; i++) {
				val.add(binds[i].getValue().toString());
			}
			
			poolUsed1.add(Integer.parseInt(val.get(0)));
			poolUsed2.add(Integer.parseInt(val.get(1)));
			
			poolFree1.add(Integer.parseInt(val.get(2)));
			poolFree2.add(Integer.parseInt(val.get(3)));
			
			cpu5sec.add(Integer.parseInt(val.get(4)));
			cpu1min.add(Integer.parseInt(val.get(5)));
			cpu5min.add(Integer.parseInt(val.get(6)));
			
			ArrayList<List<Integer>> matrix = new ArrayList<List<Integer>>();
			matrix.add(poolUsed1);
			matrix.add(poolUsed2);
			matrix.add(poolFree1);
			matrix.add(poolFree2);
			matrix.add(cpu5sec);
			matrix.add(cpu1min);
			matrix.add(cpu5min);
			
			time.add(addTime); addTime += interval;
			
			val.clear();
			
			if (!isCreated) {
				f.add(new Graphs(matrix, time));
				isCreated = true;
			}
			else
				f.repaint();
		} else { System.out.println("Msg type: ERROR_TYPE"); }
	}
	
	public void getMIBValues() {
		try {
			session = new SnmpSession(ip, port, comunity, comunity, version);
			poller = new SnmpPoller(session);
			poller.addListener((sender, msg)->{handle(sender,msg);});
			poller.snmpGetPoll(OIDS, interval);
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	public static void main(String[] args) {
		JFrame f = new JFrame();
		JPanel p = new JPanel();
		
		JLabel label1 = new JLabel("Input interval: ");
	    p.add(label1, BorderLayout.PAGE_START);
		
		JButton button1 = new JButton("Add");
		p.add(button1, BorderLayout.PAGE_END);
		
		TextArea area1 = new TextArea();
		area1.setSize(25,25);
		p.add(area1, BorderLayout.LINE_END);
		
		f.add(p);
		f.setSize(700,225);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		
		button1.addActionListener((e)->{
			String intervalStr = area1.getText().toString();
			int interval = Integer.parseInt(intervalStr);
			f.dispose();
			MIBValues mib1 = new MIBValues("192.168.10.1", "router1", interval, 100, 200);
			MIBValues mib2 = new MIBValues("192.168.20.1", "router2", interval, 713, 200);
			MIBValues mib3 = new MIBValues("192.168.30.1", "router3", interval, 1500, 200);
		});
	}
}
