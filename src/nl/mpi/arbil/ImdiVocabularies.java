package nl.mpi.arbil;

import java.io.File;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Document   : ImdiVocabularies
 * Created on : 
 * @author Peter.Withers@mpi.nl
 */
public class ImdiVocabularies {

    Hashtable<String, Vocabulary> vocabulariesTable = new Hashtable<String, Vocabulary>();
    // IMDIVocab has been abandoned, the mpi.vocabs.IMDIVocab class is too scary
    // every time IMDIVocab in the API is called (correction its in a static stansa ARRRGGG) it downloads the mpi homepage before anything else
    // mpi.vocabs.IMDIVocab cv = mpi.vocabs.IMDIVocab.get(vocabularyLocation);
    // cv.cslist2array(vocabularyLocation)
    // the output of the following looks similar however they differ slightly and only a few have comments
//        System.out.println("CVentries: "+cv.getCVentries());
//        System.out.println("NameEntries: "+cv.getNameEntries());
//        System.out.println("ValueEntries: "+cv.getValueEntries()); // this one is has comments
//        System.out.println("VocabHashtable: "+cv.getVocabHashtable());
//        System.out.println("Vocabs: "+cv.getVocabs());
//        System.out.println("DescriptionEntries: "+cv.getDescriptionEntries());
//        System.out.println("Entries: "+cv.getEntries());      
//    }
    static private ImdiVocabularies singleInstance = null;

    static synchronized public ImdiVocabularies getSingleInstance() {
        if (singleInstance == null) {
            singleInstance = new ImdiVocabularies();
        }
        return singleInstance;
    }

    private ImdiVocabularies() {
    }

    public boolean vocabularyContains(String vocabularyLocation, String valueString) {
        if (!vocabulariesTable.containsKey(vocabularyLocation)) {
            parseRemoteFile(vocabularyLocation);
        }
        Vocabulary tempVocab = vocabulariesTable.get(vocabularyLocation);
        if (tempVocab != null) {
            return (null != tempVocab.findVocabularyItem(valueString));
        } else {
            return false;
        }
    }

    public void redownloadCurrentlyLoadedVocabularies() {
        int succeededCount = 0;
        for (String currentUrl : vocabulariesTable.keySet()) {
            if (LinorgSessionStorage.getSingleInstance().replaceCacheCopy(currentUrl)) {
                succeededCount++;
            }
        }
        LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Downloaded " + succeededCount + " out of the " + vocabulariesTable.size() + " vocabularies currently in use.\nYou will need to restart the application for the new vocabularies to take effect.", "Re-download Current Vocabularies");
    }

