/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.encrypt.subscriber.compatible;

import org.apache.shardingsphere.encrypt.api.config.CompatibleEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.rule.YamlEncryptTableRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropNamedRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropRuleItemEvent;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.subsciber.RuleItemChangedSubscribeEngine;

import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Compatible encrypt table subscribe engine.
 * @deprecated compatible support will remove in next version.
 */
@Deprecated
public final class CompatibleEncryptTableSubscribeEngine extends RuleItemChangedSubscribeEngine<CompatibleEncryptRuleConfiguration, EncryptTableRuleConfiguration> {
    
    public CompatibleEncryptTableSubscribeEngine(final ContextManager contextManager) {
        super(contextManager);
    }
    
    @Override
    protected EncryptTableRuleConfiguration swapRuleItemConfigurationFromEvent(final AlterRuleItemEvent event, final String yamlContent) {
        return new YamlEncryptTableRuleConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContent, YamlEncryptTableRuleConfiguration.class));
    }
    
    @Override
    protected CompatibleEncryptRuleConfiguration findRuleConfiguration(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().findSingleRule(EncryptRule.class)
                .map(optional -> getCompatibleEncryptRuleConfiguration((CompatibleEncryptRuleConfiguration) optional.getConfiguration()))
                .orElseGet(() -> new CompatibleEncryptRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>()));
    }
    
    private CompatibleEncryptRuleConfiguration getCompatibleEncryptRuleConfiguration(final CompatibleEncryptRuleConfiguration config) {
        return null == config.getTables() ? new CompatibleEncryptRuleConfiguration(new LinkedList<>(), config.getEncryptors()) : config;
    }
    
    @Override
    protected void changeRuleItemConfiguration(final AlterRuleItemEvent event, final CompatibleEncryptRuleConfiguration currentRuleConfig, final EncryptTableRuleConfiguration toBeChangedItemConfig) {
        // TODO refactor DistSQL to only persist config
        currentRuleConfig.getTables().removeIf(each -> each.getName().equals(toBeChangedItemConfig.getName()));
        currentRuleConfig.getTables().add(toBeChangedItemConfig);
    }
    
    @Override
    protected void dropRuleItemConfiguration(final DropRuleItemEvent event, final CompatibleEncryptRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getTables().removeIf(each -> each.getName().equals(((DropNamedRuleItemEvent) event).getItemName()));
    }
}
