package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.AdapterParam;
import models.AdapterVersion;
import models.MappedAdapter;
import play.libs.Json;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yerlibilgin on 09/01/15.
 */
public class MappedAdapterModel {
  public MappedAdapter mappedAdapter;
  public AdapterParam adapterParam;
  public AdapterVersion adapterVersion;

  public MappedAdapterModel(MappedAdapter mappedAdapter, AdapterParam adapterParam, AdapterVersion adapterVersion) {
    this.mappedAdapter = mappedAdapter;
    this.adapterParam = adapterParam;
    this.adapterVersion = adapterVersion;
  }

  public static MappedAdapterModel parse(String jsonString) throws ParseException {
    try {
      JsonNode parse = Json.parse(jsonString);
      Long mappedAdapterId = parse.findPath("mappedAdapterId").asLong();
      Long adapterParamId = parse.findPath("adapterParamId").asLong();
      Long adapterVersionId = parse.findPath("adapterVersionId").asLong();
      return new MappedAdapterModel(MappedAdapter.findById(mappedAdapterId), AdapterParam.findById(adapterParamId), AdapterVersion.findById(adapterVersionId));
    } catch (Exception ex) {
      throw new ParseException("Coudln't parse " + jsonString, 0);
    }
  }

  public String toJson() {
    Map<String, Object> map = new HashMap<>();

    long id = mappedAdapter == null ? -1 : mappedAdapter.id;
    map.put("mappedAdapterId", id);
    id = adapterParam == null ? -1 : adapterParam.id;
    map.put("adapterParamId", id);
    id = adapterVersion == null ? -1 : adapterVersion.id;
    map.put("adapterVersionId", id);
    JsonNode result = Json.toJson(map);
    return Json.stringify(result);
  }

  public String toJsonWith(AdapterVersion adapterVersion) {
    Map<String, Object> map = new HashMap<>();

    long id = mappedAdapter == null ? -1 : mappedAdapter.id;
    map.put("mappedAdapterId", id);
    id = adapterParam == null ? -1 : adapterParam.id;
    map.put("adapterParamId", id);
    id = adapterVersion == null ? -1 : adapterVersion.id;
    map.put("adapterVersionId", id);
    JsonNode result = Json.toJson(map);
    return Json.stringify(result);
  }
}
