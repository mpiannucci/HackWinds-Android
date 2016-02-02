package com.nucc.hackwinds.utilities;


import com.nucc.hackwinds.types.Buoy;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class LatestBuoyXMLParser {

    public static Buoy parseLatestBuoy(String xml) throws Exception {

        InputStream is = new ByteArrayInputStream(xml.getBytes());
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(is);

        Element observationNode = (Element)document.getElementsByTagName("observation").item(0);

        Buoy newBuoy = new Buoy();
        newBuoy.time = getNode("datetime", observationNode);
        newBuoy.significantWaveHeight = getNode("waveht", observationNode);
        newBuoy.dominantPeriod = getNode("domperiod", observationNode);
        newBuoy.meanDirection = Buoy.getCompassDirection(getNode("meanwavedir", observationNode));
        newBuoy.waterTemperature = getNode("watertemp", observationNode);

        return newBuoy;
    }

    // getNode function
    private static String getNode(String sTag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
                .getChildNodes();
        Node nValue = (Node) nlList.item(0);
        return nValue.getNodeValue();
    }
}
