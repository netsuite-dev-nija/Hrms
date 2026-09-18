import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class SDFCleanup {

    // Change this if your SDF project is somewhere else
    private static final String SDF_PROJECT =
            "C:\\Users\\EDWIN\\Documents\\HRMS SDF\\HRMS sdf";

    private static final String OBJECTS_FOLDER =
            SDF_PROJECT + "\\src\\Objects";

    private static final String BACKUP_FOLDER =
            SDF_PROJECT + "\\backup-before-cleanup";

    // XML fields that we want to remove
    private static final String[] FIELDS_TO_REMOVE = {
            "aidescription",
            "enabletextenhance",
    };

    public static void main(String[] args) {

        File objectsFolder = new File(OBJECTS_FOLDER);
        File backupFolder = new File(BACKUP_FOLDER);

        if (!objectsFolder.exists() || !objectsFolder.isDirectory()) {
            System.out.println("ERROR: Objects folder not found:");
            System.out.println(OBJECTS_FOLDER);
            return;
        }

        if (!backupFolder.exists()) {
            backupFolder.mkdirs();
        }

        System.out.println("==============================================");
        System.out.println("       HRMS SDF XML CLEANUP TOOL");
        System.out.println("==============================================");
        System.out.println();

        System.out.println("Objects folder:");
        System.out.println(OBJECTS_FOLDER);

        System.out.println();
        System.out.println("Backup folder:");
        System.out.println(BACKUP_FOLDER);

        System.out.println();
        System.out.println("Fields to remove:");

        for (String field : FIELDS_TO_REMOVE) {
            System.out.println("  - " + field);
        }

        System.out.println();
        System.out.println("Starting cleanup...");
        System.out.println();

        List<File> xmlFiles = findXmlFiles(objectsFolder);

        if (xmlFiles.isEmpty()) {
            System.out.println("No XML files found.");
            return;
        }

        int filesModified = 0;
        int totalRemoved = 0;
        int filesSkipped = 0;

        for (File xmlFile : xmlFiles) {

            try {

                int removed = processXmlFile(
                        xmlFile,
                        objectsFolder,
                        backupFolder
                );

                if (removed > 0) {

                    filesModified++;
                    totalRemoved += removed;

                    System.out.println(
                            "[MODIFIED] " +
                            xmlFile.getName() +
                            " -> removed " +
                            removed +
                            " field(s)"
                    );

                } else {

                    filesSkipped++;

                }

            } catch (Exception e) {

                System.out.println(
                        "[ERROR] " +
                        xmlFile.getAbsolutePath()
                );

                System.out.println(
                        "        " +
                        e.getMessage()
                );
            }
        }

        System.out.println();
        System.out.println("==============================================");
        System.out.println("                 SUMMARY");
        System.out.println("==============================================");

        System.out.println("XML files scanned : " + xmlFiles.size());
        System.out.println("Files modified    : " + filesModified);
        System.out.println("Files unchanged   : " + filesSkipped);
        System.out.println("Fields removed    : " + totalRemoved);

        System.out.println();
        System.out.println("Backup created at:");
        System.out.println(BACKUP_FOLDER);

        System.out.println();
        System.out.println("Cleanup completed.");
    }


    /**
     * Recursively find all XML files under src/Objects
     */
    private static List<File> findXmlFiles(File directory) {

        List<File> xmlFiles = new ArrayList<>();

        File[] files = directory.listFiles();

        if (files == null) {
            return xmlFiles;
        }

        for (File file : files) {

            if (file.isDirectory()) {

                xmlFiles.addAll(findXmlFiles(file));

            } else if (
                    file.isFile() &&
                    file.getName().toLowerCase().endsWith(".xml")
            ) {

                xmlFiles.add(file);
            }
        }

        return xmlFiles;
    }


    /**
     * Process one XML file.
     */
    private static int processXmlFile(
            File xmlFile,
            File objectsFolder,
            File backupFolder
    ) throws Exception {

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        // Security settings
        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                false
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false
        );

        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        DocumentBuilder builder =
                factory.newDocumentBuilder();

        Document document =
                builder.parse(xmlFile);

        document.getDocumentElement().normalize();

        int removedCount = 0;

        for (String fieldName : FIELDS_TO_REMOVE) {

            NodeList nodes =
                    document.getElementsByTagName(fieldName);

            // NodeList is live, so always remove from the end
            for (int i = nodes.getLength() - 1; i >= 0; i--) {

                Node node = nodes.item(i);

                Node parent = node.getParentNode();

                if (parent != null) {

                    parent.removeChild(node);

                    removedCount++;
                }
            }
        }

        // Nothing changed
        if (removedCount == 0) {
            return 0;
        }

        /*
         * Create backup before writing the modified file.
         */
        Path relativePath =
                objectsFolder.toPath()
                        .relativize(xmlFile.toPath());

        Path backupFile =
                backupFolder.toPath()
                        .resolve(relativePath);

        Files.createDirectories(
                backupFile.getParent()
        );

        Files.copy(
                xmlFile.toPath(),
                backupFile,
                StandardCopyOption.REPLACE_EXISTING
        );

        /*
         * Write modified XML back.
         */
        TransformerFactory transformerFactory =
                TransformerFactory.newInstance();

        Transformer transformer =
                transformerFactory.newTransformer();

        transformer.setOutputProperty(
                OutputKeys.INDENT,
                "yes"
        );

        transformer.setOutputProperty(
                OutputKeys.ENCODING,
                "UTF-8"
        );

        transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount",
                "4"
        );

        DOMSource source =
                new DOMSource(document);

        StreamResult result =
                new StreamResult(xmlFile);

        transformer.transform(source, result);

        return removedCount;
    }
}