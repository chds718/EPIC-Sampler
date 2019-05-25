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
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import data.Level;
import data.SampleFrame;

/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/

public class LevelTable extends JTable{
	LevelTable thisTable=this;
	SampleFrame frame;
	DefaultTableModel modelUnitBounds, modelUnitCosts;
	
	public void setFrame(SampleFrame frame){
		this.frame=frame;
	}
	
	public void setUnitModels(DefaultTableModel modelUnitBounds, DefaultTableModel modelUnitCosts){
		this.modelUnitBounds=modelUnitBounds;
		this.modelUnitCosts=modelUnitCosts;
	}
	
	@Override
	public TableCellEditor getCellEditor(int row, int column){
		Level curLevel=frame.levels[row];
		
		if(column==1){ //Sampling type
			final JComboBox<String> combo = new JComboBox<String>();
			combo.addItem("Simple");
			if(curLevel.hasStrata){
				combo.addItem("Stratified");
				combo.addItem("Simple OR Stratified");
			}
			combo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int selected=combo.getSelectedIndex();
					updateUnitModels(row,selected);
				}
			});
			
			return(new DefaultCellEditor(combo));
		}
		else if(column==2){ // Sampling probability
			final JComboBox<String> combo = new JComboBox<String>();
			combo.addItem("Equal");
			if(curLevel.hasSize){
				combo.addItem("Proportional to size (PPS)");
				combo.addItem("Equal OR PPS");
			}
			return(new DefaultCellEditor(combo));
		}
		return(null);
	}
	
	private void updateUnitModels(int level, int type){
		String levelName=frame.levels[level].name;
		boolean hasCost=frame.levels[level].hasCost;
		int index=-1;
		int rowCount=modelUnitBounds.getRowCount();
		int r=0;
		while(index==-1 && r<rowCount){
			String curName=(String) modelUnitBounds.getValueAt(r, 0);
			if(curName.equals(levelName)){
				index=r;
			}
			r++;
		}
		
		//remove all rows for current level
		for(r=0; r<rowCount; r++){
			String curName=(String) modelUnitBounds.getValueAt(r, 0);
			if(curName.equals(levelName)){
				modelUnitBounds.removeRow(r);
				modelUnitCosts.removeRow(r);
				r--;
				rowCount--;
			}
		}
		
		if(type==0){ //Simple
			modelUnitBounds.insertRow(index, new Object[]{null});
			modelUnitBounds.setValueAt(levelName, index, 0);
			modelUnitBounds.setValueAt("[Overall]", index, 1);
			//costs
			modelUnitCosts.insertRow(index, new Object[]{null});
			modelUnitCosts.setValueAt(levelName, index, 0);
			modelUnitCosts.setValueAt("[Overall]", index, 1);
			modelUnitCosts.setValueAt(0+"", index, 2);
			if(hasCost){
				modelUnitCosts.setValueAt("Unit-Specific", index, 3);
				modelUnitCosts.setValueAt("---", index, 4);
			}
			else{
				modelUnitCosts.setValueAt("Average", index, 3);
			}
		}
		else if(type==1){ //Stratified
			int numStrata=frame.levels[level].numStrata;
			for(int s=0; s<numStrata; s++){
				modelUnitBounds.insertRow(index+s, new Object[]{null});
				modelUnitBounds.setValueAt(levelName, index+s, 0);
				modelUnitBounds.setValueAt(frame.levels[level].strata.get(s), index+s, 1);
				//costs
				modelUnitCosts.insertRow(index+s, new Object[]{null});
				modelUnitCosts.setValueAt(levelName, index+s, 0);
				modelUnitCosts.setValueAt(frame.levels[level].strata.get(s), index+s, 1);
				modelUnitCosts.setValueAt(0+"", index+s, 2);
				if(hasCost){
					modelUnitCosts.setValueAt("Unit-Specific", index+s, 3);
					modelUnitCosts.setValueAt("---", index+s, 4);
				}
				else{
					modelUnitCosts.setValueAt("Average", index+s, 3);
				}
			}
		}
		else if(type==2){ //Both
			modelUnitBounds.insertRow(index, new Object[]{null});
			modelUnitBounds.setValueAt(levelName, index, 0);
			modelUnitBounds.setValueAt("[Overall]", index, 1);
			//costs
			modelUnitCosts.insertRow(index, new Object[]{null});
			modelUnitCosts.setValueAt(levelName, index, 0);
			modelUnitCosts.setValueAt("[Overall]", index, 1);
			modelUnitCosts.setValueAt(0+"", index, 2);
			if(hasCost){
				modelUnitCosts.setValueAt("Unit-Specific", index, 3);
				modelUnitCosts.setValueAt("---", index, 4);
			}
			else{
				modelUnitCosts.setValueAt("Average", index, 3);
			}
			
			index++;
			int numStrata=frame.levels[level].numStrata;
			for(int s=0; s<numStrata; s++){
				modelUnitBounds.insertRow(index+s, new Object[]{null});
				modelUnitBounds.setValueAt(levelName, index+s, 0);
				modelUnitBounds.setValueAt(frame.levels[level].strata.get(s), index+s, 1);
				//costs
				modelUnitCosts.insertRow(index+s, new Object[]{null});
				modelUnitCosts.setValueAt(levelName, index+s, 0);
				modelUnitCosts.setValueAt(frame.levels[level].strata.get(s), index+s, 1);
				modelUnitCosts.setValueAt(0+"", index+s, 2);
				if(hasCost){
					modelUnitCosts.setValueAt("Unit-Specific", index+s, 3);
					modelUnitCosts.setValueAt("---", index+s, 4);
				}
				else{
					modelUnitCosts.setValueAt("Average", index+s, 3);
				}
			}
		}
		
		
	}
}