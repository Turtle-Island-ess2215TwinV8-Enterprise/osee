/*
 * Created on Jul 15, 2010
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.core.model.access.exp;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.osee.framework.core.enums.PermissionEnum;
import org.eclipse.osee.framework.core.model.IBasicArtifact;

public class AccessFilterChain {
	private final List<IAccessFilter> filters;

	//input user, toCheck
	//input user, branch

	//output collection artifactTypes
	//attribute types

	public AccessFilterChain() {
		this.filters = new LinkedList<IAccessFilter>();
	}

	public void add(IAccessFilter filter) {
		filters.add(filter);
	}

	public boolean doFilter(IBasicArtifact<?> artifact, Object object, PermissionEnum toPermission, PermissionEnum agrPermission) {

		Collections.sort(filters, new Comparator<IAccessFilter>() {

			@Override
			public int compare(IAccessFilter o1, IAccessFilter o2) {
				return o1.getPriority() - o2.getPriority();
			}

		});

		for (IAccessFilter filter : filters) {
			if (filter.acceptToObject(object)) {
				agrPermission = filter.filter(artifact, object, toPermission, agrPermission, this);
			} else {
				break;
			}
		}

		boolean toReturn = false;
		if (agrPermission != null) {
			toReturn = agrPermission.matches(toPermission);
		}
		return toReturn;
	}

	public void addAll(Collection<IAccessFilter> filtersToAdd) {
		filters.addAll(filtersToAdd);
	}
}