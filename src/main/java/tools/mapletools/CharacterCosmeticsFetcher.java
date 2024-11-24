package tools.mapletools;

import java.io.*;
import java.util.*;

public class CharacterCosmeticsFetcher {
    static final String HANDBOOK_HAIR_PAGE = ToolConstants.HANDBOOK_PATH + "/Equip/Hair.txt";
    static final String HANDBOOK_FACE_PAGE = ToolConstants.HANDBOOK_PATH + "/Equip/Face.txt";
    static final String HANDBOOK_SKIN_PAGE = ToolConstants.HANDBOOK_PATH + "/Equip/Skin.txt";

    // ******************** HAIR ********************
    public static Map<String, Map<Integer, Integer>> parseHandbookHairs() {
        List<String> hairEntries = sortLinesByIdAscending(readFile(HANDBOOK_HAIR_PAGE));

        // Initialize the result map: Map<Hair Name, Map<Hair Id, Number of colors>>
        Map<String, Map<Integer, Integer>> result = new HashMap<>();

        // Variables to track the current group
        String currentHairName = "";
        Integer currentHairId = null;
        int currentHairColorRangeCount = 0;

        // Process each line
        for (String hairs : hairEntries) {
            hairs = hairs.trim();

            if (!hairs.isEmpty()) {
                // Regular expression to match the pattern: Number - Color Name - description
                String[] parts = hairs.split(" - ");

                if (parts.length == 3) {
                    try {
                        int number = Integer.parseInt(parts[0].trim());
                        String colorAndName = parts[1].trim();
                        // String description = parts[2].trim(); // can be used in the future if descriptions exist... ?

                        // Remove the color part (first word before the space)
                        String[] nameParts = colorAndName.split(" ", 2);
                        String name = (nameParts.length > 1) ? nameParts[1] : nameParts[0];

                        // Check if this number ends with a "0" (e.g., 0, 100, 110, etc.)
                        if (number % 10 == 0) {
                            // If there was a previous entry, add it to the result map
                            if (currentHairId != null) {
                                Map<Integer, Integer> innerMap = result.computeIfAbsent(currentHairName, k -> new HashMap<>());
                                innerMap.put(currentHairId, currentHairColorRangeCount);
                            }

                            // Update the current key and range count
                            currentHairId = number;
                            currentHairColorRangeCount = 0;
                            currentHairName = name;
                        } else {
                            // Increment the range count for numbers between two numbers ending in "0"
                            currentHairColorRangeCount++;
                        }
                    } catch (NumberFormatException e) {
                        // Skip any malformed lines
                        System.out.println("Skipping invalid line: " + hairs);
                    }
                }
            }
        }

        // After the loop, add the last range to the result map
        if (currentHairId != null) {
            Map<Integer, Integer> innerMap = result.computeIfAbsent(currentHairName, k -> new HashMap<>());
            innerMap.put(currentHairId, currentHairColorRangeCount);
        }

        return result;
    }

    /*
    * This returns a list of all hair ids that share a color with the current hair.
     */
    public static List<Integer> getAvailableHairsExcludingCurrent(int currentHairId) {
        Map<String, Map<Integer, Integer>> parsedHairData = parseHandbookHairs();

        // Get the last digit of the current hair ID
        int targetColor = currentHairId % 10;

        // Result list to store matching hair IDs
        List<Integer> matchingHairIds = new ArrayList<>();

        // Iterate through the parsed data
        for (Map.Entry<String, Map<Integer, Integer>> group : parsedHairData.entrySet()) {
            String hairName = group.getKey();
            Map<Integer, Integer> ranges = group.getValue();

            for (Map.Entry<Integer, Integer> range : ranges.entrySet()) {
                int baseId = range.getKey(); // ID ending in 0
                int rangeCount = range.getValue(); // Number of IDs in the range

                // Check all IDs in the range
                for (int i = 0; i <= rangeCount; i++) {
                    int hairId = baseId + i;

                    // Check if this ID has the same color and is not the current hair ID
                    if (hairId % 10 == targetColor && hairId != currentHairId) {
                        matchingHairIds.add(hairId);
                    }
                }
            }
        }
        return matchingHairIds;
    }

    /*
     * This returns a list of all hair ids for different colors of the current hair.
     */
    public static List<Integer> getAvailableHairColorsExcludingCurrent(int currentHairId) {
        Map<String, Map<Integer, Integer>> parsedHairData = parseHandbookHairs();

        // Remove the last digit of the current hair ID (color code)
        int targetStyle = currentHairId / 10;

        // Result list to store matching hair IDs
        HashSet<Integer> matchingHairIds = new HashSet<>();

        // Iterate through the parsed data
        for (Map.Entry<String, Map<Integer, Integer>> group : parsedHairData.entrySet()) {
            Map<Integer, Integer> ranges = group.getValue();

            for (Map.Entry<Integer, Integer> range : ranges.entrySet()) {
                int baseId = range.getKey(); // ID ending in 0
                int rangeCount = range.getValue(); // Number of IDs in the range

                // Check all IDs in the range
                for (int i = 0; i <= rangeCount; i++) { // Include `0` in the range
                    int hairId = baseId + i;

                    // Check if this ID has the same color and is not the current hair ID
                    if (hairId / 10 == targetStyle && hairId != currentHairId) {
                        matchingHairIds.add(hairId);
                    }
                }
            }
        }
        return matchingHairIds.stream().toList();
    }

