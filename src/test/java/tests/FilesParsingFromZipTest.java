package tests;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;

public class FilesParsingFromZipTest {

    @Test
    void readZip() throws Exception {
        ZipFile zipFile = new ZipFile("FilesInZip.zip");
        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
        while (zipFile.entries().hasMoreElements()) {
            ZipEntry entryFile = zipEntries.nextElement();
            if (entryFile.getName().contains("pdf")) {
                pdfParsingTest(zipFile.getInputStream(entryFile));
            } else if (entryFile.getName().contains("xlsx")) {
                xlsxParsingTest(zipFile.getInputStream(entryFile));
            } else if (entryFile.getName().contains("csv")) {
                csvParsingTest(zipFile.getInputStream(entryFile));
            } else if (entryFile.getName().contains("json")) {
                jsonParsingTest(zipFile.getInputStream(entryFile));
            }
        }
    }


    void pdfParsingTest(InputStream streamFile) throws Exception {
        PDF pdf = new PDF(streamFile);
        assertThat(pdf.text).contains("This is a small demonstration .pdf file -");
    }

    void xlsxParsingTest(InputStream streamFile) throws Exception {
        XLS xls = new XLS(streamFile);
        assertThat(xls.excel.getSheetAt(0)
                .getRow(3)
                .getCell(3)
                .getStringCellValue()).contains("check this text");
    }

    void csvParsingTest(InputStream streamFile) throws Exception {
        try (CSVReader reader = new CSVReader(new InputStreamReader(streamFile))) {
            List<String[]> content = reader.readAll();
            assertThat(content.get(0)).contains(
                    "John,Doe",
                    "120 jefferson st.",
                    "Riverside",
                    "NJ",
                    "08075");
        }
    }

    void jsonParsingTest(InputStream streamFile) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = streamFile) {
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JsonNode jsonNode = mapper.readValue(json, JsonNode.class);
            assertThat(jsonNode.get(0).get("firstName").asText()).isEqualTo("Joe");
            assertThat(jsonNode.get(1).get("firstName").asText()).isEqualTo("James");
        }
    }
}
