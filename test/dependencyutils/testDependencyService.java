package dependencyutils;

import minderengine.dependencyutils.DependencyService;

/**
 * Created by melis on 25/05/15.
 */
public class testDependencyService {
    public static void main(String[ ] arg){
        DependencyService.getInstance().createRepositorySystem("target/new-local-repo");

        //DependencyService.getInstance().addRepository("central", "default", "http://central.maven.org/maven2/");
        //or you may use directly DependencyService.getInstance().addMavenCentralRepository();

        DependencyService.getInstance().addRepository("Eid public repository", "default", "http://eidrepo:8081/nexus/content/groups/public/");
        //or you may use directly DependencyService.getInstance().addEidRepository();

//        String groupId = "gov.tubitak.minder";//"org.apache.maven";
//        String artifactId = "minder-common";//"maven-aether-provider";
//        String version = "0.0.5";//"3.1.0";

        /**
         * DependencyService.getInstance().downloadArtifactWithAllDependencies("org.apache.maven:maven-aether-provider:3.1.0");
         * String classPathString = DependencyService.getInstance().generateClassPathStringForArtifact();
         * System.out.println("Classpath: " + classPathString );
         */

        String dependencyString = "org.apache.maven:maven-aether-provider:3.1.0\ngov.tubitak.minder:minder-common:0.0.5";
        //String dependencyString = "gov.tubitak.minder:minder-common:0.0.5";
        String path = DependencyService.getInstance().getClassPathString(dependencyString);
        System.out.println("path:\n"+path);
    }

    /* public static void main(String[ ] arg){
        /**
         * Creates a repository system that can maintain many repositories

        RepositorySystem repositorySystem = Booter.newRepositorySystem();

        /**
         * Session properties (such as where to download etc.)

        DefaultRepositorySystemSession session = Booter.newRepositorySystemSession( repositorySystem );

        /**
         * Determines what will happen in case of version conflicts.

        session.setConfigProperty( ConflictResolver.CONFIG_PROP_VERBOSE, true );
        session.setConfigProperty( DependencyManagerUtils.CONFIG_PROP_VERBOSE, true );

        /**
         * Specify the artifact eg. "org.apache.maven:maven-aether-provider:3.1.0"

        Artifact artifact = new DefaultArtifact( "org.apache.maven:maven-aether-provider:3.1.0" );

        /**
         * A request to read an artifact descriptor.
         * Booter.newRepositories method: Declares the central maven repository as a repository in our repository system.

        ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
        descriptorRequest.setArtifact( artifact );
        descriptorRequest.setRepositories( Booter.newRepositories( repositorySystem, session ) );
        ArtifactDescriptorResult descriptorResult = null;
        try {
            descriptorResult = repositorySystem.readArtifactDescriptor(session, descriptorRequest);
        } catch (ArtifactDescriptorException e) {
            e.printStackTrace();
        }


        /**
         * A request to collect the transitive dependencies and to build a dependency graph from them.

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRootArtifact( descriptorResult.getArtifact() );
        collectRequest.setDependencies( descriptorResult.getDependencies() );
        collectRequest.setManagedDependencies( descriptorResult.getManagedDependencies() );
        collectRequest.setRepositories( descriptorRequest.getRepositories() );

        CollectResult collectResult = null;
        try {
            collectResult = repositorySystem.collectDependencies( session, collectRequest );
        } catch (DependencyCollectionException e) {
            e.printStackTrace();
        }

        collectResult.getRoot().accept( new ConsoleDependencyGraphDumper() );

    }
    */

}
