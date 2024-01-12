package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import java.io.Serializable;

/** Represents information stored for a file
 *
 *  @author David Kim
 */

public class Blob implements Serializable {
    private File file;
    private String selfHash;
    private byte[] content;
    private Branch branch;

    public Blob(File f, Branch b) {
        file = f;
        content = Utils.readContents(f);
        selfHash = generateHash();
        branch = b;
    }

    public String generateHash() {
        byte[] hash = Utils.serialize(this);
        return Utils.sha1(hash);
    }

    public String getSelfHash() {
        return selfHash;
    }

    public String getName() {
        return file.getName();
    }

    public byte[] getContent() {
        return content;
    }

    public File getFile() {
        return file;
    }

    public Branch getBranch() {
        return branch;
    }
}
