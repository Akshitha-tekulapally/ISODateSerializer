import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.mongodb.BasicDBObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IsoDateDeserializer extends StdDeserializer<BasicDBObject> {

    public IsoDateDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public BasicDBObject deserialize(JsonParser parser, DeserializationContext deserializer) throws IOException {
        return deserializeObject(parser,deserializer);
    }
    private  List<Object> deserializeArray(JsonParser p, DeserializationContext ctxt
    ) throws IOException
    {
        List<Object> node = new ArrayList();
        while (true) {
            JsonToken t = p.nextToken();
            switch (t.id()) {
                case JsonTokenId.ID_START_OBJECT:
                case JsonTokenId.ID_EMBEDDED_OBJECT:
                    node.add(deserializeObject(p, ctxt));
                    break;
                case JsonTokenId.ID_START_ARRAY:
                    node.add(deserializeArray(p, ctxt));
                    break;
                case JsonTokenId.ID_END_ARRAY:
                    return node;
                case JsonTokenId.ID_STRING:
                    node.add(getValue(p.getText()));
                    break;
                case JsonTokenId.ID_NUMBER_INT:
                    node.add(p.getIntValue());
                    break;
                case JsonTokenId.ID_TRUE:
                    node.add(p.getBooleanValue());
                    break;
                case JsonTokenId.ID_FALSE:
                    node.add(p.getBooleanValue());
                    break;
                case JsonTokenId.ID_NULL:
                    node.add(null);
                    break;
                default:
                    break;
            }
        }
    }
    private BasicDBObject deserializeObject(JsonParser p, DeserializationContext ctxt
    ) throws IOException
    {
        BasicDBObject basicDBObject=new BasicDBObject();
        String key = p.nextFieldName();
        for (; key != null; key = p.nextFieldName()) {
            Object value=null;
            JsonToken t = p.nextToken();
            if (t == null) {
                t = JsonToken.NOT_AVAILABLE;
            }
            switch (t.id()) {
                case JsonTokenId.ID_START_OBJECT:
                case JsonTokenId.ID_EMBEDDED_OBJECT:
                    value = deserializeObject(p, ctxt);
                    break;
                case JsonTokenId.ID_START_ARRAY:
                    value = deserializeArray(p, ctxt);
                    break;

                case JsonTokenId.ID_STRING:
                    value =getValue(p.getText());
                    break;
                case JsonTokenId.ID_NUMBER_INT:
                    value = p.getIntValue();
                    break;
                case JsonTokenId.ID_TRUE:
                    value = true;
                    break;
                case JsonTokenId.ID_FALSE:
                    value = false;
                    break;
                case JsonTokenId.ID_NULL:
                    value = null;
                    break;
                default:

            }
            basicDBObject.append(key,value);
        }
        return basicDBObject;
    }
    public Object getValue(String value){
        String stringPattern="^ISODate\\(([0-9]*?)\\)$";
        Pattern pattern=Pattern.compile(stringPattern);
        Matcher matcher=pattern.matcher(value);
        if(matcher.find()){
            String longValue=matcher.group(1);
            return new Date(Long.parseLong(longValue));
        }
        return value;
    }
}