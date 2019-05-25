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

/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/

@XmlRootElement(name="FinalSampUnit")
public class FinalSampUnit {
	//data
	@XmlElement public String name;
	@XmlElement	public String stratum;
	@XmlElement public double ipw; // Inverse-probability weight
	@XmlElement (name="FinalSampUnit", type=FinalSampUnit.class) public ArrayList<FinalSampUnit> children;
	
	
	public FinalSampUnit(SampUnit orig){
		this.name=orig.name;
		this.stratum=orig.stratum;
		this.ipw=orig.ipw;
		children=new ArrayList<FinalSampUnit>();
	}
	
	public FinalSampUnit(){ //no-arg constructor
		
	}

}