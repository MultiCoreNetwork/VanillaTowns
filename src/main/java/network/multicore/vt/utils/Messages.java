package network.multicore.vt.utils;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import network.multicore.vt.VanillaTowns;

import java.util.List;

public class Messages {
    private static Messages instance;
    private final Section messages;

    private Messages(VanillaTowns plugin) {
        this.messages = plugin.config().getSection("messages");
    }

    public static Messages get() {
        if (instance == null) throw new IllegalStateException("Messages not initialized");
        return instance;
    }

    public static void init(VanillaTowns plugin) {
        instance = new Messages(plugin);
    }

    public String get(String route) {
        return messages.getString(route, "");
    }

    public String getAndReplace(String route, String[] targets, Object[] replacements) {
        String msg = messages.getString(route, "");

        for (int i = 0; i < targets.length; i++) {
            msg = msg.replace("{" + targets[i] + "}", replacements[i].toString());
        }

        return msg;
    }

    public String getAndReplace(String route, String target, Object replacement) {
        return getAndReplace(route, new String[]{target}, new Object[]{replacement});
    }

    public String getAndReplace(String route, String target1, Object replacement1, String target2, Object replacement2) {
        return getAndReplace(route, new String[]{target1, target2}, new Object[]{replacement1, replacement2});
    }

    public String getAndReplace(String route, String target1, Object replacement1, String target2, Object replacement2, String target3, Object replacement3) {
        return getAndReplace(route, new String[]{target1, target2, target3}, new Object[]{replacement1, replacement2, replacement3});
    }

    public String getAndReplace(String route, String target1, Object replacement1, String target2, Object replacement2, String target3, Object replacement3, String target4, Object replacement4) {
        return getAndReplace(route, new String[]{target1, target2, target3, target4}, new Object[]{replacement1, replacement2, replacement3, replacement4});
    }

    public String getAndReplace(String route, String target1, Object replacement1, String target2, Object replacement2, String target3, Object replacement3, String target4, Object replacement4, String target5, Object replacement5) {
        return getAndReplace(route, new String[]{target1, target2, target3, target4, target5}, new Object[]{replacement1, replacement2, replacement3, replacement4, replacement5});
    }

    public String getAndReplace(String route, String target1, Object replacement1, String target2, Object replacement2, String target3, Object replacement3, String target4, Object replacement4, String target5, Object replacement5, String target6, Object replacement6) {
        return getAndReplace(route, new String[]{target1, target2, target3, target4, target5, target6}, new Object[]{replacement1, replacement2, replacement3, replacement4, replacement5, replacement6});
    }

    public String getAndReplace(String route, String target1, Object replacement1, String target2, Object replacement2, String target3, Object replacement3, String target4, Object replacement4, String target5, Object replacement5, String target6, Object replacement6, String target7, Object replacement7) {
        return getAndReplace(route, new String[]{target1, target2, target3, target4, target5, target6, target7}, new Object[]{replacement1, replacement2, replacement3, replacement4, replacement5, replacement6, replacement7});
    }

    public String getAndReplace(String route, String target1, Object replacement1, String target2, Object replacement2, String target3, Object replacement3, String target4, Object replacement4, String target5, Object replacement5, String target6, Object replacement6, String target7, Object replacement7, String target8, Object replacement8) {
        return getAndReplace(route, new String[]{target1, target2, target3, target4, target5, target6, target7, target8}, new Object[]{replacement1, replacement2, replacement3, replacement4, replacement5, replacement6, replacement7, replacement8});
    }

    public List<String> getList(String route) {
        return messages.getStringList(route);
    }

    public List<String> getListAndReplace(String route, String[] targets, Object[] replacements) {
        List<String> list = messages.getStringList(route);

        for (int i = 0; i < list.size(); i++) {
            String msg = list.get(i);

            for (int j = 0; j < targets.length; j++) {
                msg = msg.replace("{" + targets[j] + "}", replacements[j].toString());
            }

            list.set(i, msg);
        }

        return list;
    }

    public List<String> getListAndReplace(String route, String target, Object replacement) {
        return getListAndReplace(route, new String[]{target}, new Object[]{replacement});
    }

    public List<String> getListAndReplace(String route, String target1, Object replacement1, String target2, Object replacement2) {
        return getListAndReplace(route, new String[]{target1, target2}, new Object[]{replacement1, replacement2});
    }

    public List<String> getListAndReplace(String route, String target1, Object replacement1, String target2, Object replacement2, String target3, Object replacement3) {
        return getListAndReplace(route, new String[]{target1, target2, target3}, new Object[]{replacement1, replacement2, replacement3});
    }

    public List<String> getListAndReplace(String route, String target1, Object replacement1, String target2, Object replacement2, String target3, Object replacement3, String target4, Object replacement4) {
        return getListAndReplace(route, new String[]{target1, target2, target3, target4}, new Object[]{replacement1, replacement2, replacement3, replacement4});
    }

    public List<String> getListAndReplace(String route, String target1, Object replacement1, String target2, Object replacement2, String target3, Object replacement3, String target4, Object replacement4, String target5, Object replacement5) {
        return getListAndReplace(route, new String[]{target1, target2, target3, target4, target5}, new Object[]{replacement1, replacement2, replacement3, replacement4, replacement5});
    }

    public List<String> getListAndReplace(String route, String target1, Object replacement1, String target2, Object replacement2, String target3, Object replacement3, String target4, Object replacement4, String target5, Object replacement5, String target6, Object replacement6) {
        return getListAndReplace(route, new String[]{target1, target2, target3, target4, target5, target6}, new Object[]{replacement1, replacement2, replacement3, replacement4, replacement5, replacement6});
    }

    public List<String> getListAndReplace(String route, String target1, Object replacement1, String target2, Object replacement2, String target3, Object replacement3, String target4, Object replacement4, String target5, Object replacement5, String target6, Object replacement6, String target7, Object replacement7) {
        return getListAndReplace(route, new String[]{target1, target2, target3, target4, target5, target6, target7}, new Object[]{replacement1, replacement2, replacement3, replacement4, replacement5, replacement6, replacement7});
    }
}
