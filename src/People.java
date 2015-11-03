import com.sun.tools.internal.ws.processor.model.Model;
import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by zach on 10/19/15.
 */
public class People {
    public static void main(String[] args) {
        ArrayList<Person> people = new ArrayList();

        String fileContent = readFile("people.csv");
        String[] lines = fileContent.split("\n");

        for (String line : lines) {
            if (line == lines[0])
                continue;

            String[] columns = line.split(",");
            Person person = new Person(Integer.valueOf(columns[0]), columns[1], columns[2], columns[3], columns[4], columns[5]);
            people.add(person);

            // c0 - ID  c1 - first name  c2 - last name  c3 - email  c4 - country  c5 - ip address
        }

        Spark.get(
                "/",
                ((request, response) -> {
                    String count = request.queryParams("count");
                    String previous = request.queryParams("previous");
                    String next = request.queryParams("next");
                    int countNum;
                    boolean previousShow = false;
                    boolean nextShow = false;

                    // Setting up current count
                    if(count == null) {
                        countNum = 0;
                    } else {
                        countNum = Integer.valueOf(count);
                    }

                    int nextNum = countNum + 20;  // next page button setup / logic
                    int previousNum = countNum - 20;

                    if (countNum < people.size() - 20) {
                        nextShow = true;
                    }

                    // Setting up Previous button setup / logic
                    if (countNum > 0) {
                        previousShow = true;
                    }

                    // use sublist to page through the ArrayList
                    ArrayList<Person> peopleList = new ArrayList(people.subList(countNum, nextNum));

                    // Setting up Search by first letter of first name

                    // boolean previousShow = countNum >= 0;
                    // boolean nextShow = peopleList.size() >= countNum;


                    HashMap map = new HashMap();
                    map.put("list", peopleList);
                    map.put("count", countNum);
                    map.put("previous", previousNum);
                    map.put("next", nextNum);
                    map.put("previousShow", previousShow);
                    map.put("nextShow", nextShow);

                    return new ModelAndView(map, "people.html");
                }),

                new MustacheTemplateEngine()
        );

        Spark.get (
                "/person",
                ((request, response) -> {

                    String id = request.queryParams("id");

                    int idNum = Integer.valueOf(id);
                    Person person = people.get(idNum - 1);

                    HashMap m = new HashMap();
                    m.put("person", person);

                    return new ModelAndView(m, "person.html");

                }),

                new MustacheTemplateEngine()
        );

        Spark.get(
                "/results",
                ((request, response) -> {
                    String search = request.queryParams("search").toLowerCase();

                    ArrayList<Person> results = new ArrayList();

                    for (Person p : people) {
                        if (p.firstName.toLowerCase().contains(search) || p.lastName.toLowerCase().contains(search)) {
                            results.add(p);
                        }
                    }

                    HashMap m = new HashMap();
                    m.put("results", results);
                    return new ModelAndView(m, "results.html");
                }),

                new MustacheTemplateEngine()

        );


    }

    static String readFile(String fileName) {
        File f = new File(fileName);
        try {
            FileReader fr = new FileReader(f);
            int fileSize = (int) f.length();
            char[] fileContent = new char[fileSize];
            fr.read(fileContent);
            return new String(fileContent);
        } catch (Exception e) {
            return null;
        }
    }
}
