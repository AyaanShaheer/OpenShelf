package com.openshelf.metadata;

import com.openshelf.common.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

@Component
public class DublinCoreXmlParser {

    public List<Map<String, String>> parse(InputStream inputStream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            doc.getDocumentElement().normalize();

            List<Map<String, String>> records = new ArrayList<>();
            NodeList recordNodes = doc.getElementsByTagNameNS("*", "record");
            if (recordNodes.getLength() == 0) {
                recordNodes = doc.getElementsByTagName("record");
            }
            if (recordNodes.getLength() == 0) {
                // treat root/child elements as single record if DC tags present
                Map<String, String> single = extractDcFields(doc.getDocumentElement());
                if (!single.isEmpty()) records.add(single);
                return records;
            }
            for (int i = 0; i < recordNodes.getLength(); i++) {
                Node node = recordNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    records.add(extractDcFields((Element) node));
                }
            }
            return records;
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Invalid or malformed XML file");
        }
    }

    private Map<String, String> extractDcFields(Element element) {
        Map<String, String> map = new LinkedHashMap<>();
        NodeList all = element.getElementsByTagName("*");
        for (int i = 0; i < all.getLength(); i++) {
            Node n = all.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) continue;
            String local = n.getLocalName();
            if (local == null || local.isBlank()) {
                String name = n.getNodeName();
                int idx = name.indexOf(':');
                local = idx >= 0 ? name.substring(idx + 1) : name;
            }
            String value = n.getTextContent() == null ? "" : n.getTextContent().trim();
            if (value.isBlank()) continue;
            String key = local.toLowerCase(Locale.ROOT);
            if (isDcField(key)) {
                map.merge(key, value, (a, b) -> a.contains(b) ? a : a + "; " + b);
            }
        }
        return map;
    }

    private boolean isDcField(String key) {
        return Set.of("title","creator","subject","description","publisher","contributor","date","type","format",
                "identifier","source","language","relation","coverage","rights").contains(key);
    }
}
