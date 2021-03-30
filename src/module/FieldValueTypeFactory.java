package module;

import java.util.HashMap;
import java.lang.reflect.Field;

public class FieldValueTypeFactory {

    public static Object valueWithFieldType(Object obj, Field field){
        Class fieldType = field.getType();

        if(fieldType.equals(String.class))
            return obj.toString();

        if(fieldType.equals(Integer.class))
            return Integer.valueOf(obj.toString());

        if(fieldType.equals(Double.class))
            return Double.valueOf(obj.toString());

        if(fieldType.equals(Boolean.class))
            return Boolean.valueOf(obj.toString());

        if(fieldType.equals(Byte.class))
            return Byte.valueOf(obj.toString());

        if(fieldType.equals(Float.class))
            return Float.valueOf(obj.toString());

        return null;
    }
}
