package minderengine.dependencyutils;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by melis on 25/05/15.
 */
public class DependencyService {
    /**
     * Singleton Pattern
     */
    private static DependencyService instance;
    public synchronized static DependencyService getInstance() {
        if (null == instance) {
            instance = new DependencyService();
        }
        return instance;

    }

    private DependencyService() {

    }

    private RepositorySystem repositorySystem;
    private DefaultRepositorySystemSession session;
    private List<RemoteRepository> repositoryList;
    private List<String> allResolvedDependencies ;

    public void createRepositorySystem(String localRepoDirectory){
        /**
         * Creates a repository system that can maintain many repositories
         */
        repositorySystem = Booter.newRepositorySystem();

        /**
         * Session properties (such as where to download etc.)
         */
        session = Booter.newRepositorySystemSession( repositorySystem, localRepoDirectory );
        session.setConfigProperty( ConflictResolver.CONFIG_PROP_VERBOSE, true );
        session.setConfigProperty( DependencyManagerUtils.CONFIG_PROP_VERBOSE, true );

        repositoryList = new ArrayList<RemoteRepository>();
        allResolvedDependencies = new ArrayList<String>();
    }


    public void addMavenCentralRepository(){
        addRepository("central", "default", "http://central.maven.org/maven2/");
    }

    public void addEidRepository(){
        addRepository("Eid public repository", "default", "http://eidrepo:8081/nexus/content/groups/public/");
    }

    public void addRepository(String id, String type, String url){
        repositoryList.add(Booter.addRepository(id, type, url));
    }

    public String getClassPathString(String dependencyString){
        allResolvedDependencies = new ArrayList<String>();

        String[] dependencies = dependencyString.split("\\n");
        for (int i=0 ; i < dependencies.length ; i++){
            String currentItem = dependencies[i];
            downloadArtifactWithAllDependencies(currentItem);
        }

        return generateClassPathStringForArtifact();
    }

    public void downloadArtifactWithAllDependencies(String artifactInfo){
        /**
         * Specify the artifact eg. "org.apache.maven:maven-aether-provider:3.1.0"
         */
        //Artifact artifact = new DefaultArtifact( groupID + ":" + artifactID + ":" + version);
        Artifact artifact = new DefaultArtifact( artifactInfo );

        DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter( JavaScopes.COMPILE );

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot( new Dependency( artifact, JavaScopes.COMPILE ) );
        collectRequest.setRepositories( repositoryList );

        DependencyRequest dependencyRequest = new DependencyRequest( collectRequest, classpathFlter );

        List<ArtifactResult> artifactResults = null;
        try {
            artifactResults = repositorySystem.resolveDependencies( session, dependencyRequest ).getArtifactResults();
        } catch (DependencyResolutionException e) {
            e.printStackTrace();
        }


        for ( ArtifactResult artifactResult : artifactResults )
        {
            System.out.println( artifactResult.getArtifact() + " resolved to " + artifactResult.getArtifact().getFile() );
            allResolvedDependencies.add(artifactResult.getArtifact().getFile()+"");
        }
    }

    /**
     * Generates the classpath for updating dependencies dynamically.
     */
    public String generateClassPathStringForArtifact(){
        //mvn exec:exec -Dexec.args="-classpath %classpath com.acme.Main" \ -Dexec.executable="java"
        String classPathString = "";
        for(Iterator<String> i = allResolvedDependencies.iterator(); i.hasNext(); ) {
            String item = i.next();
            classPathString += item + File.pathSeparator;
        }

        return classPathString;
    }
}