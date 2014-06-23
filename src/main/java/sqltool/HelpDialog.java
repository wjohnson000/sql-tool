/*
 * Created on Mar 16, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package sqltool;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import sqltool.common.SqlToolkit;


/**
 * @author wjohnson
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class HelpDialog extends JDialog  {

	private static final long serialVersionUID = -8582130974454553238L;

	String tempText =
		"<html>\n" +
		"<head><title>SqlLite Help</title></head>\n" +
		"<body>\n" +
		"<strong>Help for this application ...</strong><br>\n" +
		"... is on the way.  Please stay tuned.\n" +
		"</body>\n" +
		"</html>";
	
	public HelpDialog(Frame parent) {
		super(parent);
		SqlToolkit.appLogger.logDebug("Creating a 'HelpDialog' ...");
		try {
			buildUI();
		} catch(Exception e) {
			e.printStackTrace();
		}
		pack();
	}
	
	private void buildUI() {
		JLabel mainLabel = new JLabel();
		JButton closeBtn = new JButton("Close");
		JTextPane helpArea = new JTextPane();
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().add(helpArea);
		helpArea.setEditorKit(new javax.swing.text.html.HTMLEditorKit());
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		setResizable(true);
		setSize(720, 540);
		setTitle("SqlLite Help ...");
		mainLabel.setText("Select the topic for help ...");
		
		helpArea.setText(tempText);
		helpArea.setEditable(false);
		closeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		});
		
		getContentPane().setLayout(new GridBagLayout());
		getContentPane().add(
				mainLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
						GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(8, 8, 0, 0), 0, 0));
		getContentPane().add(
				scrollPane, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
						GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(4, 8, 0, 0), 0, 0));
		getContentPane().add(
				closeBtn, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(6, 0, 8, 0), 0, 0));
	}
}
