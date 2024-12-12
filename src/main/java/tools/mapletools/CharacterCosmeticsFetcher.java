package tools.mapletools;

import client.Client;
import client.SkinColor;
import scripting.AbstractPlayerInteraction;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class CharacterCosmeticsFetcher extends AbstractPlayerInteraction  {
    static final String HANDBOOK_HAIR_PAGE = ToolConstants.HANDBOOK_PATH + "/Equip/Hair.txt";
    static final String HANDBOOK_FACE_PAGE = ToolConstants.HANDBOOK_PATH + "/Equip/Face.txt";
    static final String HANDBOOK_SKIN_PAGE = ToolConstants.HANDBOOK_PATH + "/Equip/Skin.txt";

    public CharacterCosmeticsFetcher(Client c) {
        super(c);
    }

    // ******************** HAIR ********************
    public static Map<Integer, Integer> parseHandbookHairs() {
        return parseHairs(sortLinesByIdAscending(readFile(HANDBOOK_HAIR_PAGE)));
    }

    /*
    * Returns a Map<Hair ID, Number of Hair Color Variants>
     */
    public static Map<Integer, Integer> parseHairs(List<String> hairEntries){

        // Initialize the result map: Map<Hair Name, Map<Hair Id, Number of colors>>
        Map<Integer, Integer> result = new HashMap<>();

        // Variables to track the current group
        Integer currentHairId = null;
        int currentHairColorRangeCount = 0;

        // Process each line
        for (String hair : hairEntries) {
            hair = hair.trim();

            if (!hair.isEmpty()) {
                // Regular expression to match the pattern: Number - Color Name - description
                String[] parts = hair.split(" - ");

                // Skip logic if the formatting is incorrect
                if (parts.length == 3) {
                    try {
                        int hairId = Integer.parseInt(parts[0].trim());

                        // Check if this number ends with a "0" (e.g., 11040, 82100, 54110, etc.)
                        if (hairId % 10 == 0) {
                            // If there was a previous entry, add it to the result map
                            if (currentHairId != null) {
                                result.put(currentHairId ,currentHairColorRangeCount);
                            }

                            // Update the current key and range count
                            currentHairId = hairId;
                            currentHairColorRangeCount = 0;
                        } else {
                            // Increment the range count for numbers between two numbers ending in "0"
                            currentHairColorRangeCount++;
                        }
                    } catch (NumberFormatException e) {
                        // Skip any malformed lines
                        System.out.println("Skipping invalid line: " + hair);
                    }
                }
            }
        }

        // After the loop, add the last range to the result map
        if (currentHairId != null) {
            result.put(currentHairId, currentHairColorRangeCount);
        }

        return result;
    }

    /*
    * Returns a list of all hair ids that share a color with the current hair.
     */
    public static List<Integer> getAvailableHairsForCurrentColor(int currentHairId) {
        int currentHairColor = calculateHairColor(currentHairId);
        Map<Integer, Integer> parsedHairData = parseHandbookHairs().entrySet().stream()
                .filter(entry -> entry.getValue() >= currentHairColor)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Set<Integer> baseHairIds = parsedHairData.keySet();
        return buildHairVariantList(currentHairId, baseHairIds, currentHairColor);
    }

    /*
     * Returns a list of all hair ids for different colors of the current hair.
     */
    public static List<Integer> getAvailableHairColorsForCurrentStyle(int currentHairId) {
        Map<Integer, Integer> parsedHairData = parseHandbookHairs();
        return buildHairColorVariantList(currentHairId, parsedHairData.get(calculateHairIdNoColor(currentHairId)));
    }

    /*
     * Returns a random hair id from the handbook file.
     */
    public static Integer getRandomHairFromHandbook() {
        Map<Integer, Integer> parsedHairs = parseHandbookHairs();
        Set<Integer> baseHairIds = parsedHairs.keySet();

        Random rand = new Random();
        int randomIndex = rand.nextInt(baseHairIds.size());
        int randomColor = rand.nextInt(parsedHairs.get(baseHairIds.stream().toList().get(randomIndex)));

        return baseHairIds.stream().toList().get(randomIndex) + randomColor;
    }

    /*
     * Builds a list of hair colors based on how many colors there are.
     *
     * Assumes colors are sequential: (##0##, ##1##, ##2##, etc)
     */
    private static List<Integer> buildHairColorVariantList(int currentHairId, int rangeCount) {
        List<Integer> colorVariants = new ArrayList<>();
        int targetStyle = calculateHairIdNoColor(currentHairId);
        // Check all IDs in the range
        for (int i = 0; i < rangeCount + 1; i++) { // Include `0` in the range
            int hairId = targetStyle + i;

            // Check if this ID has the same color and is not the current hair ID
            if (hairId != currentHairId) {
                colorVariants.add(hairId);
            }
        }

        return colorVariants;
    }

    /*
     * Builds a list of hairs based on current hair color.
     */
    private static List<Integer> buildHairVariantList(int currentHairId, Set<Integer> baseHairIds, int color) {
        // Result list to store matching hair IDs
        List<Integer> hairIds = new ArrayList<>();

        // Iterate through the parsed data
        for (Integer hair : baseHairIds) {
            Integer hairId = hair + color;
            if(currentHairId != hairId){
                hairIds.add(hairId);
            }
        }

        Collections.sort(hairIds);

        return hairIds;
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
        return parseFaceEntries(sortFacesAscending(sortLinesByIdAscending(readFile(HANDBOOK_FACE_PAGE))));
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

    /*
     * Returns a random face id from the handbook file.
     */
    public static Integer getRandomFaceFromHandbook() {
        Map<Integer, Integer> parsedFaces = parseHandbookFaces();
        Set<Integer> baseFaceIds = parsedFaces.keySet();

        Random rand = new Random();
        int randomIndex = rand.nextInt(baseFaceIds.size());
        int randomColor = rand.nextInt(parsedFaces.get(baseFaceIds.stream().toList().get(randomIndex)));

        return baseFaceIds.stream().toList().get(randomIndex) + (randomColor*100);
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
    public static Map<Integer, String> parseHandbookSkins() {
        return parseSkins(sortLinesByIdAscending(readFile(HANDBOOK_SKIN_PAGE)));
    }

    /*
     * Returns a Map<Hair ID, Number of Hair Color Variants>
     */
    public static Map<Integer, String> parseSkins(List<String> skinEntries){
        // Initialize the result map: Map<Skin Id, Skin Name>
        Map<Integer, String> result = new HashMap<>();

        // Process each line
        for (String skin : skinEntries) {
            skin = skin.trim();

            if (!skin.isEmpty()) {
                // Regular expression to match the pattern: Number - Color Name - description
                String[] parts = skin.split(" - ");

                // Skip logic if the formatting is incorrect
                if (parts.length == 3) {
                    try {
                        int skinId = Integer.parseInt(parts[0].trim());
                        String skinName = parts[1].trim();
                        // Add it to the result map
                        if (skinName != null) {
                            result.put(skinId, skinName);
                        }
                    } catch (NumberFormatException e) {
                        // Skip any malformed lines
                        System.out.println("Skipping invalid line: " + skin);
                    }
                }
            }
        }

        return result;
    }

    /*
     * Returns a random skin id from the handbook file.
     */
    public static List<Integer> getOtherSkinColors(SkinColor currentSkin) {
        Map<Integer, String> parsedSkins = parseHandbookSkins();
        Set<Integer> skinIds = parsedSkins.keySet();

        skinIds.removeIf(skinId -> skinId == currentSkin.getId());

        return skinIds.stream().toList();
    }

    /*
     * Returns a random skin id from the handbook file.
     */
    public static Integer getRandomSkinFromHandbook() {
        Map<Integer, String> parsedSkins = parseHandbookSkins();
        Set<Integer> baseSkinIds = parsedSkins.keySet();

        Random rand = new Random();
        int randomIndex = rand.nextInt(baseSkinIds.size());

        return baseSkinIds.stream().toList().get(randomIndex);
    }

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
