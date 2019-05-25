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
import java.io.InputStream;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/
public class HtmlSelection implements Transferable{
	
	private static List<DataFlavor> htmlFlavors = new ArrayList<>(3);

    static {

        try {
            htmlFlavors.add(new DataFlavor("text/html;class=java.lang.String"));
            htmlFlavors.add(new DataFlavor("text/html;class=java.io.Reader"));
            htmlFlavors.add(new DataFlavor("text/html;charset=unicode;class=java.io.InputStream"));
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

    }

    private String html;

    public HtmlSelection(String html) {
        this.html = html;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return (DataFlavor[]) htmlFlavors.toArray(new DataFlavor[htmlFlavors.size()]);
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return htmlFlavors.contains(flavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (String.class.equals(flavor.getRepresentationClass())) {
            return html;
        } else if (Reader.class.equals(flavor.getRepresentationClass())) {
            return new StringReader(html);
        } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
            return new StringBufferInputStream(html);
        }
        throw new UnsupportedFlavorException(flavor);
    }

}