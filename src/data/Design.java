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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/

@XmlRootElement(name="Design")
public class Design {
	//data
	@XmlElement public int numLevels;
	@XmlElement public boolean pps[];
	@XmlElement public boolean stratified[];
	@XmlElement public boolean sampleInt[][];
	@XmlElement public int sampleNums[][];
	@XmlElement public double samplePercents[][];
	@XmlElement int floor[][], ceiling[][];
	//transient
	@XmlTransient String tagComma;
	@XmlTransient public String tagTab;
	
	public Design(int numLevels, int numLevelStrata[]){
		this.numLevels=numLevels;
		pps=new boolean[numLevels];
		stratified=new boolean[numLevels];
		sampleInt=new boolean[numLevels][];
		sampleNums=new int[numLevels][];
		samplePercents=new double[numLevels][];
		floor=new int[numLevels][];
		ceiling=new int[numLevels][];
		for(int l=0; l<numLevels; l++){
			sampleInt[l]=new boolean[numLevelStrata[l]];
			sampleNums[l]=new int[numLevelStrata[l]];
			samplePercents[l]=new double[numLevelStrata[l]];
			floor[l]=new int[numLevelStrata[l]];
			ceiling[l]=new int[numLevelStrata[l]];
		}
	}
	
	public Design(){ //no-arg constructor
		
	}
	
	public void buildTag(){
		//build tag
		tagComma="";
		tagTab="";
		//Sample design
		for(int i=0; i<pps.length; i++){ //level
			tagComma+=",";
			tagTab+="\t";
			tagComma+=stratified[i]+",";
			tagTab+=stratified[i]+"\t";
			tagComma+=pps[i]+",";
			tagTab+=pps[i]+"\t";
			//units
			if(sampleInt[i][0]){ //int
				tagComma+=sampleNums[i][0];
				tagTab+=sampleNums[i][0];
			}
			else{ //percent
				tagComma+=samplePercents[i][0];
				tagTab+=samplePercents[i][0];
			}
			//Strata
			for(int s=1; s<sampleInt[i].length; s++){ //strata
				if(sampleInt[i][s]){ //int
					tagComma+="-"+sampleNums[i][s];
					tagTab+="-"+sampleNums[i][s];
				}
				else{ //percent
					tagComma+="-"+samplePercents[i][s];
					tagTab+="-"+samplePercents[i][s];
				}
			}
		}
	}
}