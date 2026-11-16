package com.openshelf.imports;

import com.openshelf.common.exception.BadRequestException;
import com.openshelf.metadata.DublinCoreXmlParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DublinCoreXmlParserTest {

    private final DublinCoreXmlParser parser = new DublinCoreXmlParser();

    @Test
    void parsesDublinCoreRecords() {
        String xml = """
                <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
                  <record>
                    <dc:title>Open Scholarship</dc:title>
                    <dc:creator>Elena Morales</dc:creator>
                    <dc:identifier>DOI-123</dc:identifier>
                  </record>
                </metadata>
                """;
        List<Map<String, String>> records = parser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        assertThat(records).hasSize(1);
        assertThat(records.get(0).get("title")).isEqualTo("Open Scholarship");
        assertThat(records.get(0).get("creator")).isEqualTo("Elena Morales");
        assertThat(records.get(0).get("identifier")).isEqualTo("DOI-123");
    }

    @Test
    void rejectsMalformedXml() {
        String xml = "<metadata><record><dc:title>Broken</record></metadata>";
        assertThatThrownBy(() -> parser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsExternalEntityPayloads() {
        String xml = """
                <?xml version="1.0"?>
                <!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
                  <record><dc:title>&xxe;</dc:title></record>
                </metadata>
                """;
        assertThatThrownBy(() -> parser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(BadRequestException.class);
    }
}
