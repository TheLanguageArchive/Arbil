/*
 * Copyright (C) 2013 Max Planck Institute for Psycholinguistics
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.mpi.arbil.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.DataOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
public class ArbilLogConsole {

    private final static Logger logger = LoggerFactory.getLogger(ArbilLogConsole.class);
    private final JDialog logDialog;
    private final JTextArea logTextArea;
    private Handler logHandler;

    public ArbilLogConsole(Frame owner) {
	// Create the dialog
	logDialog = new JDialog(owner, "Arbil log console");
	logDialog.setModal(false);
	logDialog.setLayout(new BorderLayout());
	logDialog.setSize(new Dimension(600, 400));
	logDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

	// Add log text area
	logTextArea = new JTextArea();
	logTextArea.setEditable(false);
	logTextArea.setLineWrap(true);
	final JScrollPane logTextAreaScrollPane = new JScrollPane(logTextArea);
	logDialog.add(logTextAreaScrollPane, BorderLayout.CENTER);

	// Add a close button
	final JButton closeButton = new JButton(new AbstractAction("Close") {
	    public void actionPerformed(ActionEvent e) {
		logDialog.dispose();
	    }
	});
	logDialog.add(closeButton, BorderLayout.SOUTH);

	// Create listener to make sure that before disposal of the dialog, the handler gets remove from the logger
	logDialog.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosed(WindowEvent e) {
		removeHandlerFromLogger();
		logger.info("Log console closing");
	    }
	});

	// Done
	logger.debug("Log console created");
    }

    public void show() {
	addNewHandlerToLogger();
	logDialog.setVisible(true);
	logger.info("Log console shown");
    }

    private void addNewHandlerToLogger() throws SecurityException {
	logHandler = new ArbilLogConsoleHandler();
	LogManager.getLogManager().getLogger("").addHandler(logHandler);
    }

    private void removeHandlerFromLogger() throws SecurityException {
	if (logHandler != null) {
	    logger.debug("Removing log handler");
	    LogManager.getLogManager().getLogger("").removeHandler(logHandler);
	}
    }

    private class ArbilLogConsoleHandler extends Handler {

	private final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

	@Override
	public void publish(final LogRecord record) {
	    SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    logTextArea.append(String.format("%s [%s] %s - %s\n",
			    df.format(new Date(record.getMillis())),
			    record.getLevel().toString(),
			    record.getLoggerName(),
			    record.getMessage().toString()));
		    if (record.getThrown() != null) {
			printStackTrace();
		    }
		}

		private void printStackTrace() {
		    final StringWriter sw = new StringWriter();
		    record.getThrown().printStackTrace(new PrintWriter(sw, true));
		    logTextArea.append(sw.toString());
		}
	    });
	}

	@Override
	public void flush() {
	    logger.debug("Log console handler flushed");
	}

	@Override
	public void close() throws SecurityException {
	    //throw new UnsupportedOperationException("Not supported yet.");
	}
    }
}
