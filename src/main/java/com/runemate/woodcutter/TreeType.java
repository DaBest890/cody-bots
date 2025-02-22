package com.runemate.woodcutter;

/*
 * This is an enum that holds some constant (immutable) data such as the name of the tree, the type of logs it produces and the woodcutting
 * level required to chop the trees.
 */
public enum TreeType {
    NORMAL("Tree", "Logs", 1),
    OAK("Oak tree", "Oak logs", 15),
    WILLOW("Willow tree", "Willow logs", 30),
    MAPLE("Maple tree", "Maple logs", 45),
    YEW("Yew tree", "Yew logs", 60),
    MAGIC("Magic tree", "Magic logs", 75);

    private final String treeName;
    private final String logName;
    private final int requiredLevel;

    TreeType(final String treeName, final String logName, final int requiredLevel) {
        this.treeName = treeName;
        this.logName = logName;
        this.requiredLevel = requiredLevel;
    }

    public String getTreeName() {
        return treeName;
    }

    public String getLogName() {
        return logName;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }
}
