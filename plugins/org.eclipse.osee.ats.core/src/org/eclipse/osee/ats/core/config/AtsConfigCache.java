/*
 * Created on May 31, 2012
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.ats.core.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.osee.ats.api.IAtsConfigObject;
import org.eclipse.osee.ats.api.team.IAtsTeamDefinition;
import org.eclipse.osee.ats.core.config.internal.ActionableItemFactory;
import org.eclipse.osee.ats.core.config.internal.TeamDefinitionFactory;
import org.eclipse.osee.ats.core.config.internal.VersionFactory;
import org.eclipse.osee.framework.core.util.XResultData;
import org.eclipse.osee.framework.jdk.core.type.HashCollection;

/**
 * @author Donald G. Dunne
 */
public class AtsConfigCache {

   public static AtsConfigCache instance = new AtsConfigCache();

   /***
    * Allows cache to be loaded and quick swapped
    */
   public static synchronized void setCurrent(AtsConfigCache newInstance) {
      instance = newInstance;
   }

   // cache by guid and any other cachgeByTag item (like static id)
   private final List<IAtsConfigObject> configObjects = new CopyOnWriteArrayList<IAtsConfigObject>();
   private final HashCollection<String, IAtsConfigObject> tagToConfigObject =
      new HashCollection<String, IAtsConfigObject>(true, CopyOnWriteArrayList.class);

   public void cache(IAtsConfigObject configObject) {
      configObjects.add(configObject);
      cacheByTag(configObject.getGuid(), configObject);
   }

   public void cacheByTag(String tag, IAtsConfigObject configObject) {
      tagToConfigObject.put(tag, configObject);
   }

   @SuppressWarnings("unchecked")
   public final <A extends IAtsConfigObject> List<A> getByTag(String tag, Class<A> clazz) {
      List<A> objs = new ArrayList<A>();
      Collection<IAtsConfigObject> values = tagToConfigObject.getValues(tag);
      if (values != null) {
         for (IAtsConfigObject obj : values) {
            if (clazz.isInstance(obj)) {
               objs.add((A) obj);
            }
         }
      }
      return objs;
   }

   @SuppressWarnings("unchecked")
   public final <A extends IAtsConfigObject> A getSoleByTag(String tag, Class<A> clazz) {
      Collection<IAtsConfigObject> values = tagToConfigObject.getValues(tag);
      if (values != null) {
         for (IAtsConfigObject obj : values) {
            if (clazz.isInstance(obj)) {
               return (A) obj;
            }
         }
      }
      return null;
   }

   @SuppressWarnings("unchecked")
   public final <A extends IAtsConfigObject> A getSoleByName(String name, Class<A> clazz) {
      for (IAtsConfigObject obj : get(clazz)) {
         if (obj.getName().equals(name)) {
            return (A) obj;
         }
      }
      return null;
   }

   @SuppressWarnings("unchecked")
   public final <A extends IAtsConfigObject> List<A> get(Class<A> clazz) {
      List<A> objs = new ArrayList<A>();
      for (IAtsConfigObject obj : configObjects) {
         if (clazz.isInstance(obj)) {
            objs.add((A) obj);
         }
      }
      return objs;
   }

   public final <A extends IAtsConfigObject> A getSoleByGuid(String guid, Class<A> clazz) {
      List<A> list = getByTag(guid, clazz);
      if (list.isEmpty()) {
         return null;
      }
      return list.iterator().next();
   }

   public final IAtsConfigObject getSoleByGuid(String guid) {
      return getSoleByGuid(guid, IAtsConfigObject.class);
   }

   public IAtsTeamDefinition getSoleByName(String teamDefName) {
      return null;
   }

   @SuppressWarnings("unchecked")
   public final <A extends IAtsConfigObject> List<A> getByName(String name, Class<A> clazz) {
      List<A> objs = new ArrayList<A>();
      for (IAtsConfigObject obj : configObjects) {
         if (clazz.isInstance(obj) && obj.getName().equals(name)) {
            objs.add((A) obj);
         }
      }
      return objs;
   }

   public void decache(IAtsConfigObject atsObject) {
      configObjects.remove(atsObject);
      List<String> keysToRemove = new ArrayList<String>();
      for (Entry<String, Collection<IAtsConfigObject>> entry : tagToConfigObject.entrySet()) {
         if (entry.getValue().contains(atsObject)) {
            keysToRemove.add(entry.getKey());
         }
      }
      for (String key : keysToRemove) {
         tagToConfigObject.removeValue(key, atsObject);
      }
   }

   public void clearCaches() {
      tagToConfigObject.clear();
      configObjects.clear();
   }

   public IActionableItemFactory getActionableItemFactory() {
      return new ActionableItemFactory(this);
   }

   public ITeamDefinitionFactory getTeamDefinitionFactory() {
      return new TeamDefinitionFactory(this);
   }

   public IVersionFactory getVersionFactory() {
      return new VersionFactory(this);
   }

   public void getReport(XResultData rd) {
      rd.logWithFormat("AtsConfigCache id %s\n", AtsConfigCache.instance);
      rd.logWithFormat("TagToConfigObject size %d\n", tagToConfigObject.keySet().size());
      rd.logWithFormat("ConfigObjects size %d\n", configObjects.size());
   }

   @Override
   public String toString() {
      return configObjects.toString();
   }
}
