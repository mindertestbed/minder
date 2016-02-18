package editormodels;

import controllers.MappedWrapperModel;
import models.Visibility;
import play.data.validation.Constraints;

import java.util.List;

/**
 * Created by edonafasllija on 17/02/16.
 */
public class JobTemplateEditorModel {
    public Long id;

    @Constraints.Required
    public String name;

    public Long tdlID;

    public List<MappedWrapperModel> wrapperMappingList;

    public String mtdlParameters;

    public Visibility visibility;
}
