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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import data.Design;
import data.Project;
import data.SampleFrame;

/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/

public class PanelDesign extends JPanel{
	SampleFrame frame;
	Design design;
	GraphNode root;
	ArrayList<JLabel> labels;
	int maxX=0, maxY=0;
	
	protected int canvasWidth, canvasHeight;
	
	//protected int canvasWidth=450, canvasHeight=1000; //Default canvas size
	int yOffset;
	int xOffset;
	
	/**
	 * Default constructor
	 */
	public PanelDesign(){
		setBorder(BorderFactory.createLineBorder(Color.black));
		this.setBackground(Color.WHITE);
		labels=new ArrayList<JLabel>();
	}
	
	/**
	 * Updates the display
	 */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);       

		if(root!=null){
			paintNodes(root,g);
		}
	}  
	
	/**
	 * Recursively paint nodes
	 */
	private void paintNodes(GraphNode node, Graphics g){
		node.paintComponent(g);
		int numChildren=node.children.size();
		for(int c=0; c<numChildren; c++){
			paintNodes(node.children.get(c),g);
		}
	}

	public void drawGraph(Project project, Design design){
		//clear old design
		int numLbls=labels.size();
		for(int i=0; i<numLbls; i++){
			this.remove(labels.get(i));
		}
		labels.clear();
		
		this.frame=project.sampleFrame;
		this.design=design;
		
		
		//Dimension dim=this.getSize();
		//canvasHeight=dim.height;
		int numLevels=design.numLevels;
		
		//int unitHeight=(canvasHeight/(numLevels+1))/2;
		int unitHeight=30;
		yOffset=unitHeight*2;
		xOffset=5;
		
		root=new GraphNode();
		root.root=true;
		root.name=project.name;
		root.height=30;
		root.yPos=root.height/2;
		this.add(root.lblName); labels.add(root.lblName);
		
		maxX=0; maxY=0;
		addChildren(root,0);
		
		//canvasWidth=Math.max(maxX, 500);
		canvasHeight=maxY+root.height/2;
		canvasWidth=maxX+xOffset;
		
		//center tree
		int shift=canvasWidth/2-(root.xPos+root.width/2);
		shiftX(root,shift);
		
		revalidate();
		repaint();
		
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(canvasWidth,canvasHeight);
	}
	
	private void shiftX(GraphNode parent, int shift){
		parent.xPos+=shift;
		int numChildren=parent.children.size();
		for(int c=0; c<numChildren; c++){
			shiftX(parent.children.get(c),shift);
		}
	}
	
	private void addChildren(GraphNode parent, int levelIndex){
		if(levelIndex==design.numLevels){ //last level
			parent.xPos=maxX+xOffset;
			maxX=parent.xPos+parent.width;
			maxY=Math.max(maxY, parent.yPos+parent.height);
		}
		else{
			int curX=0, totalChildren=0;
			if(design.stratified[levelIndex]==false){ //overall
				int numChildren=design.sampleNums[levelIndex][0];
				boolean prop=false;
				if(design.sampleInt[levelIndex][0]==false){ //proportion
					numChildren=1;
					prop=true;
				}
				totalChildren=numChildren;
				for(int c=0; c<numChildren; c++){
					GraphNode child=new GraphNode();
					child.parent=parent;
					child.name=frame.levels[levelIndex].name;
					child.pps=design.pps[levelIndex];
					if(prop){
						child.percent=true;
						child.strPercent=Math.round(design.samplePercents[levelIndex][0]*100)+"%";
						this.add(child.lblPercent); labels.add(child.lblPercent);
					}
					
					child.height=parent.height;
					child.yPos=parent.yPos+yOffset;
					parent.children.add(child);

					this.add(child.lblName); labels.add(child.lblName);
					this.add(child.lblProb); labels.add(child.lblProb);
					addChildren(child,levelIndex+1);

					curX+=child.xPos;
				}
			}
			else{ //stratified
				int numStrata=frame.levels[levelIndex].numStrata;
				for(int s=0; s<numStrata; s++){
					String stratum=frame.levels[levelIndex].strata.get(s);
					int numChildren=design.sampleNums[levelIndex][s];
					boolean prop=false;
					if(design.sampleInt[levelIndex][s]==false){ //proportion
						numChildren=1;
						prop=true;
					}
					
					totalChildren+=numChildren;
					for(int c=0; c<numChildren; c++){
						GraphNode child=new GraphNode();
						child.parent=parent;
						child.name=frame.levels[levelIndex].name;
						child.stratified=true;
						child.stratum=stratum;
						child.pps=design.pps[levelIndex];
						if(prop){
							child.percent=true;
							child.strPercent=Math.round(design.samplePercents[levelIndex][0]*100)+"%";
							this.add(child.lblPercent); labels.add(child.lblPercent);
						}
						child.height=parent.height;
						child.yPos=parent.yPos+yOffset;
						parent.children.add(child);

						this.add(child.lblName); labels.add(child.lblName);
						this.add(child.lblStratum); labels.add(child.lblStratum);
						this.add(child.lblProb); labels.add(child.lblProb);
						addChildren(child,levelIndex+1);

						curX+=child.xPos;
					}
					
				}
			}
			
			curX/=totalChildren;
			parent.xPos=curX;
		}
	}
	
}