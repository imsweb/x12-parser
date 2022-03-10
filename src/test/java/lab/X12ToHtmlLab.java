package lab;

import java.awt.Desktop;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.imsweb.x12.reader.X12Reader;
import com.imsweb.x12.writer.X12Writer;

/**
 * The following program will output an HTML file from an x12 file.
 */
public class X12ToHtmlLab {

    /**
     * Export x12 file to HTML.
     * @param args One argument is required, the path of the x12 file.
     */
    public static void main(String [] args) throws Exception {
        X12ToHtmlLab x12ToHtmlLab = new X12ToHtmlLab(new File(args[0]));
        x12ToHtmlLab.generateHtmlFromX12();
    }

    private final File _x12File;

    public X12ToHtmlLab(File x12File) {
        this._x12File = x12File;
    }

    public void generateHtmlFromX12() throws Exception {
        X12Reader fromFileUtf8 = new X12Reader(X12Reader.FileType.ANSI837_5010_X222, _x12File,
            StandardCharsets.UTF_8);

        String x12Template = IOUtils.toString(getClass().getResourceAsStream("/html/x12-template.html"), StandardCharsets.UTF_8);

        X12Writer writer = new X12Writer(fromFileUtf8);
        String x12HtmlSegment = writer.toHtml();

        String fullX12Html = String.format(x12Template, x12HtmlSegment);

        // Below is a useful snippet that will create a fully formatted HTML file from x12.
        // In the src/tests/css directory you will see a "css" directory that contains some css that will format
        // the X12-html output nicely.
        File testDir = new File("build", UUID.randomUUID().toString());
        FileUtils.forceMkdir(testDir);
        FileUtils.copyDirectory(new File(Paths.get("src", "test", "css").toAbsolutePath().toString()), new File(testDir, "css"));
        File htmlFileOutput = new File(testDir, "render-x12.html");
        FileUtils.writeStringToFile(htmlFileOutput, fullX12Html, StandardCharsets.UTF_8);

        // To view the full HTML document, open the file at htmlFileOutput.getAbsolutePath().
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(FileSystems.getDefault().getPath(htmlFileOutput.getAbsolutePath()).toAbsolutePath().toUri());
        }
    }
}
