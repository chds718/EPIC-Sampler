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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/

@XmlRootElement(name="Level")
public class Level {
	@XmlElement
	public String name;
	@XmlElement public int numRows, numCols;
	@XmlElement public String headers[];
	@XmlElement public String data[][];
	@XmlElement public boolean hasStrata, hasSize, hasCost;
	
	@XmlTransient int outcomeCol=-1, strataCol=-1, costCol=-1, ppsCol=-1;
	@XmlTransient public ArrayList<String> strata;
	@XmlTransient public int numStrata;
	
	@XmlTransient ArrayList<String> samplePaths;
	
	/**
	 * No argument constructor - for XML binding
	 */
	public Level(){
		
	}
	
	public void updateColumns(SampleFrame sampleFrame, int levelIndex){
		ArrayList<String> headerNames=new ArrayList<String>();
		//add parent levels
		for(int i=0; i<levelIndex; i++){
			headerNames.add(sampleFrame.levels[i].name);
		}
		headerNames.add(name);
		headerNames.add("Outcome"); 
		if(hasStrata){headerNames.add("Stratum");}
		if(hasSize){headerNames.add("Size");}
		if(hasCost){headerNames.add("Cost");}

		numCols=headerNames.size();
		headers=new String[numCols];
		numRows=0;
		data=new String[numRows][numCols];
		strataCol=-1; costCol=-1; ppsCol=-1;
		for(int c=0; c<numCols; c++){
			headers[c]=headerNames.get(c);
		}
	}
	
	public void validate(SampleFrame sampleFrame, int levelIndex, ArrayList<String> errors){
		mapColumns();
		
		if(numRows==0){
			errors.add(name+": No sample units entered");
		}
		
		//ensure sample paths are valid
		samplePaths=new ArrayList<String>();
		if(levelIndex==0){
			for(int i=0; i<numRows; i++){
				String curName=data[i][0];
				if(samplePaths.contains(curName)){
					errors.add(name+", Row "+(i+1)+": Duplicate entry ("+curName+")");
				}
				else{
					samplePaths.add(curName);
				}
			}
		}
		else{ 
			for(int i=0; i<numRows; i++){
				//check parent path
				String path=data[i][0];
				for(int j=1; j<levelIndex; j++){
					path+=":"+data[i][j];
				}
				if(!sampleFrame.levels[levelIndex-1].samplePaths.contains(path)){
					errors.add(name+", Row "+(i+1)+": Invalid sample path ("+path+")");
				}
				else{
					//check full path
					path+=":"+data[i][levelIndex];
					if(samplePaths.contains(path)){
						errors.add(name+", Row "+(i+1)+": Duplicate entry ("+path+")");
					}
					else{
						samplePaths.add(path);
					}
				}
			}
		}
		
		//ensure outcomes are valid
		for(int i=0; i<numRows; i++){
			String curOutcome=data[i][outcomeCol].replaceAll(",", ""); //strip commas
			if(curOutcome==null || curOutcome.isEmpty()){
				errors.add(name+", Row "+(i+1)+": Outcome is blank");
			}
			else{ //ensure is valid number
				try{
					double test=Double.parseDouble(curOutcome);
					if(test<0){
						errors.add(name+", Row "+(i+1)+": Invalid outcome entered ("+curOutcome+")");
					}
				}
				catch(NumberFormatException e){
					errors.add(name+", Row "+(i+1)+": Invalid outcome entered ("+curOutcome+")");
				}
			}
		}
		//ensure strata are entered if selected
		strata=new ArrayList<String>();
		if(hasStrata){
			for(int i=0; i<numRows; i++){
				String curStratum=data[i][strataCol];
				if(curStratum==null || curStratum.isEmpty()){
					errors.add(name+", Row "+(i+1)+": Stratum is blank");
				}
				if(!strata.contains(curStratum)){
					strata.add(curStratum);
				}
			}
		}
		else{
			strata.add("[Overall]");
		}
		numStrata=strata.size();
		
		//ensure size is valid number if selected
		if(hasSize){
			for(int i=0; i<numRows; i++){
				String curSize=data[i][ppsCol].replaceAll(",", ""); //strip commas
				if(curSize==null || curSize.isEmpty()){
					errors.add(name+", Row "+(i+1)+": Size is blank");
				}
				else{ //ensure is valid number
					try{
						double test=Double.parseDouble(curSize);
						if(test<0){
							errors.add(name+", Row "+(i+1)+": Invalid size entered ("+curSize+")");
						}
					}
					catch(NumberFormatException e){
						errors.add(name+", Row "+(i+1)+": Invalid size entered ("+curSize+")");
					}
				}
			}
		}
		//ensure cost is valid if selected
		if(hasCost){
			for(int i=0; i<numRows; i++){
				String curCost=data[i][costCol].replaceAll(",", ""); //strip commas
				if(curCost==null || curCost.isEmpty()){
					errors.add(name+", Row "+(i+1)+": Cost is blank");
				}
				else{ //ensure is valid number
					try{
						double test=Double.parseDouble(curCost);
						if(test<0){
							errors.add(name+", Row "+(i+1)+": Invalid cost entered ("+curCost+")");
						}
					}
					catch(NumberFormatException e){
						errors.add(name+", Row "+(i+1)+": Invalid cost entered ("+curCost+")");
					}
				}
			}
		}
		
	}
	
	private void mapColumns(){
		for(int c=0; c<numCols; c++){
			if(headers[c].matches("Outcome")){outcomeCol=c;}
			else if(headers[c].matches("Stratum")){strataCol=c;}
			else if(headers[c].matches("Size")){ppsCol=c;}
			else if(headers[c].matches("Cost")){costCol=c;}
		}
	}
	
	
	public int getColIndex(String colName){
		int index=-1;
		int i=0;
		while(index==-1 && i<numCols){
			if(headers[i].matches(colName)){index=i;}
			i++;
		}
		return(index);
	}
	
	public double[] getColumn(int colIndex){
		double col[]=new double[numRows];
		for(int r=0; r<numRows; r++){
			col[r]=Double.parseDouble(data[r][colIndex]);
		}
		return(col);
	}
}