package guru.qa;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;


public class FilesParsingTest {

    private ClassLoader cl = FilesParsingTest.class.getClassLoader();

    @ParameterizedTest(name = "Пользователь может увидеть в pdf-файле текст {0}")
    @ValueSource(strings = {
            "CONVERT YOUR LOGO TO .PDF", "Insert Your Image", "Resize Your Image"
    })
    void userCanSeeTextInPdfTest(String pdfText) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(
                cl.getResourceAsStream("samples.zip")
        )) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals("sample_pdf.pdf")) {
                    PDF pdfFile = new PDF(zis);
                    assertThat(pdfFile.text).contains(pdfText);
                }
            }
        }
    }

    @Test
    void userCanSeeSalaryValueInXlsxTest() throws Exception {
        try (ZipInputStream zis = new ZipInputStream(cl.getResourceAsStream("samples.zip")
        )) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals("sample_xlsx.xlsx")) {
                    XLS xlsFile = new XLS(zis);
                    double actualValue = xlsFile.excel.getSheetAt(0).getRow(1).getCell(4).getNumericCellValue();
                    assertThat(actualValue).isEqualTo(84219.49731068664);
                }
            }
        }
    }

    @Test
    void userCanSeeInformationInCvsFileTest() throws Exception {
        try (ZipInputStream zis = new ZipInputStream(cl.getResourceAsStream("samples.zip")
        )) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals("sample_csv.csv")) {
                    CSVReader csvReader = new CSVReader(new InputStreamReader(zis));
                    List<String[]> data = csvReader.readAll();
                    Assertions.assertArrayEquals(
                            new String[]{"John Doe", "Designer", "325 Pine Street", "", "Seattle"}, data.get(1));
                    Assertions.assertArrayEquals(
                            new String[]{"Edward Green", "Developer", "110 Pike Street", "WA", "Seattle"}, data.get(3));
                }
            }
        }
    }

    @Test
    void json() throws Exception {



    }


}



