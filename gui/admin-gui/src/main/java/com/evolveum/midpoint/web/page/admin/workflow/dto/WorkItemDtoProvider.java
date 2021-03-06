/*
 * Copyright (c) 2010-2013 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.web.page.admin.workflow.dto;

import com.evolveum.midpoint.model.api.ModelAuthorizationAction;
import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.ObjectPaging;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.builder.QueryBuilder;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.security.api.MidPointPrincipal;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.exception.*;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.component.data.BaseSortableDataProvider;
import com.evolveum.midpoint.web.security.SecurityUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import org.apache.wicket.Component;

import java.util.*;

import static com.evolveum.midpoint.gui.api.util.WebComponentUtil.*;
import static com.evolveum.midpoint.prism.query.OrderDirection.DESCENDING;
import static com.evolveum.midpoint.schema.GetOperationOptions.createResolve;
import static com.evolveum.midpoint.xml.ns._public.common.common_3.WorkItemType.*;

/**
 * @author lazyman
 */
public class WorkItemDtoProvider extends BaseSortableDataProvider<WorkItemDto> {

    private static final transient Trace LOGGER = TraceManager.getTrace(WorkItemDtoProvider.class);
    private static final String DOT_CLASS = WorkItemDtoProvider.class.getName() + ".";
    private static final String OPERATION_LIST_ITEMS = DOT_CLASS + "listItems";
    private static final String OPERATION_COUNT_ITEMS = DOT_CLASS + "countItems";

    boolean claimable;
	boolean all;

    public String currentUser() {
        MidPointPrincipal principal = SecurityUtils.getPrincipalUser();
        if (principal == null) {
            return "Unknown";
        }

        return principal.getOid();
    }

    public WorkItemDtoProvider(Component component, boolean claimable, boolean all) {
        super(component);
        this.claimable = claimable;
		this.all = all;
    }

    @Override
    public Iterator<? extends WorkItemDto> internalIterator(long first, long count) {
        getAvailableData().clear();

        Task task = getTaskManager().createTaskInstance();
        OperationResult result = new OperationResult(OPERATION_LIST_ITEMS);

        try {
            ObjectQuery query = createQuery(first, count, result);
            Collection<SelectorOptions<GetOperationOptions>> options =
                    Arrays.asList(
                            SelectorOptions.create(new ItemPath(F_ASSIGNEE_REF), createResolve()));
            List<WorkItemType> items = getModel().searchContainers(WorkItemType.class, query, options, task, result);

            for (WorkItemType item : items) {
                try {
                    getAvailableData().add(new WorkItemDto(item));
                } catch (Exception e) {
                    LoggingUtils.logUnexpectedException(LOGGER, "Unhandled exception when listing work item {}", e, item);
                    result.recordFatalError("Couldn't list work item.", e);
                }
            }

        } catch (SchemaException|ObjectNotFoundException|SecurityViolationException|ConfigurationException|RuntimeException ex) {
            LoggingUtils.logUnexpectedException(LOGGER, "Unhandled exception when listing work items", ex);
            result.recordFatalError("Couldn't list work items.", ex);
        }

        if (result.isUnknown()) {
            result.computeStatus();
        }

        return getAvailableData().iterator();
    }

    private ObjectQuery createQuery(long first, long count, OperationResult result) throws SchemaException {
        ObjectQuery query = createQuery(result);
        query.setPaging(ObjectPaging.createPaging(safeLongToInteger(first), safeLongToInteger(count), F_WORK_ITEM_CREATED_TIMESTAMP, DESCENDING));
        return query;
    }

    private ObjectQuery createQuery(OperationResult result) throws SchemaException {
		boolean authorizedToSeeAll = isAuthorized(ModelAuthorizationAction.READ_ALL_WORK_ITEMS.getUrl());
		if (all && authorizedToSeeAll) {
			return QueryBuilder.queryFor(WorkItemType.class, getPrismContext())
					.build();
		} else if (all || !claimable) {
			// not authorized to see all => sees only allocated to him (not quite what is expected, but sufficient for the time being)
			return QueryBuilder.queryFor(WorkItemType.class, getPrismContext())
					.item(WorkItemType.F_ASSIGNEE_REF).ref(currentUser())
					.build();
        } else {
			return QueryBuilder.queryFor(WorkItemType.class, getPrismContext())
					.item(WorkItemType.F_CANDIDATE_ROLES_REF).ref(getGroupsForUser(currentUser(), result))
					.build();
        }
    }

	@Override
    protected int internalSize() {
        int count = 0;
        Task task = getTaskManager().createTaskInstance();
        OperationResult result = new OperationResult(OPERATION_COUNT_ITEMS);
        try {
            ObjectQuery query = createQuery(result);
            count = getModel().countContainers(WorkItemType.class, query, null, task, result);
        } catch (SchemaException|SecurityViolationException|RuntimeException e) {
            throw new SystemException("Couldn't count work items: " + e.getMessage(), e);
        }

        if (result.isUnknown()) {
            result.computeStatus();
        }

        if (!result.isSuccess()) {
            getPage().showResult(result);
        }

        return count;
    }

    // TODO - fix this temporary implementation (perhaps by storing 'groups' in user context on logon)
	// TODO: currently we check only the direct assignments, we need to implement more complex mechanism
	public List<PrismReferenceValue> getGroupsForUser(String oid, OperationResult result) throws SchemaException {
        List<PrismReferenceValue> retval = new ArrayList<>();
		UserType userType;
		try {
			userType = getRepositoryService().getObject(UserType.class, oid, null, result).asObjectable();
		} catch (ObjectNotFoundException e) {
			return retval;
		}
		for (AssignmentType assignmentType : userType.getAssignment()) {
            ObjectReferenceType ref = assignmentType.getTargetRef();
            if (ref != null) {
				retval.add(ref.clone().asReferenceValue());
            }
        }
        return retval;
    }

}
