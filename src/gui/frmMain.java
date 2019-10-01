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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import data.Constraints;
import data.DataTransferable;
import data.Design;
import data.ErrorLog;
import data.HtmlSelection;
import data.Level;
import data.Project;
import data.RecentFiles;
import data.SampleDesign;
import data.Scheduler;
import data.SearchResults;
import filters.CSVFilter;
import filters.EPICProjectFilter;
import filters.EPICResultsFilter;

import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;

import java.awt.Insets;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import java.awt.Font;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JTextPane;
import javax.swing.border.EtchedBorder;
import javax.swing.JTextField;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartMouseEvent;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/

public class frmMain {

	JFrame frmMain;

	/**
	 * Main form of the application
	 */
	static frmMain mainForm;
	
	public Project curProject;
	
	JTabbedPane tabbedPane;
	JFileChooser fc;
	String version="2.0.2";
	
	//Sample frame input
	int indexLevel=-1;
	boolean listenTableChanges=true;
	DefaultTableModel modelLevel;
	private JTable tableLevel;
	JLabel lblLevelRows;
	JComboBox<String> comboLevel;
	JButton btnImportLevel, btnPasteLevel, btnExportLevel, btnValidateSampleFrame;
		
	//Sample frame report
	JTree treeFrame;
	JTextPane textPaneReport;
	
	//Sample design search
	public LevelTable tableLevelDesign;
	public DefaultTableModel modelLevelDesign;
	public BoundsTable tableUnitBounds;
	public DefaultTableModel modelUnitBounds;
	public CostTable tableUnitCosts;
	public DefaultTableModel modelUnitCosts;
	public JTextField textBudgetMin;
	public JTextField textBudgetMax;
	public JTextField textNumSim;
	public JCheckBox chckbxSeedSimulations;
	public JTextField textSeed;
	public Constraints constraints;
	
	//Search results
	public ArrayList<SampleDesign> results;
	public DefaultTableModel modelSearchResults;
	private JTable tableSearchResults;
	PanelDesign panelDesignGraph;
	DefaultXYDataset chartDataResults, chartDataScatter;
	JFreeChart chartResults, chartScatter;
	JComboBox<String> comboXAxis, comboYAxis;
	int xAxis=0, yAxis=3;
	double dataScatter[][];
	SampleDesign curDesign;
	
	//bottom console
	JTextArea textArea;
	
	boolean unsavedChanges;
	
	public RecentFiles recentFiles;
	ErrorLog errorLog;
	