    public Vocabulary getVocabulary(ImdiField originatingImdiField, String vocabularyLocation) {
        if (originatingImdiField != null) {
            if (vocabularyLocation == null) {// || vocabularyLocation.length() == 0) {
                return null;
            }
            String fieldPath = originatingImdiField.getGenericFullXmlPath();
            // testing code for checking the language fields have the required triggers
            if (vocabularyLocation.endsWith("MPI-Languages.xml")) {
                boolean foundTrigger = false;
//            System.out.println("vocabularyLocation: " + vocabularyLocation);
//            System.out.println("Field: " + originatingImdiField.getFullXmlPath());
                for (String[] currentTrigger : originatingImdiField.parentImdi.getNodeTemplate().fieldTriggersArray) {
                    if (fieldPath.equals(currentTrigger[0])) {
                        foundTrigger = true;
                    }
                }
                if (!foundTrigger) {
                    if (!fieldPath.equals(".METATRANSCRIPT.Session.Resources.LexiconResource(x).MetaLanguages.Language")) {
                        GuiHelper.linorgBugCatcher.logError(new Exception("Missing Field Trigger for: " + fieldPath + " in " + originatingImdiField.parentImdi.getUrlString()));
                    }
                }
            }
            ///////////////////////////////
            // look for genre / sub genre redirects in the template
            String vocabularyRedirectField = null;
            for (String[] currentRedirect : originatingImdiField.parentImdi.getNodeTemplate().genreSubgenreArray) {
                if (fieldPath.equals(currentRedirect[0])) {
                    vocabularyRedirectField = currentRedirect[1];
                }
            }
            if (vocabularyRedirectField != null) {
                ImdiField[] tempField = originatingImdiField.getSiblingField(vocabularyRedirectField);
                if (tempField != null) {
                    String redirectFieldString = tempField[0].toString();
                    // TODO: this may need to put the (\d) back into the (x) as is done for the FieldChangeTriggers
                    Vocabulary tempVocabulary = tempField[0].getVocabulary();
                    VocabularyItem redirectFieldVocabItem = tempVocabulary.findVocabularyItem(redirectFieldString);
//                System.out.println("redirectFieldString: " + redirectFieldString);
                    if (redirectFieldVocabItem != null && redirectFieldVocabItem.followUpVocabulary != null) {
                        System.out.println("redirectFieldVocabItem.followUpVocabulary: " + redirectFieldVocabItem.followUpVocabulary);
                        String correctedUrl = tempVocabulary.resolveFollowUpUrl(redirectFieldVocabItem.followUpVocabulary);
                        // change the requested vocabulary string to the redirected value
                        vocabularyLocation = correctedUrl;
                        System.out.println("redirected vocabularyLocation: " + vocabularyLocation);
                    }
                }
            }
        }
        ///////////////////////////////
        if (vocabularyLocation == null || vocabularyLocation.length() == 0) {
            return null;
        } else {
            if (!vocabulariesTable.containsKey(vocabularyLocation)) {
                parseRemoteFile(vocabularyLocation);
            }
            return vocabulariesTable.get(vocabularyLocation);
        }
    }

    public Vocabulary getEmptyVocabulary(String vocabularyLocation) {
        System.out.println("getEmptyVocabulary: " + vocabularyLocation);
        if (vocabularyLocation == null || vocabularyLocation.length() == 0) {
            return null;
        } else {
            if (!vocabulariesTable.containsKey(vocabularyLocation)) {
                Vocabulary vocabulary = new Vocabulary(vocabularyLocation);
                vocabulariesTable.put(vocabularyLocation, vocabulary);
                return vocabulary;
            }
            return vocabulariesTable.get(vocabularyLocation);
        }
    }

    synchronized public void parseRemoteFile(String vocabRemoteUrl) {
        if (vocabRemoteUrl != null && !vocabulariesTable.containsKey(vocabRemoteUrl)) {
            File cachedFile = LinorgSessionStorage.getSingleInstance().updateCache(vocabRemoteUrl, null, false, new DownloadAbortFlag(), null);
            // this delete is for testing only!!! new File(cachePath).delete();
            if (!cachedFile.exists()) {
                String backupPath = "/nl/mpi/arbil/resources/IMDI/FallBack/" + cachedFile.getName();
                System.out.println("backupPath: " + backupPath);
                URL backUp = this.getClass().getResource(backupPath);
                if (backUp != null) {
                    LinorgSessionStorage.getSingleInstance().saveRemoteResource(backUp, cachedFile, null, true, new DownloadAbortFlag(), null);
                }
            }
            System.out.println("parseRemoteFile: " + cachedFile);
            Vocabulary vocabulary = new Vocabulary(vocabRemoteUrl);
            vocabulariesTable.put(vocabRemoteUrl, vocabulary);
            try {
                javax.xml.parsers.SAXParserFactory saxParserFactory = javax.xml.parsers.SAXParserFactory.newInstance();
                javax.xml.parsers.SAXParser saxParser = saxParserFactory.newSAXParser();
                org.xml.sax.XMLReader xmlReader = saxParser.getXMLReader();
                xmlReader.setFeature("http://xml.org/sax/features/validation", false);
                xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
                xmlReader.setContentHandler(new SaxVocabularyHandler(vocabulary));
                xmlReader.parse(cachedFile.getCanonicalPath());
            } catch (Exception ex) {
                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("A controlled vocabulary could not be read.\n" + vocabRemoteUrl + "\nSome fields may not show all options.", "Load Controlled Vocabulary");
            }
        }
    }

