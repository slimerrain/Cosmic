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

        // Get the last digit of the current hair ID, indicating color
        int targetColor = currentHairId % 10;

        // Result list to store matching hair IDs
        List<Integer> hairIds = new ArrayList<>();

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
                        hairIds.add(hairId);
                    }
                }
            }
        }

        return hairIds;
    }

    /*
     * This returns a list of all hair ids for different colors of the current hair.
     */
    public static List<Integer> getAvailableHairColorsExcludingCurrent(int currentHairId) {
        Map<String, Map<Integer, Integer>> parsedHairData = parseHandbookHairs();

        // Result list to store matching hair IDs
        HashSet<Integer> colorVariants = new HashSet<>();

        // Iterate through the parsed data
        for (Map.Entry<String, Map<Integer, Integer>> group : parsedHairData.entrySet()) {
            Map<Integer, Integer> ranges = group.getValue();

            for (Map.Entry<Integer, Integer> range : ranges.entrySet()) {
                int baseId = range.getKey(); // ID ending in 0
                int rangeCount = range.getValue(); // Number of IDs in the range

                colorVariants.addAll(buildHairColorVariantList(baseId, rangeCount));
            }
        }
        return colorVariants.stream().toList();
    }

    /*
     * Builds a list of eye colors based on how many colors there are.
     *
     * Assumes colors are sequential: (##0##, ##1##, ##2##, etc)
     */
    private static List<Integer> buildHairColorVariantList(int currentHairId, int rangeCount) {
        List<Integer> colorVariants = new ArrayList<>();
        int targetStyle = calculateHairIdNoColor(currentHairId);
        // Check all IDs in the range
        for (int i = 0; i <= rangeCount; i++) { // Include `0` in the range
            int hairId = targetStyle + i;

            // Check if this ID has the same color and is not the current hair ID
            if (hairId != currentHairId) {
                colorVariants.add(hairId);
            }
        }
        return colorVariants;
    }

    /*
     * Removes color from hair id
     * Ex. 12345 -> 12340, 23754 -> 23750, etc
     */
    private static int calculateHairIdNoColor(int id) {
        return (id / 10) * 10;
    }

    /*
     * Extracts color from hair id
     * Ex. 12345 -> 5, 23754 -> 4, etc
     */
    private static int calculateHairColor(int id) {
        return id % 10;
    }

    // ******************** FACES ********************
    /*
    * Returns a list of all available faces for current eye color, except current face
     */
    public static Map<Integer, Integer> parseHandbookFaces() {
        return parseFaceEntries(sortFacesAscending(readFile(HANDBOOK_FACE_PAGE)));
    }

    /*
     * Takes in a list of handbook-formatted face entries
     * ( `ID - Name (Color) - description` )
     *
     * Returns a Map<Base Face ID, Number of eye color variants>
     *
     * Assumes that the list of entries is sorted by outer 4 digits ( XX%XX ), ignoring the center digit.
     * ( see sortFacesAscending() )
     */
    private static Map<Integer, Integer> parseFaceEntries (List<String> faceEntries) {
        // Initialize the result map: Map<Face ID, Number of eye color variants>
        Map<Integer, Integer> result = new HashMap<>();

        // Variables to track the current face calculations
        Integer currentFaceId = null;
        int currentEyeColorRangeCount = 0;

        for (String face : faceEntries) {
            face = face.trim();
            if (!face.isEmpty()) {
                // Regex to extract the pattern: ID - Name (Color) - description
                String[] parts = face.split(" - ");

                // Only will process properly formatted entries. Skips all other entries.
                if (parts.length == 3) {
                    try {
                        // Extract ID from current entry iteration.
                        int id = Integer.parseInt(parts[0].trim());

                        // Check if this id is a base face id and not a color variant
                        // (aka new set of faces to start counting.)
                        if (calculateEyeColor(id) == 0) {
                            // Put final result of color count into map (unless this is the first ID.)
                            if (currentFaceId != null) {
                                result.put(currentFaceId, currentEyeColorRangeCount);
                            }

                            // Reset the current key and range count
                            currentEyeColorRangeCount = 0;
                            currentFaceId = id;
                        } else {
                            currentEyeColorRangeCount++;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Skipping invalid line: " + face);
                    }
                }
            }
        }

        // After the loop, add the last range to the result map
        if (currentFaceId != null) {
            result.put(currentFaceId, currentEyeColorRangeCount);
        }

        return result;
    }

    /*
     * Returns a list of all available eye colors for current face, except current color
     */
    public static List<Integer> getEyeColorsForCurrentFace(int currentFaceId) {
        Map<Integer, Integer> faceEntries = parseHandbookFaces();
        int targetStyle = calculateFaceIdNoColor(currentFaceId);
        return buildEyeColorVariantList(currentFaceId, faceEntries.get(targetStyle));
    }

    /*
     * Returns a list of all available faces for current eye color, except current face
     */
    public static List<Integer> getFacesForCurrentEyeColor(int currentFaceId) {
        Map<Integer, Integer> faceEntries = parseHandbookFaces();
        int targetColor = calculateEyeColor(currentFaceId);
        Set<Integer> baseFaceIds = faceEntries.keySet();
        return buildFaceVariantList(currentFaceId, baseFaceIds, targetColor);
    }

    // Sort lines numerically by the Face at the start of each line
    private static List<String> sortFacesAscending(List<String> lines) {
        lines.sort((line1, line2) -> {
            try {
                int id1 = Integer.parseInt(line1.split(" - ")[0].trim());
                int id2 = Integer.parseInt(line2.split(" - ")[0].trim());

                return Integer.compare(calculateFaceIdNoColor(id1), calculateFaceIdNoColor(id2));
            } catch (NumberFormatException e) {
                return 0; // Keep the original order if parsing fails
            }
        });
        return lines;
    }

    /*
     * Removes color from face id
     * Ex. 12345 -> 12045, 23754 -> 23054, etc
     */
    private static int calculateFaceIdNoColor(int id) {
        return (id / 1000) * 1000 + (id % 100);
    }

    /*
     * Extracts color from face id
     * Ex. 12345 -> 3, 23754 -> 7, etc
     */
    private static int calculateEyeColor(int id) {
        return (id / 100) % 10;
    }

    /*
     * Builds a list of eye colors based on how many colors there are.
     *
     * Assumes colors are sequential: (##0##, ##1##, ##2##, etc)
     */
    private static List<Integer> buildEyeColorVariantList(int currentFaceId, int colors) {
        List<Integer> results = new ArrayList<>();
        int baseFaceId = calculateFaceIdNoColor(currentFaceId);
        for (int i = 0; i < colors + 1; i++) {
            int eyeColorVariant = baseFaceId + (i * 100);
            if (eyeColorVariant != currentFaceId) {
                results.add(eyeColorVariant);
            }
        }
        return results;
    }

    /*
     * Builds a list of faces based on current eye color.
     */
    private static List<Integer> buildFaceVariantList(int currentFaceId, Set<Integer> baseFaceIds, int color) {
        List<Integer> results = new ArrayList<>();
        for (Integer baseFaceId : baseFaceIds) {
            int faceId = baseFaceId + (color * 100);
            if (faceId != currentFaceId) {
                results.add(faceId);
            }
        }
        return results;
    }

    // ******************** SKIN ********************

    // ******************** GENDER ********************

    // ******************** RANDOMIZER ********************

    // ******************** GENERAL HELPERS ********************
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
}
