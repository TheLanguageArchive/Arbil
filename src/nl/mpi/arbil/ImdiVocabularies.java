package nl.mpi.arbil;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Document   : ImdiVocabularies
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ImdiVocabularies {

    Hashtable<String, Vector> vocabulariesTable = new Hashtable();

    public ImdiVocabularies() {
//        parseRemoteFile("/home/petwit/IMDI-Tools/Profiles/local/DBD_Profile.Profile.xml");
//        parseRemoteFile("http://www.mpi.nl/world/corpus/HTMLcorpusContent.html");
//        parseRemoteFile("http:///www.mpi.nl/world/corpus/HTMLcorpusContent.html");
//        parseRemoteFile("http:////www.mpi.nl/world/corpus/HTMLcorpusContent.html");
//        getVocabulary("http://www.mpi.nl/IMDI/Schema/Continents.xml");
//        System.exit(0);
    }

//    private void useIMDIVocab(String vocabularyLocation) {        
    // this has been abandoned, the mpi.vocabs.IMDIVocab class is too scary
    // every time this is called (correction its in a static stansa ARRRGGG) it downloads the mpi homepage before anything else 
//        mpi.vocabs.IMDIVocab cv = mpi.vocabs.IMDIVocab.get(vocabularyLocation);
    //cv.cslist2array(vocabularyLocation)
    // the output of the following looks similar however they differ slightly and only a few have comments
//        System.out.println("CVentries: "+cv.getCVentries());
//        System.out.println("NameEntries: "+cv.getNameEntries());
//        System.out.println("ValueEntries: "+cv.getValueEntries()); // this one is has comments
//        System.out.println("VocabHashtable: "+cv.getVocabHashtable());
//        System.out.println("Vocabs: "+cv.getVocabs());
//        System.out.println("DescriptionEntries: "+cv.getDescriptionEntries());
//        System.out.println("Entries: "+cv.getEntries());      
//    }
    public void addVocabularyEntry(String vocabularyLocation, String entryString) {
        Vector<VocabularyItem> tempVocab = vocabulariesTable.get(vocabularyLocation);
        if (!tempVocab.contains(entryString)) {
            tempVocab.add(new VocabularyItem(entryString, null));
        }
    }

    private VocabularyItem findVocabularyItem(Enumeration<VocabularyItem> vocabularyEmun, String searchString) {
        while (vocabularyEmun.hasMoreElements()) {
            VocabularyItem currentVocabItem = vocabularyEmun.nextElement();
            if (currentVocabItem.languageName.equals(searchString)) {
                return currentVocabItem;
            }
        }
        return null;
    }

    public boolean vocabularyContains(String vocabularyLocation, String valueString) {
        if (!vocabulariesTable.containsKey(vocabularyLocation)) {
            parseRemoteFile(vocabularyLocation);
        }
        Vector<VocabularyItem> tempVocab = vocabulariesTable.get(vocabularyLocation);
        if (tempVocab != null) {
            return (findVocabularyItem(tempVocab.elements(), valueString) != null);
        } else {
            return false;
        }
    }

    public Enumeration<VocabularyItem> getVocabulary(String vocabularyLocation) {
        if (vocabularyLocation == null || vocabularyLocation.length() == 0) {
            return null;
        }
        if (!vocabulariesTable.containsKey(vocabularyLocation)) {
            parseRemoteFile(vocabularyLocation);
        }
//        Vector vocabularyList;
//        if (vocabulariesTable.containsKey(vocabularyLocation)) {
//            return (Vector) vocabulariesTable.get(vocabularyLocation);
//        } else {
//            vocabularyList = new Vector();
//            vocabularyList.add("time: " + System.currentTimeMillis());
//            vocabularyList.add("time: " + System.currentTimeMillis());
//            vocabularyList.add("time: " + System.currentTimeMillis());
//            vocabularyList.add("time: " + System.currentTimeMillis());
//            vocabularyList.add("time: " + System.currentTimeMillis());
//            vocabularyList.add("time: " + System.currentTimeMillis());
//            vocabulariesTable.put(vocabularyLocation, vocabularyList);
//        }
        Vector<VocabularyItem> tempVocab = vocabulariesTable.get(vocabularyLocation);
        if (tempVocab != null) {
            return tempVocab.elements();
        } else {
            return null;
        }
    }

    synchronized public void parseRemoteFile(String vocabRemoteUrl) {
        if (vocabRemoteUrl != null && !vocabulariesTable.containsKey(vocabRemoteUrl)) {
            String cachePath = GuiHelper.linorgSessionStorage.updateCache(vocabRemoteUrl, false);
//            new File(cachePath).delete(); // TODO: remove me!!!
            if (!new File(cachePath).exists()) {
                URL backUp = this.getClass().getResource("/nl/mpi/arbil/resources/IMDI/Fallback/" + new File(cachePath).getName());
                if (backUp != null) {
                    cachePath = backUp.getFile();
                }
            }
//            if (!new File(cachePath).exists()) {
//                if (!missingVocabMessageShown && GuiHelper.linorgWindowManager.linorgFrame != null && GuiHelper.linorgWindowManager.linorgFrame.isShowing()) {
//                    missingVocabMessageShown = true;
//                    GuiHelper.linorgWindowManager.addMessageDialogToQueue("A controlled vocabulary could not be accessed.\nSome fields may not show all options.");
//                }
//            }
            System.out.println("parseRemoteFile: " + cachePath);
            Vector<VocabularyItem> vocabularyList = new Vector();
            vocabulariesTable.put(vocabRemoteUrl, vocabularyList);
            try {
                javax.xml.parsers.SAXParserFactory saxParserFactory = javax.xml.parsers.SAXParserFactory.newInstance();
                javax.xml.parsers.SAXParser saxParser = saxParserFactory.newSAXParser();
                org.xml.sax.XMLReader xmlReader = saxParser.getXMLReader();
                xmlReader.setFeature("http://xml.org/sax/features/validation", false);
                xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
                xmlReader.setContentHandler(new SaxVocabularyHandler(vocabularyList));
                xmlReader.parse(cachePath);
            } catch (Exception ex) {
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("A controlled vocabulary could not be read.\n" + vocabRemoteUrl + "\nSome fields may not show all options.");
//                    brokenVocabMessageShown = true;
//                }
            //GuiHelper.linorgBugCatcher.logError(ex);
//                System.out.println("Deleting file presumed erroneous");
//                new File(cachePath).delete(); // delete the file on the assumption that it is corrupt
            // if the vocab could not be loaded then add the key to prevent retries and set the value to null to indicate that there was an error
//                System.out.println("Inserting null vocab to prevent repeated server hits: " + vocabRemoteUrl);
//                vocabulariesTable.put(vocabRemoteUrl, null); // prevent further attempts in this application instance
//            System.out.println(ex.getMessage());
            }
            System.out.println("vocabularyList: " + vocabularyList);
        }
    }

    private class SaxVocabularyHandler extends org.xml.sax.helpers.DefaultHandler {

        Vector<VocabularyItem> collectedVocab;

        public SaxVocabularyHandler(Vector<VocabularyItem> vocabList) {
            super();
            collectedVocab = vocabList;
        }

        @Override
        public void startElement(String uri, String name, String qName, org.xml.sax.Attributes atts) {
            if (name.equals("Entry")) {
                String vocabName = atts.getValue("Value");
                String vocabCode = atts.getValue("Code");
                if (vocabName != null) {
                    collectedVocab.add(new VocabularyItem(vocabName, vocabCode));
                }
            }
        }
    }

    public class VocabularyItem {

        public String languageName;
        public String languageCode;

        public VocabularyItem(String languageNameLocal, String languageCodeLocal) {
            languageName = languageNameLocal;
            languageCode = languageCodeLocal;
        }

        @Override
        public String toString() {
            return languageName;
        }
    }
}
