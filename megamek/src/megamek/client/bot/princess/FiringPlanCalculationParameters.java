/*
 * Copyright (c) 2024 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MegaMek.
 *
 * MegaMek is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MegaMek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MegaMek. If not, see <http://www.gnu.org/licenses/>.
 */
package megamek.client.bot.princess;

import static megamek.client.bot.princess.FiringPlanCalculationParameters.FiringPlanCalculationType.GET;
import static megamek.client.bot.princess.FiringPlanCalculationParameters.FiringPlanCalculationType.GUESS;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import megamek.common.Entity;
import megamek.common.Targetable;
import megamek.common.annotations.Nullable;
import megamek.common.equipment.WeaponMounted;
import megamek.logging.MMLogger;
import org.apache.logging.log4j.message.ParameterizedMessage;

/**
 * This data structure contains parameters that may be passed to the "determineBestFiringPlan()"
 */
public final class FiringPlanCalculationParameters {
    private final static MMLogger LOGGER = MMLogger.create(FiringPlanCalculationParameters.class);

    // The type of firing plan calculation to carry out
    public enum FiringPlanCalculationType {
        /**
         * We're guessing the firing plan based on our estimate of enemy movement
         */
        GUESS,
        /**
         * We're getting a firing plan based on exact known enemy movement results
         */
        GET
    }

    private final Entity shooter;
    private final EntityState shooterState;
    private final Targetable target;
    private final EntityState targetState;
    private final int maxHeat;
    private final Map<WeaponMounted, Double> ammoConservation;
    private final FiringPlanCalculationType calculationType;

    public static class Builder {
        private Entity shooter = null;
        private EntityState shooterState = null;
        private Targetable target = null;
        private EntityState targetState = null;
        private int maxHeat = Entity.DOES_NOT_TRACK_HEAT;
        private Map<WeaponMounted, Double> ammoConservation = new HashMap<>();
        private FiringPlanCalculationType calculationType = GUESS;

        /**
         * The unit doing the shooting.
         */
        public Builder setShooter(final Entity value) {
            if (null == value) {
                throw new NullPointerException("Must have a shooter.");
            }
            shooter = value;
            return this;
        }

        /**
         * the current state of the shooting unit.
         */
        public Builder setShooterState(@Nullable final EntityState value) {
            shooterState = value;
            return this;
        }

        /**
         * The unit being shot at.
         */
        public Builder setTarget(final Targetable value) {
            if (null == value) {
                throw new NullPointerException("Must have a target.");
            }
            target = value;
            return this;
        }

        /**
         * The current state of the target unit.
         */
        public Builder setTargetState(@Nullable final EntityState value) {
            targetState = value;
            return this;
        }

        /**
         * How much heat we're willing to tolerate. Defaults to {@link Entity#DOES_NOT_TRACK_HEAT}
         */
        public Builder setMaxHeat(final int value) {
            if (value < 0) {
                LOGGER.warn("Invalid max heat: {}", value);
                maxHeat = 0;
                return this;
            }

            maxHeat = value;
            return this;
        }

        /**
         * Ammo conservation biases of the unit's mounted weapons. Defaults to an empty map.
         */
        public Builder setAmmoConservation(@Nullable final Map<WeaponMounted, Double> value) {
            ammoConservation = value;
            return this;
        }

        /**
         * Are we guessing or not? Defaults to {@link FiringPlanCalculationType#GUESS}
         */
        public Builder setCalculationType(final FiringPlanCalculationType value) {
            if (null == value) {
                throw new NullPointerException("Must have a calculation type.");
            }
            calculationType = value;
            return this;
        }

        /**
         * Builds the new {@link FiringPlanCalculationParameters} based on the builder properties.
         */
        public FiringPlanCalculationParameters build() {
            return new FiringPlanCalculationParameters(this);
        }

        public FiringPlanCalculationParameters buildGuess(final Entity shooter,
                                                          @Nullable final EntityState shooterState,
                                                          final Targetable target,
                                                          @Nullable final EntityState targetState, final int maxHeat,
                                                          @Nullable final Map<WeaponMounted, Double> ammoConservation) {
            return setShooter(shooter).setShooterState(shooterState)
                         .setTarget(target)
                         .setTargetState(targetState)
                         .setMaxHeat(maxHeat)
                         .setAmmoConservation(ammoConservation)
                         .setCalculationType(FiringPlanCalculationType.GUESS)
                         .build();
        }

        public FiringPlanCalculationParameters buildExact(final Entity shooter, final Targetable target,
                                                          final Map<WeaponMounted, Double> ammoConservation) {
            return setShooter(shooter).setTarget(target)
                         .setAmmoConservation(ammoConservation)
                         .setCalculationType(GET)
                         .build();
        }

    }

    // internal constructor
    private FiringPlanCalculationParameters(final Builder builder) {
        this.shooter = builder.shooter;
        this.shooterState = builder.shooterState;
        this.target = builder.target;
        this.targetState = builder.targetState;
        maxHeat = Math.max(builder.maxHeat, 0);
        this.ammoConservation = builder.ammoConservation;
        this.calculationType = builder.calculationType;
    }

    Entity getShooter() {
        return shooter;
    }

    @Nullable
    EntityState getShooterState() {
        return shooterState;
    }

    public Targetable getTarget() {
        return target;
    }

    @Nullable
    EntityState getTargetState() {
        return targetState;
    }

    int getMaxHeat() {
        return maxHeat;
    }

    @Nullable
    Map<WeaponMounted, Double> getAmmoConservation() {
        return ammoConservation;
    }

    FiringPlanCalculationType getCalculationType() {
        return calculationType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FiringPlanCalculationParameters that)) {
            return false;
        }

        if (maxHeat != that.maxHeat) {
            return false;
        }
        if (!shooter.equals(that.shooter)) {
            return false;
        }
        if (!Objects.equals(shooterState, that.shooterState)) {
            return false;
        }
        if (!target.equals(that.target)) {
            return false;
        }
        if (!Objects.equals(targetState, that.targetState)) {
            return false;
        }
        // noinspection SimplifiableIfStatement
        if (!Objects.equals(ammoConservation, that.ammoConservation)) {
            return false;
        }
        return calculationType == that.calculationType;
    }

    @Override
    public int hashCode() {
        int result = shooter.hashCode();
        result = 31 * result + (shooterState != null ? shooterState.hashCode() : 0);
        result = 31 * result + target.hashCode();
        result = 31 * result + (targetState != null ? targetState.hashCode() : 0);
        result = 31 * result + maxHeat;
        result = 31 * result + (ammoConservation != null ? ammoConservation.hashCode() : 0);
        result = 31 * result + calculationType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return new ParameterizedMessage("""
              FiringPlanCalculationParameters{
              \tshooter = {},
              \tshooterState = {},
              \ttarget = {},
              \ttargetState = {},
              \tmaxHeat = {},
              \tammoConservation = {},
              \tcalculationType = {}
              }""",
              shooter,
              shooterState,
              target,
              targetState,
              maxHeat,
              ammoConservation,
              calculationType).getFormattedMessage();
    }
}
