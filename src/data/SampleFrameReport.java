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

package data;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/

public class SampleFrameReport {
	SampleFrame sampleFrame;
	String projectName;
	String levelNames[];
	int numLevels;
	boolean levelStratified[];
	String strataNames[][];
	//summary data
	int numUnitsLevel[];
	int numUnitsStrata[][];

	/**
	 * [Level][Parent Strata][Child Strata]
	 */
	int strataCrossTabCounts[][][];
	//int strataCrossTabDenom[][];
		
	/**
	 * [Level][Strata](Children)
	 */
	ArrayList<Integer> unitsPerParent[][];
	/**
	 * [Level][Strata][Summary: Mean, Min/10/Med/90/Max]
	 */
	String unitsPerParentSummary[][][];
	String outcomeSummary[][][];
	String sizeSummary[][][];
	boolean showSize;
	
	DefaultTreeModel modelTree;
	String strReport;
	
	public SampleFrameReport(Project project, JTextPane textPaneReport, JTree treeFrame){
		sampleFrame=project.sampleFrame;
		projectName=project.name;
		numLevels=sampleFrame.numLevels;
		levelNames=new String[numLevels];
		levelStratified=new boolean[numLevels];
		strataNames=new String[numLevels][];
		unitsPerParent=new ArrayList[numLevels][];
		for(int i=0; i<numLevels; i++){
			levelNames[i]=sampleFrame.levels[i].name;
			levelStratified[i]=sampleFrame.levels[i].hasStrata;
			strataNames[i]=new String[sampleFrame.levels[i].numStrata];
			unitsPerParent[i]=new ArrayList[sampleFrame.levels[i].numStrata];
			for(int s=0; s<sampleFrame.levels[i].numStrata; s++){
				strataNames[i][s]=sampleFrame.levels[i].strata.get(s);
				unitsPerParent[i][s]=new ArrayList<Integer>();
			}
		}
		
				
		displayTree(treeFrame);
		calculate();
		printReport(textPaneReport);
	}
	
	
	private void displayTree(JTree treeFrame){
		int numChildren=sampleFrame.levels[0].numRows;
		modelTree=new DefaultTreeModel(
				new DefaultMutableTreeNode(projectName+" ("+numChildren+")") {
					{
					}
				}
			);
		treeFrame.setModel(modelTree);
		
		DefaultMutableTreeNode root=(DefaultMutableTreeNode)modelTree.getRoot();
		SampUnit rootUnit=sampleFrame.sampleTree;
		addChildren(root,rootUnit);
		
	}
	
	private void addChildren(DefaultMutableTreeNode parent, SampUnit parentUnit){
		int levelIndex=parentUnit.level;
		int numStrata=parentUnit.children.length;
		for(int s=0; s<numStrata; s++){
			int numChildren=parentUnit.children[s].size();
			for(int c=0; c<numChildren; c++){
				SampUnit childUnit=parentUnit.children[s].get(c);
				int numGrandchildren=getNumChildren(childUnit);
				if(levelIndex<numLevels-1){unitsPerParent[levelIndex][s].add(numGrandchildren);} //not last level
				String childName=childUnit.name;
				if(!childUnit.stratum.equals("[Overall]")){childName+=" - "+childUnit.stratum;}
				if(numGrandchildren>0){childName+=" ("+numGrandchildren+")";}
				DefaultMutableTreeNode child=new DefaultMutableTreeNode(childName);
				modelTree.insertNodeInto(child, parent, parent.getChildCount());
				addChildren(child,childUnit);
			}
		}
	}

	private int getNumChildren(SampUnit parent){
		int numChildren=0;
		int numStrata=parent.children.length;
		for(int s=0; s<numStrata; s++){
			numChildren+=parent.children[s].size();
		}
		return(numChildren);
	}
	
