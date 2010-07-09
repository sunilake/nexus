package org.sonatype.nexus.integrationtests.nexus3615;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Response;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.Maven2ArtifactInfoResource;
import org.sonatype.nexus.rest.model.Maven2ArtifactInfoResourceRespose;
import org.sonatype.nexus.test.utils.DeployUtils;

import com.thoughtworks.xstream.XStream;

/**
 * Tests the ?describe=maven2 content view.
 * @author Brian Demers
 *
 */
public class Nexus3615MavenInfoIT
    extends AbstractNexusIntegrationTest
{

    /**
     * Positive release tests. 
     * @throws Exception
     */
    @Test
    public void deployAndRunReleaseTests() throws Exception
    {
        // deploy releases
        Gav simpleJarGav = new Gav("nexus3615", "simpleJar", "1.0.1", null, "jar", null, null, null, false, false, null, false, null );
        this.deployGav( simpleJarGav, this.getTestRepositoryId() );
        this.downloadAndVerify( simpleJarGav, this.getTestRepositoryId() );
        
        Gav withClassifierGav = new Gav("nexus3615", "simpleJar", "1.0.1", "classifier", "jar", null, null, null, false, false, null, false, null );
        this.deployGav( withClassifierGav, this.getTestRepositoryId() );
        this.downloadAndVerify( withClassifierGav, this.getTestRepositoryId() );
        
        Gav withExtentionGav = new Gav("nexus3615", "simpleJar", "1.0.1", null, "extention", null, null, null, false, false, null, false, null );
        this.deployGav( withExtentionGav, this.getTestRepositoryId() );
        this.downloadAndVerify( withExtentionGav, this.getTestRepositoryId() );
        
        Gav withClassifierAndExtentionGav = new Gav("nexus3615", "simpleJar", "1.0.1", "classifier", "extention", null, null, null, false, false, null, false, null );
        this.deployGav( withClassifierAndExtentionGav, this.getTestRepositoryId() );
        this.downloadAndVerify( withClassifierAndExtentionGav, this.getTestRepositoryId() );
    }
    
    /**
     * Positive release tests. 
     * @throws Exception
     */
    @Test
    public void deployAndRunSnapshotTests() throws Exception
    {
        // deploy releases
        Gav simpleJarGav = new Gav("nexus3615", "simpleJar", "1.0.1-SNAPSHOT", null, "jar", 1, System.currentTimeMillis(), null, true, false, null, false, null );
        this.deployGav( simpleJarGav, REPO_TEST_HARNESS_SNAPSHOT_REPO );
        this.downloadAndVerify( simpleJarGav, REPO_TEST_HARNESS_SNAPSHOT_REPO );
        
        Gav withClassifierGav = new Gav("nexus3615", "simpleJar", "1.0.1-SNAPSHOT", "classifier", "jar", 2, System.currentTimeMillis(), null, true, false, null, false, null );
        this.deployGav( withClassifierGav, REPO_TEST_HARNESS_SNAPSHOT_REPO );
        this.downloadAndVerify( withClassifierGav, REPO_TEST_HARNESS_SNAPSHOT_REPO );
        
        Gav withExtentionGav = new Gav("nexus3615", "simpleJar", "1.0.1-SNAPSHOT", null, "extention", 3, System.currentTimeMillis(), null, true, false, null, false, null );
        this.deployGav( withExtentionGav, REPO_TEST_HARNESS_SNAPSHOT_REPO );
        this.downloadAndVerify( withExtentionGav, REPO_TEST_HARNESS_SNAPSHOT_REPO );
        
        Gav withClassifierAndExtentionGav = new Gav("nexus3615", "simpleJar", "1.0.1-SNAPSHOT", "classifier", "extention", 4, System.currentTimeMillis(), null, true, false, null, false, null );
        this.deployGav( withClassifierAndExtentionGav, REPO_TEST_HARNESS_SNAPSHOT_REPO );
        this.downloadAndVerify( withClassifierAndExtentionGav, REPO_TEST_HARNESS_SNAPSHOT_REPO );
    }
    
    @Test
    public void testNonGavArtifacts() throws Exception
    {
        // deploy a non maven path
        new DeployUtils( this ).deployWithWagon( "http", this.getRepositoryUrl( this.getTestRepositoryId() ), this.getTestFile( "pom.xml" ), "foo/bar" );
        
        // now get the info for it
        Response response = RequestFacade.doGetRequest( "service/local/repositories/" + this.getTestRepositoryId() + "/content/" + "foo/bar" + "?describe=maven2" );
        String responseText = response.getEntity().getText();
        Assert.assertEquals( "Response was: "+ responseText, 404, response.getStatus().getCode() );
        
    }
    
    public void deployGav( Gav gav, String repoId ) throws Exception
    {
        new DeployUtils( this ).deployWithWagon( "http", this.getRepositoryUrl( repoId ), this.getTestFile( "simpleJar.jar" ), this.getRelitiveArtifactPath( gav ) );
    }
    
    @Test
    public void notFoundTest() throws Exception
    {
        Gav releaseNotFoundGav = new Gav("nexus3615", "notFound", "1.0.1", null, "jar", null, null, null, false, false, null, false, null );
        Response response = this.downloadView( releaseNotFoundGav, "maven2", this.getTestRepositoryId() );
        Assert.assertEquals( "Status found: "+  response.getStatus(), 404, response.getStatus().getCode() );
        
        Gav snapshotNotFoundGav = new Gav("nexus3615", "notFound", "1.0.1-SNAPSHOT", null, "jar", 1, System.currentTimeMillis(), null, true, false, null, false, null );
        response = this.downloadView( snapshotNotFoundGav, "maven2", this.getTestRepositoryId() );
        Assert.assertEquals( "Status found: "+  response.getStatus(), 404, response.getStatus().getCode() );
    }
    
    private void downloadAndVerify( Gav gav, String repoId ) throws Exception
    {   
        XStream xstream = this.getXMLXStream();
        
        Response response = this.downloadView( gav, "maven2", repoId );
        String responseText = response.getEntity().getText();
        Assert.assertEquals( 200, response.getStatus().getCode() );
        
        Maven2ArtifactInfoResource data = ((Maven2ArtifactInfoResourceRespose) xstream.fromXML( responseText )).getData();
        
        Assert.assertEquals( gav.getArtifactId(), data.getArtifactId() );
        Assert.assertEquals( gav.getGroupId(), data.getGroupId() );
        
        if( gav.isSnapshot() && gav.getSnapshotTimeStamp() != null )
        {
            // time stamp snapshot
            String expectedVersion = gav.getVersion().replaceFirst( "SNAPSHOT", new SimpleDateFormat("yyyyMMdd.HHmmss").format( new Date( gav.getSnapshotTimeStamp() ) ) + "-" + gav.getSnapshotBuildNumber());
            Assert.assertEquals( expectedVersion, data.getVersion() );
        }
        else
        {
            // non snapshot
            Assert.assertEquals( gav.getVersion(), data.getVersion() );    
        }
        Assert.assertEquals( gav.getBaseVersion(), data.getBaseVersion() );
        Assert.assertEquals( gav.getClassifier(), data.getClassifier() );
        Assert.assertEquals( gav.getExtension(), data.getExtension() );
        
        Assert.assertEquals( this.buildExpectedDepBlock( gav ), data.getDependencyXmlChunk() );
    }
    
    private String buildExpectedDepBlock(Gav gav)
    {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append( "<dependency>\n" );
        buffer.append( "  <groupId>" ).append( gav.getGroupId() ).append( "</groupId>\n" );
        buffer.append( "  <artifactId>" ).append( gav.getArtifactId() ).append( "</artifactId>\n" );
        buffer.append( "  <version>" ).append( gav.getBaseVersion() ).append( "</version>\n" );
        if( gav.getClassifier() != null )
        {
            buffer.append( "  <classifier>" ).append(gav.getClassifier()).append( "</classifier>\n" );
        }
        if( gav.getExtension() != null && !gav.getExtension().equals( "jar" ) )
        {
            buffer.append( "  <type>" ).append( gav.getExtension() ).append( "</type>\n" );
        }
        buffer.append( "</dependency>" );
        return buffer.toString();
    }
    
    private Response downloadView( Gav gav, String describeKey, String repoId ) throws Exception
    {
        Response response = RequestFacade.doGetRequest( "service/local/repositories/" + repoId + "/content/" + this.getRelitiveArtifactPath( gav ) + "?describe="+ describeKey );
        return response;
    }
}
