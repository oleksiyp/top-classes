package extraction;

import java.io.File;

import org.apache.maven.artifact.versioning.ComparableVersion;

public class LastMavenVersionDetector {
    ComparableVersion latest = null;
    File latestFile;

    public boolean isLastVersion(File artifactFile) {
        ComparableVersion latest = getLatestVersion(artifactFile.getParentFile());
        return latest != null &&
               artifactFile.getName().equals(latest.toString());

    }

    private ComparableVersion getLatestVersion(File file) {
        file = file.getAbsoluteFile();

        if (latestFile != null && latestFile.equals(file)) {
            return latest;
        }

        latestFile = file;
        latest = null;

        File[] lst = latestFile.listFiles();
        if (lst == null) {
            return latest;
        }

        for (File versionDir : lst) {
            ComparableVersion ver = new ComparableVersion(versionDir.getName());
            if (latest == null || ver.compareTo(latest) >= 0) {
                latest = ver;
            }
        }

        return latest;
    }
}
