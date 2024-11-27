/*
* KIN
* v83 Cosmic
* Author: slimerrain
*/

var CharacterCosmeticsFetcher = Java.type('tools.mapletools.CharacterCosmeticsFetcher');

var status;
var styleOptions = Array();
var style;

// The beginning of the conversation, on interact
function start() {
    status = -1;
    style = -1;
    var options0 = Array(
        "Change Appearance",
        "Do Nothing"
    )
    cm.sendSimple("What would you like to do? \r\n" + generateSelectionMenu(options0));
}

// Restart this function every time any option is selected.
function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else if(mode == 1) {
        status++;
        switch (status) {
            // Choosing second appearance menu option
            case 0:
                // SELECTION select
                switch (selection) {
                    // Change Appearance
                    case 0:
                        var options1 = Array(
                            "Hair Style",
                            "Hair Color",
                            "Eye Shape",
                            "Eye Color",
                            "Skin Color",
                            "Randomize Appearance",
                            "Gender",
                            "Nothing"
                        )
                        cm.sendSimple("What would you like to change? \r\n" + generateSelectionMenu(options1));
                        break;

                    // Decided To Do Nothing
                    default:
                        cm.sendOk("K bye.");
                        cm.dispose();
                        break;
                }
                break;

            // Appearance change submenu options
            case 1:
                switch (selection) {
                    // Hair Style
                    case 0:
                        style = 0;
                        styleOptions = generateHairStyleListFromCurrentHair();
                        cm.sendStyle("Pick a hair style.", styleOptions);
                        break;

                    // Hair Color
                    case 1:
                        style = 0;
                        styleOptions = generateHairColorListFromCurrentHair();
                        cm.sendStyle("Pick a hair color.", styleOptions);
                        break;

                    // Eye Shape
                    case 2:
                        style = 1;
                        styleOptions = generateFaceListFromCurrentEyeColor();
                        cm.sendStyle("Pick a new face.", styleOptions);
                        break;

                    // Eye Color
                    case 3:
                        style = 1;
                        styleOptions = generateEyeColorListFromCurrentFace();
                        cm.sendStyle("Pick an eye color.", styleOptions);
                        break;

                    // Skin Color
                    case 4:
                        style = 2;
                        styleOptions = generateSkinColorListFromCurrentSkin();
                        cm.sendStyle("Pick a skin color.", styleOptions);
                        break;

                    // Randomize Appearance
                    case 5:
                        style = 3;
                        cm.sendOk("Suddenly I was hit with inspiration by your bravery! Can I do an experimental look on you?" + selection);
                        break;

                    // Gender
                    case 6:
                        style = 4;
                        cm.sendOk("Welcome to Claire's, here is your free gender surgery.");
                        break;

                    // Exit
                    default:
                        cm.sendOk("Too scared? That's okay...");
                        cm.dispose();
                        break;
                }
                break;

            case 2:
                if (style < 0) {
                    cm.sendOk("Too scared? That's okay...");
                    cm.dispose();
                }
                // hair
                else if (style == 0) {
                    cm.setHair(styleOptions[selection]);
                }
                // face
                 else if (style == 1) {
                    cm.setFace(styleOptions[selection]);
                }
                // skin
                 else if (style == 2) {
                    cm.setSkin(styleOptions[selection]);
                }
                // randomize appearance
                else if (style == 3) {
                    cm.setHair(getRandomHairFromGmHandbook());
                    cm.setFace(getRandomFaceFromGmHandbook());
                    cm.setSkin(getRandomSkinFromGmHandbook());
                    cm.sendOk("Voila! You look... wow!");
                }
                // change gender
                else if (style == 4) {
                    if(cm.getPlayer().getGender() == 0){
                        cm.getPlayer().setGender(1);
                    } else if(cm.getPlayer().getGender() == 1){
                        cm.getPlayer().setGender(0);
                    }
                }
                cm.dispose();
                break;

            default:
                cm.sendOk("See ya.");
                cm.dispose();
                break;
        }
    }
    else {
        cm.dispose()
    }
}

// helper that creates a selection list (prefixes each option with "Change")
function generateSelectionMenu(array) {
    var menu = "";
    for (var i = 0; i < array.length; i++) {
        menu += "#L" + i + "# " + array[i] + "#l\r\n";
    }
    return menu;
}

// helper that grabs a new list of all hairs from the NPC handbook.
function generateHairStyleListFromCurrentHair() {
    return CharacterCosmeticsFetcher.getAvailableHairsForCurrentColor(cm.getPlayer().getHair());
}

// helper that grabs a new list of all hair colors from the NPC handbook.
function generateHairColorListFromCurrentHair() {
    return CharacterCosmeticsFetcher.getAvailableHairColorsForCurrentStyle(cm.getPlayer().getHair());
}

// helper that grabs a new list of all faces from the NPC handbook.
function generateFaceListFromCurrentEyeColor() {
    return CharacterCosmeticsFetcher.getFacesForCurrentEyeColor(cm.getPlayer().getFace());
}

// helper that grabs a new list of all eye colors from the NPC handbook.
function generateEyeColorListFromCurrentFace() {
    return CharacterCosmeticsFetcher.getEyeColorsForCurrentFace(cm.getPlayer().getFace());
}

// helper that grabs a new list of all eye colors from the NPC handbook.
function generateSkinColorListFromCurrentSkin() {
    return CharacterCosmeticsFetcher.getOtherSkinColors(cm.getPlayer().getSkinColor());
}

// helper that grabs a random hair from the entire list of hairs
function getRandomHairFromGmHandbook() {
    return CharacterCosmeticsFetcher.getRandomHairFromHandbook();
}

// helper that grabs a random face from the entire list of faces
function getRandomFaceFromGmHandbook() {
    return CharacterCosmeticsFetcher.getRandomFaceFromHandbook();
}

// helper that grabs a random skin color from the entire list of skins
function getRandomSkinFromGmHandbook() {
    return CharacterCosmeticsFetcher.getRandomSkinFromHandbook();
}