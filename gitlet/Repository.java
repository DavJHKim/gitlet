package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import java.io.Serializable;
import java.util.*;

/** Represents a gitlet repository.
 *
 *  @author David Kim
 */
public class Repository implements Serializable {

    private static String HEAD;
    private static Stage stagingArea;
    /**
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File ACTIVE_BRANCH = new File(".gitlet/activebranch");
    public static final File STAGE = new File(".gitlet/stage");
    public static final File BLOBS = new File(".gitlet/blobs");
    public static final File COMMITS = new File(".gitlet/commits");
    public static final File BRANCHES = new File(".gitlet/branches");
    public static final File DIR_HEAD = new File(".gitlet/head");

    public Repository() {
        if (DIR_HEAD.listFiles() != null) {
            File tempHead = new File(".gitlet/head/"
                    + DIR_HEAD.listFiles()[0].getName());
            HEAD = Utils.readObject(tempHead, String.class);
        }
        File tempStage = new File(".gitlet/stage/stagingArea.txt");
        if (tempStage.exists()) {
            stagingArea = Utils.readObject(tempStage, Stage.class);
        }
    }

    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control "
                    + "system already exists in the current directory.");
            return;
        }
        GITLET_DIR.mkdir();
        STAGE.mkdir();
        BLOBS.mkdir();
        COMMITS.mkdir();
        BRANCHES.mkdir();
        DIR_HEAD.mkdir();
        ACTIVE_BRANCH.mkdir();

        Commit initialCommit = new Commit();
        File initCommit = new File(".gitlet/commits/"
                + initialCommit.getSelfHash() + ".txt");
        Utils.writeContents(initCommit, "");
        Utils.writeObject(initCommit, initialCommit);

        HEAD = initialCommit.getSelfHash();
        File initHead = new File(".gitlet/head/" + HEAD + ".txt");
        Utils.writeContents(initHead, "");
        Utils.writeObject(initHead, HEAD);

        Branch initialBranch = new Branch();
        File initBranch = new File(".gitlet/branches/"
                + initialBranch.getBranchName() + ".txt");
        Utils.writeContents(initBranch, "");
        Utils.writeObject(initBranch, initialBranch);

        File activeb = new File(ACTIVE_BRANCH + "/"
                + initialBranch.getBranchName() + ".txt");
        Utils.writeContents(activeb, "");
        Utils.writeObject(activeb, initialBranch);

        stagingArea = new Stage();
        File initStage = new File(".gitlet/stage/"
                + "stagingArea" + ".txt");
        Utils.writeContents(initStage, "");
        Utils.writeObject(initStage, stagingArea);
    }

    public static void add(String name) {
        File temp = findFilesHelper(CWD, name);
        if (temp == null) {
            System.out.println("File does not exist.");
            return;
        }

        Branch b = Utils.readObject(ACTIVE_BRANCH.listFiles()[0], Branch.class);
        Blob blob = new Blob(temp, b);

        File[] blobs = BLOBS.listFiles();
        for (File file : blobs) {
            if ((blob.getSelfHash() + ".txt").equals(file.getName())) {
                if (stagingArea.getStageRemove().contains(name)) {
                    stagingArea.getStageRemove().remove(name);
                    File saveArea = new File(".gitlet/stage/stagingArea.txt");
                    Utils.writeContents(saveArea, "");
                    Utils.writeObject(saveArea, stagingArea);
                    return;
                }
                return;
            }
        }

        stagingArea.add(blob.getName(), blob.getSelfHash());
        File saveArea = new File(".gitlet/stage/stagingArea.txt");
        Utils.writeContents(saveArea, "");
        Utils.writeObject(saveArea, stagingArea);

        File saveBlob = new File(".gitlet/blobs/" + blob.getSelfHash() + ".txt");
        Utils.writeContents(saveBlob, "");
        Utils.writeObject(saveBlob, blob);
    }

    public static void commit(String message) {
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }
        if (stagingArea.getHashMap().isEmpty() && stagingArea.getStageRemove().isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        if (COMMITS.listFiles().length == 1) {
            Commit initCommit = Utils.readObject(COMMITS.listFiles()[0], Commit.class);
            Commit temp = new Commit(message, initCommit.getSelfHash(), stagingArea.getHashMap(), stagingArea.getStageRemove());

            for (File f : BRANCHES.listFiles()) {
                Branch b = Utils.readObject(f, Branch.class);
                if (b.isTag()) {
                    File tempDeleteHead = new File(".gitlet/head/" + DIR_HEAD.listFiles()[0].getName());
                    //Destructive be careful
                    tempDeleteHead.delete();

                    HEAD = temp.getSelfHash();
                    File tempHead = new File(".gitlet/head/" + temp.getSelfHash() + ".txt");
                    Utils.writeContents(tempHead, "");
                    Utils.writeObject(tempHead, HEAD);

                    b.setCommitId(HEAD);
                    File tempBranch = new File(".gitlet/branches/" + b.getBranchName() + ".txt");
                    Utils.writeContents(tempBranch, "");
                    Utils.writeObject(tempBranch, b);

                    File tempCommit = new File(".gitlet/commits/" + temp.getSelfHash() + ".txt");
                    Utils.writeContents(tempCommit, "");
                    Utils.writeObject(tempCommit, temp);
                }
            }
            commitHelper1(temp);
        } else {
            Commit tempCommit = new Commit(message, HEAD, stagingArea.getHashMap(), stagingArea.getStageRemove());

            Branch b = null;
            for (File f : BRANCHES.listFiles()) {
                Branch tempB = Utils.readObject(f, Branch.class);
                if (tempB.getCommitId().equals(HEAD)) {
                    b = tempB;
                    break;
                }
            }

            commitHelper(tempCommit, b);
        }
        stagingArea.getHashMap().clear();
        stagingArea.getStageRemove().clear();
        File saveArea = new File(".gitlet/stage/stagingArea.txt");
        Utils.writeContents(saveArea, "");
        Utils.writeObject(saveArea, stagingArea);
    }

    public static void checkout(String[] args) {
        if (args.length == 2) {
            File tempBranchFile = findFilesHelper(BRANCHES, args[1] + ".txt");
            if (tempBranchFile == null) {
                System.out.println("No such branch exists.");
                return;
            }
            Branch tempBranch = Utils.readObject(tempBranchFile, Branch.class);
            if ((tempBranch.getBranchName() + ".txt").equals
                    (ACTIVE_BRANCH.listFiles()[0].getName())) {
                System.out.println("No need to " +
                        "checkout the current branch.");
                return;
            }
            Commit headCommit = Utils.readObject(findFilesHelper
                    (COMMITS, HEAD + ".txt"), Commit.class);
            Commit tempCommit = Utils.readObject(findFilesHelper
                    (COMMITS, tempBranch.getCommitId() + ".txt"), Commit.class);
            if (tempCommit.getBlobs() == null && headCommit.getBlobs() == null) {
                doNothing();
            } else if (tempCommit.getBlobs() == null) {
                for (File f : getAllFilesCWD(CWD)) {
                    if (headCommit.getBlobs().containsKey(f.getName())) {
                        //Destructive be careful
                        findFilesHelper(CWD, f.getName()).delete();
                    }
                }
            } else {
                checkoutHelper(headCommit, tempCommit);
            }
            stagingArea.getHashMap().clear();
            stagingArea.getStageRemove().clear();
            File saveArea = new File(".gitlet/stage/stagingArea.txt");
            Utils.writeContents(saveArea, "");
            Utils.writeObject(saveArea, stagingArea);

            File tempDeleteHead = new File(".gitlet/head/"
                    + DIR_HEAD.listFiles()[0].getName());
            //Destructive be careful
            tempDeleteHead.delete();

            HEAD = tempCommit.getSelfHash();
            File tempHead = new File(".gitlet/head/"
                    + tempCommit.getSelfHash() + ".txt");
            Utils.writeContents(tempHead, "");
            Utils.writeObject(tempHead, HEAD);

            ACTIVE_BRANCH.listFiles()[0].delete();
            File activeb = new File(ACTIVE_BRANCH + "/"
                    + tempBranch.getBranchName() + ".txt");
            Utils.writeContents(activeb, "");
            Utils.writeObject(activeb, tempBranch);

            if (tempCommit.getBlobs() != null) {
                for (String s : tempCommit.getBlobs().keySet()) {
                    File fileBlob = findFilesHelper(BLOBS,
                            tempCommit.getBlobs().get(s) + ".txt");
                    Blob tempBlob = Utils.readObject(fileBlob, Blob.class);
                    byte[] content = tempBlob.getContent();
                    Utils.writeContents(tempBlob.getFile(), "");
                    Utils.writeContents(tempBlob.getFile(), content);
                }
            }

            tempBranch.setTag(true);
            Utils.writeContents(tempBranchFile, "");
            Utils.writeObject(tempBranchFile, tempBranch);
        } else if (args.length == 3) {
            checkoutHelper2(args[2]);
        } else if (args.length == 4) {
            checkoutHelper1(args[1], args[2], args[3]);
        } else {
            System.out.println("Incorrect operands.");
            return;
        }
    }

    public static void log() {
        File commitFound = findFilesHelper(COMMITS, HEAD + ".txt");
        if (commitFound != null) {
            Commit tempCommit =
                    Utils.readObject(commitFound, Commit.class);
            while (tempCommit != null) {
                System.out.println("===");
                System.out.println("commit " + tempCommit.getSelfHash());
                System.out.println("Date: " + tempCommit.getDate());
                System.out.println(tempCommit.getMessage());
                System.out.println();
                if (!tempCommit.getParentHash().equals("")) {
                    File parentCommit =
                            findFilesHelper(COMMITS,
                                    tempCommit.getParentHash() + ".txt");
                    tempCommit = Utils.readObject(parentCommit, Commit.class);
                } else {
                    break;
                }
            }
        }
    }

    public static void status() {
        System.out.println("=== Branches ===");
        ArrayList<File> brs = new ArrayList<File>();
        for (File f : BRANCHES.listFiles()) {
            brs.add(f);
        }
        String tempString = "";
        for (File f : brs) {
            Branch temp = Utils.readObject(f, Branch.class);
            if (temp.getCommitId().equals(HEAD)) {
                tempString = temp.getBranchName();
                brs.remove(f);
                break;
            }
        }
        System.out.println("*" + tempString);
        Collections.sort(brs);
        for (File f : brs) {
            Branch temp = Utils.readObject(f, Branch.class);
            System.out.println(temp.getBranchName());
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        ArrayList<String> stagedFiles =
                new ArrayList<String>(stagingArea.getHashMap().keySet());
        Collections.sort(stagedFiles);
        for (String s : stagedFiles) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        ArrayList<String> removedFiles =
                new ArrayList<String>(stagingArea.getStageRemove());
        for (String s : removedFiles) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
    }

    public static void rm(String name) {
        File temp = findFilesHelper(COMMITS, HEAD + ".txt");
        Commit tempCommit = Utils.readObject(temp, Commit.class);
        if (tempCommit.getBlobs() != null) {
            ArrayList<String> comFiles =
                    new ArrayList<>(tempCommit.getBlobs().keySet());
            for (String s : comFiles) {
                if (s.equals(name)) {
                    File tempDelete = findFilesHelper(CWD, name);
                    stagingArea.addToRemove(name);
                    if (tempDelete == null) {
                        if (stagingArea.getHashMap().containsKey(name)) {
                            stagingArea.getHashMap().remove(name);
                        }
                        File saveArea = new File(".gitlet/"
                                + "stage/stagingArea.txt");
                        Utils.writeContents(saveArea, "");
                        Utils.writeObject(saveArea, stagingArea);
                        return;
                    }
                    //Destructive be careful
                    tempDelete.delete();
                    if (stagingArea.getHashMap().containsKey(name)) {
                        stagingArea.getHashMap().remove(name);
                    }
                    File saveArea = new File(".gitlet/"
                            + "stage/stagingArea.txt");
                    Utils.writeContents(saveArea, "");
                    Utils.writeObject(saveArea, stagingArea);
                    return;
                }
            }
        }
        if (stagingArea.getHashMap().containsKey(name)) {
            stagingArea.getHashMap().remove(name);
            File saveArea = new File(".gitlet/stage/stagingArea.txt");
            Utils.writeContents(saveArea, "");
            Utils.writeObject(saveArea, stagingArea);
            return;
        }
        System.out.println("No reason to remove the file.");
        return;
    }

    public static void globalLog() {
        File[] temp = COMMITS.listFiles();
        for (File f : temp) {
            Commit tempCommit = Utils.readObject(f, Commit.class);
            System.out.println("===");
            System.out.println("commit " + tempCommit.getSelfHash());
            System.out.println("Date: " + tempCommit.getDate());
            System.out.println(tempCommit.getMessage());
            System.out.println();
        }
    }

    public static void find(String message) {
        File[] temp = COMMITS.listFiles();
        int count = 0;
        for (File f : temp) {
            Commit tempCommit = Utils.readObject(f, Commit.class);
            if (message.equals(tempCommit.getMessage())) {
                System.out.println(tempCommit.getSelfHash());
                count++;
            }
        }
        if (count == 0) {
            System.out.println("Found no commit with that message.");
            return;
        }
    }

    public static void reset(String commitId) {
        File tempCommitFile = findFilesHelper(COMMITS, commitId + ".txt");
        if (tempCommitFile != null) {
            Commit headCommit = Utils.readObject(findFilesHelper(
                    COMMITS, HEAD + ".txt"), Commit.class);
            Commit tempCommit = Utils.readObject(tempCommitFile, Commit.class);
            if (tempCommit.getBlobs() == null
                    && headCommit.getBlobs() == null) {
                doNothing();
            } else if (tempCommit.getBlobs() == null) {
                for (File f : getAllFilesCWD(CWD)) {
                    if (headCommit.getBlobs().containsKey(f.getName())) {
                        //Destructive be careful
                        findFilesHelper(CWD, f.getName()).delete();
                    }
                }
            } else {
                for (File f : getAllFilesCWD(CWD)) {
                    if (!headCommit.getBlobs().containsKey(f.getName())
                            && tempCommit.getBlobs().containsKey(f.getName())) {
                        System.out.println("There is an untracked file in the "
                                + "way; delete it, or add and commit it first.");
                        return;
                    }
                }
                for (File f : getAllFilesCWD(CWD)) {
                    if (headCommit.getBlobs().containsKey(f.getName())
                            && !tempCommit.getBlobs().containsKey(f.getName())) {
                        //Destructive be careful
                        findFilesHelper(CWD, f.getName()).delete();
                    }
                }
            }
            stagingArea.getHashMap().clear();
            stagingArea.getStageRemove().clear();
            File saveArea = new File(".gitlet/stage/stagingArea.txt");
            Utils.writeContents(saveArea, "");
            Utils.writeObject(saveArea, stagingArea);

            Branch b = findHeadBranch();
            File tempDeleteHead = new File(".gitlet/head/"
                    + DIR_HEAD.listFiles()[0].getName());
            //Destructive be careful
            tempDeleteHead.delete();

            HEAD = tempCommit.getSelfHash();
            File tempHead = new File(".gitlet/head/"
                    + tempCommit.getSelfHash() + ".txt");
            Utils.writeContents(tempHead, "");
            Utils.writeObject(tempHead, HEAD);
            if (tempCommit.getBlobs() != null) {
                for (String s : tempCommit.getBlobs().keySet()) {
                    File fileBlob = findFilesHelper(BLOBS
                            , tempCommit.getBlobs().get(s) + ".txt");
                    Blob tempBlob = Utils.readObject(fileBlob, Blob.class);
                    byte[] content = tempBlob.getContent();
                    Utils.writeContents(tempBlob.getFile(), "");
                    Utils.writeContents(tempBlob.getFile(), content);
                }
            }
            b.setCommitId(HEAD);
            ACTIVE_BRANCH.listFiles()[0].delete();
            File activeb = new File(ACTIVE_BRANCH
                    + "/" + b.getBranchName() + ".txt");
            Utils.writeContents(activeb, "");
            Utils.writeObject(activeb, b);
            File tempBranchFile = new File(".gitlet/branches/"
                    + b.getBranchName() + ".txt");
            Utils.writeContents(tempBranchFile, "");
            Utils.writeObject(tempBranchFile, b);
            return;
        }
        System.out.println("No commit with that id exists.");
        return;
    }

    public static void branch(String branchName) {
        if (findFilesHelper(BRANCHES, branchName + ".txt") != null) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        Branch temp = new Branch(branchName, false);
        File saveBranch = new File(BRANCHES + "/" + branchName + ".txt");
        Utils.writeContents(saveBranch, "");
        Utils.writeObject(saveBranch, temp);
    }

    public static void rmBranch(String branchName) {
        File tempBranchFile = findFilesHelper(BRANCHES,
                branchName + ".txt");
        if (tempBranchFile == null) {
            System.out.println("A branch with "
                    + "that name does not exist.");
            return;
        }
        Branch tempBranch = Utils.readObject(tempBranchFile,
                Branch.class);
        if (tempBranch.getCommitId().equals(HEAD)) {
            System.out.println("Cannot remove "
                    + "the current branch.");
            return;
        } else {
            File fileCommit = findFilesHelper(COMMITS,
                    tempBranch.getCommitId() + ".txt");
            Commit tempCommit = Utils.readObject(fileCommit,
                    Commit.class);
            tempBranchFile.delete();
            return;
        }
    }

    //Source: StackOverflow https://stackoverflow.com
    // /questions/15482423/how-to-list-the-files-in-current-directory
    private static File findFilesHelper(File curDirec, String name) {
        File[] temp = curDirec.listFiles();
        for (File file : temp) {
            if (file.isDirectory()) {
                if (findFilesHelper(file, name) != null) {
                    return findFilesHelper(file, name);
                }
            }
            if (file.getName().equals(name)) {
                return file;
            }
        }
        return null;
    }

    private static Branch findHeadBranch() {
        for (File f : BRANCHES.listFiles()) {
            Branch b = Utils.readObject(f, Branch.class);
            if (b.getCommitId().equals(HEAD)) {
                return b;
            }
        }
        return null;
    }

    public static String getHEAD() {
        return HEAD;
    }

    //Source: StackOverflow https://stackoverflow.com/questions
    // /14676407/list-all-files-in-the-folder-and-also-sub-folders
    public static List<File> getAllFilesCWD(File dir) {
        File directory = dir;

        List<File> result = new ArrayList<File>();

        File[] fList = directory.listFiles();
        for (File f : fList) {
            if (f.isFile()) {
                result.add(f.getAbsoluteFile());
            } else if (f.isDirectory()) {
                result.addAll(getAllFilesCWD(f.getAbsoluteFile()));
            }
        }
        return result;
    }

    public static void doNothing() {
        return;
    }

    public static void commitHelper(Commit tempCommit, Branch b) {
        File tempDeleteHead = new File(".gitlet/head/"
                + DIR_HEAD.listFiles()[0].getName());
        //Destructive be careful
        tempDeleteHead.delete();

        HEAD = tempCommit.getSelfHash();
        File tempHead = new File(".gitlet/head/"
                + tempCommit.getSelfHash() + ".txt");
        Utils.writeContents(tempHead, "");
        Utils.writeObject(tempHead, HEAD);

        b.setCommitId(HEAD);
        File tempBranch = new File(".gitlet/branches/"
                + b.getBranchName() + ".txt");
        Utils.writeContents(tempBranch, "");
        Utils.writeObject(tempBranch, b);

        ACTIVE_BRANCH.listFiles()[0].delete();
        File activeb = new File(ACTIVE_BRANCH + "/"
                + b.getBranchName() + ".txt");
        Utils.writeContents(activeb, "");
        Utils.writeObject(activeb, b);

        File tempCom = new File(".gitlet/commits/"
                + tempCommit.getSelfHash() + ".txt");
        Utils.writeContents(tempCom, "");
        Utils.writeObject(tempCom, tempCommit);
    }

    public static void commitHelper1(Commit temp) {
        File tempDeleteHead = new File(".gitlet/head/"
                + DIR_HEAD.listFiles()[0].getName());
        //Destructive be careful
        tempDeleteHead.delete();

        HEAD = temp.getSelfHash();
        File tempHead = new File(".gitlet/head/"
                + temp.getSelfHash() + ".txt");
        Utils.writeContents(tempHead, "");
        Utils.writeObject(tempHead, HEAD);

        Branch b = Utils.readObject(findFilesHelper(BRANCHES,
                "main.txt"), Branch.class);
        b.setCommitId(HEAD);
        File tempBranch = new File(".gitlet/branches/main.txt");
        Utils.writeContents(tempBranch, "");
        Utils.writeObject(tempBranch, b);

        ACTIVE_BRANCH.listFiles()[0].delete();
        File activeb = new File(ACTIVE_BRANCH
                + "/" + b.getBranchName() + ".txt");
        Utils.writeContents(activeb, "");
        Utils.writeObject(activeb, b);

        File tempCommit = new File(".gitlet/commits/"
                + temp.getSelfHash() + ".txt");
        Utils.writeContents(tempCommit, "");
        Utils.writeObject(tempCommit, temp);
    }

    public static void checkoutHelper(Commit headCommit,
                                      Commit tempCommit) {
        if (headCommit.getBlobs() == null) {
            for (File f : getAllFilesCWD(CWD)) {
                if (tempCommit.getBlobs().get(f.getName())
                        != null) {
                    Branch b = Utils.readObject(ACTIVE_BRANCH.
                            listFiles()[0], Branch.class);
                    Blob blob = new Blob(f, b);
                    int count = 0;
                    for (File file : BLOBS.listFiles()) {
                        if ((blob.getSelfHash() + ".txt").equals(file.getName())) {
                            count++;
                        }
                    }
                    if (count == 0) {
                        System.out.println("There is an untracked file"
                                + " in the way; delete it, "
                                + "or add and commit it first.");
                        return;
                    }
                }
            }
        }
        for (File f : getAllFilesCWD(CWD)) {
            if (!headCommit.getBlobs().containsKey(f.getName())
                    && tempCommit.getBlobs().containsKey(f.getName())) {
                System.out.println("There is an untracked file "
                        + "in the way; delete it, or add and commit it first.");
                return;
            }
        }
        for (File f : getAllFilesCWD(CWD)) {
            if (headCommit.getBlobs().containsKey(f.getName())
                    && !tempCommit.getBlobs().containsKey(f.getName())) {
                //Destructive be careful
                findFilesHelper(CWD, f.getName()).delete();
            }
        }
    }

    public static void checkoutHelper1(
            String argsOne, String argsTwo,
            String argsThree) {
        if (argsTwo.equals("--")) {
            String tempCommitId = argsOne;
            for (File f : COMMITS.listFiles()) {
                if (f.getName().contains(tempCommitId)) {
                    tempCommitId = f.getName();
                    break;
                }
            }
            File[] tempCommits = COMMITS.listFiles();
            for (File file : tempCommits) {
                if (file.getName().equals(tempCommitId)) {
                    Commit nameCommit = Utils.readObject(file, Commit.class);
                    if (nameCommit.getBlobs().containsKey(argsThree)) {
                        File n = findFilesHelper(CWD, argsThree);
                        File fileBlob = findFilesHelper(BLOBS,
                                nameCommit.getBlobs().get(argsThree) + ".txt");
                        Blob tempBlob =
                                Utils.readObject(fileBlob, Blob.class);
                        byte[] blobContent = tempBlob.getContent();
                        Utils.writeContents(n, blobContent);
                        return;
                    }
                    System.out.println("File does" +
                            " not exist in that commit.");
                    return;
                }
            }
            System.out.println("No commit with that id exists.");
            return;
        }
        System.out.println("Incorrect operands.");
        return;
    }

    public static void checkoutHelper2(String argsTwo) {
        File[] tempCommits = COMMITS.listFiles();
        for (File file : tempCommits) {
            if (file.getName().equals(HEAD + ".txt")) {
                Commit nameCommit = Utils.readObject(file, Commit.class);
                if (nameCommit.getBlobs().containsKey(argsTwo)) {
                    File n = findFilesHelper(CWD, argsTwo);
                    File fileBlob = findFilesHelper(BLOBS,
                            nameCommit.getBlobs().get(argsTwo) + ".txt");
                    Blob tempBlob = Utils.readObject(fileBlob, Blob.class);
                    byte[] blobContent = tempBlob.getContent();
                    Utils.writeContents(n, blobContent);
                    return;
                }
            }
        }
        System.out.println("File does not exist in that commit.");
        return;
    }
}
