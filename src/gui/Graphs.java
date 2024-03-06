package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Graphs extends JPanel {
	private static Color[] colors = new Color[] {
			Color.BLUE,
			Color.CYAN,
			Color.DARK_GRAY,
			Color.MAGENTA,
			Color.RED,
			Color.ORANGE,		
			Color.GREEN
	};
	
	private ArrayList<List<Integer>>x;
	private List<Integer>y;
	
	private int move = 20;
	
	private int getMax(int a, int b, int c) {
		int max = -10000;
		
		for (int i = 0; i < x.size(); i++) {
			if (i != a && i != b && i != c)
				continue;
			List<Integer> mib = x.get(i);
			for(int j = 0; j < mib.size() - 1; j++) {
				if (mib.get(j + 1) - mib.get(j) > max) max = mib.get(j + 1) - mib.get(j);
			}	
		}

		return max;
	}
	
	private int getMin(int a, int b, int c) {
		int min = 10000;
		
		for (int i = 0; i < x.size(); i++) {
			if (i != a && i != b && i != c)
				continue;
			List<Integer> mib = x.get(i);
			for(int j = 0; j < mib.size() - 1; j++) {
				if (mib.get(j + 1) - mib.get(j) < min) min = mib.get(j + 1) - mib.get(j);
			}	
		}

		return min;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g1 = (Graphics2D)g;
		int w = getWidth();
		int h = getHeight();
		
		g1.draw(new Line2D.Double(move, move, move, h / 2 - move)); //y
		g1.draw(new Line2D.Double(move, h / 2 - move, w / 2 - move, h / 2 - move)); //x
		//g1.drawString("0", move - 2, h / 2 - move + 13);
		
		g1.draw(new Line2D.Double(w/2 + move, move, w /2 + move, h / 2 - move)); //y
		g1.draw(new Line2D.Double(w / 2 + move, h / 2 - move, w - move, h / 2 - move)); //x
		//g1.drawString("0", w/2 + move - 2, h / 2 - move + 13);
		
		g1.draw(new Line2D.Double(move, h / 2 + move, move, h - move)); //y
		g1.draw(new Line2D.Double(move, h - move, w / 2 - move, h - move)); //x
		//g1.drawString("0", move - 2, h - move + 13);
		
		g1.setColor(Color.BLACK);
		g1.setFont(new Font("Serif", Font.BOLD, 20));
		
		g1.drawString("CPU", 2 * move, move);
		g1.drawString("I/O", w/2 + 2 * move, move);
		g1.drawString("USAGE", 2 * move, h/2 + move/2);
		
		g1.setFont(new Font("Arial", Font.BOLD, 17));
		g1.setColor(Color.BLACK);
		g1.drawString("LEGEND", w * 1/2 + 2 * move, h/2 + 4 * move + 10);
			
		g1.setFont(new Font("Serif", Font.ITALIC, 15));
		
		g1.setColor(colors[0]);
		g1.fill(new Ellipse2D.Double(w * 1/2 + move, h/2 + 5 * move, 6, 6));
		g1.drawString("Pool used - CPU", w * 1/2 + 2 * move, h/2 + 5 * move + 10);
		
		g1.setColor(colors[2]);
		g1.fill(new Ellipse2D.Double(w * 1/2 + move, h/2 + 6 * move, 6, 6));
		g1.drawString("Pool free - CPU", w * 1/2 + 2 * move, h/2 + 6 * move + 10);
		
		g1.setColor(colors[1]);
		g1.fill(new Ellipse2D.Double(w * 1/2 + move, h/2 + 7 * move, 6, 6));
		g1.drawString("Pool used - I/O", w * 1/2 + 2 * move, h/2 + 7 * move + 10);
		
		g1.setColor(colors[3]);
		g1.fill(new Ellipse2D.Double(w * 1/2 + move, h/2 + 8 * move, 6, 6));
		g1.drawString("Pool free - I/O", w * 1/2 + 2 * move, h/2 + 8 * move + 10);
		
		g1.setColor(colors[4]);
		g1.fill(new Ellipse2D.Double(w * 1/2 + move, h/2 + 9 * move, 6, 6));
		g1.drawString("CPU usage 5 sec", w * 1/2 + 2 * move, h/2 + 9 * move + 10);
		
		g1.setColor(colors[5]);
		g1.fill(new Ellipse2D.Double(w * 1/2 + move, h/2 + 10 * move, 6, 6));
		g1.drawString("CPU usage 1 min", w * 1/2 + 2 * move, h/2 + 10 * move + 10);
		
		g1.setColor(colors[6]);
		g1.fill(new Ellipse2D.Double(w * 1/2 + move, h/2 + 11 * move, 6, 6));
		g1.drawString("CPU usage 5 min", w * 1/2 + 2 * move, h/2 + 11 * move + 10);
		
		this.setBackground(Color.WHITE);
				
		if (x.size() < 1) 
			return;
		
		if (x.get(0).size() <= 1)
			return;
		
		for (int j = 0; j < x.size(); j++) {
			System.out.println("Graphs 132");
			g1.setPaint(colors[j]);
			List<Integer> mib = x.get(j); 
			
			for (int i = 0; i < mib.size() - 2; i++) {
				if (j == 1 || j == 3) {
					double inc = (double)(w / 2 - 2 * move) / x.get(0).size();
					double scale = (double)(h / 2 - 2 * move) / (getMax(1,3,3) == getMin(1,3,3) ? 1 : getMax(1,3,3) - getMin(1,3,3));
					
					double x1 = move + i * inc + w / 2;
					double y1 = h - move - scale * (mib.get(i + 1) - mib.get(i) - getMin(1,3,3)) - h / 2 - j / 2 * 5;
					double x2 = move + (i + 1) * inc + w / 2;
					double y2 = h - move - scale * (mib.get(i + 2) - mib.get(i + 1) - getMin(1,3,3)) - h / 2 - j / 2 * 5;
					
					g1.draw(new Line2D.Double(x1, y1, x2, y2));
					
					//g1.drawString(Integer.toString(getMin(1,3,3)), w/2 + move - 2, h / 2 - move + 13);
				}
				else if (j == 0 || j == 2) {
					double inc = (double)(w / 2 - 2 * move) / x.get(0).size();
					double scale = (double)(h / 2 - 2 * move) / (getMax(0,2,2) == getMin(0,2,2) ? 1 : getMax(0,2,2) - getMin(0,2,2));
					
					double x1 = move + i * inc;
					double y1 = h - move - scale * (mib.get(i + 1) - mib.get(i) - getMin(0,2,2)) - h / 2 - j / 2 * 5;
					double x2 = move + (i + 1) * inc;
					double y2 = h - move - scale * (mib.get(i + 2) - mib.get(i + 1) - getMin(0,2,2)) - h / 2 - j / 2 * 5;
					
					g1.draw(new Line2D.Double(x1, y1, x2, y2));
					
					//g1.drawString(Integer.toString(getMin(0,2,2)), move - 2, h / 2 - move + 13);
				}
				else {
					double inc = (double)(w / 2 - 2 * move) / x.get(0).size();
					double scale = (double)(h / 2 - 2 * move) / (getMax(4,5,6) == getMin(4,5,6) ? 1 : getMax(4,5,6) - getMin(4,5,6));
					
					double x1 = move + i * inc;
					double y1 = h - move - scale * (mib.get(i + 1) - mib.get(i) - getMin(4,5,6)) - (j - 4) * 5;
					double x2 = move + (i + 1) * inc;
					double y2 = h - move - scale * (mib.get(i + 2) - mib.get(i + 1) - getMin(4,5,6)) - (j - 4) * 5;
					
					g1.draw(new Line2D.Double(x1, y1, x2, y2));
					
					//g1.drawString(Integer.toString(getMin(4,5,6)), move - 2, h - move + 13);
				}
			}
		}
	}
	
	public Graphs(ArrayList<List<Integer>>x, List<Integer>y) { this.x = x; this.y = y; }
}