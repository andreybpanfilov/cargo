/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ========================================================================
 */
package org.codehaus.cargo.sample.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import junit.framework.Test;

import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.sample.java.validator.HasBundleSupportValidator;
import org.codehaus.cargo.sample.java.validator.Validator;
import org.codehaus.cargo.sample.java.validator.IsLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;

public class BundleCapabilityContainerTest extends AbstractCargoTestCase
{
    public BundleCapabilityContainerTest(String testName, EnvironmentTestData testData)
        throws Exception
    {
        super(testName, testData);
    }

    public static Test suite() throws Exception
    {
        CargoTestSuite suite = new CargoTestSuite(
            "Tests that run on containers supporting OSGi deployments");

        suite.addTestSuite(BundleCapabilityContainerTest.class, new Validator[] {
            new IsLocalContainerValidator(),
            new HasStandaloneConfigurationValidator(),
            new HasBundleSupportValidator()});
        return suite;
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));
    }

    public void testStartWithBundleDeployed() throws Exception
    {
        BufferedReader reader;
        File bundleOutput = new File(getLocalContainer().getConfiguration().getHome(),
            "bundle-output.txt");
        assertFalse(bundleOutput + " already exists!", bundleOutput.isFile());

        Deployable bundle = new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            getTestData().getTestDataFileFor("simple-bundle"), DeployableType.BUNDLE);

        getLocalContainer().getConfiguration().addDeployable(bundle);

        getLocalContainer().start();
        assertEquals(State.STARTED, getContainer().getState());
        assertTrue(bundleOutput + " does not exist!", bundleOutput.isFile());
        reader = new BufferedReader(new FileReader(bundleOutput));
        assertEquals("Hello, World", reader.readLine());
        reader.close();
        reader = null;
        System.gc();

        getLocalContainer().stop();
        assertEquals(State.STOPPED, getContainer().getState());
        reader = new BufferedReader(new FileReader(bundleOutput));
        assertEquals("Goodbye, World", reader.readLine());
        reader.close();
        reader = null;
        System.gc();
    }
}