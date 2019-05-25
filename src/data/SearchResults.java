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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import gui.frmMain;

/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/

@XmlRootElement(name="SearchResults")
public class SearchResults{
	//sample frame info
	@XmlElement int numLevels;
	@XmlElement String levelNames[];
	@XmlElement boolean hasStrata[], hasCost[], hasSize[];
	@XmlElement String strataNames[][];
	@XmlElement double outcomeTotals[], costTotals[], sizeTotals[];
	
	//search info
	@XmlElement(name="Constraints", type=Constraints.class)	public Constraints constraints;
	@XmlElement(name="SampleDesign", type=SampleDesign.class) public ArrayList<SampleDesign> results;
	
	
	public SearchResults(){
		
	}
	
	public void save(String filepath) throws JAXBException, IOException{
		JAXBContext context = JAXBContext.newInstance(SearchResults.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		
		// Write to File
		FileOutputStream fstreamO=new FileOutputStream(filepath);
		m.marshal(this,fstreamO);
		fstreamO.close();
	}
	
	public void getSampleFrameInfo(SampleFrame sampleFrame){
		numLevels=sampleFrame.numLevels;
		levelNames=new String[numLevels];
		hasStrata=new boolean[numLevels]; hasCost=new boolean[numLevels]; hasSize=new boolean[numLevels];
		strataNames=new String[numLevels][];
		outcomeTotals=new double[numLevels]; costTotals=new double[numLevels]; sizeTotals=new double[numLevels];
		for(int i=0; i<numLevels; i++){
			Level curLevel=sampleFrame.levels[i];
			levelNames[i]=curLevel.name;
			hasStrata[i]=curLevel.hasStrata;
			if(hasStrata[i]){
				strataNames[i]=new String[curLevel.numStrata];
				for(int s=0; s<curLevel.numStrata; s++){
					strataNames[i][s]=curLevel.strata.get(s);
				}
			}
			for(int r=0; r<curLevel.numRows; r++){
				outcomeTotals[i]+=Double.parseDouble(curLevel.data[r][curLevel.outcomeCol].replaceAll(",", ""));
			}
			hasCost[i]=curLevel.hasCost;
			if(curLevel.hasCost){
				for(int r=0; r<curLevel.numRows; r++){
					costTotals[i]+=Double.parseDouble(curLevel.data[r][curLevel.costCol].replaceAll(",", ""));
				}
			}
			hasSize[i]=curLevel.hasSize;
			if(curLevel.hasSize){
				for(int r=0; r<curLevel.numRows; r++){
					sizeTotals[i]+=Double.parseDouble(curLevel.data[r][curLevel.ppsCol].replaceAll(",", ""));
				}
			}
		}
	}
			
	//Check if compatible with current project sample frame
	public boolean isCompatible(SampleFrame sampleFrame){
		if(numLevels!=sampleFrame.numLevels){return(false);}
		for(int i=0; i<numLevels; i++){
			Level curLevel=sampleFrame.levels[i];
			if(!levelNames[i].equals(curLevel.name)){return(false);}
			if(hasStrata[i]!=curLevel.hasStrata){return(false);}
			if(hasStrata[i]){
				if(strataNames[i].length!=curLevel.numStrata){return(false);}
				for(int s=0; s<curLevel.numStrata; s++){
					if(!strataNames[i][s].equals(curLevel.strata.get(s))){return(false);}
				}
			}
			//compare outcomes
			double curOutcomeTotals=0;
			for(int r=0; r<curLevel.numRows; r++){
				curOutcomeTotals+=Double.parseDouble(curLevel.data[r][curLevel.outcomeCol].replaceAll(",", ""));
			}
			if(curOutcomeTotals!=outcomeTotals[i]){return(false);}
			//compare cost
			if(hasCost[i]!=curLevel.hasCost){return(false);}
			if(hasCost[i]){
				double curCostTotals=0;
				for(int r=0; r<curLevel.numRows; r++){
					curCostTotals+=Double.parseDouble(curLevel.data[r][curLevel.costCol].replaceAll(",", ""));
				}
				if(curCostTotals!=costTotals[i]){return(false);}
			}
			//compare size
			if(hasSize[i]!=curLevel.hasSize){return(false);}
			if(hasSize[i]){
				double curSizeTotals=0;
				for(int r=0; r<curLevel.numRows; r++){
					curSizeTotals+=Double.parseDouble(curLevel.data[r][curLevel.ppsCol].replaceAll(",", ""));
				}
				if(curSizeTotals!=sizeTotals[i]){return(false);}
			}
		}
	
		return(true);
	}
	
	public void load(frmMain mainForm){
		mainForm.constraints=constraints;
		mainForm.results=results;
		
		//display constraints
		constraints.display(mainForm);
	
		//display results
		mainForm.modelSearchResults.setRowCount(0);
		int numRows=results.size();
		for(int r=0; r<numRows; r++){
			results.get(r).addToTable(mainForm.modelSearchResults, r, mainForm.curProject.sampleFrame);
		}
	}
	
}