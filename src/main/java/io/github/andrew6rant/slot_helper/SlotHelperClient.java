package io.github.andrew6rant.slot_helper;

import io.github.andrew6rant.slot_helper.data.ResourceLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public class SlotHelperClient implements ClientModInitializer {
    // Massive thanks to omoflop, much of this code is derived from CustomGUI

    public static void warn(Object s, Object... args) {
        print("[WARN] " + s, args);
    }
    public static void print(Object s, Object... args) {
        String _s = (s == null ? "null" : s.toString());
        System.out.printf("[Slot Helper] %s\n", String.format(_s,args));
    }

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new ResourceLoader());
    }
}
