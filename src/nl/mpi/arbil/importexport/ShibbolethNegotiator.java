package nl.mpi.arbil.importexport;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyStoreException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.Calendar;
import javax.swing.JButton;
import javax.swing.JPanel;
import nl.mpi.arbil.GuiHelper;
import nl.mpi.arbil.LinorgWindowManager;
import nl.nikhef.slcshttps.CAHttps;
import nl.nikhef.slcshttps.SURFCAHttps;
import nl.nikhef.slcshttps.PKCS12Https;
import nl.nikhef.slcshttps.gui.SURFCAInitDialog;
import nl.nikhef.slcshttps.gui.CATool;
import nl.nikhef.slcshttps.gui.GraphTools;
import nl.nikhef.slcshttps.trust.HttxURLConnection;

/**
 * Document   : ShibbolethNegotiator
 * Created on : Dec 1, 2009, 11:29:52 PM
 * @author Peter.Withers@mpi.nl
 */
public class ShibbolethNegotiator implements ActionListener {

    CAHttps myCA = null;
    JPanel shibbolethControlPanel = null;
    JButton infoCaMenuItem;

    public ShibbolethNegotiator() {
        // TODO: find a home for these properties, maybe in the jar file
        System.setProperty(SURFCAHttps.CERT_URL_PROPERTY, "https://knikker.surfnet.nl/onlineca/x509.php");
        System.setProperty(SURFCAHttps.AUTH_URL_PROPERTY, "https://knikker.surfnet.nl/onlineca/x509.php?hash=");
    }

    public boolean checkCertDate() {
        Calendar dateCheckCalendar = Calendar.getInstance();
        System.out.println("Today : " + dateCheckCalendar.getTime());

        // Substract 1 days from the calendar so that it is know that the cert will not expire durring the process
        dateCheckCalendar.add(Calendar.DATE, -12);
        try {
            myCA.getCertificate().checkValidity(dateCheckCalendar.getTime());
        } catch (CertificateExpiredException cee) {
            return false;
        } catch (CertificateNotYetValidException cnyve) {
            return false;
        } catch (KeyStoreException kse) {
            return false;
        }
        return true;
    }

    public HttpURLConnection getShibbolethConnection(HttpURLConnection currentConnection) {
        try {
            return new HttxURLConnection(currentConnection);
        } catch (IOException ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The Shibboleth Connection failed with: " + ex.getMessage(), "Shibboleth Connection");
            return currentConnection;
        }
    }

    public void actionPerformed(ActionEvent e) {

        // CA initialization
        if (e.getActionCommand().equals("caInit")) {
            try {
                /*
                myCA=new SURFCAHttps();
                ((SURFCAHttps)myCA).initialize(new PopupCommunicator());
                ((SURFCAHttps)myCA).storeCertificate();
                myCA.setHttpsSSLSocketFactory();*/
//		myCA=SURFCAInitDialog.getDialog((SURFCAHttps)myCA);
                myCA = SURFCAInitDialog.getDialog(myCA);
            } catch (Exception caExcep) {
                GuiHelper.linorgBugCatcher.logError(caExcep);
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The Shibboleth 'CA Init' failed with: " + caExcep.getMessage(), "Shibboleth Connection");
            }
        }
        // PKCS12 import initialization
        if (e.getActionCommand().equals("pkcs12Import")) {
            try {
                if (myCA == null || GraphTools.confirmPopup("CA exists, replace?")) {
                    myCA = new PKCS12Https();
                    ((PKCS12Https) myCA).initialize();
                    ((PKCS12Https) myCA).storeCertificate();
//		    myCA.setHttpsSSLSocketFactory();
                    myCA.setHttxSSLSocketFactory();
                }
            } catch (Exception caExcep) {
                GuiHelper.linorgBugCatcher.logError(caExcep);
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The Shibboleth 'pkcs12 Import' failed with: " + caExcep.getMessage(), "Shibboleth Connection");
            }
        }
        // Certificate Information
        if (e.getActionCommand().equals("caInfo")) {
            try {
                CATool.showCATool(myCA);
            } catch (Exception certExcep) {
                GuiHelper.linorgBugCatcher.logError(certExcep);
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("The Shibboleth 'CA Info' failed with: " + certExcep.getMessage(), "Shibboleth Connection");
            }
        }
        if (infoCaMenuItem != null) {
            infoCaMenuItem.setEnabled(myCA != null);
        }
    }

    public JPanel getControlls() {
        if (shibbolethControlPanel == null) {
            shibbolethControlPanel = new JPanel();
            shibbolethControlPanel.setLayout(new javax.swing.BoxLayout(shibbolethControlPanel, javax.swing.BoxLayout.PAGE_AXIS));
            shibbolethControlPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
            infoCaMenuItem = new JButton("Certificate Information");
            infoCaMenuItem.setMnemonic('N');
            infoCaMenuItem.setActionCommand("caInfo");
            infoCaMenuItem.addActionListener(this);
            infoCaMenuItem.setEnabled(myCA != null);
            shibbolethControlPanel.add(infoCaMenuItem);
            JButton initCaMenuItem = new JButton("Log in");
            initCaMenuItem.setMnemonic('I');
            initCaMenuItem.setActionCommand("caInit");
            initCaMenuItem.addActionListener(this);
            shibbolethControlPanel.add(initCaMenuItem);
            JButton pkcs12ImportMenuItem = new JButton("Import PKCS12 file");
            pkcs12ImportMenuItem.setMnemonic('P');
            pkcs12ImportMenuItem.setActionCommand("pkcs12Import");
            pkcs12ImportMenuItem.addActionListener(this);
            shibbolethControlPanel.add(pkcs12ImportMenuItem);
        }
        return shibbolethControlPanel;
    }
}
