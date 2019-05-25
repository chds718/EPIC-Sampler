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
import java.util.ArrayList;

import javax.swing.JTextArea;

/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/

public class Scheduler {
	Constraints constraints;
	public ArrayList<Design> schedule;
	ArrayList<String> uniqueDesigns;
	int numLevels;
	int numLevelStrata[];
	ArrayList<boolean[][]> designCombos;
	JTextArea textArea;
	
	public Scheduler(Constraints constraints, JTextArea textArea){
		try{
			this.constraints=constraints;
			this.numLevels=constraints.numLevels;
			numLevelStrata=new int[numLevels];
			this.textArea=textArea;
			
			schedule=new ArrayList<Design>();
			uniqueDesigns=new ArrayList<String>();
			
			designCombos=new ArrayList();
			ComboNode comboTree=new ComboNode();
			buildTree(comboTree,0);
			
			int numCombos=designCombos.size();
			for(int i=0; i<numCombos; i++){
				boolean method[][]=designCombos.get(i);
				
				//Initialize all to min
				int curNums[][]=new int[numLevels][];
				double curPercents[][]=new double[numLevels][];
				for(int l=0; l<numLevels; l++){
					if(method[0][l]==false){ //overall
						int numStrata=1;
						numLevelStrata[l]=numStrata;
						curNums[l]=new int[numStrata];
						curPercents[l]=new double[numStrata];
						if(constraints.sampleInt[l][0]){
							curNums[l][0]=constraints.minUnits[l][0];
						}
						else{
							curNums[l][0]=-1;
							curPercents[l][0]=constraints.minPercent[l][0];
						}
					}
					else{ //stratified
						int numStrata=constraints.averageCost[l].length-1;
						numLevelStrata[l]=numStrata;
						curNums[l]=new int[numStrata];
						curPercents[l]=new double[numStrata];
						
						for(int s=0; s<numStrata; s++){
							if(constraints.sampleInt[l][s+1]){
								curNums[l][s]=constraints.minUnits[l][s+1];
							}
							else{
								curNums[l][s]=-1;
								curPercents[l][s]=constraints.minPercent[l][s+1];
							}
						}
					}
				}
				
				//scheduleDesign(0,0,curNums,curPercents,method);
				scheduleDesign(0,0,curNums,curPercents,method);
			}
			
			
		}catch(Exception e){
			textArea.append("Error: "+e.toString()+"\n");
			e.printStackTrace();
		}
	}
	
