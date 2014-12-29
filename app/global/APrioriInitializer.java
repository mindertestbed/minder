package global;

import com.avaje.ebean.Ebean;
import models.TestCase;
import models.TestCaseCategory;
import models.TestCaseGroup;
import models.User;
import mtdl.MinderTdl;
import mtdl.TdlCompiler;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yerlibilgin on 29/12/14.
 */
public class APrioriInitializer {
  public static void initialize() {
    User user = User.find.findUnique();

    List<Object> yamlDumpList = new ArrayList<>();

    Yaml yaml = new Yaml();
    if (user != null) {
      System.out.println(user);
      System.out.println(user.email);
      user.testCaseCategories = new ArrayList<>();
    } else {
      System.out.println("User null");
      user = (User) play.libs.Yaml.load("/initial-data.yml");
      user.testCaseCategories = new ArrayList<>();
      user.save();
    }
    yamlDumpList.add(user);

    if (TestCaseCategory.find.all().size() == 0) {
      //we don't have any test categories
      try {
        Ebean.beginTransaction();
        {
          TestCaseCategory category = createCategory(user, "WP6.1 Test Cases", "Test cases extracted from WP6.1 Test Assertions", null);
          {
            TestCaseGroup group = createGroup(category, "AS4 Tests", "All test cases related to AS4", null);
            {
              TestCase tc = createTestCase(user, group, tdl("as41.tdl"), "A test case to check the eb code party id", null);
              System.out.println("-------------------");
              tc.save();
              group.testCases.add(tc);
            }
            {
              TestCase tc = createTestCase(user, group, tdl("as42.tdl"), "A test case to check the hede hodo", null);
              System.out.println("-------------------");
              tc.save();
              group.testCases.add(tc);
            }
            group.save();
            category.testCaseGroups.add(group);
          }
          {
            TestCaseGroup group = createGroup(category, "SMP Tests", "All test cases related to SMP", null);
            {
              TestCase tc = createTestCase(user, group, tdl("SMP1.tdl"), "Check for the public key of BDX", null);
              System.out.println("-------------------");
              tc.save();
              group.testCases.add(tc);
            }
            group.save();
            category.testCaseGroups.add(group);
          }
          {
            TestCaseGroup group = createGroup(category, "BDXL Tests", "All test cases related to BDXL", null);
            {
              TestCase tc = createTestCase(user, group, tdl("BDXL1.tdl"), "Check for the BDXL id,address match", null);
              System.out.println("-------------------");
              tc.save();
              group.testCases.add(tc);
            }
            group.save();
            category.testCaseGroups.add(group);
          }
          category.save();

          user.testCaseCategories.add(category);
        }
        user.save();

        System.out.println("*************");
        Ebean.commitTransaction();
        System.out.println("###################");
      } catch (Exception ex) {
        ex.printStackTrace();
        Ebean.endTransaction();
      }
    } else {
      List<TestCaseCategory> tcc = TestCaseCategory.find.all();
      yamlDumpList.addAll(tcc);
    }

    try (PrintWriter pw = new PrintWriter(new FileWriter("conf/initial3-data.yml"))) {
      yaml.dumpAll(yamlDumpList.iterator(), pw);
      pw.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  private static String tdl(String s) {
    StringBuilder tdl = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("sampletdl/" + s)))) {
      String line;
      while ((line = br.readLine()) != null)
        tdl.append(line).append('\n');

      return tdl.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static TestCase createTestCase(User user, TestCaseGroup group, String tdl, String shortDescription, String description) {
    TestCase testCase = new TestCase();
    testCase.testCaseGroup = group;
    Class<MinderTdl> clz = TdlCompiler.compileTdl(user.email, tdl);
    testCase.testCaseName = clz.getSimpleName();
    testCase.parameters = detectParameters(tdl);
    testCase.tdl = tdl;
    testCase.testAssertion = null;
    testCase.shortDescription = shortDescription;
    testCase.description = null;
    return testCase;
  }

  private static String detectParameters(String tdl) {
    Pattern pattern = Pattern.compile("\"$([a-zA-Z0-9\\-_])\"");
    Matcher matcher = pattern.matcher(tdl);

    StringBuilder params = new StringBuilder();
    while (matcher.find()) {
      String varName = tdl.substring(matcher.start() + 1, matcher.end());
      params.append(varName).append("|");
    }

    if (params.length() > 0)
      params.deleteCharAt(params.length() - 1);

    return params.toString();
  }

  private static TestCaseCategory createCategory(User user, String name, String shortDescription, String description) {
    TestCaseCategory category = new TestCaseCategory();
    category.name = name;
    category.shortDescription = shortDescription;
    category.owner = user;
    category.description = description;
    category.testCaseGroups = new ArrayList<>();
    return category;
  }


  private static TestCaseGroup createGroup(TestCaseCategory category, String name, String shortDescription, String description) {
    TestCaseGroup tgg = new TestCaseGroup();
    tgg.testCaseCategory = category;
    tgg.name = name;
    tgg.description = description;
    tgg.shortDescription = shortDescription;
    tgg.testCases = new ArrayList<>();

    return tgg;
  }
}
