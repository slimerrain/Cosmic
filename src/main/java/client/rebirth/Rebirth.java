package client.rebirth;

import client.Character;
import client.Job;

public class Rebirth {
    public static boolean isEligibleForRebirth (Character chr) {
        return chr.getLevel() > 199;
    }

    public static void rebirth (Character chr) {
        rebirthToJob(chr, (chr.getJob().getId()/1000)*1000);
    }

    public static void rebirthToExplorer (Character chr) {
        rebirthToJob(chr, 0);
    }

    public static void rebirthToCygnus (Character chr) {
        rebirthToJob(chr, 1000);
    }

    public static void rebirthToAran (Character chr) {
        rebirthToJob(chr, 2000);
    }

    public static void rebirthToJob (Character chr, int jobId) {
        chr.setLevel(1);
        chr.setJob(Job.getById(jobId));
        chr.saveCharToDB();
    }
}
