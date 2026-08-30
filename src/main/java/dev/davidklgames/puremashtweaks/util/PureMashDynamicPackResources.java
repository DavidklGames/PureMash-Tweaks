package dev.davidklgames.puremashtweaks.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.davidklgames.puremashtweaks.PureMashTweaks;
import dev.davidklgames.puremashtweaks.registry.ModSingularities;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.InclusiveRange;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 100% In-Memory Virtual Pack for Dynamic Singularity Models, Item Definitions, Tags, and Cosmic Recipe in 26.1.2.
 */
public class PureMashDynamicPackResources implements PackResources {

    private final PackLocationInfo location;
    private final PackType packType;
    private final Map<Identifier, String> clientResources = new HashMap<>();
    private final Map<Identifier, String> serverResources = new HashMap<>();

    public PureMashDynamicPackResources(PackLocationInfo location, PackType packType) {
        this.location = location;
        this.packType = packType;
        this.buildResources();
    }

    private void buildResources() {
        if (this.packType == PackType.CLIENT_RESOURCES) {
            // 1. Build Item Definitions and Model JSONs for every registered singularity
            for (var holder : ModSingularities.REGISTERED_SINGULARITIES) {
                if (holder != null) {
                    String name = holder.getId().getPath();

                    Identifier itemDefId = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "items/" + name + ".json");
                    this.clientResources.put(itemDefId, createItemDefinitionJson(name));

                    Identifier modelId = Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "models/item/" + name + ".json");
                    this.clientResources.put(modelId, createModelJson());
                }
            }
        } else if (this.packType == PackType.SERVER_DATA) {
            // 2. Build Clean Global Singularity Tags (Single standard directory: tags/item/)
            String allSingularitiesJson = createAllSingularitiesTagJson();

            this.serverResources.put(Identifier.fromNamespaceAndPath("c", "tags/item/singularities.json"), allSingularitiesJson);
            this.serverResources.put(Identifier.fromNamespaceAndPath("c", "tags/item/puremash/singularity.json"), allSingularitiesJson);

            // 3. Build Clean Individual Singularity Tags
            for (var holder : ModSingularities.REGISTERED_SINGULARITIES) {
                if (holder != null) {
                    String name = holder.getId().getPath();
                    String cleanName = name.replace("_singularity", "");
                    String individualTagJson = createIndividualTagJson(name);

                    this.serverResources.put(Identifier.fromNamespaceAndPath("c", "tags/item/singularities/" + cleanName + ".json"), individualTagJson);
                }
            }

            // 4. Fixed Cosmic Singularity Recipe
            this.serverResources.put(
                    Identifier.fromNamespaceAndPath(PureMashTweaks.MODID, "recipe/synthesis/cosmic_singularity.json"),
                    createCosmicRecipeJson()
            );
        }
    }

    @Override
    public @NonNull PackLocationInfo location() {
        return this.location;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String @NonNull ... segments) {
        if (segments.length == 1 && segments[0].equals("pack.mcmeta")) {
            int format = (this.packType == PackType.CLIENT_RESOURCES) ? SharedConstants.RESOURCE_PACK_FORMAT_MAJOR : SharedConstants.DATA_PACK_FORMAT_MAJOR;
            String json = "{\n  \"pack\": {\n    \"description\": \"PureMash Dynamic In-Memory Pack\",\n    \"pack_format\": " + format + "\n  }\n}";
            return () -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        }
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(@NonNull PackType type, @NonNull Identifier location) {
        if (type != this.packType) return null;
        Map<Identifier, String> map = (type == PackType.CLIENT_RESOURCES) ? this.clientResources : this.serverResources;
        String content = map.get(location);
        if (content != null) {
            return () -> new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        }
        return null;
    }

    @Override
    public void listResources(@NonNull PackType type, @NonNull String namespace, @NonNull String path, @NonNull ResourceOutput output) {
        if (type != this.packType) return;
        Map<Identifier, String> map = (type == PackType.CLIENT_RESOURCES) ? this.clientResources : this.serverResources;

        for (Map.Entry<Identifier, String> entry : map.entrySet()) {
            Identifier loc = entry.getKey();
            if (loc.getNamespace().equals(namespace) && loc.getPath().startsWith(path)) {
                output.accept(loc, () -> new ByteArrayInputStream(entry.getValue().getBytes(StandardCharsets.UTF_8)));
            }
        }
    }

    @Override
    public @NonNull Set<String> getNamespaces(@NonNull PackType type) {
        if (type != this.packType) return Set.of();
        Map<Identifier, String> map = (type == PackType.CLIENT_RESOURCES) ? this.clientResources : this.serverResources;
        Set<String> namespaces = new HashSet<>();
        for (Identifier loc : map.keySet()) {
            namespaces.add(loc.getNamespace());
        }
        return namespaces;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getMetadataSection(@NonNull MetadataSectionType<T> type) {
        if (type == PackMetadataSection.forPackType(this.packType) || type == PackMetadataSection.CLIENT_TYPE || type == PackMetadataSection.SERVER_TYPE || type == PackMetadataSection.FALLBACK_TYPE) {
            int majorFormat = (this.packType == PackType.CLIENT_RESOURCES) ? SharedConstants.RESOURCE_PACK_FORMAT_MAJOR : SharedConstants.DATA_PACK_FORMAT_MAJOR;
            PackFormat format = PackFormat.of(majorFormat);
            return (T) new PackMetadataSection(
                    Component.literal("PureMash Dynamic In-Memory Pack"),
                    new InclusiveRange<>(format)
            );
        }
        return null;
    }

    @Override
    public void close() {}

    private static String createItemDefinitionJson(String name) {
        JsonObject root = new JsonObject();
        JsonObject modelObj = new JsonObject();
        modelObj.addProperty("type", PureMashTweaks.MODID + ":halo");
        modelObj.addProperty("base", PureMashTweaks.MODID + ":item/" + name);
        modelObj.addProperty("halo_model", PureMashTweaks.MODID + ":item/halo");

        JsonObject haloObj = new JsonObject();
        haloObj.addProperty("color", -15658735);
        haloObj.addProperty("pulse", false);
        haloObj.addProperty("size", 2.6F);
        haloObj.addProperty("texture", PureMashTweaks.MODID + ":item/halo");

        modelObj.add("halo", haloObj);
        root.add("model", modelObj);
        root.addProperty("oversized_in_gui", true);
        return root.toString();
    }

    private static String createModelJson() {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:item/generated");

        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", PureMashTweaks.MODID + ":item/singularity");
        textures.addProperty("layer1", PureMashTweaks.MODID + ":item/singularity_mask");

        root.add("textures", textures);
        return root.toString();
    }

    private static String createAllSingularitiesTagJson() {
        JsonObject root = new JsonObject();
        root.addProperty("replace", false);

        JsonArray values = new JsonArray();
        for (var holder : ModSingularities.REGISTERED_SINGULARITIES) {
            if (holder != null && holder != ModSingularities.COSMIC_SINGULARITY) {
                values.add(holder.getId().toString());
            }
        }
        root.add("values", values);
        return root.toString();
    }

    private static String createIndividualTagJson(String fullItemPath) {
        JsonObject root = new JsonObject();
        root.addProperty("replace", false);

        JsonArray values = new JsonArray();
        values.add(PureMashTweaks.MODID + ":" + fullItemPath);

        root.add("values", values);
        return root.toString();
    }

    private static String createCosmicRecipeJson() {
        JsonObject root = new JsonObject();
        root.addProperty("type", PureMashTweaks.MODID + ":cosmic_synthesis");
        return root.toString();
    }
}