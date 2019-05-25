/**
 * EPIC Sampler
 * Copyright (C) 2018-2019 Center for Health Decision Science, Harvard T.H. Chan School of Public Health
 *
 * This file is part of the EPIC Sampler. The EPIC Sampler is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * The EPIC Sampler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with The EPIC Sampler.  If not, see <http://www.gnu.org/licenses/>.
 */

package gui;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.SwingConstants;


/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/

public class GraphNode{
	
	public int xPos,yPos;
	public int width,height;
	
	public boolean root;
	public String name, stratum, strPercent;
	public boolean stratified, pps, percent;
	
	public JLabel lblName, lblStratum, lblProb, lblPercent;
	
	GraphNode parent;
	ArrayList<GraphNode> children; 
	
	/**
	 * Default constructor
	 */
	public GraphNode(){
		children=new ArrayList<GraphNode>();
		
		root=false;
		width=35; height=50;
		
		lblName=new JLabel();
		lblName.setHorizontalAlignment(SwingConstants.CENTER);
		
		stratified=false;
		lblStratum=new JLabel();
		lblStratum.setHorizontalAlignment(SwingConstants.CENTER);
		lblStratum.setForeground(Color.BLUE);
		lblStratum.setVisible(false);
		
		lblProb=new JLabel();
		lblProb.setHorizontalAlignment(SwingConstants.CENTER);
		lblProb.setForeground(new Color(139,0,0)); //darkred
		
		lblPercent=new JLabel();
		lblPercent.setHorizontalAlignment(SwingConstants.CENTER);
		lblPercent.setVisible(false);
		lblPercent.setForeground(new Color(0,100,0)); //green
	
	}
	
	public void paintComponent(Graphics g){
		g.setColor(new Color(240,248,255)); //Fill color
		
		g.fillRect(xPos, yPos, width, height);
		g.setColor(Color.BLACK);
		Graphics2D g2d = (Graphics2D) g;
		//g2d.setStroke(new BasicStroke(2f));
		g2d.setStroke(new BasicStroke(1f));
		g.drawRect(xPos,yPos,width,height);
		
		lblName.setText(name);
		lblName.setFont(lblName.getFont().deriveFont(8f));
		lblName.setBounds(xPos, yPos, width, height);
		if(stratified){
			lblName.setBounds(xPos, yPos, width, height/2);
			lblStratum.setText(stratum);
			lblStratum.setBounds(xPos, yPos+height/2, width, height/2);
			lblStratum.setFont(lblStratum.getFont().deriveFont(8f));
			lblStratum.setVisible(true);
		}
		if(percent){
			lblPercent.setText(strPercent);
			lblPercent.setBounds(xPos, yPos-height/2, width, height/2);
			lblPercent.setFont(lblPercent.getFont().deriveFont(8f));
			lblPercent.setVisible(true);
	
		}
		
		//Draw line to parent
		if(root==false){
			g.drawLine(parent.xPos+parent.width/2, parent.yPos+parent.height, xPos+width/2, yPos);
			//sampling probability
			if(pps==false){lblProb.setText("=");}
			else{lblProb.setText("PPS");}
			lblProb.setBounds((parent.xPos+xPos)/2, (parent.yPos+yPos)/2, width, height);
			lblProb.setFont(lblProb.getFont().deriveFont(8f));
		}
	}


}