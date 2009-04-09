/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpi.linorg;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author petwit
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

    public void parseRemoteFile(String vocabRemoteUrl) {
        String cachePath = GuiHelper.linorgSessionStorage.updateCache(vocabRemoteUrl, false);

        System.out.println("parseRemoteFile: " + cachePath);
        try {
//            javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
//            factory.setValidating(true);
//            factory.setNamespaceAware(true);
//            javax.xml.parsers.SAXParser parser = factory.newSAXParser();
//
//            org.xml.sax.XMLReader reader = parser.getXMLReader();
//            reader.setErrorHandler(new saxVocabularyHandler());
//            reader.parse(new org.xml.sax.InputSource(new java.io.FileReader(vocabUrl)));

            javax.xml.parsers.SAXParserFactory saxParserFactory = javax.xml.parsers.SAXParserFactory.newInstance();
            javax.xml.parsers.SAXParser saxParser = saxParserFactory.newSAXParser();
            org.xml.sax.XMLReader xmlReader = saxParser.getXMLReader();
            //xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setFeature("http://xml.org/sax/features/validation", false);
            xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
            Vector<VocabularyItem> vocabularyList = new Vector();
            xmlReader.setContentHandler(new SaxVocabularyHandler(vocabularyList));
            xmlReader.parse(cachePath);
            vocabulariesTable.put(vocabRemoteUrl, vocabularyList);
//            org.xml.sax.XMLReader xmlReader = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
//            saxVocabularyHandler handler = new saxVocabularyHandler();
//            xmlReader.setContentHandler(handler);
//            xmlReader.setErrorHandler(handler);
//            java.io.FileReader r = new java.io.FileReader(vocabUrl);
//            xmlReader.parse(new org.xml.sax.InputSource(r));
        //xmlReader.parse(new BufferedInputStream(inputStreamFromURLConnection), saxDefaultHandler);
        } catch (Exception ex) {
            GuiHelper.linorgBugCatcher.logError(ex);
        // if the vocab could not be loaded then add the key to prevent retries and set the value to null to indicate that there was an error
        //vocabulariesTable.put(vocabUrl, null);
//            System.out.println(ex.getMessage());
        }
    }

    private class SaxVocabularyHandler extends org.xml.sax.helpers.DefaultHandler {

        Vector<VocabularyItem> collectedVocab;

        public SaxVocabularyHandler(Vector<VocabularyItem> vocabList) {
            super();
            collectedVocab = vocabList;
        }
        ////////////////////////////////////////////////////////////////////
        // Event handlers.
        ////////////////////////////////////////////////////////////////////
//        public void startDocument() {
//            System.out.println("Start document");
//        }
//
//        public void endDocument() {
//            System.out.println("End document");
////            vocabulariesTable
//        }
        @Override
        public void startElement(String uri, String name, String qName, org.xml.sax.Attributes atts) {
            if (name.equals("Entry")) {
                String vocabName = atts.getValue("Value");
                String vocabCode = atts.getValue("Code");
                if (vocabName != null) {
                    collectedVocab.add(new VocabularyItem(vocabName, vocabCode));
                }
            }
//            if ("".equals(uri)) {
//                System.out.println("Start element: " + qName);
//                //System.out.println("Start element atts: " + atts);
//            } else {
//                System.out.println("Start element: {" + uri + "}" + name);
//                System.out.println("Start element atts: " + atts.getValue("Value"));
//            }
        }
//        public void endElement(String uri, String name, String qName) {
//            if ("".equals(uri)) {
//                System.out.println("End element: " + qName);
//            } else {
//                System.out.println("End element:   {" + uri + "}" + name);
//            }
//        }
//
//        public void characters(char ch[], int start, int length) {
//            System.out.print("Characters:    \"");
//            for (int i = start; i < start + length; i++) {
//                switch (ch[i]) {
//                    case '\\':
//                        System.out.print("\\\\");
//                        break;
//                    case '"':
//                        System.out.print("\\\"");
//                        break;
//                    case '\n':
//                        System.out.print("\\n");
//                        break;
//                    case '\r':
//                        System.out.print("\\r");
//                        break;
//                    case '\t':
//                        System.out.print("\\t");
//                        break;
//                    default:
//                        System.out.print(ch[i]);
//                        break;
//                }
//            }
//            System.out.print("\"\n");
//        }
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
