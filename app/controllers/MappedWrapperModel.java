package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.data.format.Formatters;
import play.libs.Json;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by yerlibilgin on 09/01/15.
 */
public class MappedWrapperModel {
  public final Long id;
  public final String name;
  public final String value;

  public MappedWrapperModel(Long id, String name, String value) {
    this.id = id;
    this.name = name;
    this.value = value;
  }

  public static MappedWrapperModel parse(String jsonString) throws ParseException {
    try {
      JsonNode parse = Json.parse(jsonString);
      Long id = parse.findPath("id").asLong();
      String paramName = parse.findPath("name").asText();
      String wrapper = parse.findPath("value").asText();
      return new MappedWrapperModel(id, paramName, wrapper);
    } catch (Exception ex) {
      throw new ParseException("Coudln't parse " + jsonString, 0);
    }
  }

  public static String jsonFor(Long id, String name, String value) {
    return new MappedWrapperModel(id, name, value).toJson();
  }

  private String toJson() {
    Map<String, Object> map = new HashMap<>();
    map.put("id", id);
    map.put("name", name);
    map.put("value", value);
    JsonNode result = Json.toJson(map);
    return Json.stringify(result);
  }

  static {
    Formatters.register(MappedWrapperModel.class, new Formatters.SimpleFormatter<MappedWrapperModel>() {
      @Override
      public MappedWrapperModel parse(String jsonString, Locale arg1) throws ParseException {
        return MappedWrapperModel.parse(jsonString);
      }

      @Override
      public String print(MappedWrapperModel mappedWrapperModel, Locale arg1) {
        return mappedWrapperModel.toJson();
      }
    });
  }
}
