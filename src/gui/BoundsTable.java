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

package gui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/

public class BoundsTable extends JTable{
	BoundsTable thisTable=this;
	
	
	@Override
	public TableCellEditor getCellEditor(int row, int column){
		if(column==2){ //Bound type
			final JComboBox<String> combo = new JComboBox<String>(new DefaultComboBoxModel<String>(new String[]{"Count","Proportion"}));
			
			combo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int selected=combo.getSelectedIndex();
					if(selected==0){ //Count
						for(int i=5; i<8; i++){
							thisTable.setValueAt("---",row, i);
						}
					}
					else{ //Proportion
						for(int i=5; i<8; i++){
							thisTable.setValueAt("",row, i);
						}
					}
				}
			});
			
			return(new DefaultCellEditor(combo));
		}
		
		return super.getCellEditor(row, column);
	}
	
	
}