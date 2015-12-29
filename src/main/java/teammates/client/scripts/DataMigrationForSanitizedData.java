package teammates.client.scripts;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import javax.jdo.PersistenceManager;
import teammates.client.remoteapi.RemoteApiClient;
import teammates.common.datatransfer.StudentAttributes;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.common.util.StringHelper;
import teammates.common.util.Utils;
import teammates.logic.core.StudentsLogic;
import teammates.storage.datastore.Datastore;

public class DataMigrationForSanitizedData extends RemoteApiClient {
    private final boolean isPreview = true;
    
    public static void main(String[] args) throws IOException {
        DataMigrationForSanitizedData migrator = new DataMigrationForSanitizedData();
        migrator.doOperationRemotely();
    }

    @Override
    protected void doOperation() {
        Datastore.initialize();

        List<StudentAttributes> allStudents = getAllStudents();
        if (isPreview) {
            System.out.println("Fixing Sanitization for students...");
        }
        int numberOfAffectedStudents = 0;
        for (StudentAttributes student : allStudents) {
            if (!isPreview) {
                fixSanitizedDataForStudent(student);
            } else {
                if (previewSanitizedDataForStudent(student)) {
                    numberOfAffectedStudents++;
                }
            }
        }
        if (isPreview) {
            System.out.println("There are/is " + numberOfAffectedStudents + " student(s) affected!");
        } else {
            System.out.println("Sanitization fixing done!");
        }
    }

    private boolean previewSanitizedDataForStudent(StudentAttributes student) {
        boolean hasSanitizedData = checkStudentHasSanitizedData(student);
        if (hasSanitizedData) {
            System.out.println("Updating student having email: " + student.email);
            
            if (isSanitizedString(student.comments)) {
                System.out.println("comments: " + student.comments);
                System.out.println("new comments: " + fixSanitization(student.comments));
            }
            if (isSanitizedString(student.course)) {
                System.out.println("course: " + student.course);
                System.out.println("new course: " + fixSanitization(student.course));
            }
            if (isSanitizedString(student.email)) {
                System.out.println("email: " + student.email);
                System.out.println("new email: " + fixSanitization(student.email));
            }
            if (isSanitizedString(student.googleId)) {
                System.out.println("googleId: " + student.googleId);
                System.out.println("new googleId: " + fixSanitization(student.googleId));
            }
            if (isSanitizedString(student.lastName)) {
                System.out.println("lastName: " + student.lastName);
                System.out.println("new lastName: " + fixSanitization(student.lastName));
            }
            if (isSanitizedString(student.name)) {
                System.out.println("name: " + student.name);
                System.out.println("new name: " + fixSanitization(student.name));
            }
            if (isSanitizedString(student.section)) {
                System.out.println("section: " + student.section);
                System.out.println("new section: " + fixSanitization(student.section));
            }
            if (isSanitizedString(student.team)) {
                System.out.println("team: " + student.team);
                System.out.println("new team: " + fixSanitization(student.team));
            }
            System.out.println();
        }
        return hasSanitizedData;
    }

    private boolean isSanitizedString(String s){
        
        if ((s.indexOf('<') >= 0) || (s.indexOf('>') >= 0) || (s.indexOf('\"') >= 0) || 
            (s.indexOf('/') >= 0) || (s.indexOf('\'') >= 0)) {
            return false;
        } else if ((s.indexOf("&lt;") >= 0) || (s.indexOf("&gt;") >= 0) || (s.indexOf("&quot;") >= 0) || 
                    (s.indexOf("&#x2f;") >= 0) || (s.indexOf("&#39;") >= 0) || (s.indexOf("&amp;") >= 0)) {
            return true;
        }
        return false;
    }
    
    private String fixSanitization(String s) {
        if (isSanitizedString(s)) {
            return StringHelper.recoverFromSanitizedText(s);
        }
        return s;
    }
    
    private boolean checkStudentHasSanitizedData(StudentAttributes student) {
        return isSanitizedString(student.comments) || isSanitizedString(student.course) || 
               isSanitizedString(student.email) || isSanitizedString(student.googleId) || 
               isSanitizedString(student.lastName) || isSanitizedString(student.name) || 
               isSanitizedString(student.section) || isSanitizedString(student.team);
    }
    
    private void fixSanitizationForStudent(StudentAttributes student) {
        student.comments = fixSanitization(student.comments);
        student.course = fixSanitization(student.course);
        student.email = fixSanitization(student.email);
        student.googleId = fixSanitization(student.googleId);
        student.lastName = fixSanitization(student.lastName);
        student.name = fixSanitization(student.name);
        student.section = fixSanitization(student.section);
        student.team = fixSanitization(student.team);
    }
    
    private void fixSanitizedDataForStudent(StudentAttributes student) {
        try {
            boolean hasSanitizedData = checkStudentHasSanitizedData(student);
            if (hasSanitizedData) {
                fixSanitizationForStudent(student);
                StudentsLogic.inst().updateStudent(student.email, student);
            }
        } catch (InvalidParametersException e) {
            Utils.getLogger().log(Level.INFO, "Student " + student.email + " invalid!");
            e.printStackTrace();
        } catch (EntityDoesNotExistException e) {
            Utils.getLogger().log(Level.INFO, "Student " + student.email + " does not exist!");
            e.printStackTrace();
        }
    }

    protected PersistenceManager getPM() {
        return Datastore.getPersistenceManager();
    }

    protected List<StudentAttributes> getAllStudents() {
        StudentsLogic studentsLogic = StudentsLogic.inst();
        return studentsLogic.getAllStudents();
    }
}