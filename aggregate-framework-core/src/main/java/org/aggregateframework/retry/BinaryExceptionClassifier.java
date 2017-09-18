/*
 * Copyright 2006-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aggregateframework.retry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by changming.xie on 2/1/16.
 * part of the source code come from open source:spring-retry.
 */
public class BinaryExceptionClassifier extends SubclassClassifier<Throwable, Boolean> {

	private boolean traverseCauses;

	public BinaryExceptionClassifier(boolean defaultValue) {
		super(defaultValue);
	}

	public BinaryExceptionClassifier(Collection<Class<? extends Throwable>> exceptionClasses, boolean value) {
		this(!value);
		if (exceptionClasses != null) {
			Map<Class<? extends Throwable>, Boolean> map = new HashMap<Class<? extends Throwable>, Boolean>();
			for (Class<? extends Throwable> type : exceptionClasses) {
				map.put(type, !getDefault());
			}
			setTypeMap(map);
		}
	}

	public BinaryExceptionClassifier(Collection<Class<? extends Throwable>> exceptionClasses) {
		this(exceptionClasses, true);
	}

	public BinaryExceptionClassifier(Map<Class<? extends Throwable>, Boolean> typeMap) {
		this(typeMap, false);
	}

	public BinaryExceptionClassifier(Map<Class<? extends Throwable>, Boolean> typeMap, boolean defaultValue) {
		super(typeMap, defaultValue);
	}


	public void setTraverseCauses(boolean traverseCauses) {
		this.traverseCauses = traverseCauses;
	}

	@Override
	public Boolean classify(Throwable classifiable) {
		Boolean classified = super.classify(classifiable);
		if (!this.traverseCauses) {
			return classified;
		}

		if (classified.equals(this.getDefault())) {
			Throwable cause = classifiable;
			do {
				if (this.getClassified().containsKey(cause.getClass())) {
					return classified; // non-default classification
				}
				cause = cause.getCause();
				classified = super.classify(cause);
			}
			while (cause != null && classified.equals(this.getDefault()));
		}

		return classified;
	}

}
