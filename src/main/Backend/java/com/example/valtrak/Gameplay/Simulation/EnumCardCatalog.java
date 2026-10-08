package com.example.valtrak.Gameplay.Simulation;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo.AmmoSupplyCrate;
import com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo.FuelSupplyDrum;
import com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo.RepairSupplyKit;
import com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo.SupplyCrate;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleClass;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Ammunition;
import com.example.valtrak.Data.CardLibrary.Interfaces.Vehicle.GroundVehicleCardInterface;
import com.example.valtrak.Data.CardLibrary.Vehicles.Germany.GermanVehicles;
import com.example.valtrak.Data.CardLibrary.Vehicles.Russia.RussianVehicles;
import com.example.valtrak.Data.CardLibrary.Vehicles.Support.ReconVehicles;
import com.example.valtrak.Data.CardLibrary.Vehicles.Support.UavTeams;
import com.example.valtrak.Data.CardLibrary.Vehicles.US.USGroundVehicles;
import com.example.valtrak.Gameplay.Engine.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The card library built straight from the enums, with no database. Card ids are 1, 2, 3... in a fixed order,
 * so simulations are repeatable. (The server's own catalog reads the same cards from the database.)
 */
public final class EnumCardCatalog implements CardCatalog {

    private final Map<Long, CardSpec> specs = new LinkedHashMap<>();

    public EnumCardCatalog() {
        this(false);
    }

    /**
     * @param fillMissingAmmo add a synthetic "5x" crate for every ammunition type that has no card yet (the .50 cal,
     *                        105mm, TOW and 40mm HE have none). Only for simulations: it lets every tank fire.
     */
    public EnumCardCatalog(boolean fillMissingAmmo) {
        long id = 1;
        for (GroundVehicleCardInterface[] nation : new GroundVehicleCardInterface[][]{
                USGroundVehicles.values(), RussianVehicles.values(), GermanVehicles.values(),
                ReconVehicles.values(), UavTeams.values()}) {
            for (GroundVehicleCardInterface v : nation) {
                var attacks = v.getVehicleAttacks().stream()
                        .map(a -> new AttackSpec(a.getAttackSlot(), a.getAttackName(), a.getWeapon(), a.getBaseDamage(),
                                a.getAmmoCost(), a.getFuelCost(), a.getSpecialEffect()))
                        .toList();
                specs.put(id, new VehicleSpec(id, v.getVehicleName(), v.getLevel(),
                        VehicleClass.valueOf(v.getVehicleClass().name()), v.getVehicleHP(), v.getVehicleArmor(), attacks,
                        v.getAbility() == null ? null
                                : new AbilitySpec(v.getAbility().type(), v.getAbility().power(), v.getAbility().fuelCost())));
                id++;
            }
        }
        for (AmmoSupplyCrate c : AmmoSupplyCrate.values()) {
            specs.put(id, new ResourceSpec(id, c.getItemName(), c.getCardLevel(), ResourceKind.AMMO, c.getAmmunition(), c.getCount()));
            id++;
        }
        if (fillMissingAmmo) {
            java.util.Set<Ammunition> have = java.util.EnumSet.noneOf(Ammunition.class);
            for (AmmoSupplyCrate c : AmmoSupplyCrate.values()) have.add(c.getAmmunition());
            for (Ammunition a : Ammunition.values()) {
                if (have.contains(a)) continue;
                specs.put(id, new ResourceSpec(id, "5x " + a.name() + " (synthetic)", CardLevel.UNCOMMON, ResourceKind.AMMO, a, 5));
                id++;
            }
        }
        for (FuelSupplyDrum c : FuelSupplyDrum.values()) {
            specs.put(id, new ResourceSpec(id, c.getItemName(), c.getCardLevel(), ResourceKind.FUEL, null, c.getCount()));
            id++;
        }
        for (RepairSupplyKit c : RepairSupplyKit.values()) {
            specs.put(id, new ResourceSpec(id, c.getItemName(), c.getCardLevel(), ResourceKind.REPAIR, null, c.getRepairAmount()));
            id++;
        }
        for (SupplyCrate c : SupplyCrate.values()) {
            specs.put(id, new ResourceSpec(id, c.getItemName(), c.getCardLevel(), ResourceKind.SUPPLY, null, c.getCount()));
            id++;
        }
    }

    public Collection<CardSpec> all() {
        return specs.values();
    }

    @Override
    public CardSpec find(long cardId) {
        return specs.get(cardId);
    }
}
