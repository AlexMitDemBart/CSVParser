package module;

import annotation.CSVEntity;
import annotation.CSVField;
import exception.CSVEntityAnnotationMissingException;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CSVParser<T> {

    private static final Character COMMA_SEPARATOR = ',';
    private static final String COLUMN_DELIMITER = "\"";

    private final List<String> fieldValuesToParse = new ArrayList();
    private final HashSet<String> headersForCsv = new HashSet<>();
    private final Class<T> clazz;

    public CSVParser(Class<T> clazz) {
        this.clazz = clazz;
    }

    public List<String> getFieldValuesToParse() {
        return fieldValuesToParse;
    }

    public boolean isObjectParsable(T object) throws CSVEntityAnnotationMissingException {
       if(object.getClass().isAnnotationPresent(CSVEntity.class)){return true;}
       else{
           String errorMessage = "Class: " + object.getClass() + " missing the " + CSVEntity.class;
           throw new CSVEntityAnnotationMissingException(errorMessage);
       }
    }

    public boolean isCsvFileParsable(String filePath) {
        var splitLines = new ArrayList<List<String>>();

        try{
            var inputStream = new FileInputStream(filePath);
            var streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            var reader = new BufferedReader(streamReader);

            String line;
            int numberOfElementsPerLine = 0;
            while((line = reader.readLine()) != null){
                splitLines.add(Arrays.asList(line.split(COMMA_SEPARATOR.toString())));
            }

            numberOfElementsPerLine = splitLines.get(0).size();

            for(List<String> lineElements : splitLines){
                if(!(numberOfElementsPerLine == lineElements.size()))
                    return false;
            }
        }catch(IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    public String parse(List<T> objects){
        for(T object : objects){
            Field[] fields = object.getClass().getDeclaredFields();

            for(Field field : fields){
                try {
                    if(field.isAnnotationPresent(CSVField.class)){
                        var pd = new PropertyDescriptor(field.getName(), object.getClass());
                        String fieldValue = pd.getReadMethod().invoke(object).toString();
                        fieldValuesToParse.add(fieldValue);
                        headersForCsv.add(pd.getName());
                    }
                } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return toCsvRepresentation(fieldValuesToParse);
    }

    public String toCsvRepresentation(List<String> elements) {
        StringBuilder sb = new StringBuilder();
        int counter = 0;

        for (String header : headersForCsv) {
            sb.append(header).append(COMMA_SEPARATOR);
        }

        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length()-1);
            sb.append(System.getProperty("line.separator"));

            for (String element : elements) {
                boolean endOfLineReached = counter > 0 && counter % headersForCsv.size() == 0;
                if(endOfLineReached){
                    sb.deleteCharAt(sb.length()-1);
                    sb.append(System.getProperty("line.separator"));
                }

                sb.append(formatString(element)).append(COMMA_SEPARATOR);

                counter++;
            }

            sb.deleteCharAt(sb.length()-1);
            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }

    public static String formatString(String word){
        StringBuilder sb = new StringBuilder();
        sb.append(COLUMN_DELIMITER);
        boolean wordContainsColumnDelimiterOrCommaSeparator =
                word.contains(COLUMN_DELIMITER) || word.contains(COMMA_SEPARATOR.toString());

        if(wordContainsColumnDelimiterOrCommaSeparator){
            for (Character sign : word.toCharArray()){
                if(sign.equals(COLUMN_DELIMITER.charAt(0))){
                    sb.append(",\"\"");
                }
                else if(sign.equals(COMMA_SEPARATOR)){
                    sb.append("\"\"");
                }
                else{
                    sb.append(sign);
                }
            }
        }
        else{
            sb.append(word);
        }

        sb.append("\"");
        return sb.toString();
    }

    public List<T> parseCsvToObject(String file) throws IOException, IntrospectionException,
                NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        var result = new ArrayList<T>();
        var reader = new BufferedReader(new FileReader(file));
        var map = new HashMap<Object, Object>();

        String header = reader.readLine();
        Object[] keys = header.split(COMMA_SEPARATOR.toString());
        String line = null;

        while ((line = reader.readLine()) != null) {
            Object[] values = (Object[])line.split(COMMA_SEPARATOR.toString());
            for (int i = 0; i < keys.length; i++) {
                map.put(keys[i], values[i]);
            }

            T object = null;
            for(Field field : clazz.getDeclaredFields()){
                var propertyDescriptor = new PropertyDescriptor(field.getName(), clazz);
                Method writeMethod = propertyDescriptor.getWriteMethod();

                if(object == null)
                    object = clazz.getDeclaredConstructor().newInstance();

                Object oldTypedFieldValue = map.get(field.getName());
                Object newTypedFieldValue = FieldValueTypeFactory.valueWithFieldType(oldTypedFieldValue, field);
                writeMethod.invoke(object, newTypedFieldValue);
            }
            result.add(object);
        }
        reader.close();

        return result;
    }


}
