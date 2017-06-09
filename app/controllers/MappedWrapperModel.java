package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.MappedWrapper;
import models.WrapperParam;
import models.WrapperVersion;
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
  public MappedWrapper mappedWrapper;
  public WrapperParam wrapperParam;
  public WrapperVersion wrapperVersion;

  public MappedWrapperModel(MappedWrapper mappedWrapper, WrapperParam wrapperParam, WrapperVersion wrapperVersion) {
    this.mappedWrapper = mappedWrapper;
    this.wrapperParam = wrapperParam;
    this.wrapperVersion = wrapperVersion;
  }

  public static MappedWrapperModel parse(String jsonString) throws ParseException {
    try {
      JsonNode parse = Json.parse(jsonString);
      Long mappedWrapperId = parse.findPath("mappedWrapperId").asLong();
      Long wrapperParamId = parse.findPath("wrapperParamId").asLong();
      Long wrapperVersionId = parse.findPath("wrapperVersionId").asLong();
      return new MappedWrapperModel(MappedWrapper.findById(mappedWrapperId), WrapperParam.findById(wrapperParamId), WrapperVersion.findById(wrapperVersionId));
    } catch (Exception ex) {
      throw new ParseException("Coudln't parse " + jsonString, 0);
    }
  }

  public String toJson() {
    Map<String, Object> map = new HashMap<>();

    long id = mappedWrapper == null ? -1 : mappedWrapper.id;
    map.put("mappedWrapperId", id);
    id = wrapperParam == null ? -1 : wrapperParam.id;
    map.put("wrapperParamId", id);
    id = wrapperVersion == null ? -1 : wrapperVersion.id;
    map.put("wrapperVersionId", id);
    JsonNode result = Json.toJson(map);
    return Json.stringify(result);
  }

  public String toJsonWith(WrapperVersion wrapperVersion) {
    Map<String, Object> map = new HashMap<>();

    long id = mappedWrapper == null ? -1 : mappedWrapper.id;
    map.put("mappedWrapperId", id);
    id = wrapperParam == null ? -1 : wrapperParam.id;
    map.put("wrapperParamId", id);
    id = wrapperVersion == null ? -1 : wrapperVersion.id;
    map.put("wrapperVersionId", id);
    JsonNode result = Json.toJson(map);
    return Json.stringify(result);
  }
}
