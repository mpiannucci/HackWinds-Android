package com.nucc.hackwinds.utilities;


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class HashMapXMLParser {

    public static Map<String, String> convertNodesFromXml(String xml) throws Exception {

        InputStream is = new ByteArrayInputStream(xml.getBytes());
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(is);
        return createMap(document.getDocumentElement());
    }

    public static Map<String, String> createMap(Node node) {
        Map<String, String> map = new HashMap<String, String>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.hasAttributes()) {
                for (int j = 0; j < currentNode.getAttributes().getLength(); j++) {
                    Node item = currentNode.getAttributes().item(i);
                    map.put(item.getNodeName(), item.getTextContent());
                }
            }
            if (node.getFirstChild() != null && node.getFirstChild().getNodeType() == Node.ELEMENT_NODE) {
                map.putAll(createMap(currentNode));
            } else if (node.getFirstChild().getNodeType() == Node.TEXT_NODE) {
                map.put(node.getLocalName(), node.getTextContent());
            }
        }
        return map;
    }
}
