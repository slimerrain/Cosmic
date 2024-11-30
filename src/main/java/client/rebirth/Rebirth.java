package client.rebirth;

import client.Character;
import client.Job;
import client.Stat;
import tools.DatabaseConnection;
import tools.PacketCreator;
import tools.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
        addCharacterRebirths(chr);
        List<Pair<Stat, Integer>> statup = new ArrayList<>(1);
        statup.add(new Pair<>(Stat.LEVEL, 1));
        chr.getClient().sendPacket(PacketCreator.updatePlayerStats(statup, true, chr));
        chr.updateStrDexIntLuk(4,chr.getRebirthCount() * 100);
    }

    public static void addCharacterRebirths(Character chr) {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE characters SET rebirths = ? WHERE id = ?")) {
            ps.setInt(1, chr.getRebirthCount() + 1);
            ps.setInt(2, chr.getId());
            ps.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
}