    // ******************** FACES ********************
    /*
    * returns a list of all available faces for current eye color, except current face
     */
    public static Map<String, Map<Integer, Integer>> parseHandbookFaces() {
        List<String> faceEntries = sortFacesAscending(readFile(HANDBOOK_FACE_PAGE));

        // Initialize the result map: Map<Face Name, Map<Face Id, Number of colors>>
        Map<String, Map<Integer, Integer>> result = new HashMap<>();

        // Variables to track the current group
        String currentFaceName = "";
        Integer currentFaceId = null;
        int currentEyeColorRangeCount = 0;

        for (String face : faceEntries) {
            face = face.trim();
            if (!face.isEmpty()) {
                // Regex to extract the pattern: ID - Name (Color) - (description)
                String[] parts = face.split(" - ");

                if (parts.length == 3) {
                    try {
                        int number = Integer.parseInt(parts[0].trim());
                        String nameAndColor = parts[1].trim();
                        // String description = parts[2].trim(); // can be used in the future if descriptions exist... ?

                        // Remove the color part
                        String[] nameParts = nameAndColor.split("\\(", 2);
                        String name = nameParts[0];

                        // Check if this number's center digit is a "0" (e.g., 0, 100, 110, etc.)
                        if ((number / 100) % 10 == 0) {
                            // If there was a previous entry, add it to the result map
                            if (currentFaceId != null) {
                                Map<Integer, Integer> innerMap = result.computeIfAbsent(currentFaceName, k -> new HashMap<>());
                                innerMap.put(currentFaceId, currentEyeColorRangeCount);
                            }

                            // Update the current key and range count
                            currentFaceId = number;
                            currentEyeColorRangeCount = 0;
                            currentFaceName = name;
                        } else {
                            // Increment the range count for numbers between two numbers ending in "0"
                            currentEyeColorRangeCount++;
                        }
                    } catch (NumberFormatException e) {
                        // Skip any malformed lines
                        System.out.println("Skipping invalid line: " + face);
                    }
                }
            }
        }
        return result;
    }

    /*
     * returns a list of all available eye colors for current face, except current color
     */
    public static List<Integer> getEyeColorsForCurrentFace(int currentFaceId) {
        Map<String, Map<Integer, Integer>> faceEntries = parseHandbookFaces();

        int targetStyle = shiftedHundredsId(currentFaceId) / 10;

        // Result list to store matching hair IDs
        HashSet<Integer> matchingFaceIds = new HashSet<>();

        for (Map.Entry<String, Map<Integer, Integer>> group : faceEntries.entrySet()) {
            Map<Integer, Integer> ranges = group.getValue();

            for (Map.Entry<Integer, Integer> range : ranges.entrySet()) {
                int baseId = range.getKey(); // ID with 0 hundreds place
                int rangeCount = range.getValue(); // Number of IDs in the range

                // Check all IDs in the range
                for (int i = 0; i <= rangeCount; i++) { // Include `0` in the range
                    int faceId = baseId + (i * 100);

                    // Check if this ID has the same color and is not the current hair ID
                    if (shiftedHundredsId(faceId) / 10 == targetStyle && faceId != currentFaceId) {
                        matchingFaceIds.add(faceId);
                    }
                }
            }
        }

        System.out.println();

        return matchingFaceIds.stream().toList();
    }

    // ******************** SKIN ********************

    // ******************** GENDER ********************

    // ******************** RANDOMIZER ********************

    // ******************** HELPERS ********************
    // Method to read the file and return a list of lines
    private static List<String> readFile(String fileName) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    // Sort lines numerically by the ID at the start of each line
    private static List<String> sortLinesByIdAscending(List<String> lines) {
        lines.sort((line1, line2) -> {
            try {
                int id1 = Integer.parseInt(line1.split(" - ")[0].trim());
                int id2 = Integer.parseInt(line2.split(" - ")[0].trim());
                return Integer.compare(id1, id2);
            } catch (NumberFormatException e) {
                return 0; // Keep the original order if parsing fails
            }
        });
        return lines;
    }

    // Sort lines numerically by the Face at the start of each line
    private static List<String> sortFacesAscending(List<String> lines) {
        lines.sort((line1, line2) -> {
            try {
                int id1 = Integer.parseInt(line1.split(" - ")[0].trim());
                int id2 = Integer.parseInt(line2.split(" - ")[0].trim());

                return Integer.compare(shiftedHundredsId(id1), shiftedHundredsId(id2));
            } catch (NumberFormatException e) {
                return 0; // Keep the original order if parsing fails
            }
        });
        return lines;
    }

    private static int shiftedHundredsId(int id) { // ex. 23056
        int hundredsDigit = (id / 100) % 10; // ex. 0
        int right = (id % 100) * 10 + hundredsDigit; // ex. 560
        int left = (id / 1000) * 1000; // ex. 23000
        return left + right; // ex. 23560
    }
}
