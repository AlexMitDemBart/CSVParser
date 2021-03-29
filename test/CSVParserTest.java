import entity.Person;
import module.CSVParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CSVParserTest {

    private Person person;
    private List<Person> persons = new ArrayList<>(){};
    private CSVParser<Person> csvParser = new CSVParser<>(Person.class);
    private CSVParser<Integer> csvParser2 = new CSVParser<>(Integer.class);

    @BeforeEach
    public void init(){
        person = new Person("Max", "Muster");

        persons.add(new Person("Max\"", "Muster"));
        persons.add(new Person("Herbert", "Janzen\" moinsen"));
        persons.add(new Person("Verena", "Hof,stadt"));
    }

    @Test
    public void parsing(){
        String parsedValues2 = csvParser.parse(persons);
        System.out.println(parsedValues2);
    }

    @Test
    public void parsable(){
        assertTrue(csvParser.isParsable(person));
        assertFalse(csvParser2.isParsable(5));
    }

    @Test
    public void readCsv() throws IOException, IntrospectionException, InvocationTargetException,
                NoSuchMethodException, InstantiationException, IllegalAccessException {
        CSVParser parser = new CSVParser(Person.class);

        List<Person> expected = List.of(
                new Person("johann","maier"),
                new Person("stefanie","huber"));

        List<Person> result = parser.readCsvFile("test/csvTest.txt");

        assertEquals(expected, result);
    }
}
