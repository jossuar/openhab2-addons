/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.caddx.internal.action;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.caddx.internal.handler.ThingHandlerZone;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the automation engine action handler service for the
 * caddx bridge actions.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@ThingActionsScope(name = "caddx")
@NonNullByDefault
public class CaddxZoneActions implements ThingActions, ICaddxZoneActions {
    private final Logger logger = LoggerFactory.getLogger(CaddxZoneActions.class);

    private static final String HANDLER_IS_NULL = "ThingHandlerZone is null!";
    private static final String ACTION_CLASS_IS_WRONG = "Instance is not a CaddxZoneActions class.";

    private @Nullable ThingHandlerZone handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ThingHandlerZone) {
            this.handler = (ThingHandlerZone) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    private static ICaddxZoneActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(CaddxZoneActions.class.getName())) {
            if (actions instanceof ICaddxZoneActions) {
                return (ICaddxZoneActions) actions;
            } else {
                return (ICaddxZoneActions) Proxy.newProxyInstance(ICaddxZoneActions.class.getClassLoader(),
                        new Class[] { ICaddxZoneActions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
    }

    @Override
    @RuleAction(label = "bypass", description = "Bypass the zone")
    public void bypass() {
        // Check of parameters
        ThingHandlerZone thingHandler = this.handler;
        if (thingHandler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        thingHandler.bypass();
    }

    @RuleAction(label = "bypass", description = "Bypass the zone")
    public static void bypass(@Nullable ThingActions actions) {
        invokeMethodOf(actions).bypass();
    }
}