	private void calculate(){
		//Overall number of units
		numUnitsLevel=new int[numLevels];
		numUnitsStrata=new int[numLevels][];
		for(int i=0; i<numLevels; i++){
			Level curLevel=sampleFrame.levels[i];
			numUnitsLevel[i]=curLevel.numRows;
			if(curLevel.hasStrata){
				numUnitsStrata[i]=new int[curLevel.numStrata];
				for(int r=0; r<curLevel.numRows; r++){
					int sIndex=curLevel.strata.indexOf(curLevel.data[r][curLevel.strataCol]);
					numUnitsStrata[i][sIndex]++;
				}
			}
		}
		
		//Children per parent
		unitsPerParentSummary=new String[numLevels][][];
		for(int i=0; i<numLevels-1; i++){ //not last level
			Level curLevel=sampleFrame.levels[i];
			int numStrata=1; //overall
			if(curLevel.hasStrata){numStrata+=curLevel.numStrata;}
			unitsPerParentSummary[i]=new String[numStrata][6];
			ArrayList<Integer> overall=new ArrayList<Integer>();
			
			if(curLevel.hasStrata){
				for(int s=0; s<curLevel.numStrata; s++){
					ArrayList<Integer> curList=unitsPerParent[i][s];
					overall.addAll(curList); //append
					unitsPerParentSummary[i][s+1]=calcSummary(curList);
				}
			}
			else{
				overall=unitsPerParent[i][0];
			}
			
			//overall
			unitsPerParentSummary[i][0]=calcSummary(overall);
		}
		
		//Strata crosstabs by level
		strataCrossTabCounts=new int[numLevels][][];
		for(int i=0; i<numLevels; i++){
			int s1=sampleFrame.levels[i].numStrata;
			int s2=1;
			if(i<numLevels-1){s2=sampleFrame.levels[i+1].numStrata;}
			strataCrossTabCounts[i]=new int[s1][s2];
		}
		calcCrossTab(sampleFrame.sampleTree,-1);
		
		//Outcome and size summary
		outcomeSummary=new String[numLevels][][];
		sizeSummary=new String[numLevels][][];
		showSize=false;
		for(int i=0; i<numLevels; i++){
			Level curLevel=sampleFrame.levels[i];
			
			//build list
			ArrayList listOutcomes[]=new ArrayList[curLevel.numStrata];
			ArrayList listSizes[]=new ArrayList[curLevel.numStrata];
			for(int s=0; s<curLevel.numStrata; s++){
				listOutcomes[s]=new ArrayList<Double>();
				listSizes[s]=new ArrayList<Double>();
			}
			
			for(int r=0; r<curLevel.numRows; r++){
				int sIndex=0; //default overall
				if(curLevel.hasStrata){sIndex=curLevel.strata.indexOf(curLevel.data[r][curLevel.strataCol]);}
				//outcome
				String val=curLevel.data[r][curLevel.outcomeCol].replace(",",""); //strip commas
				listOutcomes[sIndex].add(Double.parseDouble(val));
				//size
				if(curLevel.hasSize){
					showSize=true;
					val=curLevel.data[r][curLevel.ppsCol].replace(",",""); //strip commas
					listSizes[sIndex].add(Double.parseDouble(val));
				}
			}
			
			//calc summary
			if(curLevel.hasStrata){
				//outcome
				outcomeSummary[i]=new String[curLevel.numStrata+1][6];
				ArrayList overallOutcome=new ArrayList<Double>();
				for(int s=0; s<curLevel.numStrata; s++){
					overallOutcome.addAll(listOutcomes[s]); //append
					outcomeSummary[i][s+1]=calcSummary(listOutcomes[s]);
				}
				outcomeSummary[i][0]=calcSummary(overallOutcome);
				//size
				sizeSummary[i]=new String[curLevel.numStrata+1][6];
				if(curLevel.hasSize){
					ArrayList overallSize=new ArrayList<Double>();
					for(int s=0; s<curLevel.numStrata; s++){
						overallSize.addAll(listSizes[s]); //append
						sizeSummary[i][s+1]=calcSummary(listSizes[s]);
					}
					sizeSummary[i][0]=calcSummary(overallSize);
				}
				else{
					sizeSummary[i][0]=new String[]{"NA","NA","NA","NA","NA","NA"};
				}
			}
			else{ //overall only
				//outcome
				outcomeSummary[i]=new String[1][6];
				outcomeSummary[i][0]=calcSummary(listOutcomes[0]);
				//size
				sizeSummary[i]=new String[1][6];
				if(curLevel.hasSize){
					sizeSummary[i][0]=calcSummary(listSizes[0]);
				}
				else{
					sizeSummary[i][0]=new String[]{"NA","NA","NA","NA","NA","NA"};
				}
			}
		}
	}
	
