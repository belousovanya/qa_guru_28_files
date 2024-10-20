package tests;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class FilesParsingTest {

    private ClassLoader cl = FilesParsingTest.class.getClassLoader();

    private InputStream getFileFromZip(String zipFilePath, String targetFileName) throws Exception {
        ZipInputStream zis = new ZipInputStream(cl.getResourceAsStream(zipFilePath));
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.getName().equals(targetFileName)) {
                return zis;
            }
        }
        throw new IllegalArgumentException("File " + targetFileName + " not found in " + zipFilePath);
    }

    @ParameterizedTest(name = "Пользователь может увидеть в pdf-файле текст {0}")
    @ValueSource(strings = {
            "CONVERT YOUR LOGO TO .PDF", "Insert Your Image", "Resize Your Image"
    })
    void pdfFileContainsTextTest(String pdfText) throws Exception {
        try (InputStream is = getFileFromZip("samples.zip", "sample_pdf.pdf")) {
            PDF pdfFile = new PDF(is);
            assertThat(pdfFile.text).contains(pdfText);
        }
    }

    @Test
    void xlsxFileContainsExpectedSalaryValueTest() throws Exception {
        try (InputStream is = getFileFromZip("samples.zip", "sample_xlsx.xlsx")) {
            XLS xlsFile = new XLS(is);
            double actualValue = xlsFile.excel.getSheetAt(0).getRow(1).getCell(4).getNumericCellValue();
            assertThat(actualValue).isEqualTo(84219.49731068664);
        }
    }

    @Test
    void csvFileContainsExpectedInformationTest() throws Exception {
        try (InputStream is = getFileFromZip("samples.zip", "sample_csv.csv")) {
            CSVReader csvReader = new CSVReader(new InputStreamReader(is));
            List<String[]> data = csvReader.readAll();
            Assertions.assertArrayEquals(
                    new String[]{"John Doe", "Designer", "325 Pine Street", "", "Seattle"}, data.get(1));
            Assertions.assertArrayEquals(
                    new String[]{"Edward Green", "Developer", "110 Pike Street", "WA", "Seattle"}, data.get(3));
        }
    }

    static Stream<Arguments> jsonFileContainsExpectedSeasonsAndEpisodesTest() throws Exception {
        return Stream.of(
                Arguments.of(0, 1, "The Name of the Game"),
                Arguments.of(0, 2, "Cherry"),
                Arguments.of(0, 3, "Get Some"),
                Arguments.of(1, 1, "The Big Ride"),
                Arguments.of(1, 2, "Proper Preparation and Planning"),
                Arguments.of(1, 3, "Over the Hill with the Swords of a Thousand Men")
        );
    }

    @ParameterizedTest(name = "У сезона {0}, Эпизод {1} называется {2}")
    @MethodSource("jsonFileContainsExpectedSeasonsAndEpisodesTest")
    void jsonFileContainsExpectedSeasonsAndEpisodesTest(int seasonIndex, int episodeNumber, String expectedTitle) throws Exception {
        try (InputStream is = cl.getResourceAsStream("theBoys.json")) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(is);
            JsonNode seasonsNode = rootNode.get("seasons");

            JsonNode season = seasonsNode.get(seasonIndex);
            JsonNode episodes = season.get("episodes");
            JsonNode episode = episodes.get(episodeNumber - 1);

            Assertions.assertEquals(episodeNumber, episode.get("episode_number").asInt());
            Assertions.assertEquals(expectedTitle, episode.get("title").asText());
        }
    }
}