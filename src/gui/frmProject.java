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
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.awt.Toolkit;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import data.Level;
import data.Project;
import javax.swing.JButton;
import javax.swing.JDialog;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.awt.event.ActionEvent;

/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/

public class frmProject{

	public JDialog frmProject;
	private JTextField textName;
	DefaultTableModel modelLevels;
	private JTable tableLevels;
	
	Project curProject;
	frmMain mainForm;
	
	/**
	 * Constructor
	 */
	public frmProject(Project curProject1, boolean newProject, frmMain mainForm1) {
		curProject=curProject1;
		mainForm=mainForm1;
		initialize();
		if(newProject==false){
			getData();
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmProject = new JDialog();
		frmProject.setAlwaysOnTop(true);
		
		//frmProject.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		frmProject.setResizable(false);
		frmProject.setIconImage(Toolkit.getDefaultToolkit().getImage(frmProject.class.getResource("/images/EPIC-Logo.png")));
		frmProject.setTitle("Project Settings");
		frmProject.setBounds(100, 100, 453, 276);
		frmProject.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmProject.getContentPane().setLayout(null);
		
		JLabel lblSampleFrameName = new JLabel("Project Name:");
		lblSampleFrameName.setBounds(17, 22, 130, 16);
		frmProject.getContentPane().add(lblSampleFrameName);
		
		textName = new JTextField();
		textName.setBounds(147, 16, 280, 28);
		frmProject.getContentPane().add(textName);
		textName.setColumns(10);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(17, 83, 410, 106);
		frmProject.getContentPane().add(scrollPane);
		
		modelLevels=new DefaultTableModel(
				new Object[][] {
					{"1", "Region",false,false,false},
					{"2", "District",false,false,false},
					{"3", "Facility",false,false,false},
				},
				new String[] {
					"Level", "Name","Strata","Size","Cost"
				}
			) {
				boolean[] columnEditables = new boolean[] {
					false, true, true, true, true
				};
				public boolean isCellEditable(int row, int column) {
					return columnEditables[column];
				}
			};
		
		tableLevels = new JTable();
		tableLevels.setModel(modelLevels);
		for(int c=2; c<5; c++){
			TableColumn tc=tableLevels.getColumnModel().getColumn(c);
			tc.setCellEditor(tableLevels.getDefaultEditor(Boolean.class));
			tc.setCellRenderer(tableLevels.getDefaultRenderer(Boolean.class));
		}
		tableLevels.setRowSelectionAllowed(false);
		tableLevels.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableLevels.setShowVerticalLines(true);
		tableLevels.getTableHeader().setReorderingAllowed(false);
		scrollPane.setViewportView(tableLevels);
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean valid=true;
				//get frame name
				String name=textName.getText();
				if(name.isEmpty()){
					valid=false;
					JOptionPane.showMessageDialog(frmProject,"Please enter a name for the project!");
				}
				
				//validate level names
				ArrayList<String> names=new ArrayList<String>();
				int numRows=tableLevels.getRowCount();
				for(int r=0; r<numRows; r++){
					String curName=(String)tableLevels.getValueAt(r, 1);
					if(curName==null || curName.isEmpty()){
						valid=false;
						JOptionPane.showMessageDialog(frmProject,"Please enter a name for Level "+(r+1)+"!");
					}
					else{
						if(names.contains(curName)){
							valid=false;
							JOptionPane.showMessageDialog(frmProject,"Duplicate level names ("+(curName)+")!");
						}
						else{
							names.add(curName);
						}
					}
				}
				//warn that data will be lost if go ahead
				
				
				//save project
				if(valid==true){
					curProject.name=name;
					
					//Sample frame
					int numLevels=names.size();
					curProject.sampleFrame.numLevels=numLevels;
					Level oldLevels[]=curProject.sampleFrame.levels;
					int numOldLevels=oldLevels.length;
					curProject.sampleFrame.levels=new Level[numLevels];
					for(int i=0; i<curProject.sampleFrame.numLevels; i++){
						curProject.sampleFrame.levels[i]=new Level();
						Level curLevel=curProject.sampleFrame.levels[i];
						if(i<numOldLevels){ //re-point data
							curLevel.headers=oldLevels[i].headers;
							curLevel.data=oldLevels[i].data;
						}
						curLevel.name=names.get(i);
						curLevel.hasStrata=(boolean) tableLevels.getValueAt(i, 2);
						curLevel.hasSize=(boolean) tableLevels.getValueAt(i, 3);
						curLevel.hasCost=(boolean) tableLevels.getValueAt(i, 4);
						curLevel.updateColumns(curProject.sampleFrame,i);
					}
					
					mainForm.listenTableChanges=false;
					mainForm.displayProject();
					mainForm.changesMade();				
					mainForm.listenTableChanges=true;
					
					frmProject.dispose();
				}
			}
		});
		btnSave.setBounds(104, 195, 90, 28);
		frmProject.getContentPane().add(btnSave);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmProject.dispose();
			}
		});
		btnCancel.setBounds(255, 195, 90, 28);
		frmProject.getContentPane().add(btnCancel);
		
		JButton btnAddLevel = new JButton("Add Level");
		btnAddLevel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int selected=tableLevels.getSelectedRow();
				if(selected==-1){ //append to end
					modelLevels.addRow(new Object[]{null});
				}
				else{ //insert below
					modelLevels.insertRow(selected,new Object[]{null});
				}
				int numRows=modelLevels.getRowCount();
				for(int r=0; r<numRows; r++){
					modelLevels.setValueAt(r+1, r, 0);
				}
			}
		});
		btnAddLevel.setBounds(104, 50, 112, 28);
		frmProject.getContentPane().add(btnAddLevel);
		
		JButton btnRemoveLevel = new JButton("Remove Level");
		btnRemoveLevel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selected=tableLevels.getSelectedRow();
				int numRows=modelLevels.getRowCount();
				if(numRows>1){
					if(selected==-1){ //remove last
						modelLevels.removeRow(numRows-1);
					}
					else{ //remove selected
						modelLevels.removeRow(selected);
					}
					numRows=modelLevels.getRowCount();
					for(int r=0; r<numRows; r++){
						modelLevels.setValueAt(r+1, r, 0);
					}
				}
			}
		});
		btnRemoveLevel.setBounds(228, 50, 112, 28);
		frmProject.getContentPane().add(btnRemoveLevel);
		

	}
	
	private void getData(){
		textName.setText(curProject.name);
		
		//Sample frame
		modelLevels.setRowCount(0);
		for(int i=0; i<curProject.sampleFrame.numLevels; i++){
			modelLevels.addRow(new Object[]{null});
			modelLevels.setValueAt((i+1), i, 0);
			Level curLevel=curProject.sampleFrame.levels[i];
			modelLevels.setValueAt(curLevel.name, i, 1);
			modelLevels.setValueAt(curLevel.hasStrata, i, 2);
			modelLevels.setValueAt(curLevel.hasSize, i, 3);
			modelLevels.setValueAt(curLevel.hasCost, i, 4);
		}
	}
}


