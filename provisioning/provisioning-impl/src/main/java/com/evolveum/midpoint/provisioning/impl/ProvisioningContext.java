/**
 * Copyright (c) 2015 Evolveum
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
package com.evolveum.midpoint.provisioning.impl;

import java.util.Collection;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.common.refinery.RefinedObjectClassDefinition;
import com.evolveum.midpoint.common.refinery.RefinedResourceSchema;
import com.evolveum.midpoint.common.refinery.ResourceShadowDiscriminator;
import com.evolveum.midpoint.provisioning.ucf.api.ConnectorInstance;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowKindType;

/**
 * @author semancik
 *
 */
public class ProvisioningContext {
	
	private ResourceType resource;
	private RefinedResourceSchema refinedSchema;
	private ResourceShadowDiscriminator shadowCoordinates;
	private RefinedObjectClassDefinition objectClassDefinition;
	private Task task;
	private ConnectorInstance connector;
	
	public ResourceType getResource() {
		return resource;
	}
	
	public void setResource(ResourceType resource) {
		this.resource = resource;
	}
	
	public RefinedResourceSchema getRefinedSchema() {
		return refinedSchema;
	}
	
	public void setRefinedSchema(RefinedResourceSchema refinedSchema) {
		this.refinedSchema = refinedSchema;
	}
	
	public ResourceShadowDiscriminator getShadowCoordinates() {
		return shadowCoordinates;
	}
	
	public void setShadowCoordinates(ResourceShadowDiscriminator shadowCoordinates) {
		this.shadowCoordinates = shadowCoordinates;
	}
	
	public RefinedObjectClassDefinition getObjectClassDefinition() {
		return objectClassDefinition;
	}
	
	public void setObjectClassDefinition(RefinedObjectClassDefinition objectClassDefinition) {
		this.objectClassDefinition = objectClassDefinition;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}
	
	public String getChannel() {
		return task==null?null:task.getChannel();
	}

	public ConnectorInstance getConnector() {
		return connector;
	}

	public void setConnector(ConnectorInstance connector) {
		this.connector = connector;
	}
	
	public boolean isWildcard() {
		return objectClassDefinition == null;
	}
	
	/**
	 * Creates a context for a different object class on the same resource.
	 */
	public ProvisioningContext spawn(ShadowKindType kind, String intent) {
		ProvisioningContext ctx = spawnSameResource();
		RefinedResourceSchema refinedSchema = ctx.getRefinedSchema();
		RefinedObjectClassDefinition objectClassDefinition = refinedSchema.getRefinedDefinition(kind, intent);
		ctx.setObjectClassDefinition(objectClassDefinition);
		return ctx;
	}
	
	/**
	 * Creates a context for a different object class on the same resource.
	 */
	public ProvisioningContext spawn(ShadowKindType kind, Collection<String> intents) throws SchemaException {
		ProvisioningContext ctx = spawnSameResource();
		RefinedResourceSchema refinedSchema = ctx.getRefinedSchema();
		RefinedObjectClassDefinition objectClassDefinition = refinedSchema.getRefinedDefinition(kind, intents);
		ctx.setObjectClassDefinition(objectClassDefinition);
		return ctx;
	}
	
	/**
	 * Creates a context for a different object class on the same resource.
	 */
	public ProvisioningContext spawn(QName objectClassQName) throws SchemaException {
		ProvisioningContext ctx = spawnSameResource();
		RefinedResourceSchema refinedSchema = ctx.getRefinedSchema();
		RefinedObjectClassDefinition objectClassDefinition = refinedSchema.getRefinedDefinition(objectClassQName);
		ctx.setObjectClassDefinition(objectClassDefinition);
		return ctx;
	}
	
	private ProvisioningContext spawnSameResource() {
		ProvisioningContext ctx = new ProvisioningContext();
		ctx.setTask(this.getTask());
		ctx.setResource(this.getResource());
		ctx.setConnector(this.getConnector());
		ctx.setRefinedSchema(this.getRefinedSchema());
		return ctx;
	}
}