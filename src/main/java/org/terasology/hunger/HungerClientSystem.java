// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.hunger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;

/**
 * Handles client-side functionality for Hunger features.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class HungerClientSystem extends BaseComponentSystem {
    /**
     * The logger for debugging to the log files.
     */
    private static final Logger logger = LoggerFactory.getLogger(HungerAuthoritySystem.class);

    @In
    private NUIManager nuiManager;

    @In
    private Time time;

    /**
     * Adds the hunger bar to the player's HUD.
     */
    @Override
    public void preBegin() {
        nuiManager.getHUD().addHUDElement("Hunger:Hunger");
    }
}
