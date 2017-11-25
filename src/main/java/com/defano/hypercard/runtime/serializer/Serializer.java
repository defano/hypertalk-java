package com.defano.hypercard.runtime.serializer;

import com.google.gson.*;
import com.defano.hypertalk.ast.common.Value;

import javax.imageio.ImageIO;
import javax.swing.text.StyledDocument;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * A utility for serializing/de-serializing HyperTalk Java objects.
 */
public class Serializer {

    private final static Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new PostConstructAdapterFactory())
            .registerTypeAdapter(Value.class, new ValueSerializer())
            .registerTypeAdapter(byte[].class, new ImageSerializer())
            .registerTypeAdapter(StyledDocument.class, new com.defano.hypercard.runtime.serializer.DocumentSerializer())
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .create();

    @SuppressWarnings("unchecked")
    public static <T> T copy(T t) {
        return (T) deserialize(serialize(t), t.getClass());
    }

    /**
     * Serializes the contents of an Object to a JSON-formatted string.
     *
     * @param object The object graph to serialize.
     * @return The serialized, JSON-formatted string.
     */
    public static String serialize(Object object) {
        return gson.toJson(object);
    }

    /**
     * Serializes the contents of an Object to a file.
     *
     * @param file The file that should be written with the JSON-formatted serialization data.
     * @param object The object graph to be serialized. Object graph cannot contain cycles!
     * @throws IOException Thrown if an error occurs serializing the data or writing it to the file.
     */
    public static void serialize (File file, Object object) throws IOException {
        Files.write(file.toPath(), serialize(object).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Attempts to deserialize a JSON-formatted string into an Object of the requested type.
     *
     * @param json A JSON string to deserialize; typically generated using the {@link #serialize(Object)} method.
     * @param clazz The class of object to deserialize into.
     * @param <T> A type representing the deserialized object class.
     * @return A deserialized representation of the given file.
     */
    private static <T> T deserialize(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    /**
     * Attempts to deserialize the contents of a file into an Object of the requested type.
     *
     * @param file The file to deserialize; should a plain-text, JSON-formatted file generated using the
     *             {@link #serialize(File, Object)} method.
     * @param clazz The class of object to deserialize into.
     * @param <T> A type representing the deserialized object class.
     * @return A deserialized representation of the given file.
     */
    public static <T> T deserialize (File file, Class<T> clazz) {
        try {
            return deserialize(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8), clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read the file. The file may be corrupted.", e);
        }
    }

    /**
     * Serializes a BufferedImage into an array of bytes representing the PNG-formatted image data.
     *
     * @param image The image to serialize
     * @return The serialized image data
     */
    public static byte[] serializeImage(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            baos.flush();
            byte[] serialized = baos.toByteArray();
            baos.close();
            return serialized;
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while trying to save the image.", e);
        }
    }

    /**
     * Given a byte array of serialized PNG image data (created using the {@link #serializeImage(BufferedImage)} method,
     * this method returns a BufferedImage. If the array is empty or null, returns a single-pixel, transparent image.
     *
     * @param imageData The PNG image data to deserialize into a BufferedImage
     * @return The BufferedImage
     */
    public static BufferedImage deserializeImage(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            return new BufferedImage(1,1, BufferedImage.TYPE_INT_ARGB);
        } else {
            try {
                ByteArrayInputStream stream = new ByteArrayInputStream(imageData);
                return ImageIO.read(stream);
            } catch (IOException e) {
                throw new RuntimeException("An error occurred reading the image. This stack may be corrupted.", e);
            }
        }
    }

}
