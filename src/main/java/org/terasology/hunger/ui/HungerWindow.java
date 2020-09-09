// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.hunger.ui;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.hunger.HungerUtils;
import org.terasology.hunger.component.HungerComponent;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.UILoadBar;


public class HungerWindow extends CoreHudWidget {
    /**
     * This method initialises the hunger bar UI for a player and updates it based on current hunger.
     */
    @Override
    public void initialise() {
        UILoadBar hunger = find("hunger", UILoadBar.class);
        hunger.bindVisible(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                EntityRef character = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
                return character != null && character.hasComponent(HungerComponent.class);
            }
        });
        hunger.bindValue(
                new Binding<Float>() {
                    @Override
                    public Float get() {
                        EntityRef character = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
                        if (character == null || !character.hasComponent(HungerComponent.class)) {
                            return 0.0f;
                        }

                        HungerComponent hunger = character.getComponent(HungerComponent.class);
                        return HungerUtils.getHungerForEntity(character) / hunger.maxFoodCapacity;
                    }

                    @Override
                    public void set(Float value) {
                    }
                });
    }
}
