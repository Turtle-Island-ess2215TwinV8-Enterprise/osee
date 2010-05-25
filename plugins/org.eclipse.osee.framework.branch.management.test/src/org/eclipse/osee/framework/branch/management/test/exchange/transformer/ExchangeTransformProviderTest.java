/*
 * Created on May 24, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.branch.management.test.exchange.transformer;

import java.util.Collection;
import java.util.Iterator;
import junit.framework.Assert;
import org.eclipse.osee.framework.branch.management.exchange.transform.ExchangeTransformProvider;
import org.eclipse.osee.framework.branch.management.exchange.transform.IExchangeTransformProvider;
import org.eclipse.osee.framework.branch.management.exchange.transform.IOseeExchangeVersionTransformer;
import org.eclipse.osee.framework.branch.management.exchange.transform.V0_8_3Transformer;
import org.eclipse.osee.framework.branch.management.exchange.transform.V0_9_0Transformer;
import org.eclipse.osee.framework.branch.management.exchange.transform.V0_9_2Transformer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Version;

/**
 * Test Case for {@link ExchangeTransformProvider}
 * 
 * @author Roberto E. Escobar
 */
public class ExchangeTransformProviderTest {
   private static IExchangeTransformProvider transformProvider = new ExchangeTransformProvider(null);

   @BeforeClass
   public static void setup() {
      transformProvider = new ExchangeTransformProvider(null);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testGetApplicableTransforms() {
      assertApplicable("0.0.0", V0_8_3Transformer.class, V0_9_0Transformer.class, V0_9_2Transformer.class);
      assertApplicable("0.0.0.v201009081001", V0_8_3Transformer.class, V0_9_0Transformer.class, V0_9_2Transformer.class);
      assertApplicable("0.8.2.v201009081001", V0_8_3Transformer.class, V0_9_0Transformer.class, V0_9_2Transformer.class);
      assertApplicable("0.8.3.v201009081001", V0_9_0Transformer.class, V0_9_2Transformer.class);
      assertApplicable("0.9", V0_9_2Transformer.class);
      assertApplicable("0.9.1.v201009081001", V0_9_2Transformer.class);
      assertApplicable("0.9.2");
      assertApplicable("1");
   }

   private static void assertApplicable(String versionToCheck, Class<? extends IOseeExchangeVersionTransformer>... expectedTransforms) {
      Version version = new Version(versionToCheck);

      String message = String.format("Version[%s]", version);

      Collection<IOseeExchangeVersionTransformer> actualTransforms =
            transformProvider.getApplicableTransformers(version);
      Assert.assertEquals(message, expectedTransforms.length, actualTransforms.size());

      Iterator<IOseeExchangeVersionTransformer> iterator = actualTransforms.iterator();
      for (int index = 0; index < expectedTransforms.length; index++) {
         Object expected = expectedTransforms[index];
         Object actual = iterator.next();

         Assert.assertEquals(message, expected, actual.getClass());
      }

   }
}
