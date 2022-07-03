package io.github.andrew6rant.slot_helper.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.andrew6rant.slot_helper.SlotHelperClient;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.JsonHelper;

import java.io.*;
import java.util.Map;
import java.util.Optional;

public class ResourceLoader implements SimpleSynchronousResourceReloadListener {
    // Massive thanks to omoflop, much of this code is derived from CustomGUI

    public static final Identifier RESOURCE_LOADER_ID = new Identifier("slot_helper", "gui_overrides");

    public static Map<Identifier, Resource> resources;

    public static JsonObject jsonObject;

    @Override
    public Identifier getFabricId() {
        return RESOURCE_LOADER_ID;
    }

    public static Map<Identifier, Resource> getResources() {
        return resources;
    }
    public static JsonObject getJSON() {
        return jsonObject;
    }

    @Override
    public void reload(ResourceManager manager) {
        // Clear previously loaded data (if any)

        // Find our custom assets located in textures/gui
        resources = manager.findResources("textures/gui/slot_helper", path -> path.getPath().endsWith(".json"));
        //System.out.println("Found " + resources.size() + " overrides: "+ resources.keySet());

        for (Identifier id : resources.keySet()) {
            try {
                // Get the resource
                Optional<Resource> r = manager.getResource(id);
                //System.out.println("Found resource: "+r.isPresent()+ " for "+id.toTranslationKey()+ " "+id.toUnderscoreSeparatedString());
                // Read our resource as a JSON
                String data = inputStreamToString(r.get().getInputStream());
                jsonObject = JsonHelper.deserialize(data);
                //System.out.println("Deserialized JSON: "+jsonObject);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                SlotHelperClient.warn("Caught exception: %s", ex.toString());
            }
        }

    }
    /**
     * Converts an input stream into a string, for reading JSONs and other files
     * @param stream
     * @return
     * @throws IOException
     */
    private static String inputStreamToString(InputStream stream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = stream.read(buffer)) != -1;) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }
}
