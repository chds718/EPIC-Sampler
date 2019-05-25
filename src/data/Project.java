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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/

@XmlRootElement(name="Project")
public class Project {

	@XmlElement public String name;
	@XmlElement(name="SampleFrame", type=SampleFrame.class) public SampleFrame sampleFrame;
	
	
	@XmlTransient public String filepath=null;
	@XmlTransient public ErrorLog errorLog;
	
	public Project(){
		//default project
		sampleFrame=new SampleFrame();
	}

	
	public void save(){
		try{

			JAXBContext context = JAXBContext.newInstance(Project.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			// Write to File
			FileOutputStream fstreamO=new FileOutputStream(filepath);
			m.marshal(this,fstreamO);
			fstreamO.close();
			//FileWriter fstreamO=new FileWriter(filepath);
			//BufferedWriter out=new BufferedWriter(fstreamO);
			//m.marshal(this,out);
			//out.close();
		
		}catch(Exception e){
			e.printStackTrace();
			errorLog.recordError(e);
		}
	}
	
}