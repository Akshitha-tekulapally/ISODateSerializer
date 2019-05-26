import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.BasicDBObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main {

    public static void main(String[] args){
        Student student1=new Student();
        student1.setName("A");
        student1.setRollNo(1);
        student1.setJoinedOn(new Date());
        Student student2=new Student();
        student2.setName("B");
        student2.setRollNo(2);
        student2.setJoinedOn(new Date());

        List<Student>students=new ArrayList<Student>();
        students.add(student1);
        students.add(student2);

        School school=new School();
        school.setSchoolName("Convent School");
        school.setStudents(students);
        school.setEstablishedOn(new Date());
        school.setTestDouble(1.0);

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(BasicDBObject.class, new IsoDateDeserializer(BasicDBObject.class));
        module.addSerializer(Date.class,new IsoDateSerializer());
        mapper.registerModule(module);
        String serializedString=null;
        try {
            serializedString= mapper.writeValueAsString(school);
            System.out.println("serialized string");
            System.out.println(serializedString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        BasicDBObject readValue=null;
        try {
            readValue = mapper.readValue(serializedString, BasicDBObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(readValue!=null && readValue.get("establishedOn")!=null){
            System.out.println(readValue.get("establishedOn").getClass());
        }
        //insert the value into mongo and check


    }
}