	private void calcCrossTab(SampUnit parent, int parentStrata){
		int numStrata=parent.children.length;
		int levelIndex=parent.level-1;
		for(int s=0; s<numStrata; s++){
			int numChildren=parent.children[s].size();
			for(int c=0; c<numChildren; c++){		
				SampUnit child=parent.children[s].get(c);
				int s2=sampleFrame.levels[levelIndex+1].strata.indexOf(child.stratum);
				if(levelIndex>=0){ //not root
					strataCrossTabCounts[levelIndex][parentStrata][s2]++;
				}
				calcCrossTab(child,s);
			}
		}
	}
	
	private String[] calcSummary(ArrayList list){
		String summary[]=new String[6];
		int numVals=list.size();
		if(numVals>0){
			double curMean=0;
			for(int n=0; n<numVals; n++){curMean+=Double.parseDouble(list.get(n)+"");}
			Collections.sort(list);
			curMean/=(numVals*1.0);
			curMean=Math.round(curMean*10)/10.0;
			summary[0]=curMean+""; //mean
			summary[1]=list.get(0)+""; //min
			summary[2]=list.get(calcPercentileIndex(0.1,numVals))+""; //10%
			summary[3]=list.get(calcPercentileIndex(0.5,numVals))+""; //50%
			summary[4]=list.get(calcPercentileIndex(0.9,numVals))+""; //90%
			summary[5]=list.get(numVals-1)+""; //max
		}
		return(summary);
	}
	
	private int calcPercentileIndex(double percentile, int size){
		int index=(int)Math.round(percentile*size)-1;
		index=Math.max(index, 0); //floor of 0
		index=Math.min(index, size-1); //ceiling of size-1
		return(index);
	}
	
	private void print(String str){
		strReport+=str;
	}
	
