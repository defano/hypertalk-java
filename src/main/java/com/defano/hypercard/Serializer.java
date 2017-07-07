/*
 * Serializer
 * hypertalk-java
 *
 * Created by Matt DeFano on 2/19/17 3:10 PM.
 * Copyright © 2017 Matt DeFano. All rights reserved.
 */

package com.defano.hypercard;

import com.google.gson.*;
import com.defano.hypertalk.ast.common.Value;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

public class Serializer {

    private final static Gson gson = new GsonBuilder()
            .registerTypeAdapter(Value.class, new ValueSerializer())
            .registerTypeAdapter(byte[].class, new ImageSerializer())
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .create();

    public static <T> T copy(T t) {
        return (T) deserialize(serialize(t), t.getClass());
    }

    public static String serialize (Object object) {
        return gson.toJson(object);
    }

    public static void serialize (File file, Object object) throws IOException {
        Files.write(file.toPath(), serialize(object).getBytes(StandardCharsets.UTF_8));
    }

    public static <T> T deserialize (String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static <T> T deserialize (File file, Class<T> clazz) {
        try {
            return deserialize(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8), clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to open card.");
        }
    }

    /**
     * Used to serialize/deserialize a Value object as a primitive. Replaces JSON like "visible":{"value":"true"} with
     * "visible":true.
     */
    public static class ValueSerializer implements JsonSerializer<Value>, JsonDeserializer<Value> {
        @Override
        public Value deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new Value(json.getAsString());
        }

        @Override
        public JsonElement serialize(Value src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.stringValue());
        }
    }


    private static class ImageSerializer implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Base64.getDecoder().decode(json.getAsString());
        }

        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(Base64.getEncoder().encodeToString(src));
        }
    }
}
