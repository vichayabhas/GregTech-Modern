package com.gregtechceu.gtceu.api.recipe.content;

import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;

import com.lowdragmc.lowdraglib.LDLib;

import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

public interface IContentSerializer<T> {

    default void toNetwork(FriendlyByteBuf buf, T content) {
        buf.writeUtf(LDLib.GSON.toJson(toJson(content)));
    }

    default T fromNetwork(FriendlyByteBuf buf) {
        return fromJson(LDLib.GSON.fromJson(buf.readUtf(), JsonElement.class));
    }

    T fromJson(JsonElement json);

    JsonElement toJson(T content);

    T of(Object o);

    T defaultValue();

    @SuppressWarnings("unchecked")
    default void toNetworkContent(FriendlyByteBuf buf, Content content) {
        T inner = (T) content.getContent();
        toNetwork(buf, inner);
        buf.writeVarInt(content.chance);
        buf.writeVarInt(content.maxChance);
        buf.writeVarInt(content.tierChanceBoost);
        buf.writeBoolean(content.slotName != null);
        if (content.slotName != null) {
            buf.writeUtf(content.slotName);
        }
        buf.writeBoolean(content.uiName != null);
        if (content.uiName != null) {
            buf.writeUtf(content.uiName);
        }
    }

    default Content fromNetworkContent(FriendlyByteBuf buf) {
        T inner = fromNetwork(buf);
        int chance = buf.readVarInt();
        int maxChance = buf.readVarInt();
        int tierChanceBoost = buf.readVarInt();
        String slotName = null;
        if (buf.readBoolean()) {
            slotName = buf.readUtf();
        }
        String uiName = null;
        if (buf.readBoolean()) {
            uiName = buf.readUtf();
        }
        return new Content(inner, chance, maxChance, tierChanceBoost, slotName, uiName);
    }

    @SuppressWarnings("unchecked")
    default JsonElement toJsonContent(Content content) {
        JsonObject json = new JsonObject();
        json.add("content", toJson((T) content.getContent()));
        json.addProperty("chance", content.chance);
        json.addProperty("maxChance", content.maxChance);
        json.addProperty("tierChanceBoost", content.tierChanceBoost);
        if (content.slotName != null)
            json.addProperty("slotName", content.slotName);
        if (content.uiName != null)
            json.addProperty("uiName", content.uiName);
        return json;
    }

    default Content fromJsonContent(JsonElement json) {
        JsonObject jsonObject = json.getAsJsonObject();
        T inner = fromJson(jsonObject.get("content"));
        int chance = jsonObject.has("chance") ? jsonObject.get("chance").getAsInt() : ChanceLogic.getMaxChancedValue();
        int maxChance = jsonObject.has("maxChance") ? jsonObject.get("maxChance").getAsInt() :
                ChanceLogic.getMaxChancedValue();
        int tierChanceBoost = jsonObject.has("tierChanceBoost") ? jsonObject.get("tierChanceBoost").getAsInt() : 0;
        String slotName = jsonObject.has("slotName") ? jsonObject.get("slotName").getAsString() : null;
        String uiName = jsonObject.has("uiName") ? jsonObject.get("uiName").getAsString() : null;
        return new Content(inner, chance, maxChance, tierChanceBoost, slotName, uiName);
    }

    default Tag toNbt(T content) {
        return JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, this.toJson(content));
    }

    default Tag toNbtGeneric(Object content) {
        return toNbt((T) content);
    }

    default T fromNbt(Tag tag) {
        var json = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, tag);
        return fromJson(json);
    }
}
