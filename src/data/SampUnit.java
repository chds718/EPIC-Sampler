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

/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/

public class SampUnit {
	String name;
	String stratum;
	ArrayList<SampUnit> children[];
	public double ipw; // Inverse-probability weight
		
	int level;
	int stratumIndex;
	double cost; //Cost of visiting this unit
	double outcome; //Cost/doses/etc.
	double pps; //Population/doses/etc.
	
	double childWeights[][];
	double totalChildWeights[];
	
	int numSampled;
	public String treePath;
	
	public SampUnit(String name, int numStrata){
		//this.id=id;
		this.name=name;
		children=new ArrayList[numStrata];
		for(int s=0; s<numStrata; s++){
			children[s]=new ArrayList<SampUnit>();
		}
	}

	public void addChild(SampUnit child, int stratum){
		children[stratum].add(child);
	}

}