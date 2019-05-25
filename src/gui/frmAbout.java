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

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextArea;

import data.ErrorLog;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import java.awt.Font;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

/**
* @author Zachary J. Ward (zward@hsph.harvard.edu)
* @version 2.0
*/

public class frmAbout {
	
	public JDialog frmAbout;
	String version;
	ErrorLog errorLog;
	JTextArea textArea;
	
	/**
	 *  Default Constructor
	 */
	public frmAbout(String version, ErrorLog errorLog) {
		this.version=version;
		this.errorLog=errorLog;
		initialize();
	}

	/**
	 * Initializes the contents of the frame, including ActionListeners for the Combo-boxes and buttons on the form.
	 */
	private void initialize() {
		try{
			frmAbout = new JDialog();
			frmAbout.setModalityType(ModalityType.APPLICATION_MODAL);
			frmAbout.setTitle("About the EPIC Sampler");
			frmAbout.setResizable(false);
			frmAbout.setBounds(100, 100, 500, 400);
			frmAbout.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frmAbout.getContentPane().setLayout(null);
									
			JButton btnOk = new JButton("OK");
			btnOk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frmAbout.dispose();
				}
			});
			btnOk.setBounds(392, 334, 90, 28);
			frmAbout.getContentPane().add(btnOk);
			
			JScrollPane scrollPane_1 = new JScrollPane();
			scrollPane_1.setBounds(6, 117, 476, 194);
			frmAbout.getContentPane().add(scrollPane_1);
			
			textArea = new JTextArea();
			textArea.setWrapStyleWord(true);
			textArea.setText("The EPIC Sampler is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.\r\n\r\nThe EPIC Sampler is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.\r\n\r\nYou should have received a copy of the GNU General Public License along with the EPIC Sampler.  If not, see <http://www.gnu.org/licenses/>.");
			textArea.setLineWrap(true);
			textArea.setEditable(false);
			scrollPane_1.setViewportView(textArea);
			
			JLabel lblLogo = new JLabel("");
			lblLogo.setIcon(new ImageIcon(frmAbout.class.getResource("/images/EPIC-Logo_48.png")));
			lblLogo.setBounds(71, 10, 48, 43);
			frmAbout.getContentPane().add(lblLogo);
			
			JLabel lblTitle = new JLabel("EPIC Sampler");
			lblTitle.setFont(new Font("Bell MT", Font.BOLD, 28));
			lblTitle.setBounds(131, 10, 225, 42);
			frmAbout.getContentPane().add(lblTitle);
			
			JLabel lblVersion = new JLabel("Version "+version);
			lblVersion.setFont(new Font("SansSerif", Font.PLAIN, 14));
			lblVersion.setBounds(131, 54, 182, 16);
			frmAbout.getContentPane().add(lblVersion);
			
			JLabel lblURL = new JLabel("<HTML><FONT color=\"#000099\"><U>http://immunizationeconomics.org/sample-design-optimizer</U></FONT></HTML>");
			lblURL.setFont(new Font("SansSerif", Font.PLAIN, 13));
			lblURL.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						Desktop.getDesktop().browse(new URI("http://immunizationeconomics.org/sample-design-optimizer"));
					} catch (Exception e1) {
						e1.printStackTrace();
						errorLog.recordError(e1);
					}
				}
			});
			lblURL.setBounds(131, 76, 351, 20);
			lblURL.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
			frmAbout.getContentPane().add(lblURL);
			
			JLabel lblGnuGeneralPublic = new JLabel("GNU General Public License:");
			lblGnuGeneralPublic.setBounds(6, 126, 182, 16);
			frmAbout.getContentPane().add(lblGnuGeneralPublic);
			
			JLabel lblNewLabel = new JLabel("\u00A9 2018-2019 CHDS");
			lblNewLabel.setBounds(131, 98, 182, 16);
			frmAbout.getContentPane().add(lblNewLabel);
			
			
		} catch (Exception ex){
			ex.printStackTrace();
			errorLog.recordError(ex);
		}
	}
}
