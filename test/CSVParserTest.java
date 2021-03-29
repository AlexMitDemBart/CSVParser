import entity.Person;
import exception.CSVEntityAnnotationMissingException;
import module.CSVParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CSVParserTest {

    private Person person;
    private final List<Person> persons = new ArrayList<>(){};
    private final CSVParser<Person> csvParser = new CSVParser<>(Person.class);
    private final CSVParser<Integer> csvParser2 = new CSVParser<>(Integer.class);

    @BeforeEach
    public void init(){
        person = new Person("Max", "Muster",34);

        persons.add(new Person("Max\"", "Muster",40));
        persons.add(new Person("Herbert", "Janzen\" moinsen",29));
        persons.add(new Person("Verena", "Hof,stadt",30));
    }

    @Test
    public void parsing(){
        String parsedValues2 = csvParser.parse(persons);
        System.out.println(parsedValues2);
    }

    @Test
    public void parsable(){
        try{
            assertTrue(csvParser.isObjectParsable(person));
            assertFalse(csvParser2.isObjectParsable(5));
        } catch (CSVEntityAnnotationMissingException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void parseCsvToObject() throws IOException, IntrospectionException, InvocationTargetException,
                NoSuchMethodException, InstantiationException, IllegalAccessException {
        var parser = new CSVParser<Person>(Person.class);

        List<Person> expected = List.of(
                new Person("johann","maier",23),
                new Person("ste\"fanie","huber",40));

        List<Person> result = parser.parseCsvToObject("test/validCsvFile");

        assertEquals(expected, result);
    }

    @Test
    public void csvFileIsParsable() throws IOException {
        var parser = new CSVParser<Person>(Person.class);
        assertTrue(parser.isCsvFileParsable("test/validCsvFile"));
        assertFalse(parser.isCsvFileParsable("test/invalidCsvFile"));
    }
}
