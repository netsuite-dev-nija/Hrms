import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SdfScriptPathUpdater {

    // SDF project location
    private static final Path PROJECT_PATH =
            Paths.get("C:\\Users\\EDWIN\\Documents\\HRMS SDF\\HRMS sdf\\src");

    // Objects folder
    private static final Path OBJECTS_PATH =
            PROJECT_PATH.resolve("Objects");

    /*
     * Detect:
     *
     * /SuiteBundles/Bundle <ANY_NUMBER>/<ANY_PATH>
     *
     * Example:
     *
     * /SuiteBundles/Bundle 563694/HRIS Employee/file.js
     *
     * /SuiteBundles/Bundle 123456/HRIS Leave/file.js
     *
     * /SuiteBundles/Bundle 987654/HRIS Payroll/Sub/file.js
     *
     * The part after "Bundle <number>/" is captured.
     */
    private static final Pattern SCRIPT_FILE_PATTERN =
            Pattern.compile(
                    "<scriptfile>\\[/SuiteBundles/Bundle\\s+\\d+/(.*?)\\]</scriptfile>",
                    Pattern.CASE_INSENSITIVE
            );

    public static void main(String[] args) {

        System.out.println("==============================================");
        System.out.println("      SDF SCRIPT PATH UPDATER");
        System.out.println("==============================================");

        System.out.println();
        System.out.println("Project:");
        System.out.println(PROJECT_PATH);

        System.out.println();
        System.out.println("Scanning Objects folder...");

        try {

            // Check Objects folder
            if (!Files.exists(OBJECTS_PATH)) {

                System.out.println();
                System.out.println("ERROR:");
                System.out.println("Objects folder not found:");
                System.out.println(OBJECTS_PATH);

                return;
            }

            // Scan all XML files inside Objects
            try (Stream<Path> files = Files.walk(OBJECTS_PATH)) {

                files.filter(Files::isRegularFile)
                        .filter(path ->
                                path.toString()
                                        .toLowerCase()
                                        .endsWith(".xml")
                        )
                        .forEach(SdfScriptPathUpdater::processXml);
            }

            System.out.println();
            System.out.println("==============================================");
            System.out.println("              PROCESS COMPLETED");
            System.out.println("==============================================");

        } catch (IOException e) {

            System.out.println();
            System.out.println("ERROR:");
            e.printStackTrace();
        }
    }

    private static void processXml(Path xmlFile) {

        try {

            // Read XML
            String content = Files.readString(
                    xmlFile,
                    StandardCharsets.UTF_8
            );

            // Create matcher
            Matcher matcher =
                    SCRIPT_FILE_PATTERN.matcher(content);

            StringBuffer updatedContent =
                    new StringBuffer();

            boolean changed = false;

            while (matcher.find()) {

                /*
                 * Example matcher.group(1):
                 *
                 * HRIS Employee/employee.js
                 *
                 * or:
                 *
                 * HRIS Leave/leave.js
                 *
                 * or:
                 *
                 * HRIS Payroll/Subfolder/payroll.js
                 */
                String remainingPath =
                        matcher.group(1);

                // Create new SDF path
                String newPath =
                        "/SuiteScripts/" + remainingPath;

                // Get complete old path
                String oldPath =
                        matcher.group(0)
                                .replace(
                                        "<scriptfile>[",
                                        ""
                                )
                                .replace(
                                        "]</scriptfile>",
                                        ""
                                );

                // Create replacement XML
                String replacement =
                        "<scriptfile>[" +
                        newPath +
                        "]</scriptfile>";

                // Replace
                matcher.appendReplacement(
                        updatedContent,
                        Matcher.quoteReplacement(
                                replacement
                        )
                );

                System.out.println();
                System.out.println("----------------------------------------------");

                System.out.println("XML File:");
                System.out.println(xmlFile.getFileName());

                System.out.println();

                System.out.println("OLD:");
                System.out.println(oldPath);

                System.out.println();

                System.out.println("NEW:");
                System.out.println(newPath);

                changed = true;
            }

            // Add remaining XML content
            matcher.appendTail(updatedContent);

            // Save only if changes were made
            if (changed) {

                Files.writeString(
                        xmlFile,
                        updatedContent.toString(),
                        StandardCharsets.UTF_8
                );

                System.out.println();
                System.out.println("STATUS: UPDATED");

            }

        } catch (IOException e) {

            System.out.println();
            System.out.println(
                    "ERROR processing XML: "
                            + xmlFile
            );

            e.printStackTrace();
        }
    }
}