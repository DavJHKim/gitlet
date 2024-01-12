package gitlet;

import static gitlet.Utils.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.*;

/** Stage class for GitLet and a way to store files, a subset of the Git version-control system.
 *  @author David Kim
 */

public class Stage implements Serializable {
    private HashMap<String, String> stageAdd;
    private ArrayList<String> stageRemove = new ArrayList<String>();

    public Stage() {
        stageAdd = new HashMap<String, String>();
    }

    public void add(String name, String selfHash) {
        stageAdd.put(name, selfHash);
    }

    public void addToRemove(String name) {
        stageRemove.add(name);
    }

    public HashMap<String, String> getHashMap() {
        return stageAdd;
    }

    public ArrayList<String> getStageRemove() {
        return stageRemove;
    }
}
