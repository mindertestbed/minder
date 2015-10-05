package rest.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.TestGroup;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import rest.model.TestGroupDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: yerlibilgin
 * @date: 23/09/15.
 */
public class GroupRestService extends Controller {
  public static Result listGroups(){
    StringBuffer xml = new StringBuffer("<response>\n\t<groups>\n");

    final List<TestGroup> groups = TestGroup.findAll();
    final List<TestGroupDTO> all = new ArrayList<>(groups.size());
    for (TestGroup testGroup : groups) {
      all.add(TestGroupDTO.dto2model(testGroup));
    }

    xml.append("\t</groups>\n</response>");

    final JsonNode jsonNode = Json.toJson(TestGroup.findAll());
    return ok(jsonNode, "utf-8");
  }
}
