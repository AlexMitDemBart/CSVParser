package module;

import annotation.CSVEntity;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class CSVParser<T> {

    private List<String> elementsToParse = new ArrayList();

    public List<String> getElementsToParse() {
        return elementsToParse;
    }

    public void setElementsToParse(List<String> elementsToParse) {
        this.elementsToParse = elementsToParse;
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
                } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return toCsvRepresentation(elementsToParse);
    }

    //TODO: Implement headers and line breaks
    public String toCsvRepresentation(List<String> elements){
        StringBuilder sb = new StringBuilder();

        for(String element : elements){
            sb.append(element).append(",");
        }

        String csvString = sb.toString();

        if(csvString.length() > 0)
            csvString = csvString.substring(0, csvString.length() -1);

        return csvString;
    }

}
