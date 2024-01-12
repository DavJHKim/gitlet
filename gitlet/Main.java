package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author David Kim
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */

    public static void main(String[] args) {
        Repository repo = new Repository();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                initSetUp(args, repo);
                break;
            case "add":
                addSetUp(args, repo);
                break;
            case "checkout":
                checkoutSetUp(args, repo);
                break;
            case "log":
                logSetUp(args, repo);
                break;
            case "commit":
                commitSetUp(args, repo);
                break;
            case "status":
                statusSetUp(args, repo);
                break;
            case "rm":
                rmSetUp(args, repo);
                break;
            case "global-log":
                globalLogSetUp(args, repo);
                break;
            case "find":
                findSetUp(args, repo);
                break;
            case "reset":
                resetSetUp(args, repo);
                break;
            case "branch":
                branchSetUp(args, repo);
                break;
            case "rm-branch":
                rmBranchSetUp(args, repo);
                break;
            default:
                System.out.println("No command with that name exists.");
                return;
        }
    }

    public static void initSetUp(String[] args, Repository repo) {
        validateNumArgs(args, 1);
        repo.init();
    }

    public static void addSetUp(String[] args, Repository repo) {
        validateNumArgs(args, 2);
        repo.add(args[1]);
    }

    public static void commitSetUp(String[] args, Repository repo) {
        validateNumArgs(args, 2);
        repo.commit(args[1]);
    }

    public static void checkoutSetUp(String[] args, Repository repo) {
        repo.checkout(args);
    }

    public static void logSetUp(String[] args, Repository repo) {
        validateNumArgs(args, 1);
        repo.log();
    }

    public static void statusSetUp(String[] args, Repository repo) {
        validateNumArgs(args, 1);
        repo.status();
    }

    public static void rmSetUp(String[] args, Repository repo) {
        validateNumArgs(args, 2);
        repo.rm(args[1]);
    }

    public static void globalLogSetUp(String[] args, Repository repo) {
        validateNumArgs(args, 1);
        repo.globalLog();
    }

    public static void findSetUp(String[] args, Repository repo) {
        validateNumArgs(args, 2);
        repo.find(args[1]);
    }

    public static void resetSetUp(String[] args, Repository repo) {
        validateNumArgs(args, 2);
        repo.reset(args[1]);
    }

    public static void branchSetUp(String[] args, Repository repo) {
        validateNumArgs(args, 2);
        repo.branch(args[1]);
    }

    public static void rmBranchSetUp(String[] args, Repository repo) {
        validateNumArgs(args, 2);
        repo.rmBranch(args[1]);
    }

    //Taken from lab08
    public static void exitWithError(String message) {
        if (message != null && !message.equals("")) {
            System.out.println(message);
        }
        System.exit(-1);
    }

    //Taken from lab08
    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(-1);
        }
    }
}
