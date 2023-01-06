package se.mwthinker;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.IOException;
import java.util.Base64;

public class Base64Deserializer extends JsonDeserializer<Object> implements ContextualDeserializer {

    private Class<?> resultClass;

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
        this.resultClass = property.getType().getRawClass();
        return this;
    }

    @Override
    public Object deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getValueAsString();

        try {
            byte[] decodedValue = Base64.getMimeDecoder().decode(value);
            return new ObjectMapper().readValue(decodedValue, this.resultClass);
        } catch (IllegalArgumentException | JsonParseException e) {
            String fieldName = parser.getParsingContext().getCurrentName();
            var wrapperClass = parser.getParsingContext().getCurrentValue().getClass();

            throw new InvalidFormatException(
                    parser,
                    String.format("Value for '%s' is not a base64 encoded JSON", fieldName),
                    value,
                    wrapperClass
            );
        }
    }
}
