package module;

import annotation.CSVEntity;
import annotation.CSVField;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class CSVParser<T> {

    private static final Character COMMA_SEPARATOR = ',';
    private static final String COLUMN_DELIMITER = "\"";

    private final List<String> elementsToParse = new ArrayList();
    private final HashSet<String> headers = new HashSet<>();
    private final Class<T> clazz;

    public CSVParser(Class<T> clazz) {
        this.clazz = clazz;
    }

    public List<String> getElementsToParse() {
        return elementsToParse;
    }

    public boolean isObjectParsable(T object){
        //TODO CSVEntity exception with class information
        return object.getClass().isAnnotationPresent(CSVEntity.class);
    }

    //TODO test for "," in elements
    public boolean isCsvFileParsable(String filePath) {
        var splitLines = new ArrayList<List<String>>();
        try{
            var inputStream = new FileInputStream(filePath);
            var reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            String line;
            int elementPerLine = 0;
            while((line = reader.readLine()) != null){
                splitLines.add(Arrays.asList(line.split(",")));
            }
            elementPerLine = splitLines.get(0).size();

            for(List<String> elements : splitLines){
                if(!(elementPerLine == elements.size()))
                    return false;
            }
        }catch(UnsupportedEncodingException e){
            return false;
        }catch(IOException e){
            e.printStackTrace();
            e.getMessage();
        }

        return true;
    }

    public String parse(List<T> objects){
        for(T object : objects){
            Field[] fields = object.getClass().getDeclaredFields();

            for(Field field : fields){
                try {
                    if(field.isAnnotationPresent(CSVField.class)){
                        PropertyDescriptor pd = new PropertyDescriptor(field.getName(), object.getClass());
                        String fieldValue = pd.getReadMethod().invoke(object).toString();
                        elementsToParse.add(fieldValue);
                        headers.add(pd.getName());
                    }
                } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
                    //TODO Custom exception with class information
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
                    sb.deleteCharAt(sb.length()-1);
                    sb.append(System.getProperty("line.separator"));
                }

                sb.append(formatString(element)).append(",");

                counter++;
            }

            sb.deleteCharAt(sb.length()-1);
            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }

    //TODO refactor method
    public static String formatString(String word){
        StringBuilder sb = new StringBuilder();
        sb.append(COLUMN_DELIMITER);
        boolean wordContainsColumnDelimiterOrCommaSeparator =
                word.contains(COLUMN_DELIMITER) || word.contains(COMMA_SEPARATOR.toString());

        if(wordContainsColumnDelimiterOrCommaSeparator){
            for (Character sign : word.toCharArray()){
                if(sign.equals('"') || sign.equals(COMMA_SEPARATOR)){
                    if(sign.equals(COMMA_SEPARATOR)){
                        sb.append(",\"\"");
                    }else{
                        sb.append("\"\"");
                    }
                }else{
                    sb.append(sign);
                }
            }
        }else{
            sb.append(word);
        }
        sb.append("\"");
        return sb.toString();
    }

    public List<T> parseCsvToObject(String file) throws IOException, IntrospectionException,
                NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        var result = new ArrayList<T>();
        var reader = new BufferedReader(new FileReader(file));
        var map = new HashMap<String, String>();

        String header = reader.readLine();
        String[] keys = header.split(",");
        String line = null;

        while ((line = reader.readLine()) != null) {
            String[] values = line.split(",");
            for (int i = 0; i < keys.length; i++) {
                map.put(keys[i], values[i]);
            }

            T object = null;
            for(Field field : clazz.getDeclaredFields()){
                var propertyDescriptor = new PropertyDescriptor(field.getName(), clazz);
                Method writeMethod = propertyDescriptor.getWriteMethod();
                object = object == null ? clazz.getDeclaredConstructor().newInstance() : object;
                //TODO map.get returns only Strings
                writeMethod.invoke(object, map.get(field.getName()));
            }
            result.add(object);
        }
        reader.close();

        return result;
    }
}
