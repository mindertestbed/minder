package controllers.common.enumeration;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebean.Update;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Secured;
import global.Util;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;

/**
 * Created by melis on 11/11/15.
 */
public class Utils extends Controller{

    @Security.Authenticated(Secured.class)
    public static Result doEditField(Class<?> editorClass, Class<?> cls, JsonNode jsonNode,User localUser) {
        long id = jsonNode.findPath("id").asInt();
        String field = jsonNode.findPath("field").asText();
        String newValue = jsonNode.findPath("newValue").asText();
        String converter = jsonNode.findPath("converter").asText();

        System.out.println("Converter3 [" + (converter == null) + "]");
        try {
            try {
                Util.checkField(editorClass, field, newValue);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
                return badRequest(ex.getMessage());
            }

            //check access.
            // first lets see if we have a field named owner
            try {
                Field fld = cls.getDeclaredField("owner");

                //we have an owner. Lets verify if we are the rightfull owner

                String queryString = "find " + cls.getSimpleName() + " where id = :id";
                Query<?> query = Ebean.createQuery(cls, queryString);
                query.setParameter("id", id);
                Object o = query.findUnique();
                User user = (User) fld.get(o);
                System.out.println("UNique " + user.email);


                if (localUser == null || !localUser.email.equals(user.email)) {
                    return badRequest("You don't have the permission to modify this resource");
                }
            } catch (NoSuchFieldException ex) {

            }
            String updStatement = "update " + cls.getSimpleName() + " set " + field + " = :value where id = :id";
            Update<?> update = Ebean.createUpdate(cls, updStatement);

            Object newValueConverted = newValue;
            if (converter != null && converter.length() != 0)
                newValueConverted = Util.convertValue(converter, newValue);

            update.set("value", newValueConverted);
            update.set("id", id);
            update.execute();
            ObjectNode node = Json.newObject();
            node.put("value", newValueConverted.toString());
            return ok(node.toString());
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            e.printStackTrace();
            return badRequest(sw.toString());
        }
    }

}
