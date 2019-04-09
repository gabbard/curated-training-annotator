package edu.isi.vista.gigawordIndexer;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parse the annotated Gigaword corpus
 */
public class AnnotatedGigawordParser extends DefaultHandler
{
    
    private List<Article> articleList = new ArrayList<>();
    
    // flags for parsing articles
    private boolean inDocument = false;
    private boolean inSentences = false;
    private boolean inToken = false;
    private boolean inWord = false;
    private boolean inOffsetBegin = false;
    
    // variables for reconstructing articles
    private StringBuilder docText = new StringBuilder();
    private String currentDocId = "";
    private String currentWord = "";
    private int currentOffsetBegin = 0;
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
    {
        if (qName.equals("DOC")) {
            inDocument = true;
            currentDocId = attributes.getValue("id");
        }
        else if (inDocument && qName.equals("sentences")) {
            inSentences = true;
        }
        else if (inSentences && qName.equals("token")) {
            inToken = true;
        }
        else if (inToken && qName.equals("word")) {
            inWord = true;
        }
        else if (inToken && qName.equals("CharacterOffsetBegin")) {
            inOffsetBegin = true;
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName)
    {
        if (qName.equals("DOC")) {
            inDocument = false;
        }
        else if (inDocument && qName.equals("sentences")) {
            inSentences = false;
            articleList.add(new Article(currentDocId, docText.toString()));
            docText = new StringBuilder();
        }
        else if (inSentences && qName.equals("token")) {
            inToken = false;
            while (docText.length() < currentOffsetBegin) {
                docText.append(" ");
            }
            docText.append(currentWord);
        }
        else if (inToken && qName.equals("word")) {
            inWord = false;
        }
        else if (inToken && qName.equals("CharacterOffsetBegin")) {
            inOffsetBegin = false;
        }
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (inWord) {
            currentWord = new String(ch, start, length);
        }
        if (inOffsetBegin) {
            currentOffsetBegin = Integer.parseInt(new String(ch, start, length).trim());
        }
        
    }
    
    public List<Article> getArticleList() {
        return articleList;
    }
}
