
package gui;

import javax.swing.JFrame;
import java.awt.Dialog.ModalityType;

import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextArea;

import data.ErrorLog;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 *
 */
public class frmErrorLog {
	
	public JDialog frmErrorLog;
	String version;
	JTextArea txtLog;
	ErrorLog log;
	
	/**
	 *  Default Constructor
	 */
	public frmErrorLog(ErrorLog log) {
		this.log=log;
		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmErrorLog = new JDialog();
			frmErrorLog.setModalityType(ModalityType.APPLICATION_MODAL);
			frmErrorLog.setTitle("EPIC Sampler - Error Log");
			frmErrorLog.setResizable(false);
			frmErrorLog.setBounds(100, 100, 800, 600);
			frmErrorLog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmErrorLog.getContentPane().setLayout(null);
									
			JButton btnOk = new JButton("OK");
			btnOk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmErrorLog.dispose();
				}
			});
			btnOk.setBounds(698, 531, 90, 28);
			frmErrorLog.getContentPane().add(btnOk);
			
			JScrollPane scrollPane_1 = new JScrollPane();
			scrollPane_1.setBounds(6, 6, 782, 513);
			frmErrorLog.getContentPane().add(scrollPane_1);
			
			txtLog = new JTextArea();
			txtLog.setWrapStyleWord(true);
			txtLog.setLineWrap(true);
			txtLog.setEditable(false);
			scrollPane_1.setViewportView(txtLog);
			
			//Display log
			txtLog.append("Version: "+log.version+"\n\n");
			int numErrors=log.errors.size();
			txtLog.append("Errors ("+numErrors+"):\n");
			for(int i=0; i<numErrors; i++){
				txtLog.append(log.errors.get(i)+"\n");
			}
			txtLog.append("\n\n");
			txtLog.append("---------------------------------------------------------------------------------------------------\n");
			txtLog.append("System properties\n");
			for(int i=0; i<log.systemInfo.length; i++){
				txtLog.append(log.systemInfo[i][0]+": "+log.systemInfo[i][1]+"\n");
			}
			txtLog.setCaretPosition(0);
			
			
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
}
