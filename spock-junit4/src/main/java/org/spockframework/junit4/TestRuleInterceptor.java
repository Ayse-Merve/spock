/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.junit4;

import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.*;

import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class TestRuleInterceptor extends AbstractRuleInterceptor {
  private final SpecInfo spec;
  public TestRuleInterceptor(List<FieldInfo> ruleFields, SpecInfo spec) {
    super(ruleFields);
    this.spec = spec;
  }

  @Override
  public void intercept(final IMethodInvocation invocation) throws Throwable {
    Statement stat = createBaseStatement(invocation);

    Description description = JUnitDescriptionGenerator.describeIteration(invocation.getIteration(), spec);
    for (FieldInfo field : ruleFields) {
      TestRule rule = (TestRule) getRuleInstance(field, invocation.getInstance());
      stat = rule.apply(stat, description);
    }

    evaluateStatement(stat);
  }
}
