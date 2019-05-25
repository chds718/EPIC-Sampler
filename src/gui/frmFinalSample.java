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
import java.awt.Dialog.ModalityType;
import java.awt.datatransfer.Clipboard;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;

import data.DataTransferable;
import data.ErrorLog;
import data.FinalSampUnit;
import data.Project;
import data.SampleDesign;
import data.SampleFrame;
import filters.CSVFilter;

import javax.swing.ImageIcon;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTable;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.awt.event.ActionEvent;

/**
 * @author Zachary J. Ward (zward@hsph.harvard.edu)
 * @version 2.0
 */

public class frmFinalSample {
	Project curProject;
	SampleFrame curFrame;
	SampleDesign curDesign;
	ErrorLog errorLog;
	
	public JDialog frmFinalSample;
	DefaultTableModel modelSample;
	private JTable tableSample;

	/**
	 *  Default Constructor
	 */
	public frmFinalSample(Project curProject, SampleDesign curDesign, ErrorLog errorLog) {
		this.curProject=curProject;
		this.curFrame=curProject.sampleFrame;
		this.curDesign=curDesign;
		this.errorLog=errorLog;
		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		frmFinalSample = new JDialog();
		frmFinalSample.setModalityType(ModalityType.APPLICATION_MODAL);
		frmFinalSample.setTitle("EPIC Sampler - Final Sample");
		frmFinalSample.setBounds(100, 100, 1203, 500);
		frmFinalSample.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{539, 631, 0};
		gridBagLayout.rowHeights = new int[]{24, 429, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		frmFinalSample.getContentPane().setLayout(gridBagLayout);

		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane_1.gridheight = 2;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 0;
		frmFinalSample.getContentPane().add(scrollPane_1, gbc_scrollPane_1);

		PanelFinalSample panelGraph = new PanelFinalSample();
		panelGraph.drawGraph(curProject.name,curDesign.finalSample);
		scrollPane_1.setViewportView(panelGraph);

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		GridBagConstraints gbc_toolBar = new GridBagConstraints();
		gbc_toolBar.anchor = GridBagConstraints.NORTHWEST;
		gbc_toolBar.insets = new Insets(0, 0, 5, 0);
		gbc_toolBar.gridx = 1;
		gbc_toolBar.gridy = 0;
		frmFinalSample.getContentPane().add(toolBar, gbc_toolBar);

		JButton btnCopy = new JButton("Copy");
		btnCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int numRows=modelSample.getRowCount();
				int numCol=modelSample.getColumnCount();
				String data[][]=new String[numRows+1][numCol];
				//Get headers
				for(int c=0; c<numCol; c++){
					data[0][c]=modelSample.getColumnName(c);
				}
				//Get row
				for(int r=0; r<numRows; r++){
					for(int c=0; c<numCol; c++){
						data[r+1][c]=modelSample.getValueAt(r, c)+"";
					}
				}
				
				Clipboard clip=Toolkit.getDefaultToolkit().getSystemClipboard();
				clip.setContents(new DataTransferable(data), null);
			}
		});
		btnCopy.setIcon(new ImageIcon(frmMain.class.getResource("/images/copy_16.png")));
		toolBar.add(btnCopy);

		JButton btnExport = new JButton("Export");
		btnExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser fc=new JFileChooser();
					fc.setDialogTitle("Export Sample");
					fc.setApproveButtonText("Export");
					fc.setFileFilter(new CSVFilter());

					int returnVal = fc.showSaveDialog(frmFinalSample);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						frmFinalSample.setCursor(new Cursor(Cursor.WAIT_CURSOR));
						File file = fc.getSelectedFile();
						String path=file.getAbsolutePath();
						path=path.replaceAll(".csv", "");
						//Open file for writing
						FileWriter fstream = new FileWriter(path+".csv"); //Create new file
						BufferedWriter out = new BufferedWriter(fstream);
						
						int numRows=modelSample.getRowCount();
						int numCols=modelSample.getColumnCount();
						//Headers
						for(int i=0; i<numCols-1; i++){
							out.write(modelSample.getColumnName(i)+",");
						}
						out.write(modelSample.getColumnName(numCols-1));
						out.newLine();
						//Data
						for(int i=0; i<numRows; i++){
							for(int j=0; j<numCols-1; j++){
								out.write(modelSample.getValueAt(i, j)+",");
							}
							out.write(modelSample.getValueAt(i, numCols-1)+"");
							out.newLine();
						}
						out.close();
												
						//Open file
						Desktop dt = Desktop.getDesktop();
						dt.open(new File(path+".csv"));
						frmFinalSample.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}

				}catch(Exception er){
					frmFinalSample.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					JOptionPane.showMessageDialog(frmFinalSample,er.getMessage());
					errorLog.recordError(er);
				}
				
			}
		});
		btnExport.setIcon(new ImageIcon(frmMain.class.getResource("/images/export.png")));
		toolBar.add(btnExport);

		JLabel lblGnuGeneralPublic = new JLabel("GNU General Public License:");
		GridBagConstraints gbc_lblGnuGeneralPublic = new GridBagConstraints();
		gbc_lblGnuGeneralPublic.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblGnuGeneralPublic.insets = new Insets(0, 0, 0, 5);
		gbc_lblGnuGeneralPublic.gridx = 0;
		gbc_lblGnuGeneralPublic.gridy = 1;
		frmFinalSample.getContentPane().add(lblGnuGeneralPublic, gbc_lblGnuGeneralPublic);

		modelSample=new DefaultTableModel(
				new Object[][] {},
				new String[] {}
				) {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 1;
		frmFinalSample.getContentPane().add(scrollPane, gbc_scrollPane);

		tableSample = new JTable();
		tableSample.setModel(modelSample);
		tableSample.setShowVerticalLines(true);
		tableSample.setRowSelectionAllowed(false);
		scrollPane.setViewportView(tableSample);

		for(int i=0; i<curFrame.numLevels; i++){
			modelSample.addColumn(curFrame.levels[i].name);
		}
		modelSample.addColumn("Sample Weight");

		buildTable(curDesign.finalSample,null);


	}

	private void buildTable(FinalSampUnit curUnit, ArrayList<String> path){
		ArrayList<String> curPath=new ArrayList<String>();
		if(path!=null){
			for(int i=0; i<path.size(); i++){
				curPath.add(path.get(i));
			}
		}
		curPath.add(curUnit.name+" ("+curUnit.stratum+")");

		int numChildren=curUnit.children.size();
		if(numChildren==0){ //last level
			modelSample.addRow(new Object[]{null});
			int r=modelSample.getRowCount()-1;
			for(int i=0; i<curFrame.numLevels; i++){
				modelSample.setValueAt(curPath.get(i+1), r, i);
			}

			modelSample.setValueAt(Math.round(curUnit.ipw*100)/100.0, r, curFrame.numLevels);
		}
		else{
			for(int i=0; i<numChildren; i++){
				buildTable(curUnit.children.get(i),curPath);
			}
		}
	}
}