	private void buildTree(ComboNode parent,int level){
		//stratified
		if(constraints.stratified[level]<2){ //simple XOR stratified
			boolean stratified=false;
			if(constraints.stratified[level]==1){stratified=true;}
			
			if(constraints.pps[level]<2){ //equal XOR pps
				boolean pps=false;
				if(constraints.pps[level]==1){pps=true;}
				
				ComboNode child=new ComboNode(parent,level);
				child.method[0][level]=stratified; child.method[1][level]=pps;
				if(level<numLevels-1){buildTree(child,level+1);}
				else{designCombos.add(child.method);} //last level, add combo to list
			}
			else if(constraints.pps[level]==2){ //equal OR pps
				//equal
				ComboNode child1=new ComboNode(parent,level);
				child1.method[0][level]=stratified; child1.method[1][level]=false;
				if(level<numLevels-1){buildTree(child1,level+1);}
				else{designCombos.add(child1.method);} //last level, add combo to list
				//pps
				ComboNode child2=new ComboNode(parent,level);
				child2.method[0][level]=stratified; child2.method[1][level]=true;
				if(level<numLevels-1){buildTree(child2,level+1);}
				else{designCombos.add(child2.method);} //last level, add combo to list
			}
		}
		else if(constraints.stratified[level]==2){ //simple OR stratified
			if(constraints.pps[level]<2){ //equal XOR pps
				boolean pps=false;
				if(constraints.pps[level]==1){pps=true;}
				
				//simple
				ComboNode child1=new ComboNode(parent,level);
				child1.method[0][level]=false; child1.method[1][level]=pps;
				if(level<numLevels-1){buildTree(child1,level+1);}
				else{designCombos.add(child1.method);} //last level, add combo to list
				//stratified
				ComboNode child2=new ComboNode(parent,level);
				child2.method[0][level]=true; child2.method[1][level]=pps;
				if(level<numLevels-1){buildTree(child2,level+1);}
				else{designCombos.add(child2.method);} //last level, add combo to list
			}
			else if(constraints.pps[level]==2){ //equal OR pps
				//simple-equal
				ComboNode child1=new ComboNode(parent,level);
				child1.method[0][level]=false; child1.method[1][level]=false;
				if(level<numLevels-1){buildTree(child1,level+1);}
				else{designCombos.add(child1.method);} //last level, add combo to list
				//simple-pps
				ComboNode child2=new ComboNode(parent,level);
				child2.method[0][level]=false; child2.method[1][level]=true;
				if(level<numLevels-1){buildTree(child2,level+1);}
				else{designCombos.add(child2.method);} //last level, add combo to list
				//stratified-equal
				ComboNode child3=new ComboNode(parent,level);
				child3.method[0][level]=true; child3.method[1][level]=false;
				if(level<numLevels-1){buildTree(child3,level+1);}
				else{designCombos.add(child3.method);} //last level, add combo to list
				//stratified-pps
				ComboNode child4=new ComboNode(parent,level);
				child4.method[0][level]=true; child4.method[1][level]=true;
				if(level<numLevels-1){buildTree(child4,level+1);}
				else{designCombos.add(child4.method);} //last level, add combo to list
			}
		}
	}
	
	
	private void scheduleDesign(int curLevel, int curStratum, int nums[][], double percents[][], boolean method[][]){
		if(curLevel<numLevels && curStratum<numLevelStrata[curLevel]){
			//deep copy arrays
			int curNums[][]=new int[numLevels][]; double curPercents[][]=new double[numLevels][];
			for(int l=0; l<numLevels; l++){
				curNums[l]=new int[numLevelStrata[l]]; curPercents[l]=new double[numLevelStrata[l]];
				for(int s=0; s<numLevelStrata[l]; s++){
					curNums[l][s]=nums[l][s]; curPercents[l][s]=percents[l][s];
				}
			}
			
			if(method[0][curLevel]==false){ //overall
				if(constraints.sampleInt[curLevel][0]){ //count
					for(int k=constraints.minUnits[curLevel][0]; k<=constraints.maxUnits[curLevel][0]; k++){
						curNums[curLevel][0]=k;
						addDesign(curNums,curPercents,method);
						//next level
						scheduleDesign(curLevel+1,0,curNums,curPercents,method);
					}
				}
				else{ //proportion
					curNums[curLevel][0]=-1;
					double k=constraints.minPercent[curLevel][0];
					while(k<=constraints.maxPercent[curLevel][0]){
						curPercents[curLevel][0]=k;
						addDesign(curNums,curPercents,method);
						k+=constraints.stepPercent[curLevel][0];
						k=(double)(Math.round(k*10000)/10000.0);
						//next level
						scheduleDesign(curLevel+1,0,curNums,curPercents,method);
					}
				}
			}
			else{ //stratified
				if(constraints.sampleInt[curLevel][curStratum+1]){ //count
					for(int k=constraints.minUnits[curLevel][curStratum+1]; k<=constraints.maxUnits[curLevel][curStratum+1]; k++){
						curNums[curLevel][curStratum]=k;
						addDesign(curNums,curPercents,method);
						//next stratum
						scheduleDesign(curLevel,curStratum+1,curNums,curPercents,method);
						//next level
						scheduleDesign(curLevel+1,0,curNums,curPercents,method);
					}
				}
				else{ //proportion
					curNums[curLevel][curStratum]=-1;
					double k=constraints.minPercent[curLevel][curStratum+1];
					while(k<=constraints.maxPercent[curLevel][curStratum+1]){
						curPercents[curLevel][curStratum]=k;
						addDesign(curNums,curPercents,method);
						k+=constraints.stepPercent[curLevel][curStratum+1];
						k=(double)(Math.round(k*10000)/10000.0);
						//next stratum
						scheduleDesign(curLevel,curStratum+1,curNums,curPercents,method);
						//next level
						scheduleDesign(curLevel+1,0,curNums,curPercents,method);
					}
				}
			}
		}
		
	}
	
	private void addDesign(int nums[][], double percents[][], boolean method[][]){
		String strDesign="";
		Design curDesign=new Design(numLevels,numLevelStrata);
		for(int l=0; l<numLevels; l++){
			curDesign.stratified[l]=method[0][l];
			strDesign+=curDesign.stratified[l]+"_";
			curDesign.pps[l]=method[1][l];
			strDesign+=curDesign.pps[l]+"_";
			for(int s=0; s<numLevelStrata[l]; s++){
				if(nums[l][s]==-1){ //percent
					curDesign.sampleInt[l][s]=false;
					curDesign.samplePercents[l][s]=percents[l][s];
					strDesign+=curDesign.samplePercents[l][s]+"_";
					if(curDesign.stratified[l]==false){ //overall
						curDesign.floor[l][s]=constraints.floor[l][0];
						curDesign.ceiling[l][s]=constraints.ceiling[l][0];
					}
					else{
						curDesign.floor[l][s]=constraints.floor[l][s+1];
						curDesign.ceiling[l][s]=constraints.ceiling[l][s+1];
					}
				}
				else{ //int
					curDesign.sampleInt[l][s]=true;
					curDesign.sampleNums[l][s]=nums[l][s];
					strDesign+=curDesign.sampleNums[l][s]+"_";
				}
			}
			strDesign+=" ";
		}
		curDesign.buildTag();
		if(!uniqueDesigns.contains(strDesign)){
			uniqueDesigns.add(strDesign);
			schedule.add(curDesign);
		}
	}
	
	private class ComboNode {
		/**
		 * [Stratified/PPS][Level]
		 */
		boolean method[][];
		
		/**
		 * Constructor
		 */
		ComboNode(){
			method=new boolean[2][numLevels];
		}
		
		ComboNode(ComboNode parent, int level){
			method=new boolean[2][numLevels];
			//propogate down
			for(int i=0; i<level; i++){
				method[0][i]=parent.method[0][i];
				method[1][i]=parent.method[1][i];
			}
		}
	}
}