	JMenuItem mntmEditSampleFrame, mntmSave, mntmSaveAs;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					mainForm=new frmMain();
					mainForm.frmMain.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public frmMain() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmMain = new JFrame();
		frmMain.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		
		frmMain.setIconImage(Toolkit.getDefaultToolkit().getImage(frmMain.class.getResource("/images/EPIC-Logo.png")));
		frmMain.setTitle("EPIC Sampler");
		frmMain.setBounds(100, 100, 900, 500);
		frmMain.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frmMain.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{546, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		frmMain.getContentPane().setLayout(gridBagLayout);
		
		errorLog=new ErrorLog(version);
		fc=new JFileChooser();
		recentFiles=new RecentFiles();
		listenTableChanges=true;
		unsavedChanges=false;
		
		modelLevel=new DefaultTableModel(){
			public boolean isCellEditable(int row, int column) {
				return true;
			}
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if(modelLevel.getRowCount()==0){
					return Object.class;
				}
				return getValueAt(0, columnIndex).getClass();
			}
		};
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.85);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		GridBagConstraints gbc_splitPane = new GridBagConstraints();
		gbc_splitPane.fill = GridBagConstraints.BOTH;
		gbc_splitPane.gridx = 0;
		gbc_splitPane.gridy = 0;
		frmMain.getContentPane().add(splitPane, gbc_splitPane);
		
		JScrollPane scrollPane = new JScrollPane();
		splitPane.setRightComponent(scrollPane);
		
		textArea = new JTextArea();
		//textArea.setLineWrap(true);
		textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		
		textArea.setText("EPIC Sampler v"+version+"\n");
		textArea.append("Copyright © 2018-2019 Center for Health Decision Science, Harvard T.H. Chan School of Public Health\n\n");
		textArea.append("This is free software and comes with ABSOLUTELY NO WARRANTY.\n");
		textArea.append("See Help -> About for distribution details.");
		textArea.setCaretPosition(0);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		splitPane.setLeftComponent(tabbedPane);
		tabbedPane.setEnabled(false);
		
		JPanel panelSampleFrame = new JPanel();
				
		JLabel lblInputs=new JLabel("Sample Frame Inputs");
		//Icon icon=new ImageIcon(frmMain.class.getResource("/images/input.png"));;
		//lblInputs.setIcon(icon);
		lblInputs.setIcon(new ScaledIcon("/images/input",16,16,true));
		lblInputs.setIconTextGap(5);
		lblInputs.setHorizontalTextPosition(SwingConstants.RIGHT);
		
		tabbedPane.addTab("Sample Frame Inputs", null, panelSampleFrame, null);
		tabbedPane.setTabComponentAt(0, lblInputs);
		
		GridBagLayout gbl_panelSampleFrame = new GridBagLayout();
		gbl_panelSampleFrame.columnWidths = new int[]{0, 0};
		gbl_panelSampleFrame.rowHeights = new int[]{30, 0, 0};
		gbl_panelSampleFrame.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panelSampleFrame.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		panelSampleFrame.setLayout(gbl_panelSampleFrame);
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		GridBagConstraints gbc_toolBar = new GridBagConstraints();
		gbc_toolBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_toolBar.insets = new Insets(0, 0, 5, 0);
		gbc_toolBar.gridx = 0;
		gbc_toolBar.gridy = 0;
		panelSampleFrame.add(toolBar, gbc_toolBar);
		
		JLabel lblNewLabel = new JLabel("   ");
		toolBar.add(lblNewLabel);
		
		JLabel lblLevel = new JLabel("Level:  ");
		lblLevel.setIcon(new ScaledIcon("/images/levels",16,16,true));
		toolBar.add(lblLevel);
		
		comboLevel = new JComboBox<String>();
		comboLevel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				listenTableChanges=false;
				indexLevel=comboLevel.getSelectedIndex();
				displayLevel();
				listenTableChanges=true;
			}
		});
		comboLevel.setModel(new DefaultComboBoxModel<String>(new String[] {"Region", "District", "Facility"}));
		comboLevel.setEnabled(false);
		toolBar.add(comboLevel);
		
		lblLevelRows = new JLabel(" 0 rows ");
		lblLevelRows.setHorizontalAlignment(SwingConstants.CENTER);
		toolBar.add(lblLevelRows);
		
		JSeparator separator_4 = new JSeparator();
		separator_4.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(separator_4);
		
		btnImportLevel = new JButton("Import");
		btnImportLevel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					fc.setDialogTitle("Import Level");
					fc.setApproveButtonText("Import");
					fc.setFileFilter(new CSVFilter());

					int returnVal = fc.showOpenDialog(frmMain);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						frmMain.setCursor(new Cursor(Cursor.WAIT_CURSOR));
						File file = fc.getSelectedFile();
						FileInputStream fstream = new FileInputStream(file);
						DataInputStream in = new DataInputStream(fstream);
						BufferedReader br = new BufferedReader(new InputStreamReader(in));
						String strLine;
						//Check that headers match
						strLine=br.readLine(); //Headers
						String headers[]=strLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
						boolean validCols=checkHeaders(headers);
						if(validCols==true){
							//Data
							modelLevel.setRowCount(0);
							strLine=br.readLine(); //First line
							while(strLine!=null){
								String data[]=strLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
								for(int i=0; i<data.length; i++){
									data[i]=data[i].replace("\"", ""); //strip quotation marks
								}
								
								modelLevel.addRow(data);
								strLine=br.readLine();
							}
						}
						saveLevelData();
						changesMade();
						br.close();
						lblLevelRows.setText(" "+modelLevel.getRowCount()+" ");
						frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}

				}catch(Exception er){
					frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					JOptionPane.showMessageDialog(frmMain,er.getMessage());
					errorLog.recordError(er);
				}
			}
		});
		btnImportLevel.setIcon(new ScaledIcon("/images/import",16,16,true));
		btnImportLevel.setDisabledIcon(new ScaledIcon("/images/import",16,16,false));
		btnImportLevel.setEnabled(false);
		toolBar.add(btnImportLevel);
		
		btnPasteLevel = new JButton("Paste");
		btnPasteLevel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					frmMain.setCursor(new Cursor(Cursor.WAIT_CURSOR));
					Clipboard clip=Toolkit.getDefaultToolkit().getSystemClipboard();
					String strData=(String) clip.getData(DataFlavor.stringFlavor);
					String rows[]=strData.split("\n");
					String headers[]=rows[0].split("\t");
					boolean validCols=checkHeaders(headers);
					if(validCols==true){
						int numCols=headers.length;
						int numRows=rows.length;
						modelLevel.setRowCount(rows.length-1);
						for(int r=1; r<numRows; r++){
							String curRow[]=rows[r].split("\t");
							int curNumCols=Math.min(curRow.length, numCols); //check for blanks
							for(int c=0; c<curNumCols; c++){
								modelLevel.setValueAt(curRow[c], r-1, c);
							}
						}
					}
					saveLevelData();
					changesMade();
					lblLevelRows.setText(" "+modelLevel.getRowCount()+" ");
					frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}catch(Exception er){
					frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					JOptionPane.showMessageDialog(frmMain,er.getMessage());
					errorLog.recordError(er);
				}
			}
		});
		btnPasteLevel.setIcon(new ScaledIcon("/images/paste",16,16,true));
		btnPasteLevel.setDisabledIcon(new ScaledIcon("/images/paste",16,16,false));
		btnPasteLevel.setEnabled(false);
		toolBar.add(btnPasteLevel);
		
		btnExportLevel = new JButton("Export");
		btnExportLevel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					fc.setDialogTitle("Export Level");
					fc.setApproveButtonText("Export");
					fc.setFileFilter(new CSVFilter());

					int returnVal = fc.showSaveDialog(frmMain);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						frmMain.setCursor(new Cursor(Cursor.WAIT_CURSOR));
						File file = fc.getSelectedFile();
						String path=file.getAbsolutePath();
						path=path.replaceAll(".csv", "");
						//Open file for writing
						FileWriter fstream = new FileWriter(path+".csv"); //Create new file
						BufferedWriter out = new BufferedWriter(fstream);
						
						int numCols=modelLevel.getColumnCount();
						int numRows=modelLevel.getRowCount();
						//Headers
						for(int i=0; i<numCols-1; i++){
							out.write(modelLevel.getColumnName(i)+",");
						}
						out.write(modelLevel.getColumnName(numCols-1));
						out.newLine();
						//Data
						for(int i=0; i<numRows; i++){
							for(int j=0; j<numCols-1; j++){
								out.write(modelLevel.getValueAt(i, j)+",");
							}
							out.write(modelLevel.getValueAt(i, numCols-1)+"");
							out.newLine();
						}
						out.close();
						
						//JOptionPane.showMessageDialog(frmMain, "Exported!");
												
						//Open file
						Desktop dt = Desktop.getDesktop();
						dt.open(new File(path+".csv"));
						frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}

				}catch(Exception er){
					frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					JOptionPane.showMessageDialog(frmMain,er.getMessage());
					errorLog.recordError(er);
				}
			}
		});
		btnExportLevel.setIcon(new ScaledIcon("/images/export",16,16,true));
		btnExportLevel.setDisabledIcon(new ScaledIcon("/images/export",16,16,false));
		btnExportLevel.setEnabled(false);
		toolBar.add(btnExportLevel);
		
		JSeparator separator_3 = new JSeparator();
		separator_3.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(separator_3);
		
		btnValidateSampleFrame = new JButton("Validate Sample Frame");
		btnValidateSampleFrame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frmMain.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				if(curProject.sampleFrame.validate(textArea,curProject,textPaneReport,treeFrame)){
					tabbedPane.setEnabledAt(1, true);
					tabbedPane.setEnabledAt(2, true);
					tabbedPane.setEnabledAt(3, true);
					
					tabbedPane.setSelectedIndex(1);
				}
				frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
		btnValidateSampleFrame.setIcon(new ScaledIcon("/images/validate",16,16,true));
		btnValidateSampleFrame.setDisabledIcon(new ScaledIcon("/images/validate",16,16,false));
		btnValidateSampleFrame.setEnabled(false);
		toolBar.add(btnValidateSampleFrame);
				
		
		JScrollPane scrollPaneLevel = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		panelSampleFrame.add(scrollPaneLevel, gbc_scrollPane);
		
		tableLevel = new JTable();
		tableLevel.setModel(modelLevel);
		tableLevel.setRowSelectionAllowed(false);
		tableLevel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableLevel.setShowVerticalLines(true);
		tableLevel.getTableHeader().setReorderingAllowed(false);
		tableLevel.setAutoCreateRowSorter(true);
		scrollPaneLevel.setViewportView(tableLevel);
		
		tableLevel.getModel().addTableModelListener(
				new TableModelListener() 
				{
					public void tableChanged(TableModelEvent evt) 
					{
						if(listenTableChanges){
							saveLevelData();
							changesMade();
						}
					}
				});
		
		JLabel lblReport=new JLabel("Sample Frame Report");
		//Icon iconReport=new ImageIcon(frmMain.class.getResource("/images/report.png"));;
		//lblReport.setIcon(iconReport);
		lblReport.setIcon(new ScaledIcon("/images/report",16,16,true));
		lblReport.setIconTextGap(5);
		lblReport.setHorizontalTextPosition(SwingConstants.RIGHT);
		
		JPanel panelSampleFrameReport = new JPanel();
		tabbedPane.addTab("Sample Frame Report", null, panelSampleFrameReport, null);
		tabbedPane.setTabComponentAt(1, lblReport);
		tabbedPane.setEnabledAt(1, false);
		GridBagLayout gbl_panelSampleFrameReport = new GridBagLayout();
		gbl_panelSampleFrameReport.columnWidths = new int[]{324, 0, 0};
		gbl_panelSampleFrameReport.rowHeights = new int[]{0, 0, 0};
		gbl_panelSampleFrameReport.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panelSampleFrameReport.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		panelSampleFrameReport.setLayout(gbl_panelSampleFrameReport);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.gridheight = 2;
		gbc_scrollPane_1.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 0;
		panelSampleFrameReport.add(scrollPane_1, gbc_scrollPane_1);
		
		treeFrame = new JTree();
		treeFrame.setModel(new DefaultTreeModel(
			new DefaultMutableTreeNode("Root") {
				{}
			}
		));
		scrollPane_1.setViewportView(treeFrame);
		
		JToolBar toolBar_1 = new JToolBar();
		toolBar_1.setFloatable(false);
		toolBar_1.setRollover(true);
		GridBagConstraints gbc_toolBar_1 = new GridBagConstraints();
		gbc_toolBar_1.anchor = GridBagConstraints.NORTHWEST;
		gbc_toolBar_1.insets = new Insets(0, 0, 5, 0);
		gbc_toolBar_1.gridx = 1;
		gbc_toolBar_1.gridy = 0;
		panelSampleFrameReport.add(toolBar_1, gbc_toolBar_1);
		
		JButton btnCopyReport = new JButton("Copy");
		btnCopyReport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Clipboard clip=Toolkit.getDefaultToolkit().getSystemClipboard();
				clip.setContents(new HtmlSelection(textPaneReport.getText()), null);
			}
		});
		//btnCopyReport.setIcon(new ImageIcon(frmMain.class.getResource("/images/copy_16.png")));
		btnCopyReport.setIcon(new ScaledIcon("/images/copy",16,16,true));
		toolBar_1.add(btnCopyReport);
		
		JButton btnPrint = new JButton("Print");
		btnPrint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					boolean done=textPaneReport.print();
					if (done) {
                        JOptionPane.showMessageDialog(null, "Printing is done");
                    } else {
                        JOptionPane.showMessageDialog(null, "Error while printing");
                    }
					
				}catch(Exception ex){
					JOptionPane.showMessageDialog(frmMain, "Error: "+ex.toString());
					errorLog.recordError(ex);
				}
				
			}
		});
		//btnPrint.setIcon(new ImageIcon(frmMain.class.getResource("/images/print.png")));
		btnPrint.setIcon(new ScaledIcon("/images/print",16,16,true));
		toolBar_1.add(btnPrint);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_2.gridx = 1;
		gbc_scrollPane_2.gridy = 1;
		panelSampleFrameReport.add(scrollPane_2, gbc_scrollPane_2);
		
		textPaneReport = new JTextPane();
		textPaneReport.setContentType("text/html");
		textPaneReport.setEditable(false);
		scrollPane_2.setViewportView(textPaneReport);
		
		
		JLabel lblSearch=new JLabel("Sample Design Search");
		//Icon iconSearch=new ImageIcon(frmMain.class.getResource("/images/search.png"));;
		//lblSearch.setIcon(iconSearch);
		lblSearch.setIcon(new ScaledIcon("/images/search",16,16,true));
		lblSearch.setIconTextGap(5);
		lblSearch.setHorizontalTextPosition(SwingConstants.RIGHT);
				
		JPanel panelSampleDesignSearch = new JPanel();
		tabbedPane.addTab("Sample Design Search", null, panelSampleDesignSearch, null);
		GridBagLayout gbl_panelSampleDesignSearch = new GridBagLayout();
		gbl_panelSampleDesignSearch.columnWidths = new int[]{0, 0};
		gbl_panelSampleDesignSearch.rowHeights = new int[]{0, 0};
		gbl_panelSampleDesignSearch.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panelSampleDesignSearch.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panelSampleDesignSearch.setLayout(gbl_panelSampleDesignSearch);
		
		modelLevelDesign=new DefaultTableModel(
				new Object[][] {},
				new String[] {"Level", "Sampling Type", "Sampling Probability"}
				) {
			boolean[] columnEditables = new boolean[] {
					false, true, true
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		};


		modelUnitBounds=new DefaultTableModel(
				new Object[][] {},
				new String[] {"Level", "Stratum", "Search Type", "Min", "Max", "Step Size (optional)", "Floor (optional)", "Ceiling (optional)"}
				) {
			boolean[] columnEditables = new boolean[] {
					false, false, true, true, true, true, true, true
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		};

		modelUnitCosts=new DefaultTableModel(
				new Object[][] {},
				new String[] {"Level", "Stratum","Fixed Cost","Variable Cost Type","Variable Cost"}
				) {
			boolean[] columnEditables = new boolean[] {
					false, false, true, true, true
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		};
		
		JPanel panelSearchConstraints = new JPanel();
		GridBagConstraints gbc_panelSearchConstraints = new GridBagConstraints();
		gbc_panelSearchConstraints.fill = GridBagConstraints.BOTH;
		gbc_panelSearchConstraints.gridx = 0;
		gbc_panelSearchConstraints.gridy = 0;
		panelSampleDesignSearch.add(panelSearchConstraints, gbc_panelSearchConstraints);
		panelSearchConstraints.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagLayout gbl_panelSearchConstraints = new GridBagLayout();
		gbl_panelSearchConstraints.columnWidths = new int[]{0, 49, 0, 0, 80, 0, 80, 0, 0};
		gbl_panelSearchConstraints.rowHeights = new int[]{0, 0, 100, 0, 0, 0, 0, 0, 0, 0};
		gbl_panelSearchConstraints.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panelSearchConstraints.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		panelSearchConstraints.setLayout(gbl_panelSearchConstraints);
		
		JLabel lblSearchConstraints = new JLabel("Search Constraints");
		lblSearchConstraints.setFont(new Font("Dialog", Font.BOLD, 12));
		GridBagConstraints gbc_lblSearchConstraints = new GridBagConstraints();
		gbc_lblSearchConstraints.gridwidth = 8;
		gbc_lblSearchConstraints.insets = new Insets(0, 0, 5, 0);
		gbc_lblSearchConstraints.gridx = 0;
		gbc_lblSearchConstraints.gridy = 0;
		panelSearchConstraints.add(lblSearchConstraints, gbc_lblSearchConstraints);
		
		JLabel lblLevelSampling = new JLabel("Level Sampling");
		GridBagConstraints gbc_lblLevelSampling = new GridBagConstraints();
		gbc_lblLevelSampling.gridwidth = 8;
		gbc_lblLevelSampling.insets = new Insets(0, 0, 5, 0);
		gbc_lblLevelSampling.gridx = 0;
		gbc_lblLevelSampling.gridy = 1;
		panelSearchConstraints.add(lblLevelSampling, gbc_lblLevelSampling);
		
		JScrollPane scrollPane_3 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_3 = new GridBagConstraints();
		gbc_scrollPane_3.gridwidth = 8;
		gbc_scrollPane_3.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_3.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_3.gridx = 0;
		gbc_scrollPane_3.gridy = 2;
		panelSearchConstraints.add(scrollPane_3, gbc_scrollPane_3);
		
		tableLevelDesign = new LevelTable();
		tableLevelDesign.setModel(modelLevelDesign);
		tableLevelDesign.setRowSelectionAllowed(false);
		tableLevelDesign.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableLevelDesign.setShowVerticalLines(true);
		tableLevelDesign.getTableHeader().setReorderingAllowed(false);
		
		scrollPane_3.setViewportView(tableLevelDesign);
		
		JLabel lblNumberOfUnits = new JLabel("Number of Units");
		GridBagConstraints gbc_lblNumberOfUnits = new GridBagConstraints();
		gbc_lblNumberOfUnits.gridwidth = 8;
		gbc_lblNumberOfUnits.insets = new Insets(0, 0, 5, 0);
		gbc_lblNumberOfUnits.gridx = 0;
		gbc_lblNumberOfUnits.gridy = 3;
		panelSearchConstraints.add(lblNumberOfUnits, gbc_lblNumberOfUnits);
		
		JScrollPane scrollPane_4 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_4 = new GridBagConstraints();
		gbc_scrollPane_4.gridwidth = 8;
		gbc_scrollPane_4.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_4.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_4.gridx = 0;
		gbc_scrollPane_4.gridy = 4;
		panelSearchConstraints.add(scrollPane_4, gbc_scrollPane_4);
		
		tableUnitBounds = new BoundsTable();
		tableUnitBounds.setModel(modelUnitBounds);
		tableUnitBounds.setRowSelectionAllowed(false);
		tableUnitBounds.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableUnitBounds.setShowVerticalLines(true);
		tableUnitBounds.getTableHeader().setReorderingAllowed(false);
		scrollPane_4.setViewportView(tableUnitBounds);
		
		JLabel lblDataCollectionCosts = new JLabel("Data Collection Costs");
		GridBagConstraints gbc_lblDataCollectionCosts = new GridBagConstraints();
		gbc_lblDataCollectionCosts.gridwidth = 8;
		gbc_lblDataCollectionCosts.insets = new Insets(0, 0, 5, 0);
		gbc_lblDataCollectionCosts.gridx = 0;
		gbc_lblDataCollectionCosts.gridy = 5;
		panelSearchConstraints.add(lblDataCollectionCosts, gbc_lblDataCollectionCosts);
		
		JScrollPane scrollPane_5 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_5 = new GridBagConstraints();
		gbc_scrollPane_5.gridwidth = 8;
		gbc_scrollPane_5.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_5.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_5.gridx = 0;
		gbc_scrollPane_5.gridy = 6;
		panelSearchConstraints.add(scrollPane_5, gbc_scrollPane_5);
		
		tableUnitCosts = new CostTable();
		tableUnitCosts.setModel(modelUnitCosts);
		tableUnitCosts.setRowSelectionAllowed(false);
		tableUnitCosts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableUnitCosts.setShowVerticalLines(true);
		tableUnitCosts.getTableHeader().setReorderingAllowed(false);
		scrollPane_5.setViewportView(tableUnitCosts);
		
		JLabel lblBudgetContraint = new JLabel("Budget Constraint");
		GridBagConstraints gbc_lblBudgetContraint = new GridBagConstraints();
		gbc_lblBudgetContraint.gridwidth = 2;
		gbc_lblBudgetContraint.anchor = GridBagConstraints.EAST;
		gbc_lblBudgetContraint.insets = new Insets(0, 0, 5, 5);
		gbc_lblBudgetContraint.gridx = 1;
		gbc_lblBudgetContraint.gridy = 7;
		panelSearchConstraints.add(lblBudgetContraint, gbc_lblBudgetContraint);
		
		JLabel lblMin = new JLabel("Min:");
		GridBagConstraints gbc_lblMin = new GridBagConstraints();
		gbc_lblMin.anchor = GridBagConstraints.EAST;
		gbc_lblMin.insets = new Insets(0, 0, 5, 5);
		gbc_lblMin.gridx = 3;
		gbc_lblMin.gridy = 7;
		panelSearchConstraints.add(lblMin, gbc_lblMin);
		
		textBudgetMin = new JTextField("0");
		GridBagConstraints gbc_textBudgetMin = new GridBagConstraints();
		gbc_textBudgetMin.insets = new Insets(0, 0, 5, 5);
		gbc_textBudgetMin.fill = GridBagConstraints.HORIZONTAL;
		gbc_textBudgetMin.gridx = 4;
		gbc_textBudgetMin.gridy = 7;
		panelSearchConstraints.add(textBudgetMin, gbc_textBudgetMin);
		textBudgetMin.setColumns(10);
		
		JLabel lblMax = new JLabel("Max:");
		GridBagConstraints gbc_lblMax = new GridBagConstraints();
		gbc_lblMax.anchor = GridBagConstraints.EAST;
		gbc_lblMax.insets = new Insets(0, 0, 5, 5);
		gbc_lblMax.gridx = 5;
		gbc_lblMax.gridy = 7;
		panelSearchConstraints.add(lblMax, gbc_lblMax);
		
		textBudgetMax = new JTextField();
		GridBagConstraints gbc_textBudgetMax = new GridBagConstraints();
		gbc_textBudgetMax.insets = new Insets(0, 0, 5, 5);
		gbc_textBudgetMax.fill = GridBagConstraints.HORIZONTAL;
		gbc_textBudgetMax.gridx = 6;
		gbc_textBudgetMax.gridy = 7;
		panelSearchConstraints.add(textBudgetMax, gbc_textBudgetMax);
		textBudgetMax.setColumns(10);
		
		chckbxSeedSimulations = new JCheckBox("Seed simulations");
		chckbxSeedSimulations.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if(chckbxSeedSimulations.isSelected()){
					textSeed.setEnabled(true);
				}
				else{
					textSeed.setEnabled(false);
				}
			}
		});
		GridBagConstraints gbc_chckbxSeedSimulations = new GridBagConstraints();
		gbc_chckbxSeedSimulations.anchor = GridBagConstraints.EAST;
		gbc_chckbxSeedSimulations.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxSeedSimulations.gridx = 0;
		gbc_chckbxSeedSimulations.gridy = 8;
		panelSearchConstraints.add(chckbxSeedSimulations, gbc_chckbxSeedSimulations);
		
		textSeed = new JTextField();
		textSeed.setEnabled(false);
		textSeed.setText("999");
		GridBagConstraints gbc_textSeed = new GridBagConstraints();
		gbc_textSeed.anchor = GridBagConstraints.NORTH;
		gbc_textSeed.insets = new Insets(0, 0, 0, 5);
		gbc_textSeed.fill = GridBagConstraints.HORIZONTAL;
		gbc_textSeed.gridx = 1;
		gbc_textSeed.gridy = 8;
		panelSearchConstraints.add(textSeed, gbc_textSeed);
		textSeed.setColumns(10);
		
		JLabel lblSimulations = new JLabel("# simulations:");
		GridBagConstraints gbc_lblSimulations = new GridBagConstraints();
		gbc_lblSimulations.anchor = GridBagConstraints.EAST;
		gbc_lblSimulations.gridwidth = 2;
		gbc_lblSimulations.insets = new Insets(0, 0, 0, 5);
		gbc_lblSimulations.gridx = 2;
		gbc_lblSimulations.gridy = 8;
		panelSearchConstraints.add(lblSimulations, gbc_lblSimulations);
		
		JButton btnRunSearch = new JButton("Run Search");
		btnRunSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//Start a new thread (Non-UI)
				Thread SimThread = new Thread(){
					public void run(){   
						constraints=new Constraints(curProject.sampleFrame);
						ArrayList<String> errors=constraints.parse(mainForm);
						
						int numErrs=errors.size();
						if(numErrs>0){ //Errors found
							textArea.setText(numErrs+" errors found:\n");
							for(int i=0; i<numErrs; i++){
								textArea.append(errors.get(i)+"\n");
							}
						}
						else{ //Run search
							textArea.setText("Building schedule...");
							Scheduler scheduler=new Scheduler(constraints,textArea);
							textArea.append("done\n");
							
							tabbedPane.setEnabledAt(3, true);
							tabbedPane.setSelectedIndex(3);
							//clear table
							modelSearchResults.setRowCount(0);
							
							int numDesigns=scheduler.schedule.size();
							int numIterations=constraints.numIterations;
							textArea.append(numDesigns+" designs to evaluate\n");
							textArea.append("\nEvaluating sample designs over "+numIterations+" iterations\n\n");
							//headers
							textArea.append("Design\t");
							for(int l=0; l<constraints.numLevels; l++){textArea.append(curProject.sampleFrame.levels[l].name+"\t\t\t");}
							textArea.append("\n");
							textArea.append("#\t");
							for(int l=0; l<constraints.numLevels; l++){textArea.append("Strata\tPPS  \tUnits\t");}
							textArea.append("Progress\n");
							textArea.append("------\t");
							for(int l=0; l<constraints.numLevels; l++){textArea.append("------\t-----\t-----\t");}
							textArea.append("-----------\n");
							
							long startTime=System.currentTimeMillis();
							final ProgressMonitor progress=new ProgressMonitor(frmMain, "Sampling...", "", 0, numDesigns);
							boolean wasCancelled=false;

							results=new ArrayList<SampleDesign>();
							
							double minRange=Double.POSITIVE_INFINITY, maxRange=0;
							
							for(int i=0; i<numDesigns; i++){
								Design curDesign=scheduler.schedule.get(i);
								textArea.append((i+1)+curDesign.tagTab+"\t");

								SampleDesign design=new SampleDesign(textArea,curProject.sampleFrame.sampleTree, constraints,curDesign);
								if(constraints.seedSims){
									design.generator.setSeed(constraints.seed);
								}
								
								design.buildSampleWeights(curProject.sampleFrame.sampleTree, 0); //send root
								design.estimateIPW(numIterations);
								design.calculateCosts(numIterations);
								
								design.getFinalSample();
								results.add(design);
								
								design.addToTable(modelSearchResults,i,curProject.sampleFrame);
								minRange=Math.min(minRange, design.kde[0][0]);
								maxRange=Math.max(maxRange, design.kde[0][99]);
								
								//Update progress bar
								if(progress.isCanceled()){ //End simulation
									i=numDesigns;
									wasCancelled=true;
								}
								double prog=((i+1)/(numDesigns*1.0))*100;
								long remTime=(long) ((System.currentTimeMillis()-startTime)/prog); //Number of miliseconds per percent
								remTime=(long) (remTime*(100-prog));
								remTime=remTime/1000;
								String seconds = Integer.toString((int)(remTime % 60));
								String minutes = Integer.toString((int)(remTime/60));
								if(seconds.length()<2){seconds="0"+seconds;}
								if(minutes.length()<2){minutes="0"+minutes;}
								progress.setProgress(i+1);
								progress.setNote("Time left: "+minutes+":"+seconds);

							}
							
							XYPlot plotResults = chartResults.getXYPlot();
			        		plotResults.getDomainAxis().setRange(minRange, maxRange);
			        	
						}
					}
				};
				SimThread.start();
			}
		});
		
		textNumSim = new JTextField();
		textNumSim.setText("10,000");
		GridBagConstraints gbc_textNumSim = new GridBagConstraints();
		gbc_textNumSim.insets = new Insets(0, 0, 0, 5);
		gbc_textNumSim.fill = GridBagConstraints.HORIZONTAL;
		gbc_textNumSim.gridx = 4;
		gbc_textNumSim.gridy = 8;
		panelSearchConstraints.add(textNumSim, gbc_textNumSim);
		textNumSim.setColumns(10);
		GridBagConstraints gbc_btnRunSearch = new GridBagConstraints();
		gbc_btnRunSearch.insets = new Insets(0, 0, 0, 5);
		gbc_btnRunSearch.gridx = 6;
		gbc_btnRunSearch.gridy = 8;
		panelSearchConstraints.add(btnRunSearch, gbc_btnRunSearch);
		
		modelSearchResults=new DefaultTableModel(
				new Object[][] {},
				new String[] {"#", "Sampling Type", "Sampling Probability"}
				){
			public boolean isCellEditable(int row, int column) {
				return false;
			}
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if(modelSearchResults.getRowCount()==0){
					return Object.class;
				}
				return getValueAt(0, columnIndex).getClass();
			}
		};
		
						
		tabbedPane.setTabComponentAt(2, lblSearch);
		tabbedPane.setEnabledAt(2, false);
		
		
		JLabel lblSearchResults=new JLabel("Search Results");
		//Icon iconSearchResults=new ImageIcon(frmMain.class.getResource("/images/searchResults.png"));
		//lblSearchResults.setIcon(iconSearchResults);
		lblSearchResults.setIcon(new ScaledIcon("/images/searchResults",16,16,true));
		lblSearchResults.setIconTextGap(5);
		lblSearchResults.setHorizontalTextPosition(SwingConstants.RIGHT);
		
		JPanel panelSearchResults = new JPanel();
		tabbedPane.addTab("Search Results", null, panelSearchResults, null);
		tabbedPane.setTabComponentAt(3, lblSearchResults);
		tabbedPane.setEnabledAt(3, false);
		GridBagLayout gbl_panelSearchResults = new GridBagLayout();
		gbl_panelSearchResults.columnWidths = new int[]{450, 0};
		gbl_panelSearchResults.rowHeights = new int[]{0, 0};
		gbl_panelSearchResults.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panelSearchResults.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panelSearchResults.setLayout(gbl_panelSearchResults);
		
		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setResizeWeight(0.35);
		GridBagConstraints gbc_splitPane_1 = new GridBagConstraints();
		gbc_splitPane_1.fill = GridBagConstraints.BOTH;
		gbc_splitPane_1.gridx = 0;
		gbc_splitPane_1.gridy = 0;
		panelSearchResults.add(splitPane_1, gbc_splitPane_1);
		
		JPanel panelAllDesigns = new JPanel();
		splitPane_1.setRightComponent(panelAllDesigns);
		GridBagLayout gbl_panelAllDesigns = new GridBagLayout();
		gbl_panelAllDesigns.columnWidths = new int[]{0, 0};
		gbl_panelAllDesigns.rowHeights = new int[]{0, 0};
		gbl_panelAllDesigns.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panelAllDesigns.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panelAllDesigns.setLayout(gbl_panelAllDesigns);
		
		JSplitPane splitPane_3 = new JSplitPane();
		splitPane_3.setResizeWeight(0.5);
		splitPane_3.setOrientation(JSplitPane.VERTICAL_SPLIT);
		GridBagConstraints gbc_splitPane_3 = new GridBagConstraints();
		gbc_splitPane_3.fill = GridBagConstraints.BOTH;
		gbc_splitPane_3.gridx = 0;
		gbc_splitPane_3.gridy = 0;
		panelAllDesigns.add(splitPane_3, gbc_splitPane_3);
		
		JPanel panelAllDesignsTable = new JPanel();
		splitPane_3.setLeftComponent(panelAllDesignsTable);
		GridBagLayout gbl_panelAllDesignsTable = new GridBagLayout();
		gbl_panelAllDesignsTable.columnWidths = new int[]{0, 0};
		gbl_panelAllDesignsTable.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panelAllDesignsTable.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panelAllDesignsTable.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		panelAllDesignsTable.setLayout(gbl_panelAllDesignsTable);
		
		JLabel lblAllDesigns = new JLabel("All Designs");
		lblAllDesigns.setFont(new Font("SansSerif", Font.BOLD, 12));
		GridBagConstraints gbc_lblAllDesigns = new GridBagConstraints();
		gbc_lblAllDesigns.insets = new Insets(0, 0, 5, 0);
		gbc_lblAllDesigns.anchor = GridBagConstraints.NORTH;
		gbc_lblAllDesigns.gridx = 0;
		gbc_lblAllDesigns.gridy = 0;
		panelAllDesignsTable.add(lblAllDesigns, gbc_lblAllDesigns);
		
		JToolBar toolBar_2 = new JToolBar();
		GridBagConstraints gbc_toolBar_2 = new GridBagConstraints();
		gbc_toolBar_2.insets = new Insets(0, 0, 5, 0);
		gbc_toolBar_2.anchor = GridBagConstraints.NORTHWEST;
		gbc_toolBar_2.gridx = 0;
		gbc_toolBar_2.gridy = 1;
		panelAllDesignsTable.add(toolBar_2, gbc_toolBar_2);
		toolBar_2.setRollover(true);
		toolBar_2.setFloatable(false);
		
		JButton btnCopy = new JButton("Copy");
		btnCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int numRows=tableSearchResults.getRowCount();
				if(numRows>0){
					int numCol=modelSearchResults.getColumnCount();
					String data[][]=new String[numRows+1][numCol];
					//Get headers
					for(int c=0; c<numCol; c++){
						data[0][c]=modelSearchResults.getColumnName(c);
					}
					//Get row
					for(int r=0; r<numRows; r++){
						for(int c=0; c<numCol; c++){
							data[r+1][c]=modelSearchResults.getValueAt(r, c)+"";
						}
					}
					
					Clipboard clip=Toolkit.getDefaultToolkit().getSystemClipboard();
					clip.setContents(new DataTransferable(data), null);
				}
				
			}
		});
		//btnCopy.setIcon(new ImageIcon(frmMain.class.getResource("/images/copy_16.png")));
		btnCopy.setIcon(new ScaledIcon("/images/copy",16,16,true));
		toolBar_2.add(btnCopy);
		
		JButton btnExport = new JButton("Export");
		btnExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int numRows=tableSearchResults.getRowCount();
				if(numRows>0){
					try {
						fc.setDialogTitle("Export Results");
						fc.setApproveButtonText("Export");
						fc.setFileFilter(new CSVFilter());

						int returnVal = fc.showSaveDialog(frmMain);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							frmMain.setCursor(new Cursor(Cursor.WAIT_CURSOR));
							File file = fc.getSelectedFile();
							String path=file.getAbsolutePath();
							path=path.replaceAll(".csv", "");
							//Open file for writing
							FileWriter fstream = new FileWriter(path+".csv"); //Create new file
							BufferedWriter out = new BufferedWriter(fstream);
							
							int numCols=modelSearchResults.getColumnCount();
							//Headers
							for(int i=0; i<numCols-1; i++){
								out.write(modelSearchResults.getColumnName(i)+",");
							}
							out.write(modelSearchResults.getColumnName(numCols-1));
							out.newLine();
							//Data
							for(int i=0; i<numRows; i++){
								for(int j=0; j<numCols-1; j++){
									out.write(modelSearchResults.getValueAt(i, j)+",");
								}
								out.write(modelSearchResults.getValueAt(i, numCols-1)+"");
								out.newLine();
							}
							out.close();
													
							//Open file
							Desktop dt = Desktop.getDesktop();
							dt.open(new File(path+".csv"));
							frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						}

					}catch(Exception er){
						frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						JOptionPane.showMessageDialog(frmMain,er.getMessage());
						errorLog.recordError(er);
					}
				}
			}
		});
		btnExport.setIcon(new ScaledIcon("/images/export",16,16,true));
		toolBar_2.add(btnExport);
		
		JSeparator separator_5 = new JSeparator();
		separator_5.setOrientation(SwingConstants.VERTICAL);
		toolBar_2.add(separator_5);
		
		JButton btnSaveResults = new JButton("Save");
		btnSaveResults.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try{
					fc.setDialogTitle("Save Search Results");
					fc.setApproveButtonText("Save");
					fc.setFileFilter(new EPICResultsFilter());

					int returnVal = fc.showSaveDialog(frmMain);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						frmMain.setCursor(new Cursor(Cursor.WAIT_CURSOR));
						File file = fc.getSelectedFile();
						String path=file.getAbsolutePath();
						path=path.replaceAll(".epicRes", "");

						SearchResults searchResults=new SearchResults();
						searchResults.getSampleFrameInfo(curProject.sampleFrame);
						searchResults.constraints=constraints;
						searchResults.results=results;
						searchResults.save(path+".epicRes");
						frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
				}catch(Exception e){
					frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					JOptionPane.showMessageDialog(frmMain, "Error: "+e.toString());
					errorLog.recordError(e);
				}
			}
		});
		btnSaveResults.setIcon(new ScaledIcon("/images/save",16,16,true));
		toolBar_2.add(btnSaveResults);
		
		JButton btnLoad = new JButton("Load");
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					fc.setFileFilter(new EPICResultsFilter());
					fc.setDialogTitle("Load Search Results");
					fc.setApproveButtonText("Load");

					int returnVal = fc.showOpenDialog(frmMain);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						String name=file.getName();
						String filepath=file.getAbsolutePath();

						frmMain.setCursor(new Cursor(Cursor.WAIT_CURSOR));
						
						JAXBContext context = JAXBContext.newInstance(SearchResults.class);
						Unmarshaller un = context.createUnmarshaller();
						SearchResults searchResults = (SearchResults) un.unmarshal(new File(filepath));
						
						//check if compatible
						if(searchResults.isCompatible(curProject.sampleFrame)){
							searchResults.load(mainForm);
						}
						else{
							JOptionPane.showMessageDialog(frmMain, "Incompatible with current sample frame!");
						}
						
						frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}

				}catch(Exception ex){
					frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					JOptionPane.showMessageDialog(frmMain, "Error: "+ex.toString());
					errorLog.recordError(ex);
				}
			}
		});
		btnLoad.setIcon(new ScaledIcon("/images/load",16,16,true));
		toolBar_2.add(btnLoad);
		
		JSeparator separator_6 = new JSeparator();
		toolBar_2.add(separator_6);
		
		tableSearchResults = new JTable(modelSearchResults);
		//tableSearchResults.setRowSelectionAllowed(false);
		tableSearchResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableSearchResults.setShowVerticalLines(true);
		tableSearchResults.getTableHeader().setReorderingAllowed(false);
		tableSearchResults.setAutoCreateRowSorter(true);
		
		tableSearchResults.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
	        public void valueChanged(ListSelectionEvent event) {
	        	int row=tableSearchResults.getSelectedRow();
	        	if(row!=-1){
	        		int selected=tableSearchResults.convertRowIndexToModel(row);
	        		curDesign=results.get(selected);
	        		//graph design
	        		panelDesignGraph.drawGraph(curProject, curDesign.curDesign);
	        		
	        		//plot results
	        		XYPlot plotResults = chartResults.getXYPlot();
	        		XYLineAndShapeRenderer rendererResults = new XYLineAndShapeRenderer(true,false);
	        		DefaultDrawingSupplier supplierResults = new DefaultDrawingSupplier();
	        		rendererResults.setSeriesPaint(0, supplierResults.getNextPaint());
	        		ValueMarker marker = new ValueMarker(curDesign.trueTotal);  // position is the value on the axis
	        		marker.setPaint(Color.black);
	        		if(chartDataResults.getSeriesCount()>1){
	        			plotResults.removeDomainMarker(marker);
		        		chartDataResults.removeSeries("1");
	        		}
	        		chartDataResults.addSeries("1",curDesign.kde);
	        		plotResults.addDomainMarker(marker);
	        		plotResults.setRenderer(rendererResults);
	        		
	        		//update all results chart
	        		updateScatter(selected);
	        	}
	        }
	    });
		
		JScrollPane scrollPane_7 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_7 = new GridBagConstraints();
		gbc_scrollPane_7.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_7.gridx = 0;
		gbc_scrollPane_7.gridy = 2;
		panelAllDesignsTable.add(scrollPane_7, gbc_scrollPane_7);
		scrollPane_7.setViewportView(tableSearchResults);
		
		JPanel panelAllDesignsChart = new JPanel();
		splitPane_3.setRightComponent(panelAllDesignsChart);
		GridBagLayout gbl_panelAllDesignsChart = new GridBagLayout();
		gbl_panelAllDesignsChart.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panelAllDesignsChart.rowHeights = new int[]{0, 0, 0};
		gbl_panelAllDesignsChart.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panelAllDesignsChart.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		panelAllDesignsChart.setLayout(gbl_panelAllDesignsChart);
		
		JLabel lblXaxis = new JLabel("X-Axis:");
		GridBagConstraints gbc_lblXaxis = new GridBagConstraints();
		gbc_lblXaxis.insets = new Insets(0, 0, 5, 5);
		gbc_lblXaxis.anchor = GridBagConstraints.EAST;
		gbc_lblXaxis.gridx = 0;
		gbc_lblXaxis.gridy = 0;
		panelAllDesignsChart.add(lblXaxis, gbc_lblXaxis);
		
		comboXAxis = new JComboBox<String>();
		comboXAxis.setModel(new DefaultComboBoxModel<String>(new String[] {"Cost", "SD", "Error - Absolute", "Error - Relative", "Within 1%", "Within 5%", "Within 10%", "Within 25%"}));
		comboXAxis.setSelectedIndex(0);
		GridBagConstraints gbc_comboXAxis = new GridBagConstraints();
		gbc_comboXAxis.insets = new Insets(0, 0, 5, 5);
		gbc_comboXAxis.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboXAxis.gridx = 1;
		gbc_comboXAxis.gridy = 0;
		panelAllDesignsChart.add(comboXAxis, gbc_comboXAxis);
		
		JLabel lblYaxis = new JLabel("Y-Axis:");
		GridBagConstraints gbc_lblYaxis = new GridBagConstraints();
		gbc_lblYaxis.insets = new Insets(0, 0, 5, 5);
		gbc_lblYaxis.anchor = GridBagConstraints.EAST;
		gbc_lblYaxis.gridx = 2;
		gbc_lblYaxis.gridy = 0;
		panelAllDesignsChart.add(lblYaxis, gbc_lblYaxis);
		
		comboYAxis = new JComboBox<String>();
		comboYAxis.setModel(new DefaultComboBoxModel<String>(new String[] {"Cost", "SD", "Error - Absolute", "Error - Relative", "Within 1%", "Within 5%", "Within 10%", "Within 25%"}));
		comboYAxis.setSelectedIndex(3);
		GridBagConstraints gbc_comboYAxis = new GridBagConstraints();
		gbc_comboYAxis.insets = new Insets(0, 0, 5, 5);
		gbc_comboYAxis.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboYAxis.gridx = 3;
		gbc_comboYAxis.gridy = 0;
		panelAllDesignsChart.add(comboYAxis, gbc_comboYAxis);
		
		chartDataScatter = new DefaultXYDataset();
		chartScatter = ChartFactory.createScatterPlot(null, "Cost", "Error - Relative", chartDataScatter, PlotOrientation.VERTICAL, false, false, false);
		chartScatter.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
		
		XYLineAndShapeRenderer rendererScatter = new XYLineAndShapeRenderer(false,true);
		rendererScatter.setSeriesPaint(0, Color.BLUE);
		rendererScatter.setSeriesShape(0, new Ellipse2D.Double(-2.5,-2.5,5,5));
		rendererScatter.setSeriesPaint(1, new Color(255,0,0,100));
		rendererScatter.setSeriesShape(1, new Ellipse2D.Double(-5,-5,10,10));
		XYPlot plotScatter = chartScatter.getXYPlot();
		plotScatter.setRenderer(rendererScatter);
		plotScatter.setSeriesRenderingOrder(SeriesRenderingOrder.REVERSE);
				
		JButton btnUpdate = new JButton("Update");
		btnUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//Get selected axes
				int testX=comboXAxis.getSelectedIndex();
				int testY=comboYAxis.getSelectedIndex();
				if(testX==testY){
					JOptionPane.showMessageDialog(frmMain, "Please select different X and Y axes!");
				}
				else{
					xAxis=testX;
					yAxis=testY;
					XYPlot plot=chartScatter.getXYPlot();
					plot.getDomainAxis().setLabel((String) comboXAxis.getSelectedItem());
					plot.getRangeAxis().setLabel((String) comboYAxis.getSelectedItem());
					
					int row=tableSearchResults.getSelectedRow();
					int selected=-1;
					if(row!=-1){
						selected=tableSearchResults.convertRowIndexToModel(row);
					}
					updateScatter(selected);
				}
			}
		});
		GridBagConstraints gbc_btnUpdate = new GridBagConstraints();
		gbc_btnUpdate.insets = new Insets(0, 0, 5, 0);
		gbc_btnUpdate.gridx = 4;
		gbc_btnUpdate.gridy = 0;
		panelAllDesignsChart.add(btnUpdate, gbc_btnUpdate);
				
		ChartPanel panelChartAll = new ChartPanel(chartScatter,false);
		panelChartAll.addChartMouseListener(new ChartMouseListener() {
			public void chartMouseClicked(ChartMouseEvent event) {
				if(event.getTrigger().getButton()==MouseEvent.BUTTON1 && event.getTrigger().getClickCount()==1){

					Point2D p=event.getTrigger().getPoint();
					Rectangle2D plotArea = panelChartAll.getScreenDataArea();
					XYPlot plot = (XYPlot) chartScatter.getPlot(); // your plot
					double clickX=p.getX();
					double clickY=p.getY();
					
					double minDist=Double.POSITIVE_INFINITY;
					int minIndex=-1;
					for(int r=0; r<dataScatter[0].length; r++){
						double curX=plot.getDomainAxis().valueToJava2D(dataScatter[0][r], plotArea, plot.getDomainAxisEdge());
						double curY=plot.getRangeAxis().valueToJava2D(dataScatter[1][r], plotArea, plot.getRangeAxisEdge());
						
						double x2=(curX-clickX)*(curX-clickX);
						double y2=(curY-clickY)*(curY-clickY);
						double curDist=Math.sqrt(x2+y2);
						if(curDist<minDist){
							minDist=curDist;
							minIndex=r;
						}
					}
					
					int select=tableSearchResults.convertRowIndexToView(minIndex);
					tableSearchResults.setRowSelectionInterval(select, select); //select row
					tableSearchResults.scrollRectToVisible(tableSearchResults.getCellRect(select,0, true)); //scroll to row
										
				}
			}
			public void chartMouseMoved(ChartMouseEvent event) {
			}
		});
		
		GridBagConstraints gbc_panelChartAll = new GridBagConstraints();
		gbc_panelChartAll.gridwidth = 5;
		gbc_panelChartAll.fill = GridBagConstraints.BOTH;
		gbc_panelChartAll.gridx = 0;
		gbc_panelChartAll.gridy = 1;
		panelAllDesignsChart.add(panelChartAll, gbc_panelChartAll);
		
		JPanel panelDesignViewer = new JPanel();
		splitPane_1.setLeftComponent(panelDesignViewer);
		GridBagLayout gbl_panelDesignViewer = new GridBagLayout();
		gbl_panelDesignViewer.columnWidths = new int[]{0, 0};
		gbl_panelDesignViewer.rowHeights = new int[]{0, 0};
		gbl_panelDesignViewer.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panelDesignViewer.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panelDesignViewer.setLayout(gbl_panelDesignViewer);
		
		JSplitPane splitPane_2 = new JSplitPane();
		splitPane_2.setResizeWeight(0.5);
		splitPane_2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		GridBagConstraints gbc_splitPane_2 = new GridBagConstraints();
		gbc_splitPane_2.fill = GridBagConstraints.BOTH;
		gbc_splitPane_2.gridx = 0;
		gbc_splitPane_2.gridy = 0;
		panelDesignViewer.add(splitPane_2, gbc_splitPane_2);
		
		chartDataResults = new DefaultXYDataset();
		chartResults = ChartFactory.createScatterPlot(null, "Total Outcome", "Density", chartDataResults, PlotOrientation.VERTICAL, false, false, false);
		chartResults.getXYPlot().setBackgroundPaint(new Color(1,1,1,1));
		
		ChartPanel panelChart = new ChartPanel(chartResults,false);
		splitPane_2.setRightComponent(panelChart);
		
		JPanel panelCurDesign = new JPanel();
		splitPane_2.setLeftComponent(panelCurDesign);
		GridBagLayout gbl_panelCurDesign = new GridBagLayout();
		gbl_panelCurDesign.columnWidths = new int[]{0, 0, 0};
		gbl_panelCurDesign.rowHeights = new int[]{0, 0, 0};
		gbl_panelCurDesign.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_panelCurDesign.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		panelCurDesign.setLayout(gbl_panelCurDesign);
		
		JLabel lblCurrentDesign = new JLabel("Current Design");
		lblCurrentDesign.setFont(new Font("SansSerif", Font.BOLD, 12));
		GridBagConstraints gbc_lblCurrentDesign = new GridBagConstraints();
		gbc_lblCurrentDesign.insets = new Insets(0, 0, 5, 5);
		gbc_lblCurrentDesign.gridx = 0;
		gbc_lblCurrentDesign.gridy = 0;
		panelCurDesign.add(lblCurrentDesign, gbc_lblCurrentDesign);
		
		JButton btnDrawFinalSample = new JButton("View Final Sample");
		btnDrawFinalSample.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmFinalSample window=new frmFinalSample(curProject,curDesign,errorLog);
				window.frmFinalSample.setVisible(true);
			}
		});
		GridBagConstraints gbc_btnDrawFinalSample = new GridBagConstraints();
		gbc_btnDrawFinalSample.insets = new Insets(0, 0, 5, 0);
		gbc_btnDrawFinalSample.gridx = 1;
		gbc_btnDrawFinalSample.gridy = 0;
		panelCurDesign.add(btnDrawFinalSample, gbc_btnDrawFinalSample);
		
		JScrollPane scrollPane_6 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_6 = new GridBagConstraints();
		gbc_scrollPane_6.gridwidth = 2;
		gbc_scrollPane_6.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane_6.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_6.gridx = 0;
		gbc_scrollPane_6.gridy = 1;
		panelCurDesign.add(scrollPane_6, gbc_scrollPane_6);
		
		panelDesignGraph = new PanelDesign();
		scrollPane_6.setViewportView(panelDesignGraph);
				
		JMenuBar menuBar = new JMenuBar();
		frmMain.setJMenuBar(menuBar);
		
		JMenu mnSampleFrame = new JMenu("Project");
		menuBar.add(mnSampleFrame);
		
		JMenuItem mntmNewSampleFrame = new JMenuItem("New");
		mntmNewSampleFrame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				curProject=new Project();
				curProject.errorLog=errorLog;
				frmProject window=new frmProject(curProject,true,mainForm);
				window.frmProject.setVisible(true);
			}
		});
		mnSampleFrame.add(mntmNewSampleFrame);
		
		JMenuItem mntmOpenSampleFrame = new JMenuItem("Open...");
		mntmOpenSampleFrame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					fc.resetChoosableFileFilters();
					fc.addChoosableFileFilter(new EPICProjectFilter());
					fc.setAcceptAllFileFilterUsed(false);

					fc.setDialogTitle("Open Project");
					fc.setApproveButtonText("Open");

					int returnVal = fc.showOpenDialog(frmMain);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						openProject(file);
					}

				}catch(Exception e){
					JOptionPane.showMessageDialog(frmMain, "Error: "+e.toString());
					errorLog.recordError(e);
				}
			}
		});
		mnSampleFrame.add(mntmOpenSampleFrame);
		
		mntmEditSampleFrame = new JMenuItem("Edit Settings...");
		mntmEditSampleFrame.setEnabled(false);
		mntmEditSampleFrame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmProject window=new frmProject(curProject,false,mainForm);
				window.frmProject.setVisible(true);
			}
		});
		
		
		final JMenu mnOpenRecent = new JMenu("Open Recent");
		mnOpenRecent.addMenuListener(new MenuListener() {
			public void menuCanceled(MenuEvent arg0) {
			}
			public void menuDeselected(MenuEvent arg0) {
			}
			public void menuSelected(MenuEvent arg0) {
				recentFiles.buildList(mnOpenRecent,mainForm);
			}
		});
		mnSampleFrame.add(mnOpenRecent);
		
		JSeparator separator = new JSeparator();
		mnSampleFrame.add(separator);
		
		mntmSave = new JMenuItem("Save");
		KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		mntmSave.setAccelerator(ctrlS);
		mntmSave.setEnabled(false);
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveProject();
			}
		});
		mnSampleFrame.add(mntmSave);
		
		mntmSaveAs = new JMenuItem("Save As...");
		mntmSaveAs.setEnabled(false);
		mntmSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser fc=null;
					fc=new JFileChooser(curProject.filepath);
					fc.setFileFilter(new EPICProjectFilter());
					fc.setAcceptAllFileFilterUsed(false);

					fc.setDialogTitle("Save Project");
					fc.setApproveButtonText("Save");

					int returnVal = fc.showSaveDialog(frmMain);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						String path=file.getAbsolutePath();
						String name=file.getName();
						curProject.name=name.replaceAll(".epic", "");
						path=path.replaceAll(".epic", "");
						curProject.filepath=path+".epic";
						curProject.save();
						unsavedChanges=false;
						frmMain.setTitle("EPIC Sampler - "+curProject.name);
					}

				}catch(Exception e1){
					e1.printStackTrace();
					errorLog.recordError(e1);
				}
			}
		});
		mnSampleFrame.add(mntmSaveAs);
		
		JSeparator separator_1 = new JSeparator();
		mnSampleFrame.add(separator_1);
		mnSampleFrame.add(mntmEditSampleFrame);
		
		JSeparator separator_2 = new JSeparator();
		mnSampleFrame.add(separator_2);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		mnSampleFrame.add(mntmExit);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmAbout window=new frmAbout(version,errorLog);
				window.frmAbout.setVisible(true);
			}
		});
		mnHelp.add(mntmAbout);
		
		JMenuItem mntmErrorLog = new JMenuItem("Error Log");
		mntmErrorLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmErrorLog window=new frmErrorLog(errorLog);
				window.frmErrorLog.setVisible(true);
			}
		});
		mnHelp.add(mntmErrorLog);
	}
	
	
	private boolean checkHeaders(String headers[]){
		boolean validCols=true;
		int colNum=headers.length;
		if(colNum!=modelLevel.getColumnCount()){
			validCols=false;
			JOptionPane.showMessageDialog(frmMain, "Number of columns does not match!");
		}
		else{
			int c=0;
			while(c<colNum && validCols==true){
				if(!headers[c].equals(modelLevel.getColumnName(c))){
					validCols=false;
					JOptionPane.showMessageDialog(frmMain, "Column headers do not match! Expected "+modelLevel.getColumnName(c)+", got "+headers[c]);
				}
				c++;
			}
		}
		return(validCols);
	}
	
	private void saveLevelData(){
		Level curLevel=curProject.sampleFrame.levels[indexLevel];
		curLevel.numRows=modelLevel.getRowCount();
		curLevel.numCols=modelLevel.getColumnCount();
		
		//headers
		curLevel.headers=new String[curLevel.numCols];
		for(int c=0; c<curLevel.numCols; c++){
			curLevel.headers[c]=modelLevel.getColumnName(c);
		}
		
		//rows
		curLevel.data=new String[curLevel.numRows][curLevel.numCols];
		for(int r=0; r<curLevel.numRows; r++){
			for(int c=0; c<curLevel.numCols; c++){
				curLevel.data[r][c]=(String) modelLevel.getValueAt(r, c);
			}
		}
	}
	
	private void updateComboLevels(){
		String levelNames[]=new String[curProject.sampleFrame.numLevels];
		for(int i=0; i<curProject.sampleFrame.numLevels; i++){
			levelNames[i]=curProject.sampleFrame.levels[i].name;
		}
		comboLevel.setModel(new DefaultComboBoxModel<String>(levelNames));
		comboLevel.setSelectedIndex(0);
	}
	
	private void updateDesignOptions(){
		tableLevelDesign.setFrame(curProject.sampleFrame);
		tableLevelDesign.setUnitModels(modelUnitBounds,modelUnitCosts);
		tableUnitCosts.setFrame(curProject.sampleFrame);
		
		int numLevels=curProject.sampleFrame.numLevels;
		modelLevelDesign.setRowCount(numLevels);
		for(int i=0; i<numLevels; i++){
			modelLevelDesign.setValueAt(curProject.sampleFrame.levels[i].name, i, 0);
			modelLevelDesign.setValueAt("Simple", i, 1);
			if(curProject.sampleFrame.levels[i].hasSize){
				modelLevelDesign.setValueAt("Proportional to size (PPS)", i, 2);
			}
			else{ //No size
				modelLevelDesign.setValueAt("Equal", i, 2);
			}
			
		}
		
		modelUnitBounds.setRowCount(numLevels);
		for(int i=0; i<numLevels; i++){
			modelUnitBounds.setValueAt(curProject.sampleFrame.levels[i].name, i, 0);
			modelUnitBounds.setValueAt("[Overall]", i, 1);
		}
		
		modelUnitCosts.setRowCount(numLevels);
		for(int i=0; i<numLevels; i++){
			modelUnitCosts.setValueAt(curProject.sampleFrame.levels[i].name, i, 0);
			modelUnitCosts.setValueAt("[Overall]", i, 1);
			modelUnitCosts.setValueAt("0", i, 2); //fixed costs
			if(curProject.sampleFrame.levels[i].hasCost){ //unit-specific
				modelUnitCosts.setValueAt("Unit-Specific", i, 3);
				modelUnitCosts.setValueAt("---", i, 4);
			}
			else{ //average
				modelUnitCosts.setValueAt("Average", i, 3);
			}
		}
	}
	
	private void updateResultsTable(){
		//design
		modelSearchResults.setColumnCount(3);
		for(int i=0; i<curProject.sampleFrame.numLevels; i++){
			modelSearchResults.addColumn(curProject.sampleFrame.levels[i].name);
		}
		//results
		modelSearchResults.addColumn("Sample Cost");
		modelSearchResults.addColumn("Outcome SD");
		modelSearchResults.addColumn("Mean Error - Absolute");
		modelSearchResults.addColumn("Mean Error - Relative");
		modelSearchResults.addColumn("Within 1%");
		modelSearchResults.addColumn("Within 5%");
		modelSearchResults.addColumn("Within 10%");
		modelSearchResults.addColumn("Within 25%");
		modelSearchResults.addColumn("Insufficient %");
	}
	
	
	private void displayLevel(){
		if(indexLevel!=-1){
			modelLevel.setRowCount(0);
			modelLevel.setColumnCount(0);
			
			Level curLevel=curProject.sampleFrame.levels[indexLevel];
			int numRows=curLevel.numRows;
			int numCol=curLevel.numCols;
			
			//headers
			for(int c=0; c<numCol; c++){
				modelLevel.addColumn(curLevel.headers[c]);
			}
			//rows
			for(int r=0; r<numRows; r++){
				modelLevel.addRow(new Object[]{null});
				for(int c=0; c<numCol; c++){
					modelLevel.setValueAt(curLevel.data[r][c], r, c);
				}
			}
			
			lblLevelRows.setText(" "+numRows+" rows ");
		}
	}
	
	public void displayProject(){
		frmMain.setTitle("EPIC Sampler - "+curProject.name);
		//update levels and display level 0
		updateComboLevels();
		//displayLevel();
		//enable menu
		mntmSave.setEnabled(true);
		mntmSaveAs.setEnabled(true);
		mntmEditSampleFrame.setEnabled(true);
		//enable panels
		tabbedPane.setEnabled(true);
		//sample frame
		comboLevel.setEnabled(true);
		btnImportLevel.setEnabled(true);
		btnPasteLevel.setEnabled(true);
		btnExportLevel.setEnabled(true);
		btnValidateSampleFrame.setEnabled(true);
		//search design
		updateDesignOptions();
		updateResultsTable();
		
		tabbedPane.setSelectedIndex(0); //frame inputs
		tabbedPane.setEnabledAt(1, false); //sample frame report
		tabbedPane.setEnabledAt(2, false); //sample design search
		tabbedPane.setEnabledAt(3, false); //search results
	}
	
	public void changesMade(){
		frmMain.setTitle("EPIC Sampler - "+curProject.name+"*");
		unsavedChanges=true;
		
		tabbedPane.setSelectedIndex(0);
		tabbedPane.setEnabledAt(1, false);
		tabbedPane.setEnabledAt(2, false);
		tabbedPane.setEnabledAt(3, false); //search results
	}
	
	public void saveProject(){
		try {
			if(curProject.filepath==null){
				fc.resetChoosableFileFilters();
				fc.addChoosableFileFilter(new EPICProjectFilter());
				fc.setAcceptAllFileFilterUsed(false);
				fc.setDialogTitle("Save Project");
				fc.setApproveButtonText("Save");

				int returnVal = fc.showSaveDialog(frmMain);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					String name=file.getName();
					curProject.name=name.replaceAll(".epic", "");
					String path=file.getAbsolutePath();
					path=path.replaceAll(".epic", "");
					curProject.filepath=path+".epic";
					curProject.save();
					frmMain.setTitle("EPIC Sampler - "+curProject.name);
					unsavedChanges=false;
					recentFiles.updateList(curProject.filepath);
				}
			}
			else{
				curProject.save();
				frmMain.setTitle("EPIC Sampler - "+curProject.name);
				unsavedChanges=false;
				recentFiles.updateList(curProject.filepath);
			}
		}catch(Exception e1){
			e1.printStackTrace();
			errorLog.recordError(e1);
		}
	}
	
	public void openProject(File file){
		try{
			boolean open=true;
			if(curProject!=null){
				open=saveChangesPrompt();
			}
			
			if(open){
				String name=file.getName();
				String filepath=file.getAbsolutePath();

				frmMain.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				listenTableChanges=false;

				JAXBContext context = JAXBContext.newInstance(Project.class);
				Unmarshaller un = context.createUnmarshaller();
				Project project = (Project) un.unmarshal(new File(filepath));
				curProject = project;
				curProject.name=name.replaceAll(".epic", "");
				curProject.filepath=filepath;
				curProject.errorLog=errorLog;
				displayProject();
				unsavedChanges=false;

				recentFiles.updateList(filepath);
				listenTableChanges=true;
				frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
			
		}catch(Exception e){
			JOptionPane.showMessageDialog(frmMain, "Error: "+e.toString());
			errorLog.recordError(e);
			listenTableChanges=true;
			frmMain.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	private boolean saveChangesPrompt(){
		boolean proceed=true;
		if(unsavedChanges){
			int choice=JOptionPane.showConfirmDialog(frmMain, curProject.name+" has unsaved changes that will be lost.  Do you want to save now?");
			if(choice==JOptionPane.YES_OPTION){saveProject();}
			else if(choice==JOptionPane.CANCEL_OPTION || choice==JOptionPane.CLOSED_OPTION){
				proceed=false;
			}
		}
		return(proceed);
	}
	
	private void updateScatter(int selected){
		if(chartDataScatter.getSeriesCount()==2){
			chartDataScatter.removeSeries("selected");
			chartDataScatter.removeSeries("dots");
		}
		else if(chartDataScatter.getSeriesCount()==1){
			chartDataScatter.removeSeries("dots");
		}
		
		XYPlot plot = chartScatter.getXYPlot();
		plot.clearDomainMarkers();
		plot.clearRangeMarkers();
		if(xAxis==0){ //Cost
			ValueMarker marker = new ValueMarker(constraints.maxBudget);
    		marker.setPaint(Color.red);
    		plot.addDomainMarker(marker);
		}
		if(yAxis==0){ //Cost
			ValueMarker marker = new ValueMarker(constraints.maxBudget);
    		marker.setPaint(Color.red);
    		plot.addRangeMarker(marker);
		
		}
						
		int numRows=modelSearchResults.getRowCount();
		dataScatter=new double[2][numRows];
		for(int r=0; r<numRows; r++){
			SampleDesign curDesign=results.get(r);
			dataScatter[0][r]=curDesign.getValue(xAxis);
			dataScatter[1][r]=curDesign.getValue(yAxis);
		}
		chartDataScatter.addSeries("dots", dataScatter);
		
		if(selected!=-1){
			double curPoint[][]=new double[2][1];
			SampleDesign curDesign=results.get(selected);
			curPoint[0][0]=curDesign.getValue(xAxis);
			curPoint[1][0]=curDesign.getValue(yAxis);
			chartDataScatter.addSeries("selected", curPoint);
		}
	}
	
	private void exit(){
		if(saveChangesPrompt()){
			System.exit(0);
		}
	}
}
