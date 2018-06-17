package utils;

import com.avaje.ebean.Ebean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import minderengine.AdapterIdentifier;
import minderengine.TestEngine;
import models.Adapter;
import models.AdapterParam;
import models.AdapterVersion;
import models.EndPointIdentifier;
import models.ParamSignature;
import models.Tdl;
import mtdl.AdapterFunction;
import mtdl.EndpointParser;
import mtdl.EndpointRivet;
import mtdl.MinderTdl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yerlibilgin
 */
public class TdlUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(TdlUtils.class);

  /**
   * Processes the rivets of a tdl and discovers the endpoint
   * rivets and parameteric rivets and saves the data
   */
  public static void detectAndSaveParameters(Tdl newTdl) {
    LOGGER.info("Detect parameters for the newTdl " + newTdl.testCase.name + "." + newTdl.version);
    LinkedHashMap<String, Set<AdapterFunction>> descriptionMap = TestEngine.describeTdl(newTdl);

    Set<String> identifierSet = EndpointParser.detectEndPointIdentifiers(newTdl.tdl)._1;

    if (!identifierSet.isEmpty()) {
      newTdl.httpEndpointIdentifiers = new ArrayList<>();
      for (String str : identifierSet) {
        final EndPointIdentifier identifier = new EndPointIdentifier();

        String[] sptl = str.split(":");
        identifier.method = sptl[0];
        identifier.identifier = sptl[1];
        newTdl.httpEndpointIdentifiers.add(identifier);
        identifier.tdl = newTdl;
        identifier.save();

      }
    }

    List<AdapterParam> adapterParamList = new ArrayList<>();
    int currentIndex = -1;
    for (Map.Entry<String, Set<AdapterFunction>> entry : descriptionMap.entrySet()) {
      currentIndex++;
      //make sure that we are looping on variables.
      final String key = entry.getKey();

      LOGGER.debug("Processing adapter identifier " + key);
      if (!key.startsWith("$")) {
        //make sure that the entry really exists.

        AdapterIdentifier adapterIdentifier = AdapterIdentifier.parse(key);

        if (adapterIdentifier.getName().equals(MinderTdl.NULL_ADAPTER_NAME())) {
          //skip null adapter
          continue;
        }

        //if this is an end point rivet then its signature must be handled differently
        if (adapterIdentifier.getName().equals(EndpointRivet.ADAPTER_NAME())) {
          if (currentIndex == 0) {
            LOGGER.debug("This is an endpoint adapter and comes first");
            //this is the first rivet, hence this tdl is an HTTP end point listener
            //and it can be triggered asynchronously
            newTdl.isHttpEndpoint = true;
          } else {
            LOGGER.debug("This is an endpoint adapter but it is not the first. The test will suspend during execution");
          }
        } else {
          LOGGER.debug("Found an absoulute named adapter");
          Adapter adapter = Adapter.findByName(adapterIdentifier.getName());
          if (adapter == null) {
            //oops
            throw new IllegalArgumentException("No adapter with name " + adapterIdentifier.getName());
          }
          //check if a version is used in the name
          if (adapterIdentifier.getVersion() != null) {
            //we have a version, check if the version exists
            AdapterVersion adapterVersion = AdapterVersion.findAdapterAndVersion(adapter, adapterIdentifier.getVersion());
            if (adapterVersion == null) {
              throw new IllegalArgumentException(
                  "No adapter version " + adapterIdentifier.getVersion() + " for " + adapterIdentifier.getName());
            }
          }
        }
        //this is not a parametric adapter. Skip the rest
        continue;
      }

      LOGGER.debug("This is a paramteric adapter. Process its signature");
      //this is a parametric adapter (is starts with a $ sign)
      //so we need to parse its parameters and save them
      //in the db so that it can later be matched to one of the
      //existing adapters
      AdapterParam adapterParam;
      adapterParam = new AdapterParam();
      adapterParam.name = key;
      adapterParam.signatures = new ArrayList<>();
      adapterParam.tdl = newTdl;
      adapterParamList.add(adapterParam);

      for (AdapterFunction signalSlot : entry.getValue()) {
        ParamSignature ps = new ParamSignature();
        ps.signature = signalSlot.signature().replaceAll("\\s", "");
        ps.adapterParam = adapterParam;
        adapterParam.signatures.add(ps);
        LOGGER.debug(key + "." + ps.signature);
      }

      newTdl.parameters.add(adapterParam);
    }

    newTdl.save();

    LOGGER.debug("Save parameters");
    for (AdapterParam adapterParam : adapterParamList) {
      adapterParam.save();
      for (ParamSignature signature : adapterParam.signatures) {
        signature.save();
      }
    }
    LOGGER.debug("Detect parameters done");
  }
}
