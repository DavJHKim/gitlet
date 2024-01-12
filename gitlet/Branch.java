package gitlet;

import static gitlet.Utils.*;
import java.io.Serializable;

/** Represents a branch of the version.
 *
 *  @author David Kim
 */

public class Branch implements Serializable {
    private String branchName;
    private String commitId;
    private boolean tag;

    //initial commit
    public Branch() {
        branchName = "main";
        commitId = Repository.getHEAD();
        tag = false;
    }

    public Branch(String bName, boolean t) {
        branchName = bName;
        commitId = Repository.getHEAD();
        tag = t;
    }

    public void saveBranchToCommit(Commit c) {
        commitId = c.getSelfHash();
    }

    public String getBranchName() {
        return branchName;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public boolean isTag() {
        return tag;
    }

    public void setTag(boolean t) {
        tag = t;
    }
}