	private void printReport(JTextPane textPaneReport){
		strReport="";
		textPaneReport.setFont(new Font("Consolas", Font.PLAIN, 14));
		
		HTMLEditorKit kit = new HTMLEditorKit();
		textPaneReport.setEditorKit(kit);
		
		//add html styles
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("th {border-bottom: 1px solid black}");
        		
        print("<html><body><b>Sample Frame Report</b><br>");
		print(new Date()+"<br>");
		print("Project: "+projectName+"<br><br>");

		//number of units
		print("<table>");
		print("<caption>Number of Units</caption>");
		print("<tr><th>Level (Stratum)</th><th>Units</th></tr>");
		for(int i=0; i<numLevels; i++){
			print("<tr><td>"+levelNames[i]+"</td><td align=\"right\">"+numUnitsLevel[i]+"</td></tr>");
			if(levelStratified[i]){
				for(int s=0; s<strataNames[i].length; s++){
					print("<tr><td>&nbsp;&nbsp;&nbsp;&nbsp;"+strataNames[i][s]+"</td><td align=\"right\">"+numUnitsStrata[i][s]+"</td></tr>");
				}
			}
		}
		print("</table>");
		print("<br><br>");
		
		//number of children per unit
		printSummaryTable("Number of Children per Parent Unit",numLevels-1,unitsPerParentSummary);
		print("<br><br>");
		
		//strata crosstabs, count
		for(int i=0; i<numLevels-1; i++){
			Level parentLevel=sampleFrame.levels[i];
			Level childLevel=sampleFrame.levels[i+1];
			print("<table>");
			print("<caption>Strata Crosstab - "+parentLevel.name+", "+childLevel.name+" (Counts)</caption>");
			int numStrata1=parentLevel.numStrata, numStrata2=childLevel.numStrata;
			//rows=parent strata, cols=child strata
			//headers
			print("<tr><td></td><td colspan=\""+numStrata2+"\"><b>"+childLevel.name+" Strata</b></td></tr>");
			print("<tr>");
			print("<th>"+parentLevel.name+" Strata</th>");
			for(int c=0; c<numStrata2; c++){print("<th>"+childLevel.strata.get(c)+"</th>");}
			print("</tr>");
			//data
			for(int r=0; r<numStrata1; r++){
				print("<tr>");
				print("<td>"+parentLevel.strata.get(r)+"</td>");
				for(int c=0; c<numStrata2; c++){
					print("<td align=\"right\">"+strataCrossTabCounts[i][r][c]+"</td>");
				}
				print("</tr>");
			}
			print("</table>");
			print("<br>");
		}
		print("<br>");
		
		//strata crosstabs, mean
		for(int i=0; i<numLevels-1; i++){
			Level parentLevel=sampleFrame.levels[i];
			Level childLevel=sampleFrame.levels[i+1];
			print("<table>");
			print("<caption>Strata Crosstab - "+parentLevel.name+", "+childLevel.name+" (Means)</caption>");
			int numStrata1=parentLevel.numStrata, numStrata2=childLevel.numStrata;
			//rows=parent strata, cols=child strata
			//headers
			print("<tr><td></td><td colspan=\""+numStrata2+"\"><b>"+childLevel.name+" Strata</b></td></tr>");
			print("<tr>");
			print("<th>"+parentLevel.name+" Strata</th>");
			for(int c=0; c<numStrata2; c++){print("<th>"+childLevel.strata.get(c)+"</th>");}
			print("</tr>");
			//data
			for(int r=0; r<numStrata1; r++){
				print("<tr>");
				print("<td>"+parentLevel.strata.get(r)+"</td>");
				for(int c=0; c<numStrata2; c++){
					double denom=numUnitsLevel[i]*1.0;
					if(parentLevel.hasStrata){
						denom=numUnitsStrata[i][r]*1.0;
					}
					double mean=strataCrossTabCounts[i][r][c]/denom;
					mean=Math.round(mean*10)/10.0;
					print("<td align=\"right\">"+mean+"</td>");
				}
				print("</tr>");
			}
			print("</table>");
			print("<br>");
		}
		print("<br>");

		//outcomes summary
		printSummaryTable("Outcome Summary",numLevels,outcomeSummary);
		print("<br><br>");
		
		//size summary
		if(showSize){
			printSummaryTable("Size Summary",numLevels,sizeSummary);
		}
		
		print("</body></html>");
        
		Document doc = kit.createDefaultDocument();
		textPaneReport.setDocument(doc);
		textPaneReport.setText(strReport);
		
		textPaneReport.setCaretPosition(0); //go to top
	}
	
	private void printSummaryTable(String caption, int numLevels, String summary[][][]){
		print("<table>");
		print("<caption>"+caption+"</caption>");
		print("<tr><th>Level (Stratum)</th><th>Mean</th><th>Min</th><th>10%</th><th>Median</th><th>90%</th><th>Max</th></tr>");
		for(int i=0; i<numLevels; i++){
			//overall level
			print("<tr><td>"+levelNames[i]);
			for(int z=0; z<6; z++){
				String val=summary[i][0][z];
				if(val==null){val="NA";}
				print("</td><td align=\"right\">"+val+"</td>");
			}
			print("</tr>");
			//strata
			if(levelStratified[i]){
				for(int s=0; s<strataNames[i].length; s++){
					print("<tr><td>&nbsp;&nbsp;&nbsp;&nbsp;"+strataNames[i][s]);
					for(int z=0; z<6; z++){
						String val=summary[i][s+1][z];
						if(val==null){val="NA";}
						print("</td><td align=\"right\">"+val+"</td>");
					}
					print("</tr>");
				}
			}
		}
		print("</table>");
	}
	
}