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
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/

@XmlRootElement(name="SampleFrame")
public class SampleFrame {
	@XmlElement public int numLevels;
	@XmlElement (name="Level", type=Level.class) public Level levels[];	
	
	@XmlTransient public SampUnit sampleTree;
	@XmlTransient SampleFrameReport report;
	@XmlTransient SampUnit curLevelUnits[], prevLevelUnits[]; //temporary pointers for building tree
	
	public SampleFrame(){
		//default sample frame
		numLevels=3;
		String levelNames[]=new String[]{"Region","District","Facility"};
		levels=new Level[numLevels];
		for(int i=0; i<numLevels; i++){
			levels[i]=new Level();
			levels[i].name=levelNames[i];
		}
	}
	
	public boolean validate(JTextArea textArea, Project curProject, JTextPane paneReport, JTree treeFrame){
		boolean valid=true;
		ArrayList<String> errors=new ArrayList<String>();
		for(int i=0; i<numLevels; i++){
			levels[i].validate(this,i,errors);
		}
	
		int numErrors=errors.size();
		if(numErrors==0){
			valid=true;
			buildTree();
			report=new SampleFrameReport(curProject,paneReport,treeFrame);
			textArea.setText("Sample frame validated!\n");
		}
		else{
			valid=false;
			textArea.setText(numErrors+" errors found!\n");
			for(int i=0; i<numErrors; i++){
				textArea.append(errors.get(i)+"\n");
			}
			textArea.setCaretPosition(0);
		}
	
		return(valid);
	}
	
	private double parseDatum(String datum){
		datum=datum.replace(",", ""); //strip commas
		double val=Double.parseDouble(datum);
		return(val);
	}
		
	private void buildTree(){
		numLevels=levels.length;
		//Add level 0
		sampleTree=new SampUnit("root",levels[0].numStrata); //Create root
		sampleTree.level=0;
		//Add level 1
		Level L1=levels[0];
		int numStrata=1;
		if(levels.length>1){numStrata=levels[1].numStrata;} //L2 strata
		curLevelUnits=new SampUnit[L1.numRows];
		for(int r=0; r<L1.numRows; r++){
			String name=L1.data[r][0];
			SampUnit curUnit=new SampUnit(name,numStrata);
			curLevelUnits[r]=curUnit;
			curUnit.treePath=name;
			curUnit.level=1;
			curUnit.outcome=parseDatum(L1.data[r][L1.outcomeCol]);
			if(L1.ppsCol!=-1){curUnit.pps=parseDatum(L1.data[r][L1.ppsCol]);}
			else{curUnit.pps=1;}
			if(L1.costCol!=-1){curUnit.cost=parseDatum(L1.data[r][L1.costCol]);}
			else{curUnit.cost=0;}
			if(L1.strataCol!=-1){curUnit.stratum=L1.data[r][L1.strataCol];}
			else{curUnit.stratum="[Overall]";}
			//curL1.samplePath=name+","+curUnit.stratum+",";
			int stratumIndex=L1.strata.indexOf(curUnit.stratum);
			curUnit.stratumIndex=stratumIndex;
			sampleTree.addChild(curUnit,stratumIndex);
		}
		//Add next levels
		for(int i=1; i<numLevels-1; i++){
			addMiddleLevel(i);
		}
		addLastLevel(numLevels-1);
		
	}
	
	private void addMiddleLevel(int levelIndex){
		Level curLevel=levels[levelIndex];
		int numStrata=levels[levelIndex+1].numStrata; //strata of next level
		prevLevelUnits=curLevelUnits;
		curLevelUnits=new SampUnit[curLevel.numRows];
		for(int r=0; r<curLevel.numRows; r++){
			String curPath=curLevel.data[r][0];
			for(int z=1; z<levelIndex; z++){curPath+=";"+curLevel.data[r][z];}
			//find parent
			boolean found=false;
			int z=0;
			while(found==false && z<prevLevelUnits.length){
				if(prevLevelUnits[z].treePath.equals(curPath)){
					found=true;
					String name=curLevel.data[r][levelIndex];
					SampUnit curUnit=new SampUnit(name,numStrata);
					curLevelUnits[r]=curUnit;
					curUnit.treePath=curPath+";"+name;
					curUnit.level=levelIndex+1;
					curUnit.outcome=parseDatum(curLevel.data[r][curLevel.outcomeCol]);
					if(curLevel.ppsCol!=-1){curUnit.pps=parseDatum(curLevel.data[r][curLevel.ppsCol]);}
					else{curUnit.pps=1;}
					if(curLevel.costCol!=-1){curUnit.cost=parseDatum(curLevel.data[r][curLevel.costCol]);}
					else{curUnit.cost=0;}
					if(curLevel.strataCol!=-1){curUnit.stratum=curLevel.data[r][curLevel.strataCol];}
					else{curUnit.stratum="[Overall]";}
					//curL.samplePath=parent.samplePath+name+","+curL.stratum+",";
					int stratumIndex=curLevel.strata.indexOf(curUnit.stratum);
					curUnit.stratumIndex=stratumIndex;
					prevLevelUnits[z].addChild(curUnit,stratumIndex);
				}
				z++;
			}
		}
	}
	
	private void addLastLevel(int levelIndex){
		Level curLevel=levels[levelIndex];
		prevLevelUnits=curLevelUnits;
		for(int r=0; r<curLevel.numRows; r++){
			String curPath=curLevel.data[r][0];
			for(int z=1; z<levelIndex; z++){curPath+=";"+curLevel.data[r][z];}
			//find parent
			boolean found=false;
			int z=0;
			while(found==false && z<prevLevelUnits.length){
				if(prevLevelUnits[z].treePath.equals(curPath)){
					found=true;
					String name=curLevel.data[r][levelIndex];
					SampUnit curUnit=new SampUnit(name,1);
					curUnit.treePath=curPath+";"+name;
					curUnit.level=levelIndex+1;
					curUnit.outcome=parseDatum(curLevel.data[r][curLevel.outcomeCol]);
					if(curLevel.ppsCol!=-1){curUnit.pps=parseDatum(curLevel.data[r][curLevel.ppsCol]);}
					else{curUnit.pps=1;}
					if(curLevel.costCol!=-1){curUnit.cost=parseDatum(curLevel.data[r][curLevel.costCol]);}
					else{curUnit.cost=0;}
					if(curLevel.strataCol!=-1){curUnit.stratum=curLevel.data[r][curLevel.strataCol];}
					else{curUnit.stratum="[Overall]";}
					//curL.samplePath=parent.samplePath+name+","+curL.stratum+",";
					int stratumIndex=curLevel.strata.indexOf(curUnit.stratum);
					curUnit.stratumIndex=stratumIndex;
					prevLevelUnits[z].addChild(curUnit,stratumIndex);
				}
				z++;
			}
		}
	}
	
}