package models;

import com.avaje.ebean.*;
import com.avaje.ebean.Query;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * @author: yerlibilgin
 * @date: 07/08/15.
 */

/**
 * Created by yerlibilgin on 22/12/14.
 */
@Entity
@Table(name = "Tdl")
public class Tdl extends Model {
    @Id
    public long id;

    @ManyToOne
    @Column(nullable = false)
    public TestCase testCase;

    @Column(nullable = false, length = ModelConstants.MAX_TDL_LENGTH, columnDefinition = "TEXT")
    public String tdl;

    @Column(nullable = false)
    public String version;

    @OneToMany
    public List<AbstractJob> jobs;

    public Date creationDate;


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public List<WrapperParam> parameters;

    private static final Finder<Long, Tdl> find = new Finder<>(Tdl.class);

    public static Tdl getLatestTdl(TestCase testCase) {
        final List<Tdl> list = findByTestCase(testCase);

        if (list.size() == 0)
            return null;

        return list.get(0);
    }

    public static List<Tdl> findByTestCase(TestCase testCase) {
        return find.where().eq("testCase", testCase).orderBy().desc("creationDate").findList();
    }

    public static Tdl findById(Long tdlId) {
        return find.byId(tdlId);
    }

    public static List<Tdl> getAllUnparametricTdls() {
        RawSql rawSql = RawSqlBuilder.unparsed("SELECT t.* FROM tdl t LEFT JOIN wrapperparam w ON t.id = w.tdl_id WHERE w.tdl_id IS NULL")
                .columnMapping("id", "id")
                .columnMapping("test_case_id","testCase.id")
                .columnMapping("tdl","tdl")
                .columnMapping("version","version")
                .columnMapping("creation_date","creationDate")
                .create();

        Query<Tdl> query = Ebean.find(Tdl.class);
        query.setRawSql(rawSql);
        List<Tdl> tdlList = query.findList();

        return tdlList;
    }
}
