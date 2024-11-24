/*
* KIN
* v83 Cosmic
* Author: slimerrain
*/

var status;
var styleOptions = Array();
var style;

// The beginning of the conversation, on interact
function start() {
    status = -1;
    style = -1;
    var options0 = Array(
        "Appearance",
        "Nothing"
    )
    cm.sendSimple("What would you like to do? \r\n" + generateSelectionMenu(options0));
}

// Restart this function every time any option is selected.
function action(mode, type, selection) {
    // Mode -1 if end chat during status == 1 selections
    if (mode == -1) {
        endConversation(status, selection);
    } else {
        // default mode when continuing conversation
        if (mode == 1) {
            status++;
        // Mode 0? If end chat during status == 2 selections
        } else {
            endConversation(status, selection);
        }

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
                    case 1:
                        endConversation(status, selection);
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
                        cm.sendStyle("Pick a contact color.", styleOptions);
                        break;

                    // Skin Color
                    case 4:
                        style = 2;
                        cm.sendNext("case " + selection);
                        break;

                    // Randomize Appearance
                    case 5:
                        style = 3;
                        cm.sendNext("case " + selection);
                        break;

                    // Gender
                    case 6:
                        style = 4;
                        cm.sendOk("Welcome to Claire's, here is your free gender surgery.");
                        break;

                    // Exit
                    case 7:
                        endConversation(status, selection);
                }
                break;

            case 2:
                if (style < 0) {
                    endConversation(status, selection);
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
                    cm.sendOk("Ayo? Skin color change?");
                }
                // randomize appearance
                else if (style == 3) {
                    cm.sendOk("You're very brave!");
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

            default:
                endConversation(status, selection);
        }
    }
}

// helper that creates a selection list (prefixes each option with "Change")
function generateSelectionMenu(array) {
    var menu = "";
    for (var i = 0; i < array.length; i++) {
        menu += "#L" + i + "# Change " + array[i] + "#l\r\n";
    }
    return menu;
}

// helper that grabs a new list of all hairs from the NPC handbook.
function generateHairStyleListFromCurrentHair() {
    var CharacterCosmeticsFetcher = Java.type('tools.mapletools.CharacterCosmeticsFetcher');
    return CharacterCosmeticsFetcher.getAvailableHairsExcludingCurrent(cm.getPlayer().getHair());
}

// helper that grabs a new list of all hairs from the NPC handbook.
function generateHairColorListFromCurrentHair() {
    var CharacterCosmeticsFetcher = Java.type('tools.mapletools.CharacterCosmeticsFetcher');
    return CharacterCosmeticsFetcher.getAvailableHairColorsExcludingCurrent(cm.getPlayer().getHair());
}

// helper that grabs a new list of all hairs from the NPC handbook.
function generateFaceListFromCurrentEyeColor() {
    var CharacterCosmeticsFetcher = Java.type('tools.mapletools.CharacterCosmeticsFetcher');
    return CharacterCosmeticsFetcher.getFacesForCurrentEyeColor(cm.getPlayer().getHair());
}

// helper that grabs a new list of all hairs from the NPC handbook.
function generateEyeColorListFromCurrentFace() {
    var CharacterCosmeticsFetcher = Java.type('tools.mapletools.CharacterCosmeticsFetcher');
    return CharacterCosmeticsFetcher.getEyeColorsForCurrentFace(cm.getPlayer().getHair());
}

// Handles different goodbyes (for fun. Didn't need to do all this tbh.)
function endConversation(bye, selection) {
    switch(bye) {
        case 0:
            cm.sendOk("See ya.");
            break;
        case 1:
            cm.sendOk("Too scared? It's okay, maybe one day.");
            break;
        case 2:
            cm.sendOk("Have a glamorous day!");
            break;
        default:
            cm.sendOk("Have an interesting day~");
            break;
    }
    cm.dispose();
}