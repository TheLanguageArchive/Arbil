package mpi.linorg;

import java.io.File;
import java.util.Arrays;
import java.util.Hashtable;

/**
 * ArbilTemplateManager.java
 * Created on Jul 15, 2009, 11:56:57 AM
 * @author Peter.Withers@mpi.nl
 */
public class ArbilTemplateManager {

    private String defaultArbilTemplateName = "Default";
    static private ArbilTemplateManager singleInstance = null;
    private Hashtable<String, ArbilTemplate> templatesHashTable;
    private ArbilTemplate defaultArbilTemplate;

    static synchronized public ArbilTemplateManager getSingleInstance() {
        if (singleInstance == null) {
            singleInstance = new ArbilTemplateManager();
        }
        return singleInstance;
    }

    public File getTemplateFile(String currentTemplate) {
        File currentTemplateFile = new File(getTemplateDirectory().getAbsolutePath() + File.separatorChar + currentTemplate + File.separatorChar + "template.xml");
        if (!currentTemplateFile.getParentFile().exists()) {
            currentTemplateFile.getParentFile().mkdir();
        }
        return currentTemplateFile;
    }

    public String getCurrentTemplate() {
        return defaultArbilTemplateName;
    }

    public void setCurrentTemplate(String currentTemplateLocal) {
        defaultArbilTemplateName = currentTemplateLocal;
    }

    private File getTemplateDirectory() {
        return new File(LinorgSessionStorage.getSingleInstance().storageDirectory + "templates");
    }

    public String[] getAvailableTemplates() {
        File templatesDir = getTemplateDirectory();
        if (!templatesDir.exists()) {
            templatesDir.mkdir();
        }
        String[] templatesList = templatesDir.list();
        Arrays.sort(templatesList);
        return templatesList;
    }

    private ArbilTemplateManager() {
        templatesHashTable = new Hashtable<String, ArbilTemplate>();
        defaultArbilTemplate = getTemplate("DefaultArbilTemplate");
    }

    public ArbilTemplate getDefaultTemplate() {
        return defaultArbilTemplate;
    }

    public ArbilTemplate getTemplate(String templateName) {
        ArbilTemplate returnTemplate = null;
        if (templateName != null && templateName.length() > 0) {
            if (!templatesHashTable.containsKey(templateName)) {
//                LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("Template Not Found: " + templateName, "Arbil Template Manager");
                returnTemplate = new ArbilTemplate();
                returnTemplate.readTemplate(getTemplateFile(templateName));
                templatesHashTable.put(templateName, returnTemplate);
            } else {
                returnTemplate = templatesHashTable.get(templateName);
            }
        } else {
            returnTemplate = defaultArbilTemplate;
        }
        return returnTemplate;
    }

//    public void readTemplate(String templatePath) {
//        try {
//            javax.xml.parsers.SAXParserFactory saxParserFactory = javax.xml.parsers.SAXParserFactory.newInstance();
//            javax.xml.parsers.SAXParser saxParser = saxParserFactory.newSAXParser();
//            org.xml.sax.XMLReader xmlReader = saxParser.getXMLReader();
//            xmlReader.setFeature("http://xml.org/sax/features/validation", false);
//            xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
//            xmlReader.setContentHandler(new SaxVocabularyHandler(vocabulary));
//            xmlReader.parse(cachePath);
//        } catch (Exception ex) {
//            LinorgWindowManager.getSingleInstance().addMessageDialogToQueue("A controlled vocabulary could not be read.\n" + vocabRemoteUrl + "\nSome fields may not show all options.", "Load Controlled Vocabulary");
//        }
//    }

//    private class SaxVocabularyHandler extends org.xml.sax.helpers.DefaultHandler {

//        Vocabulary collectedVocab;
//        VocabularyItem currentVocabItem = null;

//        public SaxVocabularyHandler(Vocabulary vocabList) {
//            super();
//            collectedVocab = vocabList;
//        }

//        @Override
//        public void characters(char[] charArray, int start, int length) {
//            if (currentVocabItem != null) {
//                String nodeContents = "";
//                for (int charCounter = start; charCounter < start + length; charCounter++) {
//                    nodeContents = nodeContents + charArray[charCounter];
//                }
//                currentVocabItem.descriptionString = nodeContents;
//            }
//        }
//
//        @Override
//        public void endElement(String uri, String localName, String qName) {
//            currentVocabItem = null;
//        }

//        @Override
//        public void startElement(String uri, String name, String qName, org.xml.sax.Attributes atts) {
////            System.out.println("startElement: " + name);
//            if (name.equals("VocabularyRedirect")) { // or should this be Redirect
//                // when getting the list check attribute in the field X for the vocab location
//                collectedVocab.vocabularyRedirectField = atts.getValue("SourceFieldName");
//                System.out.println("VocabularyRedirect: " + collectedVocab.vocabularyRedirectField);
//            }
//            if (name.equals("Entry")) {
//                String vocabName = atts.getValue("Value");
//                String vocabCode = atts.getValue("Code");
//                String followUpVocab = atts.getValue("FollowUp");
//                currentVocabItem = new VocabularyItem(vocabName, vocabCode, followUpVocab);
//                collectedVocab.vocabularyItems.add(currentVocabItem);
//            }
//        }
//    }
}
