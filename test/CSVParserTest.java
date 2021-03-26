import entity.Person;
import module.CSVParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CSVParserTest {

    private Person person;
    private List<Person> persons = new ArrayList<>(){};
    private CSVParser<Person> csvParser = new CSVParser<>();
    private CSVParser<Integer> csvParser2 = new CSVParser<>();

    @BeforeEach
    public void init(){
        person = new Person("Max", "Muster");

        persons.add(new Person("Max", "Muster"));
        persons.add(new Person("Herbert", "Janzen"));
        persons.add(new Person("Verena", "Hofstadt"));
    }

    @Test
    public void parsing(){
        String parsedValues = csvParser.parse(new ArrayList<Person>(Arrays.asList(person)));
        System.out.println(parsedValues);

        String parsedValues2 = csvParser.parse(persons);
        System.out.println(parsedValues2);
    }

    @Test
    public void parsable(){
        assertTrue(csvParser.isParsable(person));
        assertFalse(csvParser2.isParsable(5));
    }

}
