package gitlet;

import java.util.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

/** Represents a gitlet commit object.
 *
 *  @author David Kim
 */
public class Commit implements Serializable {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    private String parentHash;
    private String selfHash;
    private String message;
    private Date date;
    private HashMap<String, String> blobs;
    private ArrayList<String> removedFiles;

    //initial Commit
    public Commit() {
        message = "initial commit";
        date = new Date(0);
        parentHash = "";
        selfHash = generateHash();
        blobs = null;
        removedFiles = new ArrayList<String>();
    }

    public Commit(String mes, String parHash, HashMap<String, String> tempBlob,
                  ArrayList<String> tempRemovedFiles) {
        message = mes;
        date = new Date();
        parentHash = parHash;
        selfHash = generateHash();
        blobs = tempBlob;
        removedFiles = tempRemovedFiles;
    }

    public void add(String name, String sHash) {
        blobs.put(name, sHash);
    }

    public String generateHash() {
        byte[] commit = Utils.serialize(this);
        return Utils.sha1(commit);
    }

    public String getSelfHash() {
        return selfHash;
    }

    public String getDate() {
        DateFormat form = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        return form.format(date);
    }

    public String getMessage() {
        return message;
    }

    public String getParentHash() {
        return parentHash;
    }

    public HashMap<String, String> getBlobs() {
        return blobs;
    }

    public ArrayList<String> getRemovedFiles() {
        return removedFiles;
    }
}

