package module;

import annotation.CSVEntity;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CSVParser<T> {

    private final List<String> elementsToParse = new ArrayList();
    private final HashSet<String> headers = new HashSet<>();

    public List<String> getElementsToParse() {
        return elementsToParse;
    }

    public boolean isParsable(T object){
        return object.getClass().isAnnotationPresent(CSVEntity.class);
    }

    public String parse(List<T> objects){
        for(T object : objects){
            Field[] fields = object.getClass().getDeclaredFields();

            for(Field field : fields){
                try {
                    PropertyDescriptor pd = new PropertyDescriptor(field.getName(), object.getClass());
                    elementsToParse.add((String)pd.getReadMethod().invoke(object));
                    headers.add(pd.getName());
                } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return toCsvRepresentation(elementsToParse);
    }

    public String toCsvRepresentation(List<String> elements) {
        StringBuilder sb = new StringBuilder();
        int counter = 0;

        for (String header : headers) {
            sb.append(header).append(",");
        }

        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length()-1);
            sb.append(System.getProperty("line.separator"));

            for (String element : elements) {
                if(counter > 0 && counter % headers.size() == 0){
                    sb.append(System.getProperty("line.separator"));
                }
                sb.append(element).append(",");
                counter++;
            }

            sb.deleteCharAt(sb.length()-1);
            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }
}
