import java.util.Date;
import java.util.List;

public class School {
    private String schoolName;
    private List<Student>students;
    private Date establishedOn;

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public Date getEstablishedOn() {
        return establishedOn;
    }

    public void setEstablishedOn(Date establishedOn) {
        this.establishedOn = establishedOn;
    }
}
