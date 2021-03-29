package module;

import annotation.CSVEntity;
import entity.Person;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.CharacterCodingException;
import java.util.*;

public class CSVParser<T> {

    private final List<String> elementsToParse = new ArrayList();
    private final HashSet<String> headers = new HashSet<>();
    private Class<T> clazz;

    public CSVParser(Class<T> clazz) {
        this.clazz = clazz;
    }

    public List<String> getElementsToParse() {
        return elementsToParse;
    }

    public boolean objectIsParsable(T object){
        return object.getClass().isAnnotationPresent(CSVEntity.class);
    }

    public boolean csvFileIsParsable(String filePath) {
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

    public static String formatString(String word){
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        if(word.contains("\"") || word.contains(",")){
            for (Character sign : word.toCharArray()){
                if(sign.equals('"') || sign.equals(',')){
                    if(sign.equals(',')){
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

    public List<T> readCsvFile(String file) throws IOException, IntrospectionException,
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
                writeMethod.invoke(object, map.get(field.getName()));
            }
            result.add(object);
        }
        reader.close();

        return result;
    }
}
