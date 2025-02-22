package com.runemate.woodcutter;

import com.runemate.ui.setting.annotation.open.*;
import com.runemate.ui.setting.open.*;

/*
 * This is a very simple implementation of a settings descriptor. The descriptor needs to be annotated with '@SettingsGroup' and extend
 * 'Settings' in order for RuneMate to identify it as such.
 *
 * This is an interface, so you do not need to provide implementations for the methods. However, you can mark the methods as 'default'
 * if you want to provide default values for your settings.
 */
@SettingsGroup
public interface WoodcuttingSettings extends Settings {

    /*
     * The @Setting annotation provides RuneMate with the information it needs to display this setting on the user interface.
     * The 'key' attribute tells RuneMate what name to store the setting under.
     * The 'title' attribute tells RuneMate what name to display next to the setting in the user interface.
     * The 'order' attribute tells RuneMate how to order the settings.
     */
    @Setting(key = "treeType", title = "Tree type", order = 1)
    default TreeType getTreeType() {
        return TreeType.NORMAL;
    }

}
