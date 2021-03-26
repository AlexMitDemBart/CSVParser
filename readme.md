# CSV Parser

## Description
CSV parser which works with generic types and annotations.<br/>
No use of third party libraries.


## How it works
the parser checks for the annotation *CSVEntity* in classes to check if the class is parsable.<br/>
If you parse a class the parser will recognize fields with the annotation *CSVField* <br/>
and returns a csv formatted string.

To mark a class as parsable for the CSV Parser add the annotation like below:
```java
**@CSVEntity**
public class Person {
```

To mark fields of a class for the CSV Parser do following:
```java
    **@CSVField**
    private String name;
    **@CSVField**
    private String surname;
```

## Code Sample

<span style="color: green;">parse method uses PropertyDescriptor to invoke getters</span>
```java
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
``` 

