package com.edlplan.replay;

import static com.osudroid.data.Scores.ScoreInfo;

import com.osudroid.data.BeatmapInfo;
import com.osudroid.data.ScoreInfo;
import com.rian.osu.mods.LegacyModConverter;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import ru.nsu.ccfit.zuev.osu.scoring.Replay;

public class OsuDroidReplayPack {

    public static void packTo(File file, BeatmapInfo beatmapInfo, ScoreInfo scoreInfo) throws Exception {
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(pack(beatmapInfo, scoreInfo));
        outputStream.close();
    }

    public static byte[] pack(BeatmapInfo beatmapInfo, ScoreInfo scoreInfo) throws Exception {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream outputStream = new ZipOutputStream(byteArrayOutputStream)){
            outputStream.putNextEntry(new ZipEntry("entry.json"));

            JSONObject entryJson = new JSONObject();
            JSONObject replayData = new JSONObject();

            replayData.put("filename", beatmapInfo.getFullBeatmapsetName() + '/' + beatmapInfo.getFullBeatmapName());
            // The keys don't correspond to the score info table columns in order to keep compatibility with the old replays.
            replayData.put("playername", scoreInfo.getPlayerName());
            replayData.put("replayfile", scoreInfo.getReplayFilename());
            replayData.put("beatmapMD5", scoreInfo.getBeatmapMD5());
            replayData.put("mods", scoreInfo.getMods());
            replayData.put("score", scoreInfo.getScore());
            replayData.put("combo", scoreInfo.getMaxCombo());
            replayData.put("mark", scoreInfo.getMark());
            replayData.put("h300k", scoreInfo.getHit300k());
            replayData.put("h300", scoreInfo.getHit300());
            replayData.put("h100k", scoreInfo.getHit100k());
            replayData.put("h100", scoreInfo.getHit100());
            replayData.put("h50", scoreInfo.getHit50());
            replayData.put("misses", scoreInfo.getMisses());
            replayData.put("accuracy", scoreInfo.getAccuracy());
            replayData.put("time", scoreInfo.getTime());
            replayData.put("beatmapMD5", scoreInfo.getSliderEndHits());

            var sliderTickHits = scoreInfo.getSliderTickHits();
            var sliderEndHits = scoreInfo.getSliderEndHits();

            // Inspection will complain about unnecessary unboxing, but JSONObject.put internally casts
            // all Number types to doubles while we want to keep the original integer values.
            //noinspection UnnecessaryUnboxing
            replayData.put("sliderTickHits", sliderTickHits != null ? sliderTickHits.intValue() : null);
            //noinspection UnnecessaryUnboxing
            replayData.put("sliderEndHits", sliderEndHits != null ? sliderEndHits.intValue() : null);

            entryJson.put("version", 3);
            entryJson.put("replaydata", replayData);

            outputStream.write(entryJson.toString(2).getBytes());

            outputStream.putNextEntry(new ZipEntry(scoreInfo.getReplayFilename()));

            File file = new File(scoreInfo.getReplayPath());

            try (FileInputStream inputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int l;
                while ((l = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, l);
                }
            }

            outputStream.finish();
            return byteArrayOutputStream.toByteArray();
        }
    }

    public static ReplayEntry unpack(InputStream raw) throws IOException, JSONException {
        ZipInputStream inputStream = new ZipInputStream(raw);
        ReplayEntry entry = new ReplayEntry();
        Map<String, byte[]> zipEntryMap = new HashMap<>();
        for (ZipEntry zipEntry = inputStream.getNextEntry(); zipEntry != null; zipEntry = inputStream.getNextEntry()) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int l;
            while ((l = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, l);
            }
            zipEntryMap.put(zipEntry.getName(), byteArrayOutputStream.toByteArray());
            System.out.println("解压文件：" + zipEntry.getName() + " size: " + zipEntryMap.get(zipEntry.getName()).length);
        }
        inputStream.close();

        var json = new JSONObject(new String(zipEntryMap.get("entry.json")));
        int version = json.getInt("version");
        var replayData = json.getJSONObject("replaydata");
        var replayFilename = FilenameUtils.getName(replayData.getString("replayfile"));
        var replayFile = zipEntryMap.get(replayFilename);

        if (version < 2) {
            // Exported replay v1 does not contain MD5 hash, so we need to obtain it from the odr file.
            try (var byteArrayInputStream = new ByteArrayInputStream(replayFile)) {
                var replay = new Replay(false);

                if (!replay.load(byteArrayInputStream, replayFilename, false)) {
                    throw new IOException("Failed to load replay");
                }

                replayData.put("beatmapMD5", replay.getMd5());
            }
        }

        if (version < 3) {
            // Exported replays older than v3 stores mods in the `mod` key.
            // Additionally, it uses the legacy mods format and needs to be converted.
            var oldMods = replayData.getString("mod");

            replayData.put("mods", LegacyModConverter.convert(oldMods).serializeMods(false).toString());
            replayData.remove("mod");
        }

        entry.scoreInfo = ScoreInfo(replayData);
        entry.replayFile = replayFile;

        return entry;
    }

    public static class ReplayEntry {
        public ScoreInfo scoreInfo;
        public byte[] replayFile;
    }
}