    private class SaxVocabularyHandler extends org.xml.sax.helpers.DefaultHandler {

        Vocabulary collectedVocab;
        VocabularyItem currentVocabItem = null;

        public SaxVocabularyHandler(Vocabulary vocabList) {
            super();
            collectedVocab = vocabList;
        }

        @Override
        public void characters(char[] charArray, int start, int length) {
            if (currentVocabItem != null) {
                String nodeContents = "";
                for (int charCounter = start; charCounter < start + length; charCounter++) {
                    nodeContents = nodeContents + charArray[charCounter];
                }
                currentVocabItem.descriptionString = nodeContents;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            currentVocabItem = null;
        }

        @Override
        public void startElement(String uri, String name, String qName, org.xml.sax.Attributes atts) {
//            System.out.println("startElement: " + name);
//            this VocabularyRedirect code has been replaced by the templates
//            if (name.equals("VocabularyRedirect")) { // or should this be Redirect
//                // when getting the list check attribute in the field X for the vocab location
//                collectedVocab.vocabularyRedirectField = atts.getValue("SourceFieldName");
//                System.out.println("VocabularyRedirect: " + collectedVocab.vocabularyRedirectField);
//            }
            if (name.equals("Entry")) {
                String vocabName = atts.getValue("Value");
                String vocabCode = atts.getValue("Code");
                String followUpVocab = atts.getValue("FollowUp");
                currentVocabItem = new VocabularyItem(vocabName, vocabCode, followUpVocab);
                collectedVocab.vocabularyItems.add(currentVocabItem);
            }
        }
    }

    public class Vocabulary {

        private Vector<VocabularyItem> vocabularyItems = new Vector<VocabularyItem>();
//        this VocabularyRedirect code has been replaced by the templates
//        public String vocabularyRedirectField = null; // the sibling imdi field that changes this vocabularies location
        public String vocabularyUrlRedirected = null; // the url of the vocabulary indicated by the value of the vocabularyRedirectField
        private String vocabularyUrl = null;

        public Vocabulary(String locationUrl) {
            vocabularyUrl = locationUrl;
        }

        public void addEntry(String entryString, String entryCode) {
            boolean itemExistsInVocab = false;
            for (VocabularyItem currentVocabularyItem : vocabularyItems.toArray(new VocabularyItem[]{})) {
                if (currentVocabularyItem.languageName.equals(entryString)) {
                    itemExistsInVocab = true;
                }
            }
            if (!itemExistsInVocab) {
                vocabularyItems.add(new VocabularyItem(entryString, entryCode, null));
            }
        }

        public VocabularyItem findVocabularyItem(String searchString) {
            for (VocabularyItem currentVocabularyItem : vocabularyItems.toArray(new VocabularyItem[]{})) {
                if (currentVocabularyItem.languageName.equals(searchString)) {
                    return currentVocabularyItem;
                }
            }
            return null;
        }

        public String resolveFollowUpUrl(String folowUpString) {
            vocabularyUrlRedirected = folowUpString;
            String vocabUrlDirectory = vocabularyUrl.substring(0, vocabularyUrl.lastIndexOf("/") + 1);
            System.out.println("vocabUrlDirectory: " + vocabUrlDirectory);
            return (vocabUrlDirectory + folowUpString);
        }

        public VocabularyItem[] getVocabularyItems() {
            return vocabularyItems.toArray(new VocabularyItem[]{});
        }
    }

    public class VocabularyItem implements Comparable {

        public String languageName;
        public String languageCode;
        public String followUpVocabulary;
        public String descriptionString;

        public VocabularyItem(String languageNameLocal, String languageCodeLocal, String followUpVocabularyLocal) {
            languageName = languageNameLocal;
            languageCode = languageCodeLocal;
            followUpVocabulary = followUpVocabularyLocal;
        }

        @Override
        public String toString() {
            return languageName;
        }

        public int compareTo(Object otherObject) {
            return this.toString().compareTo(otherObject.toString());
        }
    }
}
