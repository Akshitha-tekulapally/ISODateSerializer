import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.util.RawValue;
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
                    node.add(deserializeObject(p, ctxt));
                    break;
                case JsonTokenId.ID_EMBEDDED_OBJECT:
                    node.add( _fromEmbedded(p, ctxt));
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
                    deserializeAny(p, ctxt);
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
                    value = deserializeObject(p, ctxt);
                    break;
                case JsonTokenId.ID_EMBEDDED_OBJECT:
                    value= _fromEmbedded(p, ctxt);
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
                    value = deserializeAny(p, ctxt);
            }
            basicDBObject.append(key,value);
        }
        return basicDBObject;
    }
    protected final Object deserializeAny(JsonParser p, DeserializationContext ctxt
                                           ) throws IOException
    {
        switch (p.getCurrentTokenId()) {
            case JsonTokenId.ID_END_OBJECT:
                return null;
            case JsonTokenId.ID_FIELD_NAME:
                /*return deserializeObjectAtName(p, ctxt, nodeFactory);*/
            case JsonTokenId.ID_EMBEDDED_OBJECT:
                return _fromEmbedded(p, ctxt);
            case JsonTokenId.ID_STRING:
                return getValue(p.getText());
            case JsonTokenId.ID_NUMBER_INT:
                return _fromInt(p, ctxt);
            case JsonTokenId.ID_NUMBER_FLOAT:
                return _fromFloat(p, ctxt);
            case JsonTokenId.ID_TRUE:
                return true;
            case JsonTokenId.ID_FALSE:
                return false;
            case JsonTokenId.ID_NULL:
                return null;
            default:
        }
        return  ctxt.handleUnexpectedToken(handledType(), p);
    }

    protected final Object _fromInt(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        JsonParser.NumberType nt;
        int feats = ctxt.getDeserializationFeatures();
        if ((feats & F_MASK_INT_COERCIONS) != 0) {
            if (DeserializationFeature.USE_BIG_INTEGER_FOR_INTS.enabledIn(feats)) {
                nt = JsonParser.NumberType.BIG_INTEGER;
            } else if (DeserializationFeature.USE_LONG_FOR_INTS.enabledIn(feats)) {
                nt = JsonParser.NumberType.LONG;
            } else {
                nt = p.getNumberType();
            }
        } else {
            nt = p.getNumberType();
        }
        if (nt == JsonParser.NumberType.INT) {
            return p.getIntValue();
        }
        if (nt == JsonParser.NumberType.LONG) {
            return p.getLongValue();
        }
        return p.getBigIntegerValue();
    }

    protected final Object _fromFloat(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        JsonParser.NumberType nt = p.getNumberType();
        if (nt == JsonParser.NumberType.BIG_DECIMAL) {
            return p.getDecimalValue();
        }
        if (ctxt.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)) {
            if (p.isNaN()) {
                return p.getDoubleValue();
            }
            return p.getDecimalValue();
        }
        if (nt == JsonParser.NumberType.FLOAT) {
            return p.getFloatValue();
        }
        return p.getDoubleValue();
    }

    protected final Object _fromEmbedded(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        Object ob = p.getEmbeddedObject();
        if (ob == null) {
            return null;
        }
        Class<?> type = ob.getClass();
        if (type == byte[].class) {
            return  ob;
        }
        if (ob instanceof RawValue) {
            return  ob;
        }
        if (ob instanceof JsonNode) {

            return  ob;
        }
        return ob;
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