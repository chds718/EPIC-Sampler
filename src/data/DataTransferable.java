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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/

public class DataTransferable implements Transferable
{
   public DataTransferable(String data[][]){
	   //Build string
	   strData=""; //empty string
	   int numRow=data.length;
	   int numCol=data[0].length;
	   for(int r=0; r<numRow; r++){
		   for(int c=0; c<numCol; c++){
			   strData+=data[r][c]+"\t";
		   }
		   strData+="\n";
	   }
   }
   public DataFlavor[] getTransferDataFlavors(){return new DataFlavor[] { DataFlavor.stringFlavor };}
   public boolean isDataFlavorSupported(DataFlavor flavor){return flavor.equals(DataFlavor.stringFlavor);}
   public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException{
      if (flavor.equals(DataFlavor.stringFlavor)){return strData;}
      else{throw new UnsupportedFlavorException(flavor);}
   }
   private String strData;
}

	
	
